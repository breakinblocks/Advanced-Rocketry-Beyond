// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.life.client.LeakMarker;
import advRocketry.processing.ProcessingFluids;
import advRocketry.util.ClientSync;
import advRocketry.util.MachineEnergy;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class OxygenBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 16000;
    public static final int ENERGY_CAPACITY = 10000;
    private static final int RESCAN_DELAY = 20;
    private static final int FALLBACK_SCAN_INTERVAL = 600;

    public boolean active;
    private static final LongOpenHashSet EMPTY_ROOM = new LongOpenHashSet();

    private LongOpenHashSet room = EMPTY_ROOM;
    private LongSet watchedChunks = LongSets.EMPTY_SET;
    private final BlockPos[] neighbors;
    private final CarbonScrubberBlockEntity[] adjacentScrubbers = new CarbonScrubberBlockEntity[6];
    private boolean updatingNeighbors;
    private boolean roomKnown;
    private boolean checkedSky;
    private BlockPos leak;
    private boolean dirty;
    private long lastScan;
    private long fallbackScan;
    private BoundingBox explored;
    public final MachineEnergy energy = MachineEnergy.consumer(ENERGY_CAPACITY, this::setChanged);
    private final ClientSync clientSync = new ClientSync();
    public final FluidTank tank =
            new FluidTank(
                    TANK_CAPACITY,
                    fluid -> fluid.is(ProcessingFluids.OXYGEN) || (charger() && fluid.is(ProcessingFluids.HYDROGEN))) {
                @Override
                protected void onContentsChanged() {
                    setChanged();
                }
            };

    public OxygenBlockEntity(BlockPos pos, BlockState state) {
        super(LifeSupportRegistry.OXYGEN_ENTITY.get(), pos, state);
        Direction[] directions = Direction.values();
        neighbors = new BlockPos[directions.length];
        for (int i = 0; i < directions.length; i++) neighbors[i] = pos.relative(directions[i]);
    }

    private boolean fillsFromTank(ItemStack armor) {
        if (armor.isEmpty() || !tank.getFluid().is(ProcessingFluids.OXYGEN)) return false;
        var handler = armor.getCapability(Capabilities.FluidHandler.ITEM);
        return handler != null && handler.fill(tank.getFluid().copy(), IFluidHandler.FluidAction.SIMULATE) > 0;
    }

    public boolean charger() {
        return getBlockState().getBlock() instanceof OxygenBlock block && block.charger;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OxygenMenu(id, inventory, this);
    }

    public BlockPos leak() {
        return leak;
    }

    public int roomSize() {
        return room.size();
    }

    public void setRoom(Set<BlockPos> room) {
        LongOpenHashSet keys = new LongOpenHashSet(room.size());
        LongSet chunks = new LongOpenHashSet();
        for (BlockPos pos : room) {
            keys.add(pos.asLong());
            for (int x = (pos.getX() - 1) >> 4; x <= (pos.getX() + 1) >> 4; x++)
                for (int z = (pos.getZ() - 1) >> 4; z <= (pos.getZ() + 1) >> 4; z++) chunks.add(ChunkPos.asLong(x, z));
        }
        checkedSky = false;
        watchedChunks = chunks;
        assignRoom(keys, BoundingBox.encapsulatingPositions(room).orElse(null));
    }

    private void assignRoom(LongOpenHashSet room, BoundingBox explored) {
        this.room = room;
        this.explored = explored;
        roomKnown = true;
        SealedRooms.register(this);
        dirty = false;
        lastScan = level == null ? 0 : level.getGameTime();
        // Distribute periodic validation across ticks when many vents power up together.
        long untilPhase =
                FALLBACK_SCAN_INTERVAL - Math.floorMod(lastScan - worldPosition.hashCode(), FALLBACK_SCAN_INTERVAL);
        fallbackScan = lastScan + (untilPhase < RESCAN_DELAY ? FALLBACK_SCAN_INTERVAL : untilPhase);
    }

    boolean supplies(BlockPos pos, long key) {
        if (!active) return false;
        if (!roomKnown) return pos.closerThan(worldPosition, AdvancedRocketryConfig.oxygenVentSize());
        return explored != null && explored.isInside(pos) && room.contains(key);
    }

    void blockChanged(BlockPos pos) {
        if (!updatingNeighbors
                && roomKnown
                && explored != null
                && pos.getX() >= explored.minX() - 1
                && pos.getX() <= explored.maxX() + 1
                && pos.getY() >= explored.minY() - 1
                && (checkedSky || pos.getY() <= explored.maxY() + 1)
                && pos.getZ() >= explored.minZ() - 1
                && pos.getZ() <= explored.maxZ() + 1) dirty = true;
    }

    LongSet watchedChunks() {
        if (roomKnown) return watchedChunks;
        int radius = AdvancedRocketryConfig.oxygenVentSize();
        var chunks = new LongOpenHashSet();
        for (int x = (worldPosition.getX() - radius) >> 4; x <= (worldPosition.getX() + radius) >> 4; x++)
            for (int z = (worldPosition.getZ() - radius) >> 4; z <= (worldPosition.getZ() + radius) >> 4; z++)
                chunks.add(ChunkPos.asLong(x, z));
        return chunks;
    }

    void chunkChanged(boolean loaded) {
        if (!loaded || room.isEmpty()) dirty = true;
    }

    private boolean scanDue() {
        if (!roomKnown) return true;
        long elapsed = level.getGameTime() - lastScan;
        return elapsed < 0 || level.getGameTime() >= fallbackScan || dirty && elapsed >= RESCAN_DELAY;
    }

    private void scanRoom() {
        var parents = new Long2ByteOpenHashMap();
        LongOpenHashSet leaked = new LongOpenHashSet();
        SealedRooms.Scan found = null;
        SealedRooms.Scan worst = null;
        BoundingBox box = null;
        var reader = new SealedRooms.Reader(level);
        for (BlockPos origin : neighbors) {
            SealedRooms.Scan scan = SealedRooms.scan(reader, origin, worldPosition, parents, leaked);
            if (scan.explored() != null) box = box == null ? scan.explored() : box.encapsulate(scan.explored());
            if (!scan.room().isEmpty()) {
                found = scan;
                break;
            }
            if (scan.leaked() && (worst == null || rank(scan) > rank(worst))) worst = scan;
        }
        leak = found == null && worst != null ? worst.leak() : null;
        checkedSky = reader.checkedSky();
        watchedChunks = reader.watchedChunks();
        assignRoom(found == null ? new LongOpenHashSet() : found.room(), box);
    }

    private static long rank(SealedRooms.Scan scan) {
        return (scan.gap() != null ? 1L << 40 : 0) + scan.visited();
    }

    private void forgetRoom() {
        SealedRooms.remove(this);
        watchedChunks = LongSets.EMPTY_SET;
        room = EMPTY_ROOM;
        roomKnown = false;
        dirty = false;
        explored = null;
    }

    public void tick() {
        if (level == null) return;
        clientSync.update(
                this, energy.getEnergyStored() + 31L * (leak == null ? 0 : leak.hashCode()) + (active ? 7 : 0));
        if (charger()) {
            for (var player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).expandTowards(0, 2, 0))) {
                var armor = player.getItemBySlot(EquipmentSlot.CHEST);
                if (armor.getItem() instanceof SpaceSuitItem suit) {
                    int moved = suit.transferOxygen(armor, tank.getFluid().copy(), true);
                    tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
                } else if (!level.isClientSide && fillsFromTank(armor)) {
                    var handler = armor.getCapability(Capabilities.FluidHandler.ITEM);
                    int moved = handler.fill(tank.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
                    tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
                    if (moved > 0) player.setItemSlot(EquipmentSlot.CHEST, handler.getContainer());
                } else if (tank.getFluid().is(ProcessingFluids.OXYGEN) && SpaceBreathing.sealed(level, armor)) {
                    int moved = SpaceBreathing.fill(armor, tank.getFluidAmount());
                    tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            return;
        }
        LongOpenHashSet previousRoom = active ? room : EMPTY_ROOM;
        int scrubbers = 0;
        int adjacent = 0;
        for (BlockPos next : neighbors) {
            if (level.getBlockEntity(next) instanceof CarbonScrubberBlockEntity scrubber) {
                adjacentScrubbers[adjacent++] = scrubber;
                if (scrubber.canScrub()) scrubbers++;
            }
        }
        int power = (int) Math.ceil((1 + scrubbers * 10) * AdvancedRocketryConfig.oxygenVentPowerMultiplier());
        boolean powered = energy.getEnergyStored() >= power && !level.hasNeighborSignal(worldPosition);
        if (powered) {
            if (scanDue()) scanRoom();
        } else if (roomKnown) forgetRoom();
        int consumption = (int) Math.ceil(room.size()
                * Math.max(.01 - scrubbers * .005, 0)
                * AdvancedRocketryConfig.oxygenVentConsumptionMultiplier());
        boolean previous = active;
        active = powered && !room.isEmpty() && tank.getFluidAmount() >= consumption;
        if (!previousRoom.isEmpty() && (!active || previousRoom != room) && level instanceof ServerLevel serverLevel)
            LightingRegistry.atmosphereLost(serverLevel, previousRoom, active ? room : EMPTY_ROOM);
        if (active != previous) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        for (int i = 0; i < adjacent; i++) {
            var scrubber = adjacentScrubbers[i];
            adjacentScrubbers[i] = null;
            if (scrubber.isRemoved()) continue;
            if (active && AdvancedRocketryConfig.scrubberRequiresCartridge() && level.getGameTime() % 200 == 0)
                scrubber.useCharge();
            boolean scrubbing = active && scrubber.canScrub();
            BlockState state = scrubber.getBlockState();
            if (state.getValue(CarbonScrubberBlock.POWERED) != scrubbing) {
                updatingNeighbors = true;
                try {
                    level.setBlock(scrubber.getBlockPos(), state.setValue(CarbonScrubberBlock.POWERED, scrubbing), 3);
                } finally {
                    updatingNeighbors = false;
                }
            }
        }
        if (active) {
            energy.consume(power, false);
            tank.drain(consumption, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public void tickClient() {
        if (level == null || charger()) return;
        if (!active) {
            if (leak != null) LeakMarker.tick(this, leak);
            return;
        }
        if (level.getGameTime() % 85 != Math.floorMod(worldPosition.hashCode(), 85)) return;
        level.playLocalSound(
                worldPosition.getX() + .5,
                worldPosition.getY() + .5,
                worldPosition.getZ() + .5,
                LifeSupportRegistry.OXYGEN_VENT_SOUND.get(),
                SoundSource.BLOCKS,
                .5f,
                1f,
                false);
    }

    public void decommission() {
        active = false;
        SealedRooms.remove(this);
        if (level instanceof ServerLevel serverLevel) LightingRegistry.atmosphereLost(serverLevel, room, EMPTY_ROOM);
        forgetRoom();
        if (level == null) return;
        for (Direction direction : Direction.values()) {
            BlockPos next = worldPosition.relative(direction);
            BlockState state = level.getBlockState(next);
            if (state.is(LifeSupportRegistry.CARBON_SCRUBBER.get()) && state.getValue(CarbonScrubberBlock.POWERED))
                level.setBlock(next, state.setValue(CarbonScrubberBlock.POWERED, false), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (roomKnown) {
            dirty = true;
            SealedRooms.register(this);
        }
    }

    @Override
    public void setRemoved() {
        SealedRooms.remove(this);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
        tag.putBoolean("active", active);
        if (leak != null) tag.putLong("leak", leak.asLong());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("energy"));
        active = tag.getBoolean("active");
        leak = tag.contains("leak") ? BlockPos.of(tag.getLong("leak")) : null;
        tank.readFromNBT(registries, tag.getCompound("tank"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

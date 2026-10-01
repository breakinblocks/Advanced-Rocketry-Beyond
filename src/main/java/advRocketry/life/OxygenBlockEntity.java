// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.processing.ProcessingFluids;
import advRocketry.util.MachineEnergy;
import java.util.Collection;
import java.util.HashSet;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class OxygenBlockEntity extends BlockEntity implements MenuProvider {
  public static final int TANK_CAPACITY = 16000;
  public static final int ENERGY_CAPACITY = 10000;
  private static final int RESCAN_DELAY = 20;
  private static final int FALLBACK_SCAN_INTERVAL = 600;

  public boolean active;
  private Set<BlockPos> room = Set.of();
  private boolean roomKnown;
  private boolean dirty;
  private long lastScan;
  private BoundingBox explored;
  public final MachineEnergy energy = MachineEnergy.consumer(ENERGY_CAPACITY, this::setChanged);
  public final FluidTank tank =
      new FluidTank(
          TANK_CAPACITY,
          fluid ->
              fluid.is(ProcessingFluids.oxygen())
                  || (charger() && fluid.is(ProcessingFluids.hydrogen()))) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };

  public OxygenBlockEntity(BlockPos pos, BlockState state) {
    super(LifeSupportRegistry.OXYGEN_ENTITY.get(), pos, state);
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

  public Set<BlockPos> room() {
    return room;
  }

  public void setRoom(Set<BlockPos> room) {
    assignRoom(room, BoundingBox.encapsulatingPositions(room).orElse(null));
  }

  private void assignRoom(Set<BlockPos> room, BoundingBox explored) {
    this.room = room;
    this.explored = explored;
    roomKnown = true;
    dirty = false;
    lastScan = level == null ? 0 : level.getGameTime();
  }

  boolean supplies(BlockPos pos) {
    if (!active) return false;
    if (!roomKnown) return pos.closerThan(worldPosition, AdvancedRocketryConfig.oxygenVentSize());
    return explored != null && explored.isInside(pos) && room.contains(pos);
  }

  void blockChanged(BlockPos pos) {
    if (roomKnown
        && explored != null
        && pos.getX() >= explored.minX() - 1
        && pos.getX() <= explored.maxX() + 1
        && pos.getY() >= explored.minY() - 1
        && pos.getY() <= explored.maxY() + 1
        && pos.getZ() >= explored.minZ() - 1
        && pos.getZ() <= explored.maxZ() + 1) dirty = true;
  }

  void chunkLoaded(ChunkPos chunk) {
    if (roomKnown
        && room.isEmpty()
        && explored != null
        && chunk.getMinBlockX() <= explored.maxX()
        && chunk.getMaxBlockX() >= explored.minX()
        && chunk.getMinBlockZ() <= explored.maxZ()
        && chunk.getMaxBlockZ() >= explored.minZ()) dirty = true;
  }

  private boolean scanDue() {
    if (!roomKnown) return true;
    long elapsed = level.getGameTime() - lastScan;
    return elapsed < 0 || elapsed >= (dirty ? RESCAN_DELAY : FALLBACK_SCAN_INTERVAL);
  }

  private void scanRoom() {
    Set<BlockPos> seen = new HashSet<>();
    Set<BlockPos> found = Set.of();
    for (Direction direction : Direction.values()) {
      BlockPos origin = worldPosition.relative(direction);
      if (seen.contains(origin)) continue;
      found = SealedRooms.scan(level, origin, worldPosition, seen);
      if (!found.isEmpty()) break;
    }
    assignRoom(found, BoundingBox.encapsulatingPositions(seen).orElse(null));
  }

  private void forgetRoom() {
    room = Set.of();
    roomKnown = false;
    dirty = false;
    explored = null;
  }

  public void tick() {
    if (level == null) return;
    if (charger()) {
      for (var player :
          level.getEntitiesOfClass(Player.class, new AABB(worldPosition).expandTowards(0, 2, 0))) {
        var armor = player.getItemBySlot(EquipmentSlot.CHEST);
        if (armor.getItem() instanceof SpaceSuitItem suit) {
          int moved = suit.transferOxygen(armor, tank.getFluid().copy(), true);
          tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
        } else if (tank.getFluid().is(ProcessingFluids.oxygen())
            && SpaceBreathing.sealed(level, armor)) {
          int moved = SpaceBreathing.fill(armor, tank.getFluidAmount());
          tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
        }
      }
      return;
    }
    SealedRooms.register(this);
    Set<BlockPos> previousRoom = active ? room : Set.of();
    int scrubbers = 0;
    for (Direction direction : Direction.values()) {
      if (level.getBlockEntity(worldPosition.relative(direction))
              instanceof CarbonScrubberBlockEntity scrubber
          && scrubber.canScrub()) {
        scrubbers++;
      }
    }
    int power =
        (int) Math.ceil((1 + scrubbers * 10) * AdvancedRocketryConfig.oxygenVentPowerMultiplier());
    boolean powered = energy.getEnergyStored() >= power && !level.hasNeighborSignal(worldPosition);
    if (!powered) forgetRoom();
    else if (scanDue()) scanRoom();
    int consumption =
        (int)
            Math.ceil(
                room.size()
                    * Math.max(.01 - scrubbers * .005, 0)
                    * AdvancedRocketryConfig.oxygenVentConsumptionMultiplier());
    boolean previous = active;
    active = powered && !room.isEmpty() && tank.getFluidAmount() >= consumption;
    if (!previousRoom.isEmpty()
        && (!active || previousRoom != room)
        && level instanceof ServerLevel serverLevel)
      LightingRegistry.atmosphereLost(serverLevel, active ? lost(previousRoom) : previousRoom);
    if (active != previous) {
      setChanged();
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    for (Direction direction : Direction.values()) {
      BlockPos next = worldPosition.relative(direction);
      BlockState state = level.getBlockState(next);
      if (state.is(LifeSupportRegistry.CARBON_SCRUBBER.get())) {
        if (active
            && AdvancedRocketryConfig.scrubberRequiresCartridge()
            && level.getGameTime() % 200 == 0
            && level.getBlockEntity(next) instanceof CarbonScrubberBlockEntity scrubber)
          scrubber.useCharge();
        boolean scrubbing =
            active
                && level.getBlockEntity(next) instanceof CarbonScrubberBlockEntity scrubber
                && scrubber.canScrub();
        if (state.getValue(CarbonScrubberBlock.POWERED) != scrubbing)
          level.setBlock(next, state.setValue(CarbonScrubberBlock.POWERED, scrubbing), 3);
      }
    }
    if (active) {
      energy.consume(power, false);
      tank.drain(consumption, IFluidHandler.FluidAction.EXECUTE);
    }
  }

  private Collection<BlockPos> lost(Set<BlockPos> previousRoom) {
    return previousRoom.stream().filter(pos -> !room.contains(pos)).toList();
  }

  public void tickClient() {
    if (level == null || charger() || !active) return;
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
    if (level instanceof ServerLevel serverLevel)
      LightingRegistry.atmosphereLost(serverLevel, room);
    forgetRoom();
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
    tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    energy.setEnergy(tag.getInt("energy"));
    active = tag.getBoolean("active");
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

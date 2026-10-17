// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;

/** Passive saved configuration; only capability-connected endpoints receive a ticker. */
public final class TransportBlockEntity extends BlockEntity implements MenuProvider {
  public static final int INSERT = 0, EXTRACT = 1, DISABLED = 2;
  public final int[] modes = new int[6];
  private final Map<Direction, BlockCapabilityCache<?, Direction>> capabilities =
      new EnumMap<>(Direction.class);
  private boolean available = true;

  private <T> BlockCapabilityCache<T, Direction> cache(
      BlockCapability<T, Direction> capability, Direction side) {
    return BlockCapabilityCache.create(
        capability,
        (ServerLevel) level,
        worldPosition.relative(side),
        side.getOpposite(),
        () -> available && !isRemoved(),
        () -> TransportNetworks.capabilityChanged(this));
  }

  Object endpointCapability(Direction side) {
    return capabilities
        .computeIfAbsent(
            side,
            direction ->
                switch (kind()) {
                  case ENERGY -> cache(Capabilities.EnergyStorage.BLOCK, direction);
                  case FLUID -> cache(Capabilities.FluidHandler.BLOCK, direction);
                  case ITEM -> cache(Capabilities.ItemHandler.BLOCK, direction);
                })
        .getCapability();
  }

  public final TransportSideConfig[] sides = new TransportSideConfig[6];
  public int channel = -1;
  public long sensorUntil;
  public long lastTransferTick;
  public int lastTransferAmount;
  public int networkSize;
  public int blockedReason;
  public boolean connectionsDirty = true;
  private long powerTick = Long.MIN_VALUE;
  private boolean power;

  public TransportSideConfig side(Direction side) {
    return sides[side.ordinal()];
  }

  public void wake() {
    powerTick = Long.MIN_VALUE;
    for (var side : sides) side.retry.wake();
  }

  public boolean gate(int mode) {
    return mode == 0 || powered() == (mode == 1);
  }

  public boolean extracting(Direction side) {
    return extracting() && gate(side(side).extractRedstone);
  }

  public boolean inserting(Direction side) {
    return gate(side(side).insertRedstone);
  }

  public boolean accepts(Direction side, ItemStack stack) {
    return side(side).configured ? side(side).accepts(stack) : accepts(stack);
  }

  public void transferred(Direction side, int amount) {
    lastTransferAmount =
        lastTransferTick == level.getGameTime() ? lastTransferAmount + amount : amount;
    lastTransferTick = level.getGameTime();
    blockedReason = 0;
    if (side(side).sensor) {
      boolean on = sensorUntil > level.getGameTime();
      sensorUntil = level.getGameTime() + 20;
      if (!on) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
      level.scheduleTick(worldPosition, getBlockState().getBlock(), 20);
    }
  }

  public boolean roundRobin;
  public int redstone;
  public int cursor;
  public int sourceCursor;
  public ItemStack filter = ItemStack.EMPTY;
  public ItemStack pendingItem = ItemStack.EMPTY;
  public FluidStack pendingFluid = FluidStack.EMPTY;
  public Fluid lock = Fluids.EMPTY;
  public int pendingEnergy;
  public long activeUntil;
  long budgetTick = Long.MIN_VALUE;
  int spent;
  final int[] energyIn = new int[6];
  final int[] energyOut = new int[6];
  long energyTick = Long.MIN_VALUE;

  void resetEnergyBudget() {
    if (energyTick != level.getGameTime()) {
      energyTick = level.getGameTime();
      Arrays.fill(energyIn, 0);
      Arrays.fill(energyOut, 0);
    }
  }

  public TransportBlockEntity(BlockPos pos, BlockState state) {
    super(TransportRegistry.ENTITY.get(), pos, state);
    Arrays.setAll(sides, index -> new TransportSideConfig());
  }

  public TransportRegistry.Kind kind() {
    return ((TransportBlock) getBlockState().getBlock()).kind;
  }

  public boolean accepts(ItemStack stack) {
    return filter.isEmpty() || ItemStack.isSameItemSameComponents(filter, stack);
  }

  public boolean extracting() {
    return redstone == 0 || powered() == (redstone == 1);
  }

  private boolean powered() {
    if (powerTick != level.getGameTime()) {
      power = scanPower();
      powerTick = level.getGameTime();
    }
    return power;
  }

  private boolean scanPower() {
    for (Direction side : Direction.values()) {
      BlockPos neighbor = worldPosition.relative(side);
      if (!level.hasChunkAt(neighbor)) continue;
      BlockState state = level.getBlockState(neighbor);
      if (state.getSignal(level, neighbor, side) > 0) return true;
      if (!state.isRedstoneConductor(level, neighbor)) continue;
      for (Direction sourceSide : Direction.values()) {
        BlockPos source = neighbor.relative(sourceSide);
        if (!source.equals(worldPosition)
            && level.hasChunkAt(source)
            && level.getBlockState(source).getDirectSignal(level, source, sourceSide) > 0)
          return true;
      }
    }
    return false;
  }

  public void policyChanged() {
    setChanged();
    TransportNetworks.routesChanged(this, true);
  }

  public void changed() {
    wake();
    connectionsDirty = true;
    setChanged();
    TransportNetworks.invalidate(level, worldPosition);
    refreshConnections();
  }

  public void refreshConnections() {
    if (level == null || level.isClientSide || isRemoved()) return;
    connectionsDirty = false;
    BlockState old = getBlockState(), state = old;
    boolean endpoint = false;
    for (Direction direction : Direction.values()) {
      BlockPos next = worldPosition.relative(direction);
      boolean connected = false;
      if (modes[direction.ordinal()] != DISABLED) {
        if (level.hasChunkAt(next)
            && level.getBlockEntity(next) instanceof TransportBlockEntity other)
          connected =
              other.kind() == kind()
                  && other.channel == channel
                  && other.modes[direction.getOpposite().ordinal()] != DISABLED;
        else if (TransportNetworks.capability(this, direction) != null) {
          connected = true;
          endpoint = true;
        }
      }
      state = state.setValue(TransportBlock.SIDES[direction.ordinal()], connected);
    }
    state =
        state.setValue(
            TransportBlock.ENDPOINT,
            endpoint || !pendingItem.isEmpty() || !pendingFluid.isEmpty() || pendingEnergy > 0);
    if (!state.equals(old))
      level.setBlock(worldPosition, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    available = true;
    connectionsDirty = true;
    wake();
    capabilities.clear();
    if (level != null && !level.isClientSide) {
      TransportNetworks.invalidate(level, worldPosition);
      level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }
  }

  @Override
  public void onChunkUnloaded() {
    available = false;
    capabilities.clear();
    TransportNetworks.invalidate(level, worldPosition);
    super.onChunkUnloaded();
  }

  @Override
  public void setRemoved() {
    available = false;
    capabilities.clear();
    TransportNetworks.invalidate(level, worldPosition);
    super.setRemoved();
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putIntArray("sides", modes);
    tag.putInt("channel", channel);
    var configs = new ListTag();
    for (var side : sides) configs.add(side.save(registries));
    tag.put("side_config", configs);
    tag.putBoolean("roundRobin", roundRobin);
    tag.putInt("redstone", redstone);
    tag.putInt("cursor", cursor);
    tag.putInt("sourceCursor", sourceCursor);
    if (!filter.isEmpty()) tag.put("filter", filter.save(registries));
    if (!pendingItem.isEmpty()) tag.put("item", pendingItem.save(registries));
    if (!pendingFluid.isEmpty()) tag.put("fluid", pendingFluid.save(registries));
    tag.putString("lock", BuiltInRegistries.FLUID.getKey(lock).toString());
    tag.putInt("energy", pendingEnergy);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    Arrays.fill(modes, INSERT);
    int[] saved = tag.getIntArray("sides");
    for (int i = 0; i < Math.min(6, saved.length); i++) modes[i] = Math.clamp(saved[i], 0, 2);
    channel = tag.contains("channel") ? Math.clamp(tag.getInt("channel"), -1, 15) : -1;
    var configs = tag.getList("side_config", 10);
    for (int i = 0; i < 6; i++)
      sides[i] =
          i < configs.size()
              ? TransportSideConfig.load(configs.getCompound(i), registries)
              : new TransportSideConfig();
    wake();
    roundRobin = tag.getBoolean("roundRobin");
    redstone = Math.clamp(tag.getInt("redstone"), 0, 2);
    cursor = Math.max(0, tag.getInt("cursor"));
    sourceCursor = Math.max(0, tag.getInt("sourceCursor"));
    filter = ItemStack.parseOptional(registries, tag.getCompound("filter"));
    pendingItem = ItemStack.parseOptional(registries, tag.getCompound("item"));
    pendingFluid = FluidStack.parseOptional(registries, tag.getCompound("fluid"));
    ResourceLocation key = ResourceLocation.tryParse(tag.getString("lock"));
    lock = key == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(key);
    pendingEnergy = Math.clamp(tag.getInt("energy"), 0, 5000);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.adv_rocketry." + kind().id);
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new TransportMenu(id, inventory, this);
  }
}

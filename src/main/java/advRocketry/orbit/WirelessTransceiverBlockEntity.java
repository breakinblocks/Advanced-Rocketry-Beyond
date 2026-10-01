// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Persistent original 100-unit typed buffers, connected through the native data-bus inventory. */
public final class WirelessTransceiverBlockEntity extends BlockEntity {
  public enum Mode {
    OFF,
    SOURCE,
    SINK
  }

  private static final List<ResearchType> TYPES = ResearchType.TRANSCEIVER_TYPES;
  private static final Set<WirelessTransceiverBlockEntity> LOADED =
      Collections.newSetFromMap(new WeakHashMap<>());
  private static long generation;
  private static Map<Long, List<WirelessTransceiverBlockEntity>> sources;
  private final Map<ResearchType, Integer> data = new LinkedHashMap<>();
  private Mode mode = Mode.OFF;
  private long cachedNetwork;
  private long cachedGeneration = -1;

  public WirelessTransceiverBlockEntity(BlockPos pos, BlockState state) {
    super(OrbitalRegistry.WIRELESS_ENTITY.get(), pos, state);
  }

  public Mode mode() {
    return mode;
  }

  public void cycleMode() {
    mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
    invalidate();
    setChanged();
  }

  private static void invalidate() {
    generation++;
    sources = null;
  }

  public String positionKey() {
    return level.dimension().location() + "/" + worldPosition.asLong();
  }

  public long network() {
    if (!(level instanceof ServerLevel serverLevel)) return 0;
    if (cachedGeneration != generation) {
      cachedNetwork = GalaxyData.get(serverLevel.getServer()).wirelessNetwork(positionKey());
      cachedGeneration = generation;
    }
    return cachedNetwork;
  }

  private static List<WirelessTransceiverBlockEntity> sources(long network) {
    if (sources == null) {
      sources = new HashMap<>();
      for (WirelessTransceiverBlockEntity entity : LOADED)
        if (!entity.isRemoved() && entity.mode == Mode.SOURCE && entity.network() != 0)
          sources.computeIfAbsent(entity.network(), key -> new ArrayList<>()).add(entity);
    }
    return sources.getOrDefault(network, List.of());
  }

  public static boolean link(
      WirelessTransceiverBlockEntity first, WirelessTransceiverBlockEntity second) {
    if (first == second
        || !(first.level instanceof ServerLevel left)
        || !(second.level instanceof ServerLevel right)
        || left.getServer() != right.getServer()) return false;
    GalaxyData.get(left.getServer()).linkWireless(first.positionKey(), second.positionKey());
    LOADED.add(first);
    LOADED.add(second);
    invalidate();
    return true;
  }

  public void unlink() {
    if (level instanceof ServerLevel serverLevel)
      GalaxyData.get(serverLevel.getServer()).removeWireless(positionKey());
    invalidate();
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level instanceof ServerLevel) LOADED.add(this);
    invalidate();
  }

  @Override
  public void onChunkUnloaded() {
    LOADED.remove(this);
    invalidate();
    super.onChunkUnloaded();
  }

  @Override
  public void setRemoved() {
    LOADED.remove(this);
    invalidate();
    super.setRemoved();
  }

  private ItemStackHandler adjacentData() {
    if (level == null) return null;
    BlockPos neighbor =
        worldPosition.relative(
            getBlockState().getValue(WirelessTransceiverBlock.FACING).getOpposite());
    BlockEntity entity = level.getBlockEntity(neighbor);
    if (entity instanceof PortBlockEntity port && port.kind() == MachinePorts.Kind.DATA_BUS)
      return port.inventory;
    return entity instanceof OrbitalBlockEntity machine ? machine.inventory : null;
  }

  private void pull() {
    ItemStackHandler inventory = adjacentData();
    if (inventory == null) return;
    for (int slot = 0; slot < inventory.getSlots(); slot++) {
      ItemStack unit = inventory.getStackInSlot(slot);
      if (!unit.is(OrbitalRegistry.DATA_UNIT.get())) continue;
      StoredResearch research = DataUnitItem.research(unit);
      ResearchType type = research.type();
      if (type == null || !TYPES.contains(type)) continue;
      int moved = Math.min(research.amount(), 100 - data.getOrDefault(type, 0));
      if (moved <= 0) continue;
      data.merge(type, moved, Integer::sum);
      ItemStack updated = unit.copy();
      DataUnitItem.store(updated, type, research.amount() - moved);
      inventory.setStackInSlot(slot, updated);
      setChanged();
    }
  }

  private void receive() {
    long network = network();
    if (network == 0) return;
    for (WirelessTransceiverBlockEntity source : sources(network)) {
      if (source == this
          || source.isRemoved()
          || source.mode != Mode.SOURCE
          || !(source.level instanceof ServerLevel sourceLevel)
          || sourceLevel.getServer() != level.getServer()) continue;
      for (ResearchType type : TYPES) {
        int moved = Math.min(source.data.getOrDefault(type, 0), 100 - data.getOrDefault(type, 0));
        if (moved <= 0) continue;
        source.data.put(type, source.data.get(type) - moved);
        data.merge(type, moved, Integer::sum);
        source.setChanged();
        setChanged();
      }
    }
  }

  private void push() {
    ItemStackHandler inventory = adjacentData();
    if (inventory == null) return;
    for (ResearchType type : TYPES) {
      int stored = data.getOrDefault(type, 0);
      if (stored <= 0) continue;
      for (int slot = 0; slot < inventory.getSlots() && stored > 0; slot++) {
        ItemStack unit = inventory.getStackInSlot(slot);
        if (!unit.is(OrbitalRegistry.DATA_UNIT.get())) continue;
        StoredResearch research = DataUnitItem.research(unit);
        if (!research.empty() && research.type() != type) continue;
        int moved = Math.min(stored, ResearchType.UNIT_CAPACITY - research.amount());
        if (moved <= 0) continue;
        ItemStack updated = unit.copy();
        DataUnitItem.store(updated, type, research.amount() + moved);
        inventory.setStackInSlot(slot, updated);
        stored -= moved;
        setChanged();
      }
      data.put(type, stored);
    }
  }

  public void tick() {
    if (!(level instanceof ServerLevel)) return;
    if (mode == Mode.SOURCE) pull();
    else if (mode == Mode.SINK) {
      receive();
      push();
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putString("mode", mode.name());
    CompoundTag buffers = new CompoundTag();
    data.forEach((type, amount) -> buffers.putInt(type.getSerializedName(), amount));
    tag.put("data", buffers);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    try {
      mode = Mode.valueOf(tag.getString("mode"));
    } catch (IllegalArgumentException ignored) {
      mode = Mode.OFF;
    }
    invalidate();
    data.clear();
    CompoundTag buffers = tag.getCompound("data");
    for (ResearchType type : TYPES)
      if (buffers.contains(type.getSerializedName()))
        data.put(type, Math.clamp(buffers.getInt(type.getSerializedName()), 0, 100));
  }
}

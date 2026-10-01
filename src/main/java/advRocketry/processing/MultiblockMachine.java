// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.multiblock.Multiblock;
import advRocketry.multiblock.PlacedMultiblock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Structure coordinates follow the original MIT LibVulpes TileMultiBlock convention. */
public abstract class MultiblockMachine extends BlockEntity {
  private static final Map<Level, Map<BlockPos, BlockPos>> OWNERS = new WeakHashMap<>();
  private final List<BlockPos> parts = new ArrayList<>();
  private final List<BlockPos> ports = new ArrayList<>();

  protected MultiblockMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  protected abstract String multiblockId();

  public abstract boolean hideParts();

  public PlacedMultiblock structure() {
    return PlacedMultiblock.of(this, multiblockId());
  }

  public boolean structureChunksLoaded() {
    PlacedMultiblock structure = structure();
    return structure != null && structure.loaded();
  }

  public boolean scanStructure() {
    if (level == null || level.isClientSide || !structureChunksLoaded()) return false;
    List<BlockPos> foundParts = new ArrayList<>();
    List<BlockPos> foundPorts = new ArrayList<>();
    for (Multiblock.Placed cell : structure().cells()) {
      BlockPos pos = cell.pos();
      BlockState actual = level.getBlockState(pos);
      var owners = OWNERS.computeIfAbsent(level, ignored -> new HashMap<>());
      BlockPos owner = owners.get(pos);
      if (owner != null && !owner.equals(worldPosition)) {
        if (!level.isLoaded(owner) || level.getBlockEntity(owner) instanceof MultiblockMachine) {
          unform();
          return false;
        }
        owners.remove(pos);
      }
      if (!cell.key().test(actual)) {
        unform();
        return false;
      }
      if (!pos.equals(worldPosition)) foundParts.add(pos);
      if (level.getBlockEntity(pos) instanceof PortBlockEntity) foundPorts.add(pos);
    }
    parts.clear();
    parts.addAll(foundParts);
    ports.clear();
    ports.addAll(foundPorts);
    if (!getBlockState().getValue(ProcessingBlock.FORMED)) {
      level.setBlock(
          worldPosition,
          getBlockState().setValue(ProcessingBlock.FORMED, true),
          Block.UPDATE_CLIENTS);
    }
    for (BlockPos pos : parts) {
      OWNERS.get(level).put(pos, worldPosition);
      BlockState state = level.getBlockState(pos);
      if (state.hasProperty(MachinePartBlock.ASSEMBLED)
          && state.getValue(MachinePartBlock.ASSEMBLED) != hideParts()) {
        level.setBlock(
            pos, state.setValue(MachinePartBlock.ASSEMBLED, hideParts()), Block.UPDATE_CLIENTS);
      }
      if (hideParts() && state.hasProperty(MachinePartBlock.ASSEMBLED))
        level.scheduleTick(pos, state.getBlock(), 40);
    }
    return true;
  }

  public void unform() {
    if (level == null || level.isClientSide) return;
    for (BlockPos pos : parts) {
      OWNERS.computeIfAbsent(level, ignored -> new HashMap<>()).remove(pos, worldPosition);
      if (!level.isLoaded(pos)) continue;
      BlockState state = level.getBlockState(pos);
      if (state.hasProperty(MachinePartBlock.ASSEMBLED)
          && state.getValue(MachinePartBlock.ASSEMBLED)) {
        level.setBlock(
            pos, state.setValue(MachinePartBlock.ASSEMBLED, false), Block.UPDATE_CLIENTS);
      }
    }
    BlockState controller = level.getBlockState(worldPosition);
    if (controller.is(getBlockState().getBlock())
        && controller.hasProperty(ProcessingBlock.FORMED)
        && controller.getValue(ProcessingBlock.FORMED)) {
      level.setBlock(
          worldPosition, controller.setValue(ProcessingBlock.FORMED, false), Block.UPDATE_CLIENTS);
    }
    ports.clear();
    parts.clear();
  }

  public static boolean isPartClaimed(Level level, BlockPos pos) {
    BlockPos owner = OWNERS.getOrDefault(level, Map.of()).get(pos);
    if (owner == null) return false;
    if (!level.isLoaded(owner)) return true;
    return level.getBlockEntity(owner) instanceof MultiblockMachine machine
        && machine.getBlockState().getValue(ProcessingBlock.FORMED);
  }

  public List<BlockPos> menuPorts() {
    return List.copyOf(ports);
  }

  private List<PortBlockEntity> ports(MachinePorts.Kind kind) {
    List<PortBlockEntity> result = new ArrayList<>();
    for (BlockPos pos : ports) {
      if (level.isLoaded(pos)
          && level.getBlockEntity(pos) instanceof PortBlockEntity port
          && port.kind() == kind) result.add(port);
    }
    return result;
  }

  public List<PortBlockEntity> getItemInTiles() {
    return ports(MachinePorts.Kind.ITEM_INPUT);
  }

  public List<PortBlockEntity> getItemOutTiles() {
    return ports(MachinePorts.Kind.ITEM_OUTPUT);
  }

  public List<PortBlockEntity> getFluidInTiles() {
    return ports(MachinePorts.Kind.FLUID_INPUT);
  }

  public List<PortBlockEntity> getFluidOutTiles() {
    return ports(MachinePorts.Kind.FLUID_OUTPUT);
  }

  public List<PortBlockEntity> getEnergyInputTiles() {
    List<PortBlockEntity> power = ports(MachinePorts.Kind.ENERGY_INPUT);
    power.addAll(ports(MachinePorts.Kind.CREATIVE_ENERGY_INPUT));
    return power;
  }

  public long getTotalEnergyStored(List<PortBlockEntity> ports) {
    return ports.stream().mapToLong(port -> port.energyStorage.getEnergyStored()).sum();
  }

  public void consumeEnergy(int amount, List<PortBlockEntity> ports) {
    int remaining = amount;
    for (var port : ports) remaining -= port.energyStorage.consume(remaining, false);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide) scanStructure();
  }
}

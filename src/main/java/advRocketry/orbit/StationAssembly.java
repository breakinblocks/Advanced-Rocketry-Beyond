// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.processing.MachinePorts;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.util.AssemblyFailure;
import advRocketry.util.Texts;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Original station-assembler pad bounds, converted to a packed native block palette. */
public final class StationAssembly {
  private StationAssembly() {}

  public static void pack(RocketBlockEntity builder, ServerPlayer player) {
    if (!(builder.getLevel() instanceof ServerLevel level)) return;
    try {
      BlockPos pad = null;
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        BlockPos candidate = builder.getBlockPos().relative(direction).below();
        if (level.getBlockState(candidate).is(RocketRegistry.PARTS.get("launch_pad").get())) {
          pad = candidate;
          break;
        }
      }
      if (pad == null)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.place_the_station_builder_beside_a"));
      Set<BlockPos> pads = new HashSet<>();
      ArrayDeque<BlockPos> pending = new ArrayDeque<>();
      pending.add(pad);
      while (!pending.isEmpty()) {
        BlockPos position = pending.removeFirst();
        if (pads.contains(position)
            || !level.getBlockState(position).is(RocketRegistry.PARTS.get("launch_pad").get()))
          continue;
        pads.add(position);
        if (pads.size() > 256)
          throw new AssemblyFailure(
              Component.translatable(
                  "message.adv_rocketry.station_assembly.launch_pad_exceeds_16_by_16"));
        for (Direction direction : Direction.Plane.HORIZONTAL)
          pending.add(position.relative(direction));
      }
      int minX = pads.stream().mapToInt(BlockPos::getX).min().orElseThrow();
      int maxX = pads.stream().mapToInt(BlockPos::getX).max().orElseThrow();
      int minZ = pads.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
      int maxZ = pads.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
      int width = maxX - minX + 1, depth = maxZ - minZ + 1;
      if (width > 16 || depth > 16 || pads.size() != width * depth)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.launch_pad_must_be_a_solid"));
      int height = 0;
      for (int x = minX - 1; x <= maxX + 1; x++)
        for (int z = minZ - 1; z <= maxZ + 1; z++) {
          if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) continue;
          int tower = 0;
          while (tower < 64
              && level
                  .getBlockState(new BlockPos(x, pad.getY() + 1 + tower, z))
                  .is(MachinePorts.STRUCTURE_TOWER.get())) tower++;
          height = Math.max(height, tower);
        }
      if (height < 4)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.a_structure_tower_at_least_four"));
      BlockPos origin = new BlockPos(minX, pad.getY() + 1, minZ);
      List<BlockPos> captured = new ArrayList<>();
      boolean hatch = false;
      for (int y = 0; y < height; y++)
        for (int z = 0; z < depth; z++)
          for (int x = 0; x < width; x++) {
            BlockPos position = origin.offset(x, y, z);
            if (!level.hasChunkAt(position))
              throw new AssemblyFailure(
                  Component.translatable(
                      "message.adv_rocketry.station_assembly.station_extends_into_unloaded_chunks"));
            BlockState state = level.getBlockState(position);
            if (state.isAir() || state.canBeReplaced()) continue;
            if (state.getDestroySpeed(level, position) < 0)
              throw new AssemblyFailure(
                  Component.translatable(
                      "message.adv_rocketry.station_assembly.station_contains_an_unmovable_block"));
            hatch |= state.is(OrbitalRegistry.HATCH.get());
            captured.add(position);
          }
      if (!hatch)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.station_module_requires_a_satellite_hatch"));
      if (!builder.stationInventory.getStackInSlot(0).is(OrbitalRegistry.HATCH.get().asItem())
          || !builder.stationInventory.getStackInSlot(1).is(OrbitalRegistry.STATION_CHIP.get())
          || builder.stationInventory.getStackInSlot(1).has(ModComponents.STATION_LINK)
          || !builder.stationInventory.getStackInSlot(2).isEmpty()
          || !builder.stationInventory.getStackInSlot(3).isEmpty())
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.supply_a_satellite_hatch_and_blank"));
      if (builder.energy.consume(1000, true) < 1000)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.station_assembly.station_packing_requires_1_000_fe"));
      for (BlockPos position : captured) {
        if (!level.mayInteract(player, position)
            || NeoForge.EVENT_BUS
                .post(
                    new BlockEvent.BreakEvent(
                        level, position, level.getBlockState(position), player))
                .isCanceled())
          throw new AssemblyFailure(
              Texts.translate(
                  "message.adv_rocketry.station_assembly.station_packing_is_not_allowed_at",
                  position.toShortString()));
      }
      for (BlockPos position : captured) {
        var entity = level.getBlockEntity(position);
        if (entity instanceof OrbitalBlockEntity orbital) OrbitalBlock.cleanup(orbital);
        else if (entity instanceof LandingPadBlockEntity landingPad) landingPad.unregister();
      }
      RocketStructure structure = new RocketStructure();
      structure.width = width;
      structure.depth = depth;
      structure.height = height;
      List<BlockPos> removed = new ArrayList<>();
      for (BlockPos position : captured) {
        BlockState state = level.getBlockState(position);
        if (state.isAir() || state.canBeReplaced()) continue;
        var entity = level.getBlockEntity(position);
        CompoundTag data =
            entity == null
                ? new CompoundTag()
                : entity.saveWithFullMetadata(level.registryAccess());
        structure.cells.add(new RocketStructure.Cell(position.subtract(origin), state, data));
        removed.add(position);
      }
      Station record = new Station();
      record.structure = structure.save(true);
      long id = GalaxyData.get(level.getServer()).newStation(record);
      ItemStack packed = new ItemStack(OrbitalRegistry.STATION.get());
      packed.set(ModComponents.STATION_LINK, new StationLink(id));
      ItemStack chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
      chip.set(ModComponents.STATION_LINK, new StationLink(id));
      for (BlockPos position : removed) level.removeBlockEntity(position);
      for (BlockPos position : removed) level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
      builder.energy.consume(1000, false);
      builder.stationInventory.extractItem(0, 1, false);
      builder.stationInventory.extractItem(1, 1, false);
      builder.stationInventory.setStackInSlot(2, packed);
      builder.stationInventory.setStackInSlot(3, chip);
      builder.status = Texts.translate("status.adv_rocketry.station_assembly.station_packed", id);
    } catch (IllegalArgumentException exception) {
      builder.status = AssemblyFailure.describe(exception);
    }
    builder.setChanged();
    level.sendBlockUpdated(
        builder.getBlockPos(), builder.getBlockState(), builder.getBlockState(), 3);
  }
}

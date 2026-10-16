// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.LaunchPad;
import advRocketry.rocket.AssemblyAccess;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.util.AssemblyFailure;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Original station-assembler pad bounds, converted to a packed native block palette. */
public final class StationAssembly {
  private StationAssembly() {}

  public static void pack(RocketBlockEntity builder, ServerPlayer player) {
    if (!(builder.getLevel() instanceof ServerLevel level)) return;
    try {
      LaunchPad bounds = LaunchPad.scan(level, builder.getBlockPos());
      int width = bounds.width(), depth = bounds.depth(), height = bounds.height();
      BlockPos origin = bounds.origin();
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
      AssemblyAccess.checkBreak(level, captured, player);
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

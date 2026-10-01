// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.multiblock.Multiblock;
import advRocketry.multiblock.Multiblocks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared projector requirements used by both the preview and material list. */
public final class ProjectorBlueprint {
  public record Cell(BlockPos position, Object key, Predicate<BlockState> matches) {}

  public record Material(List<Block> alternatives, int count, boolean optional) {}

  private ProjectorBlueprint() {}

  public static List<Material> materials(RegistryAccess registries, ResourceLocation id) {
    Multiblock multiblock = Multiblocks.get(registries, id);
    if (multiblock == null) return List.of();
    Map<Character, Integer> counts = new LinkedHashMap<>();
    for (Multiblock.Cell cell : multiblock.cells()) counts.merge(cell.symbol(), 1, Integer::sum);
    List<Material> materials = new ArrayList<>();
    counts.forEach(
        (symbol, count) -> {
          Multiblock.Key key = multiblock.key(symbol);
          List<Block> alternatives =
              key.blocks().stream().filter(block -> block.asItem() != Items.AIR).toList();
          if (!alternatives.isEmpty()) materials.add(new Material(alternatives, count, key.air()));
        });
    return materials;
  }

  public static List<Cell> cells(
      RegistryAccess registries, ResourceLocation id, BlockPos origin, Direction facing) {
    Multiblock multiblock = Multiblocks.get(registries, id);
    if (multiblock == null) return List.of();
    List<Cell> cells = new ArrayList<>();
    for (Multiblock.Placed placed : multiblock.place(origin, facing))
      cells.add(new Cell(placed.pos(), placed.symbol(), placed.key()::test));
    return List.copyOf(cells);
  }
}

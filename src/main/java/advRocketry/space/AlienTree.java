// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Four-wide lightwood trunk and radial leaf pods adapted from the original alien tree. */
public final class AlienTree {
  private AlienTree() {}

  public static boolean grow(LevelAccessor level, BlockPos base, RandomSource random) {
    int height = 20 + random.nextInt(10);
    if (base.getY() < level.getMinBuildHeight() + 1
        || base.getY() + height + 5 >= level.getMaxBuildHeight()) return false;
    BlockState soil = level.getBlockState(base.below());
    if (!soil.is(Blocks.DIRT)
        && !soil.is(Blocks.GRASS_BLOCK)
        && !soil.is(Blocks.PODZOL)
        && !soil.is(Blocks.MYCELIUM)
        && !soil.is(Blocks.MOSS_BLOCK)) return false;
    for (int y = 0; y <= height + 2; y++) {
      int radius = y == 0 ? 0 : 3;
      for (int x = -radius; x <= radius + 1; x++)
        for (int z = -radius; z <= radius + 1; z++) {
          BlockState existing = level.getBlockState(base.offset(x, y, z));
          if (!existing.isAir()
              && !existing.canBeReplaced()
              && !existing.is(ProcessingRegistry.part("alien_leaves"))
              && !existing.is(ProcessingRegistry.part("alien_log"))
              && !existing.is(ProcessingRegistry.part("alien_sapling"))) return false;
        }
    }
    BlockState log = ProcessingRegistry.part("alien_log").defaultBlockState();
    BlockState leaves =
        ProcessingRegistry.part("alien_leaves")
            .defaultBlockState()
            .setValue(LeavesBlock.DISTANCE, 6);
    for (int y = 0; y < height; y++)
      for (int x = 0; x < 2; x++)
        for (int z = 0; z < 2; z++) level.setBlock(base.offset(x, y, z), log, 2);
    for (int x = -1; x <= 2; x++)
      for (int z = -1; z <= 2; z++)
        if ((x == -1 || x == 2) != (z == -1 || z == 2))
          level.setBlock(base.offset(x, 0, z), log, 2);
    int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int tier = 0; tier < 2; tier++)
      for (int[] direction : directions) {
        int dx = direction[0], dz = direction[1];
        int y =
            tier == 0 ? random.nextInt(10) + height / 6 : random.nextInt(5) + height - height / 3;
        BlockPos root =
            base.offset(
                dx > 0 || dz < 0 && dx == 0 ? 1 : 0, y, dx > 0 || dz < 0 && dx == 0 ? 0 : 1);
        pod(level, root, random.nextInt(5) + (tier == 0 ? 6 : 3), dx, dz, log, leaves);
      }
    int top = height - 1;
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 1; z++) {
        for (int down = 0; down < height - 4; down++) {
          int radius = Math.abs(x) + Math.abs(z);
          if (radius < (down < height / 3 ? 3 : 4))
            crown(level, base.offset(0, top - 1 - down, 0), x, z, leaves);
        }
        if ((x > -2 || z > -1) && (x != -1 || z != -2))
          crown(level, base.offset(0, top + 1, 0), x, z, leaves);
      }
    if (random.nextBoolean())
      for (int x = 0; x < 2; x++)
        for (int z = 0; z < 2; z++) leaf(level, base.offset(x, top + 2, z), leaves);
    for (int x = -3; x <= 4; x++)
      for (int z = -3; z <= 4; z++)
        if ((x != -3 || z != -3)
            && (x != -3 || z != 4)
            && (x != 4 || z != -3)
            && (x != 4 || z != 4)
            && (Math.abs(x) < 3 || Math.abs(z) < 3)) leaf(level, base.offset(x, top, z), leaves);
    return true;
  }

  private static void crown(LevelAccessor level, BlockPos base, int x, int z, BlockState leaves) {
    leaf(level, base.offset(x, 0, z), leaves);
    leaf(level, base.offset(1 - x, 0, z), leaves);
    leaf(level, base.offset(x, 0, 1 - z), leaves);
    leaf(level, base.offset(1 - x, 0, 1 - z), leaves);
  }

  private static void pod(
      LevelAccessor level,
      BlockPos root,
      int length,
      int dx,
      int dz,
      BlockState log,
      BlockState leaves) {
    BlockState branchLog =
        log.setValue(RotatedPillarBlock.AXIS, dx != 0 ? Direction.Axis.X : Direction.Axis.Z);
    for (int step = 0; step < length; step++) {
      BlockPos branch = root.offset(dx * step, step >= length / 2 ? 2 : 0, dz * step);
      if (!wood(level, branch, branchLog)
          || !wood(level, branch.below(), branchLog)
          || !wood(level, branch.offset(dz, 0, dx), branchLog)
          || !wood(level, branch.offset(dz, -1, dx), branchLog)) break;
    }
    BlockPos center = root.offset(dx * length, 1, dz * length);
    for (int x = -4; x < 4; x++)
      for (int y = -4; y < 4; y++)
        for (int z = -4; z < 4; z++)
          if (x * x + y * y + z * z < 17) leaf(level, center.offset(x, y, z), leaves);
  }

  private static boolean wood(LevelAccessor level, BlockPos pos, BlockState log) {
    BlockState existing = level.getBlockState(pos);
    if (!existing.canBeReplaced()
        && !existing.is(ProcessingRegistry.part("alien_leaves"))
        && !existing.is(ProcessingRegistry.part("alien_log"))
        && !existing.is(ProcessingRegistry.part("alien_sapling"))) return false;
    level.setBlock(pos, log, 2);
    return true;
  }

  private static void leaf(LevelAccessor level, BlockPos pos, BlockState leaves) {
    if (level.getBlockState(pos).isAir()) level.setBlock(pos, leaves, 2);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Optional original launch-pad scorching and temporary exhaust fire. */
public final class RocketScorch {
  private RocketScorch() {}

  public static void scorch(ServerLevel level, BlockPos center, int thrust) {
    int radius = Math.clamp((int) Math.pow(Math.max(1, thrust), .4), 1, 8);
    for (int x = -radius; x <= radius; x++)
      for (int z = -radius; z <= radius; z++)
        for (int y = -3; y <= 0; y++) {
          if (x * x + y * y + z * z > radius * radius) continue;
          BlockPos pos = center.offset(x, y, z);
          if (!level.hasChunkAt(pos)) continue;
          BlockState state = level.getBlockState(pos);
          if (state.isAir()) continue;
          if (level.random.nextInt(80) == 0) {
            BlockState damaged = damaged(state);
            if (damaged != null) level.setBlock(pos, damaged, 3);
          }
          BlockPos above = pos.above();
          if (level.getBlockState(above).isAir())
            level.setBlock(above, RocketRegistry.ROCKET_FIRE.get().defaultBlockState(), 3);
        }
  }

  static BlockState damaged(BlockState state) {
    if (state.is(Blocks.STONE) || state.is(Blocks.STONE_BRICKS))
      return Blocks.COBBLESTONE.defaultBlockState();
    if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL))
      return ProcessingRegistry.part("basalt").defaultBlockState();
    if (state.is(ProcessingRegistry.part("basalt")) || state.is(Blocks.NETHERRACK))
      return Blocks.MAGMA_BLOCK.defaultBlockState();
    if (state.is(Blocks.MAGMA_BLOCK)) return Blocks.LAVA.defaultBlockState();
    if (state.is(Blocks.GRASS_BLOCK)) return Blocks.DIRT.defaultBlockState();
    if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT))
      return Blocks.SAND.defaultBlockState();
    if (state.is(Blocks.SAND)
        || state.is(Blocks.SANDSTONE)
        || state.is(ProcessingRegistry.part("moon_turf"))) return Blocks.GLASS.defaultBlockState();
    if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.SNOW_BLOCK))
      return Blocks.WATER.defaultBlockState();
    if (state.is(Blocks.SNOW) || state.is(Blocks.WATER)) return Blocks.AIR.defaultBlockState();
    if (state.is(BlockTags.LOGS)
        || state.is(BlockTags.LEAVES)
        || state.is(BlockTags.PLANKS)
        || state.is(BlockTags.FLOWERS)) return Blocks.FIRE.defaultBlockState();
    return null;
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.Heightmap;

/** Forms the original emergency landing float when the entire footprint rests on lava. */
public final class LandingFloatLogic {
  private static final int VOID_LANDING_HEIGHT = 64;

  private LandingFloatLogic() {}

  public static int groundHeight(ServerLevel level, int x, int z, int clearance) {
    if (!level.dimensionType().hasCeiling())
      return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    BlockPos.MutableBlockPos pos =
        new BlockPos.MutableBlockPos(
            x, level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 1, z);
    int open = 0;
    for (; pos.getY() > level.getMinBuildHeight(); pos.move(Direction.DOWN)) {
      if (!supports(level, pos)) {
        open++;
        continue;
      }
      if (open >= clearance) return pos.getY() + 1;
      open = 0;
    }
    return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
  }

  public static boolean isVoid(ServerLevel level, int x, int z) {
    return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
        <= level.getMinBuildHeight();
  }

  public static int voidLandingHeight(ServerLevel level) {
    return Math.max(level.getSeaLevel(), level.getMinBuildHeight() + VOID_LANDING_HEIGHT);
  }

  public static boolean placeIfNeeded(
      ServerLevel level, double centerX, double centerZ, int landingY, int width, int depth) {
    int minX = (int) Math.floor(centerX - width / 2d);
    int minZ = (int) Math.floor(centerZ - depth / 2d);
    int supportY = landingY - 1;
    boolean hazard = true;
    for (int x = minX; x < minX + width; x++)
      for (int z = minZ; z < minZ + depth; z++)
        if (!hazard(level, new BlockPos(x, supportY, z))) hazard = false;
    if (!hazard) return false;
    for (int x = minX - 3; x < minX + width + 3; x++)
      for (int z = minZ - 3; z < minZ + depth + 3; z++) {
        BlockPos support = new BlockPos(x, supportY, z);
        if (hazard(level, support))
          level.setBlock(support, RocketRegistry.LANDING_FLOAT.get().defaultBlockState(), 3);
      }
    return true;
  }

  private static boolean hazard(ServerLevel level, BlockPos support) {
    if (level.getFluidState(support).is(FluidTags.LAVA)) return true;
    return level.getBlockState(support).isAir() && isVoid(level, support.getX(), support.getZ());
  }

  private static boolean supports(ServerLevel level, BlockPos pos) {
    return level.getBlockState(pos).blocksMotion() || !level.getFluidState(pos).isEmpty();
  }
}

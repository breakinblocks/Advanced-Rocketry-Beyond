// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

/** Forms the original emergency landing float when the entire footprint rests on lava. */
public final class LandingFloatLogic {
  private LandingFloatLogic() {}

  public static boolean placeIfNeeded(
      ServerLevel level, double centerX, double centerZ, int landingY, int width, int depth) {
    int minX = (int) Math.floor(centerX - width / 2d);
    int minZ = (int) Math.floor(centerZ - depth / 2d);
    int supportY = landingY - 1;
    boolean hazard = true;
    for (int x = minX; x < minX + width; x++)
      for (int z = minZ; z < minZ + depth; z++) {
        BlockPos support = new BlockPos(x, supportY, z);
        if (!level.getFluidState(support).is(FluidTags.LAVA)) hazard = false;
      }
    if (!hazard) return false;
    for (int x = minX - 3; x < minX + width + 3; x++)
      for (int z = minZ - 3; z < minZ + depth + 3; z++) {
        BlockPos support = new BlockPos(x, supportY, z);
        if (level.getFluidState(support).is(FluidTags.LAVA))
          level.setBlock(support, RocketRegistry.LANDING_FLOAT.get().defaultBlockState(), 3);
      }
    return true;
  }
}

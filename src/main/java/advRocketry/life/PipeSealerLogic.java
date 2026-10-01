// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.transport.TransportBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** The original four-seal cross creates an airtight virtual center. */
public final class PipeSealerLogic {
  private PipeSealerLogic() {}

  public static boolean seals(Level level, BlockPos center) {
    if (!level.hasChunkAt(center)
        || (!level.isEmptyBlock(center)
            && !(level.getBlockState(center).getBlock() instanceof TransportBlock))) return false;
    return frame(level, center.below(), center.above(), center.east(), center.west())
        || frame(level, center.below(), center.above(), center.north(), center.south())
        || frame(level, center.north(), center.south(), center.east(), center.west());
  }

  private static boolean frame(
      Level level, BlockPos first, BlockPos second, BlockPos third, BlockPos fourth) {
    return sealer(level, first)
        && sealer(level, second)
        && sealer(level, third)
        && sealer(level, fourth);
  }

  private static boolean sealer(Level level, BlockPos pos) {
    return level.hasChunkAt(pos)
        && level.getBlockState(pos).is(LifeSupportRegistry.PIPE_SEALER.get());
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** Original redstone-driven field extension and retraction, one block per five ticks. */
public final class ForceFieldLogic {
  private ForceFieldLogic() {}

  public static void advance(OrbitalBlockEntity projector) {
    if (!projector.forceFieldProjector() || !(projector.getLevel() instanceof ServerLevel level))
      return;
    Direction facing = projector.getBlockState().getValue(ForceFieldProjectorBlock.FACING);
    if (level.hasNeighborSignal(projector.getBlockPos())) {
      if (projector.fieldLength >= 32) return;
      BlockPos next = projector.getBlockPos().relative(facing, projector.fieldLength + 1);
      if (!level.hasChunkAt(next) || !level.isInWorldBounds(next)) return;
      if (level.getBlockState(next).canBeReplaced())
        level.setBlock(next, OrbitalRegistry.FORCE_FIELD.get().defaultBlockState(), 3);
      if (level.getBlockState(next).is(OrbitalRegistry.FORCE_FIELD.get())) {
        projector.fieldLength++;
        projector.setChanged();
      }
    } else if (projector.fieldLength > 0) {
      BlockPos end = projector.getBlockPos().relative(facing, projector.fieldLength);
      if (!level.hasChunkAt(end)) return;
      if (level.getBlockState(end).is(OrbitalRegistry.FORCE_FIELD.get()))
        level.removeBlock(end, false);
      projector.fieldLength--;
      projector.setChanged();
    }
  }

  public static void remove(OrbitalBlockEntity projector) {
    if (!(projector.getLevel() instanceof ServerLevel level)) return;
    Direction facing = projector.getBlockState().getValue(ForceFieldProjectorBlock.FACING);
    for (int step = 1; step <= projector.fieldLength; step++) {
      BlockPos pos = projector.getBlockPos().relative(facing, step);
      if (level.hasChunkAt(pos) && level.getBlockState(pos).is(OrbitalRegistry.FORCE_FIELD.get()))
        level.removeBlock(pos, false);
    }
    projector.fieldLength = 0;
    projector.setChanged();
  }
}

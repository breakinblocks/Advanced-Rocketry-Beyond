// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Six-direction redstone force-field projector. */
public final class ForceFieldProjectorBlock extends DirectionalOrbitalBlock {
  public ForceFieldProjectorBlock(Properties properties) {
    super(Kind.FORCE_FIELD_PROJECTOR, properties);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    return InteractionResult.PASS;
  }
}

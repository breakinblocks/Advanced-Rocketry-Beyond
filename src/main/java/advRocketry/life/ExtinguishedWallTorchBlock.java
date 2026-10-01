// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Wall-mounted companion of the extinguished floor torch. */
public final class ExtinguishedWallTorchBlock extends WallTorchBlock {
  public ExtinguishedWallTorchBlock(Properties properties) {
    super(ParticleTypes.FLAME, properties);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {}

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    return LightingRegistry.relight(stack, level, pos, state);
  }
}

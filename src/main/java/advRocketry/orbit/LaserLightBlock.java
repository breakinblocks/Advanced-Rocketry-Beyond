// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Invisible full-bright shaft marker from the original orbital terrain drill. */
public final class LaserLightBlock extends Block {
  public LaserLightBlock(Properties properties) {
    super(properties);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }
}

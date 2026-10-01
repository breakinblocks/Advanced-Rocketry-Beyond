// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;

/** Six-direction station module docking face. */
public final class DockingPortBlock extends DirectionalOrbitalBlock {
  public DockingPortBlock(Properties properties) {
    super(Kind.DOCKING_PORT, properties);
  }

  @Override
  protected Direction placementFacing(BlockPlaceContext context) {
    Direction clicked = context.getClickedFace();
    return clicked.getAxis().isVertical()
        ? clicked
        : context.getHorizontalDirection().getOpposite();
  }
}

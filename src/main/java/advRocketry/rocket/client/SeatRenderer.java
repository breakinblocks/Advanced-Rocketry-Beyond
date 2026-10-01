// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.rocket.SeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** The rider renders normally; the mount has no visible geometry. */
public final class SeatRenderer extends EntityRenderer<SeatEntity> {
  public SeatRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public ResourceLocation getTextureLocation(SeatEntity entity) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}

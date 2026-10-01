// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.orbit.RailgunCargoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

/** Renders the original railgun's rising item without creating a collectible duplicate. */
public final class RailgunCargoRenderer extends EntityRenderer<RailgunCargoEntity> {
  private final ItemRenderer items;

  public RailgunCargoRenderer(EntityRendererProvider.Context context) {
    super(context);
    items = context.getItemRenderer();
  }

  @Override
  public void render(
      RailgunCargoEntity entity,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight) {
    if (entity.display().isEmpty()) return;
    pose.pushPose();
    pose.scale(.7f, .7f, .7f);
    items.renderStatic(
        entity.display(),
        ItemDisplayContext.GROUND,
        packedLight,
        OverlayTexture.NO_OVERLAY,
        pose,
        buffers,
        entity.level(),
        entity.getId());
    pose.popPose();
    super.render(entity, yaw, partialTick, pose, buffers, packedLight);
  }

  @Override
  public ResourceLocation getTextureLocation(RailgunCargoEntity entity) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}

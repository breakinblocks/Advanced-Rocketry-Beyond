// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.rocket.RocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class RocketRenderer extends EntityRenderer<RocketEntity> {
  private final BlockRenderDispatcher blocks;

  public RocketRenderer(EntityRendererProvider.Context context) {
    super(context);
    blocks = context.getBlockRenderDispatcher();
  }

  @Override
  public void render(
      RocketEntity rocket,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light) {
    var structure = rocket.structure();
    pose.pushPose();
    if (rocket.flight() == 3 || rocket.asteroidRcs()) {
      pose.mulPose(Axis.YP.rotationDegrees(-yaw));
      pose.mulPose(Axis.XP.rotationDegrees(90));
    }
    pose.translate(-structure.width / 2d, 0, -structure.depth / 2d);
    for (var cell : structure.cells) {
      pose.pushPose();
      pose.translate(cell.position().getX(), cell.position().getY(), cell.position().getZ());
      blocks.renderSingleBlock(cell.state(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
      pose.popPose();
    }
    pose.popPose();
    super.render(rocket, yaw, partialTick, pose, buffers, light);
  }

  @Override
  public ResourceLocation getTextureLocation(RocketEntity rocket) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}

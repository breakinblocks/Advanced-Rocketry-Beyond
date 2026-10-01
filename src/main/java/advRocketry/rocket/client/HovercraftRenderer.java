// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.Main;
import advRocketry.processing.client.ObjModel;
import advRocketry.rocket.HovercraftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Renders the original MIT hovercraft mesh and texture. */
public final class HovercraftRenderer extends EntityRenderer<HovercraftEntity> {
  private static final ResourceLocation MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/legacy/hovercraft.obj");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/legacy/advancedrocketry/models/hovercraft.png");
  private static ObjModel model;

  public HovercraftRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  public static void clearModel() {
    model = null;
  }

  @Override
  public void render(
      HovercraftEntity craft,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light) {
    if (model == null) model = new ObjModel(MODEL);
    pose.pushPose();
    pose.translate(0, 1, 0);
    pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
    model.renderAll(
        pose,
        buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
        light,
        OverlayTexture.NO_OVERLAY,
        -1);
    pose.popPose();
    super.render(craft, yaw, partialTick, pose, buffers, light);
  }

  @Override
  public ResourceLocation getTextureLocation(HovercraftEntity craft) {
    return TEXTURE;
  }
}

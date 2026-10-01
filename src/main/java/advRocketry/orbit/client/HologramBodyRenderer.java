// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.Main;
import advRocketry.orbit.HologramBodyEntity;
import advRocketry.processing.client.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Original MIT sphere geometry with native entity picking and hover labels. */
public final class HologramBodyRenderer extends EntityRenderer<HologramBodyEntity> {
  private static final ResourceLocation MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/legacy/hologram_sphere.obj");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
  private static ObjModel model;

  public HologramBodyRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  public static void clearModel() {
    model = null;
  }

  @Override
  public void render(
      HologramBodyEntity entity,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light) {
    if (model == null) model = new ObjModel(MODEL);
    pose.pushPose();
    pose.scale(entity.radius(), entity.radius(), entity.radius());
    model.renderAll(
        pose,
        buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
        0xf000f0,
        OverlayTexture.NO_OVERLAY,
        entity.color());
    pose.popPose();
    super.render(entity, yaw, partialTick, pose, buffers, 0xf000f0);
  }

  @Override
  protected boolean shouldShowName(HologramBodyEntity entity) {
    return entity.hasCustomName()
        && (entity.selected() || Minecraft.getInstance().crosshairPickEntity == entity);
  }

  @Override
  public ResourceLocation getTextureLocation(HologramBodyEntity entity) {
    return TEXTURE;
  }
}

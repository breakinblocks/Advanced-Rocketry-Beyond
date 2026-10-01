// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.Main;
import advRocketry.orbit.ElevatorCapsule;
import advRocketry.processing.client.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Original space-elevator capsule mesh with its original texture. */
public final class ElevatorCapsuleRenderer extends EntityRenderer<ElevatorCapsule> {
  private static final ResourceLocation MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/space_elevator.obj");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/space_elevator.png");
  private static ObjModel model;

  public ElevatorCapsuleRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  public static void clearModel() {
    model = null;
  }

  @Override
  public void render(
      ElevatorCapsule capsule,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light) {
    if (model == null) model = new ObjModel(MODEL);
    pose.pushPose();
    pose.translate(0, 1, 0);
    pose.mulPose(Axis.YP.rotationDegrees(yaw));
    var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
    model.renderPart("Capsule", pose, vertices, light, OverlayTexture.NO_OVERLAY);
    if (capsule.inMotion())
      model.renderPart("Door", pose, vertices, light, OverlayTexture.NO_OVERLAY);
    pose.popPose();
    super.render(capsule, yaw, partialTick, pose, buffers, light);
  }

  @Override
  public ResourceLocation getTextureLocation(ElevatorCapsule capsule) {
    return TEXTURE;
  }
}

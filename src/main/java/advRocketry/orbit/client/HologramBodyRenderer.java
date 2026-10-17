// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.Main;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.client.effects.HologramShader;
import advRocketry.orbit.HologramBodyEntity;
import advRocketry.processing.client.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

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
  public boolean shouldRender(
      HologramBodyEntity entity, Frustum frustum, double x, double y, double z) {
    return entity.distanceToSqr(x, y, z) < 96 * 96
        && frustum.isVisible(entity.getBoundingBox().inflate(20));
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
    pose.mulPose(
        Axis.YP.rotationDegrees(
            (float) EffectGeometry.time(entity.level(), partialTick, EffectProfiles.get("hologram"))
                * .3f));
    model.renderAll(
        pose,
        buffers.getBuffer(HologramShader.surface(TEXTURE)),
        0xf000f0,
        OverlayTexture.NO_OVERLAY,
        EffectProfiles.get("hologram").argb(entity.selected() ? 1 : .75f));
    pose.popPose();
    renderProjection(entity, partialTick, pose, buffers);
    super.render(entity, yaw, partialTick, pose, buffers, 0xf000f0);
  }

  private static void renderProjection(
      HologramBodyEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers) {
    var profile = EffectProfiles.get("hologram");
    if (!EffectGeometry.visible(entity.position(), profile)) return;
    if (!EffectGeometry.reserve()) return;
    double radius = Math.clamp(entity.radius(), .05, 8);
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    Vec3 center = entity.projectionCenter().subtract(entity.position());
    double orbit = Math.sqrt(center.x * center.x + center.z * center.z);
    if (entity.showOrbit() && orbit > .1 && orbit < 20) {
      pose.pushPose();
      pose.translate(center.x, 0, center.z);
      EffectGeometry.ring(
          pose, vertices, orbit, 0, .02, profile.argb(.5f), EffectGeometry.segments(profile));
      pose.popPose();
      if (entity.selected())
        EffectGeometry.beam(pose, vertices, Vec3.ZERO, center, .008, profile.argb(.6f));
    }
    if (entity.selected())
      EffectGeometry.ring(
          pose,
          vertices,
          radius * 1.3,
          -radius,
          .014,
          profile.argb(.8f),
          EffectGeometry.segments(profile));
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

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.client.effects.HologramShader;
import advRocketry.client.effects.HologramSphere;
import advRocketry.client.effects.ShaderCompatibility;
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

/** Smooth procedural holograms with native entity picking and a legacy model fallback. */
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
        && frustum.isVisible(entity.getBoundingBox().inflate(64));
  }

  @Override
  public void render(
      HologramBodyEntity entity,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light) {
    if (ShaderCompatibility.shadowPass()) return;
    pose.pushPose();
    // The source sphere spans -0.5..0.5; scale its diameter to the synced radius.
    float diameter = entity.radius() * 2;
    pose.scale(diameter, diameter, diameter);
    pose.mulPose(
        Axis.YP.rotationDegrees(
            (float) EffectGeometry.time(entity.level(), partialTick, EffectProfiles.get("hologram"))
                * .3f));
    var surface = HologramShader.surface(TEXTURE);
    int color = EffectProfiles.get("hologram").argb(entity.selected() ? 1 : .75f);
    if (ShaderCompatibility.shaderPackActive()) {
      renderWireSphere(pose, buffers, color);
    } else if (HologramShader.active()) {
      Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      boolean largeNearby =
          entity.radius() > 1 && entity.position().distanceToSqr(camera) < 32 * 32;
      int detail = Math.min(AdvancedRocketryConfig.effectDetail(), largeNearby ? 2 : 1);
      HologramSphere.render(pose, buffers.getBuffer(surface), detail, color);
    } else {
      if (model == null) model = new ObjModel(MODEL);
      model.renderAll(pose, buffers.getBuffer(surface), 0xf000f0, OverlayTexture.NO_OVERLAY, color);
    }
    pose.popPose();
    renderProjection(entity, partialTick, pose, buffers);
    super.render(entity, yaw, partialTick, pose, buffers, 0xf000f0);
  }

  /** Shader-pack fallback uses the same emissive geometry as the projection rings. */
  private static void renderWireSphere(PoseStack pose, MultiBufferSource buffers, int color) {
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    int segments = AdvancedRocketryConfig.effectDetail() > 1 ? 48 : 32;
    for (int latitude = -3; latitude <= 3; latitude++) {
      double angle = latitude * Math.PI / 8;
      EffectGeometry.ring(
          pose, vertices, .5 * Math.cos(angle), .5 * Math.sin(angle), .0025, color, segments);
    }
    for (int longitude = 0; longitude < 6; longitude++) {
      pose.pushPose();
      pose.mulPose(Axis.YP.rotationDegrees(longitude * 30));
      pose.mulPose(Axis.XP.rotationDegrees(90));
      EffectGeometry.ring(pose, vertices, .5, 0, .0025, color, segments);
      pose.popPose();
    }
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
    if (entity.showOrbit() && orbit > .1 && orbit < 64) {
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

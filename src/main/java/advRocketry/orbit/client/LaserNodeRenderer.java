// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.orbit.LaserNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Teal helical containment strands around a narrow beam and concentrated impact. */
public final class LaserNodeRenderer extends EntityRenderer<LaserNodeEntity> {
  public LaserNodeRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public boolean shouldRender(
      LaserNodeEntity entity, Frustum frustum, double x, double y, double z) {
    return Math.abs(entity.getX() - x) < 128
        && Math.abs(entity.getZ() - z) < 128
        && frustum.isVisible(
            new AABB(
                entity.getX() - 1,
                entity.getY(),
                entity.getZ() - 1,
                entity.getX() + 1,
                entity.getY() + 200,
                entity.getZ() + 1));
  }

  @Override
  public void render(
      LaserNodeEntity entity,
      float yaw,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight) {
    var profile = EffectProfiles.get("laser");
    if (!profile.enabled()) return;
    VertexConsumer vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    EffectGeometry.beam(pose, vertices, Vec3.ZERO, new Vec3(0, 200, 0), .04, profile.argb(.4f));
    if (EffectGeometry.visible(entity.position(), profile) && EffectGeometry.reserve()) {
      double time = EffectGeometry.time(entity.level(), partialTick, profile);
      EffectGeometry.beam(pose, vertices, Vec3.ZERO, new Vec3(0, 200, 0), .09, profile.argb(.5f));
      EffectGeometry.beam(pose, vertices, Vec3.ZERO, new Vec3(0, 200, 0), .22, profile.argb(.08f));
      for (int strand = 0; strand < 2; strand++) {
        double start = -time * .12 + strand * Math.PI;
        Vec3 previous = new Vec3(.4 * Math.cos(start), 0, .4 * Math.sin(start));
        int steps = EffectGeometry.segments(profile) * 4;
        for (int step = 1; step <= steps; step++) {
          double y = step * 32d / steps;
          double angle = y * 2.4 - time * .12 + strand * Math.PI;
          Vec3 next = new Vec3(Math.cos(angle) * .4, y, Math.sin(angle) * .4);
          EffectGeometry.beam(
              pose, vertices, previous, next, .055, profile.argb((float) (1 - y / 32)));
          previous = next;
        }
      }
      EffectGeometry.ring(
          pose, vertices, .65, .03, .035, profile.argb(.8f), EffectGeometry.segments(profile));
      EffectGeometry.beam(
          pose, vertices, new Vec3(-.2, .05, 0), new Vec3(.2, .05, 0), .12, 0xe0eafff8);
      EffectGeometry.beam(
          pose, vertices, new Vec3(0, .05, -.2), new Vec3(0, .05, .2), .12, 0xe0eafff8);
    }
    super.render(entity, yaw, partialTick, pose, buffers, packedLight);
  }

  @Override
  public ResourceLocation getTextureLocation(LaserNodeEntity entity) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}

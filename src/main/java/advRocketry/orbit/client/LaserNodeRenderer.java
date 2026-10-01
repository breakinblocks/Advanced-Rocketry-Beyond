// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.orbit.LaserNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/** Red translucent target beam adapted from the original laser-node renderer. */
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
    VertexConsumer vertices = buffers.getBuffer(RenderType.lightning());
    Matrix4f matrix = pose.last().pose();
    for (int index = 0; index < 8; index++) {
      double angle = index * Math.PI / 4;
      double next = (index + 1) * Math.PI / 4;
      float x0 = (float) Math.cos(angle) * .6f;
      float z0 = (float) Math.sin(angle) * .6f;
      float x1 = (float) Math.cos(next) * .6f;
      float z1 = (float) Math.sin(next) * .6f;
      vertices.addVertex(matrix, x0, 0, z0).setColor(240, 48, 72, 150);
      vertices.addVertex(matrix, x1, 0, z1).setColor(240, 48, 72, 150);
      vertices.addVertex(matrix, x1 * .2f, 200, z1 * .2f).setColor(240, 48, 72, 0);
      vertices.addVertex(matrix, x0 * .2f, 200, z0 * .2f).setColor(240, 48, 72, 0);
    }
    super.render(entity, yaw, partialTick, pose, buffers, packedLight);
  }

  @Override
  public ResourceLocation getTextureLocation(LaserNodeEntity entity) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}

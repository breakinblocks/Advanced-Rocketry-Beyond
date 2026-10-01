// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.processing.LaserBeamSync;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/** Original FxLaser, FxLaserHeat and FxLaserSpark effects using native world rendering. */
public final class LaserBeamRenderer {
  private record Beam(Entity shooter, Vec3 impact, long born) {}

  private record Impact(Vec3 position, Vec3 velocity, long born) {}

  private static ClientLevel level;
  private static final Map<Integer, Beam> BEAMS = new HashMap<>();
  private static final List<Impact> HEAT = new ArrayList<>();
  private static final List<Impact> SPARKS = new ArrayList<>();

  private LaserBeamRenderer() {}

  private static void checkLevel(ClientLevel current) {
    if (level == current) return;
    level = current;
    BEAMS.clear();
    HEAT.clear();
    SPARKS.clear();
  }

  public static void receive(LaserBeamSync.Payload payload) {
    checkLevel(Minecraft.getInstance().level);
    if (level == null || !level.dimension().location().equals(payload.dimension())) return;
    Entity shooter = level.getEntity(payload.shooter());
    if (shooter == null) return;
    long now = level.getGameTime();
    BEAMS.put(shooter.getId(), new Beam(shooter, payload.impact(), now));
    HEAT.add(new Impact(payload.impact(), Vec3.ZERO, now));
    for (int i = 0; i < 4; i++)
      SPARKS.add(
          new Impact(
              payload.impact(),
              new Vec3(
                  .125 - level.random.nextFloat() / 4,
                  .125 - level.random.nextFloat() / 4,
                  .125 - level.random.nextFloat() / 4),
              now));
  }

  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
    Minecraft client = Minecraft.getInstance();
    checkLevel(client.level);
    if (level == null) return;
    long now = level.getGameTime();
    BEAMS.values().removeIf(beam -> now - beam.born() > 1 || beam.shooter().isRemoved());
    HEAT.removeIf(impact -> now - impact.born() >= 20);
    SPARKS.removeIf(impact -> now - impact.born() >= 10);
    if (BEAMS.isEmpty() && HEAT.isEmpty() && SPARKS.isEmpty()) return;
    float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
    Vec3 camera = event.getCamera().getPosition();
    var pose = event.getPoseStack();
    pose.pushPose();
    var buffer = client.renderBuffers().bufferSource();
    var vertices = buffer.getBuffer(RenderType.lightning());
    Matrix4f matrix = pose.last().pose();
    for (Beam beam : BEAMS.values()) {
      Entity shooter = beam.shooter();
      double yaw = Math.toRadians(shooter.getViewYRot(partial));
      boolean firstPerson =
          shooter == client.getCameraEntity() && client.options.getCameraType().isFirstPerson();
      Vec3 origin =
          shooter
              .getPosition(partial)
              .add(
                  -Math.cos(yaw) * .3 + Math.sin(yaw) * .075,
                  firstPerson ? shooter.getEyeHeight() - .12 : 1.15,
                  -Math.sin(yaw) * .3 - Math.cos(yaw) * .075);
      line(vertices, matrix, origin.subtract(camera), beam.impact().subtract(camera), .012, 102);
    }
    for (Impact impact : HEAT) {
      double fade = 1 - (now - impact.born() + partial) / 20;
      cube(vertices, matrix, impact.position().subtract(camera), .02 * fade, (int) (255 * fade));
    }
    for (Impact impact : SPARKS) {
      double age = now - impact.born() + partial;
      Vec3 from = impact.position().add(impact.velocity().scale(age)).subtract(camera);
      line(
          vertices,
          matrix,
          from,
          from.add(impact.velocity().scale(.5)),
          .003,
          (int) (255 * (1 - age / 10)));
    }
    buffer.endBatch(RenderType.lightning());
    pose.popPose();
  }

  private static void line(
      VertexConsumer vertices, Matrix4f matrix, Vec3 from, Vec3 to, double width, int alpha) {
    Vec3 axis = to.subtract(from);
    if (axis.lengthSqr() < 1e-10) return;
    Vec3 side = axis.cross(from);
    if (side.lengthSqr() < 1e-10) side = axis.cross(new Vec3(0, 1, 0));
    if (side.lengthSqr() < 1e-10) side = axis.cross(new Vec3(1, 0, 0));
    side = side.normalize().scale(width);
    quad(
        vertices,
        matrix,
        from.subtract(side),
        to.subtract(side),
        to.add(side),
        from.add(side),
        alpha);
  }

  private static void cube(
      VertexConsumer vertices, Matrix4f matrix, Vec3 center, double size, int alpha) {
    Vec3[] points = new Vec3[8];
    for (int i = 0; i < 8; i++)
      points[i] =
          center.add(
              (i & 1) == 0 ? -size : size,
              (i & 2) == 0 ? -size : size,
              (i & 4) == 0 ? -size : size);
    for (int[] face :
        new int[][] {
          {0, 2, 3, 1}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 6, 7, 3}, {0, 4, 6, 2}, {1, 3, 7, 5}
        })
      quad(
          vertices,
          matrix,
          points[face[0]],
          points[face[1]],
          points[face[2]],
          points[face[3]],
          alpha);
  }

  private static void quad(
      VertexConsumer vertices, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int alpha) {
    for (Vec3 point : new Vec3[] {a, b, c, d})
      vertices
          .addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
          .setColor(204, 51, 51, alpha);
  }
}

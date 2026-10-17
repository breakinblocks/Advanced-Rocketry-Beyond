package advRocketry.client.effects;

import advRocketry.AdvancedRocketryConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Small shared geometry primitives for POSITION_COLOR quad buffers. */
public final class EffectGeometry {
  private static int remaining = 256;
  private EffectGeometry() {}

  public static void beginFrame(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
      remaining = 64 << AdvancedRocketryConfig.effectDetail();
  }

  public static boolean reserve() {
    return remaining-- > 0;
  }

  public static int segments(EffectProfile profile) {
    return Math.min(profile.segments(), 16 << AdvancedRocketryConfig.effectDetail());
  }

  public static void sphere(PoseStack pose, VertexConsumer vertices, double radius, int color) {
    for (int row = 0; row < 12; row++) {
      double a = Math.PI * row / 12, b = Math.PI * (row + 1) / 12;
      for (int column = 0; column < 24; column++) {
        double u = column * Math.PI / 12, v = (column + 1) * Math.PI / 12;
        vertex(pose, vertices, Math.cos(u) * Math.sin(a) * radius, Math.cos(a) * radius, Math.sin(u) * Math.sin(a) * radius, color);
        vertex(pose, vertices, Math.cos(v) * Math.sin(a) * radius, Math.cos(a) * radius, Math.sin(v) * Math.sin(a) * radius, color);
        vertex(pose, vertices, Math.cos(v) * Math.sin(b) * radius, Math.cos(b) * radius, Math.sin(v) * Math.sin(b) * radius, color);
        vertex(pose, vertices, Math.cos(u) * Math.sin(b) * radius, Math.cos(b) * radius, Math.sin(u) * Math.sin(b) * radius, color);
      }
    }
  }

  public static double time(Level level, float partialTick, EffectProfile profile) {
    return AdvancedRocketryConfig.reducedMotion() ? 0
        : ((level.getGameTime() % 72000) + partialTick) * profile.speed();
  }

  public static boolean visible(Vec3 position, EffectProfile profile) {
    return profile.enabled() && AdvancedRocketryConfig.advancedVfx()
        && Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
            .distanceToSqr(position) < 96 * 96;
  }

  public static void ring(PoseStack pose, VertexConsumer vertices, double radius, double y,
      double width, int color, int segments) {
    for (int i = 0; i < segments; i++) {
      double a = i * Math.PI * 2 / segments, b = (i + 1) * Math.PI * 2 / segments;
      vertex(pose, vertices, Math.cos(a) * (radius - width), y, Math.sin(a) * (radius - width), color);
      vertex(pose, vertices, Math.cos(b) * (radius - width), y, Math.sin(b) * (radius - width), color);
      vertex(pose, vertices, Math.cos(b) * (radius + width), y, Math.sin(b) * (radius + width), color);
      vertex(pose, vertices, Math.cos(a) * (radius + width), y, Math.sin(a) * (radius + width), color);
    }
  }

  public static void beam(PoseStack pose, VertexConsumer vertices, Vec3 from, Vec3 to,
      double width, int color) {
    Vec3 direction = to.subtract(from);
    if (direction.lengthSqr() < 1e-10) return;
    Vec3 side = direction.cross(Math.abs(direction.normalize().y) > .9
        ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize().scale(width);
    ribbon(pose, vertices, from, to, side, color);
    ribbon(pose, vertices, from, to, direction.cross(side).normalize().scale(width), color);
  }

  private static void ribbon(PoseStack pose, VertexConsumer vertices, Vec3 from, Vec3 to,
      Vec3 side, int color) {
    vertex(pose, vertices, from.x - side.x, from.y - side.y, from.z - side.z, color);
    vertex(pose, vertices, to.x - side.x, to.y - side.y, to.z - side.z, color);
    vertex(pose, vertices, to.x + side.x, to.y + side.y, to.z + side.z, color);
    vertex(pose, vertices, from.x + side.x, from.y + side.y, from.z + side.z, color);
  }

  private static void vertex(PoseStack pose, VertexConsumer vertices, double x, double y,
      double z, int color) {
    vertices.addVertex(pose.last().pose(), (float) x, (float) y, (float) z).setColor(color);
  }
}

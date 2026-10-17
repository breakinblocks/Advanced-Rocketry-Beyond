package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class MachineIndicators {
  private MachineIndicators() {}

  public static void render(
      PoseStack pose,
      MultiBufferSource buffers,
      Direction front,
      boolean active,
      float progress,
      Vec3 position) {
    var profile = EffectProfiles.get("materials");
    if (!front.getAxis().isHorizontal() || !EffectGeometry.visible(position, profile)) return;
    if (!EffectGeometry.reserve()) return;
    var glow = buffers.getBuffer(EffectRenderTypes.GLOW);
    Vec3 center = new Vec3(.5 + front.getStepX() * .506, 0, .5 + front.getStepZ() * .506);
    Vec3 tangent = new Vec3(front.getStepZ(), 0, -front.getStepX());
    int trim = profile.argb(active ? .45f : .12f);
    for (int side = -1; side <= 1; side += 2) {
      double x = side * .39;
      double inner = side * .24;
      strip(pose, glow, center, tangent, Math.min(x, inner), .1, Math.max(x, inner), .112, trim);
      strip(pose, glow, center, tangent, Math.min(x, inner), .888, Math.max(x, inner), .9, trim);
      strip(pose, glow, center, tangent, x - .006, .1, x + .006, .23, trim);
      strip(pose, glow, center, tangent, x - .006, .77, x + .006, .9, trim);
    }
    for (int i = 0; i < 5; i++) {
      double x = -.325 + i * .14;
      float strength = active && progress >= i / 5f ? .8f : .16f;
      strip(pose, glow, center, tangent, x, .768, x + .09, .792, profile.argb(strength));
    }
  }

  private static void strip(
      PoseStack pose,
      VertexConsumer vertices,
      Vec3 center,
      Vec3 tangent,
      double left,
      double bottom,
      double right,
      double top,
      int color) {
    Vec3 a = center.add(tangent.scale(left));
    Vec3 b = center.add(tangent.scale(right));
    var matrix = pose.last().pose();
    vertices.addVertex(matrix, (float) a.x, (float) bottom, (float) a.z).setColor(color);
    vertices.addVertex(matrix, (float) b.x, (float) bottom, (float) b.z).setColor(color);
    vertices.addVertex(matrix, (float) b.x, (float) top, (float) b.z).setColor(color);
    vertices.addVertex(matrix, (float) a.x, (float) top, (float) a.z).setColor(color);
  }
}

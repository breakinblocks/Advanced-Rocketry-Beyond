package advRocketry.orbit.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.orbit.OrbitalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Animated full-bright surfaces aligned to the array's existing lamps and connecting bar. */
final class SolarArrayEffects {
  private SolarArrayEffects() {}

  static void render(
      OrbitalBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers) {
    if (!entity.formed || entity.powerMadeLastTick <= 0 || entity.getLevel() == null) return;
    var profile = EffectProfiles.get("solar_array");
    if (!EffectGeometry.visible(Vec3.atCenterOf(entity.getBlockPos()), profile)
        || !EffectGeometry.reserve()) return;
    double time = EffectGeometry.time(entity.getLevel(), partialTick, profile);
    VertexConsumer glow = buffers.getBuffer(EffectRenderTypes.EMISSIVE);
    // The output blocks use batteryrf: three 2-pixel columns, 6/4/2 pixels high.
    // Match their existing green pixels rather than washing out the surrounding steel.
    for (int port = -2; port <= 0; port += 2) {
      for (int column = 0; column < 3; column++) {
        double u = (4 + column * 3) / 16.0;
        double top = (11 - column * 2) / 16.0;
        float pulse = .60f + .20f * (float) Math.sin(time * .10 - column * .65);
        int color = Math.round(pulse * profile.opacity() * 255) << 24 | 0x56ff88;
        panel(pose, glow, -.005, port + u, port + u + .125, .3125, top, color);
        panel(pose, glow, 1.005, port + 1 - u - .125, port + 1 - u, .3125, top, color);
        // Outer end faces remain visible when approaching the array from either side.
        end(
            pose,
            glow,
            port == -2 ? -2.005 : 1.005,
            port == -2 ? 1 - u - .125 : u,
            .3125,
            top,
            color);
      }
    }
    int segments = EffectGeometry.segments(profile);
    for (int i = 0; i < segments; i++) {
      double z0 = -.875 + .75 * i / segments;
      double z1 = -.875 + .75 * (i + 1) / segments;
      double distance = Math.abs((z0 + z1) * .5 + .5) / .375;
      double phase = distance - time / 40.0;
      double crest = Math.pow(.5 + .5 * Math.cos(phase * Math.PI * 2), 8);
      // A narrow traveling highlight over a quiet blue glow, clipped to the original bar.
      panel(pose, glow, .026, z0, z1, .398, .602, profile.argb(.16f + .64f * (float) crest));
      panel(
          pose,
          glow,
          .025,
          z0,
          z1,
          .482,
          .518,
          Math.round(profile.opacity() * (.1f + .65f * (float) crest) * 255) << 24 | 0xb1f4ff);
    }
  }

  private static void panel(
      PoseStack pose,
      VertexConsumer vertices,
      double x,
      double z0,
      double z1,
      double bottom,
      double top,
      int color) {
    vertex(pose, vertices, x, bottom, z0, color);
    vertex(pose, vertices, x, bottom, z1, color);
    vertex(pose, vertices, x, top, z1, color);
    vertex(pose, vertices, x, top, z0, color);
  }

  private static void end(
      PoseStack pose,
      VertexConsumer vertices,
      double z,
      double x,
      double bottom,
      double top,
      int color) {
    vertex(pose, vertices, x, bottom, z, color);
    vertex(pose, vertices, x + .125, bottom, z, color);
    vertex(pose, vertices, x + .125, top, z, color);
    vertex(pose, vertices, x, top, z, color);
  }

  private static void vertex(
      PoseStack pose, VertexConsumer vertices, double x, double y, double z, int color) {
    vertices
        .addVertex(pose.last(), (float) x, (float) y, (float) z)
        .setColor(color)
        .setUv(0, 0)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(LightTexture.FULL_BRIGHT)
        .setNormal(pose.last(), 0, 1, 0);
  }
}

package advRocketry.rocket.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** Bounded translucent meshes for pipelines that replace custom surface shaders. */
final class RocketCompatibilityGeometry {
  private RocketCompatibilityGeometry() {}

  static void plume(PoseStack pose, VertexConsumer vertices, int color, double time) {
    grid(pose, vertices, 2.5f, 0, -10, color, time, true);
  }

  static void smoke(PoseStack pose, VertexConsumer vertices, float radius, int color, float seed) {
    grid(pose, vertices, radius, radius, -radius, color, seed, false);
  }

  private static void grid(
      PoseStack pose,
      VertexConsumer vertices,
      float radius,
      float top,
      float bottom,
      int color,
      double phase,
      boolean plume) {
    int rows = plume ? 20 : 12;
    int columns = 8;
    for (int row = 0; row < rows; row++)
      for (int column = 0; column < columns; column++) {
        point(
            pose,
            vertices,
            column / (float) columns,
            row / (float) rows,
            radius,
            top,
            bottom,
            color,
            phase,
            plume);
        point(
            pose,
            vertices,
            (column + 1f) / columns,
            row / (float) rows,
            radius,
            top,
            bottom,
            color,
            phase,
            plume);
        point(
            pose,
            vertices,
            (column + 1f) / columns,
            (row + 1f) / rows,
            radius,
            top,
            bottom,
            color,
            phase,
            plume);
        point(
            pose,
            vertices,
            column / (float) columns,
            (row + 1f) / rows,
            radius,
            top,
            bottom,
            color,
            phase,
            plume);
      }
  }

  private static void point(
      PoseStack pose,
      VertexConsumer vertices,
      float u,
      float v,
      float radius,
      float top,
      float bottom,
      int color,
      double phase,
      boolean plume) {
    float x = u * 2 - 1;
    float alpha = (color >>> 24) / 255f;
    float red, green, blue;
    if (plume) {
      float width = .1f + .75f * (float) Math.pow(v, .7);
      float shape = 1 - smooth(.35f, 1, Math.abs(x));
      x *= width;
      alpha *= shape * (1 - smooth(.62f, 1, v));
      float heat = shape * shape;
      float diamond = (float) Math.pow(Math.max(0, 1 - Math.abs((v * 6 + .1f) % 1 - .5f) * 2), 3);
      float shock = .1f * diamond * (1 - smooth(.08f, .8f, v)) * heat;
      red = Mth.lerp(heat, ((color >> 16) & 255) / 255f, 1);
      green = Mth.lerp(heat, ((color >> 8) & 255) / 255f * .55f, .97f);
      blue = Mth.lerp(heat, (color & 255) / 255f * .25f, .78f);
      red = Mth.lerp(shock, red, 1);
      green = Mth.lerp(shock, green, .12f);
      blue = Mth.lerp(shock, blue, .045f);
      alpha *= .92f + .08f * (float) Math.sin(v * 24 - phase * .2);
    } else {
      float y = v * 2 - 1;
      float distance = (float) Math.sqrt(x * x + y * y);
      x *= (float) Math.sqrt(Math.max(0, 1 - y * y));
      float billow =
          .84f
              + .12f * (float) Math.sin(u * 17 + phase * 19) * (float) Math.sin(v * 13 + phase * 7);
      alpha *= 1 - smooth(.25f, billow, distance);
      red = green = blue = .48f + .15f * (1 - distance);
    }
    vertices
        .addVertex(pose.last().pose(), x * radius, Mth.lerp(v, top, bottom), 0)
        .setColor(red, green, blue, Math.clamp(alpha, 0, 1))
        .setUv(.5f, .5f)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(LightTexture.FULL_BRIGHT)
        .setNormal(pose.last(), 0, 0, 1);
  }

  private static float smooth(float low, float high, float value) {
    float t = Mth.clamp((value - low) / (high - low), 0, 1);
    return t * t * (3 - 2 * t);
  }
}

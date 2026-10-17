package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.space.Planet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

/** Thin atmospheric limb and restrained auroral curtains on atmospheric ring worlds. */
public final class PlanetAccents {
  private PlanetAccents() {}

  public static void render(PoseStack pose, Planet planet, float size, float alpha) {
    if (!AdvancedRocketryConfig.advancedVfx() || !planet.hasShading() || planet.atmosphere <= 25)
      return;
    var profile = EffectProfiles.get("planet");
    PlanetClouds.render(pose, size, alpha);
    var aurora = EffectProfiles.get("celestial");
    if (!profile.enabled() && !(aurora.enabled() && planet.rings)) return;
    RenderSystem.setShaderColor(1, 1, 1, 1);
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    var vertices =
        Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    if (profile.enabled()) {
      for (int layer = 0; layer < 5; layer++) {
        float opacity = alpha * .16f * (1 - layer / 5f);
        EffectGeometry.ring(
            pose,
            vertices,
            size * (1 + layer * .008),
            99.8,
            size * .005,
            profile.argb(opacity),
            EffectGeometry.segments(profile));
      }
    }
    if (aurora.enabled() && planet.rings) {
      double time = EffectGeometry.time(Minecraft.getInstance().level, 0, aurora);
      for (int i = 0; i < EffectGeometry.segments(aurora); i++) {
        double a = i * Math.PI / EffectGeometry.segments(aurora),
            b = (i + 1) * Math.PI / EffectGeometry.segments(aurora);
        float x0 = (float) Math.cos(a) * size * .65f, x1 = (float) Math.cos(b) * size * .65f;
        float z0 = (float) Math.sin(a) * size * .16f - size * .65f;
        float z1 = (float) Math.sin(b) * size * .16f - size * .65f;
        float curtain = size * (float) (.04 + .025 * Math.sin(a * 8 + time * .025));
        vertices.addVertex(pose.last().pose(), x0, 99.7f, z0).setColor(aurora.argb(alpha * .25f));
        vertices.addVertex(pose.last().pose(), x1, 99.7f, z1).setColor(aurora.argb(alpha * .25f));
        vertices.addVertex(pose.last().pose(), x1, 99.7f, z1 - curtain).setColor(aurora.argb(0));
        vertices.addVertex(pose.last().pose(), x0, 99.7f, z0 - curtain).setColor(aurora.argb(0));
      }
    }
    BufferUploader.drawWithShader(vertices.buildOrThrow());
  }
}

package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.client.effects.EffectProfiles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/** Sphere-projected procedural cloud bands, independent of the surface texture. */
public final class PlanetClouds {
  private static ShaderInstance shader;

  private PlanetClouds() {}

  public static void register(RegisterShadersEvent event) {
    shader = null;
    try {
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "planet_clouds"),
              DefaultVertexFormat.POSITION_TEX),
          loaded -> shader = loaded);
    } catch (IOException error) {
      LogUtils.getLogger().warn("Planet cloud shader unavailable", error);
    }
  }

  public static void render(PoseStack pose, float size, float alpha) {
    var profile = EffectProfiles.get("planet");
    if (shader == null
        || !profile.enabled()
        || size < 1
        || AdvancedRocketryConfig.effectDetail() == 0) return;
    shader
        .safeGetUniform("Motion")
        .set(AdvancedRocketryConfig.reducedMotion() ? 0f : profile.speed());
    RenderSystem.setShader(() -> shader);
    RenderSystem.setShaderColor(1, 1, 1, alpha * profile.opacity() * .4f);
    var buffer =
        Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    buffer.addVertex(pose.last().pose(), -size, 99.85f, -size).setUv(0, 0);
    buffer.addVertex(pose.last().pose(), size, 99.85f, -size).setUv(1, 0);
    buffer.addVertex(pose.last().pose(), size, 99.85f, size).setUv(1, 1);
    buffer.addVertex(pose.last().pose(), -size, 99.85f, size).setUv(0, 1);
    BufferUploader.drawWithShader(buffer.buildOrThrow());
    RenderSystem.setShaderColor(1, 1, 1, 1);
  }
}

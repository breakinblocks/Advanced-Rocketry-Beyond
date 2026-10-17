package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.ShaderCompatibility;
import advRocketry.space.Planet;
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
import net.minecraft.world.phys.Vec3;
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
          loaded -> shader = ShaderCompatibility.allowPlanetShader(loaded) ? loaded : null);
    } catch (IOException error) {
      LogUtils.getLogger().warn("Planet cloud shader unavailable", error);
    }
  }

  public static void render(PoseStack pose, Planet planet, float size, float alpha, float phase) {
    var profile = EffectProfiles.get("planet");
    if (shader == null
        || !profile.enabled()
        || size < 1
        || AdvancedRocketryConfig.effectDetail() == 0) return;
    var state = PlanetSkyState.resolve(planet, 80, 0, 0, 0, .8f);
    if (state.coverage() <= .001) return;
    var tint =
        planet.sky.clouds().color() < 0
            ? state.cloudColor()
            : Vec3.fromRGB24(planet.sky.clouds().color());
    shader.safeGetUniform("CloudColor").set((float) tint.x, (float) tint.y, (float) tint.z);
    shader.safeGetUniform("Coverage").set(state.coverage());
    shader.safeGetUniform("Seed").set((float) Math.floorMod(planet.id + planet.sky.seed(), 997));
    shader
        .safeGetUniform("LightDirection")
        .set((float) Math.sin(phase), 0f, (float) Math.cos(phase));
    shader
        .safeGetUniform("Motion")
        .set(AdvancedRocketryConfig.reducedMotion() ? 0f : planet.sky.clouds().speed());
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

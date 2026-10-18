package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.space.Planet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Layered sky geometry using standard textured/color passes understood by shader packs. */
public final class PlanetAtmosphere {
  private static final ResourceLocation SPARSE = texture("cloud_sparse");
  private static final ResourceLocation BROAD = texture("cloud_broad");
  private static final ResourceLocation WISPS = texture("cloud_wisps");

  private PlanetAtmosphere() {}

  private static ResourceLocation texture(String name) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/sky/" + name + ".png");
  }

  public static void background(
      Matrix4f view, Planet planet, PlanetSkyState state, Vec3 base, float angle) {
    RenderSystem.setShader(SkyLayerShader::color);
    var buffer =
        Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    int segments = 48, bands = 16;
    Vec3 fog = Vec3.fromRGB24(planet.fogColor);
    double light = .025 + .55 * state.daylight();
    fog = fog.scale(light * Math.min(1, state.density()));
    for (int y = 0; y < bands; y++)
      for (int x = 0; x < segments; x++) {
        backgroundVertex(buffer, view, planet, state, base, fog, angle, x, y, segments, bands);
        backgroundVertex(buffer, view, planet, state, base, fog, angle, x + 1, y, segments, bands);
        backgroundVertex(
            buffer, view, planet, state, base, fog, angle, x + 1, y + 1, segments, bands);
        backgroundVertex(buffer, view, planet, state, base, fog, angle, x, y + 1, segments, bands);
      }
    BufferUploader.drawWithShader(buffer.buildOrThrow());
  }

  private static void backgroundVertex(
      BufferBuilder buffer,
      Matrix4f view,
      Planet planet,
      PlanetSkyState state,
      Vec3 base,
      Vec3 fog,
      float angle,
      int x,
      int y,
      int segments,
      int bands) {
    double latitude = Math.PI * (y / (double) bands - .5), longitude = x * Math.PI * 2 / segments;
    float sy = (float) Math.sin(latitude), sx = (float) (Math.cos(latitude) * Math.cos(longitude));
    float sz = (float) (Math.cos(latitude) * Math.sin(longitude));
    double horizon =
        Math.pow(1 - Math.abs(sy), 4) * Math.min(1, state.density() * planet.sky.haze());
    double mix = sy < 0 ? .8 : horizon * .65;
    double weight = (.68 + .32 * Math.max(0, sy)) * (1 - mix);
    double red = base.x * weight + fog.x * mix;
    double green = base.y * weight + fog.y * mix;
    double blue = base.z * weight + fog.z * mix;
    double facing = Math.max(0, -Math.sin(angle * Math.PI * 2) * sx);
    double sunset =
        Math.min(
            .85,
            state.twilight()
                * planet.sky.sunset()
                * horizon
                * (.12 + .65 * facing * facing * facing));
    double dim = 1 - state.weather() * .35;
    red = (red * (1 - sunset) + (.7 + ((planet.skyColor >> 16) & 255) / 255d * .3) * sunset) * dim;
    green =
        (green * (1 - sunset) + (.224 + ((planet.skyColor >> 8) & 255) / 255d * .3) * sunset) * dim;
    blue = (blue * (1 - sunset) + (.07 + (planet.skyColor & 255) / 255d * .3) * sunset) * dim;
    buffer
        .addVertex(view, sx * 100, sy * 100, sz * 100)
        .setColor((float) red, (float) green, (float) blue, 1);
  }

  public static void clouds(
      Matrix4f view, Planet planet, PlanetSkyState state, double ticks, Vec3 camera) {
    if (!AdvancedRocketryConfig.advancedVfx()
        || state.coverage() <= .001
        || AdvancedRocketryConfig.effectDetail() == 0) return;
    double motion =
        AdvancedRocketryConfig.reducedMotion() ? 0 : ticks * planet.sky.clouds().speed() / 24000d;
    double direction = Math.toRadians(planet.sky.clouds().direction());
    double seed = Math.floorMod(planet.id * 31L + planet.sky.seed(), 997) / 997d;
    double u = motion * Math.cos(direction) + seed + camera.x / 4096;
    double v = motion * Math.sin(direction) + seed + camera.z / 4096;
    float broad = PlanetSkyState.smooth(.35f, .85f, state.coverage());
    Vec3 color =
        state.cloudColor().scale((.14 + state.daylight() * .86) * (1 - state.weather() * .4));
    color =
        color.lerp(
            new Vec3(1, .48, .25), Math.min(.55, state.twilight() * planet.sky.sunset() * .5));
    float opacity = Math.min(1, state.coverage() * 1.7f);
    layer(view, SPARSE, color, opacity * (1 - broad), u, v, 1);
    layer(view, BROAD, color, opacity * broad, u, v, 1);
    if (AdvancedRocketryConfig.effectDetail() > 1)
      layer(view, WISPS, color, opacity * .4f, u * .63 + .3, v * .7, 1.25);
  }

  private static void layer(
      Matrix4f view,
      ResourceLocation texture,
      Vec3 color,
      float alpha,
      double u,
      double v,
      double scale) {
    if (alpha < .001) return;
    RenderSystem.setShader(SkyLayerShader::texture);
    RenderSystem.setShaderTexture(0, texture);
    var skyTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
    skyTexture.setBlurMipmap(true, false);
    try {
      var buffer =
          Tesselator.getInstance()
              .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      int segments = AdvancedRocketryConfig.effectDetail() > 1 ? 48 : 24;
      for (int y = 0; y < 24; y++)
        for (int x = 0; x < segments; x++) {
          cloudVertex(buffer, view, color, alpha, u, v, scale, x, y, segments);
          cloudVertex(buffer, view, color, alpha, u, v, scale, x + 1, y, segments);
          cloudVertex(buffer, view, color, alpha, u, v, scale, x + 1, y + 1, segments);
          cloudVertex(buffer, view, color, alpha, u, v, scale, x, y + 1, segments);
        }
      BufferUploader.drawWithShader(buffer.buildOrThrow());
    } finally {
      skyTexture.restoreLastBlurMipmap();
    }
  }

  private static void cloudVertex(
      BufferBuilder buffer,
      Matrix4f view,
      Vec3 color,
      float alpha,
      double u,
      double v,
      double scale,
      int x,
      int y,
      int segments) {
    double elevation = y * Math.PI / 48, azimuth = x * Math.PI * 2 / segments;
    float sy = (float) Math.sin(elevation), sx = (float) (Math.cos(elevation) * Math.cos(azimuth));
    float sz = (float) (Math.cos(elevation) * Math.sin(azimuth));
    float fade = PlanetSkyState.smooth(0, .23f, sy);
    buffer
        .addVertex(view, sx * 100, sy * 100, sz * 100)
        .setUv(
            (float) (sx / Math.max(.12, sy) * scale * .4 + u % 1),
            (float) (sz / Math.max(.12, sy) * scale * .4 + v % 1))
        .setColor((float) color.x, (float) color.y, (float) color.z, alpha * fade);
  }

  public static void night(Matrix4f view, Planet planet, PlanetSkyState state, double ticks) {
    if (!AdvancedRocketryConfig.advancedVfx() || AdvancedRocketryConfig.effectDetail() == 0) return;
    float visible =
        Math.max(state.night(), 1 - Math.min(1, state.density())) * (1 - state.weather());
    if (visible <= .001) return;
    float opacity = visible * Math.min(1, state.density()) * planet.sky.aurora();
    if (opacity <= .001) return;
    double time = AdvancedRocketryConfig.reducedMotion() ? 0 : ticks / 160d;
    RenderSystem.setShader(SkyLayerShader::color);
    var buffer =
        Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    for (int i = 0; i < 96; i++) {
      for (int band = 0; band < 8; band++) {
        float low = band / 8f, high = (band + 1) / 8f;
        auroraVertex(buffer, view, planet.sky.seed(), time, i, low, opacity);
        auroraVertex(buffer, view, planet.sky.seed(), time, i + 1, low, opacity);
        auroraVertex(buffer, view, planet.sky.seed(), time, i + 1, high, opacity);
        auroraVertex(buffer, view, planet.sky.seed(), time, i, high, opacity);
      }
    }
    BufferUploader.drawWithShader(buffer.buildOrThrow());
  }

  private static void auroraVertex(
      BufferBuilder buffer,
      Matrix4f view,
      int seed,
      double time,
      int i,
      float vertical,
      float opacity) {
    double a = i / 96d * Math.PI * 2;
    double wave = Math.sin(a * 5 + time + seed) * .08 + Math.sin(a * 11 - time * .7) * .025;
    double elevation = .45 + wave + vertical * (.22 + .06 * Math.sin(a * 3 + time));
    float r = .16f + vertical * .32f, g = .85f - vertical * .6f;
    float fade =
        opacity
            * (float) Math.sin(vertical * Math.PI)
            * (.2f + .3f * (float) Math.pow(Math.sin(a * 3 + time * .2), 2));
    buffer
        .addVertex(
            view,
            (float) (Math.cos(a) * Math.cos(elevation) * 100),
            (float) (Math.sin(elevation) * 100),
            (float) (Math.sin(a) * Math.cos(elevation) * 100))
        .setColor(r, g, .65f, fade);
  }
}

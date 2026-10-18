// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space.client;

import advRocketry.Main;
import advRocketry.client.effects.ShaderCompatibility;
import advRocketry.orbit.Station;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.SpaceNavigation;
import advRocketry.space.AtmosphereVisuals;
import advRocketry.space.GalaxyData;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Native sky buffers retaining the original atmosphere fade and apparent celestial-body size. */
public final class PlanetSky {
  private static final ResourceLocation SUN =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/legacy/advancedrocketry/env/sun.png");
  private static final ResourceLocation BLACK_HOLE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/legacy/advancedrocketry/env/blackhole.png");
  private static final ResourceLocation ACCRETION_DISK =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/legacy/advancedrocketry/env/accretiondisk.png");
  private static final ResourceLocation RINGS = planetTexture("rings");
  private static final ResourceLocation SHADOW = planetTexture("shadow");
  private static final ResourceLocation ATMOSPHERE = planetTexture("atmosphere");

  private static ResourceLocation planetTexture(String name) {
    return ResourceLocation.fromNamespaceAndPath(
        Main.MODID, "textures/legacy/advancedrocketry/planets/" + name + ".png");
  }

  private static final Map<String, ResourceLocation> ICONS = new HashMap<>();

  private PlanetSky() {}

  public static void render(
      ClientLevel level, Planet planet, Matrix4f view, float partialTick, Runnable setupFog) {
    if (ShaderCompatibility.shadowPass()) return;
    Matrix4f skyView = new Matrix4f(view);
    Station station = null;
    if (planet.id == GalaxyData.SPACE_ID && Minecraft.getInstance().player != null) {
      station = GalaxySync.clientStationAt(Minecraft.getInstance().player.blockPosition());
      if (station != null) {
        double elapsed =
            station.anchored
                ? 0
                : Math.max(0, level.getGameTime() - station.rotationTime + partialTick);
        for (int axis = 0; axis < 3; axis++) {
          float angle =
              (float)
                  -Math.toRadians(
                      station.rotationAngle[axis] + station.rotationVelocity[axis] * elapsed);
          switch (axis) {
            case 0 -> skyView.rotateX(angle);
            case 1 -> skyView.rotateY(angle);
            default -> skyView.rotateZ(angle);
          }
        }
      }
    }
    Planet orbiting = station == null ? null : GalaxySync.clientPlanet(station.orbitPlanet);
    Planet source = (orbiting != null ? orbiting : planet).root(GalaxySync::clientPlanet);
    Star star = GalaxySync.clientStar(source.star);
    int distance = source.orbitalDistance;
    double visual = AtmosphereVisuals.perceivedLight(star, distance);
    float daylight =
        (float)
            (Math.max(0, Math.cos(level.getTimeOfDay(partialTick) * Math.PI * 2))
                * (PlanetEffects.visor("night_vision_upgrade") ? 1 : Math.clamp(visual, .05, 1.5)));
    float density =
        AtmosphereVisuals.densityAtHeight(
            planet.atmosphere,
            Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y);
    float atmosphere = Math.clamp(density, 0, 1);
    Vec3 color = Vec3.fromRGB24(planet.skyColor);
    if (!planet.colorOverride)
      color =
          color.multiply(
              level.getSkyColor(
                  Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), partialTick));
    double illumination =
        illumination(
            planet, level.getTimeOfDay(partialTick), level.getGameTime() + (double) partialTick);
    if (planet.id != GalaxyData.EARTH_ID) color = color.scale(illumination);
    color =
        new Vec3(Math.clamp(color.x, 0, 1), Math.clamp(color.y, 0, 1), Math.clamp(color.z, 0, 1));
    double thickness = Math.sqrt(Math.max(density, .81));
    color =
        planet.atmosphere == 0
            ? Vec3.ZERO
            : new Vec3(
                    Math.pow(color.x, thickness),
                    Math.pow(color.y, thickness),
                    Math.pow(color.z, thickness))
                .scale(atmosphere);
    RenderSystem.depthMask(false);
    RenderSystem.disableCull();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    FogRenderer.setupNoFog();
    RenderSystem.setShaderColor(1, 1, 1, 1);
    var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    var state =
        PlanetSkyState.resolve(
                planet,
                camera.y,
                level.getTimeOfDay(partialTick),
                level.getRainLevel(partialTick),
                level.getThunderLevel(partialTick),
                level
                    .getBiome(
                        Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition())
                    .value()
                    .getBaseTemperature())
            .litBy(illumination);
    PlanetAtmosphere.background(skyView, planet, state, color, level.getTimeOfDay(partialTick));
    PlanetAtmosphere.night(skyView, planet, state, level.getGameTime() + (double) partialTick);
    float starBrightness =
        Math.clamp(
            Math.max(state.night(), 1 - Math.min(1, state.density()))
                * (1 - state.weather())
                * planet.sky.stars()
                / Math.max(1, state.density()),
            0,
            1);
    Matrix4f starView =
        new Matrix4f(RenderSystem.getModelViewMatrix())
            .mul(skyView)
            .rotateY((float) (-Math.PI / 2))
            .rotateX(level.getTimeOfDay(partialTick) * (float) (Math.PI * 2));
    GalacticSky.render(
        starView, planet, star, state, starBrightness, level.getGameTime() + (double) partialTick);
    var player = Minecraft.getInstance().player;
    SpaceNavigation navigation =
        player != null && player.getVehicle() instanceof RocketEntity rocket
            ? rocket.navigation()
            : null;
    if (navigation != null) {
      drawNavigation(skyView, navigation, level.getGameTime() + (double) partialTick);
      RenderSystem.setShaderColor(1, 1, 1, 1);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.depthMask(true);
      setupFog.run();
      return;
    }
    PoseStack pose = new PoseStack();
    pose.mulPose(skyView);
    pose.mulPose(Axis.YP.rotationDegrees(-90));
    pose.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360));
    float bodyScale = 100f / Math.max(1, distance);
    float time = level.getGameTime() + partialTick;
    drawStar(
        pose,
        star == null ? 100 : star.temperature(),
        star == null ? 1 : star.size(),
        star != null && star.blackHole(),
        bodyScale,
        time);
    if (star != null && !star.companions().isEmpty()) {
      pose.pushPose();
      float phase = 360f / star.companions().size();
      for (Star.Companion companion : star.companions()) {
        pose.mulPose(Axis.YP.rotationDegrees(phase));
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(companion.separation() * bodyScale));
        drawStar(
            pose,
            companion.temperature(),
            companion.size(),
            companion.blackHole(),
            bodyScale,
            time);
        pose.popPose();
      }
      pose.popPose();
    }
    RenderSystem.setShaderColor(1, 1, 1, 1);
    if (orbiting != null) {
      pose.pushPose();
      pose.mulPose(Axis.XP.rotationDegrees(180));
      float altitude = station.altitude;
      drawPlanet(pose, orbiting, apparentSize(orbiting, altitude), 0, 1);
      pose.popPose();
    } else if (planet.parent >= 0) {
      Planet parent = GalaxySync.clientPlanet(planet.parent);
      if (parent != null) {
        double angle =
            planet.orbitalAngle(level.getGameTime() + (double) partialTick, star, parent);
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees((float) planet.orbitalPhi));
        pose.mulPose(Axis.XP.rotation((float) (Math.PI + angle)));
        drawPlanet(
            pose,
            parent,
            apparentSize(parent, planet.orbitalDistance),
            (float) angle,
            1 - atmosphere * daylight * .6f * (float) Math.clamp(illumination, 0, 1));
        pose.popPose();
      }
    }
    for (Planet moon : GalaxySync.clientPlanets()) {
      if (moon.parent != planet.id) continue;
      double angle = moon.orbitalAngle(level.getGameTime() + (double) partialTick, star, planet);
      pose.pushPose();
      pose.mulPose(Axis.ZP.rotationDegrees((float) moon.orbitalPhi));
      pose.mulPose(Axis.XP.rotation((float) angle));
      drawPlanet(
          pose,
          moon,
          apparentSize(moon, moon.orbitalDistance),
          (float) angle,
          1 - atmosphere * daylight * .6f * (float) Math.clamp(illumination, 0, 1));
      pose.popPose();
    }
    if (planet.rings) {
      pose.pushPose();
      pose.mulPose(Axis.XP.rotationDegrees(70));
      tint(planet.ringColor, .5f);
      quad(pose.last().pose(), RINGS, 150);
      pose.popPose();
    }
    PlanetAtmosphere.clouds(
        skyView, planet, state, level.getGameTime() + (double) partialTick, camera);
    RenderSystem.setShaderColor(1, 1, 1, 1);
    RenderSystem.disableBlend();
    RenderSystem.enableCull();
    RenderSystem.depthMask(true);
    setupFog.run();
  }

  static double illumination(Planet planet, float celestialAngle, double ticks) {
    Planet root = planet.root(GalaxySync::clientPlanet);
    Star star = GalaxySync.clientStar(root.star);
    double light =
        AtmosphereVisuals.surfaceLight(
            planet.atmosphere,
            celestialAngle,
            AtmosphereVisuals.perceivedLight(star, root.orbitalDistance),
            PlanetEffects.visor("night_vision_upgrade"));
    if (planet.parent >= 0) {
      light *=
          AtmosphereVisuals.eclipse(
              planet,
              planet.orbitalAngle(ticks, star, GalaxySync.clientPlanet(planet.parent)),
              root.orbitalDistance);
    } else {
      for (Planet moon : GalaxySync.clientPlanets())
        if (moon.parent == planet.id)
          light *=
              AtmosphereVisuals.eclipse(
                  moon, moon.orbitalAngle(ticks, star, planet), root.orbitalDistance);
    }
    return light;
  }

  private static float apparentSize(Planet planet, float distance) {
    return (float)
        Math.clamp(20 * 100 / Math.max(1, distance) * Math.pow(planet.gravity, .4), .2, 160);
  }

  private static void drawNavigation(Matrix4f view, SpaceNavigation navigation, double ticks) {
    Star star = GalaxySync.clientStar(navigation.star);
    Planet focus = GalaxySync.clientPlanet(navigation.focus);
    Vec3 stellarPosition =
        focus == null
            ? Vec3.ZERO
            : SpaceNavigation.bodyPosition(focus, star, null, ticks).scale(-1);
    Vec3 sunDirection = stellarPosition.subtract(navigation.position);
    PoseStack pose = facing(view, sunDirection);
    float scale = (float) (10000 / Math.max(1, sunDirection.length()));
    drawStar(
        pose,
        star == null ? 100 : star.temperature(),
        star == null ? 1 : star.size(),
        star != null && star.blackHole(),
        scale,
        (float) ticks);
    if (star != null && !star.companions().isEmpty()) {
      float phase = 360f / star.companions().size();
      for (Star.Companion companion : star.companions()) {
        pose.mulPose(Axis.YP.rotationDegrees(phase));
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(companion.separation() * scale));
        drawStar(
            pose,
            companion.temperature(),
            companion.size(),
            companion.blackHole(),
            scale,
            (float) ticks);
        pose.popPose();
      }
    }
    for (Planet body : GalaxySync.clientPlanets()) {
      if (body.id < 0
          || body.star != navigation.star
          || (focus == null ? body.parent >= 0 : body.id != focus.id && body.parent != focus.id))
        continue;
      Vec3 bodyPosition =
          focus != null && body.id == focus.id
              ? Vec3.ZERO
              : SpaceNavigation.bodyPosition(body, star, focus, ticks);
      Vec3 delta = bodyPosition.subtract(navigation.position);
      float size =
          (float)
              Math.clamp(
                  SpaceNavigation.radius(body, focus == null) * 100 / Math.max(1, delta.length()),
                  .05,
                  160);
      drawPlanet(facing(view, delta), body, size, (float) Math.atan2(delta.z, delta.x), 1);
      if (focus != null) {
        var stations =
            GalaxySync.clientStations().entrySet().stream()
                .filter(entry -> entry.getValue().orbits(body.id) && !entry.getValue().inWarp())
                .sorted(Map.Entry.comparingByKey())
                .toList();
        for (int index = 0; index < stations.size(); index++) {
          Vec3 location =
              SpaceNavigation.stationPosition(body, bodyPosition, index, stations.size())
                  .subtract(navigation.position);
          tint(0x70ffff, 1);
          quad(
              facing(view, location).last().pose(),
              SUN,
              (float) Math.clamp(3000 / Math.max(1, location.length()), .2, 12));
        }
      }
    }
  }

  private static PoseStack facing(Matrix4f view, Vec3 direction) {
    Vec3 normalized = direction.lengthSqr() < .0001 ? new Vec3(0, 1, 0) : direction.normalize();
    PoseStack pose = new PoseStack();
    pose.mulPose(view);
    pose.mulPose(
        new Quaternionf()
            .rotationTo(0, 1, 0, (float) normalized.x, (float) normalized.y, (float) normalized.z));
    return pose;
  }

  private static void tint(int color, float alpha) {
    RenderSystem.setShaderColor(
        (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, alpha);
  }

  private static void drawPlanet(
      PoseStack pose, Planet planet, float size, float shadowAngle, float alpha) {
    ResourceLocation icon =
        planet.customIcon.isBlank()
            ? planet.icon()
            : ICONS.computeIfAbsent(planet.customIcon, ignored -> customIcon(planet));
    if (planet.hasShading() && planet.rings) {
      tint(planet.ringColor, alpha);
      quad(pose.last().pose(), RINGS, size * 1.4f);
    }
    RenderSystem.setShaderColor(1, 1, 1, alpha);
    quad(pose.last().pose(), icon, size);
    PlanetAccents.render(pose, planet, size, alpha, shadowAngle);
    if (planet.hasShading()) {
      if (planet.atmosphere > 25) {
        tint(planet.skyColor, alpha * .5f);
        quad(pose.last().pose(), ATMOSPHERE, size);
      }
      pose.pushPose();
      pose.mulPose(Axis.YP.rotation((float) Math.PI / 2 - shadowAngle));
      RenderSystem.setShaderColor(0, 0, 0, alpha);
      quad(pose.last().pose(), SHADOW, size * 1.05f);
      pose.popPose();
    }
    RenderSystem.setShaderColor(1, 1, 1, 1);
  }

  private static ResourceLocation customIcon(Planet planet) {
    ResourceLocation icon = planet.icon();
    var resources = Minecraft.getInstance().getResourceManager();
    if (resources.getResource(icon).isPresent()) return icon;
    ResourceLocation legacy =
        planet.customIcon.contains(":")
            ? planetTexture("moon")
            : planetTexture(planet.customIcon.toLowerCase(Locale.ROOT));
    return resources.getResource(legacy).isPresent() ? legacy : planetTexture("moon");
  }

  public static void clear() {
    ICONS.clear();
    GalacticSky.clear();
  }

  private static void drawStar(
      PoseStack pose, int temperature, float size, boolean blackHole, float bodyScale, float time) {
    if (!blackHole) {
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
      float[] color = Star.color(temperature);
      RenderSystem.setShaderColor(color[0], color[1], color[2], 1);
      quad(pose.last().pose(), SUN, size * 15 * bodyScale);
      RenderSystem.defaultBlendFunc();
      return;
    }
    pose.pushPose();
    pose.translate(0, 100, 0);
    pose.mulPose(Axis.YP.rotationDegrees(-time * 5));
    RenderSystem.setShaderColor(1, .5f, .4f, 1);
    quad(pose.last().pose(), BLACK_HOLE, size * 2.5f * bodyScale, 0);
    pose.popPose();
    for (int layer = 0; layer < 3; layer++) {
      pose.pushPose();
      pose.translate(0, 100, 0);
      pose.mulPose(Axis.XP.rotationDegrees(80));
      pose.mulPose(Axis.YP.rotationDegrees(time / (2f + layer)));
      RenderSystem.setShaderColor(1, .5f + layer * .15f, .4f + layer * .2f, 1);
      quad(pose.last().pose(), ACCRETION_DISK, size * (20 - layer * 6.25f) * bodyScale, 0);
      pose.popPose();
    }
  }

  private static void quad(Matrix4f matrix, ResourceLocation texture, float size) {
    quad(matrix, texture, size, 100);
  }

  private static void quad(Matrix4f matrix, ResourceLocation texture, float size, float height) {
    RenderSystem.setShader(SkyLayerShader::texture);
    RenderSystem.setShaderTexture(0, texture);
    // These sky sprites are magnified far beyond their native resolution.
    // Restore the resource pack's filter after this draw, including on failure.
    var skyTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
    skyTexture.setBlurMipmap(true, false);
    try {
      BufferBuilder buffer =
          Tesselator.getInstance()
              .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      buffer.addVertex(matrix, -size, height, -size).setUv(0, 0).setColor(255, 255, 255, 255);
      buffer.addVertex(matrix, size, height, -size).setUv(1, 0).setColor(255, 255, 255, 255);
      buffer.addVertex(matrix, size, height, size).setUv(1, 1).setColor(255, 255, 255, 255);
      buffer.addVertex(matrix, -size, height, size).setUv(0, 1).setColor(255, 255, 255, 255);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
    } finally {
      skyTexture.restoreLastBlurMipmap();
    }
  }
}

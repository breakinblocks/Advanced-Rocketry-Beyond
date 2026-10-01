// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.life.AtmosphereSync;
import advRocketry.life.SpaceSuitItem;
import advRocketry.orbit.BeaconFinderItem;
import advRocketry.rocket.RocketEntity;
import advRocketry.space.AtmosphereVisuals;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;

/** Atmosphere-dependent native sky behavior, using synchronized original planet properties. */
public final class PlanetEffects extends DimensionSpecialEffects {
  private static final float OVERWORLD_CLOUDS = 192;
  private final boolean space;
  private final boolean overworld;

  public PlanetEffects(boolean space, boolean overworld) {
    super(overworld ? OVERWORLD_CLOUDS : Float.NaN, !space, SkyType.NORMAL, false, false);
    this.space = space;
    this.overworld = overworld;
  }

  public static void register(RegisterDimensionSpecialEffectsEvent event) {
    event.register(
        ResourceLocation.fromNamespaceAndPath(Main.MODID, "planet"),
        new PlanetEffects(false, false));
    event.register(
        ResourceLocation.fromNamespaceAndPath(Main.MODID, "space"), new PlanetEffects(true, false));
    event.register(Level.OVERWORLD.location(), new PlanetEffects(false, true));
  }

  private Planet planet() {
    var level = Minecraft.getInstance().level;
    return level == null ? null : GalaxySync.clientPlanet(level.dimension().location());
  }

  static boolean visor(String id) {
    var player = Minecraft.getInstance().player;
    if (player == null) return false;
    var helmet = player.getItemBySlot(EquipmentSlot.HEAD);
    return helmet.getItem() instanceof SpaceSuitItem suit && suit.countModule(helmet, id) > 0;
  }

  @Override
  public boolean renderSky(
      ClientLevel level,
      int ticks,
      float partialTick,
      Matrix4f view,
      Camera camera,
      Matrix4f projection,
      boolean foggy,
      Runnable setupFog) {
    Planet planet = planet();
    var player = Minecraft.getInstance().player;
    boolean freeFlight =
        player != null
            && player.getVehicle() instanceof RocketEntity rocket
            && rocket.navigation() != null;
    if (planet == null
        || !freeFlight
            && (planet.skyRenderOverride
                || !(overworld
                    ? AdvancedRocketryConfig.overworldSkyOverride()
                    : AdvancedRocketryConfig.skyOverride(space)))
        || foggy
        || camera.getFluidInCamera() != FogType.NONE) return false;
    PlanetSky.render(level, planet, view, partialTick, setupFog);
    return true;
  }

  @Override
  public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
    Planet planet = planet();
    if (space || planet != null && planet.atmosphere == 0) return Vec3.ZERO;
    Vec3 tint = overworld || planet == null ? color : Vec3.fromRGB24(planet.fogColor);
    if (!overworld && planet != null && !planet.colorOverride) tint = tint.multiply(color);
    if (!overworld && planet != null) {
      var level = Minecraft.getInstance().level;
      if (level != null)
        tint =
            tint.scale(PlanetSky.illumination(planet, level.getTimeOfDay(0), level.getGameTime()));
    }
    tint =
        tint.multiply(brightness * 0.94 + 0.06, brightness * 0.94 + 0.06, brightness * 0.91 + 0.09);
    return new Vec3(Math.clamp(tint.x, 0, 1), Math.clamp(tint.y, 0, 1), Math.clamp(tint.z, 0, 1));
  }

  public static void disconnected(ClientPlayerNetworkEvent.LoggingOut event) {
    AtmosphereSync.clearClient();
    BeaconFinderItem.clearClient();
    GalaxySync.clearClient();
    PlanetSky.clear();
  }

  public static void fog(ViewportEvent.RenderFog event) {
    var client = Minecraft.getInstance();
    if (client.level == null
        || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN
        || event.getType() != FogType.NONE) return;
    if (event.getCamera().getEntity() instanceof LivingEntity living
        && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS)))
      return;
    Planet planet = GalaxySync.clientPlanet(client.level.dimension().location());
    if (planet == null) return;
    int pressure =
        AtmosphereSync.clientPressure(client.level.dimension().location(), planet.atmosphere);
    var fog =
        AtmosphereVisuals.fog(pressure, visor("atmosphere_upgrade"), event.getFarPlaneDistance());
    event.setNearPlaneDistance(fog.near());
    event.setFarPlaneDistance(fog.far());
    event.setCanceled(true);
  }

  @Override
  public boolean isFoggyAt(int x, int z) {
    return false;
  }

  @Override
  public float[] getSunriseColor(float time, float partialTicks) {
    Planet planet = planet();
    return !overworld && (space || planet != null && planet.atmosphere == 0)
        ? null
        : super.getSunriseColor(time, partialTicks);
  }

  @Override
  public float getCloudHeight() {
    Planet planet = planet();
    if (planet == null) return super.getCloudHeight();
    return !space && planet.atmosphere > 75 ? OVERWORLD_CLOUDS : Float.NaN;
  }
}

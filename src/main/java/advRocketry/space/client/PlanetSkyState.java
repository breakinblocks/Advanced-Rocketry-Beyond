package advRocketry.space.client;

import advRocketry.space.AtmosphereVisuals;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.SkySettings;
import advRocketry.space.TerrainType;
import net.minecraft.world.phys.Vec3;

/** One bounded visual state shared by atmosphere, clouds and night layers. */
public record PlanetSkyState(
    float density,
    float daylight,
    float twilight,
    float night,
    float coverage,
    float weather,
    Vec3 cloudColor,
    SkySettings.Profile profile) {
  public static PlanetSkyState resolve(
      Planet planet,
      double height,
      float celestialAngle,
      float rain,
      float thunder,
      float biomeTemperature) {
    float density =
        planet.id == GalaxyData.SPACE_ID
            ? 0
            : AtmosphereVisuals.densityAtHeight(planet.atmosphere, height);
    float sun = (float) Math.cos(celestialAngle * Math.PI * 2);
    float day = smooth(-.15f, .3f, sun);
    float twilight = (1 - smooth(.05f, .4f, Math.abs(sun))) * Math.min(1, density);
    float weather = Math.clamp(Math.max(rain, thunder), 0, 1);
    SkySettings.Profile profile = planet.sky.profile();
    if (profile == SkySettings.Profile.AUTO) {
      profile =
          planet.atmosphere <= 25
              ? SkySettings.Profile.CLEAR
              : planet.terrain == TerrainType.VOLCANIC
                  ? SkySettings.Profile.ASH
                  : planet.atmosphere > 200
                      ? SkySettings.Profile.DENSE
                      : planet.temperature <= 250 || biomeTemperature < .15f
                          ? SkySettings.Profile.FROZEN
                          : SkySettings.Profile.TEMPERATE;
    }
    float coverage = planet.sky.clouds().coverage();
    if (coverage < 0)
      coverage =
          switch (profile) {
            case CLEAR -> .12f;
            case DENSE -> .85f;
            case ASH -> .65f;
            case FROZEN -> .35f;
            default -> .48f;
          };
    coverage =
        planet.sky.clouds().coverage() == 0
            ? 0
            : Math.clamp(coverage + weather * (1 - coverage) * .7f, 0, 1);
    coverage *=
        Math.min(1, density)
            * (1
                - smooth(
                    planet.sky.clouds().height(),
                    planet.sky.clouds().height() + 96,
                    (float) height));
    int color = planet.sky.clouds().color();
    Vec3 cloud =
        color >= 0
            ? Vec3.fromRGB24(color)
            : switch (profile) {
              case ASH -> new Vec3(.52, .43, .38);
              case FROZEN -> new Vec3(.78, .88, 1);
              default -> new Vec3(.93, .95, 1);
            };
    return new PlanetSkyState(density, day, twilight, 1 - day, coverage, weather, cloud, profile);
  }

  public PlanetSkyState litBy(double illumination) {
    float light = (float) Math.clamp(illumination, 0, 1);
    return new PlanetSkyState(
        density,
        daylight * light,
        twilight * light,
        Math.max(night, (1 - light) * daylight),
        coverage,
        weather,
        cloudColor,
        profile);
  }

  public static float smooth(float low, float high, float value) {
    float t = Math.clamp((value - low) / (high - low), 0, 1);
    return t * t * (3 - 2 * t);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

/** Original density-dependent fog distances, independent of the client rendering API. */
public final class AtmosphereVisuals {
  private AtmosphereVisuals() {}

  public record Fog(float near, float far) {}

  public static Fog fog(int pressure, boolean upgradedVisor, float distance) {
    int atmosphere = Math.clamp(pressure, 0, upgradedVisor ? 100 : 200);
    return atmosphere > 100
        ? new Fog(.75f * distance * (2 - atmosphere * atmosphere / 10000f), distance)
        : new Fog(
            .75f * distance * (2 - atmosphere / 100f), distance * (2.002f - atmosphere / 100f));
  }

  public static float densityAtHeight(int pressure, double height) {
    return Math.max(0, pressure) / 100f * (float) Math.clamp(1 + (256 - height) / 200, 0, 1);
  }

  public static double flux(Star star, int distance) {
    return star == null
        ? 1
        : (star.blackHole() ? .25 : 1)
            * Math.pow(star.size(), 2)
            * Math.pow(star.temperature() / 100d, 4)
            / Math.pow(Math.max(1, distance) / 100d, 2);
  }

  public static double perceivedLight(Star star, int solarDistance) {
    double flux = flux(star, solarDistance);
    return Math.pow(1.5, Math.log(Math.max(.001, flux)) / Math.log(2));
  }

  public static double surfaceLight(
      int pressure, float celestialAngle, double stellarLight, boolean nightVision) {
    double cosine = Math.cos(celestialAngle * Math.PI * 2);
    double atmosphereLight = 1 - Math.clamp(1 - (cosine * 2 + .2) - pressure / 400d, 0, 1);
    double daylight = (1 - Math.clamp(1 - (cosine * 2 + .2), 0, 1)) * .8 + .2;
    return atmosphereLight * daylight * (nightVision ? 1 : stellarLight);
  }

  /** Original eclipse attenuation around conjunction, weakened by orbital inclination. */
  public static double eclipse(Planet moon, double angle, int solarDistance) {
    double degrees = (Math.toDegrees(angle) % 360 + 360) % 360;
    double inclination =
        Math.clamp((Math.abs(Math.cos(Math.toRadians(moon.orbitalPhi))) - .95) * 20, 0, 1);
    int halfWidth = (int) ((200 - moon.orbitalDistance) / 2d);
    if (halfWidth <= 0 || degrees <= 180 - halfWidth || degrees >= 180 + halfWidth) return 1;
    double distance = solarDistance / (200 - moon.orbitalDistance + .00001);
    return inclination * Math.clamp(distance / 20 + Math.abs(degrees - 180) * distance / 10, 0, 1)
        + 1
        - inclination;
  }

  public static float[] twilight(float celestialAngle, float density, double light) {
    double cosine = Math.cos(celestialAngle * Math.PI * 2);
    if (density <= 0 || cosine < -.4 || cosine > .4) return null;
    double phase = cosine / .4 * .5 + .5;
    double alpha = 1 - (1 - Math.sin(phase * Math.PI)) * .99;
    return new float[] {
      (float) Math.clamp((phase * .3 + .7) * light, 0, 1),
      (float) Math.clamp((phase * phase * .7 + .2) * light, 0, 1),
      (float) Math.clamp((phase * phase * .1 + .2) * light, 0, 1),
      (float) Math.clamp(alpha * alpha * density, 0, 1)
    };
  }
}

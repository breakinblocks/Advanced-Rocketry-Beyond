// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.space.Planet;

/** Original PlanetaryTravelHelper distance taper for post-orbit rocket burns. */
public final class RocketTrajectory {
  private RocketTrajectory() {}

  public static int injectionBurn(Planet source, Planet target, boolean asteroid) {
    return injectionBurn(
        source,
        target,
        asteroid,
        AdvancedRocketryConfig.transBodyInjection(),
        AdvancedRocketryConfig.asteroidTbiBurnMultiplier(),
        AdvancedRocketryConfig.warpTbiBurnMultiplier());
  }

  static int injectionBurn(
      Planet source,
      Planet target,
      boolean asteroid,
      int base,
      double asteroidMultiplier,
      double warpMultiplier) {
    if (source == null || base <= 0 || !asteroid && target == null) return 0;
    if (!asteroid && source.id == target.id) return 0;
    if (asteroid) return (int) (base * Math.sqrt(asteroidMultiplier));
    boolean local =
        target.parent == source.id
            || source.parent == target.id
            || source.parent >= 0 && source.parent == target.parent;
    if (!local) return (int) (base * warpMultiplier);
    Planet moon =
        target.parent == source.id ? target : source.parent == target.id ? source : target;
    return (int) (base * Math.sqrt(moon.orbitalDistance / 100d));
  }
}

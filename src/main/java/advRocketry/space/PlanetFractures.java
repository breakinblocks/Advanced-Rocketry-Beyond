package advRocketry.space;

/** Separated island layers with smooth boundaries and gravity-shaped undersides. */
public final class PlanetFractures {
  private PlanetFractures() {}

  private static double center(long seed, int sea, int layer, double x, double z) {
    return Math.clamp(sea, 48, 120)
        - 20
        + layer * 74
        + PlanetLandforms.field(seed, 401 + layer, x, z, 240) * 24;
  }

  private static double mass(long seed, int layer, double x, double z) {
    double warp = PlanetLandforms.field(seed, 409 + layer, x, z, 290) * 45;
    return PlanetLandforms.field(seed, 419 + layer, x + warp, z - warp, 145 - layer * 20)
        + .16
        + PlanetLandforms.field(seed, 431 + layer, x, z, 42) * .12;
  }

  private static double thickness(double gravity) {
    return 16 + 12 * Math.sqrt(Math.clamp(gravity, .15, 4));
  }

  /** Uppermost uncarved island surface; empty columns return the world bottom. */
  public static double height(long seed, int sea, double gravity, double x, double z) {
    for (int layer = 2; layer >= 0; layer--) {
      double mass = mass(seed, layer, x, z);
      if (mass > 0) return center(seed, sea, layer, x, z) + Math.min(mass, 1) * 12;
    }
    return -64;
  }

  public static double density(
      long seed, int sea, double gravity, boolean caves, double x, double y, double z) {
    double rock = -32;
    for (int layer = 0; layer < 3; layer++) {
      double mass = mass(seed, layer, x, z);
      double offset = y - center(seed, sea, layer, x, z);
      // Broad tops taper into deeper points underneath; all boundaries are continuous.
      double vertical = offset >= 0 ? offset / 12 : -offset / thickness(gravity);
      rock = Math.max(rock, Math.min(mass, 1) - vertical);
    }
    if (caves) {
      double passage =
          Math.max(
              Math.abs(PlanetLandforms.noise(seed + 443, x / 75, y / 45, z / 75)),
              Math.abs(PlanetLandforms.noise(seed + 449, x / 75, y / 37, z / 75)));
      rock = Math.min(rock, (passage - .075) * 5);
    }
    return Math.clamp(rock, -32, 32);
  }

  /** Depth beneath the nearest island, so lower exposed shelves keep surface biomes. */
  public static double depth(long seed, int sea, double x, double y, double z) {
    double nearest = Double.POSITIVE_INFINITY;
    double depth = -64 - y;
    for (int layer = 0; layer < 3; layer++) {
      double mass = mass(seed, layer, x, z);
      if (mass <= 0) continue;
      double center = center(seed, sea, layer, x, z);
      if (Math.abs(y - center) < nearest) {
        nearest = Math.abs(y - center);
        depth = center + Math.min(mass, 1) * 12 - y;
      }
    }
    return depth;
  }
}

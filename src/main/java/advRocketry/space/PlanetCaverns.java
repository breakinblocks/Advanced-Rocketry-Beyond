package advRocketry.space;

/** Layered vaults connected by the planet's original three-dimensional cave network. */
public final class PlanetCaverns {
  private PlanetCaverns() {}

  public static double floor(long seed, int sea, int layer, double x, double z) {
    return Math.clamp(sea, 48, 100)
        + 12
        + layer * 72
        + PlanetLandforms.field(seed, 461 + layer, x, z, 170) * 10;
  }

  public static double ceiling(long seed, int sea, double gravity, int layer, double x, double z) {
    double arch = 1 - Math.abs(PlanetLandforms.field(seed, 467 + layer, x, z, 130));
    return floor(seed, sea, layer, x, z)
        + (28 + arch * 26) / Math.sqrt(Math.clamp(gravity, .65, 2.5));
  }

  public static double moisture(long seed, double x, double y, double z) {
    return Math.clamp(
        PlanetLandforms.moisture(seed, x, z)
            + PlanetLandforms.noise(seed + 479, x / 160, y / 58, z / 160) * .65,
        -1,
        1);
  }

  public static double density(
      long seed, int sea, double gravity, boolean caves, double x, double y, double z) {
    double rock = PlanetLandforms.density(seed, "cavern", sea, false, caves, x, y, z);
    if (!caves || y <= -52 || y >= 270) return rock;
    for (int layer = -1; layer <= 1; layer++) {
      double vertical =
          Math.max(floor(seed, sea, layer, x, z) - y, y - ceiling(seed, sea, gravity, layer, x, z))
              / 12;
      double boundary = (PlanetLandforms.field(seed, 487 + layer, x, z, 200) - .35) * 3;
      double pillars =
          (.055
                  - Math.max(
                      Math.abs(PlanetLandforms.field(seed, 491 + layer, x, z, 85)),
                      Math.abs(PlanetLandforms.field(seed, 499 + layer, x, z, 85))))
              * 8;
      rock = Math.min(rock, Math.max(vertical, Math.max(boundary, pillars)));
    }
    return Math.clamp(rock, -32, 32);
  }
}

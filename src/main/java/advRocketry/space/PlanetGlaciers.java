package advRocketry.space;

/** Broad glacial valleys and narrow fractures sharing their field with buried passages. */
public final class PlanetGlaciers {
  private PlanetGlaciers() {}

  private static double blend(double value) {
    return PlanetLandforms.smooth(Math.clamp(value, 0, 1));
  }

  public static double coast(long seed, double x, double z) {
    return blend((-.1 - PlanetLandforms.continents(seed, x, z)) / .25);
  }

  public static double fracture(long seed, double x, double z) {
    double warp = PlanetLandforms.field(seed, 509, x, z, 220) * 35;
    return Math.abs(PlanetLandforms.field(seed, 521, x + warp, z - warp, 125));
  }

  public static double uncutHeight(long seed, int sea, double x, double z) {
    double valley = blend((PlanetLandforms.channel(seed, x, z) - .045) / .23);
    double upland = sea + 46 + PlanetLandforms.field(seed, 523, x, z, 340) * 35;
    double floor = sea + 8 + PlanetLandforms.field(seed, 541, x, z, 140) * 4;
    return Math.clamp(
        PlanetLandforms.mix(PlanetLandforms.mix(floor, upland, valley), sea + 6, coast(seed, x, z)),
        -32,
        250);
  }

  public static double height(long seed, int sea, double gravity, double x, double z) {
    double incision = blend((.065 - fracture(seed, x, z)) / .05);
    return Math.clamp(
        uncutHeight(seed, sea, x, z)
            - incision * (12 + 12 * Math.sqrt(Math.clamp(gravity, .15, 4))),
        -40,
        250);
  }

  public static double density(
      long seed, int sea, double gravity, boolean caves, double x, double y, double z) {
    double top = height(seed, sea, gravity, x, z);
    double rock = (top - y) / 22;
    // Undercut coastal shelves independently of the cave toggle.
    double shelf = Math.max(Math.abs(y - (sea - 6)) / 5 - 1, (.75 - coast(seed, x, z)) * 4);
    rock = Math.min(rock, shelf);
    double uncut = uncutHeight(seed, sea, x, z);
    if (caves && y > -52 && y < uncut - 6) {
      double tunnels =
          Math.max(
              Math.abs(PlanetLandforms.noise(seed + 109, x / 78, y / 39, z / 78)),
              Math.abs(PlanetLandforms.noise(seed + 113, x / 78, y / 53, z / 78)));
      double center = uncut - 24;
      double passage = Math.max(fracture(seed, x, z) / .11 - 1, Math.abs(y - center) / 7 - 1);
      rock = Math.min(rock, Math.min((tunnels - .085) * 5, passage));
    }
    return Math.clamp(Math.max(rock, (-58 - y) / 3), -32, 32);
  }
}

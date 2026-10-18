package advRocketry.space;

/** Continuous hostile terrain fields, shared by noise generation and biome depth sampling. */
public final class PlanetGeology {
  private PlanetGeology() {}

  public static boolean enabled(int version, String profile) {
    return profile.equals("arid")
        || profile.equals("storm")
        || version >= 4 && profile.equals("volcanic")
        || version >= 5 && profile.equals("lunar")
        || version >= 6 && profile.equals("fractured")
        || version >= 7 && profile.equals("cavern");
  }

  private static double fissure(long seed, double x, double z) {
    double warp = PlanetLandforms.field(seed, 283, x, z, 260) * 44;
    return Math.abs(PlanetLandforms.field(seed, 293, x + warp, z - warp, 180));
  }

  public static double height(
      long seed, String profile, int sea, double gravity, double x, double z) {
    if (profile.equals("cavern")) return 280;
    if (profile.equals("fractured")) return PlanetFractures.height(seed, sea, gravity, x, z);
    if (profile.equals("lunar")) return PlanetFrontiers.lunarHeight(seed, sea, gravity, x, z);
    if (profile.equals("storm")) return PlanetFrontiers.stormHeight(seed, sea, gravity, x, z);
    double incision = PlanetLandforms.smooth(Math.clamp((.12 - fissure(seed, x, z)) / .09, 0, 1));
    double pull = Math.sqrt(Math.clamp(gravity, .15, 4));
    if (profile.equals("arid")) {
      double plateau =
          sea
              + 54
              + PlanetLandforms.continents(seed, x, z) * 23
              + PlanetLandforms.field(seed, 29, x, z, 370) * 27;
      double layer = Math.floor(plateau / 9);
      double step = PlanetLandforms.smooth(Math.clamp((plateau / 9 - layer - .25) / .5, 0, 1));
      double terraces = (layer + step) * 9;
      return Math.clamp(
          terraces - incision * (10 + 10 * pull) + PlanetLandforms.field(seed, 311, x, z, 35) * 1.5,
          Math.min(sea + 3, 280),
          280);
    }
    double original = PlanetLandforms.height(seed, "volcanic", sea, false, x, z);
    double bed = sea - 3 - 4 * pull + PlanetLandforms.field(seed, 311, x, z, 35);
    return Math.min(original, PlanetLandforms.mix(original, bed, incision));
  }

  public static double tubeCenter(long seed, int sea, double gravity, double x, double z) {
    return height(seed, "volcanic", sea, gravity, x, z)
        - 23
        + PlanetLandforms.field(seed, 313, x, z, 150) * 4;
  }

  public static double tubeDensity(
      long seed, int sea, double gravity, double x, double y, double z) {
    return tubeDensity(seed, x, y, z, tubeCenter(seed, sea, gravity, x, z));
  }

  private static double tubeDensity(long seed, double x, double y, double z, double center) {
    double warp = PlanetLandforms.field(seed, 317, x, z, 240) * 35;
    double across = PlanetLandforms.field(seed, 307, x + warp, z - warp, 120) / .045;
    double radius = 5 + PlanetLandforms.field(seed, 319, x, z, 100) * 1.5;
    double vertical = (y - center) / radius;
    return (Math.sqrt(across * across + vertical * vertical) - 1) * .8;
  }

  public static double density(
      long seed,
      String profile,
      int sea,
      double gravity,
      boolean caves,
      double x,
      double y,
      double z) {
    if (profile.equals("fractured"))
      return PlanetFractures.density(seed, sea, gravity, caves, x, y, z);
    if (profile.equals("cavern"))
      return PlanetCaverns.density(seed, sea, gravity, caves, x, y, z);
    double top = height(seed, profile, sea, gravity, x, z);
    double rock = (top - y) / 22;
    rock +=
        PlanetLandforms.noise(seed + 73, x / 65, y / 43, z / 65)
            * .42
            * Math.min(1, Math.abs(top - sea) / 35);
    if (caves && y > -55 && y < top - 10) {
      double rooms = PlanetLandforms.noise(seed + 101, x / 59, y / 31, z / 59);
      double tunnels =
          Math.max(
              Math.abs(PlanetLandforms.noise(seed + 109, x / 78, y / 39, z / 78)),
              Math.abs(PlanetLandforms.noise(seed + 113, x / 78, y / 53, z / 78)));
      rock = Math.min(rock, Math.min((.49 - rooms) * 3, (tunnels - .085) * 5));
      if (profile.equals("volcanic")) {
        double center = top - 23 + PlanetLandforms.field(seed, 313, x, z, 150) * 4;
        rock = Math.min(rock, tubeDensity(seed, x, y, z, center));
      }
    }
    return Math.clamp(Math.max(rock, (-58 - y) / 3), -32, 32);
  }
}

package advRocketry.space;

/**
 * Original continuous fields shared by terrain, habitats and vegetation. Coordinates are blocks.
 */
public final class PlanetLandforms {
  private PlanetLandforms() {}

  public static String profile(Planet planet) {
    if (!planet.noiseProfile.equals("auto")) return planet.noiseProfile;
    if (planet.generationVersion >= 8
        && planet.terrain == TerrainType.TERRESTRIAL
        && planet.atmosphere > 25
        && planet.temperature < 250
        && !planet.biomes.isEmpty()
        && planet.biomes.stream().allMatch(id -> id.equals(BiomeMaterials.id("crystalchasms"))))
      return "glacial";
    if (planet.generationVersion >= 5
        && planet.terrain == TerrainType.TERRESTRIAL
        && planet.atmosphere > 25
        && planet.temperature <= 450
        && !planet.biomes.isEmpty()
        && planet.biomes.stream().allMatch(id -> id.equals(BiomeMaterials.id("stormland"))))
      return "storm";
    if (planet.generationVersion >= 4
        && planet.terrain == TerrainType.TERRESTRIAL
        && planet.atmosphere > 25
        && !planet.biomes.isEmpty()
        && planet.biomes.stream().allMatch(id -> id.equals(BiomeMaterials.id("hotdryrock"))))
      return "arid";
    return switch (planet.terrain) {
      case MOON -> "lunar";
      case VOLCANIC -> "volcanic";
      case ASTEROID -> "fractured";
      case CAVE -> "cavern";
      default ->
          planet.atmosphere <= 25 ? "lunar" : planet.temperature > 450 ? "volcanic" : "temperate";
    };
  }

  public static double noise(long seed, double x, double y, double z) {
    int ix = (int) Math.floor(x), iy = (int) Math.floor(y), iz = (int) Math.floor(z);
    double fx = smooth(x - ix), fy = smooth(y - iy), fz = smooth(z - iz);
    double a = mix(hash(seed, ix, iy, iz), hash(seed, ix + 1, iy, iz), fx);
    double b = mix(hash(seed, ix, iy, iz + 1), hash(seed, ix + 1, iy, iz + 1), fx);
    double c = mix(hash(seed, ix, iy + 1, iz), hash(seed, ix + 1, iy + 1, iz), fx);
    double d = mix(hash(seed, ix, iy + 1, iz + 1), hash(seed, ix + 1, iy + 1, iz + 1), fx);
    return mix(mix(a, b, fz), mix(c, d, fz), fy);
  }

  private static double hash(long seed, int x, int y, int z) {
    long h = seed ^ x * 0x632be59bd9b4e019L ^ y * 0x9e3779b97f4a7c15L ^ z * 0x85157af5d66d9567L;
    h = (h ^ h >>> 30) * 0xbf58476d1ce4e5b9L;
    h = (h ^ h >>> 27) * 0x94d049bb133111ebL;
    return ((h ^ h >>> 31) >>> 11) * 0x1.0p-52 - 1;
  }

  public static double field(long seed, int salt, double x, double z, double scale) {
    return noise(seed + salt * 7919L, x / scale, .37, z / scale);
  }

  public static double moisture(long seed, double x, double z) {
    return field(seed, 11, x, z, 330);
  }

  public static double temperature(long seed, double x, double z) {
    return field(seed, 13, x, z, 580);
  }

  public static double continents(long seed, double x, double z) {
    return field(seed, 17, x, z, 1100);
  }

  public static double channel(long seed, double x, double z) {
    double warp = 55 * field(seed, 19, x, z, 310);
    return Math.abs(field(seed, 23, x + warp, z - warp, 240));
  }

  public static double height(
      long seed, String profile, int sea, boolean channels, double x, double z) {
    double land = continents(seed, x, z);
    double broad = field(seed, 29, x, z, 370);
    double detail = field(seed, 31, x, z, 73);
    double ridge = 1 - Math.abs(field(seed, 37, x, z, 210));
    double h =
        sea
            + land * 65
            + broad * 28
            + detail * 7
            + Math.pow(ridge, 5) * Math.max(0, broad + .25) * 100;
    if (profile.equals("lunar"))
      h = sea + 12 + land * 42 + broad * 20 + detail * 9 + Math.max(0, land) * ridge * 55;
    if (profile.equals("volcanic")) {
      double cone = Math.max(0, 1 - Math.abs(broad) * 3.7);
      h = sea + 16 + land * 35 + Math.pow(cone, 3) * 74 + detail * 8;
      // Lower a central basin inside broad volcanic ridges.
      h -= Math.max(0, cone - .88) * 440;
    }
    if (profile.equals("fractured")) h = sea + 40 + broad * 85 + detail * 15;
    if (profile.equals("cavern")) h = 285;
    if (channels && !profile.equals("fractured") && !profile.equals("cavern")) {
      double bank = smooth(Math.clamp((channel(seed, x, z) - .045) / .085, 0, 1));
      h = Math.min(h, mix(sea - 5 + detail, h, bank));
    }
    return Math.clamp(h, -40, 280);
  }

  public static double density(
      long seed,
      String profile,
      int sea,
      boolean channels,
      boolean caves,
      double x,
      double y,
      double z) {
    double top = height(seed, profile, sea, channels, x, z);
    double rock = (top - y) / 22;
    double warp = noise(seed + 73, x / 65, y / 43, z / 65);
    rock += warp * .42 * Math.min(1, Math.abs(top - sea) / 35);
    if (profile.equals("fractured")) {
      double island = field(seed, 43, x, z, 190);
      rock = Math.min(rock, (y - (top - 24 - 25 * island)) / 13);
      rock = Math.min(rock, island * 3 + .48);
    }
    if (caves && y > -55 && y < top - 7) {
      double rooms = noise(seed + 101, x / 59, y / 31, z / 59);
      double tunnels =
          Math.max(
              Math.abs(noise(seed + 109, x / 78, y / 39, z / 78)),
              Math.abs(noise(seed + 113, x / 78, y / 53, z / 78)));
      double cavity = Math.min((.49 - rooms) * 3, (tunnels - .085) * 5);
      if (profile.equals("cavern")) cavity = Math.min(cavity, (.19 - rooms) * 3);
      rock = Math.min(rock, cavity);
    }
    if (!profile.equals("fractured")) rock = Math.max(rock, (-58 - y) / 3);
    return Math.clamp(rock, -32, 32);
  }

  public static double smooth(double t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  public static double mix(double a, double b, double t) {
    return a + (b - a) * t;
  }
}

package advRocketry.space;

/** Seeded ridge and basin fields for Stormland and airless landscapes. */
public final class PlanetFrontiers {
  private PlanetFrontiers() {}

  public record Basin(double interior, double rim) {}

  public static Basin basin(long seed, double x, double z) {
    int gx = (int) Math.floor(x / 800), gz = (int) Math.floor(z / 800);
    double interior = 0, rim = 0;
    for (int a = gx - 1; a <= gx + 1; a++)
      for (int b = gz - 1; b <= gz + 1; b++) {
        double cx = (a + .5) * 800 + cell(seed + 347, a, b) * 150;
        double cz = (b + .5) * 800 + cell(seed + 349, a, b) * 150;
        double radius = 220 + cell(seed + 353, a, b) * 60;
        double distance = Math.hypot((x - cx) / radius, (z - cz) / (radius * .82));
        if (distance >= 1.25) continue;
        interior = Math.max(interior, blend((1 - distance) / .65));
        rim = Math.max(rim, blend((distance - .72) / .24) * blend((1.25 - distance) / .25));
      }
    return new Basin(interior, rim);
  }

  public static double rille(long seed, double x, double z) {
    double warp = PlanetLandforms.field(seed, 359, x, z, 300) * 65;
    double line = Math.abs(PlanetLandforms.field(seed, 367, x + warp, z - warp, 165));
    return blend((.065 - line) / .05);
  }

  public static double lunarHeight(long seed, int sea, double gravity, double x, double z) {
    Basin basin = basin(seed, x, z);
    double highlands = PlanetLandforms.height(seed, "lunar", sea, false, x, z);
    double floor = sea - 18 + PlanetLandforms.field(seed, 373, x, z, 90) * 3;
    double height = PlanetLandforms.mix(highlands, floor, basin.interior()) + basin.rim() * 18;
    return Math.clamp(
        height - rille(seed, x, z) * (4 + 5 * Math.sqrt(Math.clamp(gravity, .15, 4))), -40, 280);
  }

  public static double stormHeight(long seed, int sea, double gravity, double x, double z) {
    double angle = PlanetLandforms.field(seed, 331, 0, 0, 1) * Math.PI;
    double along = x * Math.cos(angle) + z * Math.sin(angle);
    double across = -x * Math.sin(angle) + z * Math.cos(angle);
    double ridge = 1 - Math.abs(PlanetLandforms.field(seed, 337, along * .3, across, 110));
    double exposure = Math.clamp(.45 + PlanetLandforms.field(seed, 379, x, z, 350), .1, 1);
    double h =
        sea
            + 12
            + PlanetLandforms.continents(seed, x, z) * 40
            + PlanetLandforms.field(seed, 29, x, z, 370) * 15
            + Math.pow(ridge, 7) * exposure * 95 / Math.sqrt(Math.clamp(gravity, .4, 3));
    double bank = blend((PlanetLandforms.channel(seed, x, z) - .045) / .085);
    double bed = sea - 3 - 5 * Math.sqrt(Math.clamp(gravity, .15, 4));
    return Math.clamp(Math.min(h, PlanetLandforms.mix(bed, h, bank)), -40, 280);
  }

  private static double cell(long seed, int x, int z) {
    long value = PlanetSeeds.cell(seed, x, z);
    value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
    value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
    return ((value ^ value >>> 31) >>> 11) * 0x1.0p-52 - 1;
  }

  private static double blend(double value) {
    return PlanetLandforms.smooth(Math.clamp(value, 0, 1));
  }
}

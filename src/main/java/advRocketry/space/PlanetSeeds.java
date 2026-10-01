package advRocketry.space;

import java.util.Random;

final class PlanetSeeds {
  private static final long X = 341873128712L;
  private static final long Z = 132897987541L;

  private PlanetSeeds() {}

  static long planet(long worldSeed, int id) {
    return worldSeed + id * X;
  }

  static long biomes(long seed, int id) {
    return seed ^ id * X;
  }

  static long column(long seed, int x, int z) {
    return seed ^ x * X ^ z * Z;
  }

  static long cell(long seed, int x, int z) {
    return seed + x * X + z * Z;
  }

  static boolean hit(Random random, int chance, int x, int z, boolean both) {
    boolean first = random.nextInt(chance) == Math.abs(x) % chance;
    return both
        ? first && random.nextInt(chance) == Math.abs(z) % chance
        : first || random.nextInt(chance) == Math.abs(z) % chance;
  }

  record Scatter(long seed, long xSeed, long zSeed) {
    static Scatter of(long seed) {
      Random seeder = new Random(seed);
      long xSeed = seeder.nextLong();
      return new Scatter(seed, xSeed, seeder.nextLong());
    }

    long origin(int x, int z) {
      return x * xSeed ^ z * zSeed ^ seed;
    }

    Random random(int x, int z) {
      return new Random(origin(x, z));
    }
  }
}

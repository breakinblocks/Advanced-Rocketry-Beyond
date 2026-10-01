// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original small, regular and huge crater rules adapted to native chunk surfaces. */
public final class PlanetCraters {
  private PlanetCraters() {}

  public static void generate(
      ChunkAccess chunk,
      Planet planet,
      long seed,
      BiFunction<Integer, Integer, Holder<Biome>> biomes) {
    if (!(planet.craterFrequency > 0) || !Float.isFinite(planet.craterFrequency)) return;
    List<BlockState> ores =
        planet.craterOres.stream()
            .flatMap(entry -> entry.blocks().stream())
            .distinct()
            .map(Block::defaultBlockState)
            .toList();
    double atmosphere = planet.atmosphere / 100d;
    if (atmosphere <= .05)
      generateKind(chunk, planet, seed, biomes, ores, 0, 2, 16 + 8 * (1 - atmosphere));
    generateKind(chunk, planet, seed, biomes, ores, 1, 9, 250 + 175 * (1 - atmosphere));
    if (atmosphere == 0) generateKind(chunk, planet, seed, biomes, ores, 2, 52, 200);
  }

  private static void generateKind(
      ChunkAccess chunk,
      Planet planet,
      long seed,
      BiFunction<Integer, Integer, Holder<Biome>> biomes,
      List<BlockState> ores,
      int kind,
      int range,
      double baseChance) {
    int chance =
        Math.max(1, (int) Math.min(Integer.MAX_VALUE, baseChance * planet.craterFrequency));
    PlanetSeeds.Scatter scatter = PlanetSeeds.Scatter.of(seed);
    for (int x = chunk.getPos().x - range; x <= chunk.getPos().x + range; x++)
      for (int z = chunk.getPos().z - range; z <= chunk.getPos().z + range; z++) {
        long originSeed = scatter.origin(x, z);
        Random random = new Random(originSeed);
        if (!PlanetSeeds.hit(random, chance, x, z, kind == 2)
            || !allowed(planet, biomes.apply(x * 16, z * 16), random)) continue;
        int[] coefficients = new int[5];
        for (int i = 0; i < coefficients.length; i++)
          coefficients[i] = 1 + random.nextInt(kind == 0 ? 15 : 10);
        int radius = radius(kind, planet.atmosphere < 5, random);
        int bumps = kind == 0 ? 3 : 1 + random.nextInt(kind == 2 || radius > 32 ? 5 : 4);
        boolean spire = kind == 2 && random.nextInt(4) == 0;
        paint(chunk, x * 16, z * 16, radius, bumps, coefficients, kind, spire, ores, originSeed);
      }
  }

  private static boolean allowed(Planet planet, Holder<Biome> biome, Random random) {
    if (planet.craterBiomeWeights.isEmpty()) return true;
    int frequency =
        biome
            .unwrapKey()
            .map(key -> planet.craterBiomeWeights.getOrDefault(key.location(), 0))
            .orElse(0);
    return frequency > random.nextInt(99);
  }

  private static int radius(int kind, boolean large, Random random) {
    if (kind == 0) return 4 + random.nextInt(4);
    if (kind == 2) {
      int roll = random.nextInt(400);
      return 84 + (roll < 200 ? 0 : roll < 325 ? 24 : roll < 375 ? 40 : 56) + random.nextInt(75);
    }
    int roll = random.nextInt(500);
    if (roll < 440) return 8 + random.nextInt(16);
    if (roll < 485) return 32 + random.nextInt(16);
    if (large && roll < 495) return 48 + random.nextInt(16);
    if (large && roll < 499) return 64 + random.nextInt(28);
    return 8;
  }

  static void paint(
      ChunkAccess chunk,
      int centerX,
      int centerZ,
      int baseRadius,
      int bumps,
      int[] coefficients,
      int kind,
      boolean spire,
      List<BlockState> ores,
      long seed) {
    for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++)
      for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
        int dx = x - centerX, dz = z - centerZ;
        double angle = Math.atan2(dx, dz);
        int extra = 0;
        for (int i = 2; i < Math.min(5, bumps) + 2; i++)
          extra += coefficients[i - 2] * baseRadius * Math.sin(i * angle) * .0075;
        int radius = Math.max(1, baseRadius + extra);
        int distanceSquared = dx * dx + dz * dz;
        int distance = (int) Math.sqrt(distanceSquared);
        if (distance > radius * 4) continue;
        int depth =
            ((kind == 0 ? baseRadius * baseRadius : radius * radius) - distanceSquared)
                / (radius * 2);
        int inverse = kind == 0 ? depth : radius - distance;
        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15);
        BlockState fluid = Blocks.AIR.defaultBlockState();
        int fluidTop = chunk.getMinBuildHeight() - 1;
        BlockState surface = chunk.getBlockState(new BlockPos(x, top, z));
        while (top > chunk.getMinBuildHeight() && ignored(surface)) {
          if (!surface.getFluidState().isEmpty() && top > fluidTop) {
            fluid = surface.getFluidState().createLegacyBlock();
            fluidTop = top;
          }
          surface = chunk.getBlockState(new BlockPos(x, --top, z));
        }
        if (ignored(surface)) continue;
        // Per-column randomness keeps ore/ejecta decoration independent of chunk generation order.
        Random random = new Random(PlanetSeeds.column(seed, x, z));
        int maxDepth = kind == 0 ? Integer.MAX_VALUE : kind == 2 ? 28 : baseRadius > 32 ? 16 : 12;
        int bowl = Math.min(maxDepth, depth);
        for (int y = top; y > top - bowl && y > chunk.getMinBuildHeight() + 2; y--)
          PlanetDecorations.set(
              chunk, x, y, z, y <= fluidTop ? fluid : Blocks.AIR.defaultBlockState(), 3);

        if ((kind != 0 || baseRadius > 6)
            && inverse <= radius / 4
            && inverse > -(kind == 1 ? 2 : 3) * radius) {
          double ridgeSize = Math.max(1, 12 * radius / 64d);
          double ridge =
              9 * ridgeSize * ((1d - inverse) / (.8 * radius + (inverse - 1d) * (inverse - 1d)))
                  - 1.06;
          for (int rise = -1; rise < ridge; rise++) {
            boolean place;
            if (kind == 0)
              place =
                  inverse > -.875 * radius
                      || inverse > -1.125 * radius
                          && random.nextInt(Math.abs(inverse / (radius > 48 ? 4 : 2)) + 1) == 0;
            else if (kind == 1)
              place =
                  inverse >= -.625 * radius
                      || random.nextInt(Math.abs(inverse + (int) (radius * .625)) + 1) == 0;
            else
              place =
                  inverse > -.75 * radius
                      || inverse >= -.875 * radius
                          && random.nextInt(Math.max(1, inverse + (int) (radius * .875) + 1)) != 0
                      || inverse < -.875 * radius
                          && random.nextInt(Math.abs(inverse + (int) (radius * .875)) + 1) == 0;
            if (place)
              PlanetDecorations.set(
                  chunk, x, top + rise, z, deposit(surface, ores, random, false), 3);
            if (kind != 0
                && random.nextInt(Math.abs(inverse) + 1) == 0
                && (kind == 2 || baseRadius > 40)) {
              double outer = kind == 2 ? -1.5 : -(1 + Math.max((baseRadius - 20) / 20d, .5));
              if (inverse < -.375 * radius && inverse >= outer * radius)
                PlanetDecorations.set(
                    chunk, x, top + rise + 1, z, deposit(surface, ores, random, false), 3);
              else if (inverse < outer * radius)
                PlanetDecorations.set(
                    chunk,
                    x,
                    top + rise + 1 + random.nextInt(2),
                    z,
                    deposit(surface, ores, random, false),
                    3);
            }
          }
        }
        if (depth >= 0) {
          PlanetDecorations.set(chunk, x, top - bowl, z, deposit(surface, ores, random, false), 3);
          PlanetDecorations.set(
              chunk, x, top - bowl - 1, z, deposit(surface, ores, random, false), 3);
        }
        if (spire && distance < .25 * radius) {
          int height =
              Math.min(
                  17, (int) Math.ceil(Math.pow(Math.abs(-radius / 16d + distance / 4d), 1.25)));
          for (int rise = 0; rise < height; rise++)
            PlanetDecorations.set(
                chunk, x, top + rise - 27, z, deposit(surface, ores, random, true), 3);
        }
      }
  }

  private static boolean ignored(BlockState state) {
    return state.isAir() || !state.getFluidState().isEmpty() || state.is(Blocks.ICE);
  }

  private static BlockState deposit(
      BlockState surface, List<BlockState> ores, Random random, boolean rich) {
    return random.nextInt(rich ? 4 : 24) == 0 && !ores.isEmpty()
        ? ores.get(random.nextInt(ores.size()))
        : surface;
  }
}

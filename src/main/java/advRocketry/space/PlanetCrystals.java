// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.Main;
import advRocketry.processing.ProcessingRegistry;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original BiomeGenCrystal and MapGenLargeCrystal, clipped into independently generated chunks. */
public final class PlanetCrystals {
  private static final ResourceKey<Biome> BIOME =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "crystalchasms"));
  private static final List<String> COLORS =
      List.of("amethyst", "sapphire", "emerald", "ruby", "citrine", "wulfentite");

  private PlanetCrystals() {}

  public static void generate(
      ChunkAccess chunk, long seed, BiFunction<Integer, Integer, Holder<Biome>> biomes) {
    PlanetSeeds.Scatter scatter = PlanetSeeds.Scatter.of(seed);
    for (int x = chunk.getPos().x - 8; x <= chunk.getPos().x + 8; x++)
      for (int z = chunk.getPos().z - 8; z <= chunk.getPos().z + 8; z++) {
        Random random = scatter.random(x, z);
        if (!PlanetSeeds.hit(random, 6, x, z, true) || !biomes.apply(x * 16, z * 16).is(BIOME))
          continue;
        int height = random.nextInt(40) + 10;
        int edge = random.nextInt(4) + 2;
        int xShear = 1 - (random.nextInt(6) + 3) / 4;
        int zShear = 1 - (random.nextInt(6) + 3) / 4;
        BlockState crystal =
            ProcessingRegistry.part(COLORS.get(random.nextInt(COLORS.size())) + "_crystal_block")
                .defaultBlockState();
        float shape = .01f + random.nextFloat() * .2f;
        int baseRadius = (int) (shape * edge * height + (1 - shape) * edge);
        slice(chunk, x * 16, 80, z * 16, edge + 1, baseRadius, crystal, 1, random);
        for (int y = 0; y < height; y++) {
          int radius = (int) (shape * edge * (height - y) + (1 - shape) * edge);
          slice(
              chunk,
              x * 16 + xShear * y,
              81 + y,
              z * 16 + zShear * y,
              edge + 1,
              radius,
              crystal,
              0,
              random);
        }
        slice(chunk, x * 16, 81, z * 16, edge + 1, baseRadius, crystal, 2, random);
      }
  }

  private static void slice(
      ChunkAccess chunk,
      int x,
      int y,
      int z,
      int diagonal,
      int radius,
      BlockState crystal,
      int mode,
      Random random) {
    int current = radius;
    // The original broad hexagonal sections have expanding and contracting trapezoids.
    for (int dz = -diagonal - current / 2; dz <= -current / 2; dz++) {
      row(
          chunk,
          x,
          y,
          z + dz,
          (mode == 2 ? 0 : diagonal) + current / 2,
          crystal,
          mode,
          true,
          random);
      current++;
    }
    for (int dz = -current / 2; dz <= current / 2; dz++)
      row(
          chunk,
          x,
          y,
          z + dz,
          (mode == 2 ? 0 : diagonal) + current / 2,
          crystal,
          mode,
          false,
          random);
    for (int dz = current / 2; dz <= diagonal + current / 2; dz++) {
      current--;
      row(
          chunk,
          x,
          y,
          z + dz,
          (mode == 2 ? 0 : diagonal) + current / 2,
          crystal,
          mode,
          false,
          random);
    }
  }

  private static void row(
      ChunkAccess chunk,
      int centerX,
      int y,
      int z,
      int radius,
      BlockState crystal,
      int mode,
      boolean crystalFoundation,
      Random random) {
    for (int x = centerX - radius; x <= centerX + radius; x++) {
      // Advance random for every global column so neighboring chunks reproduce the same shape.
      if (mode == 2 && random.nextInt(3) != 0) continue;
      if (x < chunk.getPos().getMinBlockX()
          || x > chunk.getPos().getMaxBlockX()
          || z < chunk.getPos().getMinBlockZ()
          || z > chunk.getPos().getMaxBlockZ()) continue;
      if (mode == 2)
        PlanetDecorations.set(chunk, x, y, z, Blocks.PACKED_ICE.defaultBlockState(), 0);
      else if (mode == 1) {
        int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15);
        for (int floor = Math.max(chunk.getMinBuildHeight(), surface); floor < y; floor++)
          PlanetDecorations.set(
              chunk,
              x,
              floor,
              z,
              crystalFoundation ? crystal : Blocks.PACKED_ICE.defaultBlockState(),
              0);
        PlanetDecorations.set(chunk, x, y, z, Blocks.PACKED_ICE.defaultBlockState(), 0);
      } else PlanetDecorations.set(chunk, x, y, z, crystal, 0);
    }
  }
}

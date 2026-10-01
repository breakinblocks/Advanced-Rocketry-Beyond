// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModTags;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketRegistry;
import advRocketry.util.IdOrTag;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original crater, volcano, geode and crystal equations adapted to chunk-local writes. */
public final class PlanetDecorations {
  private PlanetDecorations() {}

  private static final List<String> ORES =
      Stream.concat(Stream.of("copper"), OreWorldgen.NAMES.stream()).toList();

  private static final ResourceKey<Biome> ALIEN_FOREST =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "alien_forest"));
  private static final ResourceKey<Biome> STORMLAND =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "stormland"));
  private static final ResourceKey<Biome> OCEAN_SPIRES =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "oceanspires"));
  private static final ResourceKey<Biome> MARSH =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "marsh"));
  private static final ResourceKey<Biome> DEEP_SWAMP =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "deepswamp"));

  public static void generate(
      ChunkAccess chunk,
      Planet planet,
      long seed,
      BiFunction<Integer, Integer, Holder<Biome>> biomes) {
    if (planet.craters && AdvancedRocketryConfig.generateCraters())
      PlanetCraters.generate(chunk, planet, seed, biomes);
    if (planet.volcanoes && AdvancedRocketryConfig.generateVolcanoes()
        || planet.geodes && AdvancedRocketryConfig.generateGeodes()) {
      int cx = chunk.getPos().x, cz = chunk.getPos().z;
      int range =
          Math.max(
              5,
              (AdvancedRocketryConfig.geodeBaseSize()
                      + AdvancedRocketryConfig.geodeVariation() / 2
                      + 15)
                  / 16);
      for (int nx = cx - range; nx <= cx + range; nx++)
        for (int nz = cz - range; nz <= cz + range; nz++) {
          Random random = random(seed, nx, nz);
          if (planet.volcanoes
              && AdvancedRocketryConfig.generateVolcanoes()
              && random.nextDouble() < planet.volcanoFrequency / 225d)
            volcano(chunk, nx * 16, nz * 16, 64 - random.nextInt(8));
          if (planet.geodes
              && AdvancedRocketryConfig.generateGeodes()
              && geodeChance(random, nx, nz, planet.geodeFrequency)
              && geodeBiome(biomes.apply(nx * 16, nz * 16)))
            geode(
                chunk,
                nx * 16,
                nz * 16,
                AdvancedRocketryConfig.geodeBaseSize()
                    - AdvancedRocketryConfig.geodeVariation() / 2
                    + random.nextInt(AdvancedRocketryConfig.geodeVariation()),
                planet);
        }
    }
    PlanetCrystals.generate(chunk, seed, biomes);
    if (chunk.getNoiseBiome(2, 16, 2).is(OCEAN_SPIRES)) oceanSpires(chunk, seed);
    if (GalaxyData.LUNA.equals(planet.location)) apolloLander(chunk);
  }

  private static boolean geodeChance(Random random, int x, int z, float multiplier) {
    if (!(multiplier > 0) || !Float.isFinite(multiplier)) return false;
    int chance = Math.max(1, (int) (800 * (double) multiplier));
    return PlanetSeeds.hit(random, chance, x, z, false);
  }

  private static boolean geodeBiome(Holder<Biome> biome) {
    return !biome.is(BiomeTags.IS_OCEAN)
        && !biome.is(BiomeTags.IS_RIVER)
        && !biome.is(BiomeTags.IS_BEACH);
  }

  /** Original Luna landmark near 2347, 67, clipped to each generated chunk. */
  private static void apolloLander(ChunkAccess chunk) {
    int x = 2347, z = 67;
    int antennaX = x + 10, antennaZ = z + 15;
    if (chunk.getPos().x == antennaX >> 4 && chunk.getPos().z == antennaZ >> 4) {
      int antennaBase =
          chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, antennaX & 15, antennaZ & 15);
      for (int height = 0; height <= 4; height++)
        set(chunk, antennaX, antennaBase + height, antennaZ, Blocks.IRON_BARS.defaultBlockState());
      set(chunk, antennaX + 1, antennaBase + 4, antennaZ, Blocks.IRON_BARS.defaultBlockState());
      set(chunk, antennaX + 2, antennaBase + 4, antennaZ, Blocks.IRON_BARS.defaultBlockState());
    }
    if (chunk.getPos().x != x >> 4 || chunk.getPos().z != z >> 4) return;
    int base = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15);
    if (base < chunk.getMinBuildHeight() + 1) return;
    for (int offset = -3; offset <= 3; offset += 6) {
      BlockState slab =
          Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
      set(chunk, x + offset, base - 1, z, slab);
      set(chunk, x, base - 1, z + offset, slab);
      for (int height : new int[] {0, 1, 3}) {
        set(chunk, x + offset, base + height, z, Blocks.IRON_BARS.defaultBlockState());
        set(chunk, x, base + height, z + offset, Blocks.IRON_BARS.defaultBlockState());
      }
    }
    set(chunk, x, base, z, RocketRegistry.PARTS.get("rocket_motor").get().defaultBlockState());
    for (int dx = -2; dx <= 2; dx++)
      for (int dz = -2; dz <= 2; dz++) {
        if (Math.abs(dx) == 2 && Math.abs(dz) <= 1 || Math.abs(dz) == 2 && Math.abs(dx) <= 1) {
          set(chunk, x + dx, base + 2, z + dz, Blocks.GOLD_BLOCK.defaultBlockState());
          set(chunk, x + dx, base + 3, z + dz, Blocks.GOLD_BLOCK.defaultBlockState());
        } else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1)
          set(chunk, x + dx, base + 2, z + dz, Blocks.IRON_BLOCK.defaultBlockState());
      }
    for (int offset = -1; offset <= 1; offset += 2) {
      set(chunk, x + offset, base + 3, z, Blocks.IRON_BLOCK.defaultBlockState());
      set(chunk, x, base + 3, z + offset, Blocks.IRON_BLOCK.defaultBlockState());
    }
  }

  private static Random random(long seed, int x, int z) {
    return new Random(PlanetSeeds.column(seed, x, z));
  }

  private static void set(ChunkAccess chunk, int x, int y, int z, BlockState state) {
    set(chunk, x, y, z, state, 0);
  }

  static void set(ChunkAccess chunk, int x, int y, int z, BlockState state, int floor) {
    if (y >= chunk.getMinBuildHeight() + floor && y < chunk.getMaxBuildHeight())
      chunk.setBlockState(new BlockPos(x, y, z), state, false);
  }

  private static void volcano(ChunkAccess chunk, int cx, int cz, int size) {
    BlockState casing = ProcessingRegistry.part("basalt").defaultBlockState();
    BlockState lava =
        ProcessingFluids.DEFINITIONS.get("enriched_lava").block.get().defaultBlockState();
    for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++)
      for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
        double radius = Math.hypot(x - cx, z - cz);
        if (radius > size + 12) continue;
        double height =
            1 / Math.pow(1.028, radius - size * 3)
                + 25
                - 8 / Math.pow(1.09, radius - size / 2.6)
                - Math.pow(1.7, radius - size * 0.9);
        int top = Math.min(chunk.getMaxBuildHeight() - 1, (int) height);
        for (int y = 1; y <= Math.max(50, top); y++) {
          double bulb = radius * radius + (y - 25) * (y - 25);
          if (bulb < 23 * 23 || radius < 5 && y > 25 && y <= top) set(chunk, x, y, z, lava);
          else if (bulb < 25 * 25 || y <= top) set(chunk, x, y, z, casing);
        }
      }
  }

  private static void geode(ChunkAccess chunk, int cx, int cz, int radius, Planet planet) {
    List<BlockState> ores = geodeOrePalette(planet);
    for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++)
      for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
        int dx = x - cx, dz = z - cz, count = (radius * radius - dx * dx - dz * dz) / (radius * 2);
        if (count < 1) continue;
        for (int y = 64 - count + 3; y < 64 + count; y++)
          set(chunk, x, y, z, Blocks.AIR.defaultBlockState());
        BlockState ore =
            ores.isEmpty()
                ? Blocks.AIR.defaultBlockState()
                : ores.get(Math.floorMod(x / 4 + z / 4, ores.size()));
        if (!ores.isEmpty() && (x & 3) > 0 && (z & 3) > 0)
          for (int length = 1; length < 5; length++) set(chunk, x, 64 + count - length, z, ore);
        if (!ores.isEmpty() && ((x + 2) & 3) > 0 && ((z + 2) & 3) > 0)
          for (int length = 1; length < 5; length++) set(chunk, x, 64 - count + length, z, ore);
        BlockState shell = ProcessingRegistry.part("geode").defaultBlockState();
        set(chunk, x, 64 - count, z, shell);
        set(chunk, x, 64 + count, z, shell);
      }
  }

  static List<BlockState> geodeOrePalette(Planet planet) {
    Set<Block> selected = new LinkedHashSet<>();
    for (var block : BuiltInRegistries.BLOCK.getTagOrEmpty(ModTags.GEODE_ORES))
      selected.add(block.value());
    for (IdOrTag entry : planet.geodeOres) selected.addAll(entry.blocks());
    return selected.stream().map(Block::defaultBlockState).toList();
  }

  private static void oceanSpires(ChunkAccess chunk, long seed) {
    int cx = chunk.getPos().x, cz = chunk.getPos().z;
    for (int nx = cx - 1; nx <= cx + 1; nx++)
      for (int nz = cz - 1; nz <= cz + 1; nz++) {
        Random random = random(seed ^ 0x4fb2L, nx, nz);
        if (random.nextInt(4) != 0) continue;
        int centerX = nx * 16 + random.nextInt(15);
        int centerZ = nz * 16 + random.nextInt(15);
        int height = 20 + random.nextInt(10);
        for (int y = 37; y < 57 + height; y++) {
          float progress = (y - 37f) / (20f + height);
          int radius = Math.max(1, (int) (5 - 3 * progress));
          BlockState material =
              progress < .33f
                  ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                  : progress < .66f
                      ? Blocks.COBBLESTONE.defaultBlockState()
                      : progress > .95f
                          ? Blocks.GRASS_BLOCK.defaultBlockState()
                          : Blocks.DIRT.defaultBlockState();
          for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++)
            for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++)
              if (Math.abs(x - centerX) + Math.abs(z - centerZ) <= radius + 2
                  && Math.max(Math.abs(x - centerX), Math.abs(z - centerZ)) <= radius)
                set(chunk, x, y, z, material);
        }
      }
  }

  public static void ores(ChunkAccess chunk, Planet planet, long seed, Set<String> placed) {
    Random random = random(seed ^ 0x51a7L, chunk.getPos().x, chunk.getPos().z);
    for (String name : ORES) {
      if (name.equals("copper") && planet.breathable() || placed.contains(name)) continue;
      for (int vein = 0;
          vein < AdvancedRocketryConfig.orePerChunk(name, planet.atmosphere == 0);
          vein++) {
        int x = chunk.getPos().getMinBlockX() + random.nextInt(16),
            z = chunk.getPos().getMinBlockZ() + random.nextInt(16),
            y = random.nextInt(64);
        BlockState ore =
            name.equals("copper")
                ? Blocks.COPPER_ORE.defaultBlockState()
                : ProcessingRegistry.part(name + "_ore").defaultBlockState();
        for (int block = 0; block < AdvancedRocketryConfig.oreClumpSize(name); block++) {
          BlockPos pos = new BlockPos(x, y, z);
          BlockState existing = chunk.getBlockState(pos);
          if (existing.is(Blocks.STONE)
              || existing.is(Blocks.DEEPSLATE)
              || existing.is(ProcessingRegistry.part("moon_turf"))
              || existing.is(ProcessingRegistry.part("basalt"))) set(chunk, x, y, z, ore);
          x =
              Math.clamp(
                  x + random.nextInt(3) - 1,
                  chunk.getPos().getMinBlockX(),
                  chunk.getPos().getMaxBlockX());
          z =
              Math.clamp(
                  z + random.nextInt(3) - 1,
                  chunk.getPos().getMinBlockZ(),
                  chunk.getPos().getMaxBlockZ());
          y = Math.clamp(y + random.nextInt(3) - 1, 1, 63);
        }
      }
    }
  }

  public static void flora(WorldGenLevel level, ChunkAccess chunk, long seed) {
    RandomSource random =
        RandomSource.create(
            PlanetSeeds.column(seed ^ 0x28af9L, chunk.getPos().x, chunk.getPos().z));
    int originX = chunk.getPos().getMinBlockX();
    int originZ = chunk.getPos().getMinBlockZ();
    BlockPos center =
        level.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING, new BlockPos(originX + 8, 0, originZ + 8));
    if (level.getBiome(center).is(ALIEN_FOREST)) {
      if (random.nextInt(20) == 0) AlienTree.grow(level, center, random);
      grass(level, random, originX, originZ, ALIEN_FOREST, 50);
    } else if (level.getBiome(center).is(OCEAN_SPIRES)) {
      grass(level, random, originX, originZ, OCEAN_SPIRES, 7);
    } else if (level.getBiome(center).is(STORMLAND)) {
      BlockState charred = ProcessingRegistry.part("charcoal_log").defaultBlockState();
      for (int tree = 0; tree < 6; tree++) {
        BlockPos base =
            level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING,
                new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
        if (!level.getBiome(base).is(STORMLAND) || !level.getBlockState(base.below()).isSolid())
          continue;
        for (int y = 0, height = 6 + random.nextInt(3); y < height; y++) {
          BlockPos pos = base.above(y);
          if (!level.getBlockState(pos).isAir()) break;
          level.setBlock(pos, charred, 2);
        }
      }
      BlockState mushroom = ProcessingRegistry.part("electric_mushroom").defaultBlockState();
      for (int attempt = 0; attempt < 64; attempt++) {
        BlockPos pos =
            level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING,
                new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
        if (level.getBiome(pos).is(STORMLAND)
            && mushroom.canSurvive(level, pos)
            && level.getBlockState(pos).isAir()
            && random.nextInt(8) == 0) level.setBlock(pos, mushroom, 2);
      }
    } else if (level.getBiome(center).is(MARSH) || level.getBiome(center).is(DEEP_SWAMP)) {
      boolean swamp = level.getBiome(center).is(DEEP_SWAMP);
      int minX = originX, maxX = originX + 15, minZ = originZ, maxZ = originZ + 15;
      for (int attempt = 0; attempt < (swamp ? 1 : 10); attempt++) {
        BlockPos pos =
            level.getHeightmapPos(
                Heightmap.Types.OCEAN_FLOOR,
                new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
        if (!level.getBiome(pos).is(swamp ? DEEP_SWAMP : MARSH)) continue;
        BlockPos ground = pos.below();
        if (level.getBlockState(ground).is(Blocks.DIRT)
            || level.getBlockState(ground).is(Blocks.GRASS_BLOCK))
          for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
              BlockPos patch = ground.offset(dx, 0, dz);
              if (patch.getX() >= minX
                  && patch.getX() <= maxX
                  && patch.getZ() >= minZ
                  && patch.getZ() <= maxZ
                  && random.nextInt(3) != 0)
                level.setBlock(patch, Blocks.CLAY.defaultBlockState(), 2);
            }
      }
      if (swamp) {
        for (int attempt = 0; attempt < 10; attempt++) {
          BlockPos base =
              level.getHeightmapPos(
                  Heightmap.Types.MOTION_BLOCKING,
                  new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
          if (level.getBiome(base).is(DEEP_SWAMP) && random.nextInt(4) == 0)
            swampTree(level, base, random, minX, maxX, minZ, maxZ);
        }
        for (int attempt = 0; attempt < 24; attempt++) {
          BlockPos pos =
              level.getHeightmapPos(
                  Heightmap.Types.MOTION_BLOCKING,
                  new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
          if (!level.getBiome(pos).is(DEEP_SWAMP) || !level.getBlockState(pos).isAir()) continue;
          BlockState plant =
              attempt < 8
                  ? (random.nextBoolean()
                      ? Blocks.BROWN_MUSHROOM.defaultBlockState()
                      : Blocks.RED_MUSHROOM.defaultBlockState())
                  : attempt < 18
                      ? Blocks.SUGAR_CANE.defaultBlockState()
                      : attempt < 23
                          ? Blocks.SHORT_GRASS.defaultBlockState()
                          : Blocks.DEAD_BUSH.defaultBlockState();
          if (plant.canSurvive(level, pos)) level.setBlock(pos, plant, 2);
        }
      }
      for (int attempt = 0; attempt < (swamp ? 16 : 10); attempt++) {
        BlockPos pos =
            level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING,
                new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
        if (!level.getBiome(pos).is(swamp ? DEEP_SWAMP : MARSH)) continue;
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.WATER) && level.getBlockState(pos).isAir())
          level.setBlock(pos, Blocks.LILY_PAD.defaultBlockState(), 2);
        else if (swamp
            && below.is(Blocks.GRASS_BLOCK)
            && level.getBlockState(pos).isAir()
            && random.nextInt(4) == 0)
          level.setBlock(pos, Blocks.BLUE_ORCHID.defaultBlockState(), 2);
      }
    }
  }

  private static void grass(
      WorldGenLevel level,
      RandomSource random,
      int originX,
      int originZ,
      ResourceKey<Biome> biome,
      int attempts) {
    for (int attempt = 0; attempt < attempts; attempt++) {
      BlockPos pos =
          level.getHeightmapPos(
              Heightmap.Types.MOTION_BLOCKING,
              new BlockPos(originX + random.nextInt(16), 0, originZ + random.nextInt(16)));
      BlockState plant = Blocks.SHORT_GRASS.defaultBlockState();
      if (level.getBiome(pos).is(biome)
          && level.getBlockState(pos).isAir()
          && plant.canSurvive(level, pos)) level.setBlock(pos, plant, 2);
    }
  }

  private static void swampTree(
      WorldGenLevel level,
      BlockPos base,
      RandomSource random,
      int minX,
      int maxX,
      int minZ,
      int maxZ) {
    if (!level.getBlockState(base.below()).is(Blocks.GRASS_BLOCK)
        && !level.getBlockState(base.below()).is(Blocks.DIRT)) return;
    int height = 4 + random.nextInt(3);
    for (int y = 0; y < height + 2; y++) if (!level.getBlockState(base.above(y)).isAir()) return;
    for (int y = 0; y < height; y++)
      level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), 2);
    for (int y = height - 2; y <= height + 1; y++) {
      int radius = y == height + 1 ? 1 : 2;
      for (int dx = -radius; dx <= radius; dx++)
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextBoolean()) continue;
          BlockPos leaf = base.offset(dx, y, dz);
          if (leaf.getX() >= minX
              && leaf.getX() <= maxX
              && leaf.getZ() >= minZ
              && leaf.getZ() <= maxZ
              && level.getBlockState(leaf).isAir())
            level.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState(), 2);
        }
    }
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.processing.ProcessingRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Native noise generation plus the original planet surface and decoration rules. */
public final class PlanetTerrain extends NoiseBasedChunkGenerator {
  public static final MapCodec<PlanetTerrain> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      BiomeSource.CODEC
                          .fieldOf("biome_source")
                          .forGetter(PlanetTerrain::getBiomeSource),
                      NoiseGeneratorSettings.CODEC
                          .fieldOf("settings")
                          .forGetter(PlanetTerrain::generatorSettings),
                      CompoundTag.CODEC
                          .fieldOf("planet")
                          .forGetter(generator -> generator.planet.save()),
                      Codec.LONG.fieldOf("seed").forGetter(generator -> generator.seed))
                  .apply(
                      instance,
                      (biomes, settings, tag, seed) ->
                          new PlanetTerrain(biomes, settings, Planet.load(tag), seed)));
  private final Planet planet;
  private final long seed;

  public PlanetTerrain(
      BiomeSource source, Holder<NoiseGeneratorSettings> settings, Planet planet, long seed) {
    super(source, PlanetOres.noiseSettings(settings, planet));
    this.planet = planet;
    this.seed = seed;
    generationSettingsGetter = PlanetOres.settings(planet);
  }

  @Override
  protected MapCodec<? extends ChunkGenerator> codec() {
    return CODEC;
  }

  @Override
  public void createStructures(
      RegistryAccess registries,
      ChunkGeneratorStructureState state,
      StructureManager structures,
      ChunkAccess chunk,
      StructureTemplateManager templates) {
    if (AdvancedRocketryConfig.generateVanillaStructures()
        && planet.structures
        && planet.terrain != TerrainType.SPACE)
      super.createStructures(registries, state, structures, chunk, templates);
  }

  @Override
  public CompletableFuture<ChunkAccess> fillFromNoise(
      Blender blender, RandomState random, StructureManager structures, ChunkAccess chunk) {
    if (planet.terrain == TerrainType.SPACE) return CompletableFuture.completedFuture(chunk);
    return super.fillFromNoise(blender, random, structures, chunk);
  }

  @Override
  public void applyCarvers(
      WorldGenRegion level,
      long seed,
      RandomState random,
      BiomeManager biomes,
      StructureManager structures,
      ChunkAccess chunk,
      GenerationStep.Carving step) {
    if (planet.caves && planet.terrain != TerrainType.SPACE)
      super.applyCarvers(level, seed, random, biomes, structures, chunk, step);
  }

  @Override
  public void spawnOriginalMobs(WorldGenRegion level) {
    if (planet.breathable()) super.spawnOriginalMobs(level);
  }

  @Override
  public void buildSurface(
      WorldGenRegion level, StructureManager structures, RandomState random, ChunkAccess chunk) {
    if (planet.terrain == TerrainType.SPACE) return;
    super.buildSurface(level, structures, random, chunk);
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++) {
        int top = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        String biome =
            chunk
                .getNoiseBiome(x >> 2, top >> 2, z >> 2)
                .unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("");
        BlockState surface =
            switch (biome) {
              case "moon" -> ProcessingRegistry.part("moon_turf").defaultBlockState();
              case "moondark" -> ProcessingRegistry.part("moon_turf_dark").defaultBlockState();
              case "hotdryrock", "volcanic" ->
                  ProcessingRegistry.part("hot_turf").defaultBlockState();
              case "volcanicbarren" -> ProcessingRegistry.part("basalt").defaultBlockState();
              case "crystalchasms" -> Blocks.SNOW_BLOCK.defaultBlockState();
              case "oceanspires" -> Blocks.GRAVEL.defaultBlockState();
              default ->
                  switch (planet.terrain) {
                    case MOON -> ProcessingRegistry.part("moon_turf").defaultBlockState();
                    case VOLCANIC -> ProcessingRegistry.part("hot_turf").defaultBlockState();
                    default -> null;
                  };
            };
        if (surface == null) continue;
        for (int y = top; y > top - 4 && y > chunk.getMinBuildHeight(); y--) {
          pos.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
          if (!chunk.getBlockState(pos).isAir())
            chunk.setBlockState(
                pos,
                biome.equals("crystalchasms") && y < top
                    ? Blocks.PACKED_ICE.defaultBlockState()
                    : surface,
                false);
        }
      }
    PlanetDecorations.generate(
        chunk,
        planet,
        seed,
        (x, z) -> getBiomeSource().getNoiseBiome(x >> 2, 16, z >> 2, random.sampler()));
  }

  @Override
  public void applyBiomeDecoration(
      WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
    if (planet.terrain == TerrainType.SPACE) return;
    boolean decorated = planet.breathable();
    if (decorated) super.applyBiomeDecoration(level, chunk, structures);
    if (!PlanetOres.generate(level, chunk, this, planet, seed))
      PlanetDecorations.ores(chunk, planet, seed, decorated ? biomeOres(level, chunk) : Set.of());
    PlanetDecorations.flora(level, chunk, seed);
  }

  private Set<String> biomeOres(WorldGenLevel level, ChunkAccess chunk) {
    Set<Holder<Biome>> biomes = new HashSet<>();
    for (LevelChunkSection section : chunk.getSections()) section.getBiomes().getAll(biomes::add);
    var registry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
    Set<String> placed = new HashSet<>();
    for (String name : OreWorldgen.NAMES) {
      PlacedFeature feature =
          registry.get(ResourceLocation.fromNamespaceAndPath(Main.MODID, name + "_ore"));
      if (feature != null
          && biomes.stream()
              .anyMatch(biome -> generationSettingsGetter.apply(biome).hasFeature(feature)))
        placed.add(name);
    }
    return placed;
  }
}

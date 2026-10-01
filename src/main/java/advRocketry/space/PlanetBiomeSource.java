// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/** Seeded biome regions for the original configurable planet biome lists. */
public final class PlanetBiomeSource extends BiomeSource {
  public static final MapCodec<PlanetBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      Biome.CODEC.listOf().fieldOf("biomes").forGetter(source -> source.biomes),
                      Codec.intRange(1, 1000000)
                          .listOf()
                          .optionalFieldOf("weights", List.of())
                          .forGetter(source -> source.weights),
                      Codec.LONG.fieldOf("seed").forGetter(source -> source.seed),
                      Biome.CODEC.optionalFieldOf("river").forGetter(source -> source.river),
                      Biome.CODEC
                          .optionalFieldOf("frozen_river")
                          .forGetter(source -> source.frozenRiver))
                  .apply(instance, PlanetBiomeSource::new));
  private final List<Holder<Biome>> biomes;
  private final List<Integer> weights;
  private final long totalWeight;
  private final long seed;
  private final Optional<Holder<Biome>> river;
  private final Optional<Holder<Biome>> frozenRiver;

  public PlanetBiomeSource(List<Holder<Biome>> biomes, long seed) {
    this(biomes, List.of(), seed);
  }

  public PlanetBiomeSource(List<Holder<Biome>> biomes, List<Integer> weights, long seed) {
    this(biomes, weights, seed, Optional.empty(), Optional.empty());
  }

  public static PlanetBiomeSource forPlanet(
      List<Holder<Biome>> biomes,
      List<Integer> weights,
      long seed,
      Planet planet,
      Registry<Biome> registry) {
    if (!planet.hasRivers()) return new PlanetBiomeSource(biomes, weights, seed);
    return new PlanetBiomeSource(
        biomes,
        weights,
        seed,
        Optional.of(
            registry.getHolderOrThrow(
                ResourceKey.create(
                    Registries.BIOME, ResourceLocation.withDefaultNamespace("river")))),
        Optional.of(
            registry.getHolderOrThrow(
                ResourceKey.create(
                    Registries.BIOME, ResourceLocation.withDefaultNamespace("frozen_river")))));
  }

  public PlanetBiomeSource(
      List<Holder<Biome>> biomes,
      List<Integer> weights,
      long seed,
      Optional<Holder<Biome>> river,
      Optional<Holder<Biome>> frozenRiver) {
    if (biomes.isEmpty()) throw new IllegalArgumentException("Planet requires at least one biome");
    if (!weights.isEmpty() && weights.size() != biomes.size()
        || weights.stream().anyMatch(weight -> weight <= 0 || weight > 1000000))
      throw new IllegalArgumentException(
          "Planet biome weights must match the biome list and be positive");
    this.biomes = List.copyOf(biomes);
    this.weights =
        weights.isEmpty() ? biomes.stream().map(biome -> 1).toList() : List.copyOf(weights);
    totalWeight = this.weights.stream().mapToLong(Integer::longValue).sum();
    this.seed = seed;
    this.river = river;
    this.frozenRiver = frozenRiver;
  }

  @Override
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.concat(biomes.stream(), Stream.concat(river.stream(), frozenRiver.stream()));
  }

  @Override
  public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
    long value = PlanetSeeds.cell(seed, Math.floorDiv(x, 32), Math.floorDiv(z, 32));
    value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
    value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
    long selected = Math.floorMod(value ^ (value >>> 31), totalWeight);
    Holder<Biome> biome = biomes.getLast();
    for (int i = 0; i < weights.size(); i++) {
      selected -= weights.get(i);
      if (selected < 0) {
        biome = biomes.get(i);
        break;
      }
    }
    if (river.isPresent() && sampler != null && !biome.is(BiomeTags.IS_OCEAN)) {
      Climate.TargetPoint climate = sampler.sample(x, 0, z);
      // Native Overworld valleys replace the old GenLayerRiverMix river network.
      if (Math.abs(Climate.unquantizeCoord(climate.weirdness())) < .05
          && Climate.unquantizeCoord(climate.continentalness()) > -.19)
        return biome.value().getBaseTemperature() < .15
            ? frozenRiver.orElse(river.get())
            : river.get();
    }
    return biome;
  }
}

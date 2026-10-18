// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/** Seeded biome regions for the original configurable planet biome lists. */
public final class PlanetBiomeSource extends BiomeSource {
  private record Definition(
      List<Holder<Biome>> biomes,
      List<Integer> weights,
      long seed,
      Optional<Holder<Biome>> river,
      Optional<Holder<Biome>> frozenRiver,
      List<Holder<Biome>> caves,
      String profile,
      int sea,
      boolean channels,
      int version,
      double gravity,
      boolean watersheds,
      List<Holder<Biome>> riparian,
      Optional<PlanetMarineHabitat> marine) {}

  public static final MapCodec<PlanetBiomeSource> CODEC =
      RecordCodecBuilder.<Definition>mapCodec(
              instance ->
                  instance
                      .group(
                          Biome.CODEC
                              .listOf(1, 1024)
                              .fieldOf("biomes")
                              .forGetter(source -> source.biomes),
                          Codec.intRange(1, 1000000)
                              .listOf(0, 1024)
                              .optionalFieldOf("weights", List.of())
                              .forGetter(source -> source.weights),
                          Codec.LONG.fieldOf("seed").forGetter(source -> source.seed),
                          Biome.CODEC.optionalFieldOf("river").forGetter(source -> source.river),
                          Biome.CODEC
                              .optionalFieldOf("frozen_river")
                              .forGetter(source -> source.frozenRiver),
                          Biome.CODEC
                              .listOf()
                              .optionalFieldOf("cave_biomes", List.of())
                              .forGetter(source -> source.caves),
                          PlanetDensity.PROFILE_CODEC
                              .optionalFieldOf("profile", "temperate")
                              .forGetter(source -> source.profile),
                          Codec.intRange(-63, 319)
                              .optionalFieldOf("sea_level", 63)
                              .forGetter(source -> source.sea),
                          Codec.BOOL
                              .optionalFieldOf("channels", false)
                              .forGetter(source -> source.channels),
                          Codec.intRange(1, 6)
                              .optionalFieldOf("generation_version", 1)
                              .forGetter(source -> source.version),
                          Codec.doubleRange(0, Double.MAX_VALUE)
                              .optionalFieldOf("gravity", 1d)
                              .forGetter(source -> source.gravity),
                          Codec.BOOL
                              .optionalFieldOf("watersheds", false)
                              .forGetter(source -> source.watersheds),
                          Biome.CODEC
                              .listOf()
                              .optionalFieldOf("riparian_biomes", List.of())
                              .forGetter(source -> source.riparian),
                          PlanetMarineHabitat.CODEC
                              .optionalFieldOf("ocean_habitats")
                              .forGetter(source -> source.marine))
                      .apply(instance, Definition::new))
          .flatXmap(
              definition -> {
                if (!definition.weights.isEmpty()
                    && definition.weights.size() != definition.biomes.size())
                  return DataResult.error(() -> "Planet biome weights must match the biome list");
                return DataResult.success(
                    new PlanetBiomeSource(
                        definition.biomes,
                        definition.weights,
                        definition.seed,
                        definition.river,
                        definition.frozenRiver,
                        definition.caves,
                        definition.profile,
                        definition.sea,
                        definition.channels,
                        definition.version,
                        definition.gravity,
                        definition.watersheds,
                        definition.riparian,
                        definition.marine));
              },
              source ->
                  DataResult.success(
                      new Definition(
                          source.biomes,
                          source.weights,
                          source.seed,
                          source.river,
                          source.frozenRiver,
                          source.caves,
                          source.profile,
                          source.sea,
                          source.channels,
                          source.version,
                          source.gravity,
                          source.watersheds,
                          source.riparian,
                          source.marine)));
  private final List<Holder<Biome>> biomes;
  private final List<Integer> weights;
  private final long totalWeight;
  private final long seed;
  private final Optional<Holder<Biome>> river;
  private final Optional<Holder<Biome>> frozenRiver;
  private final List<Holder<Biome>> caves;
  private final String profile;
  private final int sea;
  private final boolean channels;
  private final int version;
  private final double gravity;
  private final boolean watersheds;
  private final List<Holder<Biome>> riparian;
  private final Optional<PlanetMarineHabitat> marine;

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
    if (planet.generationVersion >= 2 && planet.terrain != TerrainType.SPACE) {
      String profile = PlanetLandforms.profile(planet);
      List<Holder<Biome>> caves = new ArrayList<>();
      if (planet.caves) {
        List<String> names =
            profile.equals("lunar") || planet.atmosphere <= 25
                ? List.of("adv_rocketry:lunar_tubes", "adv_rocketry:mineral_caves")
                : profile.equals("volcanic")
                    ? List.of("adv_rocketry:lava_tubes", "adv_rocketry:mineral_caves")
                    : profile.equals("arid")
                        ? List.of("adv_rocketry:mineral_caves", "minecraft:dripstone_caves")
                        : planet.temperature < 250
                            ? List.of("adv_rocketry:subglacial_caves", "adv_rocketry:mineral_caves")
                            : List.of(
                                "adv_rocketry:root_caverns",
                                "minecraft:lush_caves",
                                "adv_rocketry:fungal_caverns",
                                "minecraft:dripstone_caves",
                                "adv_rocketry:mineral_caves");
        for (String name : names)
          registry
              .getHolder(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(name)))
              .ifPresent(caves::add);
      }
      if (caves.size() == 5
          && profile.equals("temperate")
          && planet.atmosphere > 25
          && planet.temperature >= 250
          && planet.temperature <= 450) {
        registry
            .getTag(TagKey.create(Registries.BIOME, BiomeMaterials.id("planet_biomes/caves")))
            .ifPresent(
                tag ->
                    tag.stream()
                        .sorted(
                            (a, b) ->
                                a.unwrapKey()
                                    .orElseThrow()
                                    .location()
                                    .compareTo(b.unwrapKey().orElseThrow().location()))
                        .forEach(
                            b -> {
                              if (!caves.contains(b)) caves.add(b);
                            }));
      }
      boolean water = planet.ocean.equals(ResourceLocation.withDefaultNamespace("water"));
      Optional<Holder<Biome>> river =
          planet.hasRivers()
                  && water
                  && !profile.equals("lunar")
                  && !profile.equals("volcanic")
                  && !profile.equals("fractured")
                  && !profile.equals("cavern")
                  && !profile.equals("arid")
              ? registry
                  .getHolder(ResourceKey.create(Registries.BIOME, BiomeMaterials.id("alien_river")))
                  .map(b -> b)
              : Optional.empty();
      Optional<Holder<Biome>> frozen =
          river.isPresent()
              ? registry
                  .getHolder(
                      ResourceKey.create(
                          Registries.BIOME, ResourceLocation.withDefaultNamespace("frozen_river")))
                  .map(b -> b)
              : Optional.empty();
      List<Holder<Biome>> riparian = new ArrayList<>();
      if (PlanetWatershed.enabled(planet)
          && biomes.stream()
              .anyMatch(
                  b ->
                      b.is(BiomeMaterials.id("alien_forest"))
                          || b.is(BiomeMaterials.id("deepswamp"))
                          || b.is(BiomeMaterials.id("marsh")))) {
        for (String name : List.of("marsh", "deepswamp"))
          registry
              .getHolder(ResourceKey.create(Registries.BIOME, BiomeMaterials.id(name)))
              .ifPresent(riparian::add);
      }
      return new PlanetBiomeSource(
          biomes,
          weights,
          seed,
          river,
          frozen,
          caves,
          profile,
          planet.seaLevel,
          PlanetNoise.channels(planet),
          planet.generationVersion,
          planet.gravity,
          PlanetWatershed.enabled(planet),
          riparian,
          Optional.of(
              new PlanetMarineHabitat(
                  PlanetMarineHabitat.supportsWater(planet),
                  biomes.stream()
                      .filter(b -> !PlanetMarineHabitat.ocean(b))
                      .findFirst()
                      .orElseGet(
                          () ->
                              registry.getHolderOrThrow(
                                  ResourceKey.create(
                                      Registries.BIOME,
                                      BiomeMaterials.id(
                                          profile.equals("lunar")
                                              ? "moon"
                                              : profile.equals("volcanic")
                                                  ? "volcanic"
                                                  : PlanetMarineHabitat.supportsWater(planet)
                                                      ? "alien_forest"
                                                      : "hotdryrock")))),
                  3,
                  24)));
    }
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
    this(
        biomes,
        weights,
        seed,
        river,
        frozenRiver,
        List.of(),
        "temperate",
        63,
        river.isPresent(),
        1,
        1,
        false,
        List.of(),
        Optional.empty());
  }

  private PlanetBiomeSource(
      List<Holder<Biome>> biomes,
      List<Integer> weights,
      long seed,
      Optional<Holder<Biome>> river,
      Optional<Holder<Biome>> frozenRiver,
      List<Holder<Biome>> caves,
      String profile,
      int sea,
      boolean channels,
      int version,
      double gravity,
      boolean watersheds,
      List<Holder<Biome>> riparian,
      Optional<PlanetMarineHabitat> marine) {
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
    this.caves = List.copyOf(caves);
    this.profile = profile;
    this.sea = sea;
    this.channels = channels;
    this.version = version;
    this.gravity = gravity;
    this.watersheds = watersheds;
    this.riparian = List.copyOf(riparian);
    this.marine = marine;
  }

  @Override
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.concat(
            marine.stream().map(PlanetMarineHabitat::landFallback),
            Stream.concat(
                Stream.concat(caves.stream(), riparian.stream()),
                Stream.concat(
                    biomes.stream(), Stream.concat(river.stream(), frozenRiver.stream()))))
        .distinct();
  }

  @Override
  public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
    if (version >= 2) return habitat(x * 4, y * 4, z * 4);
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

  private Holder<Biome> habitat(int x, int y, int z) {
    double moisture = PlanetLandforms.moisture(seed, x, z);
    double temperature = PlanetLandforms.temperature(seed, x, z);
    double land = PlanetLandforms.continents(seed, x, z);
    PlanetWatershed.Sample water = watersheds ? PlanetWatershed.sample(seed, gravity, x, z) : null;
    double top =
        PlanetGeology.enabled(version, profile)
            ? PlanetGeology.height(seed, profile, sea, gravity, x, z)
            : watersheds
                ? PlanetWatershed.height(seed, sea, gravity, x, z)
                : PlanetLandforms.height(seed, profile, sea, channels, x, z);
    double depth =
        version >= 6 && profile.equals("fractured")
            ? PlanetFractures.depth(seed, sea, x, y, z)
            : top - y;
    if (water != null) moisture = Math.min(1, moisture + water.banks() * .45);
    if (!caves.isEmpty() && depth > 18 + 7 * PlanetLandforms.field(seed, 59, x, z, 95)) {
      if (depth > 90 && !profile.equals("cavern") || y < -24)
        return caves.get(Math.min(4, caves.size() - 1));
      if (caves.size() <= 2) return caves.getFirst();
      if (caves.size() > 5 && PlanetLandforms.field(seed, 263, x, z, 90) > .25) {
        double patch = PlanetLandforms.field(seed, 269, x, z, 150);
        int index = Math.clamp((int) ((patch + 1) * .5 * (caves.size() - 5)), 0, caves.size() - 6);
        return caves.get(5 + index);
      }
      if (water != null
          && water.underground() < .09
          && Math.abs(y - PlanetWatershed.waterLevel(sea)) < 18)
        return caves.get(water.sediment() > 0 ? 0 : 1);
      if (moisture > .18) return caves.get(temperature > 0 ? 0 : 1);
      if (moisture < -.22) return caves.get(2);
      return caves.get(Math.min(3, caves.size() - 1));
    }
    boolean oceanRegion =
        marine.isPresent()
            && biomes.stream()
                .anyMatch(
                    b -> PlanetMarineHabitat.ocean(b) && marine.get().allows(b, sea - top, land));
    Holder<Biome> selected =
        marine.map(PlanetMarineHabitat::landFallback).orElse(biomes.getFirst());
    double lunarBasin =
        version >= 5 && profile.equals("lunar") ? PlanetFrontiers.basin(seed, x, z).interior() : -1;
    double best = Double.POSITIVE_INFINITY;
    for (int i = 0; i < biomes.size(); i++) {
      Holder<Biome> b = biomes.get(i);
      if (marine.isPresent()) {
        boolean ocean = PlanetMarineHabitat.ocean(b);
        if (ocean != oceanRegion || ocean && !marine.get().allows(b, sea - top, land)) continue;
      }
      String name = b.unwrapKey().map(k -> k.location().toString()).orElse("");
      double targetWet =
          switch (name) {
            case "adv_rocketry:deepswamp", "adv_rocketry:marsh", "adv_rocketry:stormland" -> .65;
            case "adv_rocketry:alien_forest" -> .28;
            case "adv_rocketry:hotdryrock", "adv_rocketry:volcanicbarren" -> -.65;
            default -> b.value().hasPrecipitation() ? .1 : -.3;
          };
      double targetTemp = Math.clamp((b.value().getBaseTemperature() - .65) * .9, -.8, .8);
      double targetLand =
          PlanetMarineHabitat.ocean(b) || name.equals("adv_rocketry:oceanspires")
              ? -.65
              : name.equals("adv_rocketry:moondark") || name.equals("adv_rocketry:marsh")
                  ? -.15
                  : .3;
      double variation = PlanetLandforms.field(seed ^ name.hashCode(), 61, x, z, 260);
      double score =
          Math.pow(moisture - targetWet, 2) * .45
              + Math.pow(temperature - targetTemp, 2) * .35
              + Math.pow(land - targetLand, 2) * .4
              - variation * .35
              - Math.log(weights.get(i)) * .08;
      if (lunarBasin >= 0) {
        if (name.equals("adv_rocketry:moondark")) score += (1 - lunarBasin) * 1.2;
        if (name.equals("adv_rocketry:moon")) score += lunarBasin * 1.2;
      }
      if (score < best) {
        best = score;
        selected = b;
      }
    }
    if (river.isPresent()
        && depth < 18
        && (water != null ? water.river() : PlanetLandforms.channel(seed, x, z) < .045)
        && land > -.35
        && !PlanetMarineHabitat.ocean(selected))
      return selected.value().getBaseTemperature() < .15
          ? frozenRiver.orElse(river.get())
          : river.get();
    if (water != null
        && !riparian.isEmpty()
        && depth < 18
        && water.floodplain()
        && top > sea - 2
        && top < sea + 7
        && land > -.35
        && !PlanetMarineHabitat.ocean(selected))
      return water.wetness() > .72 && water.clearing() > -.15
          ? riparian.getLast()
          : riparian.getFirst();
    return selected;
  }
}

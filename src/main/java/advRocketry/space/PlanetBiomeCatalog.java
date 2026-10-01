// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Original climate and biome-list rules applied to every biome in the loaded server registry. */
public final class PlanetBiomeCatalog {
  public record Entry(
      ResourceLocation id, float temperature, boolean ocean, boolean hot, boolean cold) {}

  public record Policy(
      Set<ResourceLocation> blacklist,
      boolean vanilla,
      List<ResourceLocation> pressure,
      List<ResourceLocation> single,
      List<ResourceLocation> airless,
      List<ResourceLocation> scorched,
      int limit) {}

  private static volatile List<Entry> entries = List.of();
  private static volatile Set<ResourceLocation> excluded = Set.of();
  private static volatile List<ResourceLocation> highPressure = List.of();
  private static volatile List<ResourceLocation> single = List.of();
  private static volatile List<ResourceLocation> airless = List.of();
  private static volatile List<ResourceLocation> scorched = List.of();

  private PlanetBiomeCatalog() {}

  public static void starting(ServerAboutToStartEvent event) {
    var registry = event.getServer().registryAccess().registryOrThrow(Registries.BIOME);
    excluded = Set.copyOf(tagged(registry, ModTags.EXCLUDED_PLANET_BIOMES));
    highPressure = tagged(registry, ModTags.HIGH_PRESSURE_BIOMES);
    single = tagged(registry, ModTags.SINGLE_BIOMES);
    airless = tagged(registry, ModTags.AIRLESS_BIOMES);
    scorched = tagged(registry, ModTags.SCORCHED_BIOMES);
    entries =
        registry
            .holders()
            .map(
                holder ->
                    new Entry(
                        holder.key().location(),
                        holder.value().getBaseTemperature(),
                        holder.is(BiomeTags.IS_OCEAN),
                        holder.is(
                                TagKey.create(Registries.BIOME, ResourceLocation.parse("c:is_hot")))
                            || holder.value().getBaseTemperature() > 1,
                        holder.is(
                                TagKey.create(
                                    Registries.BIOME, ResourceLocation.parse("c:is_cold")))
                            || holder.value().getBaseTemperature() < .2f))
            .sorted((left, right) -> left.id().compareTo(right.id()))
            .toList();
  }

  public static void stopped(ServerStoppedEvent event) {
    entries = List.of();
    excluded = Set.of();
    highPressure = List.of();
    single = List.of();
    airless = List.of();
    scorched = List.of();
  }

  private static List<ResourceLocation> tagged(Registry<Biome> registry, TagKey<Biome> tag) {
    return registry.getTag(tag).stream()
        .flatMap(HolderSet.ListBacked::stream)
        .flatMap(holder -> holder.unwrapKey().stream())
        .map(ResourceKey::location)
        .sorted()
        .distinct()
        .toList();
  }

  public static List<ResourceLocation> select(Planet planet, Random random) {
    return select(
        planet,
        random,
        entries,
        new Policy(
            excluded,
            AdvancedRocketryConfig.blacklistVanillaBiomes(),
            highPressure,
            single,
            airless,
            scorched,
            AdvancedRocketryConfig.maxBiomesPerPlanet()));
  }

  public static List<ResourceLocation> select(
      Planet planet, Random random, Collection<Entry> catalog, Policy policy) {
    if (catalog.isEmpty()) throw new IllegalStateException("Planet biome registry is not loaded");
    if (planet.atmosphere <= 25) return policy.airless();
    Set<ResourceLocation> excluded = new LinkedHashSet<>(policy.blacklist());
    excluded.addAll(policy.pressure());
    if (policy.vanilla())
      for (Entry entry : catalog)
        if (entry.id().getNamespace().equals("minecraft")) excluded.add(entry.id());
    if (random.nextInt(3) == 0) {
      List<Entry> single =
          catalog.stream()
              .filter(
                  entry ->
                      policy.single().contains(entry.id())
                          && !excluded.contains(entry.id())
                          && singleClimate(entry, planet.temperature))
              .toList();
      if (!single.isEmpty()) return List.of(single.get(random.nextInt(single.size())).id());
    }
    List<ResourceLocation> candidates = new ArrayList<>();
    if (planet.temperature > 450) candidates.addAll(policy.scorched());
    else
      for (Entry entry : catalog) {
        if (excluded.contains(entry.id())) continue;
        boolean eligible =
            planet.temperature > 325
                ? entry.hot() || entry.ocean()
                : planet.temperature > 275
                    ? !entry.cold()
                    : planet.temperature > 250 ? !entry.hot() : entry.cold();
        if (eligible) candidates.add(entry.id());
      }
    Collections.shuffle(candidates, random);
    // A zero limit cannot produce a valid native biome source, so retain one candidate.
    int count =
        policy.limit() < 0
            ? candidates.size()
            : Math.min(Math.max(1, policy.limit()), candidates.size());
    Set<ResourceLocation> result = new LinkedHashSet<>(candidates.subList(0, count));
    if (planet.atmosphere > 200 && planet.temperature > 275 && planet.temperature <= 450)
      for (Entry entry : catalog)
        if (policy.pressure().contains(entry.id())) result.add(entry.id());
    if (result.isEmpty())
      throw new IllegalStateException(
          "No eligible biomes for planet "
              + planet.name
              + "; check biome blacklists and climate ("
              + planet.temperature
              + " K)");
    return List.copyOf(result);
  }

  private static boolean singleClimate(Entry entry, int temperature) {
    if (entry.ocean() || entry.temperature() >= .2f && entry.temperature() < 1)
      return temperature > 250 && temperature <= 450;
    return entry.temperature() < .2f
        ? temperature > 175 && temperature <= 325
        : temperature > 275 && temperature <= 450;
  }
}

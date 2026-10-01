// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.DataRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/** Original XMLOreLoader rules and CustomizableOreGen, using native ore features. */
public final class PlanetOres {
  public record Entry(ResourceLocation block, int minHeight, int maxHeight, int size, int count) {
    private static final Codec<Entry> UNCHECKED =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ResourceLocation.CODEC.fieldOf("block").forGetter(Entry::block),
                        Codec.INT.fieldOf("min_height").forGetter(Entry::minHeight),
                        Codec.INT.fieldOf("max_height").forGetter(Entry::maxHeight),
                        Codec.intRange(1, MAX_CLUMP).fieldOf("size").forGetter(Entry::size),
                        Codec.intRange(1, 255).fieldOf("count").forGetter(Entry::count))
                    .apply(instance, Entry::new));
    public static final Codec<Entry> CODEC =
        UNCHECKED.validate(
            entry ->
                entry.minHeight >= entry.maxHeight
                    ? DataResult.error(() -> "Empty ore height range for " + entry.block)
                    : !BuiltInRegistries.BLOCK.containsKey(entry.block)
                        ? DataResult.error(() -> "Unknown ore block: " + entry.block)
                        : DataResult.success(entry));

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("block", block.toString());
      tag.putInt("min", minHeight);
      tag.putInt("max", maxHeight);
      tag.putInt("size", size);
      tag.putInt("count", count);
      return tag;
    }
  }

  public enum Pressure implements StringRepresentable {
    SUPER_HIGH,
    HIGH,
    NORMAL,
    LOW,
    NONE;

    public static final Codec<Pressure> CODEC = StringRepresentable.fromEnum(Pressure::values);

    @Override
    public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public enum Temperature implements StringRepresentable {
    TOO_HOT,
    HOT,
    NORMAL,
    COLD,
    FRIGID,
    SNOWBALL;

    public static final Codec<Temperature> CODEC =
        StringRepresentable.fromEnum(Temperature::values);

    @Override
    public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public record Profile(
      Optional<Pressure> pressure, Optional<Temperature> temperature, List<Entry> ores) {
    public static final Codec<Profile> CODEC =
        RecordCodecBuilder.<Profile>create(
                instance ->
                    instance
                        .group(
                            Pressure.CODEC.optionalFieldOf("pressure").forGetter(Profile::pressure),
                            Temperature.CODEC
                                .optionalFieldOf("temperature")
                                .forGetter(Profile::temperature),
                            Entry.CODEC.listOf().fieldOf("ores").forGetter(Profile::ores))
                        .apply(instance, Profile::new))
            .validate(
                profile ->
                    profile.pressure.isEmpty() && profile.temperature.isEmpty()
                        ? DataResult.error(() -> "Ore profile requires pressure or temperature")
                        : DataResult.success(profile));

    int specificity() {
      return (pressure.isPresent() ? 1 : 0) + (temperature.isPresent() ? 1 : 0);
    }

    Rule rule() {
      return new Rule(
          pressure.map(Enum::ordinal).orElse(-1),
          temperature.map(Enum::ordinal).orElse(-1),
          List.copyOf(ores));
    }
  }

  public record Rule(int pressure, int temperature, List<Entry> ores) {}

  private static final int MAX_CLUMP = 64;
  private static volatile List<Rule> rules = List.of();

  private PlanetOres() {}

  public static void starting(ServerAboutToStartEvent event) {
    rules = rules(event.getServer().registryAccess());
  }

  public static List<Rule> rules(RegistryAccess registries) {
    Map<ResourceLocation, Profile> profiles = new LinkedHashMap<>();
    registries
        .registry(DataRegistries.ORE_PROFILE)
        .ifPresent(
            registry ->
                registry
                    .entrySet()
                    .forEach(entry -> profiles.put(entry.getKey().location(), entry.getValue())));
    return rules(profiles);
  }

  public static List<Rule> rules(Map<ResourceLocation, Profile> profiles) {
    return profiles.entrySet().stream()
        .sorted(
            Comparator.<Map.Entry<ResourceLocation, Profile>>comparingInt(
                    entry -> entry.getValue().specificity())
                .thenComparing(Map.Entry::getKey))
        .map(entry -> entry.getValue().rule())
        .filter(rule -> !rule.ores().isEmpty())
        .toList();
  }

  public static List<Entry> select(Planet planet) {
    return select(planet, rules);
  }

  public static Holder<NoiseGeneratorSettings> noiseSettings(
      Holder<NoiseGeneratorSettings> holder, Planet planet) {
    NoiseGeneratorSettings base = holder.value();
    if (!base.oreVeinsEnabled() || select(planet).isEmpty()) return holder;
    return Holder.direct(
        new NoiseGeneratorSettings(
            base.noiseSettings(),
            base.defaultBlock(),
            base.defaultFluid(),
            base.noiseRouter(),
            base.surfaceRule(),
            base.spawnTarget(),
            base.seaLevel(),
            base.disableMobGeneration(),
            base.aquifersEnabled(),
            false,
            base.useLegacyRandomSource()));
  }

  public static List<Entry> select(Planet planet, List<Rule> configuration) {
    if (!planet.ores.isEmpty()) return planet.ores;
    int pressure =
        category(
            planet.originalAtmosphere == null ? planet.atmosphere : planet.originalAtmosphere,
            new int[] {800, 200, 75, 25, 0});
    int temperature = category(planet.temperature, new int[] {450, 325, 275, 250, 175, 0});
    List<Entry> selected = List.of();
    for (Rule rule : configuration)
      if ((rule.pressure < 0 || rule.pressure == pressure)
          && (rule.temperature < 0 || rule.temperature == temperature)) selected = rule.ores;
    return selected;
  }

  private static int category(int value, int[] thresholds) {
    for (int i = 0; i < thresholds.length; i++) if (value > thresholds[i]) return i;
    return thresholds.length - 1;
  }

  public static ListTag save(List<Entry> entries) {
    ListTag tag = new ListTag();
    entries.forEach(entry -> tag.add(entry.save()));
    return tag;
  }

  public static List<Entry> load(ListTag list) {
    List<Entry> entries = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      CompoundTag tag = list.getCompound(i);
      entries.add(
          new Entry(
              ResourceLocation.parse(tag.getString("block")),
              tag.getInt("min"),
              tag.getInt("max"),
              tag.getInt("size"),
              tag.getInt("count")));
    }
    return List.copyOf(entries);
  }

  public static Function<Holder<Biome>, BiomeGenerationSettings> settings(Planet planet) {
    var cache = new ConcurrentHashMap<Holder<Biome>, BiomeGenerationSettings>();
    return biome ->
        select(planet).isEmpty()
            ? biome.value().getGenerationSettings()
            : cache.computeIfAbsent(biome, key -> withoutOres(key.value().getGenerationSettings()));
  }

  public static BiomeGenerationSettings withoutOres(BiomeGenerationSettings original) {
    var result = new BiomeGenerationSettings.PlainBuilder();
    for (var stage : original.getCarvingStages())
      original.getCarvers(stage).forEach(carver -> result.addCarver(stage, carver));
    for (int step = 0; step < original.features().size(); step++)
      for (var feature : original.features().get(step)) {
        boolean ore =
            feature
                .value()
                .getFeatures()
                .anyMatch(
                    configured ->
                        configured.config() instanceof OreConfiguration config
                            && config.targetStates.stream()
                                .anyMatch(
                                    target ->
                                        BuiltInRegistries.BLOCK
                                            .getKey(target.state.getBlock())
                                            .getPath()
                                            .endsWith("_ore")));
        if (!ore) result.addFeature(step, feature);
      }
    return result.build();
  }

  public static boolean generate(
      WorldGenLevel level, ChunkAccess chunk, ChunkGenerator generator, Planet planet, long seed) {
    List<Entry> entries = select(planet);
    if (entries.isEmpty()) return false;
    RandomSource random =
        RandomSource.create(PlanetSeeds.column(seed ^ 0x51a7L, chunk.getPos().x, chunk.getPos().z));
    for (Entry entry : entries) {
      int min = Math.max(level.getMinBuildHeight(), entry.minHeight);
      int max = Math.min(level.getMaxBuildHeight(), entry.maxHeight);
      if (min >= max || !BuiltInRegistries.BLOCK.containsKey(entry.block)) continue;
      var state = BuiltInRegistries.BLOCK.get(entry.block).defaultBlockState();
      var config =
          new OreConfiguration(
              List.of(
                  OreConfiguration.target(
                      new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), state),
                  OreConfiguration.target(
                      new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), state),
                  OreConfiguration.target(
                      new BlockMatchTest(planet.fillerState().getBlock()), state)),
              Math.min(entry.size, MAX_CLUMP));
      for (int attempt = 0; attempt < entry.count; attempt++) {
        BlockPos pos =
            new BlockPos(
                chunk.getPos().getMinBlockX() + random.nextInt(16),
                min + random.nextInt(max - min),
                chunk.getPos().getMinBlockZ() + random.nextInt(16));
        Feature.ORE.place(config, level, generator, random, pos);
      }
    }
    return true;
  }
}

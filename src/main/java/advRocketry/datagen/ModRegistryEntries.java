package advRocketry.datagen;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.DataRegistries;
import advRocketry.Main;
import advRocketry.orbit.AsteroidCatalog;
import advRocketry.space.OreWorldgen;
import advRocketry.space.PlanetDefinition;
import advRocketry.space.StarDefinition;
import advRocketry.space.TerrainType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

final class ModRegistryEntries {
  private ModRegistryEntries() {}

  static RegistrySetBuilder builder() {
    return new RegistrySetBuilder()
        .add(Registries.DIMENSION_TYPE, ModRegistryEntries::dimensionTypes)
        .add(Registries.CONFIGURED_FEATURE, ModRegistryEntries::configuredFeatures)
        .add(Registries.PLACED_FEATURE, ModRegistryEntries::placedFeatures)
        .add(Registries.BIOME, ModRegistryEntries::biomes)
        .add(Registries.ENCHANTMENT, ModRegistryEntries::enchantments)
        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModRegistryEntries::biomeModifiers)
        .add(DataRegistries.ASTEROID, ModRegistryEntries::asteroids)
        .add(DataRegistries.STAR, ModRegistryEntries::stars)
        .add(DataRegistries.PLANET, ModRegistryEntries::planets)
        .add(DataRegistries.MULTIBLOCK, ModMultiblocks::bootstrap);
  }

  private static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String path) {
    return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath(Main.MODID, path));
  }

  private static void dimensionTypes(BootstrapContext<DimensionType> context) {
    context.register(
        key(Registries.DIMENSION_TYPE, "planet"), dimension(OptionalLong.empty(), "planet"));
    context.register(
        key(Registries.DIMENSION_TYPE, "space"), dimension(OptionalLong.of(18000), "space"));
  }

  private static DimensionType dimension(OptionalLong fixedTime, String effects) {
    return new DimensionType(
        fixedTime,
        true,
        false,
        false,
        false,
        1,
        true,
        false,
        -64,
        384,
        384,
        BlockTags.INFINIBURN_OVERWORLD,
        ResourceLocation.fromNamespaceAndPath(Main.MODID, effects),
        0,
        new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0));
  }

  private static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
    for (String name : OreWorldgen.NAMES) {
      Block ore =
          BuiltInRegistries.BLOCK.get(
              ResourceLocation.fromNamespaceAndPath(Main.MODID, name + "_ore"));
      context.register(
          key(Registries.CONFIGURED_FEATURE, name + "_ore"),
          new ConfiguredFeature<>(
              OreWorldgen.feature(name),
              new OreConfiguration(
                  List.of(
                      OreConfiguration.target(
                          new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                          ore.defaultBlockState()),
                      OreConfiguration.target(
                          new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                          ore.defaultBlockState())),
                  AdvancedRocketryConfig.oreClumpSize(name))));
    }
  }

  private static void placedFeatures(BootstrapContext<PlacedFeature> context) {
    var features = context.lookup(Registries.CONFIGURED_FEATURE);
    for (String name : OreWorldgen.NAMES)
      context.register(
          key(Registries.PLACED_FEATURE, name + "_ore"),
          new PlacedFeature(
              features.getOrThrow(key(Registries.CONFIGURED_FEATURE, name + "_ore")),
              List.of(
                  OreWorldgen.count(name),
                  InSquarePlacement.spread(),
                  HeightRangePlacement.uniform(
                      VerticalAnchor.absolute(0), VerticalAnchor.absolute(63)),
                  BiomeFilter.biome())));
  }

  private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
    var biomes = context.lookup(Registries.BIOME);
    var placed = context.lookup(Registries.PLACED_FEATURE);
    for (String name : OreWorldgen.NAMES)
      context.register(
          key(NeoForgeRegistries.Keys.BIOME_MODIFIERS, name + "_ore"),
          new BiomeModifiers.AddFeaturesBiomeModifier(
              biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
              HolderSet.direct(placed.getOrThrow(key(Registries.PLACED_FEATURE, name + "_ore"))),
              GenerationStep.Decoration.UNDERGROUND_ORES));
  }

  private record BiomeSpec(
      String name,
      boolean precipitation,
      float temperature,
      float downfall,
      int fog,
      int water,
      int sky,
      int grass,
      int foliage,
      float creatures) {}

  private static final List<BiomeSpec> BIOMES =
      List.of(
          new BiomeSpec(
              "alien_forest", true, .8f, .7f, 7907327, 8947967, 7907327, 7829503, 5636065, .1f),
          new BiomeSpec(
              "crystalchasms",
              true,
              .1f,
              .2f,
              10467583,
              4159204,
              10467583,
              10798573,
              10994670,
              .1f),
          new BiomeSpec(
              "deepswamp", true, .9f, .9f, 2109472, 4159204, 2109472, 6986584, 4614972, .1f),
          new BiomeSpec(
              "hotdryrock", false, .9f, 0, 13605992, 4159204, 13605992, 11701085, 9990730, .1f),
          new BiomeSpec("marsh", true, .8f, .9f, 7907327, 4159204, 7907327, 6719569, 5601092, .1f),
          new BiomeSpec("moon", false, .3f, 0, 7829367, 4159204, 7829367, 7829367, 7829367, .1f),
          new BiomeSpec(
              "moondark", false, .3f, 0, 5592405, 4159204, 5592405, 5592405, 5592405, .1f),
          new BiomeSpec(
              "oceanspires", true, .5f, .5f, 7907327, 4159204, 7907327, 7905380, 6522199, .1f),
          new BiomeSpec("space", false, 1, 0, 0, 4159204, 0, 0, 0, 0),
          new BiomeSpec(
              "stormland", true, .9f, .9f, 2105376, 4159204, 2105376, 2105376, 2105376, 0),
          new BiomeSpec("volcanic", false, 1, 0, 4469538, 4159204, 4469538, 7820346, 6110765, .1f),
          new BiomeSpec(
              "volcanicbarren", false, 1, 0, 4469538, 4159204, 4469538, 7820346, 6110765, .1f));

  private static void biomes(BootstrapContext<Biome> context) {
    var placed = context.lookup(Registries.PLACED_FEATURE);
    var carvers = context.lookup(Registries.CONFIGURED_CARVER);
    for (BiomeSpec spec : BIOMES)
      context.register(
          key(Registries.BIOME, spec.name()),
          new Biome.BiomeBuilder()
              .hasPrecipitation(spec.precipitation())
              .temperature(spec.temperature())
              .downfall(spec.downfall())
              .specialEffects(
                  new BiomeSpecialEffects.Builder()
                      .fogColor(spec.fog())
                      .waterColor(spec.water())
                      .waterFogColor(329011)
                      .skyColor(spec.sky())
                      .grassColorOverride(spec.grass())
                      .foliageColorOverride(spec.foliage())
                      .build())
              .mobSpawnSettings(
                  new MobSpawnSettings.Builder()
                      .creatureGenerationProbability(spec.creatures())
                      .build())
              .generationSettings(new BiomeGenerationSettings.Builder(placed, carvers).build())
              .build());
  }

  private static void enchantments(BootstrapContext<Enchantment> context) {
    var items = context.lookup(Registries.ITEM);
    var key = key(Registries.ENCHANTMENT, "spacebreathing");
    context.register(
        key,
        Enchantment.enchantment(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    1,
                    1,
                    Enchantment.constantCost(1),
                    Enchantment.constantCost(1),
                    1,
                    EquipmentSlotGroup.ARMOR))
            .build(key.location()));
  }

  private static AsteroidCatalog.Ore ore(String item, float weight) {
    return new AsteroidCatalog.Ore(ResourceLocation.parse(item), weight);
  }

  private static void asteroids(BootstrapContext<AsteroidCatalog.Definition> context) {
    ResourceLocation cobblestone = ResourceLocation.withDefaultNamespace("cobblestone");
    context.register(
        key(DataRegistries.ASTEROID, "small"),
        new AsteroidCatalog.Definition(
            10,
            200,
            .5f,
            .3f,
            .5f,
            20,
            1,
            cobblestone,
            List.of(
                ore("minecraft:iron_ore", 15),
                ore("minecraft:gold_ore", 10),
                ore("minecraft:redstone_ore", 10))));
    context.register(
        key(DataRegistries.ASTEROID, "light"),
        new AsteroidCatalog.Definition(
            60,
            200,
            .5f,
            .2f,
            .5f,
            15,
            1,
            cobblestone,
            List.of(
                ore("adv_rocketry:aluminum_ore", 20),
                ore("adv_rocketry:titanium_ore", 10),
                ore("minecraft:quartz_block", 5))));
    context.register(
        key(DataRegistries.ASTEROID, "iridium"),
        new AsteroidCatalog.Definition(
            100,
            75,
            .5f,
            .2f,
            .3f,
            2,
            1,
            cobblestone,
            List.of(ore("minecraft:iron_ore", 25), ore("adv_rocketry:iridium_ore", 5))));
    context.register(
        key(DataRegistries.ASTEROID, "strange"),
        new AsteroidCatalog.Definition(
            120,
            50,
            .5f,
            .2f,
            .5f,
            1,
            1,
            cobblestone,
            List.of(ore("adv_rocketry:dilithium_ore", 20), ore("minecraft:emerald_ore", 5))));
  }

  private static void star(
      BootstrapContext<StarDefinition> context,
      String key,
      String name,
      int temperature,
      int x,
      int z,
      int planets,
      int gasGiants) {
    context.register(
        key(DataRegistries.STAR, key),
        new StarDefinition(name, temperature, 1, x, z, false, List.of(), planets, gasGiants));
  }

  private static void stars(BootstrapContext<StarDefinition> context) {
    star(context, "sol", "Sol", 100, 0, 0, 9, 1);
    star(context, "alpha_centauri", "Alpha Centauri", 100, 300, -200, 5, 0);
    star(context, "bernards_star", "Bernards Star", 50, -200, 80, 7, 0);
    star(context, "proxima_centauri", "Proxima Centaurs", 200, -150, 250, 3, 0);
    star(context, "magnis_vulpes", "Magnis Vulpes", 70, -150, -250, 2, 0);
    star(context, "ma_roo", "Ma-Roo", 200, 50, -250, 6, 0);
    star(context, "alykitt", "Alykitt", 120, 75, 200, 3, 1);
  }

  private static void planets(BootstrapContext<PlanetDefinition> context) {
    ResourceLocation sol = ResourceLocation.fromNamespaceAndPath(Main.MODID, "sol");
    PlanetDefinition.Resources none =
        new PlanetDefinition.Resources(
            List.of(), List.of(), List.of(), true, List.of(), List.of(), List.of());
    context.register(
        key(DataRegistries.PLANET, "earth"),
        new PlanetDefinition(
            new PlanetDefinition.Identity(
                "Earth",
                sol,
                Optional.empty(),
                Optional.of(ResourceLocation.withDefaultNamespace("overworld")),
                true,
                ""),
            new PlanetDefinition.Physical(false, 100, true, Optional.of(286), 1, List.of()),
            new PlanetDefinition.Orbit(100, 0, 0, 24000, false),
            new PlanetDefinition.Sky(
                0xffffff, 0xc0d8ff, false, 0xffffff, false, false, Optional.empty()),
            new PlanetDefinition.Terrain(
                TerrainType.TERRESTRIAL,
                63,
                ResourceLocation.withDefaultNamespace("stone"),
                ResourceLocation.withDefaultNamespace("water"),
                true,
                true,
                false,
                List.of(),
                Map.of()),
            new PlanetDefinition.Features(false, 1, false, 1, false, 1),
            none));
    context.register(
        key(DataRegistries.PLANET, "luna"),
        new PlanetDefinition(
            new PlanetDefinition.Identity(
                "Luna",
                sol,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Main.MODID, "earth")),
                Optional.empty(),
                true,
                ""),
            new PlanetDefinition.Physical(false, 0, false, Optional.of(200), .166f, List.of()),
            new PlanetDefinition.Orbit(150, 0, 0, 128000, false),
            new PlanetDefinition.Sky(0, 0, false, 0xffffff, false, false, Optional.empty()),
            new PlanetDefinition.Terrain(
                TerrainType.MOON,
                63,
                ResourceLocation.withDefaultNamespace("stone"),
                ResourceLocation.withDefaultNamespace("water"),
                true,
                false,
                false,
                List.of(
                    new PlanetDefinition.WeightedBiome(
                        ResourceLocation.fromNamespaceAndPath(Main.MODID, "moon"),
                        Optional.empty()),
                    new PlanetDefinition.WeightedBiome(
                        ResourceLocation.fromNamespaceAndPath(Main.MODID, "moondark"),
                        Optional.empty())),
                Map.of()),
            new PlanetDefinition.Features(true, 1, false, 1, false, 1),
            none));
  }
}

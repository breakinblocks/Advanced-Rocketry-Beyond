package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import advRocketry.TestTags;
import advRocketry.processing.AlienSaplingBlock;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketRegistry;
import advRocketry.util.IdOrTag;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class PlanetTests {
  @GameTest(template = "empty")
  public static void unknownDimensionsDoNotInheritEarthSolarPower(GameTestHelper helper) {
    var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
    helper.assertTrue(nether != null, "Nether unavailable");
    helper.assertTrue(
        GalaxyData.get(nether.getServer()).find(nether) == null,
        "Test needs an unregistered dimension");
    helper.assertTrue(
        SolarPower.perPanel(nether, BlockPos.ZERO) == 0,
        "Unregistered dimension inherited Earth insolation");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void planetDayChangesPersistWithoutChangingOtherWorlds(GameTestHelper helper) {
    var world = helper.getLevel().getServer().getWorldData();
    long original = world.overworldData().getDayTime();
    var planet = new Planet(8101, "Local clock");
    planet.rotationPeriod = 48000;
    boolean[] changed = {false};
    var data = new PlanetLevelData(world, planet, () -> changed[0] = true);
    long morning = (data.getDayTime() / 24000 + 1) * 24000;
    data.setDayTime(morning);
    helper.assertTrue(data.getDayTime() == morning, "Sleep did not advance local time");
    helper.assertTrue(changed[0], "Clock change was not marked for saving");
    helper.assertTrue(world.overworldData().getDayTime() == original, "Changed overworld time");
    var loaded = Planet.load(planet.save());
    var restored = new PlanetLevelData(world, loaded, () -> {});
    helper.assertTrue(restored.getDayTime() == morning, "Local clock lost after reload");
    data.setDayTime(6000);
    helper.assertTrue(data.getDayTime() == 6000, "Explicit time change ignored");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalAtmospherePressureBoundaries(GameTestHelper helper) {
    Planet planet = new Planet(81, "Atmosphere classes");
    planet.temperature = 286;
    planet.oxygen = true;
    for (int density : new int[] {25, 26, 75, 76, 200, 201, 800, 801}) {
      planet.atmosphere = density;
      boolean expectedBreathable = density >= 76 && density <= 200;
      helper.assertTrue(
          planet.breathable() == expectedBreathable,
          "Incorrect breathability at atmosphere density " + density);
    }
    planet.atmosphere = 100;
    planet.temperature = 175;
    helper.assertTrue(
        planet.breathable(), "A cold planet with normal air was treated as an airless world");
    planet.temperature = 451;
    helper.assertTrue(!planet.breathable(), "Very hot air was treated as breathable");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void gasGiantDefaultsFollowGravityRanges(GameTestHelper helper) {
    List<ResourceLocation> heavy = GasGiantGases.select(1.5f, new Random(7));
    List<ResourceLocation> light = GasGiantGases.select(1f, new Random(7));
    Planet giant = new Planet(123, "Gas Test");
    giant.gases.add(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "hydrogen"));
    helper.assertTrue(
        GasGiantGases.harvestable(giant).size() == 1, "Empty harvestable_gases tag added gases");
    try (var tag =
        TestTags.add(
            BuiltInRegistries.FLUID,
            ModTags.HARVESTABLE_GASES,
            ProcessingFluids.DEFINITIONS.get("methane").source.get().builtInRegistryHolder(),
            Fluids.WATER.builtInRegistryHolder(),
            ProcessingFluids.DEFINITIONS.get("hydrogen").source.get().builtInRegistryHolder())) {
      List<ResourceLocation> harvestable = GasGiantGases.harvestable(giant);
      helper.assertTrue(
          harvestable.size() == 3
              && harvestable.get(1).getPath().equals("methane")
              && harvestable.get(2).equals(ResourceLocation.withDefaultNamespace("water")),
          "harvestable_gases tag did not add fluids without duplicates");
    }
    helper.assertTrue(
        heavy.contains(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "hydrogen"))
            && !heavy.contains(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "nitrogen")),
        "High-gravity giants did not use the original gas range");
    helper.assertTrue(
        light.contains(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "oxygen"))
            && light.contains(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "nitrogen")),
        "Low-gravity giants did not include oxygen and nitrogen");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void terraformerChangesLoadedBiomeColumn(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    var desert =
        level
            .registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(
                ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:desert")));
    BlockPos origin = helper.absolutePos(BlockPos.ZERO);
    int x = Math.floorDiv(origin.getX(), 4) * 4;
    int z = Math.floorDiv(origin.getZ(), 4) * 4;
    BlockPos column = new BlockPos(x, level.getMinBuildHeight(), z);
    level.getChunkAt(column);
    TerraformBiomeLogic.changeCell(level, new PlanetBiomeSource(List.of(desert), 123L), x, z);
    helper.assertTrue(
        level
            .getChunkAt(column)
            .getNoiseBiome(x >> 2, level.getMinBuildHeight() >> 2, z >> 2)
            .is(desert),
        "Terraforming did not rewrite the loaded chunk's biome column");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void terraformedBiomeTargetsSurviveReload(GameTestHelper helper) {
    Planet planet = new Planet(72, "Terraforming test");
    planet.temperature = 286;
    planet.atmosphere = 25;
    TerraformBiomeLogic.changeAtmosphere(planet, 26, 12345L);
    helper.assertTrue(
        !planet.terraformedBiomes.isEmpty(),
        "Crossing the low-pressure boundary did not select terraformed biomes");
    var selected = List.copyOf(planet.terraformedBiomes);
    TerraformBiomeLogic.changeAtmosphere(planet, 27, 12345L);
    helper.assertTrue(
        planet.terraformedBiomes.equals(selected),
        "Biome targets rerolled inside one atmosphere class");
    Planet reloaded = Planet.load(planet.save());
    helper.assertTrue(
        reloaded.terraformedBiomes.equals(selected),
        "Terraforming biome targets did not survive save and reload");
    TerraformBiomeLogic.changeAtmosphere(reloaded, 25, 12345L);
    helper.assertTrue(
        reloaded.terraformedBiomes.contains(
            ResourceLocation.fromNamespaceAndPath("adv_rocketry", "moon")),
        "Returning to an airless atmosphere did not restore regolith biomes");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void planetStateAndTerrain(GameTestHelper helper) {
    ServerLevel overworld = helper.getLevel();
    var server = overworld.getServer();
    GalaxyData galaxy = GalaxyData.get(server);
    helper.assertTrue(
        AdvancedRocketryConfig.orePerChunk("tin", false) == 10
            && AdvancedRocketryConfig.orePerChunk("copper", true) == 10
            && AdvancedRocketryConfig.oreClumpSize("tin") == 6
            && AdvancedRocketryConfig.orePerChunk("dilithium", true) == 10
            && AdvancedRocketryConfig.orePerChunk("iridium", false) == 0,
        "Original ore generation defaults changed");
    for (String ore : new String[] {"tin", "aluminum", "titanium", "dilithium", "iridium"}) {
      var id = ResourceLocation.fromNamespaceAndPath("adv_rocketry", ore + "_ore");
      helper.assertTrue(
          BuiltInRegistries.FEATURE.containsKey(id),
          "Configurable overworld ore feature did not register: " + ore);
      var placed = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).get(id);
      helper.assertTrue(
          placed != null
              && placed.feature().value().feature() instanceof OreWorldgen.ConfigurableOre
              && placed.placement().getFirst() instanceof OreWorldgen.ConfigurableCount,
          "Overworld ore feature does not use its configurable count and size: " + ore);
    }
    GalaxyData restored =
        GalaxyData.load(
            galaxy.save(new CompoundTag(), server.registryAccess()), server.registryAccess());
    helper.assertTrue(
        restored.planets.size() == galaxy.planets.size(),
        "Planet state did not survive serialization");
    boolean reachedMoon = galaxy.reachedMoon, reachedWarp = galaxy.reachedWarp;
    galaxy.reachedMoon = true;
    galaxy.reachedWarp = true;
    GalaxyData milestones =
        GalaxyData.load(
            galaxy.save(new CompoundTag(), server.registryAccess()), server.registryAccess());
    helper.assertTrue(
        milestones.reachedMoon && milestones.reachedWarp,
        "First-flight progression was not persisted");
    galaxy.reachedMoon = reachedMoon;
    galaxy.reachedWarp = reachedWarp;
    galaxy.setDirty();
    Planet moon = restored.planets.get(1);
    helper.assertTrue(
        PlanetDecorations.geodeOrePalette(moon).size() == 5,
        "The original geode ore palette is incomplete");
    moon.geodeOres.add(IdOrTag.of("adv_rocketry:dilithium_ore"));
    helper.assertTrue(
        PlanetDecorations.geodeOrePalette(moon).stream()
            .anyMatch(state -> state.is(ProcessingRegistry.part("dilithium_ore"))),
        "Planet-specific geode ore was ignored");
    helper.assertTrue(
        moon.parent == 0 && moon.atmosphere == 0 && !moon.breathable(), "Luna properties changed");
    Planet airless = new Planet(1001, "Biome Rules Test");
    airless.atmosphere = 0;
    GalaxyData.selectBiomes(airless, new Random(1));
    helper.assertTrue(
        airless.biomes.contains(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "moon"))
            && airless.biomes.contains(
                ResourceLocation.fromNamespaceAndPath("adv_rocketry", "moondark")),
        "Airless planets did not receive both original regolith biomes");
    boolean foundHighPressure = false;
    for (int seed = 0; seed < 20; seed++) {
      Planet dense = new Planet(1100 + seed, "Dense Biome Rules Test");
      dense.atmosphere = 250;
      dense.temperature = 300;
      GalaxyData.selectBiomes(dense, new Random(seed));
      if (dense.biomes.contains(
          ResourceLocation.fromNamespaceAndPath("adv_rocketry", "stormland"))) {
        foundHighPressure =
            dense.biomes.contains(
                ResourceLocation.fromNamespaceAndPath("adv_rocketry", "deepswamp"));
        break;
      }
    }
    helper.assertTrue(
        foundHighPressure,
        "High-pressure planets never received the original stormland and deep swamp biomes");
    ServerLevel moonLevel = PlanetRuntime.create(server, moon);
    helper.assertTrue(
        moonLevel.dimensionType().bedWorks(),
        "Planetary beds must allow the atmosphere-aware sleep event to decide safety");
    helper.assertTrue(
        !PlanetRespawn.allowed(moonLevel, moon, new BlockPos(0, 80, 0)),
        "Default settings should not allow respawning in lunar vacuum");
    helper.assertTrue(
        PlanetRuntime.create(server, moon) == moonLevel,
        "Creating a planet twice replaced its level");
    var generator = moonLevel.getChunkSource().getGenerator();
    var ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
    var encoded = ChunkGenerator.CODEC.encodeStart(ops, generator).getOrThrow();
    helper.assertTrue(
        ChunkGenerator.CODEC.parse(ops, encoded).getOrThrow() instanceof PlanetTerrain,
        "Planet generator codec round trip failed");
    var moonChunk = moonLevel.getChunk(0, 0);
    helper.assertTrue(
        moonChunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8) > moonLevel.getMinBuildHeight(),
        "Moon terrain is empty");
    ServerLevel space = PlanetRuntime.create(server, restored.planets.get(GalaxyData.SPACE_ID));
    var spaceChunk = space.getChunk(0, 0);
    helper.assertTrue(spaceChunk.getSections().length > 0, "Space chunk has no sections");
    for (var section : spaceChunk.getSections())
      helper.assertTrue(section.hasOnlyAir(), "Space generated terrain");
    helper.assertTrue(
        server
            .registryAccess()
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .containsKey(space.dimensionTypeRegistration().unwrapKey().orElseThrow().location()),
        "Space dimension type was not registered");
    for (String biome :
        new String[] {
          "moon", "moondark", "alien_forest", "hotdryrock", "space", "stormland",
          "crystalchasms", "deepswamp", "marsh", "oceanspires", "volcanic", "volcanicbarren"
        })
      helper.assertTrue(
          server
              .registryAccess()
              .registryOrThrow(Registries.BIOME)
              .containsKey(ResourceLocation.fromNamespaceAndPath("adv_rocketry", biome)),
          "Original biome is missing: " + biome);
    Planet forest = new Planet(997, "Alien Forest Test");
    forest.biomes.add(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "alien_forest"));
    ServerLevel forestLevel = PlanetRuntime.create(server, forest);
    var forestChunk = forestLevel.getChunk(0, 0);
    var forestKey =
        ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("adv_rocketry", "alien_forest"));
    helper.assertTrue(
        forestChunk.getNoiseBiome(2, 16, 2).is(forestKey),
        "Alien forest did not survive planet generation");
    BlockPos saplingPos = new BlockPos(8, 200, 8);
    forestLevel.setBlock(saplingPos.below(), Blocks.DIRT.defaultBlockState(), 3);
    AlienSaplingBlock sapling = (AlienSaplingBlock) ProcessingRegistry.part("alien_sapling");
    forestLevel.setBlock(saplingPos, sapling.defaultBlockState(), 3);
    sapling.performBonemeal(
        forestLevel, RandomSource.create(42), saplingPos, forestLevel.getBlockState(saplingPos));
    helper.assertTrue(
        forestLevel.getBlockState(saplingPos).getValue(AlienSaplingBlock.STAGE) == 1,
        "Alien sapling skipped its first growth stage");
    sapling.performBonemeal(
        forestLevel, RandomSource.create(42), saplingPos, forestLevel.getBlockState(saplingPos));
    helper.assertTrue(
        forestLevel.getBlockState(saplingPos).is(ProcessingRegistry.part("alien_log")),
        "Alien sapling did not grow a lightwood trunk");
    helper.assertTrue(
        forestLevel.getBlockState(saplingPos.above(10)).hasProperty(RotatedPillarBlock.AXIS),
        "Alien trunk lost its log axis state");
    helper.assertTrue(
        ProcessingRegistry.part("alien_planks").defaultBlockState().getLightEmission() == 15,
        "Original lightwood planks lost their light emission");
    helper.assertTrue(
        ProcessingRegistry.part("alien_leaves").defaultBlockState().getLightEmission() == 8,
        "Original lightwood leaves lost their light emission");
    Planet spires = new Planet(998, "Ocean Spires Test");
    spires.biomes.add(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "oceanspires"));
    ServerLevel spireLevel = PlanetRuntime.create(server, spires);
    var spireChunk = spireLevel.getChunk(0, 0);
    var spireKey =
        ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("adv_rocketry", "oceanspires"));
    helper.assertTrue(
        spireChunk.getNoiseBiome(2, 16, 2).is(spireKey),
        "Ocean spires biome did not survive planet generation");
    Planet marsh = new Planet(999, "Marsh Test");
    marsh.biomes.add(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "marsh"));
    ServerLevel marshLevel = PlanetRuntime.create(server, marsh);
    helper.assertTrue(
        marshLevel
            .getChunk(0, 0)
            .getNoiseBiome(2, 16, 2)
            .is(
                ResourceKey.create(
                    Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath("adv_rocketry", "marsh"))),
        "Marsh biome did not survive planet generation");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void originalMoonLandingSite(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    for (String milestone :
        new String[] {
          "root",
          "beer",
          "blockpresser",
          "crystalline",
          "dilithium",
          "electrifying",
          "feeltheheat",
          "flightofpheonix",
          "givingitallshesgot",
          "holographic",
          "moonlanding",
          "onesmallstep",
          "rollin",
          "spindoctor",
          "suitedup",
          "warp",
          "wenttothemoon"
        })
      helper.assertTrue(
          server
                  .getAdvancements()
                  .get(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "normal/" + milestone))
              != null,
          "Original advancement did not load: " + milestone);
    var moon = PlanetRuntime.create(server, GalaxyData.get(server).planets.get(1));
    moon.getChunk(146, 4);
    BlockPos site =
        new BlockPos(2347, moon.getHeight(Heightmap.Types.WORLD_SURFACE, 2347, 67) - 4, 67);
    boolean foundMotor = false;
    for (int y = site.getY(); y < site.getY() + 8; y++)
      if (moon.getBlockState(new BlockPos(2347, y, 67))
          .is(RocketRegistry.PARTS.get("rocket_motor").get())) foundMotor = true;
    helper.assertTrue(foundMotor, "Original Luna lander motor was not generated");
    helper.succeed();
  }
}

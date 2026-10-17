package advRocketry.space;

import advRocketry.processing.ProcessingRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class CraterTests {
  @GameTest(template = "empty")
  public static void crystalChasmsEligibilityHeightAndFoundations(GameTestHelper helper) {
    var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
    var crystal =
        registry.getHolderOrThrow(
            ResourceKey.create(
                Registries.BIOME, ResourceLocation.parse("adv_rocketry:crystalchasms")));
    var plains =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    long seed = 0;
    for (; seed < 10000; seed++) {
      Random random = new Random(seed);
      if (random.nextInt(6) == 0 && random.nextInt(6) == 0) break;
    }
    ProtoChunk allowed = flat(helper, false);
    PlanetCrystals.generate(allowed, seed, (x, z) -> crystal);
    int crystals = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 81; y < 131; y++)
          if (BuiltInRegistries.BLOCK
              .getKey(allowed.getBlockState(new BlockPos(x, y, z)).getBlock())
              .getPath()
              .endsWith("_crystal_block")) crystals++;
    helper.assertTrue(
        crystals > 0 && allowed.getBlockState(new BlockPos(0, 80, 0)).is(Blocks.PACKED_ICE),
        "Crystal Chasms lost their tall crystals or original packed-ice foundation at height 80");
    for (boolean geodes : List.of(false, true)) {
      Planet planet = new Planet(1215, "No crystals");
      planet.terrain = TerrainType.MOON;
      planet.geodes = geodes;
      planet.geodeFrequency = 1000000;
      ProtoChunk blocked = flat(helper, false);
      PlanetDecorations.generate(blocked, planet, seed, (x, z) -> plains);
      for (int x = 0; x < 16; x++)
        for (int z = 0; z < 16; z++)
          for (int y = 81; y < 131; y++)
            helper.assertTrue(
                !BuiltInRegistries.BLOCK
                    .getKey(blocked.getBlockState(new BlockPos(x, y, z)).getBlock())
                    .getPath()
                    .endsWith("_crystal_block"),
                "Ordinary moon/geode terrain acquired biome-only crystals");
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void geodesRespectBiomeExclusionsAndFrequency(GameTestHelper helper) {
    var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
    Planet planet = new Planet(1003, "Geode frequencies");
    planet.geodes = true;
    planet.geodeFrequency = .001f;
    for (String name : List.of("ocean", "river", "beach")) {
      var biome =
          registry.getHolderOrThrow(
              ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace(name)));
      ProtoChunk chunk = flat(helper, false);
      PlanetDecorations.generate(chunk, planet, 73L, (x, z) -> biome);
      helper.assertTrue(shellCount(chunk) == 0, "Geode generated in excluded biome " + name);
    }
    var plains =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    ProtoChunk common = flat(helper, false);
    PlanetDecorations.generate(common, planet, 73L, (x, z) -> plains);
    helper.assertTrue(shellCount(common) > 0, "Eligible land biome did not generate geodes");
    planet.geodeFrequency = 1000000;
    ProtoChunk rare = flat(helper, false);
    PlanetDecorations.generate(rare, planet, 73L, (x, z) -> plains);
    helper.assertTrue(
        shellCount(rare) == 0, "Larger original frequency multiplier made geodes more common");
    helper.succeed();
  }

  private static int shellCount(ProtoChunk chunk) {
    int count = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (chunk.getBlockState(new BlockPos(x, y, z)).is(ProcessingRegistry.part("geode")))
            count++;
    return count;
  }

  @GameTest(template = "empty")
  public static void craterSizesFluidsOresAndCentralPeaks(GameTestHelper helper) {
    int[] radii = {4, 24, 100};
    int[] floors = {62, 52, 36};
    for (int kind = 0; kind < 3; kind++) {
      ProtoChunk chunk = flat(helper, false);
      PlanetCraters.paint(chunk, 8, 8, radii[kind], 3, new int[5], kind, false, List.of(), 19L);
      helper.assertTrue(
          chunk.getBlockState(new BlockPos(8, floors[kind], 8)).is(Blocks.STONE)
              && chunk.getBlockState(new BlockPos(8, floors[kind] + 1, 8)).isAir(),
          "Incorrect original crater excavation depth for size class " + kind);
    }
    ProtoChunk wet = flat(helper, true);
    PlanetCraters.paint(
        wet,
        8,
        8,
        24,
        3,
        new int[5],
        1,
        false,
        List.of(Blocks.DIAMOND_ORE.defaultBlockState()),
        19L);
    helper.assertTrue(
        wet.getBlockState(new BlockPos(8, 60, 8)).is(Blocks.WATER)
            && wet.getBlockState(new BlockPos(8, 70, 8)).is(Blocks.WATER),
        "Crater excavated an air pocket under the configured ocean surface");
    int ores = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 30; y <= 70; y++)
          if (wet.getBlockState(new BlockPos(x, y, z)).is(Blocks.DIAMOND_ORE)) ores++;
    helper.assertTrue(ores > 0, "Crater ore palette did not place any deposits");
    ProtoChunk peak = flat(helper, false);
    PlanetCraters.paint(
        peak,
        8,
        8,
        100,
        3,
        new int[5],
        2,
        true,
        List.of(Blocks.DIAMOND_ORE.defaultBlockState()),
        19L);
    helper.assertTrue(
        !peak.getBlockState(new BlockPos(8, 42, 8)).isAir(),
        "Huge crater central peak was omitted");
    helper.succeed();
  }

  private static ProtoChunk flat(GameTestHelper helper, boolean water) {
    var level = helper.getLevel();
    var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
    var plains =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    var chunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, level, registry, null);
    chunk.fillBiomesFromNoise((x, y, z, sampler) -> plains, null);
    chunk.setPersistedStatus(ChunkStatus.BIOMES);
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= (water ? 70 : 64); y++)
          chunk.setBlockState(
              new BlockPos(x, y, z),
              (y > 64 ? Blocks.WATER : Blocks.STONE).defaultBlockState(),
              false);
    return chunk;
  }

  @GameTest(template = "empty")
  public static void craterBiomeFrequencyControlsActualExcavation(GameTestHelper helper) {
    var level = helper.getLevel();
    var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
    var plainsId = ResourceLocation.withDefaultNamespace("plains");
    var plains = registry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, plainsId));
    var desert =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("desert")));
    var chunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, level, registry, null);
    chunk.fillBiomesFromNoise((x, y, z, sampler) -> plains, null);
    chunk.setPersistedStatus(ChunkStatus.BIOMES);
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          chunk.setBlockState(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), false);
    Planet planet = new Planet(1002, "Crater weights");
    planet.craters = true;
    planet.craterFrequency = .001f;
    planet.craterBiomeWeights.put(plainsId, 0);
    var centers = new HashSet<BlockPos>();
    PlanetDecorations.generate(
        chunk,
        planet,
        37L,
        (x, z) -> {
          centers.add(new BlockPos(x, 0, z));
          return plains;
        });
    helper.assertTrue(unchanged(chunk), "A zero-frequency biome still received crater excavation");
    helper.assertTrue(
        centers.stream().anyMatch(pos -> pos.getX() < 0 || pos.getZ() < 0),
        "Crater eligibility did not sample neighboring crater centers");
    planet.craterBiomeWeights.put(plainsId, 100);
    PlanetDecorations.generate(chunk, planet, 37L, (x, z) -> desert);
    helper.assertTrue(
        unchanged(chunk), "An unlisted biome received craters despite the explicit allowlist");
    PlanetDecorations.generate(chunk, Planet.load(planet.save()), 37L, (x, z) -> plains);
    helper.assertTrue(
        !unchanged(chunk), "An allowed crater biome did not excavate terrain after save/reload");
    helper.succeed();
  }

  private static boolean unchanged(ProtoChunk chunk) {
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        if (!chunk.getBlockState(new BlockPos(x, 64, z)).is(Blocks.STONE)) return false;
    return true;
  }
}

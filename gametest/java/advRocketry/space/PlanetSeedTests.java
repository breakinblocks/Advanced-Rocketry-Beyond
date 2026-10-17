package advRocketry.space;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class PlanetSeedTests {
  @GameTest(template = "empty")
  public static void planetSeedReachesNativeNoiseAndClimate(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var planet = new Planet(2978, "Independent terrain seed");
    planet.biomes.add(ResourceLocation.parse("minecraft:plains"));
    galaxy.planets.put(planet.id, planet);
    galaxy.setDirty();
    var level = PlanetRuntime.create(server, planet);
    long base = server.getWorldData().worldGenOptions().seed();
    long seed = base + planet.id * 341873128712L;
    helper.assertTrue(level.getSeed() == seed, "Planet returned the shared Overworld terrain seed");
    var generator = (PlanetTerrain) level.getChunkSource().getGenerator();
    var noises = level.registryAccess().lookupOrThrow(Registries.NOISE);
    var expected = RandomState.create(generator.generatorSettings().value(), noises, seed);
    var shared = RandomState.create(generator.generatorSettings().value(), noises, base);
    var actual = level.getChunkSource().randomState();
    boolean distinct = false;
    for (int x = -64; x <= 64; x += 32) {
      var sample = actual.sampler().sample(x, 16, 41);
      helper.assertTrue(
          sample.equals(expected.sampler().sample(x, 16, 41)),
          "Native chunk noise initialized before the planet seed became available");
      distinct |= !sample.equals(shared.sampler().sample(x, 16, 41));
    }
    helper.assertTrue(distinct, "Planet climate/noise still duplicates the Overworld");
    helper.succeed();
  }
}

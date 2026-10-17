package advRocketry.space;

import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class BiomeSelectionTests {
  private static ResourceLocation id(String path) {
    return ResourceLocation.parse(path);
  }

  private static PlanetBiomeCatalog.Entry entry(String name, float temperature) {
    return new PlanetBiomeCatalog.Entry(
        id(name), temperature, false, temperature > 1, temperature < .2f);
  }

  private static final List<ResourceLocation> AIRLESS =
      List.of(id("adv_rocketry:moon"), id("adv_rocketry:moondark"));
  private static final List<ResourceLocation> SCORCHED =
      List.of(
          id("adv_rocketry:hotdryrock"),
          id("adv_rocketry:volcanic"),
          id("adv_rocketry:volcanicbarren"));

  private static final List<PlanetBiomeCatalog.Entry> BIOMES =
      List.of(
          entry("minecraft:plains", .8f),
          entry("other_mod:forest", .7f),
          entry("other_mod:pressure", .8f),
          entry("other_mod:snow", .1f),
          entry("other_mod:desert", 2f));

  @GameTest(template = "empty")
  public static void installedBiomesAndPressurePrecedence(GameTestHelper helper) {
    Planet planet = new Planet(1298, "Biome policy");
    var policy =
        new PlanetBiomeCatalog.Policy(
            Set.of(id("other_mod:pressure")),
            true,
            List.of(id("other_mod:pressure")),
            List.of(),
            AIRLESS,
            SCORCHED,
            -1);
    var normal = PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, policy);
    helper.assertTrue(
        normal.contains(id("other_mod:forest"))
            && normal.stream().noneMatch(biome -> biome.getNamespace().equals("minecraft"))
            && !normal.contains(id("other_mod:pressure"))
            && !normal.contains(id("other_mod:snow")),
        "Normal atmosphere ignored modded candidates, vanilla blacklist or cold exclusion");
    planet.atmosphere = 201;
    var high = PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, policy);
    helper.assertTrue(
        high.contains(id("other_mod:pressure")), "Pressure list did not override blacklist");
    planet.temperature = 200;
    helper.assertTrue(
        PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, policy)
            .equals(List.of(id("other_mod:snow"))),
        "Cold climate or pressure temperature gate wrong");
    planet.atmosphere = 25;
    helper.assertTrue(
        PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, policy)
            .equals(List.of(id("adv_rocketry:moon"), id("adv_rocketry:moondark"))),
        "Airless palette lost");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void singleBiomeBlacklistAndEmptyClimate(GameTestHelper helper) {
    Planet planet = new Planet(1299, "Single policy");
    var policy =
        new PlanetBiomeCatalog.Policy(
            Set.of(id("other_mod:pressure")),
            false,
            List.of(),
            List.of(id("other_mod:forest"), id("other_mod:pressure")),
            AIRLESS,
            SCORCHED,
            1);
    for (int seed = 0; seed < 50; seed++) {
      var selection = PlanetBiomeCatalog.select(planet, new Random(seed), BIOMES, policy);
      helper.assertTrue(
          selection.size() == 1 && !selection.contains(id("other_mod:pressure")),
          "Single biome bypassed blacklist or limit");
    }
    var none =
        new PlanetBiomeCatalog.Policy(
            Set.of(
                id("other_mod:forest"),
                id("other_mod:pressure"),
                id("other_mod:snow"),
                id("other_mod:desert")),
            true,
            List.of(),
            List.of(),
            AIRLESS,
            SCORCHED,
            -1);
    try {
      PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, none);
      throw new AssertionError("Empty candidates accepted");
    } catch (IllegalStateException expected) {
    }
    planet.temperature = 451;
    helper.assertTrue(
        PlanetBiomeCatalog.select(planet, new Random(1), BIOMES, none).size() == 3,
        "Original extreme-heat palette lost");
    helper.succeed();
  }
}

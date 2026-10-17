package advRocketry.space;

import advRocketry.orbit.WarpLogic;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class OreTests {
  @GameTest(template = "empty")
  public static void originalOrbitalPeriodsAndWarpCosts(GameTestHelper helper) {
    Star star = new Star(18, "Orbit star", 100, 1, 0, 0, false);
    Planet planet = new Planet(1212, "Orbit");
    planet.star = 18;
    helper.assertTrue(
        Math.abs(planet.orbitalAngle(24000 * 12, star, null) - Math.PI / 2) < 1e-9,
        "Earth-distance planet did not complete a quarter of its original 48-day year");
    Planet moon = new Planet(1213, "Moon");
    moon.star = 18;
    moon.parent = planet.id;
    moon.orbitalTheta = .3;
    helper.assertTrue(
        Math.abs(moon.orbitalAngle(24000 * 2, star, planet) - Math.PI / 2 - .3) < 1e-9,
        "Lunar period or configured base angle changed");
    moon.retrograde = true;
    helper.assertTrue(
        Math.abs(moon.orbitalAngle(24000 * 2, star, planet) + Math.PI / 2 + .3) < 1e-9,
        "Retrograde did not reverse orbital phase and base angle");
    Planet other = new Planet(1214, "Outer orbit");
    other.star = 18;
    other.orbitalDistance = 200;
    GalaxyData galaxy = new GalaxyData();
    galaxy.stars.put(star.id(), star);
    galaxy.planets.put(planet.id, planet);
    galaxy.planets.put(moon.id, moon);
    galaxy.planets.put(other.id, other);
    helper.assertTrue(
        WarpLogic.cost(planet, other, galaxy, 0) == 100
            && WarpLogic.cost(planet, other, galaxy, 24000 * 12) > 100,
        "Warp cost ignored moving orbital positions");
    helper.assertTrue(
        WarpLogic.cost(planet, moon, galaxy, 24000 * 12) == 1,
        "Moving moons lost the original parent-child warp discount");
    helper.assertTrue(
        Planet.load(moon.save()).orbitalAngle(24000 * 2, star, planet)
            == moon.orbitalAngle(24000 * 2, star, planet),
        "Reload changed orbital position");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void planetIconsAndExplicitShading(GameTestHelper helper) {
    Planet planet = new Planet(1211, "Icon rules");
    helper.assertTrue(
        planet.icon().getPath().endsWith("earthlike.png"),
        "Temperate world lost its original icon");
    planet.atmosphere = 0;
    planet.temperature = 250;
    helper.assertTrue(
        planet.icon().getPath().endsWith("moon.png"), "Airless cold world has wrong icon");
    planet.atmosphere = 100;
    helper.assertTrue(
        planet.icon().getPath().endsWith("iceworld.png"), "Cold atmosphere world has wrong icon");
    planet.temperature = 451;
    helper.assertTrue(planet.icon().getPath().endsWith("marslike.png"), "Hot world has wrong icon");
    planet.gasGiant = true;
    helper.assertTrue(
        planet.icon().getPath().endsWith("gasgiantblue.png"), "Gas giant icon lost precedence");
    planet.gasGiant = false;
    planet.terrain = TerrainType.ASTEROID;
    helper.assertTrue(
        !planet.hasShading() && planet.icon().getPath().endsWith("asteroid.png"),
        "Asteroid decoration default changed");
    planet.shading = true;
    helper.assertTrue(
        Planet.load(planet.save()).hasShading(), "Explicit asteroid shading was lost");
    GalaxyData galaxy =
        galaxy(
            Map.of("test:star", "{\"name\": \"Test\"}"),
            Map.of(
                "test:icon",
                """
                {"name": "Icon", "star": "test:star", "icon": "MyPlanet", "shading": false,
                 "rings": true}
                """),
            1L);
    Planet loaded = Planet.load(galaxy.planets.get(1).save());
    helper.assertTrue(
        !loaded.hasShading()
            && loaded.rings
            && loaded.icon().toString().equals("adv_rocketry:textures/planets/myplanet.png"),
        "Custom icon, rings or explicit disabled shading lost through JSON/save/reload");
    loaded.customIcon = "example:textures/celestial/planet.png";
    helper.assertTrue(
        loaded.icon().getNamespace().equals("example"),
        "Namespaced resource-pack texture was rewritten");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void companionStarsPersistenceAndDefaults(GameTestHelper helper) {
    GalaxyData galaxy =
        galaxy(
            Map.of(
                "test:binary",
                """
                {"name": "Binary", "temperature": 100, "size": 2, "companions": [
                  {"temperature": 20, "size": 0.5, "separation": 12},
                  {"name": "Hole", "temperature": 140, "black_hole": true}]}
                """),
            Map.of("test:orbiting", "{\"name\": \"Orbiting\", \"star\": \"test:binary\"}"),
            42L);
    Star star = galaxy.stars.get(galaxy.starDefinitions.get(ResourceLocation.parse("test:binary")));
    Star restored = Star.load(star.save());
    helper.assertTrue(
        star.equals(restored), "Companion data lost during save or network serialization");
    helper.assertTrue(
        galaxy.stars.size() == 1 && galaxy.planets.get(1).star == star.id(),
        "Companions became independent systems or changed the planet's primary star");
    helper.assertTrue(
        star.companions().size() == 2
            && star.companions()
                .getFirst()
                .equals(new Star.Companion("Binary-1", 20, .5f, false, 12))
            && star.companions().getLast().equals(new Star.Companion("Hole", 140, 1, true, 5)),
        "Companion attributes or default name/separation changed");
    var old = star.save();
    old.remove("companions");
    helper.assertTrue(Star.load(old).companions().isEmpty(), "Single-star saves no longer load");
    for (int temperature : new int[] {0, 20, 100, 200, 10000})
      for (float channel : Star.color(temperature))
        helper.assertTrue(
            Float.isFinite(channel) && channel >= 0 && channel <= 1,
            "Stellar color escaped the native shader's channel range");
    helper.assertTrue(
        StarDefinition.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"name\": \"Bad\", \"companions\": [{\"separation\": -1}]}"))
            .isError(),
        "Invalid star geometry was accepted");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void planetClimateDefaultsAndExplicitOverrides(GameTestHelper helper) {
    GalaxyData galaxy =
        galaxy(
            Map.of("test:cold", "{\"name\": \"Cold Star\", \"temperature\": 50}"),
            Map.of(
                "test:parent",
                """
                {"name": "Parent", "star": "test:cold", "orbital_distance": 200,
                 "atmosphere": 100}
                """,
                "test:moon",
                """
                {"name": "Moon", "star": "test:cold", "parent": "test:parent",
                 "orbital_distance": 10}
                """,
                "test:explicit",
                """
                {"name": "Explicit", "star": "test:cold", "parent": "test:parent",
                 "temperature": 400, "caves": true, "structures": true,
                 "biomes": [{"biome": "minecraft:desert", "weight": 1}]}
                """),
            42L);
    Planet parent = byDefinition(galaxy, "test:parent");
    Planet moon = byDefinition(galaxy, "test:moon");
    Planet explicit = byDefinition(galaxy, "test:explicit");
    int expected = GalaxyData.averageTemperature(galaxy.stars.get(parent.star), 200, 100);
    helper.assertTrue(
        parent.temperature == expected && moon.temperature == expected,
        "Bodies did not derive temperature from the primary planet's stellar orbit");
    helper.assertTrue(
        !parent.biomes.isEmpty() && !moon.biomes.isEmpty(),
        "Planets without biome lists did not select climate-appropriate biomes");
    helper.assertTrue(
        !parent.caves && !parent.structures && explicit.caves && explicit.structures,
        "Generation defaults or explicit overrides were not honored");
    helper.assertTrue(
        explicit.temperature == 400
            && explicit.biomes.equals(List.of(ResourceLocation.withDefaultNamespace("desert"))),
        "Climate defaults overwrote explicitly configured temperature or biomes");
    helper.succeed();
  }

  static GalaxyData galaxy(Map<String, String> stars, Map<String, String> planets, long seed) {
    return GalaxyDefinitions.create(definitions(stars, planets), seed);
  }

  static GalaxyDefinitions.Definitions definitions(
      Map<String, String> stars, Map<String, String> planets) {
    Map<ResourceLocation, StarDefinition> starDefinitions = new LinkedHashMap<>();
    stars.forEach(
        (key, json) ->
            starDefinitions.put(ResourceLocation.parse(key), parse(StarDefinition.CODEC, json)));
    Map<ResourceLocation, PlanetDefinition> planetDefinitions = new LinkedHashMap<>();
    planets.forEach(
        (key, json) ->
            planetDefinitions.put(
                ResourceLocation.parse(key), parse(PlanetDefinition.CODEC, json)));
    return new GalaxyDefinitions.Definitions(starDefinitions, planetDefinitions);
  }

  static <T> T parse(Codec<T> codec, String json) {
    return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
  }

  static Planet byDefinition(GalaxyData galaxy, String key) {
    ResourceLocation id = ResourceLocation.parse(key);
    return galaxy.planets.values().stream()
        .filter(planet -> id.equals(planet.location))
        .findFirst()
        .orElseThrow();
  }

  @GameTest(template = "empty")
  public static void oreProfilePrecedenceAndPersistence(GameTestHelper helper) {
    String ore =
        "{\"block\": \"%s\", \"min_height\": -32, \"max_height\": 40, \"size\": 6,"
            + " \"count\": 10}";
    String aluminum = ore.formatted("adv_rocketry:aluminum_ore");
    Map<ResourceLocation, PlanetOres.Profile> profiles = new LinkedHashMap<>();
    profiles.put(
        ResourceLocation.parse("test:pressure"),
        parse(
            PlanetOres.Profile.CODEC, "{\"pressure\": \"normal\", \"ores\": [" + aluminum + "]}"));
    profiles.put(
        ResourceLocation.parse("test:temperature"),
        parse(
            PlanetOres.Profile.CODEC,
            "{\"temperature\": \"normal\", \"ores\": ["
                + ore.formatted("adv_rocketry:iridium_ore")
                + "]}"));
    profiles.put(
        ResourceLocation.parse("test:both"),
        parse(
            PlanetOres.Profile.CODEC,
            "{\"pressure\": \"normal\", \"temperature\": \"normal\", \"ores\": ["
                + ore.formatted("adv_rocketry:tin_ore")
                + "]}"));
    var rules = PlanetOres.rules(profiles);
    Planet planet = new Planet(992, "Ore rules");
    helper.assertTrue(
        PlanetOres.select(planet, rules).getFirst().block().getPath().equals("tin_ore"),
        "Combined profile did not override single-category profiles");
    planet.atmosphere = 75;
    helper.assertTrue(
        PlanetOres.select(planet, rules).getFirst().block().getPath().equals("iridium_ore"),
        "Pressure category did not use the original strict boundary");
    planet.temperature = 275;
    helper.assertTrue(
        PlanetOres.select(planet, rules).isEmpty(), "Temperature boundary used the wrong category");
    planet.atmosphere = 100;
    planet.temperature = 286;
    TerraformBiomeLogic.changeAtmosphere(planet, 0, 19L);
    helper.assertTrue(
        PlanetOres.select(Planet.load(planet.save()), rules).equals(rules.getLast().ores()),
        "Terraforming or save/reload lost original pressure for ore selection");
    GalaxyData galaxy =
        galaxy(
            Map.of("test:star", "{\"name\": \"Test\"}"),
            Map.of(
                "test:ores",
                """
                {"name": "Ores", "star": "test:star", "force_rivers": true,
                 "sky_render_override": true, "color_override": true,
                 "crater_biomes": {"minecraft:plains": 100, "minecraft:desert": 0},
                 "biomes": [{"biome": "minecraft:plains", "weight": 9},
                            {"biome": "minecraft:desert", "weight": 1}],
                 "ores": [%s]}
                """
                    .formatted(aluminum)),
            1L);
    Planet restored = Planet.load(galaxy.planets.get(1).save());
    helper.assertTrue(
        PlanetOres.select(restored, rules).equals(rules.getFirst().ores()),
        "Planet-specific ores did not survive save/reload or override profiles");
    helper.assertTrue(
        restored.biomeWeights.get(ResourceLocation.withDefaultNamespace("plains")) == 9
            && restored.biomeWeights.get(ResourceLocation.withDefaultNamespace("desert")) == 1,
        "Biome weights did not survive planet save/reload");
    helper.assertTrue(
        restored.skyRenderOverride && restored.colorOverride && restored.forceRivers,
        "Per-planet sky/color overrides did not survive JSON and save/reload");
    helper.assertTrue(
        restored.craterBiomeWeights.get(ResourceLocation.withDefaultNamespace("plains")) == 100
            && restored.craterBiomeWeights.get(ResourceLocation.withDefaultNamespace("desert"))
                == 0,
        "Crater biome frequencies were not preserved");
    helper.assertTrue(
        PlanetOres.Profile.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"pressure\": \"normal\", \"ores\": ["
                        + aluminum.replace("\"max_height\": 40", "\"max_height\": -32")
                        + "]}"))
            .isError(),
        "Empty height range was accepted and could crash generation");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void customPlanetOreGenerationUsesFillerAndSuppressesDefaults(
      GameTestHelper helper) {
    Planet planet = new Planet(993, "Custom ore generation");
    planet.filler = ResourceLocation.withDefaultNamespace("sandstone");
    planet.ores.add(
        new PlanetOres.Entry(ResourceLocation.withDefaultNamespace("diamond_ore"), 10, 40, 48, 80));
    var level = PlanetRuntime.create(helper.getLevel().getServer(), planet);
    var chunk = level.getChunk(0, 0);
    int diamonds = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = level.getMinBuildHeight(); y < 80; y++) {
          var state = chunk.getBlockState(new BlockPos(x, y, z));
          String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
          if (state.is(Blocks.DIAMOND_ORE)) diamonds++;
          else
            helper.assertTrue(
                !block.endsWith("_ore"), "Default ore leaked into custom distribution: " + block);
        }
    helper.assertTrue(diamonds > 0, "Custom ore did not replace the planet's sandstone filler");
    var biome =
        level
            .registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(
                ResourceKey.create(
                    Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    var original = biome.value().getGenerationSettings();
    var filtered = level.getChunkSource().getGenerator().getBiomeGenerationSettings(biome);
    helper.assertTrue(
        filtered != original && !filtered.features().isEmpty(),
        "Custom ore rules removed all biome decoration or did not install the filter");
    boolean stoneRetained =
        filtered.features().stream()
            .flatMap(set -> set.stream())
            .flatMap(feature -> feature.value().getFeatures())
            .anyMatch(
                feature ->
                    feature.config() instanceof OreConfiguration config
                        && config.targetStates.stream()
                            .anyMatch(target -> target.state.is(Blocks.GRANITE)));
    helper.assertTrue(stoneRetained, "Ore override incorrectly removed decorative stone deposits");
    helper.succeed();
  }
}

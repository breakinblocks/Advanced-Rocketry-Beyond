package advRocketry.space;

import advRocketry.orbit.Satellite;
import advRocketry.orbit.Station;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class PlanetResetTests {
  @GameTest(template = "empty")
  public static void definitionResetKeepsInfrastructure(GameTestHelper helper) {
    GalaxyData galaxy = new GalaxyData();
    galaxy.planets.put(GalaxyData.EARTH_ID, new Planet(GalaxyData.EARTH_ID, "Earth"));
    galaxy.planets.put(GalaxyData.SPACE_ID, new Planet(GalaxyData.SPACE_ID, "Space"));
    galaxy.planets.put(1, new Planet(1, "Before"));
    galaxy.stars.put(0, new Star(0, "Sol", 100, 1, 0, 0, false));
    galaxy.reachedMoon = true;
    Station station = new Station();
    station.orbitPlanet = 1;
    galaxy.stations.put(7L, station);
    galaxy.satellites.put(8L, new Satellite());
    var definitions =
        OreTests.definitions(
            Map.of("test:sol", "{\"name\": \"Sol\"}"),
            Map.of(
                "test:after",
                "{\"name\": \"After\", \"star\": \"test:sol\", \"biomes\":"
                    + " [\"adv_rocketry:moon\"]}"));
    helper.assertTrue(
        GalaxyDefinitions.addMissing(galaxy, definitions, 1)
            && galaxy.planets.get(1).name.equals("Before")
            && galaxy.planets.get(2).name.equals("After"),
        "Ordinary startup replaced a saved planet instead of adding the new definition");
    helper.assertTrue(
        !GalaxyDefinitions.addMissing(galaxy, definitions, 1),
        "Known definitions were added a second time");
    GalaxyDefinitions.reset(galaxy, definitions, 1);
    helper.assertTrue(
        galaxy.planets.get(2).name.equals("After") && !galaxy.planets.containsKey(1),
        "Explicit reset did not rebuild planets from definitions");
    var reordered =
        OreTests.definitions(
            Map.of("test:sol", "{\"name\": \"Sol\"}"),
            Map.of(
                "test:after",
                "{\"name\": \"After\", \"star\": \"test:sol\", \"biomes\":"
                    + " [\"adv_rocketry:moon\"]}",
                "test:early",
                "{\"name\": \"Early\", \"star\": \"test:sol\", \"biomes\":"
                    + " [\"adv_rocketry:moon\"]}"));
    GalaxyDefinitions.reset(galaxy, reordered, 1);
    helper.assertTrue(
        galaxy.planets.get(2).name.equals("After")
            && galaxy.planets.get(3).name.equals("Early")
            && galaxy.planets.get(2).dimension.equals(ResourceLocation.parse("test:after")),
        "A new definition renumbered an existing planet");
    helper.assertTrue(
        galaxy.stations.get(7L) == station
            && galaxy.satellites.containsKey(8L)
            && galaxy.reachedMoon
            && galaxy.planets.containsKey(GalaxyData.SPACE_ID),
        "Reset discarded infrastructure or progression");
    Planet current = galaxy.planets.get(2);
    var broken =
        OreTests.definitions(
            Map.of("test:sol", "{\"name\": \"Sol\"}"),
            Map.of("test:lost", "{\"name\": \"Lost\", \"star\": \"test:missing\"}"));
    try {
      GalaxyDefinitions.reset(galaxy, broken, 1);
      throw new AssertionError("Planet orbiting an unknown star was accepted");
    } catch (IllegalStateException expected) {
    }
    helper.assertTrue(
        galaxy.planets.get(2) == current && galaxy.stations.get(7L) == station,
        "Failed reset partially mutated galaxy");
    try {
      GalaxyDefinitions.reset(galaxy, OreTests.definitions(Map.of(), Map.of()), 1);
      throw new AssertionError("Galaxy without stars was accepted");
    } catch (IllegalStateException expected) {
    }
    helper.succeed();
  }
}

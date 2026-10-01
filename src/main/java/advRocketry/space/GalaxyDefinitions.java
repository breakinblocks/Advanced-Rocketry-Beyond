package advRocketry.space;

import advRocketry.DataRegistries;
import advRocketry.Main;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class GalaxyDefinitions {
  public record Definitions(
      Map<ResourceLocation, StarDefinition> stars,
      Map<ResourceLocation, PlanetDefinition> planets) {
    public static Definitions of(RegistryAccess registries) {
      return new Definitions(
          entries(registries.registryOrThrow(DataRegistries.STAR)),
          entries(registries.registryOrThrow(DataRegistries.PLANET)));
    }

    private static <T> Map<ResourceLocation, T> entries(Registry<T> registry) {
      Map<ResourceLocation, T> result = new LinkedHashMap<>();
      registry.entrySet().forEach(entry -> result.put(entry.getKey().location(), entry.getValue()));
      return result;
    }
  }

  private GalaxyDefinitions() {}

  public static GalaxyData create(Definitions definitions, long seed) {
    GalaxyData galaxy = new GalaxyData();
    populate(galaxy, definitions, seed);
    galaxy.planets.put(GalaxyData.SPACE_ID, space());
    galaxy.setDirty();
    return galaxy;
  }

  public static void reset(GalaxyData galaxy, Definitions definitions, long seed) {
    GalaxyData staged = new GalaxyData();
    staged.starDefinitions.putAll(galaxy.starDefinitions);
    staged.planetIds.putAll(galaxy.planetIds);
    populate(staged, definitions, seed);
    Planet space = galaxy.planets.get(GalaxyData.SPACE_ID);
    staged.planets.put(GalaxyData.SPACE_ID, space == null ? space() : space);
    galaxy.stars.clear();
    galaxy.stars.putAll(staged.stars);
    galaxy.starDefinitions.clear();
    galaxy.starDefinitions.putAll(staged.starDefinitions);
    galaxy.planetIds.clear();
    galaxy.planetIds.putAll(staged.planetIds);
    galaxy.planets.clear();
    galaxy.planets.putAll(staged.planets);
    galaxy.setDirty();
  }

  public static boolean addMissing(GalaxyData galaxy, Definitions definitions, long seed) {
    boolean changed = false;
    for (var entry : sorted(definitions.stars())) {
      if (galaxy.starDefinitions.containsKey(entry.getKey())) continue;
      int id = galaxy.starId(entry.getKey());
      Star star = entry.getValue().create(id);
      galaxy.stars.put(id, star);
      galaxy.generate(
          entry.getKey(),
          star,
          entry.getValue().terrestrialPlanets(),
          entry.getValue().gasGiants(),
          new Random(seed ^ entry.getKey().hashCode()));
      changed = true;
    }
    Map<ResourceLocation, Integer> ids = new LinkedHashMap<>();
    galaxy.planets.values().stream()
        .filter(planet -> planet.location != null)
        .forEach(planet -> ids.put(planet.location, planet.id));
    List<Map.Entry<ResourceLocation, PlanetDefinition>> added = new ArrayList<>();
    for (var entry : sorted(definitions.planets())) {
      if (ids.containsKey(entry.getKey())) continue;
      added.add(entry);
    }
    for (var entry : added) ids.put(entry.getKey(), galaxy.planetId(entry.getKey()));
    if (!added.isEmpty()) {
      build(galaxy, added, ids, seed);
      changed = true;
    }
    if (changed) galaxy.setDirty();
    return changed;
  }

  private static void populate(GalaxyData galaxy, Definitions definitions, long seed) {
    var planets = sorted(definitions.planets());
    var home =
        planets.stream().filter(entry -> overworld(entry.getValue())).findFirst().orElse(null);
    var stars = new ArrayList<>(sorted(definitions.stars()));
    if (stars.isEmpty()) throw new IllegalStateException("No star definitions are loaded");
    if (home != null)
      stars.sort(
          Comparator.comparing(entry -> !entry.getKey().equals(home.getValue().identity().star())));
    for (var entry : stars) {
      int id = galaxy.starId(entry.getKey());
      galaxy.stars.put(id, entry.getValue().create(id));
    }
    Map<ResourceLocation, Integer> ids = new LinkedHashMap<>();
    ResourceLocation homeKey = home != null ? home.getKey() : Level.OVERWORLD.location();
    galaxy.planetIds.values().removeIf(id -> id == GalaxyData.EARTH_ID);
    galaxy.planetIds.put(homeKey, GalaxyData.EARTH_ID);
    if (home != null) ids.put(home.getKey(), GalaxyData.EARTH_ID);
    else {
      Planet earth = new Planet(GalaxyData.EARTH_ID, "Earth");
      earth.location = homeKey;
      earth.dimension = Level.OVERWORLD.location();
      galaxy.planets.put(GalaxyData.EARTH_ID, earth);
    }
    for (var entry : planets)
      if (entry != home) ids.put(entry.getKey(), galaxy.planetId(entry.getKey()));
    build(galaxy, planets, ids, seed);
    Random random = new Random(seed);
    for (var entry : stars)
      galaxy.generate(
          entry.getKey(),
          galaxy.stars.get(galaxy.starDefinitions.get(entry.getKey())),
          entry.getValue().terrestrialPlanets(),
          entry.getValue().gasGiants(),
          random);
  }

  private static void build(
      GalaxyData galaxy,
      List<Map.Entry<ResourceLocation, PlanetDefinition>> entries,
      Map<ResourceLocation, Integer> ids,
      long seed) {
    List<Planet> built = new ArrayList<>();
    for (var entry : entries) {
      ResourceLocation key = entry.getKey();
      PlanetDefinition definition = entry.getValue();
      Integer star = galaxy.starDefinitions.get(definition.identity().star());
      if (star == null)
        throw new IllegalStateException(
            "Planet " + key + " orbits unknown star " + definition.identity().star());
      int parent = -1;
      if (definition.identity().parent().isPresent()) {
        Integer parentId = ids.get(definition.identity().parent().get());
        if (parentId == null)
          throw new IllegalStateException(
              "Planet " + key + " orbits unknown planet " + definition.identity().parent().get());
        parent = parentId;
      }
      Planet planet = definition.create(ids.get(key), key, star, parent);
      galaxy.planets.put(planet.id, planet);
      built.add(planet);
    }
    for (int index = 0; index < built.size(); index++) {
      Planet planet = built.get(index);
      PlanetDefinition definition = entries.get(index).getValue();
      if (definition.physical().temperature().isEmpty())
        planet.temperature =
            GalaxyData.averageTemperature(
                galaxy.stars.get(planet.star),
                planet.root(galaxy.planets::get).orbitalDistance,
                planet.atmosphere);
      if (planet.biomes.isEmpty()
          && !planet.gasGiant
          && definition.identity().dimension().isEmpty())
        GalaxyData.selectBiomes(planet, new Random(PlanetSeeds.biomes(seed, planet.id)));
    }
  }

  private static boolean overworld(PlanetDefinition definition) {
    return definition
        .identity()
        .dimension()
        .map(dimension -> dimension.equals(Level.OVERWORLD.location()))
        .orElse(false);
  }

  private static <T> List<Map.Entry<ResourceLocation, T>> sorted(Map<ResourceLocation, T> map) {
    return map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
  }

  static Planet space() {
    Planet space = new Planet(GalaxyData.SPACE_ID, "Space");
    space.location = ResourceLocation.fromNamespaceAndPath(Main.MODID, "space");
    space.dimension = space.location;
    space.temperature = 0;
    space.atmosphere = 0;
    space.oxygen = false;
    space.gravity = 0.1f;
    space.skyColor = 0;
    space.fogColor = 0;
    space.terrain = TerrainType.SPACE;
    space.structures = false;
    return space;
  }
}

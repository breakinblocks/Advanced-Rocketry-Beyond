// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.Main;
import advRocketry.orbit.Mission;
import advRocketry.orbit.Satellite;
import advRocketry.orbit.Station;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** World-scoped galaxy state, replacing the original singleton and dimension.dat file. */
public final class GalaxyData extends SavedData {
  public static final int EARTH_ID = 0;
  public static final int SPACE_ID = -2;
  public static final ResourceLocation LUNA =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "luna");
  public final Map<Integer, Star> stars = new LinkedHashMap<>();
  public final Map<ResourceLocation, Integer> starDefinitions = new LinkedHashMap<>();
  public final Map<ResourceLocation, Integer> planetIds = new LinkedHashMap<>();
  public final Map<Integer, Planet> planets = new LinkedHashMap<>();
  public final Map<Long, Satellite> satellites = new LinkedHashMap<>();
  public final Map<Long, Station> stations = new LinkedHashMap<>();
  public final Map<Long, Mission> miningMissions = new LinkedHashMap<>();
  public final Map<ResourceLocation, Set<Long>> beacons = new LinkedHashMap<>();
  public final Map<String, Long> wirelessNetworks = new LinkedHashMap<>();
  private long nextSatellite = 1;
  private long nextStation = 1;
  private long nextMiningMission = 1;
  private long nextWirelessNetwork = 1;

  public long wirelessNetwork(String position) {
    return wirelessNetworks.getOrDefault(position, 0L);
  }

  public long linkWireless(String first, String second) {
    long left = wirelessNetwork(first), right = wirelessNetwork(second);
    long selected = left != 0 ? left : right != 0 ? right : nextWirelessNetwork++;
    if (right != 0 && right != selected)
      wirelessNetworks.replaceAll((position, network) -> network == right ? selected : network);
    wirelessNetworks.put(first, selected);
    wirelessNetworks.put(second, selected);
    setDirty();
    return selected;
  }

  public void removeWireless(String position) {
    if (wirelessNetworks.remove(position) != null) setDirty();
  }

  public boolean reachedMoon;
  public boolean reachedWarp;

  public static GalaxyData get(MinecraftServer server) {
    return server
        .overworld()
        .getDataStorage()
        .computeIfAbsent(
            new Factory<>(
                () ->
                    GalaxyDefinitions.create(
                        GalaxyDefinitions.Definitions.of(server.registryAccess()),
                        server.getWorldData().worldGenOptions().seed()),
                GalaxyData::load),
            "advanced_rocketry_galaxy");
  }

  public Planet planet(Level level) {
    return planets.values().stream()
        .filter(planet -> planet.dimension.equals(level.dimension().location()))
        .findFirst()
        .orElse(planets.get(EARTH_ID));
  }

  public Planet find(Level level) {
    return byDimension(level.dimension().location());
  }

  public Planet byDimension(ResourceLocation key) {
    return planets.values().stream()
        .filter(planet -> planet.dimension.equals(key))
        .findFirst()
        .orElse(null);
  }

  public Planet ensureExternalPlanet(ServerLevel level) {
    Planet existing = byDimension(level.dimension().location());
    if (existing != null) return existing;
    int id = -3;
    while (planets.containsKey(id)) id--;
    Planet external = new Planet(id, level.dimension().location().toString());
    external.location = level.dimension().location();
    external.dimension = level.dimension().location();
    planets.put(id, external);
    setDirty();
    return external;
  }

  public long newSatellite(Satellite satellite) {
    long id = nextSatellite++;
    satellite.id = id;
    satellites.put(id, satellite);
    setDirty();
    return id;
  }

  public long newStation(Station station) {
    long id = nextStation++;
    station.id = id;
    stations.put(id, station);
    setDirty();
    return id;
  }

  public long newMiningMission(Mission mission) {
    long id = nextMiningMission++;
    miningMissions.put(id, mission);
    setDirty();
    return id;
  }

  public int planetId(ResourceLocation key) {
    Integer existing = planetIds.get(key);
    if (existing != null) return existing;
    int id =
        Math.max(
                planets.keySet().stream().mapToInt(Integer::intValue).max().orElse(0),
                planetIds.values().stream().mapToInt(Integer::intValue).max().orElse(0))
            + 1;
    planetIds.put(key, id);
    return id;
  }

  public int starId(ResourceLocation key) {
    Integer existing = starDefinitions.get(key);
    if (existing != null) return existing;
    int id =
        Math.max(
                stars.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1),
                starDefinitions.values().stream().mapToInt(Integer::intValue).max().orElse(-1))
            + 1;
    starDefinitions.put(key, id);
    return id;
  }

  public void generate(
      ResourceLocation starKey, Star star, int terrestrial, int giants, Random random) {
    for (int i = 0; i < giants; i++) {
      Planet giant =
          randomPlanet(
              starKey.withSuffix("/giant_" + i), star, 150, 180, 125, 100, 100, 75, random);
      giant.gasGiant = true;
      giant.gases.addAll(GasGiantGases.select(giant.gravity, random));
      moons(star, giant, 8, random);
    }
    for (int i = 0; i < terrestrial; i++) {
      int atmosphere = i % 4 == 0 ? 0 : i != 6 && (i + 2) % 4 == 0 ? 120 : 75;
      int distance = i % 3 == 0 ? 170 : (i + 1) % 3 == 0 ? 30 : 100;
      // Keep the original call's atmosphere/distance ordering for legacy generation parity.
      Planet planet =
          randomPlanet(
              starKey.withSuffix("/planet_" + i),
              star,
              distance,
              atmosphere,
              125,
              100,
              100,
              75,
              random);
      moons(star, planet, 4, random);
    }
    setDirty();
  }

  private void moons(Star star, Planet parent, int limit, Random random) {
    if (parent.gravity < 1) return;
    int count = random.nextInt(limit);
    for (int i = 0; i < count; i++) {
      Planet moon =
          randomPlanet(
              parent.location.withSuffix("/moon_" + i),
              star,
              25,
              100,
              (int) (parent.gravity / .02f),
              25,
              100,
              50,
              random);
      moon.parent = parent.id;
      moon.name = parent.name + ": " + i;
      moon.temperature = averageTemperature(star, parent.orbitalDistance, moon.atmosphere);
    }
  }

  private Planet randomPlanet(
      ResourceLocation key,
      Star star,
      int atmosphere,
      int distance,
      int gravity,
      int atmosphereFactor,
      int distanceFactor,
      int gravityFactor,
      Random random) {
    int id = planetId(key);
    Planet planet = new Planet(id, star.name() + " " + id);
    planet.location = key;
    planet.dimension = key;
    planet.star = star.id();
    planet.known = false;
    planet.atmosphere =
        Math.clamp(atmosphere + random.nextInt(atmosphereFactor) - atmosphereFactor / 2, 0, 1600);
    planet.orbitalDistance = Math.max(1, distance + random.nextInt(distanceFactor));
    planet.gravity =
        Math.clamp(
            (gravity + random.nextInt(gravityFactor) - gravityFactor / 2f) / 100f, .05f, 1.3f);
    // Bounded outward search avoids the original orbit walk's potential infinite loop.
    while (planets.values().stream()
        .anyMatch(
            other ->
                other.star == star.id()
                    && other.parent < 0
                    && Math.abs(other.orbitalDistance - planet.orbitalDistance) < 4))
      planet.orbitalDistance++;
    planet.orbitalTheta = random.nextInt(360) * Math.PI / 180;
    planet.orbitalPhi = (random.nextGaussian() - .5) * 180;
    planet.temperature = averageTemperature(star, planet.orbitalDistance, planet.atmosphere);
    if (random.nextInt(10) == 0) planet.seaLevel = 43 + random.nextInt(40);
    float red =
        1 - Math.clamp(random.nextFloat() * .1f + (70 - planet.temperature / 3f) / 100f, .2f, 1f);
    float green = 1 - random.nextFloat() * .5f;
    float blue =
        1 - Math.clamp(random.nextFloat() * .1f + (planet.temperature / 3f - 70) / 100f, 0f, 1f);
    planet.skyColor = ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
    planet.rings = random.nextInt(50) == 0;
    planet.ringColor = planet.skyColor;
    planet.rotationPeriod = (long) (Math.pow(1 / planet.gravity, 3) * 24000);
    planet.craters = planet.atmosphere < 75;
    planet.volcanoes = planet.temperature > 450;
    planet.geodes = planet.atmosphere > 125;
    planet.structures = planet.breathable();
    planet.terrain =
        planet.volcanoes
            ? TerrainType.VOLCANIC
            : planet.atmosphere <= 25 ? TerrainType.MOON : TerrainType.TERRESTRIAL;
    if (planet.volcanoes) planet.ocean = ResourceLocation.withDefaultNamespace("lava");
    selectBiomes(planet, random);
    planets.put(id, planet);
    return planet;
  }

  static void selectBiomes(Planet planet, Random random) {
    planet.biomes.addAll(PlanetBiomeCatalog.select(planet, random));
  }

  public static int averageTemperature(Star star, int distance, int atmosphere) {
    double temperature =
        58
            * star.temperature()
            * Math.sqrt((star.size() / 215d) / (2 * Math.max(1, distance) / 100d))
            * Math.pow(.7, .25);
    return (int) (temperature * Math.max(1, 1.125 * Math.pow(atmosphere / 100d, .25)));
  }

  @Override
  public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    ListTag starTags = new ListTag();
    stars.values().forEach(star -> starTags.add(star.save()));
    tag.put("stars", starTags);
    CompoundTag starKeys = new CompoundTag();
    starDefinitions.forEach((key, id) -> starKeys.putInt(key.toString(), id));
    tag.put("star_definitions", starKeys);
    CompoundTag planetKeys = new CompoundTag();
    planetIds.forEach((key, id) -> planetKeys.putInt(key.toString(), id));
    tag.put("planet_ids", planetKeys);
    ListTag planetTags = new ListTag();
    planets.values().forEach(planet -> planetTags.add(planet.save()));
    tag.put("planets", planetTags);
    tag.putLong("next_satellite", nextSatellite);
    tag.putLong("next_station", nextStation);
    tag.putLong("next_mining_mission", nextMiningMission);
    tag.putLong("next_wireless_network", nextWirelessNetwork);
    tag.putBoolean("reached_moon", reachedMoon);
    tag.putBoolean("reached_warp", reachedWarp);
    CompoundTag satelliteTags = new CompoundTag();
    satellites.forEach((id, satellite) -> satelliteTags.put(Long.toString(id), satellite.save()));
    tag.put("satellites", satelliteTags);
    CompoundTag stationTags = new CompoundTag();
    stations.forEach((id, station) -> stationTags.put(Long.toString(id), station.save()));
    tag.put("stations", stationTags);
    CompoundTag missionTags = new CompoundTag();
    miningMissions.forEach((id, mission) -> missionTags.put(Long.toString(id), mission.save()));
    tag.put("mining_missions", missionTags);
    CompoundTag beaconTags = new CompoundTag();
    beacons.forEach(
        (dimension, positions) ->
            beaconTags.putLongArray(
                dimension.toString(), positions.stream().mapToLong(Long::longValue).toArray()));
    tag.put("beacons", beaconTags);
    CompoundTag wirelessTags = new CompoundTag();
    wirelessNetworks.forEach(wirelessTags::putLong);
    tag.put("wireless_networks", wirelessTags);
    return tag;
  }

  public static GalaxyData load(CompoundTag tag, HolderLookup.Provider registries) {
    GalaxyData galaxy = new GalaxyData();
    for (Tag entry : tag.getList("stars", Tag.TAG_COMPOUND)) {
      Star star = Star.load((CompoundTag) entry);
      galaxy.stars.put(star.id(), star);
    }
    CompoundTag starKeys = tag.getCompound("star_definitions");
    for (String key : starKeys.getAllKeys())
      galaxy.starDefinitions.put(ResourceLocation.parse(key), starKeys.getInt(key));
    CompoundTag planetKeys = tag.getCompound("planet_ids");
    for (String key : planetKeys.getAllKeys())
      galaxy.planetIds.put(ResourceLocation.parse(key), planetKeys.getInt(key));
    for (Tag entry : tag.getList("planets", Tag.TAG_COMPOUND)) {
      Planet planet = Planet.load((CompoundTag) entry);
      galaxy.planets.put(planet.id, planet);
    }
    galaxy.nextSatellite = Math.max(1, tag.getLong("next_satellite"));
    galaxy.nextStation = Math.max(1, tag.getLong("next_station"));
    galaxy.nextMiningMission = Math.max(1, tag.getLong("next_mining_mission"));
    galaxy.nextWirelessNetwork = Math.max(1, tag.getLong("next_wireless_network"));
    galaxy.reachedMoon = tag.getBoolean("reached_moon");
    galaxy.reachedWarp = tag.getBoolean("reached_warp");
    CompoundTag satellites = tag.getCompound("satellites");
    satellites
        .getAllKeys()
        .forEach(
            key -> {
              Satellite satellite = Satellite.load(satellites.getCompound(key));
              satellite.id = Long.parseLong(key);
              galaxy.satellites.put(satellite.id, satellite);
            });
    CompoundTag stations = tag.getCompound("stations");
    stations
        .getAllKeys()
        .forEach(
            key ->
                galaxy.stations.put(Long.parseLong(key), Station.load(stations.getCompound(key))));
    CompoundTag missions = tag.getCompound("mining_missions");
    missions
        .getAllKeys()
        .forEach(
            key -> {
              Mission mission = Mission.load(missions.getCompound(key));
              if (mission != null) galaxy.miningMissions.put(Long.parseLong(key), mission);
            });
    CompoundTag beaconTags = tag.getCompound("beacons");
    for (String key : beaconTags.getAllKeys()) {
      Set<Long> positions = new LinkedHashSet<>();
      for (long pos : beaconTags.getLongArray(key)) positions.add(pos);
      galaxy.beacons.put(ResourceLocation.parse(key), positions);
    }
    CompoundTag wirelessTags = tag.getCompound("wireless_networks");
    for (String key : wirelessTags.getAllKeys())
      galaxy.wirelessNetworks.put(key, wirelessTags.getLong(key));
    return galaxy;
  }
}

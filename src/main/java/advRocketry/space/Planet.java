// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.Main;
import advRocketry.util.IdOrTag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Native counterpart of the original DimensionProperties; values retain their original units. */
public final class Planet {
  public final int id;
  public String name;
  public ResourceLocation location;
  public ResourceLocation dimension;
  public int star;
  public int parent = -1;
  public int temperature = 286;
  public int atmosphere = 100;
  public Integer originalAtmosphere;
  public final List<PlanetOres.Entry> ores = new ArrayList<>();
  public final List<PlanetSpawns.Entry> spawns = new ArrayList<>();
  public float gravity = 1;
  public int orbitalDistance = 100;
  public double orbitalTheta;
  public double orbitalPhi;
  public long rotationPeriod = 24000;
  public int seaLevel = 63;
  public boolean oxygen = true;
  public boolean gasGiant;
  public boolean known = true;
  public boolean rings;
  public boolean retrograde;
  public boolean skyRenderOverride;
  public boolean colorOverride;
  public boolean forceRivers;
  public Boolean shading;
  public String customIcon = "";
  public int skyColor = 0xffffff;
  public int fogColor = 0xc0d8ff;
  public int ringColor = 0xffffff;
  public TerrainType terrain = TerrainType.TERRESTRIAL;
  public ResourceLocation filler = ResourceLocation.withDefaultNamespace("stone");
  public ResourceLocation ocean = ResourceLocation.withDefaultNamespace("water");
  public boolean craters;
  public boolean volcanoes;
  public boolean geodes;
  public boolean caves = true;
  public boolean structures = true;
  public float craterFrequency = 1;
  public float volcanoFrequency = 1;
  public float geodeFrequency = 1;
  public final List<ResourceLocation> biomes = new ArrayList<>();
  public final Map<ResourceLocation, Integer> biomeWeights = new LinkedHashMap<>();
  public final Map<ResourceLocation, Integer> craterBiomeWeights = new LinkedHashMap<>();
  public final List<ResourceLocation> terraformedBiomes = new ArrayList<>();
  public final List<ResourceLocation> gases = new ArrayList<>();
  public final List<IdOrTag> artifacts = new ArrayList<>();
  public final List<IdOrTag> laserOres = new ArrayList<>();
  public final List<IdOrTag> geodeOres = new ArrayList<>();
  public final List<IdOrTag> craterOres = new ArrayList<>();
  public boolean laserDrillable = true;

  public Planet(int id, String name) {
    this.id = id;
    this.name = name;
    dimension = ResourceLocation.fromNamespaceAndPath(Main.MODID, "planet_" + id);
  }

  public ResourceKey<Level> key() {
    return ResourceKey.create(Registries.DIMENSION, dimension);
  }

  public boolean breathable() {
    return oxygen && atmosphere > 75 && atmosphere <= 200 && temperature <= 450;
  }

  public boolean landable() {
    return !gasGiant;
  }

  public boolean hasRivers() {
    int pressure = originalAtmosphere == null ? atmosphere : originalAtmosphere;
    return forceRivers || pressure > 25 && temperature > 250 && temperature <= 450;
  }

  public boolean hasShading() {
    return shading == null ? terrain != TerrainType.ASTEROID : shading;
  }

  public Planet root(IntFunction<Planet> lookup) {
    Planet root = this;
    Set<Integer> visited = new HashSet<>();
    while (root.parent >= 0 && visited.add(root.id)) {
      Planet parent = lookup.apply(root.parent);
      if (parent == null) break;
      root = parent;
    }
    return root;
  }

  public BlockState fillerState() {
    return BuiltInRegistries.BLOCK.containsKey(filler)
        ? BuiltInRegistries.BLOCK.get(filler).defaultBlockState()
        : Blocks.STONE.defaultBlockState();
  }

  /** Original AstronomicalBodyHelper periods: 48-day years and eight-day lunar months. */
  public double orbitalAngle(double ticks, Star primaryStar, Planet parentPlanet) {
    double distance = Math.max(1, orbitalDistance) / 100d;
    double days =
        parentPlanet == null
            ? 48 * Math.pow(distance / (primaryStar == null ? 1 : primaryStar.size()), 1.5)
            : 8 * Math.sqrt(Math.pow(distance, 3) / Math.max(.001f, parentPlanet.gravity));
    double period = 24000 * days;
    double phase = ticks % period / period * Math.PI * 2;
    return (orbitalTheta + phase) * (retrograde ? -1 : 1);
  }

  /** Original DimensionProperties icon selection, using its strict climate boundaries. */
  public ResourceLocation icon() {
    if (!customIcon.isBlank()) {
      String path = customIcon.toLowerCase(Locale.ROOT);
      if (path.contains(":")) return ResourceLocation.parse(path);
      return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/planets/" + path + ".png");
    }
    String name;
    if (gasGiant) name = "gasgiantblue";
    else if (terrain == TerrainType.ASTEROID) name = "asteroid";
    else if (temperature > 450) name = "marslike";
    else if (atmosphere > 25 && temperature > 250) name = "earthlike";
    else if (temperature <= 250) name = atmosphere <= 25 ? "moon" : "iceworld";
    else if (atmosphere <= 25) name = temperature > 275 ? "marslike" : "moon";
    else name = "lava";
    return ResourceLocation.fromNamespaceAndPath(
        Main.MODID, "textures/legacy/advancedrocketry/planets/" + name + ".png");
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("id", id);
    tag.putString("name", name);
    if (location != null) tag.putString("location", location.toString());
    tag.putString("dimension", dimension.toString());
    tag.putInt("star", star);
    tag.putInt("parent", parent);
    tag.putInt("temperature", temperature);
    tag.putInt("atmosphere", atmosphere);
    if (originalAtmosphere != null) tag.putInt("original_atmosphere", originalAtmosphere);
    tag.put("ores", PlanetOres.save(ores));
    tag.put("spawns", PlanetSpawns.save(spawns));
    tag.putFloat("gravity", gravity);
    tag.putInt("orbital_distance", orbitalDistance);
    tag.putDouble("orbital_theta", orbitalTheta);
    tag.putDouble("orbital_phi", orbitalPhi);
    tag.putLong("rotation_period", rotationPeriod);
    tag.putInt("sea_level", seaLevel);
    tag.putBoolean("oxygen", oxygen);
    tag.putBoolean("gas_giant", gasGiant);
    tag.putBoolean("known", known);
    tag.putBoolean("rings", rings);
    tag.putBoolean("retrograde", retrograde);
    tag.putBoolean("sky_render_override", skyRenderOverride);
    tag.putBoolean("color_override", colorOverride);
    tag.putBoolean("force_rivers", forceRivers);
    if (shading != null) tag.putBoolean("shading", shading);
    tag.putString("custom_icon", customIcon);
    tag.putInt("sky_color", skyColor);
    tag.putInt("fog_color", fogColor);
    tag.putInt("ring_color", ringColor);
    tag.putString("terrain", terrain.getSerializedName());
    tag.putString("filler", filler.toString());
    tag.putString("ocean", ocean.toString());
    tag.putBoolean("craters", craters);
    tag.putBoolean("volcanoes", volcanoes);
    tag.putBoolean("geodes", geodes);
    tag.putBoolean("caves", caves);
    tag.putBoolean("structures", structures);
    tag.putFloat("crater_frequency", craterFrequency);
    tag.putFloat("volcano_frequency", volcanoFrequency);
    tag.putFloat("geode_frequency", geodeFrequency);
    putStrings(tag, "biomes", biomes.stream().map(ResourceLocation::toString).toList());
    CompoundTag weights = new CompoundTag();
    biomeWeights.forEach((biome, weight) -> weights.putInt(biome.toString(), weight));
    tag.put("biome_weights", weights);
    CompoundTag craterWeights = new CompoundTag();
    craterBiomeWeights.forEach((biome, weight) -> craterWeights.putInt(biome.toString(), weight));
    tag.put("crater_biome_weights", craterWeights);
    putStrings(
        tag,
        "terraformed_biomes",
        terraformedBiomes.stream().map(ResourceLocation::toString).toList());
    putStrings(tag, "gases", gases.stream().map(ResourceLocation::toString).toList());
    tag.put("artifacts", IdOrTag.save(artifacts));
    tag.put("laser_ores", IdOrTag.save(laserOres));
    tag.put("geode_ores", IdOrTag.save(geodeOres));
    tag.put("crater_ores", IdOrTag.save(craterOres));
    tag.putBoolean("laser_drillable", laserDrillable);
    return tag;
  }

  public static Planet load(CompoundTag tag) {
    Planet planet = new Planet(tag.getInt("id"), tag.getString("name"));
    if (tag.contains("location"))
      planet.location = ResourceLocation.parse(tag.getString("location"));
    planet.dimension = ResourceLocation.parse(tag.getString("dimension"));
    planet.star = tag.getInt("star");
    planet.parent = tag.getInt("parent");
    planet.temperature = tag.getInt("temperature");
    planet.atmosphere = tag.getInt("atmosphere");
    if (tag.contains("original_atmosphere"))
      planet.originalAtmosphere = tag.getInt("original_atmosphere");
    planet.ores.addAll(PlanetOres.load(tag.getList("ores", Tag.TAG_COMPOUND)));
    for (var spawn : tag.getList("spawns", Tag.TAG_COMPOUND))
      planet.spawns.add(PlanetSpawns.Entry.load((CompoundTag) spawn));
    planet.gravity = tag.getFloat("gravity");
    planet.orbitalDistance = tag.getInt("orbital_distance");
    planet.orbitalTheta = tag.getDouble("orbital_theta");
    planet.orbitalPhi = tag.getDouble("orbital_phi");
    planet.rotationPeriod = Math.max(1, tag.getLong("rotation_period"));
    planet.seaLevel = tag.getInt("sea_level");
    planet.oxygen = tag.getBoolean("oxygen");
    planet.gasGiant = tag.getBoolean("gas_giant");
    planet.known = tag.getBoolean("known");
    planet.rings = tag.getBoolean("rings");
    planet.retrograde = tag.getBoolean("retrograde");
    planet.skyRenderOverride = tag.getBoolean("sky_render_override");
    planet.colorOverride = tag.getBoolean("color_override");
    if (tag.contains("shading")) planet.shading = tag.getBoolean("shading");
    planet.customIcon = tag.getString("custom_icon");
    planet.forceRivers = tag.getBoolean("force_rivers");
    planet.skyColor = tag.getInt("sky_color");
    planet.fogColor = tag.getInt("fog_color");
    planet.ringColor = tag.getInt("ring_color");
    planet.terrain = TerrainType.byId(tag.getString("terrain"));
    planet.filler = ResourceLocation.parse(tag.getString("filler"));
    planet.ocean = ResourceLocation.parse(tag.getString("ocean"));
    planet.craters = tag.getBoolean("craters");
    planet.volcanoes = tag.getBoolean("volcanoes");
    planet.geodes = tag.getBoolean("geodes");
    planet.caves = tag.getBoolean("caves");
    planet.structures = tag.getBoolean("structures");
    planet.craterFrequency = tag.getFloat("crater_frequency");
    planet.volcanoFrequency = tag.getFloat("volcano_frequency");
    planet.geodeFrequency = tag.getFloat("geode_frequency");
    strings(tag, "biomes").stream().map(ResourceLocation::parse).forEach(planet.biomes::add);
    CompoundTag weights = tag.getCompound("biome_weights");
    for (String biome : weights.getAllKeys())
      planet.biomeWeights.put(ResourceLocation.parse(biome), weights.getInt(biome));
    CompoundTag craterWeights = tag.getCompound("crater_biome_weights");
    for (String biome : craterWeights.getAllKeys())
      planet.craterBiomeWeights.put(ResourceLocation.parse(biome), craterWeights.getInt(biome));
    strings(tag, "terraformed_biomes").stream()
        .map(ResourceLocation::parse)
        .forEach(planet.terraformedBiomes::add);
    strings(tag, "gases").stream().map(ResourceLocation::parse).forEach(planet.gases::add);
    planet.artifacts.addAll(IdOrTag.load(tag.getList("artifacts", Tag.TAG_COMPOUND)));
    planet.laserOres.addAll(IdOrTag.load(tag.getList("laser_ores", Tag.TAG_COMPOUND)));
    planet.geodeOres.addAll(IdOrTag.load(tag.getList("geode_ores", Tag.TAG_COMPOUND)));
    planet.craterOres.addAll(IdOrTag.load(tag.getList("crater_ores", Tag.TAG_COMPOUND)));
    planet.laserDrillable = !tag.contains("laser_drillable") || tag.getBoolean("laser_drillable");
    return planet;
  }

  private static void putStrings(CompoundTag tag, String key, List<String> values) {
    ListTag list = new ListTag();
    values.forEach(value -> list.add(StringTag.valueOf(value)));
    tag.put(key, list);
  }

  private static List<String> strings(CompoundTag tag, String key) {
    return tag.getList(key, Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry;

import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Original discovery and terraforming defaults exposed as world/server settings. */
public final class AdvancedRocketryConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec CLIENT_SPEC;
  private static final ModConfigSpec.BooleanValue PLANETS_MUST_BE_DISCOVERED;
  private static final ModConfigSpec.IntValue PLANET_DISCOVERY_CHANCE;
  private static final ModConfigSpec.IntValue MAX_BIOMES_PER_PLANET;
  private static final ModConfigSpec.BooleanValue ALLOW_PLANET_RESPAWN;
  private static final ModConfigSpec.BooleanValue FORCE_PLANET_RESPAWN;
  private static final ModConfigSpec.BooleanValue ENABLE_TERRAFORMING;
  private static final ModConfigSpec.BooleanValue ALLOW_TERRAFORM_NON_AR;
  private static final ModConfigSpec.DoubleValue TERRAFORM_SPEED;
  private static final ModConfigSpec.BooleanValue TERRAFORM_REQUIRES_FLUID;
  private static final ModConfigSpec.IntValue TERRAFORM_FLUID_RATE;
  private static final ModConfigSpec.IntValue BIOME_UPDATE_SPEED;
  private static final ModConfigSpec.IntValue SOLAR_GENERATOR_MULTIPLIER;
  private static final ModConfigSpec.IntValue BLACK_HOLE_POWER_MULTIPLIER;
  private static final ModConfigSpec.IntValue BLACK_HOLE_DEFAULT_BURN_TIME;
  private static final ModConfigSpec.DoubleValue GAS_MISSION_MULTIPLIER;
  private static final ModConfigSpec.BooleanValue ELECTRIC_PLANTS_SPAWN_LIGHTNING;
  private static final ModConfigSpec.BooleanValue GENERATE_CRATERS;
  private static final ModConfigSpec.BooleanValue GENERATE_VOLCANOES;
  private static final ModConfigSpec.BooleanValue GENERATE_GEODES;
  private static final ModConfigSpec.IntValue GEODE_BASE_SIZE;
  private static final ModConfigSpec.IntValue GEODE_VARIATION;
  private static final ModConfigSpec.BooleanValue GENERATE_IRIDIUM;
  private static final ModConfigSpec.BooleanValue ENABLE_ORE_GEN;
  private static final Map<String, OreSettings> ORE_SETTINGS;
  private static final ModConfigSpec.BooleanValue GENERATE_VANILLA_STRUCTURES;
  private static final ModConfigSpec.BooleanValue ENABLE_GRAVITY_CONTROLLER;
  private static final ModConfigSpec.BooleanValue ENABLE_LASER_DRILL;
  private static final ModConfigSpec.BooleanValue LASER_DRILL_PLANET;
  private static final ModConfigSpec.DoubleValue CRYSTALLIZER_MAXIMUM_GRAVITY;
  private static final ModConfigSpec.BooleanValue LAUNCH_BLOCK_DESTRUCTION;
  private static final ModConfigSpec.IntValue FUEL_POINTS_PER_DILITHIUM;
  private static final ModConfigSpec.IntValue STATION_BUILD_RADIUS;
  private static final ModConfigSpec.BooleanValue ALLOW_ZERO_G_STATIONS;
  private static final ModConfigSpec.DoubleValue WARP_TRAVEL_TIME;
  private static final ModConfigSpec.DoubleValue MINING_MISSION_TIME_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue MICROWAVE_RECEIVER_MULTIPLIER;
  private static final ModConfigSpec.BooleanValue ROCKETS_REQUIRE_FUEL;
  private static final ModConfigSpec.BooleanValue EXPERIMENTAL_SPACE_FLIGHT;
  private static final ModConfigSpec.DoubleValue ROCKET_THRUST_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue FUEL_CAPACITY_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue BUILD_SPEED_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue JETPACK_THRUST;
  private static final ModConfigSpec.IntValue OXYGEN_VENT_SIZE;
  private static final ModConfigSpec.BooleanValue OXYGEN_VENT_VOLUME_BASED;
  private static final ModConfigSpec.BooleanValue ENABLE_OXYGEN;
  private static final ModConfigSpec.BooleanValue SCRUBBER_REQUIRES_CARTRIDGE;
  private static final ModConfigSpec.DoubleValue SUIT_TANK_CAPACITY;
  private static final ModConfigSpec.DoubleValue BLOCK_TANK_CAPACITY;
  private static final ModConfigSpec.DoubleValue OXYGEN_VENT_CONSUMPTION_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue OXYGEN_VENT_POWER_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue LASER_DRILL_POWER_MULTIPLIER;
  private static final ModConfigSpec.BooleanValue CAN_BE_FUELED_BY_HAND;
  private static final ModConfigSpec.BooleanValue GRAVITY_AFFECTS_FUEL;
  private static final ModConfigSpec.IntValue ORBIT_HEIGHT;
  private static final ModConfigSpec.IntValue STATION_CLEARANCE_HEIGHT;
  private static final ModConfigSpec.DoubleValue NUCLEAR_CORE_THRUST_RATIO;
  private static final ModConfigSpec.BooleanValue AUTOMATIC_RETRO_ROCKETS;
  private static final ModConfigSpec.IntValue TRANS_BODY_INJECTION;
  private static final ModConfigSpec.DoubleValue ASTEROID_TBI_BURN_MULTIPLIER;
  private static final ModConfigSpec.DoubleValue WARP_TBI_BURN_MULTIPLIER;
  private static final ModConfigSpec.BooleanValue LOW_GRAVITY_BOOTS;
  private static final ModConfigSpec.BooleanValue SAWMILL_VANILLA_WOOD;
  private static final ModConfigSpec.BooleanValue MAKE_MATERIALS_FOR_OTHER_MODS;
  private static final ModConfigSpec.BooleanValue PLANET_SKY_OVERRIDE;
  private static final ModConfigSpec.BooleanValue STATION_SKY_OVERRIDE;
  private static final ModConfigSpec.BooleanValue OVERWORLD_SKY_OVERRIDE;
  private static final ModConfigSpec.BooleanValue ADVANCED_VFX;
  private static final ModConfigSpec.BooleanValue DROP_EXTINGUISHED_TORCHES;
  private static final ModConfigSpec.IntValue SPACE_SUIT_OXYGEN_TIME;
  private static final ModConfigSpec.IntValue VACUUM_DAMAGE;
  private static final ModConfigSpec.BooleanValue ATMOSPHERIC_NAUSEA;
  private static final ModConfigSpec.BooleanValue BLACKLIST_VANILLA_BIOMES;
  private static final ModConfigSpec.BooleanValue RESET_PLANETS_FROM_DEFINITIONS;
  private static final ModConfigSpec.BooleanValue RESET_PLANETS_ONLY_ONCE;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    builder.push("planets");
    PLANETS_MUST_BE_DISCOVERED = builder.define("planetsMustBeDiscovered", false);
    PLANET_DISCOVERY_CHANCE = builder.defineInRange("planetDiscoveryChance", 5, 1, 1000);
    MAX_BIOMES_PER_PLANET = builder.defineInRange("maxBiomesPerPlanet", 5, -1, 128);
    RESET_PLANETS_FROM_DEFINITIONS = builder.define("resetPlanetsFromDefinitions", false);
    RESET_PLANETS_ONLY_ONCE = builder.define("ResetOnlyOnce", true);
    BLACKLIST_VANILLA_BIOMES = builder.define("blackListVanillaBiomes", false);
    ALLOW_PLANET_RESPAWN = builder.define("allowPlanetRespawn", false);
    FORCE_PLANET_RESPAWN = builder.define("forcePlanetRespawn", false);
    builder.pop();
    builder.push("terraforming");
    ENABLE_TERRAFORMING = builder.define("enableTerraforming", true);
    ALLOW_TERRAFORM_NON_AR = builder.define("allowTerraformingNonARWorlds", false);
    TERRAFORM_SPEED = builder.defineInRange("terraformSpeed", 1d, .01d, 100d);
    TERRAFORM_REQUIRES_FLUID = builder.define("terraformRequiresFluid", true);
    TERRAFORM_FLUID_RATE = builder.defineInRange("terraformFluidRate", 40, 0, 10000);
    BIOME_UPDATE_SPEED = builder.defineInRange("biomeUpdateSpeed", 1, 1, 256);
    builder.pop();
    builder.push("power");
    SOLAR_GENERATOR_MULTIPLIER = builder.defineInRange("solarGeneratorMultiplier", 1, 0, 1000);
    BLACK_HOLE_POWER_MULTIPLIER = builder.defineInRange("blackHoleGeneratorMultiplier", 1, 0, 1000);
    BLACK_HOLE_DEFAULT_BURN_TIME =
        builder.defineInRange("blackHoleDefaultBurnTime", 500, 1, 1000000);
    MICROWAVE_RECEIVER_MULTIPLIER =
        builder.defineInRange("microwaveReceiverMultiplier", 1d, 0d, 1000d);
    LASER_DRILL_POWER_MULTIPLIER =
        builder.defineInRange("laserDrillPowerMultiplier", 1d, .01d, 100d);
    builder.pop();
    builder.push("missions");
    GAS_MISSION_MULTIPLIER = builder.defineInRange("gasMissionMultiplier", 1d, .01d, 100d);
    MINING_MISSION_TIME_MULTIPLIER =
        builder.defineInRange("miningMissionTimeMultiplier", 1d, .01d, 100d);
    builder.pop();
    builder.push("stations");
    FUEL_POINTS_PER_DILITHIUM = builder.defineInRange("pointsPerDilithium", 500, 1, 1000);
    STATION_BUILD_RADIUS = builder.defineInRange("SpaceStationBuildRadius", 1024, 128, 4096);
    ALLOW_ZERO_G_STATIONS = builder.define("allowZeroGSpacestations", false);
    WARP_TRAVEL_TIME = builder.defineInRange("warpTravelTime", 1d, .01d, 100d);
    builder.pop();
    builder.push("worldgen");
    ELECTRIC_PLANTS_SPAWN_LIGHTNING = builder.define("electricPlantsSpawnLightning", true);
    GENERATE_CRATERS = builder.define("generateCraters", true);
    GENERATE_VOLCANOES = builder.define("generateVolcanos", true);
    GENERATE_GEODES = builder.define("generateGeodes", true);
    GEODE_BASE_SIZE = builder.defineInRange("geodeBaseSize", 36, 4, 128);
    GEODE_VARIATION = builder.defineInRange("geodeVariation", 24, 1, 128);
    GENERATE_IRIDIUM = builder.define("generateIridium", false);
    GENERATE_VANILLA_STRUCTURES = builder.define("generateVanillaStructures", false);
    builder.pop();
    builder.push("ore_generation");
    ENABLE_ORE_GEN = builder.define("EnableOreGen", true);
    Map<String, OreSettings> ores = new LinkedHashMap<>();
    ores.put("copper", oreSettings(builder, "GenerateCopper", true, "Copper", 10, 6, null));
    ores.put("tin", oreSettings(builder, "GenerateTin", true, "Tin", 10, 6, null));
    ores.put("aluminum", oreSettings(builder, "generateAluminum", true, "Aluminum", 1, 16, null));
    ores.put("titanium", oreSettings(builder, "GenerateTitanium", true, "Titanium", 6, 6, null));
    ores.put("dilithium", oreSettings(builder, "generateDilithium", true, "Dilithium", 1, 16, 10));
    ores.put(
        "iridium",
        new OreSettings(
            GENERATE_IRIDIUM,
            builder.defineInRange("IridiumPerChunk", 1, 0, 128),
            builder.defineInRange("IridiumPerClump", 16, 1, 64),
            null));
    ORE_SETTINGS = Map.copyOf(ores);
    builder.pop();
    builder.push("machines");
    ENABLE_GRAVITY_CONTROLLER = builder.define("enableGravityMachine", true);
    ENABLE_LASER_DRILL = builder.define("enableLaserDrill", true);
    LASER_DRILL_PLANET = builder.define("laserDrillPlanet", false);
    CRYSTALLIZER_MAXIMUM_GRAVITY =
        builder.defineInRange("crystalliserMaximumGravity", 0d, 0d, 100d);
    SAWMILL_VANILLA_WOOD = builder.define("sawMillCutVanillaWood", true);
    MAKE_MATERIALS_FOR_OTHER_MODS = builder.define("makeMaterialsForOtherMods", true);
    builder.pop();
    builder.push("rockets");
    LAUNCH_BLOCK_DESTRUCTION = builder.define("launchBlockDestruction", false);
    ROCKETS_REQUIRE_FUEL = builder.define("rocketsRequireFuel", true);
    EXPERIMENTAL_SPACE_FLIGHT = builder.define("experimentalSpaceFlight", false);
    ROCKET_THRUST_MULTIPLIER = builder.defineInRange("thrustMultiplier", 1d, .01d, 100d);
    FUEL_CAPACITY_MULTIPLIER = builder.defineInRange("fuelCapacityMultiplier", 1d, .01d, 100d);
    BUILD_SPEED_MULTIPLIER = builder.defineInRange("buildSpeedMultiplier", 1d, .01d, 100d);
    GRAVITY_AFFECTS_FUEL = builder.define("gravityAffectsFuels", true);
    CAN_BE_FUELED_BY_HAND = builder.define("canBeFueledByHand", true);
    ORBIT_HEIGHT = builder.defineInRange("orbitHeight", 1000, 256, 100000);
    STATION_CLEARANCE_HEIGHT = builder.defineInRange("stationClearance", 1000, 256, 100000);
    NUCLEAR_CORE_THRUST_RATIO = builder.defineInRange("nuclearCoreThrustRatio", 1d, 0d, 100d);
    AUTOMATIC_RETRO_ROCKETS = builder.define("autoRetroRockets", true);
    TRANS_BODY_INJECTION = builder.defineInRange("transBodyInjection", 0, 0, 1000000);
    ASTEROID_TBI_BURN_MULTIPLIER = builder.defineInRange("asteroidTBIBurnMult", 1d, 0d, 100d);
    WARP_TBI_BURN_MULTIPLIER = builder.defineInRange("warpTBIBurnMult", 10d, 0d, 100d);
    builder.pop();
    builder.push("life_support");
    JETPACK_THRUST = builder.defineInRange("jetPackForce", 1.3d, 0d, 10d);
    OXYGEN_VENT_SIZE = builder.defineInRange("oxygenVentSize", 32, 1, 128);
    OXYGEN_VENT_VOLUME_BASED = builder.define("oxygenVentVolumeBased", true);
    ENABLE_OXYGEN = builder.define("enableAtmosphericEffects", true);
    VACUUM_DAMAGE = builder.defineInRange("vacuumDamage", 1, 0, 100);
    ATMOSPHERIC_NAUSEA = builder.define("enableAtmosphericNausea", true);
    LOW_GRAVITY_BOOTS = builder.define("lowGravityBoots", false);
    DROP_EXTINGUISHED_TORCHES = builder.define("dropExtinguishedTorches", false);
    SPACE_SUIT_OXYGEN_TIME = builder.defineInRange("spaceSuitO2Buffer", 30, 0, 100000);
    SCRUBBER_REQUIRES_CARTRIDGE = builder.define("scrubberRequiresCartridge", true);
    SUIT_TANK_CAPACITY = builder.defineInRange("suitTankCapacity", 1d, 0d, 100d);
    BLOCK_TANK_CAPACITY = builder.defineInRange("blockTankCapacity", 1d, 0d, 100d);
    OXYGEN_VENT_CONSUMPTION_MULTIPLIER =
        builder.defineInRange("oxygenVentConsumptionMultiplier", 1d, 0d, 100d);
    OXYGEN_VENT_POWER_MULTIPLIER = builder.defineInRange("oxygenVentPowerMultiplier", 1d, 0d, 100d);
    builder.pop();
    SPEC = builder.build();

    ModConfigSpec.Builder client = new ModConfigSpec.Builder();
    client.push("client_visuals");
    PLANET_SKY_OVERRIDE = client.define("PlanetSkyOverride", true);
    STATION_SKY_OVERRIDE = client.define("StationSkyOverride", true);
    OVERWORLD_SKY_OVERRIDE = client.define("overworldSkyOverride", true);
    ADVANCED_VFX = client.define("advancedVFX", true);
    client.pop();
    CLIENT_SPEC = client.build();
  }

  private AdvancedRocketryConfig() {}

  private static <T> T get(ModConfigSpec.ConfigValue<T> value) {
    return SPEC.isLoaded() ? value.get() : value.getDefault();
  }

  private static <T> T client(ModConfigSpec.ConfigValue<T> value) {
    return CLIENT_SPEC.isLoaded() ? value.get() : value.getDefault();
  }

  private record OreSettings(
      ModConfigSpec.BooleanValue enabled,
      ModConfigSpec.IntValue count,
      ModConfigSpec.IntValue clump,
      ModConfigSpec.IntValue moonCount) {}

  private static OreSettings oreSettings(
      ModConfigSpec.Builder builder,
      String toggle,
      boolean enabled,
      String name,
      int count,
      int clump,
      Integer moonCount) {
    return new OreSettings(
        builder.define(toggle, enabled),
        builder.defineInRange(name + "PerChunk", count, 0, 128),
        builder.defineInRange(name + "PerClump", clump, 1, 64),
        moonCount == null ? null : builder.defineInRange(name + "PerChunkLuna", moonCount, 0, 128));
  }

  public static boolean oreEnabled(String ore) {
    OreSettings settings = ORE_SETTINGS.get(ore);
    return settings != null && get(ENABLE_ORE_GEN) && get(settings.enabled);
  }

  public static int orePerChunk(String ore, boolean airless) {
    OreSettings settings = ORE_SETTINGS.get(ore);
    if (settings == null || !oreEnabled(ore)) return 0;
    return get(airless && settings.moonCount != null ? settings.moonCount : settings.count);
  }

  public static int oreClumpSize(String ore) {
    OreSettings settings = ORE_SETTINGS.get(ore);
    return settings == null ? 0 : get(settings.clump);
  }

  public static boolean planetsMustBeDiscovered() {
    return get(PLANETS_MUST_BE_DISCOVERED);
  }

  public static int planetDiscoveryChance() {
    return get(PLANET_DISCOVERY_CHANCE);
  }

  public static int maxBiomesPerPlanet() {
    return get(MAX_BIOMES_PER_PLANET);
  }

  public static boolean blacklistVanillaBiomes() {
    return get(BLACKLIST_VANILLA_BIOMES);
  }

  public static boolean resetPlanetsFromDefinitions() {
    return get(RESET_PLANETS_FROM_DEFINITIONS);
  }

  public static boolean resetPlanetsOnlyOnce() {
    return get(RESET_PLANETS_ONLY_ONCE);
  }

  public static void consumePlanetReset() {
    RESET_PLANETS_FROM_DEFINITIONS.set(false);
    SPEC.save();
  }

  public static boolean allowPlanetRespawn() {
    return get(ALLOW_PLANET_RESPAWN);
  }

  public static boolean forcePlanetRespawn() {
    return get(FORCE_PLANET_RESPAWN);
  }

  public static boolean enableTerraforming() {
    return get(ENABLE_TERRAFORMING);
  }

  public static boolean allowTerraformNonAR() {
    return get(ALLOW_TERRAFORM_NON_AR);
  }

  public static int terraformDuration() {
    return Math.max(1, (int) Math.round(18000 * get(TERRAFORM_SPEED)));
  }

  public static boolean terraformRequiresFluid() {
    return get(TERRAFORM_REQUIRES_FLUID);
  }

  public static int terraformFluidRate() {
    return get(TERRAFORM_FLUID_RATE);
  }

  public static int biomeUpdateSpeed() {
    return get(BIOME_UPDATE_SPEED);
  }

  public static boolean allowZeroGStations() {
    return get(ALLOW_ZERO_G_STATIONS);
  }

  public static boolean sawmillVanillaWood() {
    return get(SAWMILL_VANILLA_WOOD);
  }

  public static boolean makeMaterialsForOtherMods() {
    return get(MAKE_MATERIALS_FOR_OTHER_MODS);
  }

  public static boolean skyOverride(boolean station) {
    return client(station ? STATION_SKY_OVERRIDE : PLANET_SKY_OVERRIDE);
  }

  public static boolean overworldSkyOverride() {
    return client(OVERWORLD_SKY_OVERRIDE);
  }

  public static boolean advancedVfx() {
    return client(ADVANCED_VFX);
  }

  public static int solarGeneratorMultiplier() {
    return get(SOLAR_GENERATOR_MULTIPLIER);
  }

  public static int blackHolePowerMultiplier() {
    return get(BLACK_HOLE_POWER_MULTIPLIER);
  }

  public static int blackHoleDefaultBurnTime() {
    return get(BLACK_HOLE_DEFAULT_BURN_TIME);
  }

  public static double gasMissionMultiplier() {
    return get(GAS_MISSION_MULTIPLIER);
  }

  public static boolean electricPlantsSpawnLightning() {
    return get(ELECTRIC_PLANTS_SPAWN_LIGHTNING);
  }

  public static boolean generateCraters() {
    return get(GENERATE_CRATERS);
  }

  public static boolean generateVolcanoes() {
    return get(GENERATE_VOLCANOES);
  }

  public static boolean generateGeodes() {
    return get(GENERATE_GEODES);
  }

  public static int geodeBaseSize() {
    return get(GEODE_BASE_SIZE);
  }

  public static int geodeVariation() {
    return get(GEODE_VARIATION);
  }

  public static boolean generateVanillaStructures() {
    return get(GENERATE_VANILLA_STRUCTURES);
  }

  public static boolean enableGravityController() {
    return get(ENABLE_GRAVITY_CONTROLLER);
  }

  public static boolean enableLaserDrill() {
    return get(ENABLE_LASER_DRILL);
  }

  public static boolean laserDrillPlanet() {
    return get(LASER_DRILL_PLANET);
  }

  public static double crystallizerMaximumGravity() {
    return get(CRYSTALLIZER_MAXIMUM_GRAVITY);
  }

  public static boolean launchBlockDestruction() {
    return get(LAUNCH_BLOCK_DESTRUCTION);
  }

  public static int fuelPointsPerDilithium() {
    return get(FUEL_POINTS_PER_DILITHIUM);
  }

  public static int stationBuildRadius() {
    return get(STATION_BUILD_RADIUS);
  }

  public static double warpTravelTime() {
    return get(WARP_TRAVEL_TIME);
  }

  public static double miningMissionTimeMultiplier() {
    return get(MINING_MISSION_TIME_MULTIPLIER);
  }

  public static double microwaveReceiverMultiplier() {
    return get(MICROWAVE_RECEIVER_MULTIPLIER);
  }

  public static boolean rocketsRequireFuel() {
    return get(ROCKETS_REQUIRE_FUEL);
  }

  public static boolean experimentalSpaceFlight() {
    return get(EXPERIMENTAL_SPACE_FLIGHT);
  }

  public static double rocketThrustMultiplier() {
    return get(ROCKET_THRUST_MULTIPLIER);
  }

  public static double fuelCapacityMultiplier() {
    return get(FUEL_CAPACITY_MULTIPLIER);
  }

  public static double buildSpeedMultiplier() {
    return get(BUILD_SPEED_MULTIPLIER);
  }

  public static double jetpackThrust() {
    return get(JETPACK_THRUST);
  }

  public static int oxygenVentSize() {
    return get(OXYGEN_VENT_SIZE);
  }

  public static boolean oxygenVentVolumeBased() {
    return get(OXYGEN_VENT_VOLUME_BASED);
  }

  public static boolean enableOxygen() {
    return get(ENABLE_OXYGEN);
  }

  public static int vacuumDamage() {
    return get(VACUUM_DAMAGE);
  }

  public static boolean atmosphericNausea() {
    return get(ATMOSPHERIC_NAUSEA);
  }

  public static boolean lowGravityBoots() {
    return get(LOW_GRAVITY_BOOTS);
  }

  public static boolean dropExtinguishedTorches() {
    return get(DROP_EXTINGUISHED_TORCHES);
  }

  public static int sealedArmorAirCapacity() {
    return get(SPACE_SUIT_OXYGEN_TIME) * 1200;
  }

  public static boolean scrubberRequiresCartridge() {
    return get(SCRUBBER_REQUIRES_CARTRIDGE);
  }

  public static double suitTankCapacityMultiplier() {
    return get(SUIT_TANK_CAPACITY);
  }

  public static int blockTankCapacity() {
    return Math.max(0, (int) Math.round(64000 * get(BLOCK_TANK_CAPACITY)));
  }

  public static double oxygenVentConsumptionMultiplier() {
    return get(OXYGEN_VENT_CONSUMPTION_MULTIPLIER);
  }

  public static double oxygenVentPowerMultiplier() {
    return get(OXYGEN_VENT_POWER_MULTIPLIER);
  }

  public static int laserDrillEnergy() {
    return Math.max(1, (int) Math.round(10000 * get(LASER_DRILL_POWER_MULTIPLIER)));
  }

  public static boolean canBeFueledByHand() {
    return get(CAN_BE_FUELED_BY_HAND);
  }

  public static boolean gravityAffectsFuel() {
    return get(GRAVITY_AFFECTS_FUEL);
  }

  public static int orbitHeight() {
    return get(ORBIT_HEIGHT);
  }

  public static int stationClearanceHeight() {
    return get(STATION_CLEARANCE_HEIGHT);
  }

  public static double nuclearCoreThrustRatio() {
    return get(NUCLEAR_CORE_THRUST_RATIO);
  }

  public static boolean automaticRetroRockets() {
    return get(AUTOMATIC_RETRO_ROCKETS);
  }

  public static int transBodyInjection() {
    return get(TRANS_BODY_INJECTION);
  }

  public static double asteroidTbiBurnMultiplier() {
    return get(ASTEROID_TBI_BURN_MULTIPLIER);
  }

  public static double warpTbiBurnMultiplier() {
    return get(WARP_TBI_BURN_MULTIPLIER);
  }
}

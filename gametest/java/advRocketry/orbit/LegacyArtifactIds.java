// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Original XML item/metadata IDs resolved to their native registry variants. */
public final class LegacyArtifactIds {
  private static final String[] WOOL = {
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black"
  };
  private static final String[] DYES = {
    "ink_sac",
    "red_dye",
    "green_dye",
    "cocoa_beans",
    "lapis_lazuli",
    "purple_dye",
    "cyan_dye",
    "light_gray_dye",
    "gray_dye",
    "pink_dye",
    "lime_dye",
    "yellow_dye",
    "light_blue_dye",
    "magenta_dye",
    "orange_dye",
    "bone_meal"
  };

  private LegacyArtifactIds() {}

  public static ResourceLocation resolve(ResourceLocation original, int metadata) {
    String namespace = original.getNamespace();
    String name = original.getPath();
    if ((namespace.equals("libvulpes") || namespace.equals("advancedrocketry"))
        && (name.startsWith("product") || name.equals("metal0") || name.equals("coil0"))) {
      String[] materials =
          namespace.equals("libvulpes")
              ? new String[] {
                "dilithium",
                "iron",
                "gold",
                "silicon",
                "copper",
                "tin",
                "steel",
                "titanium",
                "rutile",
                "aluminum",
                "iridium"
              }
              : new String[] {"titanium_aluminide", "titanium_iridium"};
      if (metadata < 0 || metadata >= materials.length) return null;
      String product =
          name.equals("metal0") ? "block" : name.equals("coil0") ? "coil" : name.substring(7);
      if (product.equals("gem")) product = "crystal";
      String material = materials[metadata];
      ResourceLocation resolved =
          ResourceLocation.fromNamespaceAndPath(Main.MODID, material + "_" + product);
      if (material.equals("copper") && (product.equals("ingot") || product.equals("block")))
        resolved = ResourceLocation.withDefaultNamespace("copper_" + product);
      return BuiltInRegistries.ITEM.containsKey(resolved) ? resolved : null;
    }
    if (namespace.equals("libvulpes") && name.equals("ore0")) {
      String ore =
          switch (metadata) {
            case 0 -> Main.MODID + ":dilithium_ore";
            case 4 -> "minecraft:copper_ore";
            case 5 -> Main.MODID + ":tin_ore";
            case 8 -> Main.MODID + ":titanium_ore";
            case 9 -> Main.MODID + ":aluminum_ore";
            case 10 -> Main.MODID + ":iridium_ore";
            default -> null;
          };
      return ore == null ? null : ResourceLocation.parse(ore);
    }
    if (namespace.equals("minecraft")) {
      if (name.equals("wool") && metadata >= 0 && metadata < WOOL.length)
        return ResourceLocation.withDefaultNamespace(WOOL[metadata] + "_wool");
      if (name.equals("dye") && metadata >= 0 && metadata < DYES.length)
        return ResourceLocation.withDefaultNamespace(DYES[metadata]);
      if (name.equals("coal") && metadata == 1)
        return ResourceLocation.withDefaultNamespace("charcoal");
      if (metadata != 0) return null;
      name =
          switch (name) {
            case "netherbrick" -> "nether_brick";
            case "brick_block" -> "bricks";
            case "stonebrick" -> "stone_bricks";
            default -> name;
          };
      return ResourceLocation.withDefaultNamespace(name);
    }
    if (!namespace.equals("advancedrocketry") && !namespace.equals("libvulpes"))
      return metadata == 0 ? original : null;
    String variant =
        switch (name + "#" + metadata) {
          case "battery#0" -> "battery";
          case "battery#1" -> "advanced_battery";
          case "crystal#0" -> "amethyst_crystal_block";
          case "crystal#1" -> "sapphire_crystal_block";
          case "crystal#2" -> "emerald_crystal_block";
          case "crystal#3" -> "ruby_crystal_block";
          case "crystal#4" -> "citrine_crystal_block";
          case "crystal#5" -> "wulfentite_crystal_block";
          case "hatch#0" -> "block_item_input_block";
          case "hatch#1" -> "block_item_output_block";
          case "hatch#2" -> "block_fluid_input_block";
          case "hatch#3" -> "block_fluid_output_block";
          case "ic#0" -> "basic_circuit";
          case "ic#1" -> "tracking_circuit";
          case "ic#2" -> "advanced_circuit";
          case "ic#3" -> "control_circuit_board";
          case "ic#4" -> "item_io_circuit_board";
          case "ic#5" -> "fluid_io_circuit_board";
          case "itemcircuitplate#0" -> "basic_circuit_plate";
          case "itemcircuitplate#1" -> "advanced_circuit_plate";
          case "itemupgrade#0" -> "hover_upgrade";
          case "itemupgrade#1" -> "flight_speed_upgrade";
          case "itemupgrade#2" -> "legs_upgrade";
          case "itemupgrade#3" -> "padded_boots_upgrade";
          case "itemupgrade#4" -> "atmosphere_upgrade";
          case "itemupgrade#5" -> "night_vision_upgrade";
          case "loader#0" -> "data_bus";
          case "loader#1" -> "satellite_hatch";
          case "loader#2" -> "rocket_item_unloader";
          case "loader#3" -> "rocket_item_loader";
          case "loader#4" -> "rocket_fluid_unloader";
          case "loader#5" -> "rocket_fluid_loader";
          case "loader#6" -> "guidance_access_hatch";
          case "misc#0" -> "user_interface";
          case "misc#1" -> "carbon_brick";
          case "pressuretank#0" -> "portable_pressure_tank_aluminum";
          case "pressuretank#1" -> "portable_pressure_tank_steel";
          case "pressuretank#2" -> "portable_pressure_tank_titanium";
          case "pressuretank#3" -> "portable_pressure_tank_iridium";
          case "satellitepowersource#0" -> "basic_satellite_solar_panel";
          case "satellitepowersource#1" -> "large_satellite_solar_panel";
          case "satelliteprimaryfunction#0" -> "optical_sensor";
          case "satelliteprimaryfunction#1" -> "composition_sensor";
          case "satelliteprimaryfunction#2" -> "mass_detector";
          case "satelliteprimaryfunction#3" -> "microwave_transmitter";
          case "satelliteprimaryfunction#4" -> "ore_mapper";
          case "satelliteprimaryfunction#5" -> "satellite_biome_changer";
          default -> null;
        };
    if (variant != null) return ResourceLocation.fromNamespaceAndPath(Main.MODID, variant);
    if (metadata != 0) return null;
    String resolved =
        switch (name) {
          case "advancedmotor" -> "advanced_motor";
          case "advbipropellantrocketmotor" -> "advanced_bipropellant_rocket_motor";
          case "advrocketmotor" -> "advanced_rocket_motor";
          case "advstructuremachine" -> "block_advanced_structure_block";
          case "airlock_door" -> "airlock_door";
          case "alienwood" -> "alien_log";
          case "altitudecontroller" -> "station_altitude_controller";
          case "arcfurnace" -> "electric_arc_furnace";
          case "asteroidchip" -> "asteroid_chip";
          case "atmanalyser" -> "atm_analyzer";
          case "batteryrf" -> "block_energy_input_block";
          case "beacon" -> "beacon";
          case "beaconfinder" -> "beacon_finder";
          case "biomechanger" -> "biome_changer_remote";
          case "biomescanner" -> "biome_scanner";
          case "bipropellantfueltank" -> "bipropellant_fuel_tank";
          case "bipropellantrocketmotor" -> "bipropellant_rocket_motor";
          case "blackholegenerator" -> "black_hole_generator";
          case "blastbrick" -> "blast_brick";
          case "blocklens" -> "lens_block";
          case "blockpump" -> "fluid_pump";
          case "carbonscrubbercartridge" -> "carbon_scrubber_cartridge";
          case "centrifuge" -> "centrifuge";
          case "chemicalreactor" -> "chemical_reactor";
          case "circlelight" -> "circle_light";
          case "coalgenerator" -> "coal_generator";
          case "concrete" -> "concrete";
          case "crystallizer" -> "crystallizer";
          case "cuttingmachine" -> "cutting_machine";
          case "databus" -> "data_bus";
          case "dataunit" -> "item_data_storage";
          case "deployablerocketbuilder" -> "deployable_rocket_builder";
          case "drill" -> "mining_drill";
          case "electrolyser" -> "electrolyzer";
          case "elevatorchip" -> "elevator_chip";
          case "elitemotor" -> "elite_motor";
          case "enhancedmotor" -> "enhanced_motor";
          case "fluidinputhatch" -> "block_fluid_input_block";
          case "fluidoutputhatch" -> "block_fluid_output_block";
          case "forcefieldprojector" -> "force_field_projector";
          case "forgepowerinput" -> "block_energy_input_block";
          case "forgepoweroutput" -> "block_energy_output_block";
          case "fuelingstation" -> "fueling_station";
          case "fueltank" -> "fuel_tank";
          case "gravitycontroller" -> "station_gravity_controller";
          case "gravitymachine" -> "area_gravity_controller";
          case "guidancecomputer" -> "guidance_computer";
          case "holoprojector" -> "item_holoprojector";
          case "hovercraft" -> "hovercraft";
          case "inputhatch" -> "block_item_input_block";
          case "intake" -> "gas_intake";
          case "iquartzcrucible" -> "quartz_crucible";
          case "jackhammer" -> "jackhammer";
          case "jetpack" -> "jetpack";
          case "landingpad" -> "landing_pad";
          case "lathe" -> "lathe";
          case "launchpad" -> "launch_pad";
          case "lens" -> "basic_lens";
          case "linker" -> "linker";
          case "liquidtank" -> "liquid_tank";
          case "microwavereciever" -> "microwave_receiver";
          case "monitoringstation" -> "monitoring_station";
          case "motor" -> "block_motor_block";
          case "nuclearcore" -> "nuclear_core";
          case "nuclearfueltank" -> "nuclear_fuel_tank";
          case "nuclearrocketmotor" -> "nuclear_rocket_motor";
          case "observatory" -> "observatory";
          case "orescanner" -> "ore_scanner";
          case "orientationcontroller" -> "station_orientation_controller";
          case "outputhatch" -> "block_item_output_block";
          case "oxidizerfueltank" -> "oxidizer_fuel_tank";
          case "oxygencharger" -> "oxygen_charger";
          case "oxygendetection" -> "atmosphere_detector";
          case "oxygenscrubber" -> "carbon_scrubber";
          case "oxygenvent" -> "oxygen_vent";
          case "pipesealer" -> "pipe_sealer";
          case "planetanalyser" -> "astrobody_data_processor";
          case "planetholoselector" -> "holographic_planet_selector";
          case "planetidchip" -> "planet_id_chip";
          case "planks" -> "alien_planks";
          case "platepress" -> "plate_press";
          case "precisionassemblingmachine" -> "precision_assembler";
          case "precisionlaseretcher" -> "precision_laser_etcher";
          case "qcrucible" -> "quartz_crucible";
          case "railgun" -> "railgun";
          case "rocketbuilder" -> "rocket_builder";
          case "rocketmotor" -> "rocket_motor";
          case "rollingmachine" -> "rolling_machine";
          case "satellite" -> "satellite";
          case "satellitebuilder" -> "satellite_builder";
          case "satellitecontrolcenter" -> "satellite_terminal";
          case "satelliteidchip" -> "satellite_id_chip";
          case "sawblade" -> "saw_blade_assembly";
          case "sawbladeiron" -> "iron_saw_blade";
          case "sealdetector" -> "seal_detector";
          case "seat" -> "seat";
          case "smallairlockdoor" -> "airlock_door";
          case "smallplatepress" -> "plate_press";
          case "solararray" -> "solar_array";
          case "solararraypanel" -> "solar_array_panel";
          case "solargenerator" -> "solar_panel";
          case "solarpanel" -> "solar_cell";
          case "spaceboots" -> "space_boots";
          case "spacechestplate" -> "space_chestplate";
          case "spaceelevatorcontroller" -> "space_elevator";
          case "spacehelmet" -> "space_helmet";
          case "spacelaser" -> "orbital_laser";
          case "spaceleggings" -> "space_leggings";
          case "spacestation" -> "space_station";
          case "spacestationchip" -> "station_id_chip";
          case "stationbuilder" -> "station_builder";
          case "stationmarker" -> "docking_port";
          case "structuremachine" -> "block_structure_block";
          case "structuretower" -> "structure_tower";
          case "suitworkstation" -> "suit_workstation";
          case "terraformer" -> "atmosphere_terraformer";
          case "thermite" -> "thermite";
          case "thermitetorch" -> "thermite_torch";
          case "vacuumlaser" -> "vacuum_laser";
          case "wafer" -> "silicon_wafer";
          case "warpcore" -> "warp_core";
          case "warpmonitor" -> "warp_controller";
          case "wirelesstransciever" -> "wireless_transceiver";
          default -> name;
        };
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, resolved);
  }
}

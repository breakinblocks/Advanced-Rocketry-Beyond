package advRocketry.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModItemModels extends ModItemModelHelpers {

  ModItemModels(PackOutput output, ExistingFileHelper files) {
    super(output, files);
  }

  @Override
  protected void registerModels() {
    model(
        "item/advanced_battery", "minecraft:item/generated", null, true, "layer0", "item/battery1");
    model(
        "item/advanced_bipropellant_rocket_motor",
        "block/advanced_bipropellant_rocket_motor",
        null,
        true);
    model(
        "item/advanced_circuit",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/advancedcircuit");
    model(
        "item/advanced_circuit_plate",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/advancedcircuitplate");
    model("item/advanced_motor", "block/advanced_motor", null, true);
    model("item/advanced_rocket_motor", "block/advanced_rocket_motor", null, true);
    model(
        "item/airlock_door", "minecraft:item/generated", null, true, "layer0", "item/smallairlock");
    model("item/alien_leaves", "block/alien_leaves", null, true);
    model("item/alien_log", "block/alien_log", null, true);
    model("item/alien_planks", "block/alien_planks", null, true);
    model("item/alien_sapling", "block/alien_sapling", null, true);
    model("item/aluminum_block", "block/aluminum_block", null, true);
    model("item/aluminum_coil", "block/aluminum_coil", null, true);
    model("item/aluminum_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/aluminum_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/aluminum_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/aluminum_ore", "block/aluminum_ore", null, true);
    model("item/aluminum_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/aluminum_sheet", "minecraft:item/generated", null, true, "layer0", "item/sheet");
    model("item/amethyst_crystal_block", "block/amethyst_crystal_block", null, true);
    bucket("item/ammonia_bucket", "adv_rocketry:ammonia");
    model(
        "item/area_gravity_controller",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/solar");
    model(
        "item/asteroid_chip",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/asteroididchip");
    model(
        "item/astrobody_data_processor",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/solar");
    model(
        "item/atm_analyzer", "minecraft:item/generated", null, true, "layer0", "item/atmanalyzer");
    model("item/atmosphere_detector", "block/atmosphere_detector", null, true);
    model(
        "item/atmosphere_terraformer",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/solar");
    model(
        "item/atmosphere_upgrade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/antifogvisor");
    model("item/basalt", "block/basalt", null, true);
    model(
        "item/basic_circuit",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/basiccircuit");
    model(
        "item/basic_circuit_plate",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/basiccircuitplate");
    model("item/basic_lens", "minecraft:item/generated", null, true, "layer0", "item/basiclens");
    model(
        "item/basic_satellite_solar_panel",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satellitepowersource0");
    model("item/battery", "minecraft:item/generated", null, true, "layer0", "item/battery0");
    model("item/beacon", "minecraft:block/cube_all", null, true, "all", "block/beacon");
    model(
        "item/beacon_finder",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/beaconfinder");
    model(
        "item/biome_changer_remote",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/biomechanger");
    model("item/biome_scanner", "minecraft:block/cube_all", null, true, "all", "block/solar");
    model("item/bipropellant_fuel_tank", "block/bipropellant_fuel_tank", null, true);
    model("item/bipropellant_rocket_motor", "block/bipropellant_rocket_motor", null, true);
    model(
        "item/black_hole_generator",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machinegeneric");
    model("item/blast_brick", "block/blast_brick", null, true);
    model(
        "item/block_advanced_structure_block", "block/block_advanced_structure_block", null, true);
    model("item/block_coilcopper", "block/block_coilcopper", null, true);
    model("item/block_energy_input_block", "block/block_energy_input_block", null, true);
    model("item/block_energy_output_block", "block/block_energy_output_block", null, true);
    model("item/block_fluid_input_block", "block/block_fluid_input_block", null, true);
    model("item/block_fluid_output_block", "block/block_fluid_output_block", null, true);
    model("item/block_item_input_block", "block/block_item_input_block", null, true);
    model("item/block_item_output_block", "block/block_item_output_block", null, true);
    model("item/block_motor_block", "block/block_motor_block", null, true);
    model("item/block_structure_block", "block/block_structure_block", null, true);
    model(
        "item/carbon_brick", "minecraft:item/generated", null, true, "layer0", "item/carbonbrick");
    model("item/carbon_scrubber", "block/carbon_scrubber", null, true);
    model(
        "item/carbon_scrubber_cartridge",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/carbonscrubbercartrige");
    model("item/centrifuge", "block/centrifuge", null, true);
    model("item/centrifuge_casing", "block/centrifuge_casing", null, true);
    model("item/charcoal_log", "block/charcoal_log", null, true);
    model("item/chemical_reactor", "block/chemical_reactor", null, true);
    model("item/circle_light", "block/circle_light", null, true);
    model("item/citrine_crystal_block", "block/citrine_crystal_block", null, true);
    model("item/coal_generator", "block/coal_generator", null, true);
    model(
        "item/composition_sensor",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction1");
    model("item/concrete", "block/concrete", null, true);
    model(
        "item/control_circuit_board",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/controliocircuit");
    model("item/copper_coil", "block/copper_coil", null, true);
    model("item/copper_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/copper_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/copper_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/copper_rod", "minecraft:item/generated", null, true, "layer0", "item/stick");
    model("item/copper_sheet", "minecraft:item/generated", null, true, "layer0", "item/sheet");
    model("item/creative_energy_input", "block/creative_energy_input", null, true);
    model("item/crystallizer", "block/crystallizer", null, true);
    model("item/cutting_machine", "block/cutting_machine", null, true);
    model("item/data_bus", "block/data_bus", null, true);
    model("item/deployable_rocket_builder", "block/deployable_rocket_builder", null, true);
    model(
        "item/dilithium_crystal", "minecraft:item/generated", null, true, "layer0", "item/crystal");
    model("item/dilithium_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/dilithium_ore", "block/dilithium_ore", null, true);
    model("item/docking_port", "block/docking_port", null, true);
    model("item/electric_arc_furnace", "block/electric_arc_furnace", null, true);
    model("item/electric_mushroom", "block/electric_mushroom", null, true);
    model("item/electrolyzer", "block/electrolyzer", null, true);
    model(
        "item/elevator_chip",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/elevatorchip");
    model("item/elite_motor", "block/elite_motor", null, true);
    model("item/emerald_crystal_block", "block/emerald_crystal_block", null, true);
    model("item/enhanced_motor", "block/enhanced_motor", null, true);
    bucket("item/enriched_lava_bucket", "adv_rocketry:enriched_lava");
    model(
        "item/flight_speed_upgrade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/flightspeedupgrade");
    model(
        "item/fluid_io_circuit_board",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/liquidiocircuit");
    model("item/fluid_pump", "block/fluid_pump", null, true);
    model("item/force_field_projector", "block/force_field_projector", null, true);
    model("item/fuel_tank", "block/fuel_tank", null, true);
    model("item/fueling_station", "block/fueling_station", null, true);
    model("item/gas_intake", "block/gas_intake", null, true);
    model("item/geode", "block/geode", null, true);
    model("item/gold_coil", "block/gold_coil", null, true);
    model("item/gold_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/gold_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/guidance_access_hatch", "block/guidance_access_hatch", null, true);
    model("item/guidance_computer", "block/guidance_computer", null, true);
    bucket("item/helium3_bucket", "adv_rocketry:helium3");
    bucket("item/helium_bucket", "adv_rocketry:helium");
    model("item/holographic_planet_selector", "block/holographic_planet_selector", null, true);
    model("item/hot_turf", "block/hot_turf", null, true);
    model(
        "item/hover_upgrade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/hoverupgrade");
    model("item/hovercraft", "minecraft:item/generated", null, true, "layer0", "item/hovercraft");
    bucket("item/hydrogen_bucket", "adv_rocketry:hydrogen");
    model("item/iridium_block", "block/iridium_block", null, true);
    model("item/iridium_coil", "block/iridium_coil", null, true);
    model("item/iridium_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/iridium_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/iridium_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/iridium_ore", "block/iridium_ore", null, true);
    model("item/iridium_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/iridium_rod", "minecraft:item/generated", null, true, "layer0", "item/stick");
    model("item/iron_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/iron_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/iron_rod", "minecraft:item/generated", null, true, "layer0", "item/stick");
    model(
        "item/iron_saw_blade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/sawbladeiron");
    model("item/iron_sheet", "minecraft:item/generated", null, true, "layer0", "item/sheet");
    model(
        "item/item_data_storage",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/datastorageunit");
    model(
        "item/item_holoprojector",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/holoprojector");
    model(
        "item/item_io_circuit_board",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/itemiocircuit");
    model("item/jackhammer", "minecraft:item/generated", null, true, "layer0", "item/jackhammer");
    model("item/jetpack", "minecraft:item/generated", null, true, "layer0", "item/jetpack");
    model("item/landing_float", "block/landing_float", null, true);
    model("item/landing_pad", "block/landing_pad", null, true);
    model(
        "item/large_satellite_solar_panel",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satellitepowersource1");
    model("item/lathe", "block/lathe", null, true);
    model("item/launch_pad", "block/launch_pad", null, true);
    model("item/legs_upgrade", "minecraft:item/generated", null, true, "layer0", "item/bioniclegs");
    model("item/lens_block", "block/lens_block", null, true);
    model("item/linker", "minecraft:item/generated", null, true, "layer0", "item/linker");
    model("item/liquid_tank", "block/liquid_tank", null, true);
    model(
        "item/mass_detector",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction2");
    bucket("item/methane_bucket", "adv_rocketry:methane");
    model("item/microwave_receiver", "block/microwave_receiver", null, true);
    model(
        "item/microwave_transmitter",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction3");
    model("item/mining_drill", "block/mining_drill", null, true);
    model("item/monitoring_station", "block/monitoring_station", null, true);
    model("item/moon_turf", "block/moon_turf", null, true);
    model("item/moon_turf_dark", "block/moon_turf_dark", null, true);
    model(
        "item/night_vision_upgrade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/earthbrightvisor");
    bucket("item/nitrogen_bucket", "adv_rocketry:nitrogen");
    model("item/nuclear_core", "block/nuclear_core", null, true);
    model("item/nuclear_fuel_tank", "block/nuclear_fuel_tank", null, true);
    model("item/nuclear_rocket_motor", "block/nuclear_rocket_motor", null, true);
    model("item/observatory", "minecraft:block/cube_all", null, true, "all", "block/solar");
    model(
        "item/optical_sensor",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction0");
    model("item/orbital_laser", "minecraft:block/cube_all", null, true, "all", "block/solar");
    model(
        "item/ore_mapper",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction4");
    model("item/ore_scanner", "minecraft:item/generated", null, true, "layer0", "item/orescanner");
    model("item/oxidizer_fuel_tank", "block/oxidizer_fuel_tank", null, true);
    bucket("item/oxygen_bucket", "adv_rocketry:oxygen");
    model("item/oxygen_charger", "block/oxygen_charger", null, true);
    model("item/oxygen_vent", "block/oxygen_vent", null, true);
    model(
        "item/padded_boots_upgrade",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/landingboots");
    model("item/pipe_sealer", "block/pipe_sealer", null, true);
    model(
        "item/planet_id_chip",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/planetidchip");
    model("item/planet_selector", "block/planet_selector", null, true);
    model("item/plate_press", "block/plate_press", null, true);
    model(
        "item/portable_pressure_tank_aluminum",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/pressuretank0");
    model(
        "item/portable_pressure_tank_iridium",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/pressuretank3");
    model(
        "item/portable_pressure_tank_steel",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/pressuretank1");
    model(
        "item/portable_pressure_tank_titanium",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/pressuretank2");
    model("item/precision_assembler", "block/precision_assembler", null, true);
    model("item/precision_laser_etcher", "block/precision_laser_etcher", null, true);
    model("item/quartz_crucible", "block/quartz_crucible", null, true);
    model("item/railgun", "minecraft:block/cube_all", null, true, "all", "block/railgun");
    model("item/rocket_builder", "block/rocket_builder", null, true);
    model("item/rocket_fluid_loader", "block/rocket_fluid_loader", null, true);
    model("item/rocket_fluid_unloader", "block/rocket_fluid_unloader", null, true);
    bucket("item/rocket_fuel_bucket", "adv_rocketry:rocket_fuel");
    model("item/rocket_item_loader", "block/rocket_item_loader", null, true);
    model("item/rocket_item_unloader", "block/rocket_item_unloader", null, true);
    model("item/rocket_motor", "block/rocket_motor", null, true);
    model("item/rolling_machine", "block/rolling_machine", null, true);
    model("item/ruby_crystal_block", "block/ruby_crystal_block", null, true);
    model("item/sapphire_crystal_block", "block/sapphire_crystal_block", null, true);
    model("item/satellite", "minecraft:item/generated", null, true, "layer0", "item/satellite");
    model(
        "item/satellite_biome_changer",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteprimaryfunction5");
    model("item/satellite_builder", "block/satellite_builder", null, true);
    model(
        "item/satellite_data_unit",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/datastorageunit");
    model("item/satellite_hatch", "block/satellite_hatch", null, true);
    model(
        "item/satellite_id_chip",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/satelliteidchip");
    model("item/satellite_terminal", "block/satellite_terminal", null, true);
    model("item/saw_blade_assembly", "block/saw_blade_assembly", null, true);
    model(
        "item/seal_detector",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/sealdetector");
    model("item/seat", "block/seat", null, true);
    model("item/silicon_boule", "minecraft:item/generated", null, true, "layer0", "item/boule");
    model("item/silicon_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/silicon_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/silicon_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/silicon_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model(
        "item/silicon_wafer",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/siliconwafer");
    model("item/solar_array", "minecraft:block/cube_all", null, true, "all", "block/solar");
    model("item/solar_array_panel", "block/solar_array_panel", null, true);
    model("item/solar_cell", "minecraft:item/generated", null, true, "layer0", "block/solar");
    model("item/solar_panel", "block/solar_panel", null, true);
    model("item/space_boots", "minecraft:item/generated", null, true, "layer0", "item/space_boots");
    model(
        "item/space_chestplate",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/space_chestplate");
    model("item/space_elevator", "minecraft:block/cube_all", null, true, "all", "block/solar");
    model(
        "item/space_helmet", "minecraft:item/generated", null, true, "layer0", "item/space_helmet");
    model(
        "item/space_leggings",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/space_leggings");
    model(
        "item/space_station",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/spacestationcontainer");
    model("item/station_altitude_controller", "block/station_altitude_controller", null, true);
    model("item/station_builder", "block/station_builder", null, true);
    model("item/station_gravity_controller", "block/station_gravity_controller", null, true);
    model(
        "item/station_id_chip",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/stationidchip");
    model(
        "item/station_orientation_controller", "block/station_orientation_controller", null, true);
    model("item/steel_block", "block/steel_block", null, true);
    model("item/steel_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/steel_fan", "minecraft:item/generated", null, true, "layer0", "item/fan");
    model("item/steel_gear", "minecraft:item/generated", null, true, "layer0", "item/gear");
    model("item/steel_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/steel_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/steel_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/steel_rod", "minecraft:item/generated", null, true, "layer0", "item/stick");
    model("item/steel_sheet", "minecraft:item/generated", null, true, "layer0", "item/sheet");
    model("item/structure_tower", "block/structure_tower", null, true);
    model("item/suit_workstation", "block/suit_workstation", null, true);
    model("item/thermite", "minecraft:item/generated", null, true, "layer0", "item/thermite");
    model(
        "item/thermite_torch",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "legacy/advancedrocketry/blocks/thermitetorch");
    model("item/tin_block", "block/tin_block", null, true);
    model("item/tin_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/tin_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/tin_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/tin_ore", "block/tin_ore", null, true);
    model("item/tin_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/titanium_aluminide_block", "block/titanium_aluminide_block", null, true);
    model(
        "item/titanium_aluminide_dust",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/dust");
    model(
        "item/titanium_aluminide_gear",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/gear");
    model(
        "item/titanium_aluminide_ingot",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/ingot");
    model(
        "item/titanium_aluminide_nugget",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/nugget");
    model(
        "item/titanium_aluminide_plate",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/plate");
    model(
        "item/titanium_aluminide_rod",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/stick");
    model(
        "item/titanium_aluminide_sheet",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/sheet");
    model("item/titanium_block", "block/titanium_block", null, true);
    model("item/titanium_coil", "block/titanium_coil", null, true);
    model("item/titanium_dust", "minecraft:item/generated", null, true, "layer0", "item/dust");
    model("item/titanium_gear", "minecraft:item/generated", null, true, "layer0", "item/gear");
    model("item/titanium_ingot", "minecraft:item/generated", null, true, "layer0", "item/ingot");
    model("item/titanium_iridium_block", "block/titanium_iridium_block", null, true);
    model(
        "item/titanium_iridium_dust",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/dust");
    model(
        "item/titanium_iridium_gear",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/gear");
    model(
        "item/titanium_iridium_ingot",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/ingot");
    model(
        "item/titanium_iridium_nugget",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/nugget");
    model(
        "item/titanium_iridium_plate",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/plate");
    model(
        "item/titanium_iridium_rod",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/stick");
    model(
        "item/titanium_iridium_sheet",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/sheet");
    model("item/titanium_nugget", "minecraft:item/generated", null, true, "layer0", "item/nugget");
    model("item/titanium_ore", "block/titanium_ore", null, true);
    model("item/titanium_plate", "minecraft:item/generated", null, true, "layer0", "item/plate");
    model("item/titanium_rod", "minecraft:item/generated", null, true, "layer0", "item/stick");
    model("item/titanium_sheet", "minecraft:item/generated", null, true, "layer0", "item/sheet");
    model(
        "item/tracking_circuit",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/trackingcircuit");
    model(
        "item/unlit_torch",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "minecraft:block/torch");
    model(
        "item/user_interface",
        "minecraft:item/generated",
        null,
        true,
        "layer0",
        "item/userinterface");
    model("item/vacuum_laser", "block/vacuum_laser", null, true);
    model("item/vitrified_sand", "block/vitrified_sand", null, true);
    model("item/warp_controller", "block/warp_controller", null, true);
    model("item/warp_core", "block/warp_core", null, true);
    model("item/wireless_transceiver", "block/wireless_transceiver", null, true);
    model("item/wulfentite_crystal_block", "block/wulfentite_crystal_block", null, true);
  }
}

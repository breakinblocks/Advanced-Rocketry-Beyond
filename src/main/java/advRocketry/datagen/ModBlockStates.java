package advRocketry.datagen;

import java.util.Map;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModBlockStates extends ModBlockStateHelpers {
  private static final Map<String, int[]> FACING_POINTS_DOWN =
      Map.ofEntries(
          Map.entry("facing=down", new int[] {0, 0}),
          Map.entry("facing=east", new int[] {90, 90}),
          Map.entry("facing=north", new int[] {90, 0}),
          Map.entry("facing=south", new int[] {90, 180}),
          Map.entry("facing=up", new int[] {180, 0}),
          Map.entry("facing=west", new int[] {90, 270}));
  private static final Map<String, int[]> HORIZONTAL =
      Map.ofEntries(
          Map.entry("facing=east", new int[] {0, 90}),
          Map.entry("facing=north", new int[] {0, 0}),
          Map.entry("facing=south", new int[] {0, 180}),
          Map.entry("facing=west", new int[] {0, 270}));
  private static final Map<String, int[]> HORIZONTAL_EAST =
      Map.ofEntries(
          Map.entry("facing=east", new int[] {0, 0}),
          Map.entry("facing=north", new int[] {0, 270}),
          Map.entry("facing=south", new int[] {0, 90}),
          Map.entry("facing=west", new int[] {0, 180}));
  private static final Map<String, int[]> NONE = Map.ofEntries(Map.entry("", new int[] {0, 0}));
  private static final Map<String, int[]> AXIS =
      Map.ofEntries(
          Map.entry("axis=x", new int[] {90, 90}),
          Map.entry("axis=y", new int[] {0, 0}),
          Map.entry("axis=z", new int[] {90, 0}));
  private static final Map<String, int[]> FACING_FIXED =
      Map.ofEntries(
          Map.entry("facing=east", new int[] {0, 0}),
          Map.entry("facing=north", new int[] {0, 0}),
          Map.entry("facing=south", new int[] {0, 0}),
          Map.entry("facing=west", new int[] {0, 0}));
  private static final Map<String, int[]> FACING_HORIZONTAL_ONLY =
      Map.ofEntries(
          Map.entry("facing=down", new int[] {0, 0}),
          Map.entry("facing=east", new int[] {0, 90}),
          Map.entry("facing=north", new int[] {0, 0}),
          Map.entry("facing=south", new int[] {0, 180}),
          Map.entry("facing=up", new int[] {0, 0}),
          Map.entry("facing=west", new int[] {0, 270}));
  private static final Map<String, int[]> FACING_SIDEWAYS =
      Map.ofEntries(
          Map.entry("facing=down", new int[] {90, 0}),
          Map.entry("facing=east", new int[] {0, 90}),
          Map.entry("facing=north", new int[] {0, 0}),
          Map.entry("facing=south", new int[] {0, 180}),
          Map.entry("facing=up", new int[] {270, 0}),
          Map.entry("facing=west", new int[] {0, 270}));
  private static final Map<String, int[]> UPSIDE_DOWN =
      Map.ofEntries(Map.entry("", new int[] {180, 0}));

  ModBlockStates(PackOutput output, ExistingFileHelper files) {
    super(output, files);
  }

  @Override
  protected void registerStatesAndModels() {
    obj(
        "block/advanced_bipropellant_rocket_motor",
        "models/rocket/advbipropellantrocketmotor.obj",
        "block/rocket/advbipropellantrocketmotor");
    obj("block/advanced_motor", "models/libvulpes/advancedmotor.obj", "block/motor/advancedmotor");
    obj(
        "block/advanced_rocket_motor",
        "models/rocket/advrocketmotor.obj",
        "block/rocket/advrocketmotor");
    model(
        "block/airlock_door_bottom_left",
        "minecraft:block/door_bottom_left",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_bottom_left_open",
        "minecraft:block/door_bottom_left_open",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_bottom_right",
        "minecraft:block/door_bottom_right",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_bottom_right_open",
        "minecraft:block/door_bottom_right_open",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_top_left",
        "minecraft:block/door_top_left",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_top_left_open",
        "minecraft:block/door_top_left_open",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_top_right",
        "minecraft:block/door_top_right",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/airlock_door_top_right_open",
        "minecraft:block/door_top_right_open",
        "minecraft:cutout",
        true,
        "top",
        "block/smallairlockdoor_upper",
        "bottom",
        "block/smallairlockdoor_lower");
    model(
        "block/alien_leaves",
        "minecraft:block/leaves",
        "minecraft:cutout",
        true,
        "all",
        "legacy/advancedrocketry/blocks/leaves_alien");
    model(
        "block/alien_log",
        "minecraft:block/cube_column",
        null,
        true,
        "side",
        "legacy/advancedrocketry/blocks/log_blue",
        "end",
        "legacy/advancedrocketry/blocks/log_blue_top");
    model(
        "block/alien_planks",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/plank_blue");
    model(
        "block/alien_sapling",
        "minecraft:block/cross",
        "minecraft:cutout",
        true,
        "cross",
        "legacy/advancedrocketry/blocks/sapling_bluetree");
    connected("block/aluminum_coil", "block/ctm/coilside", 0);
    model(
        "block/aluminum_ore",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/libvulpes/blocks/orealuminum");
    model(
        "block/area_gravity_controller",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    model(
        "block/astrobody_data_processor",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    model(
        "block/atmosphere_detector",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/atmospheredetector");
    model(
        "block/atmosphere_detector_active",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/atmospheredetector_active");
    model(
        "block/atmosphere_terraformer",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/panelside");
    model(
        "block/basalt",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/basalt");
    model("block/beacon", "minecraft:block/block", null, true, "particle", "block/machinegeneric");
    model(
        "block/biome_scanner",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    obj("block/bipropellant_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
    obj(
        "block/bipropellant_fuel_tank_bottom",
        "models/rocket/bottomtank.obj",
        "block/rocket/bottomtank");
    obj(
        "block/bipropellant_fuel_tank_middle",
        "models/rocket/middletank.obj",
        "block/rocket/middletank");
    obj("block/bipropellant_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
    obj(
        "block/bipropellant_rocket_motor",
        "models/rocket/bipropellantrocketmotor.obj",
        "block/rocket/bipropellantrocketmotor");
    model(
        "block/black_hole_generator",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    model(
        "block/blast_brick",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/blastbrick");
    connected("block/block_advanced_structure_block", "block/ctm/advstructureblock", -1);
    connected("block/block_coilcopper", "block/ctm/coilside_copper", -1);
    model(
        "block/block_energy_input_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/batteryrf");
    model(
        "block/block_energy_output_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/batteryrf");
    model(
        "block/block_fluid_input_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/fluidinput");
    model(
        "block/block_fluid_output_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/fluidoutput");
    model(
        "block/block_item_input_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/inputhatch");
    model(
        "block/block_item_output_block",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/outputhatch");
    obj("block/block_motor_block", "models/libvulpes/motor.obj", "block/motor/motor");
    connected("block/block_structure_block", "block/ctm/structureblock", -1);
    model(
        "block/carbon_scrubber",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machinescrubber");
    model(
        "block/carbon_scrubber_active",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machinescrubber_active");
    model(
        "block/centrifuge",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/controlpanel",
        "side",
        "block/machinegeneric");
    connected("block/centrifuge_casing", "block/ctm/panelside", -1);
    model(
        "block/charcoal_log",
        "minecraft:block/cube_column",
        null,
        true,
        "side",
        "legacy/advancedrocketry/blocks/log_charcoal",
        "end",
        "legacy/advancedrocketry/blocks/log_charcoal_top");
    model(
        "block/chemical_reactor",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/controlpanel",
        "side",
        "block/machinegeneric");
    connected("block/circle_light", "block/ctm/stationlight", -1);
    model("block/coal_generator", "minecraft:block/furnace", null, true);
    model(
        "block/concrete", "minecraft:block/cube_all", null, true, "all", "block/rocketpad_noedge");
    connected("block/copper_coil", "block/ctm/coilside", 0);
    model(
        "block/creative_energy_input",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/batterycreative");
    model(
        "block/crystallizer",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/crystallizer",
        "side",
        "block/machinegeneric");
    model(
        "block/cutting_machine",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/cuttingmachine",
        "side",
        "block/machinegeneric");
    model("block/data_bus", "minecraft:block/cube_all", null, true, "all", "block/datahatch");
    model(
        "block/deployable_rocket_builder",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/monitorfront",
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/dilithium_ore",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/libvulpes/blocks/oredilithium");
    model(
        "block/docking_port",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/satellitebay",
        "side",
        "block/machinegeneric");
    model(
        "block/electric_arc_furnace",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "legacy/advancedrocketry/blocks/blastbrickfront",
        "side",
        "block/machinegeneric");
    model(
        "block/electric_mushroom",
        "minecraft:block/cross",
        "minecraft:cutout",
        true,
        "cross",
        "legacy/advancedrocketry/blocks/mushroom_electric");
    model(
        "block/electrolyzer",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/machineelectrolyzer",
        "side",
        "block/machinegeneric");
    obj("block/elite_motor", "models/libvulpes/elitemotor.obj", "block/motor/elitemotor");
    obj("block/enhanced_motor", "models/libvulpes/enhancedmotor.obj", "block/motor/enhancedmotor");
    model(
        "block/fluid_pump",
        "minecraft:block/cube_bottom_top",
        null,
        true,
        "top",
        "block/machinegeneric",
        "bottom",
        "block/pumpbottom",
        "side",
        "block/pumpside");
    model(
        "block/force_field",
        "minecraft:block/cube_all",
        "minecraft:translucent",
        true,
        "all",
        "legacy/advancedrocketry/blocks/forcefield");
    model(
        "block/force_field_projector",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/forcefieldprojector",
        "front",
        "block/forcefieldprojectorfront",
        "side",
        "block/forcefieldprojector");
    obj("block/fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
    obj("block/fuel_tank_bottom", "models/rocket/bottomtank.obj", "block/rocket/bottomtank");
    obj("block/fuel_tank_middle", "models/rocket/middletank.obj", "block/rocket/middletank");
    obj("block/fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
    model(
        "block/fueling_station",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/fuelingmachine",
        "top",
        "block/machinegeneric",
        "side",
        "block/fuelingmachine");
    model("block/gas_intake", "minecraft:block/cube_all", null, true, "all", "block/intake");
    model(
        "block/geode",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/geode");
    connected("block/gold_coil", "block/ctm/coilside", 0);
    model(
        "block/guidance_access_hatch",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/guidancecomputeraccesshatch",
        "front",
        "block/guidancecomputeraccesshatch",
        "side",
        "block/guidancecomputeraccesshatch");
    model(
        "block/guidance_computer",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/guidancecomputer",
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/holographic_planet_selector",
        "minecraft:block/slab",
        null,
        true,
        "top",
        "block/hololamp",
        "side",
        "block/panelside",
        "bottom",
        "block/machinegeneric");
    model(
        "block/hot_turf",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/hotdry_turf");
    connected("block/iridium_coil", "block/ctm/coilside", 0);
    model(
        "block/iridium_ore",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/libvulpes/blocks/oreiridium");
    model(
        "block/landing_float", "minecraft:block/cube_all", null, true, "all", "block/landingfloat");
    connected("block/landing_pad", "block/ctm/rocketpad_", -1);
    model(
        "block/lathe",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/controlpanel",
        "side",
        "block/machinegeneric");
    model(
        "block/launch_pad",
        "minecraft:block/cube_bottom_top",
        null,
        true,
        "top",
        "block/rocketpad_",
        "side",
        "block/rocketpad_xcross",
        "bottom",
        "block/rocketpad_");
    model(
        "block/lens_block", "minecraft:block/cube_all", null, true, "all", "minecraft:block/glass");
    model(
        "block/microwave_receiver",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/solar",
        "front",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/mining_drill",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/machinewarning",
        "top",
        "block/laserbottom",
        "side",
        "block/machinewarning");
    model(
        "block/monitoring_station",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/monitorrocket",
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/moon_turf",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/moon_turf");
    model(
        "block/moon_turf_dark",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/moon_turf_dark");
    model(
        "block/nuclear_core",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machinenuclear");
    obj("block/nuclear_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
    obj(
        "block/nuclear_fuel_tank_bottom",
        "models/rocket/bottomtank.obj",
        "block/rocket/bottomtank");
    obj(
        "block/nuclear_fuel_tank_middle",
        "models/rocket/middletank.obj",
        "block/rocket/middletank");
    obj("block/nuclear_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
    obj(
        "block/nuclear_rocket_motor",
        "models/rocket/nuclearrocketmotor.obj",
        "block/rocket/nuclearrocketmotor");
    model(
        "block/observatory",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    model(
        "block/orbital_laser",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    obj("block/oxidizer_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
    obj(
        "block/oxidizer_fuel_tank_bottom",
        "models/rocket/bottomtank.obj",
        "block/rocket/bottomtank");
    obj(
        "block/oxidizer_fuel_tank_middle",
        "models/rocket/middletank.obj",
        "block/rocket/middletank");
    obj("block/oxidizer_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
    model(
        "block/oxygen_charger",
        "minecraft:block/slab",
        null,
        true,
        "top",
        "block/gaschargertop",
        "bottom",
        "block/machinegeneric",
        "side",
        "block/panelside");
    model("block/oxygen_vent", "minecraft:block/cube_all", null, true, "all", "block/machinevent");
    model("block/pipe_sealer", "minecraft:block/cube_all", null, true, "all", "block/seal");
    model(
        "block/planet_selector",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric",
        "front",
        "block/guidancecomputer");
    model(
        "block/plate_press",
        "minecraft:block/piston",
        null,
        true,
        "platform",
        "minecraft:block/piston_top",
        "bottom",
        "minecraft:block/piston_bottom",
        "side",
        "minecraft:block/piston_side",
        "inside",
        "minecraft:block/piston_inner");
    model(
        "block/precision_assembler",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/precisionassemblerfront",
        "side",
        "block/machinegeneric");
    model(
        "block/precision_laser_etcher",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/machineprecisionlaseretcher",
        "side",
        "block/machinegeneric");
    model(
        "block/quartz_crucible",
        "minecraft:block/cauldron",
        null,
        false,
        "particle",
        "block/qcrucible_side",
        "top",
        "block/qcrucible_top",
        "bottom",
        "block/qcrucible_bottom",
        "side",
        "block/qcrucible_side",
        "inside",
        "block/qcrucible_inner");
    model("block/railgun", "minecraft:block/block", null, true, "particle", "block/machinegeneric");
    model(
        "block/rocket_builder",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/monitorfront",
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/rocket_fluid_loader",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/fluidinput");
    model(
        "block/rocket_fluid_unloader",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/fluidoutput");
    model(
        "block/rocket_item_loader",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/inputhatch");
    model(
        "block/rocket_item_unloader",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/outputhatch");
    obj("block/rocket_motor", "models/rocket/rocketmotor.obj", "block/rocket/rocketmotor");
    model(
        "block/rolling_machine",
        "minecraft:block/orientable",
        null,
        true,
        "top",
        "block/machinegeneric",
        "front",
        "block/controlpanel",
        "side",
        "block/machinegeneric");
    model(
        "block/satellite_builder",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/satelliteassembler");
    model(
        "block/satellite_hatch",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/satellitebay");
    model(
        "block/satellite_terminal",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/monitorsatellite");
    model(
        "block/saw_blade_assembly",
        "minecraft:block/cube_all",
        "minecraft:cutout",
        true,
        "all",
        "item/sawbladeassembly");
    model(
        "block/solar_array",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    connected("block/solar_array_panel", "block/ctm/solar", -1, "adv_rocketry:solar_panel");
    connected("block/solar_panel", "block/ctm/solar", -1, "adv_rocketry:solar_array_panel");
    model(
        "block/space_elevator",
        "minecraft:block/block",
        null,
        true,
        "particle",
        "block/machinegeneric");
    model(
        "block/station_altitude_controller",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machineorientationcontrol");
    model(
        "block/station_builder",
        "minecraft:block/orientable",
        null,
        true,
        "front",
        "block/monitorfront",
        "top",
        "block/machinegeneric",
        "side",
        "block/machinegeneric");
    model(
        "block/station_gravity_controller",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machinegeneric");
    model(
        "block/station_orientation_controller",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/machineorientationcontrol");
    model(
        "block/structure_tower",
        "minecraft:block/cube_all",
        "minecraft:cutout",
        true,
        "all",
        "block/structuretower");
    model(
        "block/thermite_torch",
        "minecraft:block/template_torch",
        "minecraft:cutout",
        true,
        "torch",
        "legacy/advancedrocketry/blocks/thermitetorch");
    model(
        "block/thermite_wall_torch",
        "minecraft:block/template_torch_wall",
        "minecraft:cutout",
        true,
        "torch",
        "legacy/advancedrocketry/blocks/thermitetorch");
    model(
        "block/tin_ore",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/libvulpes/blocks/oretin");
    connected("block/titanium_coil", "block/ctm/coilside", 0);
    model(
        "block/titanium_ore", "minecraft:block/cube_all", null, true, "all", "block/titanium_ore");
    model(
        "block/unlit_torch",
        "minecraft:block/template_torch",
        null,
        true,
        "torch",
        "minecraft:block/torch");
    model(
        "block/unlit_wall_torch",
        "minecraft:block/template_torch_wall",
        null,
        true,
        "torch",
        "minecraft:block/torch");
    model(
        "block/vacuum_laser",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/vacuumlaserfront");
    model(
        "block/vitrified_sand",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "legacy/advancedrocketry/blocks/vitrifiedsand");
    model(
        "block/warp_controller",
        "minecraft:block/cube_all",
        null,
        true,
        "all",
        "block/monitorplanet");
    model("block/warp_core", "minecraft:block/cube_all", null, true, "all", "block/warpcore");
    states(
        "advanced_bipropellant_rocket_motor",
        FACING_POINTS_DOWN,
        "",
        "block/advanced_bipropellant_rocket_motor");
    states(
        "advanced_motor",
        HORIZONTAL,
        "assembled=false",
        "block/advanced_motor",
        "assembled=true",
        "block/advanced_motor");
    states("advanced_rocket_motor", FACING_POINTS_DOWN, "", "block/advanced_rocket_motor");
    states(
        "airlock_door",
        HORIZONTAL_EAST,
        "half=lower,hinge=left,open=false",
        "block/airlock_door_bottom_left",
        "half=lower,hinge=left,open=true",
        "block/airlock_door_bottom_left_open",
        "half=lower,hinge=right,open=false",
        "block/airlock_door_bottom_right",
        "half=lower,hinge=right,open=true",
        "block/airlock_door_bottom_right_open",
        "half=upper,hinge=left,open=false",
        "block/airlock_door_top_left",
        "half=upper,hinge=left,open=true",
        "block/airlock_door_top_left_open",
        "half=upper,hinge=right,open=false",
        "block/airlock_door_top_right",
        "half=upper,hinge=right,open=true",
        "block/airlock_door_top_right_open");
    states("alien_leaves", NONE, "", "block/alien_leaves");
    states("alien_log", AXIS, "", "block/alien_log");
    states("alien_planks", NONE, "", "block/alien_planks");
    states(
        "alien_sapling", NONE, "stage=0", "block/alien_sapling", "stage=1", "block/alien_sapling");
    states("aluminum_block", NONE, "", "block/aluminum_block");
    states("aluminum_coil", NONE, "", "block/aluminum_coil");
    states("aluminum_ore", NONE, "", "block/aluminum_ore");
    states("amethyst_crystal_block", NONE, "", "block/amethyst_crystal_block");
    states("ammonia", NONE, "", "minecraft:block/water");
    states("area_gravity_controller", NONE, "", "block/area_gravity_controller");
    states("astrobody_data_processor", FACING_FIXED, "", "block/astrobody_data_processor");
    states(
        "atmosphere_detector",
        NONE,
        "powered=false",
        "block/atmosphere_detector",
        "powered=true",
        "block/atmosphere_detector_active");
    states("atmosphere_terraformer", FACING_FIXED, "", "block/atmosphere_terraformer");
    states("basalt", NONE, "", "block/basalt");
    states("beacon", NONE, "", "block/beacon");
    states("biome_scanner", NONE, "", "block/biome_scanner");
    states(
        "bipropellant_fuel_tank",
        NONE,
        "tankstates=bottom",
        "block/bipropellant_fuel_tank_bottom",
        "tankstates=middle",
        "block/bipropellant_fuel_tank_middle",
        "tankstates=top",
        "block/bipropellant_fuel_tank_top");
    states("bipropellant_rocket_motor", FACING_POINTS_DOWN, "", "block/bipropellant_rocket_motor");
    states("black_hole_generator", FACING_FIXED, "", "block/black_hole_generator");
    states("blast_brick", NONE, "", "block/blast_brick");
    states("block_advanced_structure_block", NONE, "", "block/block_advanced_structure_block");
    states("block_coilcopper", NONE, "", "block/block_coilcopper");
    states("block_energy_input_block", NONE, "", "block/block_energy_input_block");
    states("block_energy_output_block", NONE, "", "block/block_energy_output_block");
    states("block_fluid_input_block", NONE, "", "block/block_fluid_input_block");
    states("block_fluid_output_block", NONE, "", "block/block_fluid_output_block");
    states("block_item_input_block", NONE, "", "block/block_item_input_block");
    states("block_item_output_block", NONE, "", "block/block_item_output_block");
    states(
        "block_motor_block",
        HORIZONTAL,
        "assembled=false",
        "block/block_motor_block",
        "assembled=true",
        "block/block_motor_block");
    states("block_structure_block", NONE, "", "block/block_structure_block");
    states(
        "carbon_scrubber",
        NONE,
        "powered=false",
        "block/carbon_scrubber",
        "powered=true",
        "block/carbon_scrubber_active");
    states(
        "centrifuge",
        HORIZONTAL,
        "state=false",
        "block/centrifuge",
        "state=true",
        "block/centrifuge_formed");
    states("centrifuge_casing", NONE, "", "block/centrifuge_casing");
    states("charcoal_log", AXIS, "", "block/charcoal_log");
    states(
        "chemical_reactor",
        HORIZONTAL,
        "state=false",
        "block/chemical_reactor",
        "state=true",
        "block/chemical_reactor_formed");
    states("circle_light", NONE, "", "block/circle_light");
    states("citrine_crystal_block", NONE, "", "block/citrine_crystal_block");
    states("coal_generator", NONE, "", "block/coal_generator");
    states("concrete", NONE, "", "block/concrete");
    states("copper_coil", NONE, "", "block/copper_coil");
    states("creative_energy_input", NONE, "", "block/creative_energy_input");
    states(
        "crystallizer",
        HORIZONTAL,
        "state=false",
        "block/crystallizer",
        "state=true",
        "block/crystallizer_formed");
    states(
        "cutting_machine",
        HORIZONTAL,
        "state=false",
        "block/cutting_machine",
        "state=true",
        "block/cutting_machine_formed");
    states("data_bus", NONE, "", "block/data_bus");
    states(
        "deployable_rocket_builder", FACING_HORIZONTAL_ONLY, "", "block/deployable_rocket_builder");
    states("dilithium_ore", NONE, "", "block/dilithium_ore");
    states("docking_port", FACING_SIDEWAYS, "", "block/docking_port");
    states(
        "electric_arc_furnace",
        HORIZONTAL,
        "state=false",
        "block/electric_arc_furnace",
        "state=true",
        "block/electric_arc_furnace");
    states("electric_mushroom", NONE, "", "block/electric_mushroom");
    states(
        "electrolyzer",
        HORIZONTAL,
        "state=false",
        "block/electrolyzer",
        "state=true",
        "block/electrolyzer_formed");
    states(
        "elite_motor",
        HORIZONTAL,
        "assembled=false",
        "block/elite_motor",
        "assembled=true",
        "block/elite_motor");
    states("emerald_crystal_block", NONE, "", "block/emerald_crystal_block");
    states(
        "enhanced_motor",
        HORIZONTAL,
        "assembled=false",
        "block/enhanced_motor",
        "assembled=true",
        "block/enhanced_motor");
    states("enriched_lava", NONE, "", "minecraft:block/water");
    states("fluid_pump", NONE, "", "block/fluid_pump");
    states("force_field", NONE, "", "block/force_field");
    states("force_field_projector", FACING_SIDEWAYS, "", "block/force_field_projector");
    states(
        "fuel_tank",
        NONE,
        "tankstates=bottom",
        "block/fuel_tank_bottom",
        "tankstates=middle",
        "block/fuel_tank_middle",
        "tankstates=top",
        "block/fuel_tank_top");
    states(
        "fueling_station",
        HORIZONTAL,
        "powered=false",
        "block/fueling_station",
        "powered=true",
        "block/fueling_station");
    states("gas_intake", NONE, "", "block/gas_intake");
    states("geode", NONE, "", "block/geode");
    states("gold_coil", NONE, "", "block/gold_coil");
    states(
        "guidance_access_hatch",
        HORIZONTAL,
        "powered=false",
        "block/guidance_access_hatch",
        "powered=true",
        "block/guidance_access_hatch");
    states("guidance_computer", FACING_HORIZONTAL_ONLY, "", "block/guidance_computer");
    states("helium", NONE, "", "minecraft:block/water");
    states("helium3", NONE, "", "minecraft:block/water");
    states("holographic_planet_selector", NONE, "", "block/holographic_planet_selector");
    states("hot_turf", NONE, "", "block/hot_turf");
    states("hydrogen", NONE, "", "minecraft:block/water");
    states("iridium_block", NONE, "", "block/iridium_block");
    states("iridium_coil", NONE, "", "block/iridium_coil");
    states("iridium_ore", NONE, "", "block/iridium_ore");
    states("landing_float", NONE, "", "block/landing_float");
    states("landing_pad", NONE, "", "block/landing_pad");
    states("laser_light", NONE, "", "block/laser_light");
    states("lathe", HORIZONTAL, "state=false", "block/lathe", "state=true", "block/lathe_formed");
    states("launch_pad", NONE, "", "block/launch_pad");
    states("lens_block", NONE, "", "block/lens_block");
    states("liquid_tank", NONE, "", "block/liquid_tank");
    states("methane", NONE, "", "minecraft:block/water");
    states("microwave_receiver", NONE, "", "block/microwave_receiver");
    states("mining_drill", FACING_HORIZONTAL_ONLY, "", "block/mining_drill");
    states("monitoring_station", HORIZONTAL, "", "block/monitoring_station");
    states("moon_turf", NONE, "", "block/moon_turf");
    states("moon_turf_dark", NONE, "", "block/moon_turf_dark");
    states("nitrogen", NONE, "", "minecraft:block/water");
    states("nuclear_core", NONE, "", "block/nuclear_core");
    states(
        "nuclear_fuel_tank",
        NONE,
        "tankstates=bottom",
        "block/nuclear_fuel_tank_bottom",
        "tankstates=middle",
        "block/nuclear_fuel_tank_middle",
        "tankstates=top",
        "block/nuclear_fuel_tank_top");
    states("nuclear_rocket_motor", FACING_POINTS_DOWN, "", "block/nuclear_rocket_motor");
    states("observatory", FACING_FIXED, "", "block/observatory");
    states("orbital_laser", FACING_FIXED, "", "block/orbital_laser");
    states(
        "oxidizer_fuel_tank",
        NONE,
        "tankstates=bottom",
        "block/oxidizer_fuel_tank_bottom",
        "tankstates=middle",
        "block/oxidizer_fuel_tank_middle",
        "tankstates=top",
        "block/oxidizer_fuel_tank_top");
    states("oxygen", NONE, "", "minecraft:block/water");
    states("oxygen_charger", NONE, "", "block/oxygen_charger");
    states("oxygen_vent", NONE, "", "block/oxygen_vent");
    states("pipe_sealer", NONE, "", "block/pipe_sealer");
    states("planet_selector", NONE, "", "block/planet_selector");
    states("plate_press", UPSIDE_DOWN, "", "block/plate_press");
    states(
        "precision_assembler",
        HORIZONTAL,
        "state=false",
        "block/precision_assembler",
        "state=true",
        "block/precision_assembler_formed");
    states(
        "precision_laser_etcher",
        HORIZONTAL,
        "state=false",
        "block/precision_laser_etcher",
        "state=true",
        "block/precision_laser_etcher_formed");
    states("quartz_crucible", NONE, "", "block/quartz_crucible");
    states("railgun", FACING_FIXED, "", "block/railgun");
    states("rocket_builder", FACING_HORIZONTAL_ONLY, "", "block/rocket_builder");
    states(
        "rocket_fire",
        NONE,
        "age=0",
        "minecraft:block/fire_floor0",
        "age=1",
        "minecraft:block/fire_floor0",
        "age=10",
        "minecraft:block/fire_floor0",
        "age=11",
        "minecraft:block/fire_floor0",
        "age=12",
        "minecraft:block/fire_floor0",
        "age=13",
        "minecraft:block/fire_floor0",
        "age=14",
        "minecraft:block/fire_floor0",
        "age=15",
        "minecraft:block/fire_floor0",
        "age=2",
        "minecraft:block/fire_floor0",
        "age=3",
        "minecraft:block/fire_floor0",
        "age=4",
        "minecraft:block/fire_floor0",
        "age=5",
        "minecraft:block/fire_floor0",
        "age=6",
        "minecraft:block/fire_floor0",
        "age=7",
        "minecraft:block/fire_floor0",
        "age=8",
        "minecraft:block/fire_floor0",
        "age=9",
        "minecraft:block/fire_floor0");
    states(
        "rocket_fluid_loader",
        FACING_FIXED,
        "powered=false",
        "block/rocket_fluid_loader",
        "powered=true",
        "block/rocket_fluid_loader");
    states(
        "rocket_fluid_unloader",
        FACING_FIXED,
        "powered=false",
        "block/rocket_fluid_unloader",
        "powered=true",
        "block/rocket_fluid_unloader");
    states("rocket_fuel", NONE, "", "minecraft:block/water");
    states(
        "rocket_item_loader",
        FACING_FIXED,
        "powered=false",
        "block/rocket_item_loader",
        "powered=true",
        "block/rocket_item_loader");
    states(
        "rocket_item_unloader",
        FACING_FIXED,
        "powered=false",
        "block/rocket_item_unloader",
        "powered=true",
        "block/rocket_item_unloader");
    states("rocket_motor", FACING_POINTS_DOWN, "", "block/rocket_motor");
    states(
        "rolling_machine",
        HORIZONTAL,
        "state=false",
        "block/rolling_machine",
        "state=true",
        "block/rolling_machine_formed");
    states("ruby_crystal_block", NONE, "", "block/ruby_crystal_block");
    states("sapphire_crystal_block", NONE, "", "block/sapphire_crystal_block");
    states("satellite_builder", NONE, "", "block/satellite_builder");
    states("satellite_hatch", NONE, "", "block/satellite_hatch");
    states("satellite_terminal", NONE, "", "block/satellite_terminal");
    states("saw_blade_assembly", NONE, "", "block/saw_blade_assembly");
    states("seat", FACING_HORIZONTAL_ONLY, "", "block/seat");
    states("solar_array", FACING_FIXED, "", "block/solar_array");
    states("solar_array_panel", NONE, "", "block/solar_array_panel");
    states("solar_panel", NONE, "", "block/solar_panel");
    states("space_elevator", FACING_FIXED, "", "block/space_elevator");
    states("station_altitude_controller", NONE, "", "block/station_altitude_controller");
    states("station_builder", FACING_HORIZONTAL_ONLY, "", "block/station_builder");
    states("station_gravity_controller", NONE, "", "block/station_gravity_controller");
    states("station_orientation_controller", NONE, "", "block/station_orientation_controller");
    states("steel_block", NONE, "", "block/steel_block");
    states("structure_tower", NONE, "", "block/structure_tower");
    states("suit_workstation", HORIZONTAL, "", "block/suit_workstation");
    states("thermite_torch", NONE, "", "block/thermite_torch");
    states("thermite_wall_torch", HORIZONTAL, "", "block/thermite_wall_torch");
    states("tin_block", NONE, "", "block/tin_block");
    states("tin_ore", NONE, "", "block/tin_ore");
    states("titanium_aluminide_block", NONE, "", "block/titanium_aluminide_block");
    states("titanium_block", NONE, "", "block/titanium_block");
    states("titanium_coil", NONE, "", "block/titanium_coil");
    states("titanium_iridium_block", NONE, "", "block/titanium_iridium_block");
    states("titanium_ore", NONE, "", "block/titanium_ore");
    states("unlit_torch", NONE, "", "block/unlit_torch");
    states("unlit_wall_torch", HORIZONTAL, "", "block/unlit_wall_torch");
    states("vacuum_laser", NONE, "", "block/vacuum_laser");
    states("vitrified_sand", NONE, "", "block/vitrified_sand");
    states("warp_controller", NONE, "", "block/warp_controller");
    states("warp_core", HORIZONTAL, "", "block/warp_core");
    states("wireless_transceiver", HORIZONTAL, "", "block/wireless_transceiver");
    states("wulfentite_crystal_block", NONE, "", "block/wulfentite_crystal_block");
  }
}

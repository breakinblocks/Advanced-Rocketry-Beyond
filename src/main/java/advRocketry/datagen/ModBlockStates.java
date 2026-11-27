package advRocketry.datagen;

import advRocketry.space.BiomeMaterials;
import advRocketry.transport.TransportBlock;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModBlockStates extends ModBlockStateHelpers {
    private static final Map<String, int[]> FACING_POINTS_DOWN = Map.ofEntries(
            Map.entry("facing=down", new int[] {0, 0}),
            Map.entry("facing=east", new int[] {90, 90}),
            Map.entry("facing=north", new int[] {90, 0}),
            Map.entry("facing=south", new int[] {90, 180}),
            Map.entry("facing=up", new int[] {180, 0}),
            Map.entry("facing=west", new int[] {90, 270}));
    private static final Map<String, int[]> HORIZONTAL = Map.ofEntries(
            Map.entry("facing=east", new int[] {0, 90}),
            Map.entry("facing=north", new int[] {0, 0}),
            Map.entry("facing=south", new int[] {0, 180}),
            Map.entry("facing=west", new int[] {0, 270}));
    private static final Map<String, int[]> HORIZONTAL_EAST = Map.ofEntries(
            Map.entry("facing=east", new int[] {0, 0}),
            Map.entry("facing=north", new int[] {0, 270}),
            Map.entry("facing=south", new int[] {0, 90}),
            Map.entry("facing=west", new int[] {0, 180}));
    private static final Map<String, int[]> NONE = Map.ofEntries(Map.entry("", new int[] {0, 0}));
    private static final Map<String, int[]> AXIS = Map.ofEntries(
            Map.entry("axis=x", new int[] {90, 90}),
            Map.entry("axis=y", new int[] {0, 0}),
            Map.entry("axis=z", new int[] {90, 0}));
    private static final Map<String, int[]> FACING_HORIZONTAL_ONLY = Map.ofEntries(
            Map.entry("facing=down", new int[] {0, 0}),
            Map.entry("facing=east", new int[] {0, 90}),
            Map.entry("facing=north", new int[] {0, 0}),
            Map.entry("facing=south", new int[] {0, 180}),
            Map.entry("facing=up", new int[] {0, 0}),
            Map.entry("facing=west", new int[] {0, 270}));
    private static final Map<String, int[]> FACING_SIDEWAYS = Map.ofEntries(
            Map.entry("facing=down", new int[] {90, 0}),
            Map.entry("facing=east", new int[] {0, 90}),
            Map.entry("facing=north", new int[] {0, 0}),
            Map.entry("facing=south", new int[] {0, 180}),
            Map.entry("facing=up", new int[] {270, 0}),
            Map.entry("facing=west", new int[] {0, 270}));
    private static final Set<String> LAYERED_TERRAIN = Set.of(
            "weathered_basalt",
            "wet_slate",
            "rooted_loam",
            "banded_stone",
            "glacial_stone",
            "alluvial_silt",
            "cooling_crust",
            "mineral_crust",
            "dark_regolith",
            "volcanic_ash");

    ModBlockStates(PackOutput output, ExistingFileHelper files) {
        super(output, files);
    }

    private void terrain(String name, String parent, String renderType, boolean layered) {
        String folder = "block/terrain/" + name + "/";
        ConfiguredModel[] variants = new ConfiguredModel[4];
        for (int index = 0; index < variants.length; index++) {
            String suffix = index == 0 ? "" : "_" + index;
            String path = "block/" + name + suffix;
            String side = index == 0 ? path : folder + "side" + suffix;
            if (layered)
                model(
                        path,
                        parent,
                        renderType,
                        true,
                        "side",
                        side,
                        "top",
                        folder + "top" + suffix,
                        "bottom",
                        folder + "bottom" + suffix);
            else model(path, parent, renderType, true, "all", side);
            if (name.equals("rooted_loam"))
                model(
                        "block/rooted_loam_covered" + suffix,
                        "minecraft:block/cube_all",
                        null,
                        true,
                        "all",
                        folder + "bottom" + suffix);
            variants[index] = new ConfiguredModel(unchecked(path));
        }
        var states = getVariantBuilder(BuiltInRegistries.BLOCK.get(location(name)));
        states.setModels(states.partialState(), variants);
    }

    private void connectedMachinePanel(String name, String folder) {
        connected(
                "block/" + name,
                "block/ctm/" + folder,
                -1,
                "adv_rocketry:block_energy_input_block",
                "adv_rocketry:block_energy_output_block",
                "adv_rocketry:creative_energy_input",
                "adv_rocketry:block_fluid_input_block",
                "adv_rocketry:block_fluid_output_block",
                "adv_rocketry:block_item_input_block",
                "adv_rocketry:block_item_output_block",
                "adv_rocketry:data_bus",
                "adv_rocketry:centrifuge_casing");
    }

    @Override
    protected void registerStatesAndModels() {
        for (String name : BiomeMaterials.ALL.keySet()) {
            String habitat = BiomeMaterials.PLANTS.get(name);
            if (LAYERED_TERRAIN.contains(name)) {
                terrain(name, "minecraft:block/cube_bottom_top", null, true);
            } else if (name.equals("strike_glass")) {
                terrain(name, "minecraft:block/cube_all", "minecraft:translucent", false);
            } else if (name.endsWith("_log")) {
                model(
                        "block/" + name,
                        "minecraft:block/cube_column",
                        null,
                        true,
                        "side",
                        "block/" + name,
                        "end",
                        "block/" + name + "_top");
                states(name, AXIS, "", "block/" + name);
            } else if (habitat != null || name.endsWith("sapling")) {
                String parent = "minecraft:block/cross", key = "cross";
                if ("wall".equals(habitat)) {
                    parent = "minecraft:block/coral_wall_fan";
                    key = "fan";
                }
                if ("floating".equals(habitat)) {
                    parent = "minecraft:block/lily_pad";
                    key = "texture";
                }
                model(
                        "block/" + name,
                        parent,
                        "minecraft:cutout",
                        true,
                        key,
                        "block/" + name,
                        "particle",
                        "block/" + name);
                states(
                        name,
                        "wall".equals(habitat)
                                ? Map.of(
                                        "facing=north",
                                        new int[] {0, 0},
                                        "facing=south",
                                        new int[] {0, 180},
                                        "facing=east",
                                        new int[] {0, 90},
                                        "facing=west",
                                        new int[] {0, 270},
                                        "facing=up",
                                        new int[] {0, 0},
                                        "facing=down",
                                        new int[] {0, 0})
                                : NONE,
                        "",
                        "block/" + name);
            } else {
                model(
                        "block/" + name,
                        "minecraft:block/cube_all",
                        name.endsWith("leaves") ? "minecraft:cutout" : null,
                        true,
                        "all",
                        "block/" + name);
                states(name, NONE, "", "block/" + name);
            }
        }
        obj(
                "block/advanced_bipropellant_rocket_motor",
                "models/rocket/advbipropellantrocketmotor.obj",
                "block/rocket/advbipropellantrocketmotor");
        obj("block/advanced_motor", "models/libvulpes/advancedmotor.obj", "block/motor/advancedmotor");
        obj("block/advanced_rocket_motor", "models/rocket/advrocketmotor.obj", "block/rocket/advrocketmotor");
        airlockFrame("block/airlock_door_lower", "block/smallairlockdoor_lower", false);
        airlockFrame("block/airlock_door_upper", "block/smallairlockdoor_upper", true);
        terrain("alien_leaves", "minecraft:block/leaves", "minecraft:cutout", false);
        model(
                "block/alien_log",
                "minecraft:block/cube_column",
                null,
                true,
                "side",
                "block/bluewood_log",
                "end",
                "block/bluewood_log_top");
        model("block/alien_planks", "minecraft:block/cube_all", null, true, "all", "block/bluewood_planks");
        model(
                "block/alien_sapling",
                "minecraft:block/cross",
                "minecraft:cutout",
                true,
                "cross",
                "block/bluewood_sapling");
        connected("block/aluminum_block", "block/ctm/aluminum_block", -1);
        connected("block/aluminum_coil", "block/ctm/coilside", 0);
        model(
                "block/aluminum_ore",
                "minecraft:block/cube_all",
                null,
                true,
                "all",
                "legacy/libvulpes/blocks/orealuminum");
        machineCabinet("area_gravity_controller");
        machineCabinet("astrobody_data_processor");
        machineCabinet("atmosphere_detector");
        activeMachineCabinet("atmosphere_detector", "atmosphere_detector_active");
        machineCabinet("atmosphere_terraformer");
        model("block/basalt", "minecraft:block/cube_all", null, true, "all", "legacy/advancedrocketry/blocks/basalt");
        machineCabinet("beacon");
        machineCabinet("biome_scanner");
        obj("block/bipropellant_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/bipropellant_fuel_tank_bottom", "models/rocket/bottomtank.obj", "block/rocket/bottomtank");
        obj("block/bipropellant_fuel_tank_middle", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/bipropellant_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
        obj(
                "block/bipropellant_rocket_motor",
                "models/rocket/bipropellantrocketmotor.obj",
                "block/rocket/bipropellantrocketmotor");
        machineCabinet("black_hole_generator");
        connected("block/blast_brick", "block/ctm/blast_brick", -1);
        connected("block/block_advanced_structure_block", "block/ctm/advstructureblock", -1);
        connected("block/block_coilcopper", "block/ctm/coilside_copper", -1);
        connectedMachinePanel("block_energy_input_block", "energy_input");
        connectedMachinePanel("block_energy_output_block", "energy_output");
        connectedMachinePanel("block_fluid_input_block", "fluid_input");
        connectedMachinePanel("block_fluid_output_block", "fluid_output");
        connectedMachinePanel("block_item_input_block", "item_input");
        connectedMachinePanel("block_item_output_block", "item_output");
        obj("block/block_motor_block", "models/libvulpes/motor.obj", "block/motor/motor");
        connectedWithRenderType("block/block_structure_block", "block/ctm/structureblock", -1, "minecraft:cutout");
        machineCabinet("carbon_scrubber");
        activeMachineCabinet("carbon_scrubber", "carbon_scrubber");
        machineCabinet("centrifuge");
        connectedMachinePanel("centrifuge_casing", "panelside");
        model(
                "block/charcoal_log",
                "minecraft:block/cube_column",
                null,
                true,
                "side",
                "legacy/advancedrocketry/blocks/log_charcoal",
                "end",
                "legacy/advancedrocketry/blocks/log_charcoal_top");
        machineCabinet("chemical_reactor");
        connected("block/circle_light", "block/ctm/stationlight", -1);
        model("block/coal_generator", "adv_rocketry:block/art/coal_generator", "minecraft:cutout", true);
        model("block/fuel_generator", "adv_rocketry:block/art/fuel_generator", "minecraft:cutout", true);
        connected("block/concrete", "block/ctm/concrete", -1);
        connected("block/copper_coil", "block/ctm/coilside", 0);
        connectedMachinePanel("creative_energy_input", "creative_energy_input");
        machineCabinet("crystallizer");
        machineCabinet("cutting_machine");
        connectedMachinePanel("data_bus", "data_bus");
        machineCabinet("deployable_rocket_builder");
        model(
                "block/dilithium_ore",
                "minecraft:block/cube_all",
                null,
                true,
                "all",
                "legacy/libvulpes/blocks/oredilithium");
        machineCabinet("docking_port");
        machineCabinet("electric_arc_furnace");
        model(
                "block/electric_mushroom",
                "minecraft:block/cross",
                "minecraft:cutout",
                true,
                "cross",
                "legacy/advancedrocketry/blocks/mushroom_electric");
        machineCabinet("electrolyzer");
        obj("block/elite_motor", "models/libvulpes/elitemotor.obj", "block/motor/elitemotor");
        obj("block/enhanced_motor", "models/libvulpes/enhancedmotor.obj", "block/motor/enhancedmotor");
        machineCabinet("fluid_pump");
        model(
                "block/force_field",
                "minecraft:block/cube_all",
                "minecraft:translucent",
                true,
                "all",
                "legacy/advancedrocketry/blocks/forcefield");
        machineCabinet("force_field_projector");
        obj("block/fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/fuel_tank_bottom", "models/rocket/bottomtank.obj", "block/rocket/bottomtank");
        obj("block/fuel_tank_middle", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
        machineCabinet("fueling_station", true);
        machineCabinet("gas_intake");
        model("block/geode", "minecraft:block/cube_all", null, true, "all", "legacy/advancedrocketry/blocks/geode");
        connected("block/gold_coil", "block/ctm/coilside", 0);
        machineCabinet("guidance_access_hatch");
        machineCabinet("guidance_computer", true);
        machineSlab("holographic_planet_selector", false);
        model(
                "block/hot_turf",
                "minecraft:block/cube_all",
                null,
                true,
                "all",
                "legacy/advancedrocketry/blocks/hotdry_turf");
        connected("block/iridium_coil", "block/ctm/coilside", 0);
        model("block/iridium_ore", "minecraft:block/cube_all", null, true, "all", "legacy/libvulpes/blocks/oreiridium");
        model("block/landing_float", "minecraft:block/cube_all", null, true, "all", "block/landingfloat");
        connected("block/landing_pad", "block/ctm/rocketpad_", -1);
        machineCabinet("lathe");
        connected("block/launch_pad", "block/ctm/launch_pad", -1);
        connectedWithRenderType("block/lens_block", "block/ctm/lens_block", -1, "minecraft:cutout");
        machineCabinet("microwave_receiver");
        machineCabinet("mining_drill");
        machineCabinet("monitoring_station", true);
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
        machineCabinet("nuclear_core");
        obj("block/nuclear_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/nuclear_fuel_tank_bottom", "models/rocket/bottomtank.obj", "block/rocket/bottomtank");
        obj("block/nuclear_fuel_tank_middle", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/nuclear_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
        obj("block/nuclear_rocket_motor", "models/rocket/nuclearrocketmotor.obj", "block/rocket/nuclearrocketmotor");
        machineCabinet("observatory", true);
        machineCabinet("orbital_laser");
        obj("block/oxidizer_fuel_tank", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/oxidizer_fuel_tank_bottom", "models/rocket/bottomtank.obj", "block/rocket/bottomtank");
        obj("block/oxidizer_fuel_tank_middle", "models/rocket/middletank.obj", "block/rocket/middletank");
        obj("block/oxidizer_fuel_tank_top", "models/rocket/toptank.obj", "block/rocket/toptank");
        machineSlab("oxygen_charger", true);
        connected("block/oxygen_vent", "block/ctm/oxygen_vent", -1);
        machineCabinet("pipe_sealer");
        machineCabinet("plate_press");
        machineCabinet("precision_assembler");
        machineCabinet("precision_laser_etcher");
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
        machineCabinet("railgun");
        machineCabinet("rocket_builder", true);
        machineCabinet("rocket_fluid_loader");
        machineCabinet("rocket_fluid_unloader");
        machineCabinet("rocket_item_loader");
        machineCabinet("rocket_item_unloader");
        obj("block/rocket_motor", "models/rocket/rocketmotor.obj", "block/rocket/rocketmotor");
        machineCabinet("rolling_machine");
        machineCabinet("satellite_builder");
        obj("block/seat", "models/block/art/seat.obj", "block/seat").renderType("minecraft:cutout");
        machineCabinet("suit_workstation", true);
        machineCabinet("satellite_hatch");
        machineCabinet("satellite_terminal");
        model(
                "block/saw_blade_assembly",
                "minecraft:block/cube_all",
                "minecraft:cutout",
                true,
                "all",
                "item/sawbladeassembly");
        machineCabinet("solar_array");
        connected("block/solar_array_panel", "block/ctm/solar", -1, "adv_rocketry:solar_panel");
        connected("block/solar_panel", "block/ctm/solar", -1, "adv_rocketry:solar_array_panel");
        machineCabinet("space_elevator");
        machineCabinet("station_altitude_controller", true);
        machineCabinet("station_builder", true);
        machineCabinet("station_gravity_controller");
        machineCabinet("station_orientation_controller");
        connectedWithRenderType("block/structure_tower", "block/ctm/structure_tower", -1, "minecraft:cutout");
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
        model("block/tin_ore", "minecraft:block/cube_all", null, true, "all", "legacy/libvulpes/blocks/oretin");
        connected("block/titanium_coil", "block/ctm/coilside", 0);
        model("block/titanium_ore", "minecraft:block/cube_all", null, true, "all", "block/titanium_ore");
        model(
                "block/unlit_torch",
                "minecraft:block/template_torch",
                "minecraft:cutout",
                true,
                "torch",
                "block/unlit_torch");
        model(
                "block/unlit_wall_torch",
                "minecraft:block/template_torch_wall",
                "minecraft:cutout",
                true,
                "torch",
                "block/unlit_torch");
        connected("block/vacuum_laser", "block/ctm/vacuum_laser", -1);
        machineCabinet("warp_controller", true);
        machineCabinet("warp_core");
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
                "half=lower",
                "block/airlock_door_lower",
                "half=upper",
                "block/airlock_door_upper");
        states("alien_log", AXIS, "", "block/alien_log");
        states("alien_planks", NONE, "", "block/alien_planks");
        states("alien_sapling", NONE, "stage=0", "block/alien_sapling", "stage=1", "block/alien_sapling");
        states("aluminum_block", NONE, "", "block/aluminum_block");
        states("aluminum_coil", NONE, "", "block/aluminum_coil");
        states("aluminum_ore", NONE, "", "block/aluminum_ore");
        connected("block/amethyst_crystal_block", "block/ctm/crystal", 0);
        states("amethyst_crystal_block", NONE, "", "block/amethyst_crystal_block");
        states("ammonia", NONE, "", "minecraft:block/water");
        states("area_gravity_controller", NONE, "", "block/area_gravity_controller");
        states("astrobody_data_processor", HORIZONTAL, "", "block/astrobody_data_processor");
        states(
                "atmosphere_detector",
                NONE,
                "powered=false",
                "block/atmosphere_detector",
                "powered=true",
                "block/atmosphere_detector_active");
        states("atmosphere_terraformer", HORIZONTAL, "", "block/atmosphere_terraformer");
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
        states("black_hole_generator", HORIZONTAL, "", "block/black_hole_generator");
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
        states("centrifuge", HORIZONTAL, "state=false", "block/centrifuge", "state=true", "block/centrifuge_formed");
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
        connected("block/citrine_crystal_block", "block/ctm/crystal", 0);
        states("citrine_crystal_block", NONE, "", "block/citrine_crystal_block");
        states("coal_generator", HORIZONTAL, "", "block/coal_generator");
        states("fuel_generator", HORIZONTAL, "", "block/fuel_generator");
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
        states("deployable_rocket_builder", FACING_HORIZONTAL_ONLY, "", "block/deployable_rocket_builder");
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
        connected("block/emerald_crystal_block", "block/ctm/crystal", 0);
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
        states("warp_ring_light", NONE, "", "block/laser_light");
        states("formed_cover", NONE, "", "block/laser_light");
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
        states("observatory", HORIZONTAL, "", "block/observatory");
        states("orbital_laser", HORIZONTAL, "", "block/orbital_laser");
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
        states("plate_press", NONE, "", "block/plate_press");
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
        states("railgun", HORIZONTAL, "", "block/railgun");
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
                HORIZONTAL,
                "powered=false",
                "block/rocket_fluid_loader",
                "powered=true",
                "block/rocket_fluid_loader");
        states(
                "rocket_fluid_unloader",
                HORIZONTAL,
                "powered=false",
                "block/rocket_fluid_unloader",
                "powered=true",
                "block/rocket_fluid_unloader");
        states("rocket_fuel", NONE, "", "minecraft:block/water");
        states(
                "rocket_item_loader",
                HORIZONTAL,
                "powered=false",
                "block/rocket_item_loader",
                "powered=true",
                "block/rocket_item_loader");
        states(
                "rocket_item_unloader",
                HORIZONTAL,
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
        connected("block/ruby_crystal_block", "block/ctm/crystal", 0);
        states("ruby_crystal_block", NONE, "", "block/ruby_crystal_block");
        connected("block/sapphire_crystal_block", "block/ctm/crystal", 0);
        states("sapphire_crystal_block", NONE, "", "block/sapphire_crystal_block");
        states("satellite_builder", NONE, "", "block/satellite_builder");
        states("satellite_hatch", NONE, "", "block/satellite_hatch");
        states("satellite_terminal", NONE, "", "block/satellite_terminal");
        states("saw_blade_assembly", NONE, "", "block/saw_blade_assembly");
        states("seat", FACING_HORIZONTAL_ONLY, "", "block/seat");
        states("solar_array", HORIZONTAL, "", "block/solar_array");
        states("solar_array_panel", NONE, "", "block/solar_array_panel");
        states("solar_panel", NONE, "", "block/solar_panel");
        states("space_elevator", HORIZONTAL, "", "block/space_elevator");
        states("station_altitude_controller", NONE, "", "block/station_altitude_controller");
        states("station_builder", FACING_HORIZONTAL_ONLY, "", "block/station_builder");
        states("station_gravity_controller", NONE, "", "block/station_gravity_controller");
        states("station_orientation_controller", NONE, "", "block/station_orientation_controller");
        connected("block/steel_block", "block/ctm/machine_steel", 0);
        states("steel_block", NONE, "", "block/steel_block");
        states("structure_tower", NONE, "", "block/structure_tower");
        states("suit_workstation", HORIZONTAL, "", "block/suit_workstation");
        states("thermite_torch", NONE, "", "block/thermite_torch");
        states("thermite_wall_torch", HORIZONTAL, "", "block/thermite_wall_torch");
        states("tin_block", NONE, "", "block/tin_block");
        states("tin_ore", NONE, "", "block/tin_ore");
        states("titanium_aluminide_block", NONE, "", "block/titanium_aluminide_block");
        connected("block/titanium_block", "block/ctm/machine_titanium", 0);
        states("titanium_block", NONE, "", "block/titanium_block");
        states("titanium_coil", NONE, "", "block/titanium_coil");
        states("titanium_iridium_block", NONE, "", "block/titanium_iridium_block");
        states("titanium_ore", NONE, "", "block/titanium_ore");
        states("unlit_torch", NONE, "", "block/unlit_torch");
        states("unlit_wall_torch", HORIZONTAL, "", "block/unlit_wall_torch");
        states("vacuum_laser", NONE, "", "block/vacuum_laser");
        states("warp_controller", NONE, "", "block/warp_controller");
        states("warp_core", HORIZONTAL, "", "block/warp_core");
        states("wireless_transceiver", HORIZONTAL, "", "block/wireless_transceiver");
        connected("block/wulfentite_crystal_block", "block/ctm/crystal", 0);
        states("wulfentite_crystal_block", NONE, "", "block/wulfentite_crystal_block");
        for (String name : new String[] {"energy_cable", "fluid_pipe", "item_conduit"}) transport(name);
        for (String name :
                new String[] {"iridium_block", "tin_block", "titanium_aluminide_block", "titanium_iridium_block"})
            tintedIron("block/" + name);
        models().getBuilder("block/laser_light")
                .parent(unchecked("minecraft:block/block"))
                .texture("particle", location("minecraft:block/glass"));
        formed("centrifuge", "block/controlpanel");
        formed("chemical_reactor", "block/controlpanel");
        formed("crystallizer", "block/crystallizer");
        formed("cutting_machine", "block/cuttingmachine");
        formed("electric_arc_furnace", "legacy/advancedrocketry/blocks/blastbrickfront");
        formed("electrolyzer", "block/machineelectrolyzer");
        formed("lathe", "block/controlpanel");
        formed("precision_assembler", "block/precisionassemblerfront");
        formed("precision_laser_etcher", "block/machineprecisionlaseretcher");
        formed("rolling_machine", "block/controlpanel");
    }

    private void formed(String name, String particle) {
        models().getBuilder("block/" + name + "_formed").texture("particle", location(particle));
    }

    private void tintedIron(String path) {
        BlockModelBuilder builder = models().getBuilder(path)
                .parent(unchecked("minecraft:block/cube_all"))
                .texture("all", location("minecraft:block/iron_block"))
                .texture("particle", "#all");
        builder.element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .allFaces((direction, face) -> face.texture("#all").tintindex(0).cullface(direction));
    }

    private void transport(String name) {
        String[] parts = {"arm", "core", "straight"};
        for (String suffix : new String[] {"", "_active"})
            for (String part : parts)
                models().getBuilder("block/transport/" + name + "_" + part + suffix)
                        .parent(unchecked("block/transport/fiber_" + part))
                        .texture("all", location("block/transport/jackets/" + name + suffix))
                        .texture("particle", "#all");
        Block block = BuiltInRegistries.BLOCK.get(location(name));
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        String[] sides = {"north", "east", "south", "west", "up", "down"};
        int[][] sideRotations = {{0, 0}, {0, 90}, {0, 180}, {0, 270}, {270, 0}, {90, 0}};
        String[] order = {"north", "south", "east", "west", "up", "down"};
        int[][] axisRotations = {{0, 0}, {0, 90}, {90, 0}};
        for (boolean active : new boolean[] {false, true}) {
            String suffix = active ? "_active" : "";
            for (int side = 0; side < sides.length; side++)
                for (boolean connected : new boolean[] {true, false}) {
                    MultiPartBlockStateBuilder.PartBuilder part = builder.part()
                            .modelFile(unchecked("block/transport/" + name + (connected ? "_arm" : "_core") + suffix))
                            .rotationX(sideRotations[side][0])
                            .rotationY(sideRotations[side][1])
                            .addModel();
                    part.nestedGroup()
                            .condition(TransportBlock.ACTIVE, active)
                            .condition(side(block, sides[side]), connected)
                            .end();
                    for (int axis = 0; axis < 3; axis++) {
                        MultiPartBlockStateBuilder.PartBuilder.ConditionGroup group =
                                part.nestedGroup().useOr();
                        for (int index = 0; index < order.length; index++)
                            group.condition(side(block, order[index]), index / 2 != axis);
                    }
                }
        }
        for (boolean active : new boolean[] {false, true})
            for (int axis = 0; axis < 3; axis++) {
                MultiPartBlockStateBuilder.PartBuilder part = builder.part()
                        .modelFile(unchecked("block/transport/" + name + "_straight" + (active ? "_active" : "")))
                        .rotationX(axisRotations[axis][0])
                        .rotationY(axisRotations[axis][1])
                        .addModel()
                        .condition(TransportBlock.ACTIVE, active);
                for (int index = 0; index < order.length; index++)
                    part.condition(side(block, order[index]), index / 2 == axis);
            }
    }

    @SuppressWarnings("unchecked")
    private static Property<Boolean> side(Block block, String name) {
        return (Property<Boolean>) block.getStateDefinition().getProperty(name);
    }

    private void airlockFrame(String path, String texture, boolean upper) {
        BlockModelBuilder builder = models().getBuilder(path)
                .parent(unchecked("minecraft:block/block"))
                .texture("door", location(texture))
                .texture("particle", location(texture))
                .renderType("minecraft:cutout")
                .ao(false);
        airlockPost(builder, 0, 1, upper);
        airlockPost(builder, 15, 16, upper);
        float bottom = upper ? 13 : 0;
        float top = upper ? 16 : 3;
        Direction outer = upper ? Direction.UP : Direction.DOWN;
        Direction inner = upper ? Direction.DOWN : Direction.UP;
        float edge = upper ? 0 : 15;
        float rim = upper ? 2 : 15;
        builder.element()
                .from(0, bottom, 1)
                .to(3, top, 15)
                .face(Direction.WEST)
                .uvs(1, 16 - top, 15, 16 - bottom)
                .texture("#door")
                .cullface(Direction.WEST)
                .end()
                .face(Direction.EAST)
                .uvs(15, 16 - top, 1, 16 - bottom)
                .texture("#door")
                .end()
                .face(outer)
                .uvs(1, edge, 15, edge + 1)
                .texture("#door")
                .cullface(outer)
                .end()
                .face(inner)
                .uvs(1, rim, 15, rim + 1)
                .texture("#door")
                .end()
                .end();
    }

    private static void airlockPost(BlockModelBuilder builder, float from, float to, boolean upper) {
        Direction end = upper ? Direction.UP : Direction.DOWN;
        float row = upper ? 0 : 15;
        var element = builder.element().from(0, 0, from).to(3, 16, to);
        element.face(Direction.WEST)
                .uvs(from, 0, to, 16)
                .texture("#door")
                .cullface(Direction.WEST)
                .end();
        element.face(Direction.EAST).uvs(to, 0, from, 16).texture("#door").end();
        element.face(Direction.NORTH).uvs(from, 0, to, 16).texture("#door").end();
        element.face(Direction.SOUTH).uvs(from, 0, to, 16).texture("#door").end();
        element.face(end)
                .uvs(from, row, to, row + 1)
                .texture("#door")
                .cullface(end)
                .end();
        if (from == 0) element.face(Direction.NORTH).cullface(Direction.NORTH);
        else element.face(Direction.SOUTH).cullface(Direction.SOUTH);
    }

    private void machineCabinet(String name) {
        machineCabinet(name, false);
    }

    private void machineCabinet(String name, boolean splitSides) {
        machineCube(name, name, name, splitSides);
    }

    private void activeMachineCabinet(String name, String poweredFaces) {
        machineCube(name + "_active", name, poweredFaces, false);
    }

    private void machineCube(String modelName, String textureName, String poweredFaces, boolean splitSides) {
        model(
                "block/" + modelName,
                "minecraft:block/cube",
                null,
                true,
                "particle",
                "block/framed/" + modelName + "_front",
                "north",
                "block/framed/" + modelName + "_front",
                "south",
                machineFace(textureName, Direction.SOUTH, splitSides),
                "east",
                machineFace(poweredFaces, Direction.EAST, splitSides),
                "west",
                machineFace(poweredFaces, Direction.WEST, splitSides),
                "up",
                machineFace(poweredFaces, Direction.UP, splitSides),
                "down",
                machineFace(textureName, Direction.DOWN, splitSides));
    }

    private void machineSlab(String name, boolean splitSides) {
        BlockModelBuilder builder = models().getBuilder("block/" + name)
                .parent(unchecked("minecraft:block/block"))
                .texture("particle", location(machineFace(name, Direction.UP, splitSides)));
        var element = builder.element().from(0, 0, 0).to(16, 8, 16);
        for (Direction direction : Direction.values()) {
            String key = direction.getSerializedName();
            builder.texture(key, location(machineFace(name, direction, splitSides)));
            var face = element.face(direction).texture("#" + key);
            if (direction.getAxis().isHorizontal()) face.uvs(0, 8, 16, 16);
            if (direction != Direction.UP) face.cullface(direction);
        }
    }

    private static String machineFace(String name, Direction direction, boolean splitSides) {
        String suffix =
                switch (direction) {
                    case NORTH -> "front";
                    case SOUTH -> "back";
                    case EAST, WEST -> splitSides ? direction.getSerializedName() : "side";
                    case UP -> "top";
                    case DOWN -> "bottom";
                };
        return "block/framed/" + name + "_" + suffix;
    }
}

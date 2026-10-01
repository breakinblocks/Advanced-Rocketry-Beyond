package advRocketry.datagen;

import java.util.List;
import net.minecraft.data.recipes.RecipeOutput;

final class ModProcessingRecipes extends ModRecipeHelpers {
  private ModProcessingRecipes() {}

  static void build(RecipeOutput output) {
    processing(
        output,
        "processing/advbasiccircuit",
        "cutting_machine",
        300,
        100,
        List.of(stack("adv_rocketry:advanced_circuit_plate", 1)),
        List.of(stack("adv_rocketry:advanced_circuit", 4)));
    processing(
        output,
        "processing/advcircuitplate",
        "precision_assembler",
        900,
        100,
        List.of(
            stack("#c:ingots/gold", 1),
            stack("minecraft:redstone_block", 1),
            stack("adv_rocketry:silicon_wafer", 1)),
        List.of(stack("adv_rocketry:advanced_circuit_plate", 1)));
    processing(
        output,
        "processing/advcircuitplateetcher",
        "precision_laser_etcher",
        1200,
        600,
        List.of(
            kept("#c:lenses/precision_laser_etcher", 1),
            stack("#c:plates/gold", 1),
            stack("minecraft:redstone_block", 1),
            stack("adv_rocketry:silicon_wafer", 4)),
        List.of(stack("adv_rocketry:advanced_circuit_plate", 2)));
    processing(
        output,
        "processing/alienplanks",
        "cutting_machine",
        80,
        10,
        List.of(stack("adv_rocketry:alien_log", 1)),
        List.of(stack("adv_rocketry:alien_planks", 6)));
    processing(
        output,
        "processing/antifogvisor",
        "precision_assembler",
        200,
        1,
        List.of(
            stack("adv_rocketry:battery", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:control_circuit_board", 1),
            stack("adv_rocketry:basic_lens", 1)),
        List.of(stack("adv_rocketry:atmosphere_upgrade", 1)));
    processing(
        output,
        "processing/atmanalyser",
        "precision_assembler",
        1000,
        1,
        List.of(
            stack("adv_rocketry:battery", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:user_interface", 1),
            stack("adv_rocketry:basic_lens", 1),
            stack("#c:plates/tin", 1)),
        List.of(stack("adv_rocketry:atm_analyzer", 1)));
    processing(
        output,
        "processing/basiccircuit",
        "cutting_machine",
        300,
        100,
        List.of(stack("adv_rocketry:basic_circuit_plate", 1)),
        List.of(stack("adv_rocketry:basic_circuit", 4)));
    processing(
        output,
        "processing/basiccircuitplate",
        "precision_assembler",
        900,
        100,
        List.of(
            stack("#c:ingots/gold", 1),
            stack("#c:dusts/redstone", 1),
            stack("adv_rocketry:silicon_wafer", 1)),
        List.of(stack("adv_rocketry:basic_circuit_plate", 1)));
    processing(
        output,
        "processing/basiccircuitplateetcher",
        "precision_laser_etcher",
        1200,
        400,
        List.of(
            kept("#c:lenses/precision_laser_etcher", 1),
            stack("#c:plates/gold", 1),
            stack("#c:dusts/redstone", 1),
            stack("adv_rocketry:silicon_wafer", 4)),
        List.of(stack("adv_rocketry:basic_circuit_plate", 2)));
    processing(
        output,
        "processing/beaconfinder",
        "precision_assembler",
        100,
        1,
        List.of(
            stack("adv_rocketry:tracking_circuit", 1), stack("adv_rocketry:atmosphere_upgrade", 1)),
        List.of(stack("adv_rocketry:beacon_finder", 1)));
    processing(
        output,
        "processing/biomechanger",
        "precision_assembler",
        1000,
        1,
        List.of(
            stack("#c:rods/copper", 2),
            stack("#c:rods/titanium", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:silicon_wafer", 2)),
        List.of(stack("adv_rocketry:satellite_biome_changer", 1)));
    processing(
        output,
        "processing/biomechangerremote",
        "precision_assembler",
        1000,
        1,
        List.of(
            stack("adv_rocketry:battery", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:user_interface", 1),
            stack("adv_rocketry:tracking_circuit", 1),
            stack("#c:plates/tin", 1)),
        List.of(stack("adv_rocketry:biome_changer_remote", 1)));
    processing(
        output,
        "processing/bionicleg",
        "precision_assembler",
        200,
        1,
        List.of(
            stack("#adv_rocketry:motors", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:control_circuit_board", 1)),
        List.of(stack("adv_rocketry:legs_upgrade", 1)));
    processing(
        output,
        "processing/blocklens",
        "precision_assembler",
        100,
        1,
        List.of(
            stack("#c:rods/iron", 1),
            stack("adv_rocketry:basic_lens", 3),
            stack("minecraft:glass", 3)),
        List.of(stack("adv_rocketry:lens_block", 1)));
    processing(
        output,
        "processing/bonemeal_chem",
        "chemical_reactor",
        100,
        1,
        List.of(stack("minecraft:bone", 1), fluid("adv_rocketry:nitrogen", 10)),
        List.of(stack("minecraft:bone_meal", 8)));
    processing(
        output,
        "processing/boules_silicon",
        "crystallizer",
        300,
        20,
        List.of(stack("#c:ingots/silicon", 1), stack("#c:nuggets/silicon", 1)),
        List.of(stack("adv_rocketry:silicon_boule", 1)));
    processing(
        output,
        "processing/carboncartridgerefresh",
        "chemical_reactor",
        40,
        20,
        List.of(damaged("adv_rocketry:carbon_scrubber_cartridge", 1, 32766, true)),
        List.of(
            damaged("adv_rocketry:carbon_scrubber_cartridge", 1, 0, true),
            stack("minecraft:charcoal", 1)));
    processing(
        output,
        "processing/controlcircuitboard_prec",
        "precision_assembler",
        200,
        10,
        List.of(
            stack("#c:dusts/redstone", 1),
            stack("#c:plates/copper", 1),
            stack("#c:plates/steel", 1)),
        List.of(stack("adv_rocketry:control_circuit_board", 1)));
    processing(
        output,
        "processing/crystals_dilithium",
        "crystallizer",
        300,
        20,
        List.of(stack("#c:dusts/dilithium", 1)),
        List.of(stack("adv_rocketry:dilithium_crystal", 1)));
    processing(
        output,
        "processing/dataunit",
        "precision_assembler",
        500,
        60,
        List.of(
            stack("#c:dusts/redstone", 1),
            stack("#c:gems/emerald", 1),
            stack("adv_rocketry:basic_circuit", 1)),
        List.of(stack("adv_rocketry:item_data_storage", 1)));
    processing(
        output,
        "processing/elevatorchip",
        "precision_assembler",
        100,
        1,
        List.of(
            stack("adv_rocketry:station_id_chip", 1), stack("adv_rocketry:tracking_circuit", 1)),
        List.of(stack("adv_rocketry:elevator_chip", 1)));
    processing(
        output,
        "processing/enriched_lava",
        "centrifuge",
        200,
        10,
        List.of(fluid("adv_rocketry:enriched_lava", 1000)),
        List.of(fluid("minecraft:lava", 1000)),
        List.of(
            weighted(stack("#c:nuggets/copper", 1), 100),
            weighted(stack("#c:nuggets/iron", 1), 100),
            weighted(stack("#c:nuggets/tin", 1), 100),
            weighted(stack("#c:nuggets/lead", 1), 100),
            weighted(stack("#c:nuggets/silver", 1), 100),
            weighted(stack("#c:nuggets/gold", 1), 75),
            weighted(stack("#c:nuggets/diamond", 1), 10),
            weighted(stack("#c:nuggets/uranium", 1), 10),
            weighted(stack("#c:nuggets/iridium", 1), 1)),
        4);
    processing(
        output,
        "processing/flightspeed",
        "precision_assembler",
        200,
        1,
        List.of(
            stack("#c:gems/diamond", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:control_circuit_board", 1),
            stack("minecraft:fire_charge", 1)),
        List.of(stack("adv_rocketry:flight_speed_upgrade", 1)));
    processing(
        output,
        "processing/highpressuretank",
        "rolling_machine",
        100,
        4,
        List.of(stack("#c:sheets/aluminum", 2), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:portable_pressure_tank_titanium", 1)));
    processing(
        output,
        "processing/hoverupgrade",
        "precision_assembler",
        200,
        1,
        List.of(
            stack("#c:dusts/redstone", 1),
            stack("adv_rocketry:basic_circuit", 1),
            stack("adv_rocketry:control_circuit_board", 1),
            stack("minecraft:redstone_torch", 1)),
        List.of(stack("adv_rocketry:hover_upgrade", 1)));
    processing(
        output,
        "processing/hydrogenoxygen",
        "electrolyzer",
        100,
        20,
        List.of(fluid("minecraft:water", 10)),
        List.of(fluid("adv_rocketry:hydrogen", 100), fluid("adv_rocketry:oxygen", 100)));
    processing(
        output,
        "processing/ingotsteel",
        "electric_arc_furnace",
        6000,
        1,
        List.of(stack("minecraft:charcoal", 1), stack("#c:ingots/iron", 1)),
        List.of(stack("adv_rocketry:steel_ingot", 1)));
    processing(
        output,
        "processing/ingottitaniumaluminide",
        "electric_arc_furnace",
        9000,
        20,
        List.of(stack("#c:ingots/aluminum", 7), stack("#c:ingots/titanium", 3)),
        List.of(stack("adv_rocketry:titanium_aluminide_ingot", 3)));
    processing(
        output,
        "processing/ingottitaniumiridium",
        "electric_arc_furnace",
        3000,
        20,
        List.of(stack("#c:ingots/iridium", 1), stack("#c:ingots/titanium", 1)),
        List.of(stack("adv_rocketry:titanium_iridium_ingot", 2)));
    processing(
        output,
        "processing/iocircuitboard_prec",
        "precision_assembler",
        200,
        10,
        List.of(
            stack("#c:dusts/redstone", 1), stack("#c:plates/gold", 1), stack("#c:plates/steel", 1)),
        List.of(stack("adv_rocketry:item_io_circuit_board", 1)));
    processing(
        output,
        "processing/liquidiocircuitboard_prec",
        "precision_assembler",
        200,
        10,
        List.of(
            stack("#c:dusts/redstone", 1), stack("#c:gems/lapis", 1), stack("#c:plates/steel", 1)),
        List.of(stack("adv_rocketry:fluid_io_circuit_board", 1)));
    processing(
        output,
        "processing/lowpressuretank",
        "rolling_machine",
        100,
        1,
        List.of(stack("#c:sheets/iron", 2), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:portable_pressure_tank_aluminum", 1)));
    processing(
        output,
        "processing/nuclearcore",
        "precision_assembler",
        2000,
        250,
        List.of(
            stack("adv_rocketry:block_advanced_structure_block", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("#c:plates/steel", 8),
            stack("#c:gems/dilithium", 32)),
        List.of(stack("adv_rocketry:nuclear_core", 1)));
    processing(
        output,
        "processing/paddedboots",
        "precision_assembler",
        200,
        1,
        List.of(
            stack("minecraft:feather", 1),
            stack("adv_rocketry:advanced_circuit", 1),
            stack("adv_rocketry:control_circuit_board", 1),
            stack("minecraft:leather_boots", 1)),
        List.of(stack("adv_rocketry:padded_boots_upgrade", 1)));
    processing(
        output,
        "processing/planks_acacia",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:acacia_logs", 1)),
        List.of(stack("minecraft:acacia_planks", 6)));
    processing(
        output,
        "processing/planks_birch",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:birch_logs", 1)),
        List.of(stack("minecraft:birch_planks", 6)));
    processing(
        output,
        "processing/planks_cherry",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:cherry_logs", 1)),
        List.of(stack("minecraft:cherry_planks", 6)));
    processing(
        output,
        "processing/planks_dark_oak",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:dark_oak_logs", 1)),
        List.of(stack("minecraft:dark_oak_planks", 6)));
    processing(
        output,
        "processing/planks_jungle",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:jungle_logs", 1)),
        List.of(stack("minecraft:jungle_planks", 6)));
    processing(
        output,
        "processing/planks_mangrove",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:mangrove_logs", 1)),
        List.of(stack("minecraft:mangrove_planks", 6)));
    processing(
        output,
        "processing/planks_oak",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:oak_logs", 1)),
        List.of(stack("minecraft:oak_planks", 6)));
    processing(
        output,
        "processing/planks_spruce",
        "cutting_machine",
        80,
        10,
        List.of(stack("#minecraft:spruce_logs", 1)),
        List.of(stack("minecraft:spruce_planks", 6)));
    processing(
        output,
        "processing/plates_aluminum",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/aluminum", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:aluminum_plate", 1)));
    processing(
        output,
        "processing/plates_copper",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/copper", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:copper_plate", 1)));
    processing(
        output,
        "processing/plates_gold",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/gold", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:gold_plate", 1)));
    processing(
        output,
        "processing/plates_iridium",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/iridium", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:iridium_plate", 1)));
    processing(
        output,
        "processing/plates_iron",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/iron", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:iron_plate", 1)));
    processing(
        output,
        "processing/plates_silicon",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/silicon", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:silicon_plate", 1)));
    processing(
        output,
        "processing/plates_steel",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/steel", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:steel_plate", 1)));
    processing(
        output,
        "processing/plates_tin",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/tin", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:tin_plate", 1)));
    processing(
        output,
        "processing/plates_titanium",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/titanium", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_plate", 1)));
    processing(
        output,
        "processing/plates_titanium_aluminide",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/titanium_aluminide", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_aluminide_plate", 1)));
    processing(
        output,
        "processing/plates_titanium_iridium",
        "rolling_machine",
        300,
        20,
        List.of(stack("#c:ingots/titanium_iridium", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_iridium_plate", 1)));
    processing(
        output,
        "processing/press_dust_aluminum",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/aluminum", 1)),
        List.of(stack("adv_rocketry:aluminum_dust", 2)));
    processing(
        output,
        "processing/press_dust_copper",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/copper", 1)),
        List.of(stack("adv_rocketry:copper_dust", 2)));
    processing(
        output,
        "processing/press_dust_gold",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/gold", 1)),
        List.of(stack("adv_rocketry:gold_dust", 2)));
    processing(
        output,
        "processing/press_dust_iridium",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/iridium", 1)),
        List.of(stack("adv_rocketry:iridium_dust", 2)));
    processing(
        output,
        "processing/press_dust_iron",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/iron", 1)),
        List.of(stack("adv_rocketry:iron_dust", 2)));
    processing(
        output,
        "processing/press_dust_tin",
        "plate_press",
        1,
        0,
        List.of(stack("#c:ores/tin", 1)),
        List.of(stack("adv_rocketry:tin_dust", 2)));
    processing(
        output,
        "processing/press_plates_aluminum",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/aluminum", 1)),
        List.of(stack("adv_rocketry:aluminum_plate", 4)));
    processing(
        output,
        "processing/press_plates_copper",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/copper", 1)),
        List.of(stack("adv_rocketry:copper_plate", 4)));
    processing(
        output,
        "processing/press_plates_gold",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/gold", 1)),
        List.of(stack("adv_rocketry:gold_plate", 4)));
    processing(
        output,
        "processing/press_plates_iridium",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/iridium", 1)),
        List.of(stack("adv_rocketry:iridium_plate", 4)));
    processing(
        output,
        "processing/press_plates_iron",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/iron", 1)),
        List.of(stack("adv_rocketry:iron_plate", 4)));
    processing(
        output,
        "processing/press_plates_steel",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/steel", 1)),
        List.of(stack("adv_rocketry:steel_plate", 4)));
    processing(
        output,
        "processing/press_plates_tin",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/tin", 1)),
        List.of(stack("adv_rocketry:tin_plate", 4)));
    processing(
        output,
        "processing/press_plates_titanium",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/titanium", 1)),
        List.of(stack("adv_rocketry:titanium_plate", 4)));
    processing(
        output,
        "processing/press_plates_titanium_aluminide",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/titanium_aluminide", 1)),
        List.of(stack("adv_rocketry:titanium_aluminide_plate", 4)));
    processing(
        output,
        "processing/press_plates_titanium_iridium",
        "plate_press",
        1,
        0,
        List.of(stack("#c:storage_blocks/titanium_iridium", 1)),
        List.of(stack("adv_rocketry:titanium_iridium_plate", 4)));
    processing(
        output,
        "processing/pressuretank",
        "rolling_machine",
        100,
        2,
        List.of(stack("#c:sheets/steel", 2), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:portable_pressure_tank_steel", 1)));
    processing(
        output,
        "processing/rocketfuel",
        "chemical_reactor",
        100,
        10,
        List.of(fluid("adv_rocketry:oxygen", 10), fluid("adv_rocketry:hydrogen", 10)),
        List.of(fluid("adv_rocketry:rocket_fuel", 20)));
    processing(
        output,
        "processing/rods_copper",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/copper", 1)),
        List.of(stack("adv_rocketry:copper_rod", 2)));
    processing(
        output,
        "processing/rods_iridium",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/iridium", 1)),
        List.of(stack("adv_rocketry:iridium_rod", 2)));
    processing(
        output,
        "processing/rods_iron",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/iron", 1)),
        List.of(stack("adv_rocketry:iron_rod", 2)));
    processing(
        output,
        "processing/rods_steel",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/steel", 1)),
        List.of(stack("adv_rocketry:steel_rod", 2)));
    processing(
        output,
        "processing/rods_titanium",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/titanium", 1)),
        List.of(stack("adv_rocketry:titanium_rod", 2)));
    processing(
        output,
        "processing/rods_titanium_aluminide",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/titanium_aluminide", 1)),
        List.of(stack("adv_rocketry:titanium_aluminide_rod", 2)));
    processing(
        output,
        "processing/rods_titanium_iridium",
        "lathe",
        300,
        20,
        List.of(stack("#c:ingots/titanium_iridium", 1)),
        List.of(stack("adv_rocketry:titanium_iridium_rod", 2)));
    processing(
        output,
        "processing/sheets_aluminum",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/aluminum", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:aluminum_sheet", 1)));
    processing(
        output,
        "processing/sheets_copper",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/copper", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:copper_sheet", 1)));
    processing(
        output,
        "processing/sheets_iron",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/iron", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:iron_sheet", 1)));
    processing(
        output,
        "processing/sheets_steel",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/steel", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:steel_sheet", 1)));
    processing(
        output,
        "processing/sheets_titanium",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/titanium", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_sheet", 1)));
    processing(
        output,
        "processing/sheets_titanium_aluminide",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/titanium_aluminide", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_aluminide_sheet", 1)));
    processing(
        output,
        "processing/sheets_titanium_iridium",
        "rolling_machine",
        300,
        200,
        List.of(stack("#c:plates/titanium_iridium", 1), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:titanium_iridium_sheet", 1)));
    processing(
        output,
        "processing/siliconingot",
        "electric_arc_furnace",
        12000,
        1,
        List.of(stack("minecraft:sand", 1)),
        List.of(stack("adv_rocketry:silicon_ingot", 1)));
    processing(
        output,
        "processing/siliconwafer",
        "cutting_machine",
        300,
        100,
        List.of(stack("#c:boules/silicon", 1)),
        List.of(stack("adv_rocketry:silicon_wafer", 4)));
    processing(
        output,
        "processing/superhighpressuretank",
        "rolling_machine",
        400,
        8,
        List.of(stack("#c:sheets/titanium", 2), fluid("minecraft:water", 100)),
        List.of(stack("adv_rocketry:portable_pressure_tank_iridium", 1)));
    processing(
        output,
        "processing/titanium_ore_to_ingot",
        "electric_arc_furnace",
        100,
        10,
        List.of(stack("#c:ores/titanium", 1)),
        List.of(stack("adv_rocketry:titanium_ingot", 1)));
    processing(
        output,
        "processing/trackingcircuit",
        "precision_assembler",
        900,
        50,
        List.of(
            stack("#c:dusts/redstone", 1),
            stack("minecraft:ender_eye", 1),
            stack("adv_rocketry:basic_circuit_plate", 1)),
        List.of(stack("adv_rocketry:tracking_circuit", 1)));
  }
}

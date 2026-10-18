package advRocketry.datagen;

import advRocketry.Main;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.BiomeMaterials;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

final class ModTagContent {
  private static final Map<String, String> MATERIAL_KINDS =
      Map.ofEntries(
          Map.entry("ingot", "ingots"),
          Map.entry("nugget", "nuggets"),
          Map.entry("plate", "plates"),
          Map.entry("sheet", "sheets"),
          Map.entry("rod", "rods"),
          Map.entry("dust", "dusts"),
          Map.entry("gear", "gears"),
          Map.entry("coil", "coils"),
          Map.entry("ore", "ores"),
          Map.entry("boule", "boules"),
          Map.entry("fan", "fans"),
          Map.entry("block", "storage_blocks"));

  private static final Set<String> VANILLA_METALS = Set.of("iron", "gold", "copper");

  private ModTagContent() {}

  static void blocks(ModTagProvider<Block> tags) {
    for (String name : BiomeMaterials.STONES)
      tags.values("minecraft:mineable/pickaxe", "adv_rocketry:" + name);
    for (String name : BiomeMaterials.SOILS)
      tags.values("minecraft:mineable/shovel", "adv_rocketry:" + name);
    tags.values(
        "minecraft:dirt",
        "adv_rocketry:rooted_loam",
        "adv_rocketry:alluvial_silt",
        "adv_rocketry:luminous_moss");
    tags.values(
        "minecraft:logs_that_burn",
        "adv_rocketry:rootwood_log",
        "adv_rocketry:stripped_rootwood_log",
        "adv_rocketry:alien_log",
        "adv_rocketry:charcoal_log");
    tags.values(
        "minecraft:mineable/axe",
        "adv_rocketry:rootwood_log",
        "adv_rocketry:stripped_rootwood_log",
        "adv_rocketry:rootwood_planks");
    tags.values("minecraft:leaves", "adv_rocketry:rootwood_leaves", "adv_rocketry:alien_leaves");
    tags.values(
        "minecraft:saplings", "adv_rocketry:rootwood_sapling", "adv_rocketry:alien_sapling");
    tags.values("minecraft:planks", "adv_rocketry:rootwood_planks", "adv_rocketry:alien_planks");
    tags.values(
        "adv_rocketry:connected_textures",
        "adv_rocketry:block_structure_block",
        "adv_rocketry:block_advanced_structure_block",
        "adv_rocketry:centrifuge_casing",
        "adv_rocketry:block_coilcopper",
        "adv_rocketry:aluminum_coil",
        "adv_rocketry:copper_coil",
        "adv_rocketry:gold_coil",
        "adv_rocketry:iridium_coil",
        "adv_rocketry:titanium_coil",
        "adv_rocketry:solar_panel",
        "adv_rocketry:solar_array_panel",
        "adv_rocketry:landing_pad",
        "adv_rocketry:circle_light");
    tags.values(
        "adv_rocketry:geode_ores",
        "minecraft:iron_ore",
        "minecraft:gold_ore",
        "minecraft:copper_ore",
        "adv_rocketry:tin_ore",
        "minecraft:redstone_ore");
    tags.values(
        "adv_rocketry:rocket_blacklist",
        "minecraft:nether_portal",
        "minecraft:bedrock",
        "minecraft:snow",
        "minecraft:water",
        "minecraft:lava",
        "minecraft:fire",
        "adv_rocketry:rocket_fire");
    tags.values(
        "adv_rocketry:motors",
        "adv_rocketry:block_motor_block",
        "adv_rocketry:advanced_motor",
        "adv_rocketry:enhanced_motor",
        "adv_rocketry:elite_motor");
    tags.values(
        "adv_rocketry:coils",
        "adv_rocketry:block_coilcopper",
        "adv_rocketry:aluminum_coil",
        "adv_rocketry:copper_coil",
        "adv_rocketry:gold_coil",
        "adv_rocketry:iridium_coil",
        "adv_rocketry:titanium_coil");
    tags.values(
        "adv_rocketry:machine_ports",
        "adv_rocketry:block_item_input_block",
        "adv_rocketry:block_item_output_block",
        "adv_rocketry:block_fluid_input_block",
        "adv_rocketry:block_fluid_output_block",
        "adv_rocketry:block_energy_input_block",
        "adv_rocketry:creative_energy_input",
        "adv_rocketry:block_energy_output_block",
        "adv_rocketry:solar_panel",
        "adv_rocketry:data_bus");
    tags.values("adv_rocketry:sealable");
    tags.values("adv_rocketry:unsealable");
    tags.values("adv_rocketry:torches");
    tags.values("minecraft:needs_diamond_tool", "adv_rocketry:geode");
    TreeSet<String> ores = new TreeSet<>();
    for (Block block : BuiltInRegistries.BLOCK) {
      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
      if (!id.getNamespace().equals(Main.MODID) || !id.getPath().endsWith("_ore")) continue;
      String material = id.getPath().substring(0, id.getPath().length() - 4);
      tags.value(tags.key("c:ores/" + material), id);
      ores.add("#c:ores/" + material);
    }
    tags.values("c:ores", ores.toArray(String[]::new));
    tags.values(
        "minecraft:mineable/pickaxe",
        "adv_rocketry:block_advanced_structure_block",
        "adv_rocketry:block_coilcopper",
        "adv_rocketry:block_energy_input_block",
        "adv_rocketry:block_energy_output_block",
        "adv_rocketry:block_fluid_input_block",
        "adv_rocketry:block_fluid_output_block",
        "adv_rocketry:block_item_input_block",
        "adv_rocketry:block_item_output_block",
        "adv_rocketry:block_motor_block",
        "adv_rocketry:block_structure_block",
        "adv_rocketry:centrifuge",
        "adv_rocketry:chemical_reactor",
        "adv_rocketry:crystallizer",
        "adv_rocketry:cutting_machine",
        "adv_rocketry:electric_arc_furnace",
        "adv_rocketry:electrolyzer",
        "adv_rocketry:energy_cable",
        "adv_rocketry:fuel_generator",
        "adv_rocketry:fluid_pipe",
        "adv_rocketry:item_conduit",
        "adv_rocketry:lathe",
        "adv_rocketry:precision_assembler",
        "adv_rocketry:precision_laser_etcher",
        "adv_rocketry:rolling_machine",
        "adv_rocketry:solar_panel",
        "adv_rocketry:structure_tower",
        "adv_rocketry:vacuum_laser");
    tags.values(
        "minecraft:mineable/axe",
        "adv_rocketry:alien_log",
        "adv_rocketry:alien_planks",
        "adv_rocketry:charcoal_log");
    ProcessingRegistry.PART_BLOCKS.entrySet().stream()
        .filter(
            entry ->
                !Set.of(
                        "alien_log",
                        "alien_planks",
                        "alien_leaves",
                        "alien_sapling",
                        "charcoal_log",
                        "electric_mushroom")
                    .contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .forEach(
            block ->
                tags.value(
                    tags.key("minecraft:mineable/pickaxe"),
                    BuiltInRegistries.BLOCK.getKey(block.get())));
  }

  static void items(ModTagProvider<Item> tags) {
    tags.values("minecraft:leaves", "adv_rocketry:rootwood_leaves", "adv_rocketry:alien_leaves");
    for (String stone : BiomeMaterials.STONES) {
      tags.values("minecraft:stone_tool_materials", "adv_rocketry:" + stone);
      tags.values("minecraft:stone_crafting_materials", "adv_rocketry:" + stone);
    }
    tags.values(
        "minecraft:logs_that_burn",
        "adv_rocketry:rootwood_log",
        "adv_rocketry:stripped_rootwood_log",
        "adv_rocketry:alien_log",
        "adv_rocketry:charcoal_log");
    tags.values("minecraft:planks", "adv_rocketry:rootwood_planks", "adv_rocketry:alien_planks");
    tags.values(
        "minecraft:saplings", "adv_rocketry:rootwood_sapling", "adv_rocketry:alien_sapling");
    tags.values(
        "adv_rocketry:laser_drill_ores",
        "minecraft:iron_ore",
        "minecraft:gold_ore",
        "minecraft:copper_ore",
        "adv_rocketry:tin_ore",
        "minecraft:redstone_ore",
        "minecraft:diamond_ore");
    tags.values(
        "adv_rocketry:motors",
        "adv_rocketry:block_motor_block",
        "adv_rocketry:advanced_motor",
        "adv_rocketry:enhanced_motor",
        "adv_rocketry:elite_motor");
    tags.values(
        "c:pressure_tanks",
        "adv_rocketry:portable_pressure_tank_aluminum",
        "adv_rocketry:portable_pressure_tank_iridium",
        "adv_rocketry:portable_pressure_tank_steel",
        "adv_rocketry:portable_pressure_tank_titanium");
    tags.values("c:batteries", "adv_rocketry:battery");
    tags.values("c:glass_panes", "minecraft:glass_pane");
    tags.values("c:lenses/precision_laser_etcher", "adv_rocketry:basic_lens");
    tags.values("c:gems/dilithium", "adv_rocketry:dilithium_crystal");
    tags.values("c:ingots/carbon", "adv_rocketry:carbon_brick");
    tags.values("c:dusts/glowstone", "minecraft:glowstone_dust");
    tags.values("c:dusts/redstone", "minecraft:redstone");
    tags.values("c:dyes/black", "minecraft:black_dye");
    tags.values("c:dyes/lime", "minecraft:lime_dye");
    tags.values("c:dyes/yellow", "minecraft:yellow_dye");
    tags.values("c:gems/diamond", "minecraft:diamond");
    tags.values("c:gems/emerald", "minecraft:emerald");
    tags.values("c:gems/lapis", "minecraft:lapis_lazuli");
    tags.values("c:gems/quartz", "minecraft:quartz");
    tags.values("c:ingots/copper", "minecraft:copper_ingot");
    tags.values("c:ingots/gold", "minecraft:gold_ingot");
    tags.values("c:ingots/iron", "minecraft:iron_ingot");
    tags.values("c:nuggets/gold", "minecraft:gold_nugget");
    tags.values("c:nuggets/iron", "minecraft:iron_nugget");
    tags.values("c:ores/copper", "minecraft:copper_ore");
    tags.values("c:ores/gold", "minecraft:gold_ore");
    tags.values("c:ores/iron", "minecraft:iron_ore");
    tags.values("c:storage_blocks/copper", "minecraft:copper_block");
    tags.values("c:storage_blocks/gold", "minecraft:gold_block");
    tags.values("c:storage_blocks/iron", "minecraft:iron_block");
    for (Item item : BuiltInRegistries.ITEM) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
      if (!id.getNamespace().equals(Main.MODID)) continue;
      int split = id.getPath().lastIndexOf('_');
      if (split < 1) continue;
      String material = id.getPath().substring(0, split);
      String kind = MATERIAL_KINDS.get(id.getPath().substring(split + 1));
      if (kind != null && (kind.equals("ores") || material(material)))
        tags.value(tags.key("c:" + kind + "/" + material), id);
    }
  }

  private static boolean material(String name) {
    return VANILLA_METALS.contains(name)
        || BuiltInRegistries.ITEM.containsKey(
            ResourceLocation.fromNamespaceAndPath(Main.MODID, name + "_ingot"))
        || BuiltInRegistries.ITEM.containsKey(
            ResourceLocation.fromNamespaceAndPath(Main.MODID, name + "_dust"));
  }

  static void fluids(ModTagProvider<Fluid> tags) {
    for (String name : ProcessingFluids.DEFINITIONS.keySet())
      tags.values("c:" + name, "adv_rocketry:" + name, "adv_rocketry:flowing_" + name);
    tags.values("c:water", "minecraft:water", "minecraft:flowing_water");
    tags.values(
        "minecraft:lava", "adv_rocketry:enriched_lava", "adv_rocketry:flowing_enriched_lava");
    tags.values("adv_rocketry:harvestable_gases");
  }

  static void entities(ModTagProvider<EntityType<?>> tags) {
    tags.values("adv_rocketry:atmosphere_immune", "minecraft:armor_stand");
  }

  static void biomes(ModTagProvider<Biome> tags) {
    tags.optional(
        "adv_rocketry:planet_biomes/caves",
        "terralith:cave/fungal_caves",
        "terralith:cave/underground_jungle",
        "terralith:cave/andesite_caves",
        "biomesoplenty:glowing_grotto");
    tags.optional(
        "adv_rocketry:planet_biomes/excluded",
        "#minecraft:is_nether",
        "#minecraft:is_end",
        "#minecraft:is_river",
        "#c:is_cave",
        "#c:is_underground");
    tags.optional("adv_rocketry:planet_biomes/scorched", "#minecraft:is_nether");
    tags.optional("adv_rocketry:planet_biomes/airless", "#minecraft:is_end");
    tags.values(
        "adv_rocketry:planet_biomes/excluded",
        "adv_rocketry:alien_river",
        "adv_rocketry:root_caverns",
        "adv_rocketry:fungal_caverns",
        "adv_rocketry:mineral_caves",
        "adv_rocketry:lava_tubes",
        "adv_rocketry:lunar_tubes",
        "adv_rocketry:subglacial_caves");
    tags.values("minecraft:is_river", "adv_rocketry:alien_river");
    tags.values(
        "c:is_cave",
        "adv_rocketry:root_caverns",
        "adv_rocketry:fungal_caverns",
        "adv_rocketry:mineral_caves",
        "adv_rocketry:lava_tubes",
        "adv_rocketry:lunar_tubes",
        "adv_rocketry:subglacial_caves");
    tags.values(
        "adv_rocketry:planet_biomes/excluded",
        "minecraft:river",
        "minecraft:frozen_river",
        "minecraft:the_void",
        "minecraft:nether_wastes",
        "minecraft:crimson_forest",
        "minecraft:warped_forest",
        "minecraft:soul_sand_valley",
        "minecraft:basalt_deltas",
        "minecraft:the_end",
        "minecraft:end_highlands",
        "minecraft:end_midlands",
        "minecraft:small_end_islands",
        "minecraft:end_barrens",
        "adv_rocketry:moon",
        "adv_rocketry:moondark",
        "adv_rocketry:hotdryrock",
        "adv_rocketry:space",
        "adv_rocketry:volcanic");
    tags.values(
        "adv_rocketry:planet_biomes/high_pressure",
        "adv_rocketry:deepswamp",
        "adv_rocketry:stormland");
    tags.values(
        "adv_rocketry:planet_biomes/single",
        "adv_rocketry:volcanicbarren",
        "adv_rocketry:deepswamp",
        "adv_rocketry:crystalchasms",
        "adv_rocketry:alien_forest",
        "minecraft:desert",
        "minecraft:mushroom_fields",
        "minecraft:windswept_hills",
        "minecraft:snowy_plains");
    tags.values("adv_rocketry:planet_biomes/airless", "adv_rocketry:moon", "adv_rocketry:moondark");
    tags.values(
        "adv_rocketry:planet_biomes/scorched",
        "adv_rocketry:hotdryrock",
        "adv_rocketry:volcanic",
        "adv_rocketry:volcanicbarren");
  }
}

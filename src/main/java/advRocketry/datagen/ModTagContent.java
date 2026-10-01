package advRocketry.datagen;

import advRocketry.Main;
import advRocketry.processing.ProcessingRegistry;
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
        "adv_rocketry:fluid_pipe",
        "adv_rocketry:item_conduit",
        "adv_rocketry:lathe",
        "adv_rocketry:precision_assembler",
        "adv_rocketry:precision_laser_etcher",
        "adv_rocketry:rolling_machine",
        "adv_rocketry:solar_panel",
        "adv_rocketry:structure_tower",
        "adv_rocketry:vacuum_laser");
    ProcessingRegistry.PART_BLOCKS
        .values()
        .forEach(
            block ->
                tags.value(
                    tags.key("minecraft:mineable/pickaxe"),
                    BuiltInRegistries.BLOCK.getKey(block.get())));
  }

  static void items(ModTagProvider<Item> tags) {
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
    tags.values(
        "minecraft:lava", "adv_rocketry:enriched_lava", "adv_rocketry:flowing_enriched_lava");
    tags.values("adv_rocketry:harvestable_gases");
  }

  static void entities(ModTagProvider<EntityType<?>> tags) {
    tags.values("adv_rocketry:atmosphere_immune", "minecraft:armor_stand");
  }

  static void biomes(ModTagProvider<Biome> tags) {
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
        "adv_rocketry:alien_forest",
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

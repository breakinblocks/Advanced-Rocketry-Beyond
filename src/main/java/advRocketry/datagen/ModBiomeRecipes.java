package advRocketry.datagen;

import java.util.List;
import java.util.Map;
import net.minecraft.data.recipes.RecipeOutput;

/** Harvested flora and terrain remain useful outside their home planet. */
final class ModBiomeRecipes extends ModRecipeHelpers {
  private ModBiomeRecipes() {}

  static void build(RecipeOutput output) {
    Map<String, String> pigments =
        Map.ofEntries(
            Map.entry("fan_fern", "cyan"), Map.entry("spiral_seedling", "cyan"),
            Map.entry("forest_groundcover", "cyan"), Map.entry("fallen_foliage", "brown"),
            Map.entry("hanging_roots", "brown"), Map.entry("luminous_seed_cluster", "light_blue"),
            Map.entry("shelf_fungus", "orange"), Map.entry("cave_blossom", "light_blue"),
            Map.entry("jointed_reeds", "green"), Map.entry("marsh_sedge", "green"),
            Map.entry("floating_leaves", "green"), Map.entry("aquatic_frond", "cyan"),
            Map.entry("vent_growth", "orange"), Map.entry("ember_curl", "brown"),
            Map.entry("storm_blades", "light_gray"), Map.entry("spark_mushroom", "light_blue"),
            Map.entry("mineral_bloom", "purple"), Map.entry("salt_shrub", "green"),
            Map.entry("volcanic_ash", "gray"), Map.entry("luminous_moss", "green"));
    pigments.forEach(
        (plant, dye) ->
            shapeless(
                output,
                "biomes/dye_from_" + plant,
                "minecraft:" + dye + "_dye",
                1,
                item("adv_rocketry:" + plant)));
    shapeless(
        output,
        "biomes/string_from_reeds",
        "minecraft:string",
        1,
        item("adv_rocketry:jointed_reeds"),
        item("adv_rocketry:jointed_reeds"),
        item("adv_rocketry:jointed_reeds"));
    smelting(
        output,
        "biomes/charcoal_from_bluewood",
        item("adv_rocketry:alien_log"),
        "minecraft:charcoal",
        .15f,
        200);
    for (String log : List.of("rootwood_log", "stripped_rootwood_log"))
      processing(
          output,
          "biomes/saw_" + log,
          "cutting_machine",
          80,
          10,
          List.of(stack("adv_rocketry:" + log, 1)),
          List.of(stack("adv_rocketry:rootwood_planks", 6)));
    processing(
        output,
        "processing/coil_from_copper_sheets",
        "rolling_machine",
        200,
        20,
        List.of(stack("#c:sheets/copper", 8)),
        List.of(stack("adv_rocketry:copper_coil", 1)));
    processing(
        output,
        "processing/wafer_from_silicon_plate",
        "cutting_machine",
        300,
        100,
        List.of(stack("#c:plates/silicon", 1)),
        List.of(stack("adv_rocketry:silicon_wafer", 1)));
    processing(
        output,
        "processing/elite_motor_upgrade",
        "precision_assembler",
        400,
        100,
        List.of(
            stack("adv_rocketry:enhanced_motor", 1),
            stack("#c:gears/titanium_iridium", 1),
            stack("#c:sheets/titanium_iridium", 2)),
        List.of(stack("adv_rocketry:elite_motor", 1)));
  }
}

package advRocketry.processing;

import advRocketry.orbit.LegacyArtifactIds;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class OriginalRecipeParityTests {
  @GameTest(template = "empty")
  public static void originalProcessingDefinitions(GameTestHelper helper) throws Exception {
    var rows =
        JsonParser.parseString(Files.readString(Path.of("../gametest/fixtures/original-processing-recipes.json")))
            .getAsJsonArray();
    for (var entry : rows) {
      var row = entry.getAsJsonArray();
      String name = row.get(0).getAsString();
      var original = row.get(1).getAsJsonObject();
      var holder =
          helper
              .getLevel()
              .getRecipeManager()
              .byKey(ResourceLocation.parse("adv_rocketry:processing/" + name))
              .orElseThrow();
      var recipe = (ProcessingRecipe) holder.value();
      helper.assertTrue(
          recipe.ticks() == original.get("time").getAsInt()
              && recipe.energy() == original.get("energy").getAsInt(),
          "Processing cost changed: " + name);
      List<String> machines =
          List.of(
              "cuttingmachine",
              "precisionassembler",
              "precisionlaseretcher",
              "chemicalreactor",
              "rollingmachine",
              "electrolyser",
              "electricarcfurnace");
      List<String> nativeMachines =
          List.of(
              "cutting_machine",
              "precision_assembler",
              "precision_laser_etcher",
              "chemical_reactor",
              "rolling_machine",
              "electrolyzer",
              "electric_arc_furnace");
      helper.assertTrue(
          recipe
              .machine()
              .id()
              .equals(
                  nativeMachines.get(
                      machines.indexOf(original.get("type").getAsString().split(":")[1]))),
          "Processing machine changed: " + name);
      checkInputs(helper, name, original, recipe);
      checkOutputs(helper, name, original, recipe);
    }
    helper.assertTrue(rows.size() == 37, "Incomplete original processing inventory");
    helper.succeed();
  }

  private static List<JsonObject> entries(JsonObject original, String suffix) {
    List<JsonObject> entries = new ArrayList<>();
    for (String prefix : List.of("item", "fluid")) {
      var value = original.get(prefix + suffix);
      if (value == null) continue;
      JsonArray array = new JsonArray();
      if (value.isJsonArray()) array = value.getAsJsonArray();
      else array.add(value);
      for (var element : array) entries.add(element.getAsJsonObject());
    }
    return entries;
  }

  private static int count(JsonObject expected) {
    String key = expected.has("fluid") ? "amount" : "count";
    return expected.has(key) ? expected.get(key).getAsInt() : 1;
  }

  private static Fluid fluid(JsonObject expected) {
    String id = expected.get("fluid").getAsString().toLowerCase(Locale.ROOT);
    if (id.equals("rocketfuel")) id = "rocket_fuel";
    if (!id.contains(":"))
      id = (id.equals("water") || id.equals("lava") ? "minecraft:" : "adv_rocketry:") + id;
    return BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
  }

  private static ItemStack item(JsonObject expected) {
    if (expected.has("ore")) {
      var tag =
          TagKey.create(
              Registries.ITEM, LegacyOreEntries.oreDictionary(expected.get("ore").getAsString()));
      return BuiltInRegistries.ITEM
          .getTag(tag)
          .flatMap(set -> set.stream().findFirst())
          .map(ItemStack::new)
          .orElse(ItemStack.EMPTY);
    }
    var id = ResourceLocation.parse(expected.get("item").getAsString().toLowerCase(Locale.ROOT));
    int metadata = expected.has("data") ? expected.get("data").getAsInt() : 0;
    boolean cartridge = id.getPath().equals("carbonscrubbercartridge");
    var resolved = LegacyArtifactIds.resolve(id, cartridge ? 0 : metadata);
    if (resolved == null) return ItemStack.EMPTY;
    ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(resolved));
    if (cartridge) stack.setDamageValue(metadata);
    return stack;
  }

  private static void checkInputs(
      GameTestHelper helper, String name, JsonObject original, ProcessingRecipe recipe) {
    var expected = entries(original, "ingredients");
    List<SizedIngredient> items = new ArrayList<>(recipe.requiredItems());
    List<SizedFluidIngredient> fluids = new ArrayList<>(recipe.fluidInputs());
    helper.assertTrue(
        expected.size() == items.size() + fluids.size(), "Ingredient count changed: " + name);
    for (var entry : expected) {
      if (entry.has("fluid")) {
        FluidStack stack = new FluidStack(fluid(entry), count(entry));
        var match =
            fluids.stream()
                .filter(
                    ingredient ->
                        ingredient.amount() == stack.getAmount() && ingredient.test(stack))
                .findFirst();
        helper.assertTrue(match.isPresent(), "Fluid input changed: " + name);
        fluids.remove(match.get());
      } else {
        ItemStack stack = item(entry).copyWithCount(count(entry));
        var match =
            items.stream()
                .filter(
                    ingredient ->
                        ingredient.count() == count(entry)
                            && !stack.isEmpty()
                            && ingredient.ingredient().test(stack))
                .findFirst();
        helper.assertTrue(match.isPresent(), "Item input changed: " + name + " " + entry);
        items.remove(match.get());
      }
    }
  }

  private static void checkOutputs(
      GameTestHelper helper, String name, JsonObject original, ProcessingRecipe recipe) {
    var expected = entries(original, "results");
    List<ProcessingOutput> items = new ArrayList<>(recipe.outputs());
    List<FluidStack> fluids = new ArrayList<>(recipe.fluidOutputs());
    helper.assertTrue(
        expected.size() == items.size() + fluids.size(), "Result count changed: " + name);
    for (var entry : expected) {
      if (entry.has("fluid")) {
        Fluid fluid = fluid(entry);
        var match =
            fluids.stream()
                .filter(stack -> stack.is(fluid) && stack.getAmount() == count(entry))
                .findFirst();
        helper.assertTrue(match.isPresent(), "Fluid result changed: " + name);
        fluids.remove(match.get());
      } else {
        ItemStack stack = item(entry);
        var match =
            items.stream()
                .filter(
                    output ->
                        output.count() == count(entry)
                            && !stack.isEmpty()
                            && (entry.has("ore")
                                ? stack.getTags().anyMatch(tag -> output.stack().is(tag))
                                : output.matches(stack)))
                .findFirst();
        helper.assertTrue(match.isPresent(), "Item result changed: " + name + " " + entry);
        items.remove(match.get());
      }
    }
  }

  @GameTest(template = "empty")
  public static void originalCraftingOutputs(GameTestHelper helper) throws Exception {
    var rows =
        JsonParser.parseString(Files.readString(Path.of("../gametest/fixtures/original-crafting-outputs.json")))
            .getAsJsonArray();
    for (var entry : rows) {
      var row = entry.getAsJsonArray();
      String name = row.get(1).getAsString();
      var original = row.get(2).getAsJsonObject();
      var expected =
          LegacyArtifactIds.resolve(
              ResourceLocation.parse(original.get("item").getAsString().toLowerCase(Locale.ROOT)),
              original.has("data") ? original.get("data").getAsInt() : 0);
      helper.assertTrue(expected != null, "Original output does not resolve: " + name);
      var recipe =
          helper
              .getLevel()
              .getRecipeManager()
              .byKey(ResourceLocation.parse("adv_rocketry:legacy/" + name))
              .orElseThrow();
      var actual = recipe.value().getResultItem(helper.getLevel().registryAccess());
      helper.assertTrue(
          BuiltInRegistries.ITEM.getKey(actual.getItem()).equals(expected),
          "Original output changed: " + name + " expected " + expected + " got " + actual);
      helper.assertTrue(
          actual.getCount() == (original.has("count") ? original.get("count").getAsInt() : 1),
          "Original output count changed: " + name);
    }
    helper.assertTrue(rows.size() == 140, "Incomplete original recipe inventory");
    helper.succeed();
  }
}

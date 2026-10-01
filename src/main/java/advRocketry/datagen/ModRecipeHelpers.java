package advRocketry.datagen;

import advRocketry.Main;
import advRocketry.processing.MachineType;
import advRocketry.processing.ProcessingOutput;
import advRocketry.processing.ProcessingRecipe;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

abstract class ModRecipeHelpers {
  static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
  }

  static Item registered(String id) {
    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    if (item == Items.AIR) throw new IllegalStateException("Unknown recipe item " + id);
    return item;
  }

  static Ingredient tag(String tag) {
    return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag)));
  }

  static Ingredient item(String id) {
    return Ingredient.of(registered(id));
  }

  static void shaped(
      RecipeOutput output,
      String path,
      String result,
      int count,
      List<String> rows,
      Object... keys) {
    Map<Character, Ingredient> key = new LinkedHashMap<>();
    for (int index = 0; index < keys.length; index += 2)
      key.put((Character) keys[index], (Ingredient) keys[index + 1]);
    output.accept(
        id(path),
        new ShapedRecipe(
            "",
            CraftingBookCategory.MISC,
            ShapedRecipePattern.of(key, rows),
            new ItemStack(registered(result), count)),
        null);
  }

  static void shapeless(
      RecipeOutput output, String path, String result, int count, Ingredient... ingredients) {
    output.accept(
        id(path),
        new ShapelessRecipe(
            "",
            CraftingBookCategory.MISC,
            new ItemStack(registered(result), count),
            NonNullList.of(Ingredient.EMPTY, ingredients)),
        null);
  }

  static void smelting(
      RecipeOutput output,
      String path,
      Ingredient ingredient,
      String result,
      float experience,
      int time) {
    output.accept(
        id(path),
        new SmeltingRecipe(
            "",
            CookingBookCategory.MISC,
            ingredient,
            new ItemStack(registered(result)),
            experience,
            time),
        null);
  }

  record Part(String id, int amount, boolean fluid, boolean consume, int damage) {
    boolean tag() {
      return id.startsWith("#");
    }

    ResourceLocation location() {
      return ResourceLocation.parse(tag() ? id.substring(1) : id);
    }

    SizedIngredient ingredient() {
      if (tag()) return SizedIngredient.of(TagKey.create(Registries.ITEM, location()), amount);
      Item item = registered(id);
      if (damage < 0) return SizedIngredient.of(item, amount);
      return new SizedIngredient(
          DataComponentIngredient.of(false, DataComponents.DAMAGE, damage, item), amount);
    }

    SizedFluidIngredient fluidIngredient() {
      return SizedFluidIngredient.of(fluidType(), amount);
    }

    FluidStack fluidStack() {
      return new FluidStack(fluidType(), amount);
    }

    ProcessingOutput output() {
      if (tag()) return ProcessingOutput.of(TagKey.create(Registries.ITEM, location()), amount);
      Item item = registered(id);
      if (damage <= 0) return ProcessingOutput.of(item, amount);
      return new ProcessingOutput(
          Optional.of(item.builtInRegistryHolder()),
          Optional.empty(),
          amount,
          DataComponentPatch.builder().set(DataComponents.DAMAGE, damage).build(),
          1);
    }

    private Fluid fluidType() {
      Fluid fluid = BuiltInRegistries.FLUID.get(location());
      if (fluid == Fluids.EMPTY) throw new IllegalStateException("Unknown recipe fluid " + id);
      return fluid;
    }
  }

  static Part stack(String id, int amount) {
    return new Part(id, amount, false, true, -1);
  }

  static Part kept(String id, int amount) {
    return new Part(id, amount, false, false, -1);
  }

  static Part damaged(String id, int amount, int damage, boolean consume) {
    return new Part(id, amount, false, consume, damage);
  }

  static Part fluid(String id, int amount) {
    return new Part(id, amount, true, true, -1);
  }

  static ProcessingRecipe.WeightedOutput weighted(Part output, int weight) {
    return new ProcessingRecipe.WeightedOutput(output.output(), weight);
  }

  static void processing(
      RecipeOutput output,
      String path,
      String machine,
      int ticks,
      int energy,
      List<Part> inputs,
      List<Part> outputs) {
    processing(output, path, machine, ticks, energy, inputs, outputs, List.of(), 0);
  }

  static void processing(
      RecipeOutput output,
      String path,
      String machine,
      int ticks,
      int energy,
      List<Part> inputs,
      List<Part> outputs,
      List<ProcessingRecipe.WeightedOutput> weighted,
      int rolls) {
    output.accept(
        id(path),
        new ProcessingRecipe(
            machineType(machine),
            ticks,
            energy,
            inputs.stream()
                .filter(part -> !part.fluid() && part.consume())
                .map(Part::ingredient)
                .toList(),
            inputs.stream()
                .filter(part -> !part.fluid() && !part.consume())
                .map(Part::ingredient)
                .toList(),
            inputs.stream().filter(Part::fluid).map(Part::fluidIngredient).toList(),
            outputs.stream().filter(part -> !part.fluid()).map(Part::output).toList(),
            outputs.stream().filter(Part::fluid).map(Part::fluidStack).toList(),
            weighted,
            rolls),
        null);
  }

  static MachineType machineType(String id) {
    MachineType type = MachineType.byId(id);
    if (type == null) throw new IllegalStateException("Unknown machine " + id);
    return type;
  }
}

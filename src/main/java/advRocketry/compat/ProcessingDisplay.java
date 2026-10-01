// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.compat;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.life.SpaceBreathing;
import advRocketry.life.SpaceSuitItem;
import advRocketry.processing.MachineType;
import advRocketry.processing.MaterialRecipes;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.processing.ProcessingOutput;
import advRocketry.processing.ProcessingRecipe;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Shared recipe displays backed by native recipes and the same dynamic machine rules. */
public record ProcessingDisplay(
    ResourceLocation id, ProcessingRecipe recipe, ItemStack armorInput, ItemStack armorOutput) {
  public record Entry(
      List<ItemStack> items, List<FluidStack> fluids, Component label, List<Component> notes) {
    public boolean fluid() {
      return !fluids.isEmpty();
    }

    public int amount() {
      return fluid()
          ? fluids.getFirst().getAmount()
          : items.isEmpty() ? 0 : items.getFirst().getCount();
    }
  }

  public ProcessingDisplay(ResourceLocation id, ProcessingRecipe recipe) {
    this(id, recipe, ItemStack.EMPTY, ItemStack.EMPTY);
  }

  public List<ProcessingRecipe.WeightedOutput> weightedOutputs() {
    return recipe.weightedPool();
  }

  public List<Entry> inputs() {
    List<Entry> entries = new ArrayList<>();
    for (var ingredient : recipe.inputs()) entries.add(item(ingredient, List.of()));
    for (var ingredient : recipe.catalysts())
      entries.add(
          item(
              ingredient,
              List.of(Component.translatable("gui.adv_rocketry.processing.not_consumed"))));
    for (var ingredient : recipe.fluidInputs()) entries.add(fluid(ingredient));
    if (!armorInput.isEmpty() && !entries.isEmpty())
      entries.set(
          0,
          new Entry(List.of(armorInput.copy()), List.of(), armorInput.getHoverName(), List.of()));
    return entries;
  }

  public List<Entry> outputs() {
    List<Entry> entries = new ArrayList<>();
    for (var output : recipe.outputs()) entries.add(output(output));
    for (var fluid : recipe.fluidOutputs())
      entries.add(new Entry(List.of(), List.of(fluid.copy()), label(fluid), List.of()));
    if (!armorOutput.isEmpty() && !entries.isEmpty())
      entries.set(
          0,
          new Entry(
              List.of(armorOutput.copy()),
              List.of(),
              armorOutput.getHoverName(),
              List.of(
                  Component.translatable(
                      "gui.adv_rocketry.processing_category.preserves_the_armor_s_components"))));
    return entries;
  }

  public static Entry output(ProcessingOutput output) {
    List<Component> notes = new ArrayList<>();
    if (output.chance() < 1)
      notes.add(
          Texts.translate(
              "gui.adv_rocketry.processing.chance_per_item",
              String.format(Locale.ROOT, "%.2f", output.chance() * 100)));
    Component name =
        output.tag().isPresent()
            ? Component.literal("#" + output.tag().get().location())
            : output.stack().getHoverName();
    return new Entry(
        output.displayStacks(),
        List.of(),
        Component.literal(output.count() + " × ").append(name),
        notes);
  }

  private static Entry item(SizedIngredient ingredient, List<Component> notes) {
    List<ItemStack> stacks = List.of(ingredient.getItems());
    return new Entry(
        stacks,
        List.of(),
        Component.literal(ingredient.count() + " × ").append(name(ingredient.ingredient(), stacks)),
        notes);
  }

  private static Component name(Ingredient ingredient, List<ItemStack> stacks) {
    if (!ingredient.isCustom()
        && ingredient.getValues().length == 1
        && ingredient.getValues()[0] instanceof Ingredient.TagValue tag)
      return Component.literal("#" + tag.tag().location());
    return stacks.isEmpty() ? Component.literal("?") : stacks.getFirst().getHoverName();
  }

  private static Entry fluid(SizedFluidIngredient ingredient) {
    List<FluidStack> fluids = List.of(ingredient.getFluids());
    Component label =
        fluids.isEmpty()
            ? Component.literal(ingredient.amount() + " mB")
            : label(fluids.getFirst());
    return new Entry(List.of(), fluids, label, List.of());
  }

  private static Component label(FluidStack fluid) {
    return Component.literal(fluid.getAmount() + " mB ").append(fluid.getHoverName());
  }

  public static Component perRoll(List<ProcessingRecipe.WeightedOutput> pool, long weight) {
    long total = pool.stream().mapToLong(ProcessingRecipe.WeightedOutput::weight).sum();
    return Texts.translate(
        "gui.adv_rocketry.processing.per_roll",
        String.format(Locale.ROOT, "%.2f", weight * 100d / Math.max(1, total)));
  }

  public static List<ProcessingDisplay> collect() {
    var level = Minecraft.getInstance().level;
    if (level == null) return List.of();
    List<ProcessingDisplay> result = new ArrayList<>();
    for (var holder :
        level.getRecipeManager().getAllRecipesFor(ProcessingRegistry.RECIPE_TYPE.get())) {
      if (!ProcessingRecipe.enabled(holder.id())) continue;
      result.add(new ProcessingDisplay(holder.id(), holder.value()));
    }
    if (AdvancedRocketryConfig.makeMaterialsForOtherMods()) {
      Map<ResourceLocation, ProcessingDisplay> materials = new LinkedHashMap<>();
      for (MachineType machine : List.of(MachineType.LATHE, MachineType.ROLLING_MACHINE)) {
        for (var item : BuiltInRegistries.ITEM) {
          ItemStack stack = new ItemStack(item);
          ProcessingRecipe recipe = MaterialRecipes.candidate(machine, stack);
          String material = MaterialRecipes.material(stack);
          if (recipe == null
              || material == null
              || result.stream()
                  .anyMatch(
                      display ->
                          display.recipe().machine() == machine
                              && !display.recipe().inputs().isEmpty()
                              && display.recipe().inputs().getFirst().ingredient().test(stack)))
            continue;
          ResourceLocation id =
              ResourceLocation.fromNamespaceAndPath(
                  Main.MODID, "materials/" + machine.id() + "/" + material);
          materials.putIfAbsent(id, new ProcessingDisplay(id, recipe));
        }
      }
      result.addAll(materials.values());
    }
    if (AdvancedRocketryConfig.enableOxygen()) {
      var seal =
          level
              .registryAccess()
              .lookupOrThrow(Registries.ENCHANTMENT)
              .getOrThrow(SpaceBreathing.KEY);
      for (var item : BuiltInRegistries.ITEM) {
        if (!(item instanceof ArmorItem) || item instanceof SpaceSuitItem) continue;
        ItemStack input = new ItemStack(item), output = input.copy();
        output.enchant(seal, 1);
        var itemId = BuiltInRegistries.ITEM.getKey(item);
        List<SizedIngredient> inputs = new ArrayList<>();
        inputs.add(SizedIngredient.of(item, 1));
        inputs.addAll(ProcessingBlockEntity.sealingMaterials(input));
        var sealing = ProcessingBlockEntity.SEALING_RECIPE;
        var recipe =
            new ProcessingRecipe(
                sealing.machine(),
                sealing.ticks(),
                sealing.energy(),
                inputs,
                List.of(),
                List.of(ProcessingOutput.of(item, 1)));
        result.add(
            new ProcessingDisplay(
                ResourceLocation.fromNamespaceAndPath(
                    Main.MODID, "sealing/" + itemId.getNamespace() + "/" + itemId.getPath()),
                recipe,
                input,
                output));
      }
    }
    return List.copyOf(result);
  }
}

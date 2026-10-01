// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC.

package advRocketry.compat.guideme;

import advRocketry.compat.ProcessingDisplay;
import advRocketry.processing.ProcessingRecipe;
import advRocketry.processing.ProcessingRegistry;
import guideme.color.SymbolicColor;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.block.LytBlock;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Public recipe mapping also makes processing recipes available to other GuideME guides. */
public final class ProcessingRecipeLayouts implements RecipeTypeMappingSupplier {
  @Override
  public void collect(RecipeTypeMappings mappings) {
    mappings.add(
        ProcessingRegistry.RECIPE_TYPE.get(),
        holder ->
            ProcessingRecipe.enabled(holder.id())
                ? create(new ProcessingDisplay(holder.id(), holder.value()))
                : null);
  }

  public static LytBlock create(ProcessingDisplay display) {
    var recipe = display.recipe();
    var controller =
        new ItemStack(
            BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("adv_rocketry", recipe.machine().id())));
    var details = new LytVBox();
    details.setGap(3);
    details.modifyStyle(style -> style.color(SymbolicColor.CRAFTING_RECIPE_TYPE));
    details.append(
        LytParagraph.of(
            I18n.get("gui.adv_rocketry.guide.processing_cost", recipe.ticks(), recipe.energy())));
    var inputs = ingredients(display.inputs(), details, I18n.get("gui.adv_rocketry.guide.input"));
    var outputs =
        ingredients(display.outputs(), details, I18n.get("gui.adv_rocketry.guide.output"));
    var weighted = display.weightedOutputs();
    if (!weighted.isEmpty()) {
      details.append(
          LytParagraph.of(I18n.get("gui.adv_rocketry.guide.random_output", recipe.rolls())));
      for (var output : weighted) {
        details.append(
            LytParagraph.of(
                ProcessingDisplay.output(output.output()).label().getString()
                    + ": "
                    + ProcessingDisplay.perRoll(weighted, output.weight()).getString()));
      }
    }
    if (!display.armorOutput().isEmpty())
      details.append(LytParagraph.of(I18n.get("gui.adv_rocketry.guide.airtight_seal")));
    var box =
        LytStandardRecipeBox.builder()
            .icon(controller)
            .title(controller.getHoverName().getString())
            .input(inputs)
            .output(outputs)
            .addBottom(details)
            .build(new RecipeHolder<>(display.id(), recipe));
    box.setMarginBottom(8);
    return box;
  }

  private static LytSlotGrid ingredients(
      List<ProcessingDisplay.Entry> entries, LytVBox details, String role) {
    List<Ingredient> icons = new ArrayList<>();
    for (var entry : entries) {
      List<ItemStack> stacks = new ArrayList<>(entry.items());
      for (var fluid : entry.fluids()) stacks.add(new ItemStack(fluid.getFluid().getBucket()));
      icons.add(Ingredient.of(stacks.stream().filter(stack -> !stack.isEmpty())));
      String note = role + ": " + entry.label().getString();
      for (Component extra : entry.notes()) note += " (" + extra.getString() + ")";
      details.append(LytParagraph.of(note));
    }
    var grid = new LytSlotGrid(3, Math.max(1, (icons.size() + 2) / 3));
    grid.setRenderEmptySlots(false);
    for (int i = 0; i < icons.size(); i++) grid.setIngredient(i % 3, i / 3, icons.get(i));
    return grid;
  }
}

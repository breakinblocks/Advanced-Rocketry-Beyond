// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC.

package advRocketry.compat.guideme;

import advRocketry.compat.ProcessingDisplay;
import advRocketry.processing.MachineType;
import guideme.compiler.PageCompiler;
import guideme.compiler.tags.BlockTagCompiler;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytParagraph;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import java.util.Set;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Rebuilt on page open so datapack reloads and generated recipes remain accurate. */
public final class MachineRecipesTag extends BlockTagCompiler {
  @Override
  public Set<String> getTagNames() {
    return Set.of("MachineRecipes");
  }

  @Override
  protected void compile(
      PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields element) {
    String machine = element.getAttributeString("machine", "");
    String item = element.getAttributeString("item", "");
    String kind = element.getAttributeString("kind", "all");
    var itemId = ResourceLocation.tryParse(item);
    ItemStack result =
        itemId == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(itemId));
    boolean found = false;
    for (var display : ProcessingDisplay.collect()) {
      if (!machine.isEmpty() && display.recipe().machine() != MachineType.byId(machine)) continue;
      if (kind.equals("sealing") && display.armorOutput().isEmpty()) continue;
      if (kind.equals("processing") && !display.armorOutput().isEmpty()) continue;
      if (!item.isEmpty() && (result.isEmpty() || !produces(display, result))) continue;
      parent.append(ProcessingRecipeLayouts.create(display));
      found = true;
    }
    if (!found) parent.append(LytParagraph.of(I18n.get("gui.adv_rocketry.guide.no_recipes")));
  }

  private static boolean produces(ProcessingDisplay display, ItemStack result) {
    return display.recipe().outputs().stream().anyMatch(output -> output.matches(result))
        || display.weightedOutputs().stream().anyMatch(output -> output.output().matches(result));
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Original cross-mod plate and rod recipes, resolved from modern common material tags. */
public final class MaterialRecipes {
  private MaterialRecipes() {}

  public static ProcessingRecipe candidate(MachineType machine, ItemStack input) {
    if (input.isEmpty() || machine != MachineType.ROLLING_MACHINE && machine != MachineType.LATHE)
      return null;
    for (TagKey<Item> tag : input.getTags().toList()) {
      ResourceLocation id = tag.location();
      if (!id.getNamespace().equals("c") || !id.getPath().startsWith("ingots/")) continue;
      String material = id.getPath().substring("ingots/".length());
      if (material.isEmpty() || material.contains("/")) continue;
      Item output =
          machine == MachineType.ROLLING_MACHINE
              ? first("plates/" + material)
              : first("rods/" + material);
      if (output == null && machine == MachineType.LATHE) output = first("sticks/" + material);
      if (output == null) continue;
      return new ProcessingRecipe(
          machine,
          300,
          20,
          List.of(SizedIngredient.of(tag, 1)),
          machine == MachineType.ROLLING_MACHINE
              ? List.of(SizedFluidIngredient.of(Fluids.WATER, 100))
              : List.of(),
          List.of(ProcessingOutput.of(output, machine == MachineType.LATHE ? 2 : 1)));
    }
    return null;
  }

  public static String material(ItemStack input) {
    for (TagKey<Item> tag : input.getTags().toList()) {
      ResourceLocation id = tag.location();
      if (!id.getNamespace().equals("c") || !id.getPath().startsWith("ingots/")) continue;
      String material = id.getPath().substring("ingots/".length());
      if (!material.isEmpty() && !material.contains("/")) return material;
    }
    return null;
  }

  private static Item first(String path) {
    TagKey<Item> tag =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    ItemStack first = TagItems.representative(tag);
    return first.isEmpty() ? null : first.getItem();
  }
}

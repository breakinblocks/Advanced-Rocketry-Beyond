// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** Original thermite fuel burn duration. */
public final class ThermiteItem extends Item {
  public ThermiteItem(Properties properties) {
    super(properties);
  }

  @Override
  public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
    return 6000;
  }
}

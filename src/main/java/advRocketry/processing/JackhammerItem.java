// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

/** Original diamond-tier jackhammer speed, repaired with titanium rods. */
public final class JackhammerItem extends PickaxeItem {
  public JackhammerItem(Properties properties) {
    super(Tiers.DIAMOND, properties);
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? 50 : super.getDestroySpeed(stack, state);
  }

  @Override
  public boolean isValidRepairItem(ItemStack stack, ItemStack material) {
    return material.is(ProcessingRegistry.PART_ITEMS.get("titanium_rod").get());
  }
}

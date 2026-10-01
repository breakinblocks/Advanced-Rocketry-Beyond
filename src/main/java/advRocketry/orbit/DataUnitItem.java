// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Portable storage for the original distance, composition and mass research data. */
public final class DataUnitItem extends Item {
  public DataUnitItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static StoredResearch research(ItemStack stack) {
    return stack.getOrDefault(ModComponents.STORED_RESEARCH, StoredResearch.EMPTY);
  }

  public static void store(ItemStack stack, ResearchType type, int amount) {
    if (amount <= 0) stack.remove(ModComponents.STORED_RESEARCH);
    else stack.set(ModComponents.STORED_RESEARCH, new StoredResearch(type, amount));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    StoredResearch research = stack.get(ModComponents.STORED_RESEARCH);
    if (research != null && !research.empty())
      tooltip.add(
          research
              .type()
              .displayName()
              .copy()
              .append(": " + research.amount() + " / " + ResearchType.UNIT_CAPACITY));
    else
      tooltip.add(
          Texts.translate("message.adv_rocketry.data_unit.empty_0", ResearchType.UNIT_CAPACITY));
  }
}

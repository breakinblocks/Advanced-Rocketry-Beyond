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

/** Packed block palette for one original-style station module. */
public final class StationItem extends Item {
  public StationItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    if (stack.has(ModComponents.STATION_LINK))
      tooltip.add(Texts.translate("message.adv_rocketry.station.station", StationLink.read(stack)));
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.items.ComponentItemHandler;

/** Chassis and assembled satellite, with native data components in place of legacy item NBT. */
public final class SatelliteItem extends Item {
  public SatelliteItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static boolean assembled(ItemStack stack) {
    return stack.getItem() instanceof SatelliteItem && stack.has(ModComponents.SATELLITE);
  }

  public static ComponentItemHandler modules(ItemStack chassis) {
    return new ComponentItemHandler(chassis, DataComponents.CONTAINER, 7) {
      @Override
      public int getSlotLimit(int slot) {
        return 1;
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return !assembled(chassis)
            && (stack.isEmpty()
                || (slot == 0
                    ? OrbitalRegistry.type(stack) != null
                    : OrbitalRegistry.module(stack)));
      }
    };
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    SatelliteProperties properties = stack.get(ModComponents.SATELLITE);
    if (properties != null) {
      if (properties.type() != null)
        tooltip.add(
            Texts.translate(
                "message.adv_rocketry.satellite.type", properties.type().displayName()));
      tooltip.add(
          Texts.translate(
              "message.adv_rocketry.satellite.power_fe_t_fe_storage",
              properties.powerGeneration(),
              properties.powerStorage()));
      tooltip.add(Texts.translate("message.adv_rocketry.satellite.satellite", properties.id()));
    } else {
      ComponentItemHandler contents = modules(stack);
      for (int slot = 0; slot < contents.getSlots(); slot++) {
        ItemStack module = contents.getStackInSlot(slot);
        if (!module.isEmpty()) tooltip.add(module.getHoverName());
      }
    }
  }
}

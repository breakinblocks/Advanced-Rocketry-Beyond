// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/** Original 1,000 / 2,000 / 4,000 / 8,000 mB tank tiers before the server multiplier. */
public final class PressureTankItem extends Item {
  private final int capacity;

  public PressureTankItem(int capacity, Properties properties) {
    super(properties.stacksTo(1));
    this.capacity = capacity;
  }

  public int capacity() {
    return Math.max(
        0, (int) Math.round(capacity * AdvancedRocketryConfig.suitTankCapacityMultiplier()));
  }

  public FluidHandlerItemStack handler(ItemStack stack) {
    return new FluidHandlerItemStack(ModComponents.TANK_CONTENT, stack, capacity());
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    var fluid = handler(stack).getFluid();
    tooltip.add(
        Texts.translate(
            "message.adv_rocketry.pressure_tank.mb",
            fluid.isEmpty()
                ? Component.translatable("gui.adv_rocketry.fluid.empty")
                : fluid.getHoverName(),
            fluid.getAmount(),
            capacity()));
  }
}

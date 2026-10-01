// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Original asteroid ID and three research-data channels, stored in native item components. */
public final class AsteroidChipItem extends Item {
  public static final int MAX_DATA = ResearchType.UNIT_CAPACITY;

  public AsteroidChipItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static boolean programmed(ItemStack stack) {
    return stack.getItem() instanceof AsteroidChipItem && stack.has(ModComponents.ASTEROID);
  }

  public static AsteroidTarget target(ItemStack stack) {
    return stack.getOrDefault(ModComponents.ASTEROID, AsteroidTarget.NONE);
  }

  public static int data(ItemStack stack, ResearchType type) {
    return stack.getOrDefault(ModComponents.ASTEROID_RESEARCH, AsteroidResearch.EMPTY).get(type);
  }

  public static int addData(ItemStack stack, ResearchType type, int amount) {
    if (!(stack.getItem() instanceof AsteroidChipItem)
        || !ResearchType.ASTEROID_TYPES.contains(type)) return 0;
    AsteroidResearch research =
        stack.getOrDefault(ModComponents.ASTEROID_RESEARCH, AsteroidResearch.EMPTY);
    int previous = research.get(type);
    int accepted = Math.max(0, Math.min(amount, MAX_DATA - previous));
    if (accepted > 0)
      stack.set(ModComponents.ASTEROID_RESEARCH, research.with(type, previous + accepted));
    return accepted;
  }

  public static void program(ItemStack stack, String type, long seed) {
    stack.set(ModComponents.ASTEROID, new AsteroidTarget(type, seed));
  }

  public static void erase(ItemStack stack) {
    stack.remove(ModComponents.ASTEROID);
    stack.remove(ModComponents.ASTEROID_RESEARCH);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    AsteroidTarget target = target(stack);
    tooltip.add(
        programmed(stack)
            ? Component.literal(target.type() + " #" + target.seed())
            : Component.translatable(
                "message.adv_rocketry.asteroid_chip.unprogrammed_asteroid_chip"));
    for (ResearchType type : ResearchType.ASTEROID_TYPES)
      tooltip.add(type.displayName().copy().append(": " + data(stack, type) + " / " + MAX_DATA));
  }
}

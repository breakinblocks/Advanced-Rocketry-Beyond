// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/** Original airtight-seal enchantment and its oxygen buffer for ordinary armor. */
public final class SpaceBreathing {
  public static final ResourceKey<Enchantment> KEY =
      ResourceKey.create(
          Registries.ENCHANTMENT,
          ResourceLocation.fromNamespaceAndPath(Main.MODID, "spacebreathing"));

  private SpaceBreathing() {}

  public static boolean sealed(Level level, ItemStack stack) {
    if (!(stack.getItem() instanceof ArmorItem)) return false;
    var enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(KEY);
    return stack.getEnchantmentLevel(enchantment) > 0;
  }

  public static int air(ItemStack stack) {
    return Math.min(
        AdvancedRocketryConfig.sealedArmorAirCapacity(),
        Math.max(0, stack.getOrDefault(ModComponents.SEALED_AIR, 0)));
  }

  public static int fill(ItemStack stack, int available) {
    int moved =
        Math.min(
            Math.max(0, available),
            Math.max(0, AdvancedRocketryConfig.sealedArmorAirCapacity() - air(stack)));
    if (moved > 0) stack.set(ModComponents.SEALED_AIR, air(stack) + moved);
    return moved;
  }

  public static boolean consume(ItemStack stack) {
    int remaining = air(stack);
    if (remaining <= 0) return false;
    stack.set(ModComponents.SEALED_AIR, remaining - 1);
    return true;
  }
}

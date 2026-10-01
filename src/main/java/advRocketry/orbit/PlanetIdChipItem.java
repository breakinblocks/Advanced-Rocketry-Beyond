// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.space.Planet;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Original planet identification chip, using native item component storage. */
public final class PlanetIdChipItem extends Item {
  public PlanetIdChipItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static int planet(ItemStack stack) {
    return programmed(stack) ? stack.get(ModComponents.PLANET).id() : -1;
  }

  public static boolean programmed(ItemStack stack) {
    return stack.getItem() instanceof PlanetIdChipItem && stack.has(ModComponents.PLANET);
  }

  public static void program(ItemStack stack, Planet planet) {
    stack.set(ModComponents.PLANET, new ProgrammedPlanet(planet.id, planet.name));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    ProgrammedPlanet planet = stack.get(ModComponents.PLANET);
    tooltip.add(
        programmed(stack)
            ? Component.literal(planet.name() + " (#" + planet.id() + ")")
            : Component.translatable(
                "message.adv_rocketry.planet_id_chip.unprogrammed_planet_chip"));
  }
}

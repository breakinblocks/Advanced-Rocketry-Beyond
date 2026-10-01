// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiTheme;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.life.OxygenMenu;
import advRocketry.util.Texts;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class OxygenScreen extends ModContainerScreen<OxygenMenu> {
  public OxygenScreen(OxygenMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 220;
    imageHeight = 104;
  }

  private static String format(int value) {
    return String.format(Locale.ROOT, "%,d", value);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    Component fluid =
        Component.translatable(
            menu.charger() && menu.value(4) == 1
                ? "fluid_type.adv_rocketry.hydrogen"
                : "fluid_type.adv_rocketry.oxygen");
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.oxygen.mb",
            fluid,
            menu.value(1),
            format(OxygenBlockEntity.TANK_CAPACITY)),
        8,
        30,
        UiTheme.MUTED,
        false);
    if (menu.charger()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.oxygen.stand_here_to_refill_your_suit"),
          8,
          62,
          UiTheme.TEXT,
          false);
      return;
    }
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.oxygen.energy_fe",
            menu.value(0),
            format(OxygenBlockEntity.ENERGY_CAPACITY)),
        8,
        46,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.oxygen.room_volume", menu.value(2)),
        8,
        62,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        menu.value(3) == 1
            ? Component.translatable("gui.adv_rocketry.oxygen.supplying_breathable_air")
            : Component.translatable("gui.adv_rocketry.oxygen.inactive"),
        8,
        80,
        menu.value(3) == 1 ? UiTheme.POSITIVE : UiTheme.MUTED,
        false);
  }
}

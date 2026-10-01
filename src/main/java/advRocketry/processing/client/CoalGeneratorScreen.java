// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiTheme;
import advRocketry.processing.CoalGeneratorMenu;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact native readout for the original LibVulpes coal generator. */
public final class CoalGeneratorScreen extends ModContainerScreen<CoalGeneratorMenu> {
  public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 176;
    imageHeight = 166;
    inventoryLabelY = 72;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);

    UiTheme.progress(graphics, leftPos + 111, topPos + 46, 52, 6, menu.energy() / 10000d);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    drawText(
        graphics,
        Component.translatable("gui.adv_rocketry.coal_generator.fuel"),
        8,
        37,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.coal_generator.fe", menu.energy()),
        103,
        29,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.coal_generator.ticks", menu.burnTime()),
        103,
        58,
        UiTheme.TEXT,
        false);
    drawText(graphics, playerInventoryTitle, 8, inventoryLabelY, UiTheme.TEXT, false);
  }
}

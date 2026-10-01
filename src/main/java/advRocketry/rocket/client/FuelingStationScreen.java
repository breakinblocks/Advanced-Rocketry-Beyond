// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.FuelingStationMenu;
import advRocketry.rocket.InfrastructureRedstone;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FuelingStationScreen extends ModContainerScreen<FuelingStationMenu> {
  private Button output;

  public FuelingStationScreen(FuelingStationMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageHeight = 174;
    inventoryLabelY = 80;
  }

  private Component outputLabel() {
    return Texts.translate(
        "gui.adv_rocketry.fueling_station.output",
        InfrastructureRedstone.Mode.values()[Math.clamp(menu.outputMode(), 0, 2)].name());
  }

  @Override
  protected void init() {
    super.init();
    output =
        addRenderableWidget(
            UiButton.builder(
                    outputLabel(),
                    button -> {
                      if (minecraft.gameMode != null)
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    })
                .bounds(leftPos + 8, topPos + 54, 160, 20)
                .build());
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    output.setMessage(outputLabel());
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.fueling_station.5000_mb", menu.fluidAmount()),
        8,
        24,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.fueling_station.1000_fe", menu.energy()),
        8,
        38,
        UiTheme.TEXT,
        false);
    drawText(graphics, Component.literal(">"), 138, 30, UiTheme.TEXT, false);
    drawText(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, UiTheme.TEXT, false);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.InfrastructureRedstone;
import advRocketry.rocket.RocketMonitoringMenu;
import advRocketry.util.Texts;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RocketMonitoringScreen extends ModContainerScreen<RocketMonitoringMenu> {
  private Button launch, redstone;

  public RocketMonitoringScreen(RocketMonitoringMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 226;
    imageHeight = 210;
  }

  private Component redstoneLabel() {
    return Texts.translate(
        "gui.adv_rocketry.rocket_monitoring.launch_input",
        InfrastructureRedstone.Mode.values()[Math.clamp(menu.value(8), 0, 2)].name());
  }

  private void press(int id) {
    if (minecraft.gameMode != null)
      minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
  }

  @Override
  protected void init() {
    super.init();
    launch =
        addRenderableWidget(
            UiButton.builder(
                    Component.translatable("gui.adv_rocketry.rocket_monitoring.launch"),
                    button -> press(0))
                .bounds(leftPos + 8, topPos + 179, 62, 20)
                .build());
    redstone =
        addRenderableWidget(
            UiButton.builder(redstoneLabel(), button -> press(1))
                .bounds(leftPos + 74, topPos + 179, 144, 20)
                .build());
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    launch.active = menu.value(9) != 0 && menu.value(4) == 0 && menu.value(7) == 0;
    redstone.setMessage(redstoneLabel());
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    String state =
        menu.value(7) != 0
            ? "gui.adv_rocketry.rocket_monitoring.mission_in_progress"
            : menu.value(9) == 0
                ? "gui.adv_rocketry.rocket_monitoring.no_rocket"
                : switch (menu.value(4)) {
                  case 0 -> "gui.adv_rocketry.rocket_monitoring.ready";
                  case 1 -> "gui.adv_rocketry.rocket_monitoring.ascending";
                  case 2 -> "gui.adv_rocketry.rocket_monitoring.returning";
                  default -> "gui.adv_rocketry.rocket_monitoring.in_space";
                };
    drawText(graphics, Component.translatable(state), 8, 23, UiTheme.MUTED, false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.rocket_monitoring.height_m", menu.value(0)),
        8,
        40,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.rocket_monitoring.vertical_speed_m_t",
            String.format(Locale.ROOT, "%.2f", menu.value(1) / 100d)),
        8,
        53,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.rocket_monitoring.fuel_mb", menu.value(2), menu.value(3)),
        8,
        66,
        UiTheme.TEXT,
        false);
    int seconds = menu.value(5);
    String mission =
        menu.value(7) == 2
            ? "gui.adv_rocketry.rocket_monitoring.gas_collection"
            : menu.value(7) == 1
                ? "gui.adv_rocketry.rocket_monitoring.asteroid_mining"
                : "gui.adv_rocketry.rocket_monitoring.no_mission";
    drawText(graphics, Component.translatable(mission), 8, 88, UiTheme.TEXT, false);
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.rocket_monitoring.remaining",
            String.format(
                Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)),
        8,
        101,
        UiTheme.TEXT,
        false);
    String[] phases = {
      "gui.adv_rocketry.rocket_monitoring.outbound",
      "gui.adv_rocketry.rocket_monitoring.working",
      "gui.adv_rocketry.rocket_monitoring.inbound"
    };
    for (int phase = 0; phase < 3; phase++) {
      int y = 121 + phase * 17;
      drawText(graphics, Component.translatable(phases[phase]), 8, y, UiTheme.TEXT, false);
      UiTheme.progress(graphics, 74, y, 144, 10, menu.value(6) / 1000d * 3 - phase);
    }
  }
}

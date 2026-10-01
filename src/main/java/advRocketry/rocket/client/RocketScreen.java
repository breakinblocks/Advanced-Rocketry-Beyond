// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.RocketMenu;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.space.GalaxySync;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RocketScreen extends ModContainerScreen<RocketMenu> {
  public RocketScreen(RocketMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 236;
    imageHeight =
        menu.entity.kind() == RocketPartBlock.Kind.STATION_BUILDER
                || menu.entity.kind() == RocketPartBlock.Kind.GUIDANCE
            ? 210
            : 144;
  }

  @Override
  protected void init() {
    super.init();
    if (menu.entity.kind() == RocketPartBlock.Kind.BUILDER
        || menu.entity.kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER) {
      button(
          Component.translatable("gui.adv_rocketry.rocket.scan"), 0, leftPos + 8, topPos + 110, 68);
      button(
          Component.translatable("gui.adv_rocketry.rocket.assemble"),
          1,
          leftPos + 84,
          topPos + 110,
          68);
      button(
          Component.translatable("gui.adv_rocketry.rocket.launch"),
          2,
          leftPos + 160,
          topPos + 110,
          68);
    } else if (menu.entity.kind() == RocketPartBlock.Kind.STATION_BUILDER) {
      button(
          Component.translatable("gui.adv_rocketry.rocket.pack_station"),
          4,
          leftPos + 8,
          topPos + 82,
          220);
    } else if (menu.entity.kind() == RocketPartBlock.Kind.GUIDANCE)
      button(
          Component.translatable("gui.adv_rocketry.rocket.next_destination"),
          3,
          leftPos + 8,
          topPos + 82,
          220);
  }

  private void button(Component label, int action, int x, int y, int width) {
    addRenderableWidget(
        UiButton.builder(
                label,
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action))
            .bounds(x, y, width, 20)
            .build());
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    if (menu.entity.kind() == RocketPartBlock.Kind.STATION_BUILDER) {
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.rocket.energy_fe", menu.value(4) * 100),
          8,
          28,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.rocket.hatch_chip_module_chip"),
          8,
          42,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 104, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.kind() == RocketPartBlock.Kind.GUIDANCE) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.rocket.asteroid_or_station_chip"),
          8,
          42,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 104, UiTheme.TEXT, false);
      return;
    }
    var planet = GalaxySync.clientPlanet(menu.value(0));
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.rocket.destination",
            (planet == null ? Integer.toString(menu.value(0)) : planet.name)),
        8,
        28,
        UiTheme.MUTED,
        false);
    if (menu.entity.kind() == RocketPartBlock.Kind.BUILDER
        || menu.entity.kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER) {
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.rocket.mass_thrust", menu.value(1), menu.value(2)),
          8,
          46,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.rocket.tank_capacity_mb", menu.value(3)),
          8,
          62,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.rocket.energy_fe", menu.value(4) * 100),
          8,
          78,
          UiTheme.TEXT,
          false);
      if (menu.value(5) > 0)
        drawText(
            graphics,
            Texts.translate(
                "gui.adv_rocketry.rocket.assembling",
                (menu.value(6) - menu.value(5)),
                menu.value(6)),
            8,
            94,
            UiTheme.WARNING,
            false);
    } else if (menu.entity.kind() == RocketPartBlock.Kind.TANK)
      drawText(
          graphics,
          Component.translatable(
              "gui.adv_rocketry.rocket.fill_with_the_matching_propellant_bucket"),
          8,
          48,
          UiTheme.TEXT,
          false);
    else
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.rocket.board_the_rocket_and_press_jump"),
          8,
          48,
          UiTheme.TEXT,
          false);
  }
}

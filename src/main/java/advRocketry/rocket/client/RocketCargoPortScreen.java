// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.InfrastructureRedstone;
import advRocketry.rocket.RocketCargoPortMenu;
import advRocketry.util.Texts;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RocketCargoPortScreen extends ModContainerScreen<RocketCargoPortMenu> {
  private final Button[] controls = new Button[8];

  public RocketCargoPortScreen(RocketCargoPortMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 226;
    imageHeight = 226;
    inventoryLabelX = 32;
    inventoryLabelY = 132;
  }

  @Override
  protected void init() {
    super.init();
    if (menu.guidance()) {
      for (int index = 0; index < 5; index++) {
        final int id = index == 0 ? 1 : index + 7;
        int x = index < 2 ? 8 : 8 + (index - 2) * 70;
        int y = index < 2 ? 60 + index * 22 : 104;
        controls[index] =
            addRenderableWidget(
                UiButton.builder(
                        label(index),
                        button -> {
                          if (minecraft.gameMode != null)
                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                        })
                    .bounds(leftPos + x, topPos + y, index < 2 ? 210 : 68, 18)
                    .build());
      }
      return;
    }
    for (int index = 0; index < controls.length; index++) {
      final int id = index;
      int x = index < 2 ? 8 + index * 106 : 8 + (index - 2) % 3 * 70;
      int y = index < 2 ? 70 : 92 + (index - 2) / 3 * 19;
      controls[index] =
          addRenderableWidget(
              UiButton.builder(
                      label(index),
                      button -> {
                        if (minecraft.gameMode != null)
                          minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                      })
                  .bounds(leftPos + x, topPos + y, index < 2 ? 104 : 68, 18)
                  .build());
    }
  }

  private Component label(int index) {
    if (menu.guidance()) {
      if (index == 0)
        return Texts.translate(
            "gui.adv_rocketry.rocket_cargo_port.output", mode(menu.redstoneValue(1)));
      String name =
          switch (index) {
            case 1 -> "gui.adv_rocketry.cargo_port.auto_eject";
            case 2 -> "gui.adv_rocketry.cargo_port.satellite";
            case 3 -> "gui.adv_rocketry.cargo_port.planet";
            default -> "gui.adv_rocketry.cargo_port.station";
          };
      return Texts.translate(
          "gui.adv_rocketry.cargo_port.option",
          Component.translatable(name),
          Component.translatable(
              menu.ejectOption(index - 1)
                  ? "gui.adv_rocketry.cargo_port.on"
                  : "gui.adv_rocketry.cargo_port.off"));
    }
    int value = menu.redstoneValue(index);
    if (index < 2)
      return Component.translatable(
          index == 0
              ? "gui.adv_rocketry.cargo_port.input_mode"
              : "gui.adv_rocketry.cargo_port.output_mode",
          mode(value));
    String side = Direction.values()[index - 2].getName();
    return Texts.translate(
        "gui.adv_rocketry.cargo_port.option",
        side,
        Component.translatable(
            switch (value) {
              case 1 -> "gui.adv_rocketry.cargo_port.side_out";
              case 2 -> "gui.adv_rocketry.cargo_port.side_in";
              default -> "gui.adv_rocketry.cargo_port.side_off";
            }));
  }

  private static Component mode(int value) {
    return Component.translatable(
        "gui.adv_rocketry.redstone_mode."
            + InfrastructureRedstone.Mode.values()[Math.clamp(value, 0, 2)]
                .name()
                .toLowerCase(Locale.ROOT));
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    for (int index = 0; index < controls.length; index++)
      if (controls[index] != null) controls[index].setMessage(label(index));
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);

    if (menu.fluid())
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.rocket_cargo_port.fluid_16000_mb", menu.fluidAmount()),
          leftPos + 8,
          topPos + 53,
          UiTheme.TEXT,
          false);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    drawText(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, UiTheme.TEXT, false);
  }
}

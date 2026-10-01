// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.RocketConsoleMenu;
import advRocketry.space.GalaxySync;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RocketConsoleScreen extends ModContainerScreen<RocketConsoleMenu> {
  private int padOffset;
  private Button destinationButton;
  private final Button[] padRows = new Button[4];

  public RocketConsoleScreen(RocketConsoleMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 390;
    imageHeight = Math.max(216, menu.inventoryY + 92);
  }

  private Button button(Component label, int action, int x, int y, int width) {
    return addRenderableWidget(
        UiButton.builder(
                label,
                clicked -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action))
            .bounds(leftPos + x, topPos + y, width, 20)
            .build());
  }

  @Override
  protected void init() {
    super.init();
    button(Component.literal("<"), 0, 14, 40, 22).active = menu.pages > 1;
    button(Component.literal(">"), 1, 164, 40, 22).active = menu.pages > 1;
    destinationButton =
        button(Component.translatable("gui.adv_rocketry.rocket.next_destination"), 4, 204, 40, 172);
    button(Component.translatable("gui.adv_rocketry.rocket.automatic_pad"), 5, 204, 68, 172);
    for (int row = 0; row < padRows.length; row++) {
      final int index = row;
      padRows[row] =
          addRenderableWidget(
              UiButton.builder(
                      Component.empty(),
                      clicked ->
                          minecraft.gameMode.handleInventoryButtonClick(
                              menu.containerId, 10 + padOffset + index))
                  .bounds(leftPos + 204, topPos + 92 + row * 22, 172, 20)
                  .build());
    }
    button(
        Component.translatable("gui.adv_rocketry.rocket.disassemble"),
        2,
        204,
        imageHeight - 30,
        84);
    button(Component.translatable("gui.adv_rocketry.rocket.launch"), 3, 294, imageHeight - 30, 82);
    refreshPads();
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    destinationButton.active = menu.value(3) != 0;
    destinationButton.setMessage(
        destinationButton.active
            ? Component.translatable("gui.adv_rocketry.rocket_console.next_destination")
            : Component.translatable("gui.adv_rocketry.rocket_console.destination_set_by_chip"));
  }

  private void refreshPads() {
    for (int row = 0; row < padRows.length; row++) {
      int index = padOffset + row;
      padRows[row].visible = index < menu.pads.size();
      if (!padRows[row].visible) continue;
      var pad = menu.pads.get(index);
      String label =
          (pad.position().equals(menu.selectedPad) ? "> " : "")
              + (pad.name().isBlank()
                  ? pad.position().getX() + ", " + pad.position().getZ()
                  : pad.name())
              + (pad.occupied() ? " (occupied)" : "");
      padRows[row].setMessage(Component.literal(label));
      ((UiButton) padRows[row]).selected(pad.position().equals(menu.selectedPad));
      padRows[row].active = !pad.occupied();
    }
  }

  @Override
  public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    if (x >= leftPos + 204 && x < leftPos + 376 && y >= topPos + 92 && y < topPos + 180) {
      padOffset =
          Math.max(
              0,
              Math.min(
                  Math.max(0, menu.pads.size() - padRows.length),
                  padOffset - (int) Math.signum(vertical)));
      refreshPads();
      return true;
    }
    return super.mouseScrolled(x, y, horizontal, vertical);
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
        Texts.translate("gui.adv_rocketry.rocket_console.fuel", menu.value(0), menu.value(1)),
        14,
        25,
        UiTheme.MUTED,
        false);
    drawText(
        graphics,
        font.plainSubstrByWidth(menu.inventoryName.getString(), 120),
        40,
        46,
        UiTheme.TEXT,
        false);
    var planet = GalaxySync.clientPlanet(menu.value(2));
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.rocket.destination_short",
            planet == null ? menu.value(2) : planet.name),
        204,
        25,
        UiTheme.MUTED,
        false);
    drawText(graphics, playerInventoryTitle, 16, menu.inventoryY - 11, UiTheme.TEXT, false);
  }
}

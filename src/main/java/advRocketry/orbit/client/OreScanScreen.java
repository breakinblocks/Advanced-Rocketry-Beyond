// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.OreScanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class OreScanScreen extends ModContainerScreen<OreScanMenu> {
  public OreScanScreen(OreScanMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 196;
    imageHeight = 215;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        int ore = menu.pixel(x, z);
        int color =
            0xff000000 | (Math.min(255, ore + 25) << 16) | (Math.max(0, 90 - ore / 4) << 8) | 35;
        graphics.fill(
            leftPos + 18 + x * 10,
            topPos + 28 + z * 10,
            leftPos + 28 + x * 10,
            topPos + 38 + z * 10,
            color);
      }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    drawText(
        graphics,
        Component.translatable("gui.adv_rocketry.ore_scan.nearby_ore_density"),
        18,
        194,
        UiTheme.MUTED,
        false);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.client.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Common window, slot, title and tooltip treatment for every native mod menu. */
public abstract class ModContainerScreen<T extends AbstractContainerMenu>
    extends AbstractContainerScreen<T> {
  private record TextRegion(Component text, int x, int y, int width) {}

  private final List<TextRegion> clippedText = new ArrayList<>();
  private int designHeight;

  protected void drawText(
      GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
    drawText(graphics, Component.literal(text), x, y, color, shadow);
  }

  protected void drawText(
      GuiGraphics graphics, Component text, int x, int y, int color, boolean shadow) {
    int available = Math.max(0, imageWidth - x - 8);
    if (font.width(text) > available) clippedText.add(new TextRegion(text, x, y, available));
    graphics.drawString(font, UiTheme.fit(font, text, available), x, y, color, shadow);
  }

  protected ModContainerScreen(T menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @Override
  protected void init() {
    if (designHeight == 0) designHeight = imageHeight;
    imageHeight = designHeight;
    int contentBottom = menu.slots.stream().mapToInt(slot -> slot.y + 17).max().orElse(0);
    if (imageHeight > height - 4 && contentBottom <= height)
      imageHeight = Math.max(height - 4, contentBottom);
    super.init();
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    UiTheme.window(graphics, leftPos, topPos, imageWidth, imageHeight);
    for (var slot : menu.slots)
      if (slot.isActive()) UiTheme.slot(graphics, leftPos + slot.x, topPos + slot.y);
  }

  protected void drawTitle(GuiGraphics graphics) {
    UiTheme.title(graphics, font, title, 8, 8, imageWidth - 16);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    graphics.drawString(
        font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, UiTheme.MUTED, false);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    clippedText.clear();
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);
    for (var region : clippedText)
      if (mouseX >= leftPos + region.x()
          && mouseX < leftPos + region.x() + region.width()
          && mouseY >= topPos + region.y()
          && mouseY < topPos + region.y() + font.lineHeight)
        graphics.renderTooltip(
            font, font.split(region.text(), Math.min(300, width - 24)), mouseX, mouseY);
    if (font.width(title) > imageWidth - 16
        && mouseX >= leftPos + 4
        && mouseX < leftPos + imageWidth - 4
        && mouseY >= topPos + 4
        && mouseY < topPos + 20) graphics.renderTooltip(font, title, mouseX, mouseY);
  }
}

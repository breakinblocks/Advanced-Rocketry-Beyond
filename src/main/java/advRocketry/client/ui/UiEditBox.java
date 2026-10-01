// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Native editable text with shared text and focus colors. */
public final class UiEditBox extends EditBox {
  public UiEditBox(Font font, int x, int y, int width, int height, Component label) {
    super(font, x, y, width, height, label);
    setTextColor(UiTheme.TEXT);
    setTextColorUneditable(UiTheme.DISABLED);
    setTextShadow(false);
  }

  @Override
  public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.renderWidget(graphics, mouseX, mouseY, partialTick);
    if (isVisible() && isBordered())
      UiTheme.outline(
          graphics,
          getX(),
          getY(),
          getWidth(),
          getHeight(),
          isFocused() ? UiTheme.ACCENT : UiTheme.BORDER);
  }
}

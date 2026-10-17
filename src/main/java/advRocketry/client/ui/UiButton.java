// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Flat native button retaining keyboard focus, narration, tooltips and menu actions. */
public class UiButton extends Button {
  private boolean selected;
  private ResourceLocation icon;

  public UiButton icon(ResourceLocation texture) {
    icon = texture;
    return this;
  }

  private boolean automaticTooltip;

  protected UiButton(Builder builder) {
    super(builder);
  }

  public static Builder builder(Component message, OnPress onPress) {
    return new UiBuilder(message, onPress);
  }

  public void selected(boolean value) {
    selected = value;
  }

  @Override
  public void setMessage(Component message) {
    super.setMessage(message);
    if (automaticTooltip) {
      setTooltip(null);
      automaticTooltip = false;
    }
  }

  @Override
  protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    var font = Minecraft.getInstance().font;
    boolean highlighted = isHoveredOrFocused();
    int background =
        !active && !selected
            ? UiTheme.INSET
            : highlighted || selected ? UiTheme.HEADER : UiTheme.PANEL;
    graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
    UiTheme.outline(
        graphics,
        getX(),
        getY(),
        width,
        height,
        isFocused() || selected ? UiTheme.ACCENT : UiTheme.BORDER);
    int inset = icon == null ? 0 : 18;
    if (icon != null)
      graphics.blit(icon, getX() + 3, getY() + (height - 16) / 2, 0, 0, 16, 16, 16, 16);
    String label = UiTheme.fit(font, getMessage(), width - 8 - inset);
    if (font.width(getMessage()) > width - 8 - inset && getTooltip() == null) {
      setTooltip(Tooltip.create(getMessage()));
      automaticTooltip = true;
    }
    graphics.drawString(
        font,
        label,
        getX() + inset + (width - inset - font.width(label)) / 2,
        getY() + (height - 8) / 2,
        selected ? UiTheme.ACCENT : active ? UiTheme.TEXT : UiTheme.DISABLED,
        false);
  }

  private static final class UiBuilder extends Builder {
    UiBuilder(Component message, OnPress onPress) {
      super(message, onPress);
    }

    @Override
    public Button build() {
      return new UiButton(this);
    }
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Shared native UI styling, following BBLib's dark-panel visual conventions. */
public final class UiTheme {
  public static final int WINDOW = 0xff16191c;
  public static final int PANEL = 0xff1b1f23;
  public static final int INSET = 0xff0e1113;
  public static final int HEADER = 0xff12403c;
  public static final int BORDER = 0xff454c52;
  public static final int TEXT = 0xffe4e9ec;
  public static final int MUTED = 0xff8c969c;
  public static final int DISABLED = 0xff5c666c;
  public static final int ACCENT = 0xff35f1d7;
  public static final int ACCENT_DIM = 0xff17544b;
  public static final int POSITIVE = 0xff58e07e;
  public static final int WARNING = 0xffe0b458;
  public static final int NEGATIVE = 0xffe05858;
  public static final int HUD_BACKGROUND = 0xd016191c;
  private static final int METER_SEGMENTS = 10;

  private UiTheme() {}

  public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
    graphics.fill(x, y, x + width, y + 1, color);
    graphics.fill(x, y + height - 1, x + width, y + height, color);
    graphics.fill(x, y, x + 1, y + height, color);
    graphics.fill(x + width - 1, y, x + width, y + height, color);
  }

  public static void window(GuiGraphics graphics, int x, int y, int width, int height) {
    graphics.fill(x, y, x + width, y + height, WINDOW);
    outline(graphics, x, y, width, height, BORDER);
    graphics.fill(x + 4, y + 4, x + width - 4, y + 19, HEADER);
    graphics.fill(x + 4, y + 19, x + width - 4, y + 20, ACCENT_DIM);
  }

  public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
    graphics.fill(x, y, x + width, y + height, WINDOW);
    outline(graphics, x, y, width, height, BORDER);
  }

  public static void slot(GuiGraphics graphics, int x, int y) {
    graphics.fill(x - 1, y - 1, x + 17, y + 17, BORDER);
    graphics.fill(x, y, x + 16, y + 16, INSET);
  }

  public static void progress(
      GuiGraphics graphics, int x, int y, int width, int height, double value) {
    graphics.fill(x, y, x + width, y + height, INSET);
    int fill = Double.isFinite(value) ? (int) (Math.clamp(value, 0, 1) * width) : 0;
    if (fill > 0) {
      graphics.fill(x, y, x + fill, y + height, ACCENT_DIM);
      graphics.fill(x + fill - 1, y, x + fill, y + height, ACCENT);
    }
    outline(graphics, x - 1, y - 1, width + 2, height + 2, BORDER);
  }

  public static void hudPanel(
      GuiGraphics graphics, int x, int y, int width, int height, int accent) {
    graphics.fill(x, y, x + width, y + height, HUD_BACKGROUND);
    outline(graphics, x, y, width, height, BORDER);
    graphics.fill(x + 1, y + 1, x + 3, y + height - 1, accent);
  }

  public static void meter(
      GuiGraphics graphics, int x, int y, int width, int height, double value, int color) {
    graphics.fill(x, y, x + width, y + height, INSET);
    int fill = Double.isFinite(value) ? (int) Math.round(Math.clamp(value, 0, 1) * width) : 0;
    if (fill > 0) graphics.fill(x, y, x + fill, y + height, color);
    for (int segment = 1; segment < METER_SEGMENTS; segment++) {
      int divider = x + width * segment / METER_SEGMENTS;
      graphics.fill(divider, y, divider + 1, y + height, INSET);
    }
    outline(graphics, x - 1, y - 1, width + 2, height + 2, BORDER);
  }

  public static int level(double fraction) {
    return fraction <= .1 ? NEGATIVE : fraction <= .25 ? WARNING : ACCENT;
  }

  public static boolean pulse(int tick) {
    return tick / 10 % 2 == 0;
  }

  public static void marker(GuiGraphics graphics, int x, int y, int color) {
    for (int row = 0; row < 4; row++)
      graphics.fill(x - 3 + row, y + row, x + 4 - row, y + row + 1, color);
  }

  public static String fit(Font font, Component text, int width) {
    String value = text.getString();
    return font.width(value) <= width
        ? value
        : font.plainSubstrByWidth(value, Math.max(0, width - font.width("..."))) + "...";
  }

  public static void title(
      GuiGraphics graphics, Font font, Component title, int x, int y, int width) {
    graphics.drawString(font, fit(font, title, width), x, y, TEXT, false);
  }
}

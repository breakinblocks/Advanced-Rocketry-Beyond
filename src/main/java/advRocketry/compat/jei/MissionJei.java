package advRocketry.compat.jei;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class MissionJei {
    static final int LINE = 10;

    private MissionJei() {}

    static String time(long ticks) {
        long seconds = Math.max(0, ticks / 20);
        long hours = seconds / 3600;
        long minutes = seconds / 60 % 60;
        long rest = seconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, rest)
                : String.format("%d:%02d", minutes, rest);
    }

    static int text(GuiGraphics graphics, Font font, Component text, int x, int y, int width, int color) {
        for (var line : font.split(text, width)) {
            graphics.drawString(font, line, x, y, color, false);
            y += LINE;
        }
        return y;
    }
}

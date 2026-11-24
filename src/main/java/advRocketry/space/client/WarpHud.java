// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space.client;

import advRocketry.Main;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.Station;
import advRocketry.space.GalaxyData;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class WarpHud {
    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath(Main.MODID, "warp_hud");
    private static final int WIDTH = 182;

    private WarpHud() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, LAYER, WarpHud::render);
    }

    public static final int BOTTOM = 28;

    public static boolean active() {
        return transitStation() != null;
    }

    private static Station transitStation() {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.level == null || client.options.hideGui) return null;
        Planet here = GalaxySync.clientPlanet(client.level.dimension().location());
        if (here == null || here.id != GalaxyData.SPACE_ID) return null;
        Station station = GalaxySync.clientStationAt(player.blockPosition());
        return station == null || !station.inWarp() ? null : station;
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        var client = Minecraft.getInstance();
        Station station = transitStation();
        if (station == null) return;
        double time = client.level.getGameTime() + (double) delta.getGameTimeDeltaPartialTick(false);
        double progress = station.warpProgress(time);
        Planet target = GalaxySync.clientPlanet(station.warpDestination);
        long seconds = Math.max(0, (long) Math.ceil((station.warpEta - time) / 20));
        Component label = Component.translatable(
                "hud.adv_rocketry.warp_transit",
                target == null ? "?" : target.name,
                (int) (progress * 100),
                String.format("%d:%02d", seconds / 60, seconds % 60));
        int x = (graphics.guiWidth() - WIDTH) / 2;
        int y = 6;
        graphics.drawCenteredString(client.font, label, graphics.guiWidth() / 2, y, UiTheme.ACCENT);
        graphics.fill(x - 1, y + 11, x + WIDTH + 1, y + 18, UiTheme.BORDER);
        graphics.fill(x, y + 12, x + WIDTH, y + 17, UiTheme.INSET);
        graphics.fill(x, y + 12, x + (int) (WIDTH * progress), y + 17, UiTheme.ACCENT);
    }
}

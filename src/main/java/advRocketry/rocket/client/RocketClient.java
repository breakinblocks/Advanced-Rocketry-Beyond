// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.Main;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.RocketControl;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.SpaceFlightControl;
import advRocketry.rocket.SpaceNavigation;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import advRocketry.util.Texts;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class RocketClient {
  private static final ResourceLocation HUD_LAYER =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "rocket_hud");
  private static boolean jumping;
  private static final KeyMapping RCS =
      new KeyMapping(
          "key.adv_rocketry.rcs",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_R,
          "key.categories.adv_rocketry");
  private static final KeyMapping DOWN =
      new KeyMapping(
          "key.adv_rocketry.space_down",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_Q,
          "key.categories.adv_rocketry");
  private static final Map<RocketEntity, RocketEngineSound> ENGINE_SOUNDS = new IdentityHashMap<>();

  private RocketClient() {}

  public static void keys(RegisterKeyMappingsEvent event) {
    event.register(RCS);
    event.register(DOWN);
  }

  public static void layers(RegisterGuiLayersEvent event) {
    event.registerAbove(VanillaGuiLayers.HOTBAR, HUD_LAYER, RocketClient::hud);
  }

  public static int hudHeight() {
    List<Component> lines = hudLines(Minecraft.getInstance());
    return lines == null ? 0 : lines.size() * 12 + 10;
  }

  private static List<Component> hudLines(Minecraft client) {
    if (client.player == null || !(client.player.getVehicle() instanceof RocketEntity rocket))
      return null;
    if (client.options.hideGui || client.player.isSpectator()) return null;
    SpaceNavigation navigation = rocket.navigation();
    Component jump = client.options.keyJump.getTranslatedKeyMessage();
    if (navigation == null) {
      if (!rocket.asteroidRcs()) return null;
      return List.of(
          Component.translatable("gui.adv_rocketry.rocket_hud.asteroid_rcs"),
          Texts.translate(
              "gui.adv_rocketry.rocket_hud.asteroid_controls",
              RCS.getTranslatedKeyMessage(),
              jump,
              DOWN.getTranslatedKeyMessage()));
    }
    Planet focus = GalaxySync.clientPlanet(navigation.focus);
    Component region =
        focus == null
            ? Component.translatable("gui.adv_rocketry.rocket_hud.solar_navigation")
            : Texts.translate("gui.adv_rocketry.rocket_hud.orbit", focus.name);
    return List.of(
        Texts.translate(
            "gui.adv_rocketry.rocket_hud.region",
            region,
            Component.translatable(
                navigation.rcs
                    ? "gui.adv_rocketry.rocket_hud.on"
                    : "gui.adv_rocketry.rocket_hud.off")),
        Texts.translate(
            "gui.adv_rocketry.rocket_hud.speed",
            String.format(Locale.ROOT, "%.1f", navigation.velocity.length()),
            String.format(Locale.ROOT, "%.0f", navigation.position.x),
            String.format(Locale.ROOT, "%.0f", navigation.position.y),
            String.format(Locale.ROOT, "%.0f", navigation.position.z)),
        Texts.translate(
            "gui.adv_rocketry.rocket_hud.controls",
            jump,
            DOWN.getTranslatedKeyMessage(),
            RCS.getTranslatedKeyMessage()));
  }

  private static void hud(GuiGraphics graphics, DeltaTracker delta) {
    Minecraft client = Minecraft.getInstance();
    List<Component> lines = hudLines(client);
    if (lines == null) return;
    int width = 0;
    for (Component line : lines) width = Math.max(width, client.font.width(line));
    width = Math.min(width, graphics.guiWidth() - 28);
    UiTheme.hudPanel(graphics, 6, 6, width + 14, lines.size() * 12 + 6, UiTheme.ACCENT);
    for (int line = 0; line < lines.size(); line++)
      graphics.drawString(
          client.font,
          UiTheme.fit(client.font, lines.get(line), width),
          12,
          10 + line * 12,
          line == 0 ? UiTheme.TEXT : line == lines.size() - 1 ? UiTheme.MUTED : UiTheme.ACCENT,
          false);
  }

  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(RocketRegistry.ROCKET.get(), RocketRenderer::new);
    event.registerEntityRenderer(RocketRegistry.SEAT_MOUNT.get(), SeatRenderer::new);
  }

  public static void screens(RegisterMenuScreensEvent event) {
    event.register(RocketRegistry.MENU.get(), RocketScreen::new);
    event.register(RocketRegistry.CONSOLE_MENU.get(), RocketConsoleScreen::new);
    event.register(RocketRegistry.CARGO_PORT_MENU.get(), RocketCargoPortScreen::new);
    event.register(RocketRegistry.FUELING_MENU.get(), FuelingStationScreen::new);
    event.register(RocketRegistry.MONITORING_MENU.get(), RocketMonitoringScreen::new);
  }

  public static void controls(MovementInputUpdateEvent event) {
    boolean jump = event.getInput().jumping;
    boolean toggle = RCS.consumeClick();
    if (event.getEntity().getVehicle() instanceof RocketEntity rocket
        && (rocket.flight() == 3 || rocket.asteroidRcs() || rocket.flight() == 0 && toggle)) {
      boolean active = Minecraft.getInstance().screen == null;
      PacketDistributor.sendToServer(
          new SpaceFlightControl(
              active ? (int) Math.signum(event.getInput().forwardImpulse) : 0,
              active ? -(int) Math.signum(event.getInput().leftImpulse) : 0,
              active ? (jump ? 1 : 0) - (DOWN.isDown() ? 1 : 0) : 0,
              active && toggle));
      jumping = jump;
      return;
    }
    while (RCS.consumeClick()) {}
    if (jump && !jumping && event.getEntity().getVehicle() instanceof RocketEntity)
      PacketDistributor.sendToServer(new RocketControl());
    jumping = jump;
  }

  public static void tick(ClientTickEvent.Post event) {
    ENGINE_SOUNDS.entrySet().removeIf(entry -> entry.getValue().isStopped());
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) return;
    for (var entity : minecraft.level.entitiesForRendering()) {
      if (!(entity instanceof RocketEntity rocket)
          || !RocketEngineSound.active(rocket)
          || ENGINE_SOUNDS.containsKey(rocket)) continue;
      RocketEngineSound sound = new RocketEngineSound(rocket);
      ENGINE_SOUNDS.put(rocket, sound);
      minecraft.getSoundManager().play(sound);
    }
  }
}

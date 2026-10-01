// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.client.ui.UiTheme;
import advRocketry.life.AtmosphereAnalyzerItem;
import advRocketry.life.AtmosphereDetectorBlockEntity;
import advRocketry.life.AtmosphereSync;
import advRocketry.life.SpaceBreathing;
import advRocketry.life.SpaceSuitItem;
import advRocketry.orbit.BeaconFinderItem;
import advRocketry.rocket.client.RocketClient;
import advRocketry.space.GalaxySync;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original suit oxygen gauge, installed component strip and helmet atmosphere analyzer. */
public final class SuitHud {
  private static final ResourceLocation LAYER =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "suit_hud");
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private static final int MARGIN = 6;
  private static final int ROW = 18;

  private SuitHud() {}

  public static boolean analyzer(ItemStack helmet) {
    return !SpaceSuitItem.findModule(
            helmet, module -> module.getItem() instanceof AtmosphereAnalyzerItem)
        .isEmpty();
  }

  public static void register(RegisterGuiLayersEvent event) {
    event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER, SuitHud::render);
  }

  private static void render(GuiGraphics graphics, DeltaTracker delta) {
    var client = Minecraft.getInstance();
    var player = client.player;
    if (player == null || client.level == null || client.options.hideGui || player.isSpectator())
      return;
    ResourceLocation dimension = client.level.dimension().location();
    ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
    int top = MARGIN + RocketClient.hudHeight();
    warning(graphics, client.font, dimension, player.tickCount);
    if (helmet.getItem() instanceof SpaceSuitItem suit
        && suit.countModule(helmet, "beacon_finder") > 0) beacons(graphics, player, dimension);
    if (!player.isCreative()) {
      oxygen(graphics, client.font, player);
      if (helmet.getItem() instanceof SpaceSuitItem) modules(graphics, client.font, player, top);
    }
    if (analyzer(helmet)) atmosphere(graphics, client.font, player, dimension, top);
  }

  private static void warning(
      GuiGraphics graphics, Font font, ResourceLocation dimension, int tick) {
    var warning = AtmosphereSync.clientWarning(dimension, tick);
    if (warning == null) return;
    var message =
        Component.translatable(
            "hud.adv_rocketry.atmosphere_warning",
            Component.translatable(
                "atmosphere.adv_rocketry." + warning.name().toLowerCase(Locale.ROOT)));
    int width = font.width(message) + 18;
    int x = (graphics.guiWidth() - width) / 2;
    int y = graphics.guiHeight() / 4 - 8;
    UiTheme.hudPanel(graphics, x, y, width, 17, UiTheme.NEGATIVE);
    if (UiTheme.pulse(tick)) UiTheme.outline(graphics, x, y, width, 17, UiTheme.NEGATIVE);
    graphics.drawString(font, message, x + 11, y + 5, UiTheme.TEXT, false);
  }

  private static void beacons(
      GuiGraphics graphics, LocalPlayer player, ResourceLocation dimension) {
    for (var beacon : BeaconFinderItem.clientBeacons(dimension)) {
      double angle =
          BeaconFinderItem.angle(player.getX(), player.getZ(), player.getYHeadRot(), beacon);
      int x = (int) (graphics.guiWidth() * angle / 180 + graphics.guiWidth() / 2d);
      if (x >= 4 && x <= graphics.guiWidth() - 4) UiTheme.marker(graphics, x, 2, UiTheme.ACCENT);
    }
  }

  private static void oxygen(GuiGraphics graphics, Font font, LocalPlayer player) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    int air = 0, capacity = 0;
    if (chest.getItem() instanceof SpaceSuitItem suit) {
      air = suit.oxygen(chest);
      capacity = suit.oxygenCapacity(chest);
    } else if (SpaceBreathing.sealed(player.level(), chest)) {
      air = SpaceBreathing.air(chest);
      capacity = AdvancedRocketryConfig.sealedArmorAirCapacity();
    }
    if (capacity <= 0) return;
    double fraction = (double) air / capacity;
    int color = UiTheme.level(fraction);
    int width = Math.clamp(graphics.guiWidth() / 2 - 91 - MARGIN * 2, 84, 150);
    int height = 24;
    int x = MARGIN;
    int y = graphics.guiHeight() - MARGIN - height;
    boolean critical = color == UiTheme.NEGATIVE;
    UiTheme.hudPanel(
        graphics,
        x,
        y,
        width,
        height,
        critical && !UiTheme.pulse(player.tickCount) ? UiTheme.ACCENT_DIM : color);
    Component label = Component.translatable("hud.adv_rocketry.oxygen");
    Component amount = Component.translatable("hud.adv_rocketry.oxygen_amount", air, capacity);
    if (font.width(label) + font.width(amount) + 20 > width)
      amount = Component.literal(Math.round(fraction * 100) + "%");
    graphics.drawString(
        font, amount, x + width - 6 - font.width(amount), y + 5, UiTheme.TEXT, false);
    graphics.drawString(font, label, x + 8, y + 5, UiTheme.MUTED, false);
    UiTheme.meter(graphics, x + 8, y + 16, width - 14, 3, fraction, color);
  }

  private static void modules(GuiGraphics graphics, Font font, LocalPlayer player, int top) {
    List<ItemStack> pieces = new ArrayList<>();
    List<List<ItemStack>> installed = new ArrayList<>();
    int columns = 0;
    for (EquipmentSlot equipment : ARMOR) {
      ItemStack armor = player.getItemBySlot(equipment);
      if (!(armor.getItem() instanceof SpaceSuitItem suit)) continue;
      ItemStackHandler modules = suit.inventory(armor);
      List<ItemStack> stacks = new ArrayList<>();
      for (int slot = 0; slot < modules.getSlots(); slot++)
        if (!modules.getStackInSlot(slot).isEmpty()) stacks.add(modules.getStackInSlot(slot));
      pieces.add(armor);
      installed.add(stacks);
      columns = Math.max(columns, stacks.size());
    }
    if (pieces.isEmpty()) return;
    Component title = Component.translatable("hud.adv_rocketry.suit");
    int width = Math.max(32 + ROW * columns, font.width(title) + 16);
    int height = 16 + pieces.size() * ROW;
    UiTheme.hudPanel(graphics, MARGIN, top, width, height, UiTheme.ACCENT);
    graphics.drawString(font, title, MARGIN + 8, top + 4, UiTheme.MUTED, false);
    int y = top + 14;
    for (int piece = 0; piece < pieces.size(); piece++) {
      graphics.renderItem(pieces.get(piece), MARGIN + 7, y);
      if (!installed.get(piece).isEmpty())
        graphics.fill(MARGIN + 25, y + 1, MARGIN + 26, y + 15, UiTheme.BORDER);
      for (int column = 0; column < installed.get(piece).size(); column++)
        graphics.renderItem(installed.get(piece).get(column), MARGIN + 29 + column * ROW, y);
      y += ROW;
    }
  }

  private static void atmosphere(
      GuiGraphics graphics, Font font, LocalPlayer player, ResourceLocation dimension, int top) {
    var planet = GalaxySync.clientPlanet(dimension);
    int pressure =
        AtmosphereSync.clientPressure(dimension, planet == null ? 100 : planet.atmosphere);
    var type = AtmosphereSync.clientAtmosphere(dimension, planet);
    boolean breathable =
        type == AtmosphereDetectorBlockEntity.Target.AIR
            || type == AtmosphereDetectorBlockEntity.Target.PRESSURIZED_AIR;
    int state = breathable ? UiTheme.POSITIVE : UiTheme.NEGATIVE;
    Component title = Component.translatable("hud.adv_rocketry.atmosphere");
    Component name =
        Component.translatable("atmosphere.adv_rocketry." + type.name().toLowerCase(Locale.ROOT));
    Component detail =
        Component.translatable(
            "hud.adv_rocketry.pressure", String.format(Locale.ROOT, "%.2f", pressure / 100f));
    Component verdict =
        Component.translatable(
            breathable ? "hud.adv_rocketry.breathable" : "hud.adv_rocketry.unbreathable");
    int width =
        Math.max(
                Math.max(font.width(title) + 14, font.width(name)),
                font.width(detail) + 8 + font.width(verdict))
            + 16;
    int x = graphics.guiWidth() - MARGIN - width;
    UiTheme.hudPanel(graphics, x, top, width, 40, state);
    graphics.drawString(font, title, x + 8, top + 4, UiTheme.MUTED, false);
    if (UiTheme.pulse(player.tickCount))
      graphics.fill(x + width - 9, top + 6, x + width - 6, top + 9, UiTheme.ACCENT);
    graphics.drawString(font, name, x + 8, top + 16, state, false);
    graphics.drawString(font, detail, x + 8, top + 28, UiTheme.TEXT, false);
    graphics.drawString(font, verdict, x + width - 6 - font.width(verdict), top + 28, state, false);
  }
}

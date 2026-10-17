// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.transport.TransportMenu;
import advRocketry.transport.TransportRegistry;
import advRocketry.transport.TransportSideConfig;
import advRocketry.transport.TransportTagEdit;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TransportScreen extends ModContainerScreen<TransportMenu> {
  private static final String[] MODES = {
    "gui.adv_rocketry.transport.mode_insert",
    "gui.adv_rocketry.transport.mode_extract",
    "gui.adv_rocketry.transport.mode_disabled"
  };
  private static final String[] REDSTONE = {
    "gui.adv_rocketry.transport.redstone_ignore",
    "gui.adv_rocketry.transport.redstone_signal",
    "gui.adv_rocketry.transport.redstone_no_signal"
  };
  private final List<UiButton> sides = new ArrayList<>();
  private UiButton routing, redstone;
  private final List<UiButton> policies = new ArrayList<>();

  public TransportScreen(TransportMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 420;
    imageHeight = 240;
    inventoryLabelX = 129;
    inventoryLabelY = 147;
  }

  private UiButton button(int id, int x, int y, int width, Component text) {
    return (UiButton)
        addRenderableWidget(
            UiButton.builder(
                    text, b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .bounds(leftPos + x, topPos + y, width, 18)
                .build());
  }

  @Override
  protected void init() {
    super.init();
    if (menu.kind == TransportRegistry.Kind.ITEM)
      button(40, 330, 8, 82, Component.literal("Storage"));
    sides.clear();
    for (int i = 0; i < 6; i++) {
      var b = button(i, 8 + i % 3 * 56, 42 + i / 3 * 19, 54, Component.empty());
      b.setTooltip(
          Tooltip.create(
              Component.translatable(
                  "gui.adv_rocketry.transport.machine_side_insert_extract_disabled_disabling")));
      sides.add(b);
    }
    redstone = button(7, 258, 118, 76, Component.empty());
    redstone.setTooltip(
        Tooltip.create(
            Component.translatable(
                "gui.adv_rocketry.transport.controls_extraction_at_this_pipe_only")));
    if (menu.kind == TransportRegistry.Kind.ITEM) {
      routing = button(6, 336, 118, 76, Component.empty());
      routing.setTooltip(
          Tooltip.create(
              Component.translatable(
                  "gui.adv_rocketry.transport.network_wide_round_robin_gives_each")));
    }
    for (int i = 0; i < 6; i++)
      button(
          10 + i,
          8 + i * 28,
          82,
          26,
          Component.literal(
              Direction.values()[i].getName().substring(0, 1).toUpperCase(Locale.ROOT)));
    policies.clear();
    for (int i = 0; i < 13; i++)
      policies.add(button(20 + i, 180 + (i % 3) * 78, 42 + (i / 3) * 19, 76, Component.empty()));

    var tag =
        addRenderableWidget(
            new EditBox(
                font,
                leftPos + 8,
                topPos + 126,
                124,
                18,
                Component.literal("Tag: slot=namespace:path")));
    tag.setMaxLength(260);
    addRenderableWidget(
        UiButton.builder(
                Component.literal("Tag"),
                b -> {
                  String[] parts = tag.getValue().split("=", 2);
                  if (parts.length != 2) return;
                  try {
                    PacketDistributor.sendToServer(
                        new TransportTagEdit(
                            menu.containerId,
                            menu.value(13),
                            Integer.parseInt(parts[0]) - 1,
                            parts[1]));
                  } catch (NumberFormatException ignored) {
                  }
                })
            .bounds(leftPos + 136, topPos + 126, 34, 18)
            .build());
    tag.setTooltip(
        Tooltip.create(
            Component.literal("Set a filter tag, e.g. 1=c:ingots/iron. Slots are numbered 1-9.")));
    if (menu.kind == TransportRegistry.Kind.FLUID)
      button(8, 336, 118, 76, Component.translatable("gui.adv_rocketry.transport.clear_fluid_lock"))
          .setTooltip(
              Tooltip.create(
                  Component.translatable(
                      "gui.adv_rocketry.transport.sneak_use_a_filled_bucket_to")));
  }

  @Override
  public void containerTick() {
    super.containerTick();
    String[] labels = {
      "Priority " + menu.value(14) + " -",
      "Priority +",
      TransportSideConfig.Routing.values()[Math.clamp(menu.value(15), 0, 4)].name(),
      "Pull: " + new String[] {"Always", "Signal", "No signal"}[Math.clamp(menu.value(16), 0, 2)],
      "Push: " + new String[] {"Always", "Signal", "No signal"}[Math.clamp(menu.value(17), 0, 2)],
      "Cap " + (menu.value(18) == Integer.MAX_VALUE ? "max" : menu.value(18)),
      "Retry " + menu.value(19),
      "Channel " + menu.value(20),
      menu.value(21) == 0 ? "Filter off" : "Filter on",
      menu.value(22) == 0 ? "Allowlist" : "Denylist",
      menu.value(23) == 1 ? "Exact" : "Identity",
      menu.value(24) == 1 ? "Sensor on" : "Sensor off",
      menu.value(25) == 1 ? "VOID ON" : "Void off"
    };
    for (int i = 0; i < policies.size(); i++)
      policies.get(i).setMessage(Component.literal(labels[i]));
    policies
        .get(12)
        .setTooltip(
            Tooltip.create(
                Component.literal(
                    "Deletes matching overflow after real receivers. Enable deliberately.")));
    for (int i = 0; i < sides.size(); i++)
      sides
          .get(i)
          .setMessage(
              Texts.translate(
                  "gui.adv_rocketry.transport.side_mode",
                  Direction.values()[i].getName(),
                  Component.translatable(MODES[Math.clamp(menu.value(i), 0, 2)])));
    redstone.setMessage(Component.translatable(REDSTONE[Math.clamp(menu.value(7), 0, 2)]));
    if (routing != null)
      routing.setMessage(
          menu.value(6) == 1
              ? Component.translatable("gui.adv_rocketry.transport.routing_round_robin")
              : Component.translatable("gui.adv_rocketry.transport.routing_fill_first"));
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    drawText(
        graphics,
        Component.literal(
            "Editing " + Direction.values()[Math.clamp(menu.value(13), 0, 5)].getName()),
        8,
        147,
        UiTheme.MUTED,
        false);
    String rate =
        switch (menu.kind) {
          case ENERGY -> "5,000 FE/t per connection";
          case FLUID -> "4,000 mB/t";
          case ITEM -> "16 items / second";
        };
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.transport.sections", rate, menu.value(9)),
        8,
        28,
        UiTheme.MUTED,
        false);
    drawText(
        graphics,
        Component.literal(
            menu.value(10) > 0 ? "Network blocked" : menu.value(12) == 1 ? "Transferring" : "Idle"),
        278,
        28,
        UiTheme.TEXT,
        false);
    drawText(graphics, Component.literal("Shared filter"), 322, 143, UiTheme.MUTED, false);
  }
}

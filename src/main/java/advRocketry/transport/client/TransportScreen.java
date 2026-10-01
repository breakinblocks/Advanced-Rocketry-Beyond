// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.transport.TransportMenu;
import advRocketry.transport.TransportRegistry;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

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

  public TransportScreen(TransportMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 300;
    imageHeight = 232;
    inventoryLabelX = 69;
    inventoryLabelY = 139;
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
    sides.clear();
    for (int i = 0; i < 6; i++) {
      var b = button(i, 8 + i % 3 * 96, 42 + i / 3 * 21, 92, Component.empty());
      b.setTooltip(
          Tooltip.create(
              Component.translatable(
                  "gui.adv_rocketry.transport.machine_side_insert_extract_disabled_disabling")));
      sides.add(b);
    }
    redstone = button(7, 8, 88, 138, Component.empty());
    redstone.setTooltip(
        Tooltip.create(
            Component.translatable(
                "gui.adv_rocketry.transport.controls_extraction_at_this_pipe_only")));
    if (menu.kind == TransportRegistry.Kind.ITEM) {
      routing = button(6, 152, 88, 140, Component.empty());
      routing.setTooltip(
          Tooltip.create(
              Component.translatable(
                  "gui.adv_rocketry.transport.network_wide_round_robin_gives_each")));
    }
    if (menu.kind == TransportRegistry.Kind.FLUID)
      button(8, 152, 88, 140, Component.translatable("gui.adv_rocketry.transport.clear_fluid_lock"))
          .setTooltip(
              Tooltip.create(
                  Component.translatable(
                      "gui.adv_rocketry.transport.sneak_use_a_filled_bucket_to")));
  }

  @Override
  public void containerTick() {
    super.containerTick();
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
    Component status =
        menu.value(10) == 2
            ? Component.translatable("status.adv_rocketry.transport.too_large_split_at_4_096")
            : menu.value(10) == 1
                ? Component.translatable(
                    "status.adv_rocketry.transport.conflicting_fluids_clear_buffers_or_locks")
                : menu.value(12) == 1
                    ? Component.translatable("status.adv_rocketry.transport.transferring")
                    : Component.translatable("status.adv_rocketry.transport.idle");
    drawText(graphics, status, 8, 110, menu.value(10) > 0 ? UiTheme.WARNING : UiTheme.TEXT, false);
    if (menu.kind == TransportRegistry.Kind.ITEM)
      drawText(
          graphics,
          Component.translatable(
              "gui.adv_rocketry.transport.endpoint_filter_click_with_carried_item"),
          8,
          123,
          UiTheme.MUTED,
          false);
    else if (menu.kind == TransportRegistry.Kind.FLUID) {
      var fluid = BuiltInRegistries.FLUID.byId(menu.value(8));
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.transport.lock_buffer_mb",
              (fluid == Fluids.EMPTY
                  ? Component.translatable("gui.adv_rocketry.transport.any_fluid")
                  : new FluidStack(fluid, 1000).getHoverName()),
              menu.value(11)),
          8,
          123,
          UiTheme.MUTED,
          false);
    } else
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.transport.endpoint_buffer_fe", menu.value(11)),
          8,
          123,
          UiTheme.MUTED,
          false);
  }
}

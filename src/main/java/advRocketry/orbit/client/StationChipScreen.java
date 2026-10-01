// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiEditBox;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.StationChipAction;
import advRocketry.orbit.StationChipMenu;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Scrollable per-planet destinations, including the protected Last departure. */
public final class StationChipScreen extends ModContainerScreen<StationChipMenu> {
  private int offset;
  private EditBox name;
  private final Button[] rows = new Button[6];

  public StationChipScreen(StationChipMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 300;
    imageHeight = 230;
    offset = Math.max(0, menu.selected - 5);
  }

  @Override
  protected void init() {
    super.init();
    for (int i = 0; i < rows.length; i++) {
      final int row = i;
      rows[i] =
          addRenderableWidget(
              UiButton.builder(Component.empty(), button -> send(3, offset + row))
                  .bounds(leftPos + 12, topPos + 43 + i * 22, 276, 20)
                  .build());
    }
    name =
        addRenderableWidget(
            new UiEditBox(
                font,
                leftPos + 12,
                topPos + 179,
                192,
                18,
                Component.translatable("gui.adv_rocketry.station_chip.destination_name")));
    name.setMaxLength(32);
    name.setHint(Component.translatable("gui.adv_rocketry.station_chip.destination_name"));
    var add =
        addRenderableWidget(
            UiButton.builder(
                    Component.translatable("gui.adv_rocketry.station_chip.add_here"),
                    button -> send(2, 0))
                .bounds(leftPos + 210, topPos + 178, 78, 20)
                .build());
    add.active = menu.canAdd && menu.locations.size() < 128;
    var delete =
        addRenderableWidget(
            UiButton.builder(
                    Component.translatable("gui.adv_rocketry.station_chip.delete"),
                    button -> send(1, 0))
                .bounds(leftPos + 12, topPos + 204, 82, 20)
                .build());
    delete.active = menu.selected > 0;
    var clear =
        addRenderableWidget(
            UiButton.builder(
                    Component.translatable("gui.adv_rocketry.station_chip.clear_saved"),
                    button -> send(0, 0))
                .bounds(leftPos + 100, topPos + 204, 100, 20)
                .build());
    clear.active = menu.locations.size() > 1;
    addRenderableWidget(
        UiButton.builder(
                Component.translatable("gui.adv_rocketry.station_chip.done"), button -> onClose())
            .bounds(leftPos + 206, topPos + 204, 82, 20)
            .build());
    refreshRows();
  }

  private void send(int action, int index) {
    PacketDistributor.sendToServer(
        new StationChipAction(menu.containerId, action, index, name.getValue()));
  }

  private void refreshRows() {
    for (int i = 0; i < rows.length; i++) {
      int index = offset + i;
      rows[i].visible = index < menu.locations.size();
      if (rows[i].visible) {
        var location = menu.locations.get(index);
        Component name =
            index == 0
                ? Component.translatable("gui.adv_rocketry.station_chip.last")
                : Component.literal(location.name());
        rows[i].setMessage(
            Component.literal(index == menu.selected ? "> " : "")
                .append(name)
                .append(": " + location.pos().getX() + ", " + location.pos().getZ()));
        ((UiButton) rows[i]).selected(index == menu.selected);
      }
    }
  }

  @Override
  public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    if (x >= leftPos + 12 && x < leftPos + 288 && y >= topPos + 43 && y < topPos + 175) {
      offset =
          Math.max(
              0,
              Math.min(
                  Math.max(0, menu.locations.size() - rows.length),
                  offset - (int) Math.signum(vertical)));
      refreshRows();
      return true;
    }
    return super.mouseScrolled(x, y, horizontal, vertical);
  }

  @Override
  public boolean keyPressed(int key, int scanCode, int modifiers) {
    if (name.isFocused() && key != 256)
      return name.keyPressed(key, scanCode, modifiers) || name.canConsumeInput();
    return super.keyPressed(key, scanCode, modifiers);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    drawText(
        graphics,
        Texts.translate(
            "gui.adv_rocketry.station_chip.planet",
            menu.planet,
            (menu.canAdd ? ": saved return locations" : ": select a ground destination")),
        12,
        27,
        UiTheme.MUTED,
        false);
    if (menu.locations.isEmpty())
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.station_chip.no_saved_destinations"),
          18,
          52,
          UiTheme.MUTED,
          false);
  }
}

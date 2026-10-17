package advRocketry.transport.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.transport.TransportTagEdit;
import advRocketry.transport.TransportTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TransportTerminalScreen extends ModContainerScreen<TransportTerminalMenu> {
  public TransportTerminalScreen(TransportTerminalMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 300;
    imageHeight = 236;
    inventoryLabelX = 69;
    inventoryLabelY = 143;
  }

  @Override
  protected void init() {
    super.init();
    var search =
        addRenderableWidget(
            new EditBox(font, leftPos + 8, topPos + 20, 164, 18, Component.literal("Search")));
    search.setMaxLength(128);
    search.setResponder(
        value ->
            PacketDistributor.sendToServer(new TransportTagEdit(menu.containerId, 0, 0, value)));
    search.setTooltip(
        Tooltip.create(
            Component.literal(
                "Search by item name or @mod. Listings refresh while open; click to withdraw,"
                    + " right-click for one.")));
    String[] labels = {"Previous", "Next", "Deposit held", "Refill grid"};
    for (int i = 0; i < labels.length; i++) {
      int id = i;
      addRenderableWidget(
          UiButton.builder(
                  Component.literal(labels[i]),
                  b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
              .bounds(leftPos + 8 + i * 72, topPos + 120, 70, 18)
              .build());
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int x, int y) {
    super.renderLabels(graphics, x, y);
    drawText(
        graphics,
        Component.literal(
            "Page "
                + (menu.value(0) + 1)
                + "/"
                + menu.value(1)
                + (menu.value(2) == 1
                    ? " - scanning"
                    : menu.value(2) == 2 ? " - limited listing" : "")),
        8,
        112,
        UiTheme.MUTED,
        false);
    drawText(graphics, Component.literal("Crafting"), 190, 28, UiTheme.TEXT, false);
  }
}

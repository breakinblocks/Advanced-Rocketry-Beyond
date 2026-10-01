// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.processing.MachineMenu;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends ModContainerScreen<MachineMenu> {
  public static final int PROGRESS_X = 12;
  public static final int PROGRESS_Y = 122;
  public static final int PROGRESS_WIDTH = 174;
  public static final int PROGRESS_HEIGHT = 5;
  private Button previous;
  private Button next;

  public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 300;
    imageHeight = 232;
    inventoryLabelY = 138;
    inventoryLabelX = 69;
  }

  @Override
  protected void init() {
    super.init();
    previous =
        addRenderableWidget(
            UiButton.builder(Component.literal("<"), button -> turnPage(-1))
                .bounds(leftPos + 204, topPos + 114, 24, 20)
                .build());
    next =
        addRenderableWidget(
            UiButton.builder(Component.literal(">"), button -> turnPage(1))
                .bounds(leftPos + 268, topPos + 114, 24, 20)
                .build());
    updateButtons();
  }

  private void turnPage(int delta) {
    menu.page(menu.page() + delta);
    updateButtons();
  }

  private void updateButtons() {
    previous.visible = next.visible = menu.pageCount() > 1;
    previous.active = menu.page() > 0;
    next.active = menu.page() + 1 < menu.pageCount();
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
    UiTheme.progress(
        graphics,
        leftPos + PROGRESS_X,
        topPos + PROGRESS_Y,
        PROGRESS_WIDTH,
        PROGRESS_HEIGHT,
        menu.progress());
  }

  private Component resource(int index) {
    var port = menu.ports().get(index);
    if (port.hasEnergy())
      return Texts.translate("gui.adv_rocketry.machine.energy", menu.stored(index));
    if (!port.hasFluid()) return Component.empty();
    var fluid = BuiltInRegistries.FLUID.byId(menu.fluid(index));
    return Texts.translate(
        "gui.adv_rocketry.machine.fluid",
        menu.stored(index) == 0 || fluid == null
            ? Component.translatable("gui.adv_rocketry.fluid.empty")
            : fluid.getFluidType().getDescription(),
        menu.stored(index));
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    for (int i = menu.page() * 4; i < Math.min(menu.ports().size(), menu.page() * 4 + 4); i++) {
      var port = menu.ports().get(i);
      int x = 12 + i % 2 * 146, y = 26 + i % 4 / 2 * 44;
      String label =
          switch (port.kind()) {
            case ITEM_INPUT -> "gui.adv_rocketry.machine.port.item_input";
            case ITEM_OUTPUT -> "gui.adv_rocketry.machine.port.item_output";
            case FLUID_INPUT -> "gui.adv_rocketry.machine.port.fluid_input";
            case FLUID_OUTPUT -> "gui.adv_rocketry.machine.port.fluid_output";
            case DATA_BUS -> "gui.adv_rocketry.machine.port.data_bus";
            case ENERGY_INPUT -> "gui.adv_rocketry.machine.port.energy_input";
            case CREATIVE_ENERGY_INPUT -> "gui.adv_rocketry.machine.port.creative_energy_input";
            case ENERGY_OUTPUT -> "gui.adv_rocketry.machine.port.energy_output";
            case SOLAR -> "gui.adv_rocketry.machine.port.solar";
          };
      drawText(graphics, Component.translatable(label), x, y, UiTheme.TEXT, false);
      if (port.hasFluid())
        drawText(graphics, Component.literal(">"), x + 24, y + 16, UiTheme.MUTED, false);
      drawText(graphics, UiTheme.fit(font, resource(i), 130), x, y + 32, UiTheme.MUTED, false);
    }
    if (menu.pageCount() > 1)
      graphics.drawCenteredString(
          font, (menu.page() + 1) + "/" + menu.pageCount(), 248, 120, UiTheme.MUTED);
    drawText(
        graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, UiTheme.MUTED, false);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    for (int i = menu.page() * 4; i < Math.min(menu.ports().size(), menu.page() * 4 + 4); i++) {
      int x = leftPos + 12 + i % 2 * 146, y = topPos + 58 + i % 4 / 2 * 44;
      Component text = resource(i);
      if (font.width(text) > 130
          && mouseX >= x
          && mouseX < x + 130
          && mouseY >= y
          && mouseY < y + 10) graphics.renderTooltip(font, text, mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY != 0
        && mouseX >= leftPos
        && mouseX < leftPos + imageWidth
        && mouseY >= topPos + 20
        && mouseY < topPos + 134) {
      turnPage(scrollY > 0 ? -1 : 1);
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }
}

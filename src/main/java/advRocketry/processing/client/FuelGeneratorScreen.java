package advRocketry.processing.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiTheme;
import advRocketry.processing.FuelGeneratorBlockEntity;
import advRocketry.processing.FuelGeneratorMenu;
import advRocketry.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FuelGeneratorScreen extends ModContainerScreen<FuelGeneratorMenu> {
  public FuelGeneratorScreen(FuelGeneratorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 176;
    imageHeight = 194;
    inventoryLabelY = 100;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);
    UiTheme.progress(
        graphics,
        leftPos + 8,
        topPos + 44,
        160,
        5,
        menu.value(3) / (double) FuelGeneratorBlockEntity.TANK_CAPACITY);
    UiTheme.progress(
        graphics,
        leftPos + 8,
        topPos + 69,
        160,
        5,
        menu.value(0) / (double) FuelGeneratorBlockEntity.CAPACITY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    var fluid = new FluidStack(BuiltInRegistries.FLUID.byId(menu.value(4)), menu.value(3));
    Component name =
        fluid.isEmpty()
            ? Component.translatable("gui.adv_rocketry.fuel_generator.empty")
            : fluid.getHoverName();
    drawText(graphics, UiTheme.fit(font, name, 160), 8, 20, UiTheme.TEXT, false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.fuel_generator.tank", menu.value(3)),
        8,
        32,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.fuel_generator.energy", menu.value(0)),
        8,
        56,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.fuel_generator.burn", menu.value(2), menu.value(1)),
        8,
        82,
        UiTheme.TEXT,
        false);
    drawText(graphics, playerInventoryTitle, 8, inventoryLabelY, UiTheme.TEXT, false);
  }
}

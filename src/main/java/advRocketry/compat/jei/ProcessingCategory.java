// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.compat.jei;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.ui.UiTheme;
import advRocketry.compat.ProcessingDisplay;
import advRocketry.processing.MachineType;
import advRocketry.util.Texts;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Original machine category ingredients, fluids, processing costs and output chances. */
public final class ProcessingCategory implements IRecipeCategory<ProcessingDisplay> {
  private static final IDrawable SLOT =
      new IDrawable() {
        @Override
        public int getWidth() {
          return 18;
        }

        @Override
        public int getHeight() {
          return 18;
        }

        @Override
        public void draw(GuiGraphics graphics, int x, int y) {
          UiTheme.slot(graphics, x + 1, y + 1);
        }
      };
  private final RecipeType<ProcessingDisplay> type;
  private final Component title;
  private final IDrawable icon;
  private final int height;

  public ProcessingCategory(
      RecipeType<ProcessingDisplay> type,
      ItemStack machine,
      IGuiHelper gui,
      List<ProcessingDisplay> recipes) {
    this.type = type;
    title = machine.getHoverName();
    icon = gui.createDrawableItemStack(machine);
    int slots =
        recipes.stream()
            .mapToInt(
                display ->
                    Math.max(
                        display.inputs().size(),
                        display.outputs().size() + (display.weightedOutputs().isEmpty() ? 0 : 1)))
            .max()
            .orElse(3);
    height = 58 + ((slots + 2) / 3) * 20;
  }

  @Override
  public RecipeType<ProcessingDisplay> getRecipeType() {
    return type;
  }

  @Override
  public Component getTitle() {
    return title;
  }

  @Override
  public IDrawable getIcon() {
    return icon;
  }

  @Override
  public int getWidth() {
    return 184;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public ResourceLocation getRegistryName(ProcessingDisplay display) {
    return display.id();
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder, ProcessingDisplay display, IFocusGroup focuses) {
    var recipe = display.recipe();
    var inputs = display.inputs();
    for (int i = 0; i < inputs.size(); i++)
      entry(slot(builder, RecipeIngredientRole.INPUT, i), inputs.get(i));
    var outputs = display.outputs();
    for (int i = 0; i < outputs.size(); i++)
      entry(slot(builder, RecipeIngredientRole.OUTPUT, i), outputs.get(i));
    var weighted = display.weightedOutputs();
    if (!weighted.isEmpty()) {
      var slot = slot(builder, RecipeIngredientRole.OUTPUT, outputs.size());
      for (var output : weighted) slot.addItemStacks(output.output().displayStacks());
      slot.addRichTooltipCallback(
          (view, tooltip) -> {
            view.getDisplayedItemStack()
                .ifPresent(
                    stack -> {
                      long weight =
                          weighted.stream()
                              .filter(output -> output.output().matches(stack))
                              .mapToLong(output -> output.weight())
                              .sum();
                      tooltip.add(
                          Texts.translate(
                              "gui.adv_rocketry.processing_category.rolls",
                              ProcessingDisplay.perRoll(weighted, weight),
                              recipe.rolls()));
                    });
          });
    }
  }

  private static IRecipeSlotBuilder slot(
      IRecipeLayoutBuilder builder, RecipeIngredientRole role, int index) {
    return builder
        .addSlot(
            role,
            (role == RecipeIngredientRole.INPUT ? 5 : 119) + index % 3 * 20,
            18 + index / 3 * 20)
        .setBackground(SLOT, -1, -1);
  }

  private static void entry(IRecipeSlotBuilder slot, ProcessingDisplay.Entry entry) {
    if (entry.fluid()) {
      for (var fluid : entry.fluids()) slot.addFluidStack(fluid.getFluid(), fluid.getAmount());
      slot.setFluidRenderer(Math.max(1000, entry.amount()), false, 16, 16);
    } else slot.addItemStacks(entry.items());
    for (Component note : entry.notes())
      slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(note));
  }

  @Override
  public void draw(
      ProcessingDisplay display,
      IRecipeSlotsView slots,
      GuiGraphics graphics,
      double mouseX,
      double mouseY) {
    UiTheme.panel(graphics, 0, 0, getWidth(), getHeight());
    var font = Minecraft.getInstance().font;
    graphics.drawString(
        font,
        Component.translatable("gui.adv_rocketry.processing.inputs"),
        4,
        4,
        UiTheme.TEXT,
        false);
    graphics.drawString(
        font,
        Component.translatable("gui.adv_rocketry.processing.outputs"),
        118,
        4,
        UiTheme.TEXT,
        false);
    graphics.drawString(font, "->", 84, 24, UiTheme.ACCENT, false);
    var recipe = display.recipe();
    graphics.drawString(
        font,
        Texts.translate("gui.adv_rocketry.processing.ticks", recipe.ticks()),
        4,
        getHeight() - 38,
        UiTheme.TEXT,
        false);
    graphics.drawString(
        font,
        Texts.translate("gui.adv_rocketry.processing.energy", recipe.energy()),
        4,
        getHeight() - 26,
        UiTheme.TEXT,
        false);
    Component note =
        recipe.machine() == MachineType.PLATE_PRESS
            ? Component.translatable("gui.adv_rocketry.processing.redstone_press")
            : recipe.machine() == MachineType.CRYSTALLIZER
                    && AdvancedRocketryConfig.crystallizerMaximumGravity() > 0
                ? Texts.translate(
                    "gui.adv_rocketry.processing.gravity_limit",
                    AdvancedRocketryConfig.crystallizerMaximumGravity())
                : Component.translatable("gui.adv_rocketry.processing.base_time");
    graphics.drawString(
        font, UiTheme.fit(font, note, 176), 4, getHeight() - 14, UiTheme.MUTED, false);
  }
}

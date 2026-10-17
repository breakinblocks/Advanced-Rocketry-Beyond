package advRocketry.compat.jei;

import advRocketry.client.ui.UiTheme;
import advRocketry.processing.LiquidFuelRecipe;
import advRocketry.processing.MachinePorts;
import advRocketry.util.Texts;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.crafting.RecipeHolder;

public final class LiquidFuelCategory implements IRecipeCategory<LiquidFuelCategory.Display> {
  public static final RecipeType<Display> TYPE =
      RecipeType.create("adv_rocketry", "liquid_fuel", Display.class);

  public record Display(ResourceLocation id, LiquidFuelRecipe recipe) {
    public Display(RecipeHolder<LiquidFuelRecipe> holder) {
      this(holder.id(), holder.value());
    }
  }

  private final IDrawable icon;

  public LiquidFuelCategory(IGuiHelper gui) {
    icon = gui.createDrawableItemStack(new ItemStack(MachinePorts.FUEL_GENERATOR.get()));
  }

  @Override
  public RecipeType<Display> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("block.adv_rocketry.fuel_generator");
  }

  @Override
  public IDrawable getIcon() {
    return icon;
  }

  @Override
  public int getWidth() {
    return 180;
  }

  @Override
  public int getHeight() {
    return 82;
  }

  @Override
  public ResourceLocation getRegistryName(Display display) {
    return display.id();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
    var slot = builder.addSlot(RecipeIngredientRole.INPUT, 8, 8);
    for (var fluid : display.recipe().fuel().getFluids())
      slot.addFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch());
    slot.setFluidRenderer(Math.max(1000, display.recipe().fuel().amount()), false, 16, 32);
  }

  @Override
  public void draw(
      Display display, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
    UiTheme.panel(graphics, 0, 0, getWidth(), getHeight());
    var font = Minecraft.getInstance().font;
    var recipe = display.recipe();
    graphics.drawString(
        font,
        Texts.translate("gui.adv_rocketry.liquid_fuel.amount", recipe.fuel().amount()),
        32,
        8,
        UiTheme.TEXT,
        false);
    graphics.drawString(
        font,
        Texts.translate("gui.adv_rocketry.liquid_fuel.rate", recipe.fePerTick()),
        32,
        24,
        UiTheme.TEXT,
        false);
    graphics.drawString(
        font,
        Texts.translate(
            "gui.adv_rocketry.liquid_fuel.duration", recipe.ticks(), recipe.ticks() / 20d),
        8,
        48,
        UiTheme.TEXT,
        false);
    graphics.drawString(
        font,
        Texts.translate("gui.adv_rocketry.liquid_fuel.total", recipe.totalEnergy()),
        8,
        64,
        UiTheme.TEXT,
        false);
  }
}

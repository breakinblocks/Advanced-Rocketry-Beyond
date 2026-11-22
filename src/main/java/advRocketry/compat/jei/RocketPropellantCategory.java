package advRocketry.compat.jei;

import advRocketry.DataMaps;
import advRocketry.client.ui.UiTheme;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.rocket.RocketRegistry;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class RocketPropellantCategory implements IRecipeCategory<RocketPropellantCategory.Display> {
    public static final RecipeType<Display> TYPE =
            RecipeType.create("adv_rocketry", "rocket_propellant", Display.class);
    private static final int ROW = 20;

    public record Use(RocketPartBlock.Fuel fuel, float multiplier) {}

    public record Display(ResourceLocation id, Fluid fluid, List<Use> uses) {}

    public static List<Display> collect() {
        List<Display> displays = new ArrayList<>();
        BuiltInRegistries.FLUID.getDataMap(DataMaps.ROCKET_PROPELLANT).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(key -> key.location())))
                .forEach(entry -> {
                    Fluid fluid = BuiltInRegistries.FLUID.get(entry.getKey());
                    if (fluid == null || !fluid.isSource(fluid.defaultFluidState())) return;
                    var propellant = entry.getValue();
                    List<Use> uses = new ArrayList<>();
                    use(uses, RocketPartBlock.Fuel.MONOPROPELLANT, propellant.monopropellant());
                    use(uses, RocketPartBlock.Fuel.BIPROPELLANT, propellant.bipropellant());
                    use(uses, RocketPartBlock.Fuel.OXIDIZER, propellant.oxidizer());
                    use(uses, RocketPartBlock.Fuel.NUCLEAR, propellant.nuclear());
                    if (!uses.isEmpty()) displays.add(new Display(entry.getKey().location(), fluid, List.copyOf(uses)));
                });
        return displays;
    }

    private static void use(List<Use> uses, RocketPartBlock.Fuel fuel, Optional<Float> value) {
        value.filter(multiplier -> multiplier > 0).ifPresent(multiplier -> uses.add(new Use(fuel, multiplier)));
    }

    static List<ItemStack> parts(RocketPartBlock.Fuel fuel, RocketPartBlock.Kind kind) {
        RocketPartBlock.Fuel engineFuel =
                fuel == RocketPartBlock.Fuel.OXIDIZER ? RocketPartBlock.Fuel.BIPROPELLANT : fuel;
        RocketPartBlock.Fuel tankFuel = fuel;
        return RocketRegistry.PARTS.values().stream()
                .map(supplier -> supplier.get())
                .filter(part ->
                        part.kind == kind && part.fuel == (kind == RocketPartBlock.Kind.ENGINE ? engineFuel : tankFuel))
                .map(ItemStack::new)
                .toList();
    }

    private final IDrawable icon;

    public RocketPropellantCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(
                new ItemStack(RocketRegistry.PARTS.get("rocket_motor").get()));
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.adv_rocketry.jei.rocket_propellant");
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
        return 28 + RocketPartBlock.Fuel.values().length * ROW;
    }

    @Override
    public ResourceLocation getRegistryName(Display display) {
        return display.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addFluidStack(display.fluid(), 1000)
                .setFluidRenderer(1000, false, 16, 16);
        for (int i = 0; i < display.uses().size(); i++) {
            Use use = display.uses().get(i);
            int y = 28 + i * ROW;
            List<ItemStack> tanks = parts(use.fuel(), RocketPartBlock.Kind.TANK);
            List<ItemStack> engines = parts(use.fuel(), RocketPartBlock.Kind.ENGINE);
            if (!tanks.isEmpty())
                builder.addSlot(RecipeIngredientRole.CATALYST, 6, y)
                        .setBackground(ProcessingCategory.SLOT, -1, -1)
                        .addItemStacks(tanks);
            if (!engines.isEmpty())
                builder.addSlot(RecipeIngredientRole.CATALYST, 26, y)
                        .setBackground(ProcessingCategory.SLOT, -1, -1)
                        .addItemStacks(engines);
        }
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        UiTheme.panel(graphics, 0, 0, getWidth(), getHeight());
        var font = Minecraft.getInstance().font;
        MissionJei.text(
                graphics,
                font,
                Component.translatable("gui.adv_rocketry.jei.rocket_propellant.header"),
                28,
                6,
                getWidth() - 34,
                UiTheme.MUTED);
        for (int i = 0; i < display.uses().size(); i++) {
            Use use = display.uses().get(i);
            graphics.drawString(
                    font,
                    Texts.translate(
                            "gui.adv_rocketry.jei.rocket_propellant."
                                    + use.fuel().name().toLowerCase(Locale.ROOT),
                            String.format("%.2f", use.multiplier())),
                    48,
                    32 + i * ROW,
                    UiTheme.TEXT,
                    false);
        }
    }
}

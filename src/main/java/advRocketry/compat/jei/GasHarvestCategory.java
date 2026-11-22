package advRocketry.compat.jei;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.DataMaps;
import advRocketry.ModTags;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

public final class GasHarvestCategory implements IRecipeCategory<GasHarvestCategory.Display> {
    public static final RecipeType<Display> TYPE = RecipeType.create("adv_rocketry", "gas_harvest", Display.class);
    private static final int INTAKE_POWER = 10;

    public record Display(ResourceLocation id, Fluid fluid, DataMaps.GasGiantGas gas) {}

    public static List<Display> collect() {
        Map<ResourceLocation, Display> displays = new LinkedHashMap<>();
        BuiltInRegistries.FLUID.getDataMap(DataMaps.GAS_GIANT_GAS).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(key -> key.location())))
                .forEach(entry -> {
                    ResourceLocation id = entry.getKey().location();
                    Fluid fluid = BuiltInRegistries.FLUID.get(id);
                    if (entry.getValue().chance() > 0) displays.put(id, new Display(id, fluid, entry.getValue()));
                });
        for (var holder : BuiltInRegistries.FLUID.getTagOrEmpty(ModTags.HARVESTABLE_GASES))
            holder.unwrapKey()
                    .ifPresent(key -> displays.put(key.location(), new Display(key.location(), holder.value(), null)));
        List<Display> result = new ArrayList<>();
        for (Display display : displays.values())
            if (display.fluid().isSource(display.fluid().defaultFluidState())) result.add(display);
        return result;
    }

    private final IDrawable icon;

    public GasHarvestCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(ProcessingRegistry.part("gas_intake")));
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.adv_rocketry.jei.gas_harvest");
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
        return 112;
    }

    @Override
    public ResourceLocation getRegistryName(Display display) {
        return display.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addItemStack(new ItemStack(ProcessingRegistry.part("gas_intake")))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("gui.adv_rocketry.jei.gas_harvest.intake")));
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 6)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addItemStack(new ItemStack(OrbitalRegistry.FLUID_TANK.get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("gui.adv_rocketry.jei.gas_harvest.tank")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 156, 6)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addFluidStack(display.fluid(), 1000)
                .setFluidRenderer(1000, false, 16, 16);
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        UiTheme.panel(graphics, 0, 0, getWidth(), getHeight());
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "→", 84, 10, UiTheme.MUTED, false);
        var gas = display.gas();
        Component where = gas == null
                ? Component.translatable("gui.adv_rocketry.jei.gas_harvest.every_giant")
                : Texts.translate(
                        "gui.adv_rocketry.jei.gas_harvest.chance",
                        Math.round(gas.chance() * 100),
                        gravity(gas.minGravity(), gas.maxGravity()));
        int width = getWidth() - 12;
        int y = MissionJei.text(graphics, font, where, 6, 30, width, UiTheme.TEXT);
        long ticks = Math.round(2d * 1000 / INTAKE_POWER * AdvancedRocketryConfig.gasMissionMultiplier());
        y = MissionJei.text(
                graphics,
                font,
                Texts.translate("gui.adv_rocketry.jei.gas_harvest.time", MissionJei.time(ticks)),
                6,
                y,
                width,
                UiTheme.TEXT);
        y = MissionJei.text(
                graphics,
                font,
                Component.translatable("gui.adv_rocketry.jei.gas_harvest.station"),
                6,
                y + 2,
                width,
                UiTheme.MUTED);
        MissionJei.text(
                graphics,
                font,
                Component.translatable("gui.adv_rocketry.jei.gas_harvest.unmanned"),
                6,
                y,
                width,
                UiTheme.MUTED);
    }

    private static Component gravity(float minimum, float maximum) {
        if (minimum <= 0 && maximum >= 16)
            return Component.translatable("gui.adv_rocketry.jei.gas_harvest.any_gravity");
        if (minimum <= 0) return Texts.translate("gui.adv_rocketry.jei.gas_harvest.gravity_below", maximum);
        if (maximum >= 16) return Texts.translate("gui.adv_rocketry.jei.gas_harvest.gravity_above", minimum);
        return Texts.translate("gui.adv_rocketry.jei.gas_harvest.gravity_between", minimum, maximum);
    }
}

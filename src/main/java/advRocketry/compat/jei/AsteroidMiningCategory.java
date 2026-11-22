package advRocketry.compat.jei;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.AsteroidCatalog;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.rocket.RocketRegistry;
import advRocketry.util.IdOrTag;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AsteroidMiningCategory implements IRecipeCategory<AsteroidMiningCategory.Display> {
    public static final RecipeType<Display> TYPE = RecipeType.create("adv_rocketry", "asteroid_mining", Display.class);
    static final float DRILL_POWER = .02f;
    private static final int OUTPUTS_PER_ROW = 9;
    private static final int OUTPUT_ROWS = 2;

    public record Yield(Item item, int expected, int minimum, int maximum, float share) {}

    public record Display(AsteroidCatalog.Asteroid asteroid, List<Yield> yields) {
        public static Display of(AsteroidCatalog.Asteroid asteroid) {
            float massLow = Math.max(1, asteroid.mass() * (1 - asteroid.massVariability() / 2));
            float massHigh = asteroid.mass() * (1 + asteroid.massVariability() / 2);
            float richLow = Math.clamp(asteroid.richness() - asteroid.richnessVariability() / 2, 0, 1);
            float richHigh = Math.clamp(asteroid.richness() + asteroid.richnessVariability() / 2, 0, 1);
            float rich = asteroid.ores().isEmpty() ? 0 : Math.clamp(asteroid.richness(), 0, 1);
            if (asteroid.ores().isEmpty()) richLow = richHigh = 0;
            List<Yield> yields = new ArrayList<>();
            add(
                    yields,
                    asteroid.base(),
                    asteroid.mass() * (1 - rich),
                    massLow * (1 - richHigh),
                    massHigh * (1 - richLow),
                    1 - rich);
            float total = 0;
            for (AsteroidCatalog.Ore ore : asteroid.ores()) total += ore.weight();
            for (AsteroidCatalog.Ore ore : asteroid.ores()) {
                float share = total <= 0 ? 0 : ore.weight() / total;
                add(
                        yields,
                        ore.item(),
                        asteroid.mass() * rich * share,
                        massLow * richLow * share,
                        massHigh * richHigh * share,
                        rich * share);
            }
            return new Display(asteroid, List.copyOf(yields));
        }

        private static void add(
                List<Yield> yields, ResourceLocation id, float expected, float low, float high, float share) {
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
            if (item == Items.AIR) return;
            yields.add(new Yield(item, Math.max(1, Math.round(expected)), Math.round(low), Math.round(high), share));
        }
    }

    private final IDrawable icon;

    public AsteroidMiningCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get()));
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.adv_rocketry.jei.asteroid_mining");
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
        return 30 + OUTPUT_ROWS * 18 + 48;
    }

    @Override
    public ResourceLocation getRegistryName(Display display) {
        return ResourceLocation.parse(display.asteroid().id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 18)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addItemStack(new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("gui.adv_rocketry.jei.asteroid_mining.chip")));
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 18)
                .setBackground(ProcessingCategory.SLOT, -1, -1)
                .addItemStack(
                        new ItemStack(RocketRegistry.PARTS.get("mining_drill").get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("gui.adv_rocketry.jei.asteroid_mining.drill")));
        int x = 50;
        for (IdOrTag artifact : display.asteroid().requiredArtifacts()) {
            List<ItemStack> stacks = artifact.items().stream()
                    .map(item -> new ItemStack(item, Math.max(1, artifact.count())))
                    .toList();
            if (stacks.isEmpty() || x > 150) continue;
            builder.addSlot(RecipeIngredientRole.INPUT, x, 18)
                    .setBackground(ProcessingCategory.SLOT, -1, -1)
                    .addItemStacks(stacks)
                    .addRichTooltipCallback((view, tooltip) ->
                            tooltip.add(Component.translatable("gui.adv_rocketry.jei.asteroid_mining.artifact")));
            x += 18;
        }
        List<Yield> yields = display.yields();
        for (int i = 0; i < yields.size() && i < OUTPUTS_PER_ROW * OUTPUT_ROWS; i++) {
            Yield yield = yields.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 6 + i % OUTPUTS_PER_ROW * 18, 44 + i / OUTPUTS_PER_ROW * 18)
                    .setBackground(ProcessingCategory.SLOT, -1, -1)
                    .addItemStack(new ItemStack(yield.item(), yield.expected()))
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Texts.translate(
                                "gui.adv_rocketry.jei.asteroid_mining.yield",
                                yield.expected(),
                                yield.minimum(),
                                yield.maximum()));
                        tooltip.add(Texts.translate(
                                "gui.adv_rocketry.jei.asteroid_mining.share",
                                String.format("%.1f", yield.share() * 100)));
                    });
        }
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        UiTheme.panel(graphics, 0, 0, getWidth(), getHeight());
        var font = Minecraft.getInstance().font;
        var asteroid = display.asteroid();
        graphics.drawString(font, name(asteroid), 6, 5, UiTheme.ACCENT, false);
        int y = 30 + OUTPUT_ROWS * 18 + 4;
        int width = getWidth() - 12;
        y = MissionJei.text(
                graphics,
                font,
                Texts.translate(
                        "gui.adv_rocketry.jei.asteroid_mining.mass",
                        asteroid.mass(),
                        Math.round(asteroid.richness() * 100)),
                6,
                y,
                width,
                UiTheme.TEXT);
        long ticks = (long)
                (360 / DRILL_POWER * asteroid.timeMultiplier() * AdvancedRocketryConfig.miningMissionTimeMultiplier());
        y = MissionJei.text(
                graphics,
                font,
                Texts.translate("gui.adv_rocketry.jei.mission_time_one_drill", MissionJei.time(ticks)),
                6,
                y,
                width,
                UiTheme.TEXT);
        MissionJei.text(
                graphics,
                font,
                Component.translatable("gui.adv_rocketry.jei.asteroid_mining.unmanned"),
                6,
                y + 2,
                width,
                UiTheme.MUTED);
    }

    static Component name(AsteroidCatalog.Asteroid asteroid) {
        ResourceLocation id = ResourceLocation.parse(asteroid.id());
        String fallback = String.join(
                " ",
                Arrays.stream(id.getPath().split("_"))
                        .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .toList());
        return Component.translatableWithFallback(asteroid.name(), fallback + " Asteroid");
    }
}

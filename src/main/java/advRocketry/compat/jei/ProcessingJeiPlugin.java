// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.compat.jei;

import advRocketry.Main;
import advRocketry.compat.ProcessingDisplay;
import advRocketry.processing.MachineType;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.processing.client.MachineScreen;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Optional original eleven-machine JEI integration; no runtime dependency when JEI is absent. */
@JeiPlugin
public final class ProcessingJeiPlugin implements IModPlugin {
  private final Map<MachineType, RecipeType<ProcessingDisplay>> types = new LinkedHashMap<>();
  private List<ProcessingDisplay> recipes = List.of();

  @Override
  public ResourceLocation getPluginUid() {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, "processing");
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    recipes = ProcessingDisplay.collect();
    types.clear();
    for (MachineType machine : MachineType.values()) {
      var type = RecipeType.create(Main.MODID, machine.id(), ProcessingDisplay.class);
      types.put(machine, type);
      registration.addRecipeCategories(
          new ProcessingCategory(
              type,
              catalyst(machine),
              registration.getJeiHelpers().getGuiHelper(),
              recipes.stream().filter(display -> display.recipe().machine() == machine).toList()));
    }
  }

  private static ItemStack catalyst(MachineType machine) {
    return new ItemStack(
        machine.multiblock()
            ? ProcessingRegistry.MACHINE_BLOCKS.get(machine).get()
            : ProcessingRegistry.part(machine.id()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    types.forEach(
        (machine, type) ->
            registration.addRecipes(
                type,
                recipes.stream()
                    .filter(display -> display.recipe().machine() == machine)
                    .toList()));
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    types.forEach((machine, type) -> registration.addRecipeCatalyst(catalyst(machine), type));
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(
        MachineScreen.class,
        new IGuiContainerHandler<MachineScreen>() {
          @Override
          public Collection<IGuiClickableArea> getGuiClickableAreas(
              MachineScreen screen, double mouseX, double mouseY) {
            var level = Minecraft.getInstance().level;
            if (level == null
                || !(level.getBlockEntity(screen.getMenu().controller())
                    instanceof ProcessingBlockEntity machine)) return List.of();
            var type = types.get(machine.machine());
            return type == null
                ? List.of()
                : List.of(
                    IGuiClickableArea.createBasic(
                        MachineScreen.PROGRESS_X,
                        MachineScreen.PROGRESS_Y,
                        MachineScreen.PROGRESS_WIDTH,
                        MachineScreen.PROGRESS_HEIGHT,
                        type));
          }
        });
  }
}

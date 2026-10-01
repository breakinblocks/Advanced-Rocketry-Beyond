package advRocketry.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

final class ModRecipeProvider extends RecipeProvider {
  ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
    super(output, lookup);
  }

  @Override
  protected void buildRecipes(RecipeOutput output) {
    ModCraftingRecipes.build(output);
    ModProcessingRecipes.build(output);
  }
}

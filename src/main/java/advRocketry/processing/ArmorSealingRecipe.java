package advRocketry.processing;

import advRocketry.life.SpaceBreathing;
import advRocketry.life.SpaceSuitItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Datapack-controlled materials and cost for preserving and sealing one armor stack. */
public record ArmorSealingRecipe(
    ArmorItem.Type armorType, List<SizedIngredient> materials, int ticks, int energy)
    implements Recipe<SingleRecipeInput> {
  public static final MapCodec<ArmorSealingRecipe> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      ArmorItem.Type.CODEC
                          .fieldOf("armor_type")
                          .forGetter(ArmorSealingRecipe::armorType),
                      SizedIngredient.FLAT_CODEC
                          .listOf(0, 32)
                          .fieldOf("materials")
                          .forGetter(ArmorSealingRecipe::materials),
                      Codec.intRange(1, 1000000)
                          .fieldOf("ticks")
                          .forGetter(ArmorSealingRecipe::ticks),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .fieldOf("energy")
                          .forGetter(ArmorSealingRecipe::energy))
                  .apply(instance, ArmorSealingRecipe::new));

  public ArmorSealingRecipe {
    materials = List.copyOf(materials);
  }

  public static ArmorSealingRecipe find(Level level, ItemStack armor) {
    return level
        .getRecipeManager()
        .getRecipeFor(ProcessingRegistry.SEALING_TYPE.get(), new SingleRecipeInput(armor), level)
        .map(holder -> holder.value())
        .orElse(null);
  }

  @Override
  public boolean matches(SingleRecipeInput input, Level level) {
    ItemStack armor = input.item();
    return armor.getItem() instanceof ArmorItem item
        && item.getType() == armorType
        && !(item instanceof SpaceSuitItem)
        && !SpaceBreathing.sealed(level, armor);
  }

  @Override
  public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
    ItemStack output = input.item().copyWithCount(1);
    output.enchant(
        registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SpaceBreathing.KEY), 1);
    return output;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return false;
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  @Override
  public RecipeType<?> getType() {
    return ProcessingRegistry.SEALING_TYPE.get();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ProcessingRegistry.SEALING_SERIALIZER.get();
  }

  public static final class Serializer implements RecipeSerializer<ArmorSealingRecipe> {
    private static final StreamCodec<RegistryFriendlyByteBuf, ArmorSealingRecipe> STREAM =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    @Override
    public MapCodec<ArmorSealingRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ArmorSealingRecipe> streamCodec() {
      return STREAM;
    }
  }
}

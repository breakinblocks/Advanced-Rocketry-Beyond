package advRocketry.processing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public record LiquidFuelRecipe(SizedFluidIngredient fuel, int ticks, int fePerTick)
    implements Recipe<LiquidFuelRecipe.Input> {
  public static final MapCodec<LiquidFuelRecipe> CODEC =
      RecordCodecBuilder.<LiquidFuelRecipe>mapCodec(
              instance ->
                  instance
                      .group(
                          SizedFluidIngredient.FLAT_CODEC
                              .fieldOf("fuel")
                              .forGetter(LiquidFuelRecipe::fuel),
                          Codec.intRange(1, 1000000)
                              .fieldOf("ticks")
                              .forGetter(LiquidFuelRecipe::ticks),
                          Codec.intRange(1, FuelGeneratorBlockEntity.CAPACITY)
                              .fieldOf("fe_per_tick")
                              .forGetter(LiquidFuelRecipe::fePerTick))
                      .apply(instance, LiquidFuelRecipe::new))
          .validate(
              recipe ->
                  recipe.fuel().amount() <= FuelGeneratorBlockEntity.TANK_CAPACITY
                      ? DataResult.success(recipe)
                      : DataResult.error(
                          () -> "Fuel amount exceeds the generator's 16000 mB tank"));

  public long totalEnergy() {
    return (long) ticks * fePerTick;
  }

  public record Input(FluidStack fluid) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
      throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return fluid.isEmpty();
    }
  }

  @Override
  public boolean matches(Input input, Level level) {
    return fuel.test(input.fluid());
  }

  @Override
  public ItemStack assemble(Input input, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
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
    return ProcessingRegistry.LIQUID_FUEL_TYPE.get();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ProcessingRegistry.LIQUID_FUEL_SERIALIZER.get();
  }

  public static final class Serializer implements RecipeSerializer<LiquidFuelRecipe> {
    private static final StreamCodec<RegistryFriendlyByteBuf, LiquidFuelRecipe> STREAM =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    @Override
    public MapCodec<LiquidFuelRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LiquidFuelRecipe> streamCodec() {
      return STREAM;
    }
  }
}

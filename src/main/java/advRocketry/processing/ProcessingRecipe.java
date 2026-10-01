// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.AdvancedRocketryConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public record ProcessingRecipe(
    MachineType machine,
    int ticks,
    int energy,
    List<SizedIngredient> inputs,
    List<SizedIngredient> catalysts,
    List<SizedFluidIngredient> fluidInputs,
    List<ProcessingOutput> outputs,
    List<FluidStack> fluidOutputs,
    List<WeightedOutput> weightedOutputs,
    int rolls)
    implements Recipe<ProcessingInput> {
  public static final MapCodec<ProcessingRecipe> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      MachineType.CODEC.fieldOf("machine").forGetter(ProcessingRecipe::machine),
                      Codec.intRange(1, Integer.MAX_VALUE)
                          .fieldOf("ticks")
                          .forGetter(ProcessingRecipe::ticks),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .fieldOf("energy")
                          .forGetter(ProcessingRecipe::energy),
                      SizedIngredient.FLAT_CODEC
                          .listOf()
                          .optionalFieldOf("inputs", List.of())
                          .forGetter(ProcessingRecipe::inputs),
                      SizedIngredient.FLAT_CODEC
                          .listOf()
                          .optionalFieldOf("catalysts", List.of())
                          .forGetter(ProcessingRecipe::catalysts),
                      SizedFluidIngredient.FLAT_CODEC
                          .listOf()
                          .optionalFieldOf("fluid_inputs", List.of())
                          .forGetter(ProcessingRecipe::fluidInputs),
                      ProcessingOutput.CODEC
                          .listOf()
                          .optionalFieldOf("outputs", List.of())
                          .forGetter(ProcessingRecipe::outputs),
                      FluidStack.CODEC
                          .listOf()
                          .optionalFieldOf("fluid_outputs", List.of())
                          .forGetter(ProcessingRecipe::fluidOutputs),
                      WeightedOutput.CODEC
                          .listOf()
                          .optionalFieldOf("weighted_outputs", List.of())
                          .forGetter(ProcessingRecipe::weightedOutputs),
                      Codec.intRange(0, 64)
                          .optionalFieldOf("rolls", 0)
                          .forGetter(ProcessingRecipe::rolls))
                  .apply(instance, ProcessingRecipe::new));
  public static final Codec<ProcessingRecipe> CODEC = MAP_CODEC.codec();

  public ProcessingRecipe(
      MachineType machine,
      int ticks,
      int energy,
      List<SizedIngredient> inputs,
      List<SizedFluidIngredient> fluidInputs,
      List<ProcessingOutput> outputs) {
    this(machine, ticks, energy, inputs, List.of(), fluidInputs, outputs, List.of(), List.of(), 0);
  }

  public record WeightedOutput(ProcessingOutput output, int weight) {
    public static final Codec<WeightedOutput> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ProcessingOutput.CODEC.fieldOf("output").forGetter(WeightedOutput::output),
                        Codec.intRange(1, 1000000)
                            .fieldOf("weight")
                            .forGetter(WeightedOutput::weight))
                    .apply(instance, WeightedOutput::new));
  }

  public record Rolled(List<ItemStack> items, List<FluidStack> fluids) {
    public static final Rolled EMPTY = new Rolled(List.of(), List.of());
    public static final Codec<Rolled> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ItemStack.CODEC
                            .listOf()
                            .optionalFieldOf("items", List.of())
                            .forGetter(Rolled::items),
                        FluidStack.CODEC
                            .listOf()
                            .optionalFieldOf("fluids", List.of())
                            .forGetter(Rolled::fluids))
                    .apply(instance, Rolled::new));

    public boolean resolvable() {
      return items.stream().noneMatch(ItemStack::isEmpty)
          && fluids.stream().noneMatch(FluidStack::isEmpty);
    }
  }

  public Rolled roll(RandomSource random) {
    List<ItemStack> items = new ArrayList<>();
    for (var output : outputs) {
      int count = 0;
      for (int i = 0; i < output.count(); i++) if (random.nextFloat() < output.chance()) count++;
      if (count > 0) items.add(output.stack(count));
    }
    List<WeightedOutput> pool = weightedPool();
    int totalWeight = pool.stream().mapToInt(WeightedOutput::weight).sum();
    for (int i = 0; i < rolls && totalWeight > 0; i++) {
      int choice = random.nextInt(totalWeight);
      for (var output : pool) {
        choice -= output.weight();
        if (choice < 0) {
          items.add(output.output().stack());
          break;
        }
      }
    }
    return new Rolled(List.copyOf(items), fluidOutputs.stream().map(FluidStack::copy).toList());
  }

  public List<WeightedOutput> weightedPool() {
    return weightedOutputs.stream().filter(output -> output.output().resolvable()).toList();
  }

  public boolean outputsResolvable() {
    return outputs.stream().allMatch(ProcessingOutput::resolvable)
        && (rolls == 0 || !weightedPool().isEmpty());
  }

  public static boolean enabled(ResourceLocation id) {
    return AdvancedRocketryConfig.sawmillVanillaWood()
        || !id.getPath().startsWith("processing/planks_");
  }

  public static boolean available(
      List<SizedIngredient> required,
      List<SizedFluidIngredient> fluidsRequired,
      List<ItemStack> items,
      List<FluidStack> fluids) {
    List<ItemStack> itemPool = items.stream().map(ItemStack::copy).toList();
    List<FluidStack> fluidPool = fluids.stream().map(FluidStack::copy).toList();
    for (var ingredient : required) {
      int remaining = ingredient.count();
      for (var stack : itemPool) {
        if (remaining == 0) break;
        if (stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
        int take = Math.min(remaining, stack.getCount());
        stack.shrink(take);
        remaining -= take;
      }
      if (remaining > 0) return false;
    }
    for (var ingredient : fluidsRequired) {
      int remaining = ingredient.amount();
      for (var stack : fluidPool) {
        if (remaining == 0) break;
        if (stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
        int take = Math.min(remaining, stack.getAmount());
        stack.shrink(take);
        remaining -= take;
      }
      if (remaining > 0) return false;
    }
    return true;
  }

  public List<SizedIngredient> requiredItems() {
    List<SizedIngredient> required = new ArrayList<>(catalysts);
    required.addAll(inputs);
    return required;
  }

  @Override
  public boolean matches(ProcessingInput input, Level level) {
    return input.machine() == machine
        && outputsResolvable()
        && available(requiredItems(), fluidInputs, input.items(), input.fluids());
  }

  @Override
  public ItemStack assemble(ProcessingInput input, HolderLookup.Provider registries) {
    return getResultItem(registries).copy();
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return false;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().stack();
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ProcessingRegistry.SERIALIZER.get();
  }

  @Override
  public RecipeType<?> getType() {
    return ProcessingRegistry.RECIPE_TYPE.get();
  }
}

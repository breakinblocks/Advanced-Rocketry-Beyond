package advRocketry.processing;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record ProcessingInput(MachineType machine, List<ItemStack> items, List<FluidStack> fluids)
    implements RecipeInput {
  @Override
  public ItemStack getItem(int index) {
    return items.get(index);
  }

  @Override
  public int size() {
    return items.size();
  }

  @Override
  public boolean isEmpty() {
    return items.stream().allMatch(ItemStack::isEmpty)
        && fluids.stream().allMatch(FluidStack::isEmpty);
  }
}

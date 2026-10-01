// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Reserve output space in independent copies, across every hatch and every result. */
public final class OutputSimulation {
  private OutputSimulation() {}

  public static boolean canInsertAllItems(
      List<? extends IItemHandler> handlers, List<ItemStack> outputs) {
    List<ItemStack> contents = new ArrayList<>();
    List<Integer> limits = new ArrayList<>();
    for (var handler : handlers)
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        contents.add(handler.getStackInSlot(slot).copy());
        limits.add(handler.getSlotLimit(slot));
      }
    for (ItemStack output : outputs) {
      int remaining = output.getCount();
      for (int i = 0; i < contents.size() && remaining > 0; i++) {
        ItemStack stored = contents.get(i);
        if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, output)) continue;
        int space =
            Math.max(0, Math.min(limits.get(i), output.getMaxStackSize()) - stored.getCount());
        int amount = Math.min(remaining, space);
        if (stored.isEmpty()) contents.set(i, output.copyWithCount(amount));
        else stored.grow(amount);
        remaining -= amount;
      }
      if (remaining > 0) return false;
    }
    return true;
  }

  public static boolean canInsertAllFluids(
      List<? extends IFluidHandler> handlers, List<FluidStack> outputs) {
    List<FluidStack> contents = new ArrayList<>();
    List<Integer> capacities = new ArrayList<>();
    for (var handler : handlers)
      for (int tank = 0; tank < handler.getTanks(); tank++) {
        contents.add(handler.getFluidInTank(tank).copy());
        capacities.add(handler.getTankCapacity(tank));
      }
    for (FluidStack output : outputs) {
      int remaining = output.getAmount();
      for (int i = 0; i < contents.size() && remaining > 0; i++) {
        FluidStack stored = contents.get(i);
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, output)) continue;
        int amount = Math.min(remaining, Math.max(0, capacities.get(i) - stored.getAmount()));
        if (stored.isEmpty()) contents.set(i, output.copyWithAmount(amount));
        else stored.grow(amount);
        remaining -= amount;
      }
      if (remaining > 0) return false;
    }
    return true;
  }
}

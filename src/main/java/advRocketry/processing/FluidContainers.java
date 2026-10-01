// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Two-slot container exchange shared by native fluid hatches. */
public final class FluidContainers {
  private FluidContainers() {}

  public static void process(ItemStackHandler inventory, FluidTank tank) {
    process(inventory, tank, true);
  }

  public static void emptyInto(ItemStackHandler inventory, FluidTank tank) {
    process(inventory, tank, false);
  }

  private static void process(ItemStackHandler inventory, FluidTank tank, boolean allowFilling) {
    ItemStack input = inventory.getStackInSlot(0);
    var container = input.getCapability(Capabilities.FluidHandler.ITEM);
    if (container == null || container.getTanks() == 0) return;
    FluidStack contents = container.getFluidInTank(0);
    if (!allowFilling && contents.isEmpty()) return;
    boolean empty =
        !allowFilling
            || !contents.isEmpty()
                && (contents.getAmount() >= container.getTankCapacity(0) || tank.isEmpty());
    var simulated =
        empty
            ? FluidUtil.tryEmptyContainer(input, tank, Integer.MAX_VALUE, null, false)
            : FluidUtil.tryFillContainer(input, tank, Integer.MAX_VALUE, null, false);
    if (!simulated.isSuccess() || !inventory.insertItem(1, simulated.getResult(), true).isEmpty())
      return;
    var result =
        empty
            ? FluidUtil.tryEmptyContainer(input, tank, Integer.MAX_VALUE, null, true)
            : FluidUtil.tryFillContainer(input, tank, Integer.MAX_VALUE, null, true);
    if (!result.isSuccess()) return;
    inventory.extractItem(0, 1, false);
    inventory.insertItem(1, result.getResult(), false);
  }

  public static IItemHandler automation(ItemStackHandler inventory) {
    return new IItemHandler() {
      @Override
      public int getSlots() {
        return 2;
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
      }

      @Override
      public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? inventory.insertItem(slot, stack, simulate) : stack;
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot == 1 ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      }
    };
  }
}

package advRocketry.rocket;

import advRocketry.orbit.GasMission;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

final class RocketCargo {
  private RocketCargo() {}

  static int moveItems(RocketEntity rocket, ItemStackHandler station, boolean unload) {
    if (rocket.flight() != 0) return 0;
    for (RocketStructure.Cell cell : rocket.structure.cells) {
      if (!RocketEntity.isItemPort(cell)) continue;
      ItemStackHandler cargo = new ItemStackHandler(4);
      cargo.deserializeNBT(rocket.registryAccess(), cell.data().getCompound("inventory"));
      for (int sourceSlot = 0; sourceSlot < 4; sourceSlot++)
        for (int targetSlot = 0; targetSlot < 4; targetSlot++) {
          ItemStackHandler source = unload ? cargo : station;
          ItemStackHandler target = unload ? station : cargo;
          ItemStack stack = source.getStackInSlot(sourceSlot);
          if (stack.isEmpty()) continue;
          ItemStack remainder = target.insertItem(targetSlot, stack, false);
          int moved = stack.getCount() - remainder.getCount();
          if (moved <= 0) continue;
          source.extractItem(sourceSlot, moved, false);
          cell.data().put("inventory", cargo.serializeNBT(rocket.registryAccess()));
          return moved;
        }
    }
    return 0;
  }

  static boolean complete(RocketEntity rocket, boolean fluid, boolean unload) {
    for (RocketStructure.Cell cell : rocket.structure.cells) {
      if (fluid) {
        int capacity = GasMission.cellCapacity(cell);
        if (capacity == 0) continue;
        FluidTank cargo = new FluidTank(capacity);
        cargo.readFromNBT(rocket.registryAccess(), cell.data().getCompound("fluid"));
        if (unload ? !cargo.isEmpty() : cargo.getFluidAmount() < capacity) return false;
      } else {
        if (!RocketEntity.isItemPort(cell)) continue;
        ItemStackHandler cargo = new ItemStackHandler(4);
        cargo.deserializeNBT(rocket.registryAccess(), cell.data().getCompound("inventory"));
        for (int slot = 0; slot < cargo.getSlots(); slot++) {
          ItemStack stack = cargo.getStackInSlot(slot);
          if (unload
              ? !stack.isEmpty()
              : stack.isEmpty()
                  || stack.getCount() < Math.min(stack.getMaxStackSize(), cargo.getSlotLimit(slot)))
            return false;
        }
      }
    }
    return true;
  }

  static int moveFluid(RocketEntity rocket, FluidTank station, boolean unload) {
    if (rocket.flight() != 0) return 0;
    for (RocketStructure.Cell cell : rocket.structure.cells) {
      int capacity = GasMission.cellCapacity(cell);
      if (capacity == 0) continue;
      FluidTank cargo = new FluidTank(capacity);
      cargo.readFromNBT(rocket.registryAccess(), cell.data().getCompound("fluid"));
      FluidTank source = unload ? cargo : station;
      FluidTank target = unload ? station : cargo;
      if (source.isEmpty()) continue;
      int moved = target.fill(source.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
      if (moved <= 0) continue;
      source.drain(moved, IFluidHandler.FluidAction.EXECUTE);
      cell.data().put("fluid", cargo.writeToNBT(rocket.registryAccess(), new CompoundTag()));
      return moved;
    }
    return 0;
  }
}

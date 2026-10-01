// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.processing.FluidContainers;
import advRocketry.util.MachineEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original powered fueling station with explicit ground infrastructure association. */
public final class FuelingStationBlockEntity extends BlockEntity
    implements MenuProvider, RocketInfrastructure {
  private final RocketInfrastructureLink link = new RocketInfrastructureLink(this, 10);
  private InfrastructureRedstone.Mode outputMode = InfrastructureRedstone.Mode.ON;
  private ResourceLocation lastFuel;
  public final ItemStackHandler inventory =
      new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }
      };

  public boolean acceptsContainer(ItemStack stack) {
    var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    return handler != null && handler.getTanks() > 0 && validFuel(handler.getFluidInTank(0));
  }

  public IItemHandler automationItems(Direction side) {
    return new IItemHandler() {
      private int actual(int slot) {
        return side == null ? slot : side == Direction.DOWN ? 1 : 0;
      }

      @Override
      public int getSlots() {
        return side == null ? 2 : 1;
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(actual(slot));
      }

      @Override
      public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(actual(slot));
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return actual(slot) == 0 && acceptsContainer(stack);
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack)
            ? inventory.insertItem(actual(slot), stack, simulate)
            : stack;
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return actual(slot) == 1 ? inventory.extractItem(1, amount, simulate) : ItemStack.EMPTY;
      }
    };
  }

  @Override
  public boolean link(RocketEntity rocket) {
    return link.link(rocket);
  }

  @Override
  public boolean linkBuilder(RocketBlockEntity builder) {
    return link.linkBuilder(builder);
  }

  @Override
  public void unlink() {
    link.unlink();
  }

  public int outputMode() {
    return outputMode.ordinal();
  }

  public void cycleOutputMode() {
    outputMode = InfrastructureRedstone.Mode.values()[(outputMode.ordinal() + 1) % 3];
    setChanged();
  }

  public final MachineEnergy energy = MachineEnergy.consumer(1000, this::setChanged);
  public final FluidTank tank =
      new FluidTank(5000, FuelingStationBlockEntity::validFuel) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };

  public FuelingStationBlockEntity(BlockPos pos, BlockState state) {
    super(RocketRegistry.FUELING_STATION_ENTITY.get(), pos, state);
  }

  private static boolean validFuel(FluidStack stack) {
    if (stack.isEmpty()) return false;
    for (RocketPartBlock.Fuel type : RocketPartBlock.Fuel.values())
      if (RocketFuelRegistry.allowed(type, stack)) return true;
    return false;
  }

  public void tick() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    FluidContainers.emptyInto(inventory, tank);
    if (!tank.isEmpty()) {
      var fluid = BuiltInRegistries.FLUID.getKey(tank.getFluid().getFluid());
      if (!fluid.equals(lastFuel)) {
        lastFuel = fluid;
        setChanged();
      }
    }
    RocketEntity rocket = link.rocket();
    boolean full = false;
    if (rocket != null && lastFuel != null) {
      FluidStack available =
          tank.isEmpty()
              ? new FluidStack(BuiltInRegistries.FLUID.get(lastFuel), 1)
              : tank.getFluid().copyWithAmount(Math.min(10, tank.getFluidAmount()));
      int space = rocket.fluids.fill(available, IFluidHandler.FluidAction.SIMULATE);
      if (space > 0 && tank.getFluidAmount() >= 10 && energy.getEnergyStored() >= 30) {
        int moved =
            rocket.fluids.fill(available.copyWithAmount(space), IFluidHandler.FluidAction.EXECUTE);
        if (moved > 0) {
          tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
          energy.consume(30, false);
        }
      }
      for (int slot = 0; slot < rocket.fluids.getTanks(); slot++)
        if (rocket.fluids.isFluidValid(slot, available)
            && rocket.fluids.getFluidInTank(slot).getAmount()
                >= rocket.fluids.getTankCapacity(slot)) full = true;
    }
    full = outputMode.active(full);
    BlockState state = getBlockState();
    if (state.getValue(FuelingStationBlock.POWERED) != full)
      serverLevel.setBlock(worldPosition, state.setValue(FuelingStationBlock.POWERED, full), 3);
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new FuelingStationMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("fluid", tank.writeToNBT(registries, new CompoundTag()));
    tag.put("energy", energy.serializeNBT(registries));
    tag.put("inventory", inventory.serializeNBT(registries));
    tag.putInt("output_mode", outputMode.ordinal());
    if (lastFuel != null) tag.putString("last_fuel", lastFuel.toString());
    link.save(tag);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    tank.readFromNBT(registries, tag.getCompound("fluid"));
    if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
    inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    outputMode = InfrastructureRedstone.Mode.values()[Math.clamp(tag.getInt("output_mode"), 0, 2)];
    lastFuel =
        tag.contains("last_fuel") ? ResourceLocation.tryParse(tag.getString("last_fuel")) : null;
    link.load(tag);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.space.SolarPower;
import advRocketry.util.MachineEnergy;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Native hatch storage corresponding to the original LibVulpes item, fluid and RF ports. */
public final class PortBlockEntity extends BlockEntity implements MenuProvider {
  public static final int FLUID_CAPACITY = 16000;
  public final ItemStackHandler inventory =
      new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }
      };
  public final FluidTank myTank =
      new FluidTank(FLUID_CAPACITY) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };
  public final MachineEnergy energyStorage = createEnergy();
  private final NeighbourEnergy neighbours = new NeighbourEnergy(this);

  public PortBlockEntity(BlockPos pos, BlockState state) {
    super(MachinePorts.ENTITY.get(), pos, state);
  }

  private MachineEnergy createEnergy() {
    return switch (kind()) {
      case ENERGY_INPUT -> MachineEnergy.consumer(100000, this::setChanged);
      case ENERGY_OUTPUT -> MachineEnergy.generator(100000, this::setChanged);
      case SOLAR -> MachineEnergy.generator(10000, this::setChanged);
      case CREATIVE_ENERGY_INPUT ->
          new MachineEnergy(Integer.MAX_VALUE >> 4, 0, Integer.MAX_VALUE >> 4, this::setChanged) {
            @Override
            public int extractEnergy(int amount, boolean simulate) {
              return Math.max(0, Math.min(amount, capacity));
            }

            @Override
            public int consume(int amount, boolean simulate) {
              return extractEnergy(amount, simulate);
            }

            @Override
            public int getEnergyStored() {
              return capacity;
            }
          };
      default -> new MachineEnergy(100000, 0, 0, this::setChanged);
    };
  }

  public MachinePorts.Kind kind() {
    return ((PortBlock) getBlockState().getBlock()).kind();
  }

  public boolean hasItems() {
    return kind() == MachinePorts.Kind.ITEM_INPUT
        || kind() == MachinePorts.Kind.ITEM_OUTPUT
        || kind() == MachinePorts.Kind.DATA_BUS
        || hasFluid();
  }

  public boolean hasFluid() {
    return kind() == MachinePorts.Kind.FLUID_INPUT || kind() == MachinePorts.Kind.FLUID_OUTPUT;
  }

  public boolean hasEnergy() {
    return kind() == MachinePorts.Kind.ENERGY_INPUT
        || kind() == MachinePorts.Kind.CREATIVE_ENERGY_INPUT
        || kind() == MachinePorts.Kind.ENERGY_OUTPUT
        || kind() == MachinePorts.Kind.SOLAR;
  }

  public int injectGeneratedEnergy(int amount) {
    if (kind() != MachinePorts.Kind.ENERGY_OUTPUT || amount <= 0 || level == null) return 0;
    return energyStorage.generate(amount, false);
  }

  public IItemHandler automationInventory() {
    if (hasFluid()) return FluidContainers.automation(inventory);
    return new IItemHandler() {
      @Override
      public int getSlots() {
        return inventory.getSlots();
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
        return kind() == MachinePorts.Kind.ITEM_INPUT || kind() == MachinePorts.Kind.DATA_BUS;
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? inventory.insertItem(slot, stack, simulate) : stack;
      }

      @Override
      public ItemStack extractItem(int slot, int count, boolean simulate) {
        return kind() == MachinePorts.Kind.ITEM_OUTPUT || kind() == MachinePorts.Kind.DATA_BUS
            ? inventory.extractItem(slot, count, simulate)
            : ItemStack.EMPTY;
      }
    };
  }

  public IFluidHandler automationTank() {
    return new IFluidHandler() {
      @Override
      public int getTanks() {
        return 1;
      }

      @Override
      public FluidStack getFluidInTank(int tank) {
        return myTank.getFluid();
      }

      @Override
      public int getTankCapacity(int tank) {
        return myTank.getCapacity();
      }

      @Override
      public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
      }

      @Override
      public int fill(FluidStack stack, FluidAction action) {
        return kind() == MachinePorts.Kind.FLUID_INPUT ? myTank.fill(stack, action) : 0;
      }

      @Override
      public FluidStack drain(FluidStack stack, FluidAction action) {
        return kind() == MachinePorts.Kind.FLUID_OUTPUT
            ? myTank.drain(stack, action)
            : FluidStack.EMPTY;
      }

      @Override
      public FluidStack drain(int amount, FluidAction action) {
        return kind() == MachinePorts.Kind.FLUID_OUTPUT
            ? myTank.drain(amount, action)
            : FluidStack.EMPTY;
      }
    };
  }

  public void tick() {
    if (level == null || level.isClientSide) return;
    if (hasFluid()) FluidContainers.process(inventory, myTank);
    if (kind() == MachinePorts.Kind.SOLAR
        && level instanceof ServerLevel serverLevel
        && level.isDay()
        && level.canSeeSky(worldPosition.above())) {
      energyStorage.generate(SolarPower.perPanel(serverLevel, worldPosition), false);
    }
    if (kind() == MachinePorts.Kind.SOLAR
        || kind() == MachinePorts.Kind.ENERGY_OUTPUT
        || kind() == MachinePorts.Kind.CREATIVE_ENERGY_INPUT) neighbours.push(energyStorage);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    neighbours.load();
  }

  public List<BlockPos> menuPorts() {
    return List.of(worldPosition);
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new MachineMenu(id, inventory, worldPosition, menuPorts());
  }

  public void dropContents() {
    for (int slot = 0; slot < inventory.getSlots(); slot++) {
      ItemStack stack = inventory.getStackInSlot(slot).copy();
      inventory.setStackInSlot(slot, ItemStack.EMPTY);
      Block.popResource(level, worldPosition, stack);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("inventory", inventory.serializeNBT(registries));
    tag.put("fluid", myTank.writeToNBT(registries, new CompoundTag()));
    tag.put("energy", energyStorage.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    myTank.readFromNBT(registries, tag.getCompound("fluid"));
    if (tag.contains("energy")) energyStorage.deserializeNBT(registries, tag.get("energy"));
  }
}

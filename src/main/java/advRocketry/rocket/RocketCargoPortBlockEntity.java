// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.OrbitalRegistry;
import advRocketry.processing.FluidContainers;
import advRocketry.processing.PortBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Native inventory or fluid buffer for the four rocket cargo hatch roles. */
public final class RocketCargoPortBlockEntity extends BlockEntity
    implements MenuProvider, RocketInfrastructure {
  public final InfrastructureRedstone redstone = new InfrastructureRedstone();
  private final RocketInfrastructureLink link =
      new RocketInfrastructureLink(this, kind() == RocketCargoPortBlock.Kind.GUIDANCE ? 64 : 32);
  private int ejectOptions;
  private UUID guidanceArrival;
  private boolean guidanceProcessed;

  public boolean ejectOption(int index) {
    return (ejectOptions & 1 << index) != 0;
  }

  public void toggleEjectOption(int index) {
    if (index < 0 || index > 3) return;
    ejectOptions ^= 1 << index;
    setChanged();
  }

  private void ejectGuidance(RocketEntity rocket) {
    UUID arrived = rocket == null ? null : rocket.getUUID();
    if (arrived == null || !arrived.equals(guidanceArrival)) {
      if (guidanceArrival != null || arrived != null) setChanged();
      guidanceArrival = arrived;
      guidanceProcessed = false;
    }
    if (rocket == null || guidanceProcessed || !ejectOption(0)) return;
    ItemStack chip = rocket.guidanceChip();
    if (chip.isEmpty()) {
      guidanceProcessed = true;
      setChanged();
      return;
    }
    if (!(chip.is(OrbitalRegistry.SATELLITE_CHIP.get()) && ejectOption(1)
        || chip.is(OrbitalRegistry.PLANET_CHIP.get()) && ejectOption(2)
        || chip.is(OrbitalRegistry.STATION_CHIP.get()) && ejectOption(3))) return;
    for (Direction side : Direction.values()) {
      var target =
          level.getCapability(
              Capabilities.ItemHandler.BLOCK, worldPosition.relative(side), side.getOpposite());
      if (target == null
          || !ItemHandlerHelper.insertItemStacked(target, chip.copy(), true).isEmpty()) continue;
      if (ItemHandlerHelper.insertItemStacked(target, chip.copy(), false).isEmpty()) {
        rocket.setGuidanceChip(ItemStack.EMPTY);
        guidanceProcessed = true;
        setChanged();
        return;
      }
    }
  }

  @Override
  public boolean link(RocketEntity rocket) {
    if (!link.link(rocket)) return false;
    guidanceArrival = null;
    guidanceProcessed = false;
    return true;
  }

  @Override
  public boolean linkBuilder(RocketBlockEntity builder) {
    return link.linkBuilder(builder);
  }

  @Override
  public void unlink() {
    link.unlink();
    guidanceArrival = null;
    guidanceProcessed = false;
  }

  public final ItemStackHandler inventory =
      new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }
      };
  public final FluidTank tank =
      new FluidTank(PortBlockEntity.FLUID_CAPACITY) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };
  private final ItemStackHandler guidanceMirror = new ItemStackHandler(1);
  private final IFluidHandler automation =
      new IFluidHandler() {
        @Override
        public int getTanks() {
          return 1;
        }

        @Override
        public FluidStack getFluidInTank(int slot) {
          return tank.getFluid().copy();
        }

        @Override
        public int getTankCapacity(int slot) {
          return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int slot, FluidStack stack) {
          return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
          return kind() == RocketCargoPortBlock.Kind.FLUID_LOAD ? tank.fill(stack, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
          return kind() == RocketCargoPortBlock.Kind.FLUID_UNLOAD
              ? tank.drain(stack, action)
              : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
          return kind() == RocketCargoPortBlock.Kind.FLUID_UNLOAD
              ? tank.drain(amount, action)
              : FluidStack.EMPTY;
        }
      };

  public RocketCargoPortBlockEntity(BlockPos pos, BlockState state) {
    super(RocketRegistry.CARGO_PORT_ENTITY.get(), pos, state);
  }

  public RocketCargoPortBlock.Kind kind() {
    return ((RocketCargoPortBlock) getBlockState().getBlock()).kind;
  }

  public IFluidHandler automation() {
    return automation;
  }

  public IItemHandlerModifiable guidance() {
    return new IItemHandlerModifiable() {
      @Override
      public int getSlots() {
        return 1;
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
        if (level != null && level.isClientSide) return guidanceMirror.getStackInSlot(slot);
        RocketEntity rocket = link.rocket();
        return rocket == null ? ItemStack.EMPTY : rocket.guidanceChip();
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (level != null && level.isClientSide)
          return guidanceMirror.insertItem(slot, stack, simulate);
        RocketEntity rocket = link.rocket();
        if (rocket == null || !isItemValid(slot, stack) || !rocket.guidanceChip().isEmpty())
          return stack;
        if (!simulate && !rocket.setGuidanceChip(stack.copyWithCount(1))) return stack;
        return stack.copyWithCount(stack.getCount() - 1);
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (level != null && level.isClientSide)
          return guidanceMirror.extractItem(slot, amount, simulate);
        RocketEntity rocket = link.rocket();
        if (rocket == null || amount < 1) return ItemStack.EMPTY;
        ItemStack chip = rocket.guidanceChip();
        if (!simulate && !chip.isEmpty()) rocket.setGuidanceChip(ItemStack.EMPTY);
        return chip;
      }

      @Override
      public int getSlotLimit(int slot) {
        return 1;
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        if (slot != 0 || !GuidanceComputer.accepts(stack)) return false;
        if (level != null && level.isClientSide) return true;
        RocketEntity rocket = link.rocket();
        return rocket != null && rocket.guidanceCell() != null;
      }

      @Override
      public void setStackInSlot(int slot, ItemStack stack) {
        if (level != null && level.isClientSide) {
          guidanceMirror.setStackInSlot(slot, stack);
          return;
        }
        RocketEntity rocket = link.rocket();
        if (rocket != null && (stack.isEmpty() || isItemValid(slot, stack)))
          rocket.setGuidanceChip(stack);
      }
    };
  }

  public void tick() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    if (((RocketCargoPortBlock) getBlockState().getBlock()).fluid())
      FluidContainers.process(inventory, tank);
    RocketEntity rocket = link.rocket();
    if (kind() == RocketCargoPortBlock.Kind.GUIDANCE) ejectGuidance(rocket);
    if (rocket != null && redstone.permits(level, worldPosition))
      switch (kind()) {
        case ITEM_LOAD -> rocket.moveCargoItems(inventory, false);
        case ITEM_UNLOAD -> rocket.moveCargoItems(inventory, true);
        case FLUID_LOAD -> rocket.moveCargoFluid(tank, false);
        case FLUID_UNLOAD -> rocket.moveCargoFluid(tank, true);
        case GUIDANCE -> {}
      }
    BlockState state = getBlockState();
    boolean completed =
        redstone.output(
            rocket != null
                && (kind() == RocketCargoPortBlock.Kind.GUIDANCE
                    ? rocket.guidanceChip().isEmpty()
                    : rocket.cargoComplete(
                        ((RocketCargoPortBlock) getBlockState().getBlock()).fluid(),
                        kind() == RocketCargoPortBlock.Kind.ITEM_UNLOAD
                            || kind() == RocketCargoPortBlock.Kind.FLUID_UNLOAD)));
    if (state.getValue(RocketCargoPortBlock.POWERED) != completed)
      serverLevel.setBlock(
          worldPosition, state.setValue(RocketCargoPortBlock.POWERED, completed), 3);
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new RocketCargoPortMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("inventory", inventory.serializeNBT(registries));
    tag.put("fluid", tank.writeToNBT(registries, new CompoundTag()));
    redstone.save(tag);
    link.save(tag);
    tag.putInt("eject_options", ejectOptions);
    if (guidanceArrival != null) tag.putUUID("guidance_arrival", guidanceArrival);
    tag.putBoolean("guidance_processed", guidanceProcessed);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    tank.readFromNBT(registries, tag.getCompound("fluid"));
    redstone.load(tag);
    link.load(tag);
    ejectOptions = tag.getInt("eject_options") & 15;
    guidanceArrival = tag.hasUUID("guidance_arrival") ? tag.getUUID("guidance_arrival") : null;
    guidanceProcessed = tag.getBoolean("guidance_processed");
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Native 10,000 FE buffer and 40 FE/t burn cadence from TileCoalGenerator. */
public final class CoalGeneratorBlockEntity extends BlockEntity implements MenuProvider {
  private int energy;
  private int burnTime;
  public final ItemStackHandler fuel =
      new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return accepts(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }
      };
  public final IEnergyStorage output =
      new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
          return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
          int extracted = Math.min(Math.max(0, amount), energy);
          if (!simulate && extracted > 0) {
            energy -= extracted;
            setChanged();
          }
          return extracted;
        }

        @Override
        public int getEnergyStored() {
          return energy;
        }

        @Override
        public int getMaxEnergyStored() {
          return 10000;
        }

        @Override
        public boolean canExtract() {
          return true;
        }

        @Override
        public boolean canReceive() {
          return false;
        }
      };

  private final NeighbourEnergy neighbours = new NeighbourEnergy(this);

  public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
    super(MachinePorts.COAL_GENERATOR_ENTITY.get(), pos, state);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    neighbours.load();
  }

  public boolean accepts(ItemStack stack) {
    return !stack.isEmpty() && stack.getBurnTime(RecipeType.SMELTING) > 0;
  }

  public int energyStored() {
    return energy;
  }

  public int burnTime() {
    return burnTime;
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.adv_rocketry.coal_generator");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new CoalGeneratorMenu(id, inventory, worldPosition);
  }

  public void tick() {
    if (level == null || level.isClientSide) return;
    if (burnTime == 0 && energy <= 9960 && !fuel.getStackInSlot(0).isEmpty()) {
      ItemStack stack = fuel.getStackInSlot(0);
      int duration = stack.getBurnTime(RecipeType.SMELTING);
      if (duration > 0) {
        burnTime = duration;
        ItemStack remainder = stack.getCraftingRemainingItem();
        fuel.extractItem(0, 1, false);
        if (!remainder.isEmpty()) {
          if (fuel.getStackInSlot(0).isEmpty()) fuel.setStackInSlot(0, remainder);
          else Block.popResource(level, worldPosition.above(), remainder);
        }
      }
    }
    if (burnTime > 0 && energy <= 9960) {
      burnTime--;
      energy += 40;
      setChanged();
    }
    neighbours.push(output);
    if (level.getGameTime() % 20 == 0)
      level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("energy", energy);
    tag.putInt("burn_time", burnTime);
    tag.put("fuel", fuel.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    energy = Math.clamp(tag.getInt("energy"), 0, 10000);
    burnTime = Math.max(0, tag.getInt("burn_time"));
    if (tag.contains("fuel")) fuel.deserializeNBT(registries, tag.getCompound("fuel"));
  }
}

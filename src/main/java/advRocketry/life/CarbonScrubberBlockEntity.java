// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** One damageable cartridge, with original fifteen-step comparator readout. */
public final class CarbonScrubberBlockEntity extends BlockEntity {
  public final ItemStackHandler cartridge =
      new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
          if (level != null)
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }

        @Override
        public int getSlotLimit(int slot) {
          return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return stack.is(ProcessingRegistry.PART_ITEMS.get("carbon_scrubber_cartridge").get());
        }
      };

  public CarbonScrubberBlockEntity(BlockPos pos, BlockState state) {
    super(LifeSupportRegistry.SCRUBBER_ENTITY.get(), pos, state);
  }

  public boolean canScrub() {
    if (!AdvancedRocketryConfig.scrubberRequiresCartridge()) return true;
    ItemStack stack = cartridge.getStackInSlot(0);
    return stack.is(ProcessingRegistry.PART_ITEMS.get("carbon_scrubber_cartridge").get())
        && stack.getDamageValue() < stack.getMaxDamage();
  }

  public boolean useCharge() {
    if (!AdvancedRocketryConfig.scrubberRequiresCartridge()) return true;
    if (!canScrub()) return false;
    ItemStack stack = cartridge.getStackInSlot(0);
    stack.setDamageValue(stack.getDamageValue() + 1);
    setChanged();
    if (level != null)
      level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    return true;
  }

  public int comparatorOutput() {
    ItemStack stack = cartridge.getStackInSlot(0);
    if (stack.isEmpty()) return 0;
    return Math.max(0, (stack.getMaxDamage() - stack.getDamageValue() + 2184) / 2185);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("cartridge", cartridge.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    cartridge.deserializeNBT(registries, tag.getCompound("cartridge"));
  }
}

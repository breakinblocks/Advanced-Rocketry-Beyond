// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Persistent suit slot; component slots are embedded in the stored armor item. */
public final class SuitWorkstationBlockEntity extends BlockEntity implements MenuProvider {
  public final ItemStackHandler suit =
      new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
          return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return stack.getItem() instanceof SpaceSuitItem;
        }
      };

  public SuitWorkstationBlockEntity(BlockPos pos, BlockState state) {
    super(LifeSupportRegistry.SUIT_WORKSTATION_ENTITY.get(), pos, state);
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new SuitWorkstationMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("suit", suit.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    suit.deserializeNBT(registries, tag.getCompound("suit"));
  }
}

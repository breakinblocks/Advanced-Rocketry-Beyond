// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Native landing-pad name and one-slot linker persistence. */
public final class LandingPadBlockEntity extends BlockEntity {
  public String name = "";
  public final ItemStackHandler linker =
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
          return stack.getItem() instanceof LinkerItem;
        }
      };

  public LandingPadBlockEntity(BlockPos pos, BlockState state) {
    super(OrbitalRegistry.LANDING_PAD_ENTITY.get(), pos, state);
  }

  public void rename(String name) {
    this.name = name.substring(0, Math.min(32, name.length()));
    setChanged();
    LandingPadLogic.register(this);
  }

  public void tick() {
    if (level != null && level.getGameTime() % 20 == 0) LandingPadLogic.register(this);
  }

  public void unregister() {
    LandingPadLogic.remove(this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putString("name", name);
    tag.put("linker", linker.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    name = tag.getString("name");
    linker.deserializeNBT(registries, tag.getCompound("linker"));
  }
}

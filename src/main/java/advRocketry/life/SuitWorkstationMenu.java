// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.util.ModMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/** Suit slot and six live module slots backed by the armor item's components. */
public final class SuitWorkstationMenu extends ModMenu {
  private static final int CONTAINER_SLOTS = 7;
  private final SuitWorkstationBlockEntity entity;

  public SuitWorkstationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        new SuitWorkstationBlockEntity(
            buffer.readBlockPos(), LifeSupportRegistry.SUIT_WORKSTATION.get().defaultBlockState()));
  }

  public SuitWorkstationMenu(int id, Inventory inventory, SuitWorkstationBlockEntity entity) {
    super(LifeSupportRegistry.SUIT_WORKSTATION_MENU.get(), id);
    this.entity = entity;
    addSlot(new SlotItemHandler(entity.suit, 0, 8, 30));
    IItemHandlerModifiable modules = new Modules(entity);
    for (int slot = 0; slot < 6; slot++)
      addSlot(new SlotItemHandler(modules, slot, 35 + slot * 18, 30));
    addPlayerInventory(inventory, 8, 76);
  }

  @Override
  public boolean stillValid(Player player) {
    return entity != null
        && !entity.isRemoved()
        && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (index < CONTAINER_SLOTS) return quickMove(player, index, CONTAINER_SLOTS);
    boolean suit =
        index < slots.size() && slots.get(index).getItem().getItem() instanceof SpaceSuitItem;
    return quickMove(
        player, index, suit ? 0 : 1, suit ? 1 : CONTAINER_SLOTS, CONTAINER_SLOTS, slots.size());
  }

  public static IItemHandlerModifiable packedInventory(SuitWorkstationBlockEntity entity) {
    return new CombinedInvWrapper(entity.suit, new Modules(entity));
  }

  private record Modules(SuitWorkstationBlockEntity entity) implements IItemHandlerModifiable {
    private ItemStackHandler active() {
      ItemStack armor = entity.suit.getStackInSlot(0);
      return armor.getItem() instanceof SpaceSuitItem suit ? suit.inventory(armor) : null;
    }

    @Override
    public int getSlots() {
      return 6;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
      ItemStackHandler contents = active();
      return contents != null && slot < contents.getSlots()
          ? contents.getStackInSlot(slot)
          : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      ItemStackHandler contents = active();
      if (contents == null || slot >= contents.getSlots()) return stack;
      ItemStack remainder = contents.insertItem(slot, stack, simulate);
      if (!simulate && remainder.getCount() != stack.getCount()) entity.setChanged();
      return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      ItemStackHandler contents = active();
      if (contents == null || slot >= contents.getSlots()) return ItemStack.EMPTY;
      ItemStack removed = contents.extractItem(slot, amount, simulate);
      if (!simulate && !removed.isEmpty()) entity.setChanged();
      return removed;
    }

    @Override
    public int getSlotLimit(int slot) {
      return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      ItemStackHandler contents = active();
      return contents != null && slot < contents.getSlots() && contents.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
      ItemStackHandler contents = active();
      if (contents == null
          || slot >= contents.getSlots()
          || !stack.isEmpty() && !contents.isItemValid(slot, stack)) return;
      contents.setStackInSlot(slot, stack);
      entity.setChanged();
    }
  }
}

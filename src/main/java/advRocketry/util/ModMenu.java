package advRocketry.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ModMenu extends AbstractContainerMenu {
  protected ModMenu(MenuType<?> type, int id) {
    super(type, id);
  }

  protected int addPlayerInventory(Inventory inventory, int x, int y) {
    int first = slots.size();
    for (int row = 0; row < 3; row++)
      for (int column = 0; column < 9; column++)
        addSlot(playerSlot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
    for (int column = 0; column < 9; column++)
      addSlot(playerSlot(inventory, column, x + column * 18, y + 58));
    return first;
  }

  protected Slot playerSlot(Inventory inventory, int index, int x, int y) {
    return new Slot(inventory, index, x, y);
  }

  protected ItemStack quickMove(Player player, int index, int containerSlots) {
    return quickMove(player, index, 0, containerSlots, containerSlots, slots.size());
  }

  protected ItemStack quickMove(
      Player player,
      int index,
      int containerStart,
      int containerEnd,
      int playerStart,
      int playerEnd) {
    if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
    ItemStack stack = slot.getItem();
    ItemStack copy = stack.copy();
    boolean fromContainer = index >= containerStart && index < containerEnd;
    boolean moved =
        fromContainer
            ? moveItemStackTo(stack, playerStart, playerEnd, true)
            : moveItemStackTo(stack, containerStart, containerEnd, false);
    if (!moved) return ItemStack.EMPTY;
    if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
    else slot.setChanged();
    if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
    slot.onTake(player, stack);
    return copy;
  }
}

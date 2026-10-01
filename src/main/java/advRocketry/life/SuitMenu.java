// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.util.ModMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class SuitMenu extends ModMenu {
  private final Inventory owner;
  private final int armorSlot;
  private final ItemStack armor;
  public final int moduleCount;

  public SuitMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, buffer.readVarInt());
  }

  public SuitMenu(int id, Inventory inventory, int armorSlot) {
    super(LifeSupportRegistry.SUIT_MENU.get(), id);
    owner = inventory;
    this.armorSlot = armorSlot;
    armor = inventory.getItem(armorSlot);
    ItemStackHandler modules =
        armor.getItem() instanceof SpaceSuitItem suit
            ? suit.inventory(armor)
            : new ItemStackHandler(0);
    moduleCount = modules.getSlots();
    for (int slot = 0; slot < moduleCount; slot++)
      addSlot(
          new SlotItemHandler(modules, slot, 8 + slot * 18, 30) {
            @Override
            public boolean mayPickup(Player player) {
              return !armor.isEmpty() && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
              return !armor.isEmpty() && super.mayPlace(stack);
            }
          });
    addPlayerInventory(inventory, 8, 76);
  }

  @Override
  protected Slot playerSlot(Inventory inventory, int index, int x, int y) {
    return new Slot(inventory, index, x, y) {
      @Override
      public boolean mayPickup(Player player) {
        return getContainerSlot() != armorSlot;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
        return getContainerSlot() != armorSlot;
      }
    };
  }

  @Override
  public boolean stillValid(Player player) {
    return owner.player == player && !armor.isEmpty() && owner.getItem(armorSlot) == armor;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return quickMove(player, index, moduleCount);
  }
}

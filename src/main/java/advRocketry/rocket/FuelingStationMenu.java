// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.util.ModMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class FuelingStationMenu extends ModMenu {
  private final FuelingStationBlockEntity entity;
  private final ContainerData data;

  public FuelingStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        new FuelingStationBlockEntity(
            buffer.readBlockPos(), RocketRegistry.FUELING_STATION.get().defaultBlockState()));
  }

  public FuelingStationMenu(int id, Inventory inventory, FuelingStationBlockEntity entity) {
    super(RocketRegistry.FUELING_MENU.get(), id);
    this.entity = entity;
    if (entity.getLevel() == null) entity.setLevel(inventory.player.level());
    data =
        entity.getLevel().isClientSide
            ? new SimpleContainerData(3)
            : new ContainerData() {
              @Override
              public int get(int index) {
                return switch (index) {
                  case 0 -> entity.tank.getFluidAmount();
                  case 1 -> entity.energy.getEnergyStored();
                  default -> entity.outputMode();
                };
              }

              @Override
              public void set(int index, int value) {}

              @Override
              public int getCount() {
                return 3;
              }
            };
    addDataSlots(data);
    for (int slot = 0; slot < 2; slot++)
      addSlot(
          new SlotItemHandler(entity.inventory, slot, 120 + slot * 28, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
              return getSlotIndex() == 0 && entity.acceptsContainer(stack);
            }
          });
    addPlayerInventory(inventory, 8, 92);
  }

  public int fluidAmount() {
    return data.get(0);
  }

  public int energy() {
    return data.get(1);
  }

  public int outputMode() {
    return data.get(2);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (id != 0 || !stillValid(player)) return false;
    entity.cycleOutputMode();
    broadcastChanges();
    return true;
  }

  @Override
  public boolean stillValid(Player player) {
    return !entity.isRemoved() && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return index < 2
        ? quickMove(player, index, 2)
        : quickMove(player, index, 0, 1, 2, slots.size());
  }
}

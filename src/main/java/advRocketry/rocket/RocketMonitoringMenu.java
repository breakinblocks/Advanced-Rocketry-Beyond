// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.util.SplitIntData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class RocketMonitoringMenu extends AbstractContainerMenu {
  private final RocketMonitoringBlockEntity entity;
  private final SplitIntData data;

  public RocketMonitoringMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        new RocketMonitoringBlockEntity(
            buffer.readBlockPos(), RocketRegistry.MONITORING_STATION.get().defaultBlockState()));
  }

  public RocketMonitoringMenu(int id, Inventory inventory, RocketMonitoringBlockEntity entity) {
    super(RocketRegistry.MONITORING_MENU.get(), id);
    this.entity = entity;
    if (entity.getLevel() == null) entity.setLevel(inventory.player.level());
    data =
        entity.getLevel().isClientSide
            ? SplitIntData.client(10)
            : SplitIntData.server(10, entity::value);
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!stillValid(player)) return false;
    if (id == 0) return entity.launch();
    if (id != 1) return false;
    entity.cycleMode();
    broadcastChanges();
    return true;
  }

  @Override
  public boolean stillValid(Player player) {
    return !entity.isRemoved() && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slot) {
    return ItemStack.EMPTY;
  }
}

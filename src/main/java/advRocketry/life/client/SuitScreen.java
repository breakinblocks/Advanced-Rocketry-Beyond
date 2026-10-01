// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.client.ui.ModContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SuitScreen<T extends AbstractContainerMenu> extends ModContainerScreen<T> {
  public SuitScreen(T menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageHeight = 158;
    inventoryLabelY = 64;
  }
}

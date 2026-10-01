// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import java.util.Arrays;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Synchronizes a bounded 16 by 16 orbital ore-density map to its handheld screen. */
public final class OreScanMenu extends AbstractContainerMenu {
  public static final int SIZE = 256;
  private final int[] pixels;

  public OreScanMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, buffer.readVarIntArray(SIZE));
  }

  public OreScanMenu(int id, int[] values) {
    super(OrbitalRegistry.ORE_SCAN_MENU.get(), id);
    pixels = Arrays.copyOf(values, SIZE);
  }

  public int pixel(int x, int z) {
    return pixels[z * 16 + x];
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}

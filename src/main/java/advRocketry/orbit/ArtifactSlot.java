// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** One window position over the observatory's artifact store, shifted by the menu's scroll. */
final class ArtifactSlot extends Slot {
  private static final Container NONE = new SimpleContainer(0);
  private final OrbitalMenu menu;
  private final int window;

  ArtifactSlot(OrbitalMenu menu, int window, int x, int y) {
    super(NONE, window, x, y);
    this.menu = menu;
    this.window = window;
  }

  private int index() {
    return menu.artifactOffset() + window;
  }

  private boolean inRange() {
    return index() < menu.entity.artifacts.getSlots();
  }

  @Override
  public ItemStack getItem() {
    return inRange() ? menu.entity.artifacts.getStackInSlot(index()) : ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack stack) {
    if (inRange()) menu.entity.artifacts.setStackInSlot(index(), stack);
    setChanged();
  }

  @Override
  public void setChanged() {
    menu.entity.setChanged();
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public ItemStack remove(int amount) {
    return inRange() ? menu.entity.artifacts.extractItem(index(), amount, false) : ItemStack.EMPTY;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return isActive() && inRange() && menu.entity.artifacts.isItemValid(index(), stack);
  }

  @Override
  public boolean isActive() {
    return window < menu.visibleArtifactSlots();
  }
}

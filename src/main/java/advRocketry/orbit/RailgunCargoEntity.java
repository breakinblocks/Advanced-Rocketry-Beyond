// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Cosmetic rising cargo from the original railgun; the inventory transfer occurs immediately. */
public final class RailgunCargoEntity extends Entity {
  private static final EntityDataAccessor<ItemStack> DISPLAY =
      SynchedEntityData.defineId(RailgunCargoEntity.class, EntityDataSerializers.ITEM_STACK);

  public RailgunCargoEntity(EntityType<? extends RailgunCargoEntity> type, Level level) {
    super(type, level);
    noPhysics = true;
  }

  public void setDisplay(ItemStack stack) {
    entityData.set(DISPLAY, stack.copy());
  }

  public ItemStack display() {
    return entityData.get(DISPLAY);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(DISPLAY, ItemStack.EMPTY);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  @Override
  public boolean isPickable() {
    return false;
  }

  @Override
  public void tick() {
    super.tick();
    if (!level().isClientSide && (tickCount >= 200 || display().isEmpty())) {
      discard();
      return;
    }
    setPos(getX(), getY() + 2, getZ());
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Invisible mount for the original placeable rocket seat. */
public final class SeatEntity extends Entity {
  public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
    super(type, level);
    noPhysics = true;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected boolean canAddPassenger(Entity passenger) {
    return getPassengers().isEmpty();
  }

  @Override
  protected void positionRider(Entity passenger, MoveFunction callback) {
    callback.accept(passenger, getX(), getY() + .2, getZ());
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    if (!level().isClientSide && getPassengers().isEmpty()) player.startRiding(this);
    return InteractionResult.sidedSuccess(level().isClientSide);
  }

  @Override
  public boolean isPickable() {
    return false;
  }

  @Override
  public boolean isInvisible() {
    return true;
  }

  @Override
  public void tick() {
    super.tick();
    if (!level().isClientSide
        && (getPassengers().isEmpty()
            || !level()
                .getBlockState(BlockPos.containing(position()))
                .is(RocketRegistry.PARTS.get("seat").get()))) discard();
  }
}

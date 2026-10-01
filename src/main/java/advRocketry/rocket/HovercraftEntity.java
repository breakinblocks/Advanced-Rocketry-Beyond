// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Original steerable single-seat hovercraft with its 0.05 acceleration and height ceiling. */
public final class HovercraftEntity extends Entity {
  private static final double ACCELERATION = .05;
  private float forwardInput;
  private float turnInput;
  private boolean ascending;
  private boolean descending;
  private long lastInputTick = Long.MIN_VALUE;

  public HovercraftEntity(EntityType<? extends HovercraftEntity> type, Level level) {
    super(type, level);
  }

  public void control(Player rider, float forward, float turn, boolean up, boolean down) {
    if (getFirstPassenger() != rider || rider.level() != level()) return;
    if (!Float.isFinite(forward) || !Float.isFinite(turn)) return;
    forwardInput = Math.max(-1, Math.min(1, forward));
    turnInput = Math.max(-1, Math.min(1, turn));
    ascending = up;
    descending = down;
    lastInputTick = level().getGameTime();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  @Override
  protected boolean canAddPassenger(Entity passenger) {
    return getPassengers().isEmpty();
  }

  @Override
  protected void positionRider(Entity passenger, MoveFunction callback) {
    callback.accept(passenger, getX(), getY() + .5, getZ());
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    if (!level().isClientSide && getPassengers().isEmpty()) player.startRiding(this);
    return InteractionResult.sidedSuccess(level().isClientSide);
  }

  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (level().isClientSide
        || isRemoved()
        || !(source.getEntity() instanceof Player player)
        || getPassengers().contains(player)) return false;
    spawnAtLocation(new ItemStack(HovercraftRegistry.ITEM.get()));
    discard();
    return true;
  }

  @Override
  public void tick() {
    super.tick();
    if (level().isClientSide) return;
    boolean controlled =
        getFirstPassenger() instanceof Player && level().getGameTime() - lastInputTick <= 3;
    float forward = controlled ? forwardInput : 0;
    float turn = controlled ? turnInput : 0;
    boolean up = controlled && ascending;
    boolean down = !controlled || descending;
    setYRot(getYRot() + turn * 5);
    double angle = Math.toRadians(getYRot());
    Vec3 motion = getDeltaMovement();
    double x = (motion.x - Math.sin(angle) * forward * ACCELERATION) * .9;
    double z = (motion.z + Math.cos(angle) * forward * ACCELERATION) * .9;
    double y = (motion.y + (up ? ACCELERATION : 0) - (down ? ACCELERATION : 0)) * .9;
    double horizontal = Math.hypot(x, z);
    if (horizontal > .75) {
      x *= .75 / horizontal;
      z *= .75 / horizontal;
    }
    y = Math.max(-.1, Math.min(.1, y));
    if (getY() > 275) y = Math.min(0, y);
    else if (getY() > 250 && y > 0) y *= .1;
    setDeltaMovement(x, y, z);
    move(MoverType.SELF, getDeltaMovement());
    for (Entity passenger : getPassengers()) passenger.fallDistance = 0;
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}
}

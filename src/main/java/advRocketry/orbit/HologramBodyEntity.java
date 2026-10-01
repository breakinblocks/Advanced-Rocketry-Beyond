// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Transient planet, star and back-button interaction roles from the original holographic UI. */
public final class HologramBodyEntity extends Entity {
  private static final EntityDataAccessor<Float> RADIUS =
      SynchedEntityData.defineId(HologramBodyEntity.class, EntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Integer> COLOR =
      SynchedEntityData.defineId(HologramBodyEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> SELECTED =
      SynchedEntityData.defineId(HologramBodyEntity.class, EntityDataSerializers.BOOLEAN);
  private OrbitalBlockEntity owner;
  private HolographicSelectorLogic.Kind kind;
  private int bodyId;

  public HologramBodyEntity(EntityType<? extends HologramBodyEntity> type, Level level) {
    super(type, level);
    noPhysics = true;
    setInvulnerable(true);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(RADIUS, .15f);
    builder.define(COLOR, 0xd07feaff);
    builder.define(SELECTED, false);
  }

  boolean matches(HolographicSelectorLogic.Body body) {
    return kind == body.kind() && bodyId == body.id();
  }

  void update(OrbitalBlockEntity selector, HolographicSelectorLogic.Body body) {
    owner = selector;
    kind = body.kind();
    bodyId = body.id();
    entityData.set(RADIUS, body.radius());
    entityData.set(COLOR, body.color());
    entityData.set(SELECTED, body.selected());
    setCustomName(body.name());
    Vec3 center = Vec3.atLowerCornerOf(selector.getBlockPos()).add(.5, 1.05, .5).add(body.offset());
    setPos(center);
    updateBounds();
  }

  public float radius() {
    return entityData.get(RADIUS);
  }

  public int color() {
    return entityData.get(COLOR);
  }

  public boolean selected() {
    return entityData.get(SELECTED);
  }

  private void updateBounds() {
    double radius = Math.max(.1, radius());
    setBoundingBox(
        new AABB(
            getX() - radius,
            getY() - radius,
            getZ() - radius,
            getX() + radius,
            getY() + radius,
            getZ() + radius));
  }

  private boolean validOwner() {
    return owner != null
        && !owner.isRemoved()
        && level().hasChunkAt(owner.getBlockPos())
        && level().getBlockEntity(owner.getBlockPos()) == owner
        && HolographicSelectorLogic.enabled(owner);
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    if (player.isSpectator() || isRemoved()) return InteractionResult.PASS;
    if (level().isClientSide) return InteractionResult.SUCCESS;
    if (!validOwner() || !player.canInteractWithEntity(this, 1)) return InteractionResult.PASS;
    return HolographicSelectorLogic.select(owner, kind, bodyId)
        ? InteractionResult.CONSUME
        : InteractionResult.PASS;
  }

  @Override
  public boolean isPickable() {
    return !isRemoved();
  }

  @Override
  public void tick() {
    super.tick();
    updateBounds();
    if (!level().isClientSide && !validOwner()) discard();
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}
}

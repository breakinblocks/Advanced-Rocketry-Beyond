// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.util.TicketedTransit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Rideable tether car with the original ascent/descent speeds and interdimensional handoff. */
public final class ElevatorCapsule extends Entity {
  private static final TicketedTransit<ElevatorCapsule> TRANSIT =
      new TicketedTransit<>("advanced_rocketry_elevator", capsule -> capsule.phase != 0);
  private static final EntityDataAccessor<Integer> MOTION =
      SynchedEntityData.defineId(ElevatorCapsule.class, EntityDataSerializers.INT);
  private String targetDimension = "";
  private long targetPosition;
  private long sourcePosition;
  private int phase;
  private int idleTicks;
  private final TicketedTransit<ElevatorCapsule>.Tracker transit = TRANSIT.track(this);

  public ElevatorCapsule(EntityType<? extends ElevatorCapsule> type, Level level) {
    super(type, level);
    noPhysics = true;
  }

  public void depart(OrbitalBlockEntity target) {
    targetDimension = target.getLevel().dimension().location().toString();
    targetPosition = target.getBlockPos().asLong();
    sourcePosition = target.elevatorPosition;
    phase = 1;
    entityData.set(MOTION, phase);
    transit.update();
  }

  public boolean inMotion() {
    return entityData.get(MOTION) != 0;
  }

  @Override
  public void onAddedToLevel() {
    super.onAddedToLevel();
    transit.added();
  }

  @Override
  public void onRemovedFromLevel() {
    transit.removed();
    super.onRemovedFromLevel();
  }

  public static void tickUnscheduled(MinecraftServer server) {
    TRANSIT.tickUnscheduled(server);
  }

  public static void serverStopped(ServerStoppedEvent event) {
    TRANSIT.clear();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(MOTION, 0);
  }

  @Override
  protected boolean canAddPassenger(Entity passenger) {
    return getPassengers().isEmpty();
  }

  @Override
  protected void positionRider(Entity passenger, MoveFunction callback) {
    callback.accept(passenger, getX(), getY() + .25, getZ());
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  public void tick() {
    super.tick();
    if (!(level() instanceof ServerLevel level)) return;
    transit.ticked(level);
    if (phase == 0) {
      if (++idleTicks > 100 && getPassengers().isEmpty()) discard();
      return;
    }
    boolean space = level.dimension() == OrbitalRegistry.SPACE_DIMENSION;
    double speed = getY() > 255 ? 2.85 : .85;
    if (phase == 1) {
      setPos(getX(), getY() + (space ? -speed : speed), getZ());
      if (space ? getY() <= 15 : getY() >= 1000) handoff(level);
    } else {
      int landingY = BlockPos.of(targetPosition).getY() + 2;
      double next = getY() + (space ? speed : -speed);
      if (space ? next >= landingY : next <= landingY) {
        setPos(getX(), landingY, getZ());
        phase = 0;
        entityData.set(MOTION, phase);
        transit.update();
        for (Entity passenger : getPassengers()) passenger.stopRiding();
      } else setPos(getX(), next, getZ());
    }
    transit.update();
  }

  private void handoff(ServerLevel source) {
    ResourceLocation id = ResourceLocation.tryParse(targetDimension);
    ServerLevel destination =
        id == null
            ? null
            : source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
    BlockPos anchor = BlockPos.of(targetPosition);
    if (destination != null) destination.getChunkAt(anchor);
    if (destination == null
        || !(destination.getBlockEntity(anchor) instanceof OrbitalBlockEntity controller)
        || !controller.spaceElevator()
        || !SpaceElevatorLogic.complete(controller)) {
      BlockPos origin = BlockPos.of(sourcePosition).above(2);
      for (Entity passenger : getPassengers()) {
        passenger.stopRiding();
        passenger.teleportTo(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
      }
      discard();
      return;
    }
    ElevatorCapsule arriving = OrbitalRegistry.ELEVATOR_CAPSULE.get().create(destination);
    if (arriving == null) return;
    double startY = destination.dimension() == OrbitalRegistry.SPACE_DIMENSION ? 10 : 1000;
    arriving.setPos(anchor.getX() + .5, startY, anchor.getZ() + .5);
    arriving.setYRot(
        SpaceElevatorLogic.yaw(controller.getBlockState().getValue(HorizontalOrbitalBlock.FACING)));
    arriving.targetDimension = targetDimension;
    arriving.targetPosition = targetPosition;
    arriving.sourcePosition = sourcePosition;
    arriving.phase = 2;
    arriving.entityData.set(MOTION, arriving.phase);
    if (!destination.addFreshEntity(arriving)) return;
    arriving.transit.update();
    for (Entity passenger : getPassengers()) {
      passenger.stopRiding();
      if (!(passenger instanceof ServerPlayer player)) continue;
      Entity moved =
          player.changeDimension(
              new DimensionTransition(
                  destination,
                  new Vec3(arriving.getX(), startY + .25, arriving.getZ()),
                  Vec3.ZERO,
                  player.getYRot(),
                  player.getXRot(),
                  DimensionTransition.PLACE_PORTAL_TICKET));
      if (moved != null) moved.startRiding(arriving, true);
    }
    discard();
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {
    tag.putString("target_dimension", targetDimension);
    tag.putLong("target_position", targetPosition);
    tag.putLong("source_position", sourcePosition);
    tag.putInt("phase", phase);
    tag.putInt("idle_ticks", idleTicks);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {
    targetDimension = tag.getString("target_dimension");
    targetPosition = tag.getLong("target_position");
    sourcePosition = tag.getLong("source_position");
    phase = tag.getInt("phase");
    entityData.set(MOTION, phase);
    idleTicks = tag.getInt("idle_ticks");
  }
}

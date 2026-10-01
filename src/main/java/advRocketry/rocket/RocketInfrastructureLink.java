// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Saved rocket or assembler association, detached from a rocket at liftoff. */
public final class RocketInfrastructureLink {
  private final BlockEntity owner;
  private final int range;
  private UUID rocketId;
  private BlockPos builderPos;

  public RocketInfrastructureLink(BlockEntity owner, int range) {
    this.owner = owner;
    this.range = range;
  }

  public boolean link(RocketEntity rocket) {
    double radius = range + Math.max(rocket.structure().width, rocket.structure().depth);
    double dx = rocket.getX() - owner.getBlockPos().getX();
    double dz = rocket.getZ() - owner.getBlockPos().getZ();
    if (rocket.level() != owner.getLevel()
        || rocket.flight() != 0
        || dx * dx + dz * dz >= radius * radius) return false;
    rocketId = rocket.getUUID();
    builderPos = null;
    owner.setChanged();
    return true;
  }

  public boolean linkBuilder(RocketBlockEntity builder) {
    if (builder.getLevel() != owner.getLevel()
        || builder.kind() != RocketPartBlock.Kind.BUILDER
            && builder.kind() != RocketPartBlock.Kind.DEPLOYABLE_BUILDER
        || builder.getBlockPos().distSqr(owner.getBlockPos()) > (double) range * range)
      return false;
    builderPos = builder.getBlockPos().immutable();
    rocketId = null;
    owner.setChanged();
    return true;
  }

  public void unlink() {
    rocketId = null;
    builderPos = null;
    owner.setChanged();
  }

  public UUID rocketId() {
    return rocketId;
  }

  public void follow(RocketEntity rocket) {
    if (rocket.getUUID().equals(rocketId)) return;
    rocketId = rocket.getUUID();
    owner.setChanged();
  }

  public RocketEntity rocket() {
    if (!(owner.getLevel() instanceof ServerLevel level)) return null;
    if (rocketId != null && level.getEntity(rocketId) instanceof RocketEntity rocket) {
      if (!rocket.isRemoved() && rocket.flight() == 0) return rocket;
      rocketId = null;
      owner.setChanged();
    }
    return builderRocket();
  }

  public RocketEntity builderRocket() {
    if (builderPos == null
        || !(owner.getLevel() instanceof ServerLevel level)
        || !(level.getBlockEntity(builderPos) instanceof RocketBlockEntity)) return null;
    RocketEntity found =
        builderRockets(level, builderPos, owner.getBlockPos().getCenter()).stream()
            .findFirst()
            .orElse(null);
    if (found != null) follow(found);
    return found;
  }

  public static List<RocketEntity> builderRockets(Level level, BlockPos builderPos, Vec3 near) {
    return level
        .getEntitiesOfClass(
            RocketEntity.class,
            new AABB(builderPos).inflate(64),
            candidate -> candidate.flight() == 0 && candidate.belongsToBuilder(builderPos))
        .stream()
        .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(near)))
        .toList();
  }

  public void save(CompoundTag tag) {
    if (rocketId != null) tag.putUUID("linked_rocket", rocketId);
    if (builderPos != null) tag.putLong("linked_builder", builderPos.asLong());
  }

  public void load(CompoundTag tag) {
    String rocketKey = tag.hasUUID("linked_rocket") ? "linked_rocket" : "rocket";
    String builderKey = tag.contains("linked_builder") ? "linked_builder" : "builder";
    rocketId = tag.hasUUID(rocketKey) ? tag.getUUID(rocketKey) : null;
    builderPos =
        tag.contains(builderKey, Tag.TAG_LONG) ? BlockPos.of(tag.getLong(builderKey)) : null;
  }
}

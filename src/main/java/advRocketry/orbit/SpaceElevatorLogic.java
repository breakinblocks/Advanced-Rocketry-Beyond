// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/** Original 9-by-10 tether frame, paired geostationary anchors and 50,000 FE transit. */
public final class SpaceElevatorLogic {
  private static final int TRAVEL_ENERGY = 50000;

  private SpaceElevatorLogic() {}

  public static boolean complete(OrbitalBlockEntity controller) {
    return controller.spaceElevator() && PlacedMultiblock.complete(controller, "space_elevator");
  }

  public static float yaw(Direction facing) {
    return switch (facing) {
      case EAST -> 180;
      case SOUTH -> 90;
      case NORTH -> 270;
      default -> 0;
    };
  }

  public static void collectEnergy(OrbitalBlockEntity controller) {
    if (!complete(controller)) return;
    for (PortBlockEntity port :
        PlacedMultiblock.of(controller, "space_elevator").entities('P', PortBlockEntity.class))
      controller.pullFrom(port, 1000);
  }

  public static boolean link(OrbitalBlockEntity first, OrbitalBlockEntity second) {
    if (!(first.getLevel() instanceof ServerLevel firstLevel)
        || !(second.getLevel() instanceof ServerLevel secondLevel)
        || firstLevel.dimension().equals(secondLevel.dimension())
        || !first.elevatorDimension.isEmpty()
        || !second.elevatorDimension.isEmpty()) return false;
    GalaxyData galaxy = GalaxyData.get(firstLevel.getServer());
    OrbitalBlockEntity space =
        StationLogic.inSpace(galaxy, firstLevel)
            ? first
            : StationLogic.inSpace(galaxy, secondLevel) ? second : null;
    OrbitalBlockEntity ground = space == first ? second : first;
    if (space == null || !complete(first) || !complete(second)) return false;
    Station station = StationLogic.at(galaxy, space.getLevel(), space.getBlockPos());
    if (station == null
        || station.anchored
        || !tetherSafe(station)
        || station.orbitPlanet != galaxy.planet((ServerLevel) ground.getLevel()).id) return false;
    first.elevatorDimension = secondLevel.dimension().location().toString();
    first.elevatorPosition = second.getBlockPos().asLong();
    second.elevatorDimension = firstLevel.dimension().location().toString();
    second.elevatorPosition = first.getBlockPos().asLong();
    first.setChanged();
    second.setChanged();
    sync(first);
    sync(second);
    anchor(galaxy, station, true);
    return true;
  }

  static boolean tetherSafe(Station station) {
    float altitude = station.altitude;
    if (!(altitude >= 177 && altitude <= 181)) return false;
    for (int axis = 0; axis < 3; axis++) {
      if (station.rotationVelocity[axis] != 0) return false;
      if (axis != 1) {
        double angle = station.rotationAngle[axis];
        if (!Double.isFinite(angle) || Math.abs(Math.IEEEremainder(angle, 360)) > 10.8)
          return false;
      }
    }
    return true;
  }

  public static void unlink(OrbitalBlockEntity controller) {
    OrbitalBlockEntity other = partner(controller);
    if (other != null) {
      other.elevatorDimension = "";
      other.elevatorPosition = 0;
      other.setChanged();
      sync(other);
    }
    if (controller.getLevel() instanceof ServerLevel level) {
      GalaxyData galaxy = GalaxyData.get(level.getServer());
      OrbitalBlockEntity space = StationLogic.inSpace(galaxy, level) ? controller : other;
      if (space != null && space.getLevel() != null) {
        Station station = StationLogic.at(galaxy, space.getLevel(), space.getBlockPos());
        if (station != null) anchor(galaxy, station, false);
      }
    }
    controller.elevatorDimension = "";
    controller.elevatorPosition = 0;
    controller.setChanged();
    sync(controller);
  }

  private static void sync(OrbitalBlockEntity controller) {
    if (controller.getLevel() instanceof ServerLevel level)
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 3);
  }

  private static void anchor(GalaxyData galaxy, Station station, boolean anchored) {
    StationLogic.update(galaxy, station, record -> record.anchored = anchored);
  }

  public static OrbitalBlockEntity partner(OrbitalBlockEntity controller) {
    if (controller.elevatorDimension.isEmpty()
        || !(controller.getLevel() instanceof ServerLevel source)) return null;
    ResourceLocation id = ResourceLocation.tryParse(controller.elevatorDimension);
    if (id == null) return null;
    ServerLevel destination =
        source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
    if (destination == null) return null;
    BlockPos pos = BlockPos.of(controller.elevatorPosition);
    return destination.getBlockEntity(pos) instanceof OrbitalBlockEntity other
            && other.spaceElevator()
        ? other
        : null;
  }

  public static boolean travel(OrbitalBlockEntity source, ServerPlayer passenger) {
    OrbitalBlockEntity target = partner(source);
    if (target == null
        || !(target.getLevel() instanceof ServerLevel destination)
        || !complete(source)
        || !complete(target)
        || !target.elevatorDimension.equals(source.getLevel().dimension().location().toString())
        || target.elevatorPosition != source.getBlockPos().asLong()) {
      source.status =
          Component.translatable(
              "status.adv_rocketry.space_elevator.space_elevator_link_or_frame_is");
      return false;
    }
    if (source.energy.consume(TRAVEL_ENERGY, true) < TRAVEL_ENERGY) {
      source.status =
          Component.translatable(
              "status.adv_rocketry.space_elevator.space_elevator_requires_50_000_fe");
      return false;
    }
    if (!(source.getLevel() instanceof ServerLevel departure)) return false;
    if (!departure
        .getEntitiesOfClass(ElevatorCapsule.class, new AABB(source.getBlockPos()).inflate(4, 6, 4))
        .isEmpty()) {
      source.status =
          Component.translatable("status.adv_rocketry.space_elevator.a_capsule_is_already_at_this");
      return false;
    }
    ElevatorCapsule capsule = OrbitalRegistry.ELEVATOR_CAPSULE.get().create(departure);
    if (capsule == null) return false;
    BlockPos launch = source.getBlockPos().above(2);
    capsule.setPos(launch.getX() + .5, launch.getY(), launch.getZ() + .5);
    capsule.setYRot(yaw(source.getBlockState().getValue(HorizontalOrbitalBlock.FACING)));
    capsule.depart(target);
    if (!departure.addFreshEntity(capsule)) return false;
    if (!passenger.startRiding(capsule, true)) {
      capsule.discard();
      source.status =
          Component.translatable(
              "status.adv_rocketry.space_elevator.passenger_could_not_board_the_capsule");
      return false;
    }
    source.energy.consume(TRAVEL_ENERGY, false);
    source.setChanged();
    source.status = Component.translatable("status.adv_rocketry.space_elevator.capsule_launched");
    return true;
  }
}

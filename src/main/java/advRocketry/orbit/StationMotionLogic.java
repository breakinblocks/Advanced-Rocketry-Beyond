// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/** Persistent station-altitude control using the original four-to-190 orbital-distance scale. */
public final class StationMotionLogic {
  private static final String[] AXES = {"x", "y", "z"};
  private static final int GRAVITY = 0;
  private static final int ALTITUDE = 1;
  private static final int ORIENTATION = 2;
  private static final Map<Long, long[]> ADVANCED = new HashMap<>();

  private StationMotionLogic() {}

  public static int comparatorSignal(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return 0;
    if (controller.stationGravityController()) {
      double gravity = (station.gravity != null ? station.gravity : 1);
      return Math.clamp((int) ((gravity - .1) / .059), 0, 15);
    }
    if (controller.stationAltitudeController()) {
      float altitude = station.altitude;
      return Math.clamp((int) (altitude + 5) / 13, 0, 15);
    }
    return 0;
  }

  public static int targetAltitude(OrbitalBlockEntity controller) {
    return targetAltitude(station(controller));
  }

  private static int targetAltitude(Station station) {
    return station == null ? 4 : station.targetAltitude;
  }

  public static int altitude(OrbitalBlockEntity controller) {
    Station station = station(controller);
    return station == null ? 4 : Math.round(station.altitude);
  }

  public static void adjustAltitude(OrbitalBlockEntity controller, int delta) {
    Station station = station(controller);
    if (station == null) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.station_motion.altitude_controller_requires_a_station");
      return;
    }
    int target = Math.clamp(targetAltitude(station) + delta, 4, 190);
    StationLogic.update(galaxy(controller), station, record -> record.targetAltitude = target);
    controller.status =
        Texts.translate(
            "status.adv_rocketry.station_motion.target_altitude_km", (target * 200 + 100));
    controller.setChanged();
  }

  public static void tickGravity(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return;
    double current = (station.gravity != null ? station.gravity : 1);
    double target =
        Math.max(AdvancedRocketryConfig.allowZeroGStations() ? 0 : 10, station.targetGravity)
            / 100d;
    double next = current + Math.clamp(target - current, -.001, .001);
    if (next == current) return;
    GalaxyData galaxy = galaxy(controller);
    if (!claim(galaxy, station, GRAVITY, controller)) return;
    StationLogic.update(galaxy, station, record -> record.gravity = next);
  }

  public static void tickAltitude(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null || station.anchored) return;
    float current = station.altitude;
    float target = targetAltitude(station);
    float difference = target - current;
    if (Math.abs(difference) < .001f) return;
    GalaxyData galaxy = galaxy(controller);
    if (!claim(galaxy, station, ALTITUDE, controller)) return;
    float acceleration = .1f * (190 - current + 1) / 190;
    float next = Math.max(4, current + Math.clamp(difference, -acceleration, acceleration));
    StationLogic.update(galaxy, station, record -> record.altitude = next);
  }

  public static int targetRotation(OrbitalBlockEntity controller, int axis) {
    return targetRotation(station(controller), axis);
  }

  private static int targetRotation(Station station, int axis) {
    return station == null ? 0 : station.targetRotation[axis];
  }

  public static void adjustRotation(OrbitalBlockEntity controller, int axis, int delta) {
    if (axis < 0 || axis >= AXES.length) return;
    Station station = station(controller);
    if (station == null) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.station_motion.orientation_controller_requires_a_station");
      return;
    }
    int target = Math.clamp(targetRotation(station, axis) + delta, -60, 60);
    StationLogic.update(
        galaxy(controller), station, record -> record.targetRotation[axis] = target);
    controller.status =
        Texts.translate(
            "status.adv_rocketry.station_motion.target_rotation_hour",
            AXES[axis].toUpperCase(),
            target);
    controller.setChanged();
  }

  public static void resetRotation(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return;
    StationLogic.update(
        galaxy(controller),
        station,
        record -> {
          Arrays.fill(record.targetRotation, 0);
        });
    controller.status =
        Component.translatable("status.adv_rocketry.station_motion.station_rotation_target_reset");
    controller.setChanged();
  }

  public static void tickOrientation(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return;
    if (station.anchored) {
      return;
    }
    double[] velocities = new double[AXES.length];
    boolean changed = false;
    for (int i = 0; i < AXES.length; i++) {
      double velocity = station.rotationVelocity[i];
      // The original stores turns; native sky angles and velocities are degrees.
      double target = targetRotation(station, i) * 360d / 72000d;
      velocities[i] = velocity + Math.clamp(target - velocity, -7.2d, 7.2d);
      changed |= velocities[i] != velocity || velocities[i] != 0;
    }
    if (!changed) return;
    GalaxyData galaxy = galaxy(controller);
    if (!claim(galaxy, station, ORIENTATION, controller)) return;
    long time = controller.getLevel().getGameTime();
    StationLogic.update(
        galaxy,
        station,
        record -> {
          for (int i = 0; i < AXES.length; i++) {
            double next = velocities[i];
            if (next == record.rotationVelocity[i] && next == 0) continue;
            record.rotationVelocity[i] = next;
            record.rotationAngle[i] =
                Math.floorMod((long) ((record.rotationAngle[i] + next) * 1000000), 360000000)
                    / 1000000d;
          }
          record.rotationTime = time;
        });
  }

  private static boolean claim(
      GalaxyData galaxy, Station station, int motion, OrbitalBlockEntity controller) {
    long time = controller.getLevel().getGameTime();
    long[] advanced =
        ADVANCED.computeIfAbsent(
            station.id, id -> new long[] {Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE});
    if (advanced[motion] == time) return false;
    advanced[motion] = time;
    return true;
  }

  private static GalaxyData galaxy(OrbitalBlockEntity controller) {
    return GalaxyData.get(((ServerLevel) controller.getLevel()).getServer());
  }

  private static Station station(OrbitalBlockEntity controller) {
    if (!controller.stationAltitudeController()
            && !controller.stationOrientationController()
            && !controller.stationGravityController()
        || controller.getLevel() == null) return null;
    return StationLogic.at(controller.getLevel(), controller.getBlockPos());
  }

  public static int targetGravity(OrbitalBlockEntity entity) {
    Station station =
        entity.getLevel() == null ? null : StationLogic.at(entity.getLevel(), entity.getBlockPos());
    return station == null ? 100 : station.targetGravity;
  }

  public static int currentGravity(OrbitalBlockEntity entity) {
    Station station =
        entity.getLevel() == null ? null : StationLogic.at(entity.getLevel(), entity.getBlockPos());
    return station == null
        ? 100
        : (int) Math.round(station.gravity != null ? station.gravity * 100 : 100);
  }

  public static void adjustGravity(OrbitalBlockEntity entity, int delta) {
    if (!entity.stationGravityController()
        || !(entity.getLevel() instanceof ServerLevel serverLevel)) return;
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    Station station = StationLogic.at(galaxy, serverLevel, entity.getBlockPos());
    if (station == null) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.controller_must_be_on_a_deployed");
      return;
    }
    int target =
        (int)
            Math.clamp(
                targetGravity(entity) + delta,
                AdvancedRocketryConfig.allowZeroGStations() ? 0 : 10,
                100);
    StationLogic.update(galaxy, station, record -> record.targetGravity = target);
    entity.status = Texts.translate("status.adv_rocketry.orbital.target_gravity", target);
    entity.setChanged();
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.Station;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/** Original experimental rocket navigation, with authoritative server integration. */
public final class SpaceNavigation {
  public int star;
  public int focus = -1;
  public Vec3 position = Vec3.ZERO;
  public Vec3 velocity = Vec3.ZERO;
  public float yaw;
  public boolean rcs = true;

  public record Arrival(int planet, long station) {}

  public static SpaceNavigation launch(Planet origin, GalaxyData galaxy) {
    while (origin.parent >= 0 && galaxy.planets.containsKey(origin.parent))
      origin = galaxy.planets.get(origin.parent);
    SpaceNavigation result = new SpaceNavigation();
    result.star = origin.star;
    result.focus = origin.id;
    result.position = new Vec3(radius(origin, false) * 1.1, 0, 0);
    return result;
  }

  public static double radius(Planet planet, boolean solar) {
    return (solar ? planet.parent < 0 ? 1 : .2 : planet.parent < 0 ? 10 : 8)
        * Math.max(planet.gravity * planet.gravity, .5)
        * 100;
  }

  public static Vec3 bodyPosition(Planet planet, Star star, Planet parent, double ticks) {
    double angle = planet.orbitalAngle(ticks, star, parent);
    double distance =
        (parent == null ? 100 : 75) * planet.orbitalDistance + (parent == null ? 0 : 100);
    return new Vec3(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
  }

  public void move(int forward, int turn, int vertical) {
    yaw = (yaw + (rcs ? turn * 5 : 0)) % 360;
    double acceleration = rcs ? forward * 2 : 0;
    double angle = Math.toRadians(yaw);
    velocity =
        velocity.add(
            -Math.sin(angle) * acceleration,
            rcs ? vertical * .02 : 0,
            Math.cos(angle) * acceleration);
    if (acceleration == 0) velocity = velocity.scale(.98);
    // The original damping gives a natural idle stop; bound sustained thrust to prevent tunneling.
    if (velocity.lengthSqr() > 10000) velocity = velocity.normalize().scale(100);
    position = position.add(velocity);
  }

  public Arrival navigate(GalaxyData galaxy, long ticks) {
    Planet center = galaxy.planets.get(focus);
    if (center == null) {
      focus = -1;
      for (Planet planet : galaxy.planets.values()) {
        if (planet.id < 0 || planet.parent >= 0 || planet.star != star) continue;
        Vec3 body = bodyPosition(planet, galaxy.stars.get(star), null, ticks);
        if (position.distanceToSqr(body) < Math.pow(radius(planet, true), 2) * 8) {
          focus = planet.id;
          Vec3 direction =
              velocity.lengthSqr() > .001
                  ? velocity.normalize().scale(-1)
                  : position.subtract(body).normalize();
          position = direction.scale(radius(planet, false) * 16);
          return null;
        }
      }
      return null;
    }
    Arrival stationArrival = null;
    double stationDistance = 10000;
    for (Planet planet : galaxy.planets.values()) {
      if (planet.id != center.id && planet.parent != center.id) continue;
      Vec3 body =
          planet.id == center.id
              ? Vec3.ZERO
              : bodyPosition(planet, galaxy.stars.get(star), center, ticks);
      if (planet.landable()
          && position.distanceToSqr(body) < .5 * Math.pow(radius(planet, false), 2))
        return new Arrival(planet.id, 0);
      List<Map.Entry<Long, Station>> stations = stations(galaxy, planet.id);
      for (int index = 0; index < stations.size(); index++) {
        Vec3 stationPosition = stationPosition(planet, body, index, stations.size());
        double distance = position.distanceToSqr(stationPosition);
        if (distance < stationDistance) {
          stationDistance = distance;
          stationArrival = new Arrival(planet.id, stations.get(index).getKey());
        }
      }
    }
    if (stationArrival != null) return stationArrival;
    if (position.lengthSqr() > Math.pow(radius(center, false) * 16, 2)) {
      Vec3 direction = velocity.lengthSqr() > .001 ? velocity.normalize() : position.normalize();
      position =
          bodyPosition(center, galaxy.stars.get(star), null, ticks)
              .add(direction.scale(radius(center, true) * 10));
      velocity = Vec3.ZERO;
      focus = -1;
    }
    return null;
  }

  public static List<Map.Entry<Long, Station>> stations(GalaxyData galaxy, int planet) {
    return galaxy.stations.entrySet().stream()
        .filter(
            entry ->
                entry.getValue().deployed
                    && entry.getValue().attachedTo == null
                    && entry.getValue().warpEta == null
                    && entry.getValue().orbitPlanet == planet)
        .sorted(Map.Entry.comparingByKey())
        .toList();
  }

  public static Vec3 stationPosition(Planet planet, Vec3 body, int index, int count) {
    double angle = index * Math.PI * 2 / Math.max(1, count);
    return body.add(
        Math.cos(angle) * radius(planet, false) * 2,
        0,
        Math.sin(angle) * radius(planet, false) * 2);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("star", star);
    tag.putInt("focus", focus);
    tag.putDouble("x", position.x);
    tag.putDouble("y", position.y);
    tag.putDouble("z", position.z);
    tag.putDouble("vx", velocity.x);
    tag.putDouble("vy", velocity.y);
    tag.putDouble("vz", velocity.z);
    tag.putFloat("yaw", yaw);
    tag.putBoolean("rcs", rcs);
    return tag;
  }

  public static SpaceNavigation load(CompoundTag tag) {
    SpaceNavigation result = new SpaceNavigation();
    result.star = tag.getInt("star");
    result.focus = tag.getInt("focus");
    result.position = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    result.velocity = new Vec3(tag.getDouble("vx"), tag.getDouble("vy"), tag.getDouble("vz"));
    result.yaw = tag.getFloat("yaw");
    result.rcs = tag.getBoolean("rcs");
    return result;
  }
}

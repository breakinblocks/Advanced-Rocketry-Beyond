package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.SatelliteLogic;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.Milestone;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

final class RocketTransfer {
  private RocketTransfer() {}

  static void enterSpaceFlight(RocketEntity rocket, ServerLevel level) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Planet origin = galaxy.planet(level);
    if (origin.id == GalaxyData.SPACE_ID) {
      Station station = StationLogic.at(galaxy, rocket.level(), rocket.blockPosition());
      origin = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    }
    if (origin == null || !(rocket.getFirstPassenger() instanceof Player)) {
      rocket.freeLaunch = false;
      abortTransfer(rocket, level);
      return;
    }
    SatelliteLogic.deployCargo(level, rocket.structure, rocket.blockPosition());
    StationLogic.deployCargo(level, rocket.structure, rocket.blockPosition());
    rocket.navigation = SpaceNavigation.launch(origin, galaxy);
    rocket.freeLaunch = false;
    rocket.sync(RocketEntity.NAVIGATION, rocket.navigation.save());
    rocket.sync(RocketEntity.FLIGHT, 3);
    ServerLevel destination =
        PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    // Physical ships stay above the station construction space; navigation has its own coordinates.
    Vec3 location =
        new Vec3(
            rocket.getX(), Math.max(1024, AdvancedRocketryConfig.orbitHeight()), rocket.getZ());
    destination.getChunkAt(BlockPos.containing(location));
    rocket.transit.release();
    Entity transferred =
        rocket.changeDimension(
            new DimensionTransition(
                destination, location, Vec3.ZERO, 0, 0, DimensionTransition.PLACE_PORTAL_TICKET));
    if (transferred instanceof RocketEntity moved) moved.transit.update();
    else {
      rocket.navigation = null;
      rocket.sync(RocketEntity.NAVIGATION, new CompoundTag());
      abortTransfer(rocket, level);
    }
  }

  static void transfer(RocketEntity rocket, ServerLevel level) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Planet target = galaxy.planets.get(rocket.structure.destination);
    if (target == null) {
      abortTransfer(rocket, level);
      return;
    }
    int source = galaxy.planet(level).id;
    double x = rocket.getX(), z = rocket.getZ();
    Station station = null;
    if (target.id == GalaxyData.SPACE_ID) {
      station =
          rocket.navigation == null
              ? StationLogic.destination(galaxy, source, rocket.structure.stationId)
              : galaxy.stations.get(rocket.structure.stationId);
      if (station == null) {
        abortTransfer(rocket, level);
        return;
      }
      x = station.x + rocket.structure.width / 2d;
      z = station.z + rocket.structure.depth / 2d;
    }
    if (rocket.navigation != null && target.id != GalaxyData.SPACE_ID) {
      x = rocket.launchX;
      z = rocket.launchZ;
    }
    ServerLevel destination = PlanetRuntime.create(level.getServer(), target);
    BlockPos arrivalPad =
        station == null
            ? null
            : LandingPadLogic.arrivalPad(
                destination,
                station,
                rocket.selectedLandingPad(rocket.structure.stationId),
                rocket);
    if (rocket.overridePlanet == target.id) {
      x = rocket.overrideX;
      z = rocket.overrideZ;
      arrivalPad = null;
    } else if (arrivalPad != null) {
      x = arrivalPad.getX() + .5;
      z = arrivalPad.getZ() + .5;
    }
    int top = rocket.findLandingHeight(destination, x, z);
    if (station != null) {
      int height = station.structure != null ? RocketStructure.load(station.structure).height : 1;
      top = Math.max(top, station.y + Math.max(1, height));
      if (arrivalPad != null) top = Math.max(top, arrivalPad.getY() + 1);
    }
    destination.getChunkAt(BlockPos.containing(x, top, z));
    rocket.structure.destination = source;
    rocket.overridePlanet = Integer.MIN_VALUE;
    rocket.landingY = top;
    rocket.verticalSpeed = -.1;
    rocket.sync(RocketEntity.FLIGHT, 2);
    SpaceNavigation previousNavigation = rocket.navigation;
    rocket.navigation = null;
    rocket.sync(RocketEntity.NAVIGATION, new CompoundTag());
    rocket.transit.release();
    Entity transferred =
        rocket.changeDimension(
            new DimensionTransition(
                destination,
                new Vec3(x, top + (target.id == GalaxyData.SPACE_ID ? 100 : 200), z),
                Vec3.ZERO,
                0,
                0,
                DimensionTransition.PLACE_PORTAL_TICKET));
    if (transferred == null) {
      rocket.structure.destination = target.id;
      if (previousNavigation == null) abortTransfer(rocket, level);
      else {
        rocket.navigation = previousNavigation;
        rocket.navigation.velocity = Vec3.ZERO;
        rocket.sync(RocketEntity.NAVIGATION, rocket.navigation.save());
        rocket.sync(RocketEntity.FLIGHT, 3);
        rocket.transit.update();
      }
    } else if (transferred instanceof RocketEntity arriving) {
      arriving.transit.update();
      if (GalaxyData.LUNA.equals(target.location)) {
        boolean first = !galaxy.reachedMoon;
        boolean playerArrived = false;
        for (Entity passenger : arriving.getPassengers())
          if (passenger instanceof ServerPlayer player) {
            playerArrived = true;
            AdvancementLogic.grant(player, Milestone.MOON_LANDING);
            if (first) AdvancementLogic.grant(player, Milestone.FIRST_MOON_LANDING);
          }
        if (playerArrived) {
          galaxy.reachedMoon = true;
          galaxy.setDirty();
        }
      }
    }
  }

  static void abortTransfer(RocketEntity rocket, ServerLevel level) {
    rocket.overridePlanet = Integer.MIN_VALUE;
    rocket.landingY = rocket.findLandingHeight(level);
    rocket.verticalSpeed = 0;
    rocket.sync(RocketEntity.FLIGHT, 2);
    rocket.transit.update();
  }
}

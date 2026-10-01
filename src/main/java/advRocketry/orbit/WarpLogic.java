// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.GalaxySync;
import advRocketry.space.Milestone;
import advRocketry.space.Planet;
import advRocketry.util.IdOrTag;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Original station travel cost with native station ownership and dilithium consumption. */
public final class WarpLogic {
  private WarpLogic() {}

  public static int cost(Planet source, Planet target) {
    return cost(source, target, source.orbitalTheta, target.orbitalTheta);
  }

  public static int cost(Planet source, Planet target, GalaxyData galaxy, long ticks) {
    return cost(
        source,
        target,
        source.orbitalAngle(
            ticks, galaxy.stars.get(source.star), galaxy.planets.get(source.parent)),
        target.orbitalAngle(
            ticks, galaxy.stars.get(target.star), galaxy.planets.get(target.parent)));
  }

  private static int cost(Planet source, Planet target, double sourceAngle, double targetAngle) {
    if (source.star != target.star) return 500;
    if (source.parent == target.id || target.parent == source.id) return 1;
    double sx = source.orbitalDistance * Math.cos(sourceAngle);
    double sy = source.orbitalDistance * Math.sin(sourceAngle);
    double tx = target.orbitalDistance * Math.cos(targetAngle);
    double ty = target.orbitalDistance * Math.sin(targetAngle);
    return Math.max(1, (int) Math.hypot(sx - tx, sy - ty));
  }

  public static boolean warp(OrbitalBlockEntity controller) {
    if (!controller.warpController() || !(controller.getLevel() instanceof ServerLevel level))
      return false;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    if (!StationLogic.inSpace(galaxy, level)) {
      controller.status =
          Component.translatable("status.adv_rocketry.warp.warp_controller_must_be_on_a");
      return false;
    }
    Station station = StationLogic.at(galaxy, level, controller.getBlockPos());
    if (station != null && station.warpEta != null) {
      controller.status =
          Component.translatable("status.adv_rocketry.warp.station_is_already_in_warp_transit");
      return false;
    }
    if (station != null && station.anchored) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.warp.disconnect_the_space_elevator_before_warping");
      return false;
    }
    Planet source = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    int destination =
        station != null && station.warpTarget != null
            ? station.warpTarget
            : controller.warpDestination;
    Planet target = galaxy.planets.get(destination);
    if (source == null
        || target == null
        || target.id < 0
        || !target.landable()
        || source.id == target.id) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.warp.choose_another_landable_destination_planet");
      return false;
    }
    if (AdvancedRocketryConfig.planetsMustBeDiscovered()
        && !target.known
        && !PlanetDiscoveryLogic.known(station, target.id)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.warp.destination_planet_has_not_been_discovered");
      return false;
    }
    if (!meetsArtifacts(controller, target)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.warp.warp_controller_is_missing_required_artifacts");
      return false;
    }
    int fuel = cost(source, target, galaxy, level.getServer().overworld().getGameTime());
    OrbitalBlockEntity core = null;
    for (long packed : station.warpCores)
      if (level.getBlockEntity(BlockPos.of(packed)) instanceof OrbitalBlockEntity found
          && found.warpCore()
          && WarpCoreLogic.complete(found)
          && StationLogic.at(galaxy, level, found.getBlockPos()) == station) {
        core = found;
        break;
      }
    if (core != null) WarpCoreLogic.refuel(core);
    if (core == null || station.fuel < fuel || controller.energy.consume(1000, true) < 1000) {
      controller.status =
          core == null
              ? Component.translatable("status.adv_rocketry.warp.warp_core_frame_is_incomplete")
              : station.fuel < fuel
                  ? Texts.translate("status.adv_rocketry.warp.station_needs_warp_fuel", fuel)
                  : Component.translatable(
                      "status.adv_rocketry.warp.warp_controller_needs_1_000_fe");
      return false;
    }
    controller.warpDestination = destination;
    controller.energy.consume(1000, false);
    StationLogic.update(
        galaxy,
        station,
        record -> {
          record.fuel = record.fuel - fuel;
          record.orbitPlanet = -1;
          record.warpSource = source.id;
          record.warpDestination = target.id;
          record.warpEta =
              level.getServer().overworld().getGameTime()
                  + Math.max(
                      1,
                      Math.round(
                          Math.min(fuel * 5L, 5000L) * AdvancedRocketryConfig.warpTravelTime()));
          record.lastWarpCost = fuel;
          record.lastWarpTime = level.getGameTime();
        });
    boolean firstWarp = !galaxy.reachedWarp;
    for (var player : level.players())
      if (StationLogic.at(galaxy, level, player.blockPosition()) == station) {
        AdvancementLogic.grant(player, Milestone.WARP_FLIGHT);
        if (firstWarp) AdvancementLogic.grant(player, Milestone.FIRST_WARP_FLIGHT);
      }
    galaxy.reachedWarp = true;
    galaxy.setDirty();
    GalaxySync.syncStations(level.getServer());
    controller.status =
        Texts.translate("status.adv_rocketry.warp.warping_to_fuel", target.name, fuel);
    controller.setChanged();
    return true;
  }

  private static boolean meetsArtifacts(OrbitalBlockEntity controller, Planet target) {
    for (IdOrTag requirement : target.artifacts) {
      boolean found = false;
      for (int slot = 4; slot <= 8 && !found; slot++) {
        ItemStack stack = controller.inventory.getStackInSlot(slot);
        found = requirement.matches(stack) && stack.getCount() >= requirement.count();
      }
      if (!found) return false;
    }
    return true;
  }

  public static void tick(MinecraftServer server) {
    GalaxyData galaxy = GalaxyData.get(server);
    if (complete(galaxy, server.overworld().getGameTime())) GalaxySync.syncStations(server);
  }

  static boolean complete(GalaxyData galaxy, long time) {
    boolean changed = false;
    for (var entry : galaxy.stations.entrySet()) {
      Station station = entry.getValue();
      if (station.warpEta == null || time < station.warpEta) continue;
      StationLogic.update(
          galaxy,
          station,
          arrived -> {
            arrived.orbitPlanet = arrived.warpDestination;
            arrived.warpEta = null;
          });
      changed = true;
    }
    return changed;
  }

  public static void cycleDestination(OrbitalBlockEntity entity) {
    if (!entity.warpController() || !(entity.getLevel() instanceof ServerLevel serverLevel)) return;
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    Station station = StationLogic.at(galaxy, serverLevel, entity.getBlockPos());
    List<Integer> planets =
        galaxy.planets.values().stream()
            .filter(
                planet ->
                    planet.id >= 0
                        && planet.landable()
                        && (!AdvancedRocketryConfig.planetsMustBeDiscovered()
                            || planet.known
                            || station != null && PlanetDiscoveryLogic.known(station, planet.id)))
            .map(planet -> planet.id)
            .toList();
    int index = planets.indexOf(entity.warpDestination);
    if (!planets.isEmpty()) entity.warpDestination = planets.get((index + 1) % planets.size());
    if (station != null)
      StationLogic.update(galaxy, station, record -> record.warpTarget = entity.warpDestination);
    entity.setChanged();
  }
}

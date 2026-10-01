// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.LinkerItem;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.PlanetIdChipItem;
import advRocketry.orbit.SatelliteLink;
import advRocketry.orbit.StationChipLocations;
import advRocketry.orbit.StationLink;
import advRocketry.orbit.StationLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Original guidance chip and saved-linker destination rules. */
public final class GuidanceComputer {
  private GuidanceComputer() {}

  public static boolean accepts(ItemStack stack) {
    return stack.is(OrbitalRegistry.PLANET_CHIP.get())
        || stack.is(OrbitalRegistry.STATION_CHIP.get())
        || stack.is(OrbitalRegistry.ASTEROID_CHIP.get())
        || stack.is(OrbitalRegistry.SATELLITE_CHIP.get())
        || stack.getItem() instanceof LinkerItem;
  }

  public static void rememberDeparture(ServerLevel level, ItemStack chip, BlockPos origin) {
    if (!chip.is(OrbitalRegistry.STATION_CHIP.get())) return;
    int planet = GalaxyData.get(level.getServer()).planet(level).id;
    if (planet == GalaxyData.SPACE_ID) return;
    StationChipLocations.remember(chip, planet, origin);
  }

  public static boolean hasReturnLocation(ItemStack chip, int planet) {
    return chip.is(OrbitalRegistry.STATION_CHIP.get())
        && StationChipLocations.destination(chip, planet) != null;
  }

  static int nextDestination(ServerLevel level, int destination) {
    var galaxy = GalaxyData.get(level.getServer());
    int current = galaxy.planet(level).id;
    List<Planet> planets =
        galaxy.planets.values().stream()
            .filter(
                planet ->
                    planet.landable()
                        && (planet.id != GalaxyData.SPACE_ID
                            || StationLogic.orbiting(galaxy, current) != null))
            .toList();
    if (planets.isEmpty()) return destination;
    int index = 0;
    for (int i = 0; i < planets.size(); i++) if (planets.get(i).id == destination) index = i + 1;
    return planets.get(index % planets.size()).id;
  }

  public static LandingPadLogic.Target route(ServerLevel level, ItemStack chip, BlockPos origin) {
    var galaxy = GalaxyData.get(level.getServer());
    int destination;
    long stationId = 0;
    double x = origin.getX() + .5, z = origin.getZ() + .5;
    if (PlanetIdChipItem.programmed(chip)) destination = PlanetIdChipItem.planet(chip);
    else if (chip.is(OrbitalRegistry.STATION_CHIP.get())) {
      stationId = StationLink.read(chip);
      var station = galaxy.stations.get(stationId);
      if (station == null || !station.deployed) return null;
      var current =
          galaxy.planet(level).id == GalaxyData.SPACE_ID
              ? StationLogic.at(galaxy, level, origin)
              : null;
      destination =
          current != null && current.id == stationId ? current.orbitPlanet : GalaxyData.SPACE_ID;
      if (hasReturnLocation(chip, destination)) {
        BlockPos takeoff = StationChipLocations.destination(chip, destination).pos();
        x = takeoff.getX() + .5;
        z = takeoff.getZ() + .5;
      }
    } else if (chip.is(OrbitalRegistry.SATELLITE_CHIP.get())) {
      var satellite = galaxy.satellites.get(SatelliteLink.read(chip));
      if (satellite == null || !satellite.deployed) return null;
      destination = satellite.orbitPlanet;
    } else if (chip.getItem() instanceof LinkerItem) {
      GlobalPos link = LinkerItem.target(chip).position().orElse(null);
      if (link == null) return null;
      var planet = galaxy.byDimension(link.dimension().location());
      if (planet == null) return null;
      destination = planet.id;
      BlockPos pos = link.pos();
      x = pos.getX() + .5;
      z = pos.getZ() + .5;
      if (destination == GalaxyData.SPACE_ID) {
        var station = StationLogic.at(galaxy, planet, pos);
        if (station == null) return null;
        stationId = station.id;
      }
    } else return null;
    return galaxy.planets.containsKey(destination)
        ? new LandingPadLogic.Target(destination, stationId, x, z)
        : null;
  }
}

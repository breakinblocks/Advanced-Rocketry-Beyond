// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original stellar brightness and atmosphere attenuation for solar generators. */
public final class SolarPower {
  private SolarPower() {}

  /** Uses the dimension's own sky clock, including planet rotation periods and fixed times. */
  public static boolean daylight(ServerLevel level, BlockPos pos) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    if (StationLogic.inSpace(galaxy, level)) return true;
    return level.dimensionType().hasSkyLight() && Math.cos(level.getSunAngle(1)) > 0;
  }

  /** Water is transparent to solar collection; solid waterlogged blocks still obstruct it. */
  public static boolean canSeeSkyThroughWater(ServerLevel level, BlockPos pos) {
    int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
    BlockPos.MutableBlockPos cursor = pos.mutable();
    for (int y = pos.getY(); y < top; y++) {
      cursor.setY(y);
      var state = level.getBlockState(cursor);
      if (!state.is(Blocks.WATER) && state.getLightBlock(level, cursor) > 0) return false;
    }
    return true;
  }

  public static int perPanel(ServerLevel level, BlockPos pos) {
    double insolation = insolationMultiplier(level, pos);
    return Math.max(
        0,
        Math.min(
            10000,
            (int)
                Math.floor(
                    2 * 1.0005 * insolation * AdvancedRocketryConfig.solarGeneratorMultiplier())));
  }

  public static double insolationMultiplier(ServerLevel level, BlockPos pos) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Planet world = galaxy.find(level);
    if (world == null) return 0;
    boolean stationOrbit = world.id == GalaxyData.SPACE_ID;
    Planet planet = world;
    if (stationOrbit) {
      Station station = StationLogic.at(galaxy, level, pos);
      if (station == null || station.warpEta != null) return 0;
      planet = galaxy.planets.get(station.orbitPlanet);
    }
    if (planet == null) return 0;
    Planet source = planet.parent < 0 ? planet : galaxy.planets.get(planet.parent);
    Star star = galaxy.stars.get(planet.star);
    int distance = source == null ? planet.orbitalDistance : source.orbitalDistance;
    return AtmosphereVisuals.flux(star, distance)
        * Math.exp(stationOrbit ? 0 : -.0026899 * planet.atmosphere)
        * 1.308;
  }
}

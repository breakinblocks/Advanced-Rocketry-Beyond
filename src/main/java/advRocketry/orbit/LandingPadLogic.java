// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.rocket.RocketEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Station landing-pad registry and linker-based return coordinates. */
public final class LandingPadLogic {
  public record Target(int planet, long stationId, double x, double z) {}

  private LandingPadLogic() {}

  public static void register(LandingPadBlockEntity pad) {
    if (!(pad.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, pad.getBlockPos());
    if (station != null)
      StationLogic.putEntry(galaxy, station.landingPads, pad.getBlockPos(), pad.name);
  }

  public static void remove(LandingPadBlockEntity pad) {
    if (!(pad.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, pad.getBlockPos());
    if (station != null) StationLogic.removeEntry(galaxy, station.landingPads, pad.getBlockPos());
  }

  public static boolean available(ServerLevel level, BlockPos pos, RocketEntity requester) {
    level.getChunkAt(pos);
    if (!(level.getBlockEntity(pos) instanceof LandingPadBlockEntity)) return false;
    var column =
        new AABB(
            pos.getX() - 1,
            level.getMinBuildHeight(),
            pos.getZ() - 1,
            pos.getX() + 2,
            level.getMaxBuildHeight() + 2048,
            pos.getZ() + 2);
    return !RocketEntity.occupiesLandingColumn(level, column, requester);
  }

  public static BlockPos arrivalPad(ServerLevel level, Station station) {
    return arrivalPad(level, station, null, null);
  }

  public static BlockPos arrivalPad(
      ServerLevel level, Station station, BlockPos preferred, RocketEntity requester) {
    var pads = station.landingPads;
    if (preferred != null
        && pads.containsKey(preferred.asLong())
        && available(level, preferred, requester)) return preferred;
    return pads.keySet().stream()
        .map(BlockPos::of)
        .filter(pos -> available(level, pos, requester))
        .min(
            Comparator.comparingDouble(
                pos -> pos.distSqr(new BlockPos(station.x, station.y, station.z))))
        .orElse(null);
  }

  public static Target override(ServerLevel level, BlockPos rocketPos, GalaxyData galaxy) {
    for (int y = -2; y <= 1; y++)
      for (int x = -2; x <= 2; x++)
        for (int z = -2; z <= 2; z++) {
          if (!(level.getBlockEntity(rocketPos.offset(x, y, z))
              instanceof LandingPadBlockEntity pad)) continue;
          ItemStack linker = pad.linker.getStackInSlot(0);
          if (!(linker.getItem() instanceof LinkerItem)) continue;
          GlobalPos link = LinkerItem.target(linker).position().orElse(null);
          if (link == null) continue;
          Planet destination = galaxy.byDimension(link.dimension().location());
          if (destination == null) continue;
          BlockPos pos = link.pos();
          long stationId = 0;
          if (destination.id == GalaxyData.SPACE_ID) {
            Station station = StationLogic.at(galaxy, destination, pos);
            if (station == null) continue;
            stationId = station.id;
          }
          return new Target(destination.id, stationId, pos.getX() + .5, pos.getZ() + .5);
        }
    return null;
  }
}

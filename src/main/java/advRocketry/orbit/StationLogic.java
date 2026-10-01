// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Deploys packed station palettes into distinct cells in the native space dimension. */
public final class StationLogic {
  private StationLogic() {}

  public static Station orbiting(GalaxyData galaxy, int planet) {
    for (Station station : galaxy.stations.values()) if (station.orbits(planet)) return station;
    return null;
  }

  public static Station destination(GalaxyData galaxy, int planet, long stationId) {
    Station selected = galaxy.stations.get(stationId);
    return stationId > 0 && selected != null && selected.orbits(planet)
        ? selected
        : stationId == 0 ? orbiting(galaxy, planet) : null;
  }

  public static boolean inSpace(GalaxyData galaxy, Level level) {
    Planet space = galaxy.planets.get(GalaxyData.SPACE_ID);
    return space != null && level.dimension().location().equals(space.dimension);
  }

  public static Station at(Level level, BlockPos pos) {
    return level instanceof ServerLevel server
        ? at(GalaxyData.get(server.getServer()), level, pos)
        : null;
  }

  public static Station at(GalaxyData galaxy, Level level, BlockPos pos) {
    return inSpace(galaxy, level) ? at(galaxy.stations.values(), pos) : null;
  }

  public static Station at(GalaxyData galaxy, Planet dimension, BlockPos pos) {
    return dimension != null && dimension.id == GalaxyData.SPACE_ID
        ? at(galaxy.stations.values(), pos)
        : null;
  }

  public static Station at(Iterable<Station> stations, BlockPos pos) {
    Station closest = null;
    long bestDistance = Long.MAX_VALUE;
    for (Station station : stations) {
      if (!station.free()) continue;
      long dx = (long) station.x - pos.getX();
      long dz = (long) station.z - pos.getZ();
      int radius = station.radius;
      if (Math.abs(dx) >= radius || Math.abs(dz) >= radius) continue;
      long distance = dx * dx + dz * dz;
      if (distance < bestDistance) {
        closest = station;
        bestDistance = distance;
      }
    }
    return closest;
  }

  public static int orbitPlanet(GalaxyData galaxy, Level level, BlockPos pos) {
    int planet = galaxy.planet(level).id;
    if (planet != GalaxyData.SPACE_ID) return planet;
    Station station = at(galaxy.stations.values(), pos);
    return station == null ? GalaxyData.SPACE_ID : station.orbitPlanet;
  }

  public static Station update(GalaxyData galaxy, long stationId, Consumer<Station> change) {
    Station station = galaxy.stations.get(stationId);
    return station == null ? null : update(galaxy, station, change);
  }

  public static Station update(GalaxyData galaxy, Station station, Consumer<Station> change) {
    change.accept(station);
    galaxy.setDirty();
    return station;
  }

  public static void putEntry(
      GalaxyData galaxy, Map<Long, String> entries, BlockPos pos, String value) {
    if (value.equals(entries.get(pos.asLong()))) return;
    entries.put(pos.asLong(), value);
    galaxy.setDirty();
  }

  public static void removeEntry(GalaxyData galaxy, Map<Long, String> entries, BlockPos pos) {
    if (entries.remove(pos.asLong()) != null) galaxy.setDirty();
  }

  public static boolean canPlace(
      ServerLevel space, BlockPos pos, int centerX, int centerZ, int radius) {
    return Math.abs(pos.getX() - centerX) < radius
        && Math.abs(pos.getZ() - centerZ) < radius
        && space.isInWorldBounds(pos)
        && space.getWorldBorder().isWithinBounds(pos)
        && space.getBlockState(pos).canBeReplaced();
  }

  public static void place(ServerLevel space, BlockPos pos, BlockState state, CompoundTag data) {
    space.setBlock(pos, state, 2);
    BlockEntity entity = space.getBlockEntity(pos);
    if (entity != null) {
      entity.loadWithComponents(data, space.registryAccess());
      entity.setChanged();
    }
  }

  public static int deployCargo(ServerLevel level, RocketStructure rocket, BlockPos launchPos) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    ServerLevel space =
        PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    int count = 0;
    for (RocketStructure.Cell hatch : rocket.cells) {
      if (!hatch.state().is(OrbitalRegistry.HATCH.get())) continue;
      ItemStack payload = OrbitalInventory.hatchPayload(hatch, level.registryAccess());
      if (!payload.is(OrbitalRegistry.STATION.get())) continue;
      long id = StationLink.read(payload);
      Station stored = galaxy.stations.get(id);
      if (id <= 0 || stored == null || stored.deployed || stored.structure == null) continue;
      RocketStructure module = RocketStructure.load(stored.structure);
      if (module.cells.isEmpty()) continue;
      int orbit = orbitPlanet(galaxy, level, launchPos);
      long attachedTo = DockingPortLogic.attach(space, galaxy, module, orbit);
      if (attachedTo > 0) {
        update(
            galaxy,
            stored,
            record -> {
              record.deployed = true;
              record.attachedTo = attachedTo;
            });
        OrbitalInventory.setHatchPayload(hatch, level.registryAccess(), ItemStack.EMPTY);
        count++;
        continue;
      }
      if (DockingPortLogic.hasTarget(module)) continue;
      int radius = AdvancedRocketryConfig.stationBuildRadius();
      int x = radius / 2 + (int) (id % 100) * 2 * radius;
      int z = radius / 2 + (int) (id / 100) * 2 * radius;
      BlockPos origin = new BlockPos(x, 120, z);
      boolean clear = true;
      for (RocketStructure.Cell cell : module.cells)
        if (!canPlace(space, origin.offset(cell.position()), x, z, radius)) {
          clear = false;
          break;
        }
      if (!clear) continue;
      for (RocketStructure.Cell cell : module.cells)
        place(space, origin.offset(cell.position()), cell.state(), cell.data());
      update(
          galaxy,
          stored,
          record -> {
            record.deployed = true;
            record.orbitPlanet = orbit;
            record.x = x;
            record.y = 120;
            record.z = z;
            record.radius = radius;
            record.deployedTime = level.getGameTime();
          });
      OrbitalInventory.setHatchPayload(hatch, level.registryAccess(), ItemStack.EMPTY);
      count++;
    }
    return count;
  }
}

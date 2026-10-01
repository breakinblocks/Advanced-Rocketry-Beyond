// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

/** Matches original docking IDs and places a rotated module against a station port. */
public final class DockingPortLogic {
  private DockingPortLogic() {}

  public static void register(OrbitalBlockEntity port) {
    if (!port.dockingPort() || !(port.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, port.getBlockPos());
    if (station == null) return;
    if (port.dockingId.isBlank())
      StationLogic.removeEntry(galaxy, station.dockingPorts, port.getBlockPos());
    else StationLogic.putEntry(galaxy, station.dockingPorts, port.getBlockPos(), port.dockingId);
  }

  public static void remove(OrbitalBlockEntity port) {
    if (!(port.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, port.getBlockPos());
    if (station != null) StationLogic.removeEntry(galaxy, station.dockingPorts, port.getBlockPos());
  }

  public static boolean configure(OrbitalBlockEntity port, String id, String target) {
    if (!port.dockingPort() || id.length() > 32 || target.length() > 32) return false;
    port.dockingId = id.trim();
    port.dockingTarget = target.trim();
    port.setChanged();
    register(port);
    if (port.getLevel() instanceof ServerLevel level)
      level.sendBlockUpdated(port.getBlockPos(), port.getBlockState(), port.getBlockState(), 3);
    return true;
  }

  public static long attach(
      ServerLevel space, GalaxyData galaxy, RocketStructure module, int orbit) {
    for (RocketStructure.Cell source : module.cells) {
      if (!source.state().is(OrbitalRegistry.DOCKING_PORT.get())) continue;
      String target = source.data().getString("docking_target");
      if (target.isBlank()) continue;
      Direction sourceFacing = source.state().getValue(DockingPortBlock.FACING);
      for (var entry : galaxy.stations.entrySet()) {
        Station station = entry.getValue();
        if (!station.deployed || station.attachedTo != null || station.orbitPlanet != orbit)
          continue;
        for (var port : station.dockingPorts.entrySet()) {
          if (!target.equals(port.getValue())) continue;
          BlockPos destination = BlockPos.of(port.getKey());
          if (!(space.getBlockEntity(destination) instanceof OrbitalBlockEntity found)
              || !found.dockingPort()
              || !target.equals(found.dockingId)) continue;
          Direction destinationFacing = found.getBlockState().getValue(DockingPortBlock.FACING);
          for (Rotation rotation : Rotation.values()) {
            if (rotation.rotate(sourceFacing) != destinationFacing.getOpposite()) continue;
            BlockPos origin = destination.relative(destinationFacing);
            if (!clear(space, module, source.position(), origin, rotation, station)) continue;
            for (RocketStructure.Cell cell : module.cells)
              StationLogic.place(
                  space,
                  origin.offset(cell.position().subtract(source.position()).rotate(rotation)),
                  cell.state().rotate(rotation),
                  cell.data());
            return entry.getKey();
          }
        }
      }
    }
    return 0;
  }

  public static boolean hasTarget(RocketStructure module) {
    for (RocketStructure.Cell cell : module.cells)
      if (cell.state().is(OrbitalRegistry.DOCKING_PORT.get())
          && !cell.data().getString("docking_target").isBlank()) return true;
    return false;
  }

  private static boolean clear(
      ServerLevel space,
      RocketStructure module,
      BlockPos source,
      BlockPos origin,
      Rotation rotation,
      Station station) {
    int radius = station.radius;
    for (RocketStructure.Cell cell : module.cells)
      if (!StationLogic.canPlace(
          space,
          origin.offset(cell.position().subtract(source).rotate(rotation)),
          station.x,
          station.z,
          radius)) return false;
    return true;
  }
}

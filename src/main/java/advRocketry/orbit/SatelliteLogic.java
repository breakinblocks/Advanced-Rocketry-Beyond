// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Releases assembled hatch payloads once a rocket reaches orbit. */
public final class SatelliteLogic {
  public static boolean recoverCargo(ServerLevel level, RocketStructure structure, ItemStack chip) {
    if (!chip.is(OrbitalRegistry.SATELLITE_CHIP.get())) return false;
    var galaxy = GalaxyData.get(level.getServer());
    long id = SatelliteLink.read(chip);
    Satellite record = galaxy.satellites.get(id);
    if (record == null || !record.deployed) return false;
    for (var cell : structure.cells) {
      if (!cell.state().is(OrbitalRegistry.HATCH.get())) continue;
      if (!OrbitalInventory.hatchPayload(cell, level.registryAccess()).isEmpty()) continue;
      record.deployed = false;
      ItemStack satellite = new ItemStack(OrbitalRegistry.SATELLITE.get());
      satellite.set(ModComponents.SATELLITE, SatelliteProperties.of(record));
      OrbitalInventory.setHatchPayload(cell, level.registryAccess(), satellite);
      galaxy.setDirty();
      return true;
    }
    return false;
  }

  private SatelliteLogic() {}

  public static int drawMicrowaveEnergy(
      GalaxyData galaxy, long id, int orbitPlanet, int maximum, long gameTime) {
    if (maximum <= 0) return 0;
    advance(galaxy, id, gameTime);
    Satellite stored = galaxy.satellites.get(id);
    if (stored == null || !stored.orbits(SatelliteType.SOLAR_ENERGY, orbitPlanet)) return 0;
    int drawn = Math.min(maximum, stored.energy);
    if (drawn <= 0) return 0;
    stored.energy -= drawn;
    galaxy.setDirty();
    return drawn;
  }

  public static void advance(GalaxyData galaxy, long id, long gameTime) {
    Satellite record = galaxy.satellites.get(id);
    if (record == null || !record.deployed) return;
    long previous = record.lastTick;
    if (previous >= gameTime) return;
    long elapsed = Math.min(gameTime - previous, 24000);
    int generation = record.powerGeneration;
    boolean producesData = record.type != null && record.type.producesData();
    int consumption = 1 + (producesData ? 4 + Math.max(0, Math.min(160, generation) - 5) : 0);
    int net = generation - consumption;
    record.energy = (int) Math.clamp((long) record.energy + net * elapsed, 0, record.powerStorage);
    if (producesData && generation >= 10 && record.maxData > 0) {
      int interval = (int) Math.max(1, 200 / Math.sqrt(.1 * (Math.min(160, generation) - 5)));
      long earned = gameTime / interval - previous / interval;
      record.data = (int) Math.min(record.maxData, record.data + earned);
    }
    record.lastTick = gameTime;
    galaxy.setDirty();
  }

  public static int deployCargo(ServerLevel level, RocketStructure structure, BlockPos launchPos) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    int orbitPlanet = StationLogic.orbitPlanet(galaxy, level, launchPos);
    int deployed = 0;
    for (RocketStructure.Cell cell : structure.cells) {
      if (!cell.state().is(OrbitalRegistry.HATCH.get())) continue;
      ItemStack payload = OrbitalInventory.hatchPayload(cell, level.registryAccess());
      if (!SatelliteItem.assembled(payload)) continue;
      SatelliteProperties properties = payload.get(ModComponents.SATELLITE);
      long id = properties.id();
      Satellite record = galaxy.satellites.get(id);
      if (id <= 0 || record == null || record.deployed || record.type != properties.type())
        continue;
      record.deployed = true;
      record.orbitPlanet = orbitPlanet;
      record.deployedTime = level.getGameTime();
      record.lastTick = level.getGameTime();
      galaxy.setDirty();
      OrbitalInventory.setHatchPayload(cell, level.registryAccess(), ItemStack.EMPTY);
      deployed++;
    }
    return deployed;
  }
}

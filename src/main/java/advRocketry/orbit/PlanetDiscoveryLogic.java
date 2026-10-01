// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.Milestone;
import advRocketry.space.Planet;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Station-scoped planet discovery using the original distance, mass and composition data. */
public final class PlanetDiscoveryLogic {
  private PlanetDiscoveryLogic() {}

  public static boolean start(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return false;
    if (controller.discoveryTicks > 0) {
      controller.status =
          Component.translatable("status.adv_rocketry.planet_discovery.discovery_already_running");
      return false;
    }
    ItemStack chip = controller.inventory.getStackInSlot(0);
    if (!chip.is(OrbitalRegistry.PLANET_CHIP.get()) || PlanetIdChipItem.programmed(chip)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.planet_discovery.insert_a_blank_planet_id_chip");
      return false;
    }
    if (controller.unitData(1, ResearchType.DISTANCE) < 100
        || controller.unitData(2, ResearchType.MASS) < 100
        || controller.unitData(3, ResearchType.COMPOSITION) < 100) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.planet_discovery.need_100_distance_mass_and_composition");
      return false;
    }
    controller.discoveryTicks = 1;
    controller.status =
        Component.translatable("status.adv_rocketry.planet_discovery.searching_for_planets");
    controller.setChanged();
    return true;
  }

  public static boolean importChip(OrbitalBlockEntity controller) {
    Station station = station(controller);
    if (station == null) return false;
    ItemStack chip = controller.inventory.getStackInSlot(0);
    int id = PlanetIdChipItem.planet(chip);
    GalaxyData galaxy = GalaxyData.get(((ServerLevel) controller.getLevel()).getServer());
    if (id < 0 || !galaxy.planets.containsKey(id)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.planet_discovery.insert_a_programmed_planet_id_chip");
      return false;
    }
    markKnown(galaxy, station, id);
    controller.status =
        Texts.translate(
            "status.adv_rocketry.planet_discovery.imported", galaxy.planets.get(id).name);
    controller.setChanged();
    return true;
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (controller.discoveryTicks <= 0 || !(controller.getLevel() instanceof ServerLevel level))
      return;
    Station station = station(controller);
    if (station == null) return;
    if (++controller.discoveryTicks < 1000) {
      if (controller.discoveryTicks % 20 == 0) controller.setChanged();
      return;
    }
    controller.discoveryTicks = 0;
    controller.takeUnitData(1, ResearchType.DISTANCE, 100);
    controller.takeUnitData(2, ResearchType.MASS, 100);
    controller.takeUnitData(3, ResearchType.COMPOSITION, 100);
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    List<Planet> unknown = new ArrayList<>();
    for (Planet planet : galaxy.planets.values())
      if (planet.id >= 0 && !known(station, planet.id)) unknown.add(planet);
    if (unknown.isEmpty())
      controller.status =
          Component.translatable("status.adv_rocketry.planet_discovery.all_planets_discovered");
    else if (level.random.nextInt(AdvancedRocketryConfig.planetDiscoveryChance()) != 0)
      controller.status =
          Component.translatable("status.adv_rocketry.planet_discovery.no_planet_found");
    else {
      Planet planet = unknown.get(level.random.nextInt(unknown.size()));
      ItemStack chip = controller.inventory.getStackInSlot(0);
      if (chip.is(OrbitalRegistry.PLANET_CHIP.get()) && !PlanetIdChipItem.programmed(chip)) {
        PlanetIdChipItem.program(chip, planet);
        controller.inventory.setStackInSlot(0, chip);
        markKnown(galaxy, station, planet.id);
        controller.status =
            Texts.translate("status.adv_rocketry.planet_discovery.discovered", planet.name);
        for (var player : level.players())
          if (StationLogic.at(galaxy, level, player.blockPosition()) == station)
            AdvancementLogic.grant(player, Milestone.PLANET_DISCOVERY);
      }
    }
    controller.setChanged();
  }

  public static boolean known(Station station, int planetId) {
    if (planetId == GalaxyData.EARTH_ID || planetId == station.orbitPlanet) return true;
    for (int known : station.knownPlanets) if (known == planetId) return true;
    return false;
  }

  private static void markKnown(GalaxyData galaxy, Station station, int planetId) {
    if (known(station, planetId)) return;
    int[] before = station.knownPlanets;
    int[] after = new int[before.length + 1];
    System.arraycopy(before, 0, after, 0, before.length);
    after[before.length] = planetId;
    StationLogic.update(galaxy, station, record -> record.knownPlanets = after);
  }

  private static Station station(OrbitalBlockEntity controller) {
    if (!controller.warpController() || !(controller.getLevel() instanceof ServerLevel level))
      return null;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, controller.getBlockPos());
    if (station != null) return station;
    controller.status =
        Component.translatable(
            "status.adv_rocketry.planet_discovery.discovery_requires_a_deployed_station");
    return null;
  }
}

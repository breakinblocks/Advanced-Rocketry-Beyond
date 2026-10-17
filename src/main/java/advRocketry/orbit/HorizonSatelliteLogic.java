package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

/** A deployed Horizon satellite can amplify one generator at a time. */
public final class HorizonSatelliteLogic {
  private HorizonSatelliteLogic() {}

  public static boolean blackHoleSystem(GalaxyData galaxy, int planetId) {
    Planet planet = galaxy.planets.get(planetId);
    Star star = planet == null ? null : galaxy.stars.get(planet.star);
    return star != null && star.blackHole();
  }

  public static void unlink(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Satellite satellite = galaxy.satellites.get(controller.horizonSatelliteId);
    GlobalPos target = GlobalPos.of(level.dimension(), controller.getBlockPos());
    if (satellite != null && target.equals(satellite.horizonTarget)) {
      satellite.horizonTarget = null;
      galaxy.setDirty();
    }
    controller.horizonSatelliteId = 0;
    controller.horizonBonus = 0;
    controller.setChanged();
  }

  public static int bonus(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return 0;
    long id = SatelliteLink.read(controller.inventory.getStackInSlot(0));
    if (id != controller.horizonSatelliteId) {
      unlink(controller);
      controller.horizonSatelliteId = id;
      controller.setChanged();
    }
    controller.horizonBonus = 0;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Satellite satellite = galaxy.satellites.get(id);
    int orbit = StationLogic.orbitPlanet(galaxy, level, controller.getBlockPos());
    if (satellite == null
        || !satellite.orbits(SatelliteType.HORIZON, orbit)
        || !blackHoleSystem(galaxy, orbit)
        || satellite.horizonBonus <= 0) return 0;
    Station station = StationLogic.at(galaxy, level, controller.getBlockPos());
    if (station != null && station.warpEta != null) return 0;
    GlobalPos target = GlobalPos.of(level.dimension(), controller.getBlockPos());
    if (satellite.horizonTarget != null && !target.equals(satellite.horizonTarget)) return 0;
    if (satellite.horizonTarget == null) {
      satellite.horizonTarget = target;
      galaxy.setDirty();
    }
    controller.horizonBonus = Math.clamp(satellite.horizonBonus, 0, 60000);
    return controller.horizonBonus;
  }

  public static int amplifiedOutput(int base, int bonusPercent) {
    return (int)
        Math.min(Integer.MAX_VALUE, Math.max(0L, (long) base * (100L + bonusPercent) / 100));
  }
}

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class SatelliteTerminalLogic {
  private SatelliteTerminalLogic() {}

  public static boolean transferData(OrbitalBlockEntity entity) {
    if (!entity.terminal() || !(entity.getLevel() instanceof ServerLevel serverLevel)) return false;
    ItemStack chip = entity.inventory.getStackInSlot(0), unit = entity.inventory.getStackInSlot(1);
    if (!chip.is(OrbitalRegistry.SATELLITE_CHIP.get())
        || !unit.is(OrbitalRegistry.DATA_UNIT.get())) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.insert_a_linked_satellite_chip_and");
      return false;
    }
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    long id = SatelliteLink.read(chip);
    SatelliteLogic.advance(galaxy, id, serverLevel.getGameTime());
    Satellite stored = galaxy.satellites.get(id);
    if (stored == null || !stored.deployed) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.satellite_is_not_deployed");
      return false;
    }
    int here = StationLogic.orbitPlanet(galaxy, serverLevel, entity.getBlockPos());
    if (galaxy.planets.get(here) == null
        || galaxy.planets.get(stored.orbitPlanet) == null
        || galaxy.planets.get(here).star != galaxy.planets.get(stored.orbitPlanet).star) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.satellite_is_outside_this_planetary_system");
      return false;
    }
    StoredResearch unitData = DataUnitItem.research(unit);
    ResearchType type = stored.type == null ? null : stored.type.research();
    if (type == null || !unitData.empty() && unitData.type() != type) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.data_unit_contains_a_different_data");
      return false;
    }
    int amount = Math.min(stored.data, ResearchType.UNIT_CAPACITY - unitData.amount());
    if (amount <= 0 || entity.energy.consume(1, true) == 0) {
      entity.status =
          amount <= 0
              ? Component.translatable("status.adv_rocketry.orbital.no_data_available")
              : Component.translatable("status.adv_rocketry.orbital.terminal_needs_fe");
      return false;
    }
    stored.data -= amount;
    galaxy.setDirty();
    DataUnitItem.store(unit, type, unitData.amount() + amount);
    entity.inventory.setStackInSlot(1, unit);
    entity.energy.consume(1, false);
    entity.status = Texts.translate("status.adv_rocketry.orbital.downloaded_data", amount);
    entity.setChanged();
    return true;
  }
}

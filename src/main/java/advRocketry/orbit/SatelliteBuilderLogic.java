package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class SatelliteBuilderLogic {
  private SatelliteBuilderLogic() {}

  public static boolean build(OrbitalBlockEntity entity) {
    if (!entity.builder() || !(entity.getLevel() instanceof ServerLevel serverLevel)) return false;
    if (powerBelow(entity) == null) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.satellite_builder_needs_an_energy_input");
      return false;
    }
    if (entity.builderProgress > 0 || !entity.inventory.getStackInSlot(10).isEmpty()) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.assembly_already_in_progress");
      entity.setChanged();
      return false;
    }
    if (entity.inventory.hasLooseModules()) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.remove_leftover_loose_modules_before_assembly");
      entity.setChanged();
      return false;
    }
    ItemStack primary = entity.inventory.getStackInSlot(0);
    SatelliteType type = OrbitalRegistry.type(primary);
    int generation = 0, storage = 720, data = 0;
    for (int slot = 1; slot <= 6; slot++) {
      ItemStack module = entity.inventory.getStackInSlot(slot);
      generation += OrbitalRegistry.generation(module);
      storage += OrbitalRegistry.storage(module);
      data += OrbitalRegistry.data(module);
      if (!module.isEmpty() && !OrbitalRegistry.module(module)) {
        entity.status =
            Component.translatable("status.adv_rocketry.orbital.invalid_satellite_module");
        entity.setChanged();
        return false;
      }
    }
    if (type == null
        || generation == 0
        || !entity.inventory.getStackInSlot(9).isEmpty()
        || !(type == SatelliteType.ORE_SCANNER
            ? entity.inventory.getStackInSlot(8).is(OrbitalRegistry.ORE_SCANNER.get())
            : type == SatelliteType.BIOME_CHANGER
                ? entity
                    .inventory
                    .getStackInSlot(8)
                    .is(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get())
                : entity.inventory.getStackInSlot(8).is(OrbitalRegistry.SATELLITE_CHIP.get()))
        || entity.inventory.getStackInSlot(8).has(ModComponents.SATELLITE_LINK)
        || !entity.inventory.isItemValid(7, entity.inventory.getStackInSlot(7))) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.supply_a_chassis_controller_power_module");
      entity.setChanged();
      return false;
    }
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    Satellite record = new Satellite(type, generation, storage, data);
    long id = galaxy.newSatellite(record);
    ItemStack chassis = entity.inventory.extractItem(7, 1, false);
    chassis.set(ModComponents.SATELLITE, SatelliteProperties.of(record));
    entity.inventory.setStackInSlot(10, chassis);
    if (entity.inventory.getStackInSlot(8).is(OrbitalRegistry.SATELLITE_CHIP.get())
        || entity.inventory.getStackInSlot(8).is(OrbitalRegistry.ORE_SCANNER.get())
        || entity
            .inventory
            .getStackInSlot(8)
            .is(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get())) {
      ItemStack chip = entity.inventory.getStackInSlot(8);
      chip.remove(ModComponents.BIOME_SELECTION);
      chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
      entity.inventory.setStackInSlot(8, chip);
    }
    entity.builderProgress = 1;
    entity.status = Texts.translate("status.adv_rocketry.orbital.assembling_satellite", id);
    entity.setChanged();
    return true;
  }

  public static boolean copyChip(OrbitalBlockEntity entity) {
    if (!entity.builder()
        || entity.getLevel() == null
        || entity.getLevel().isClientSide
        || entity.builderProgress > 0) return false;
    if (powerBelow(entity) == null) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.chip_copier_needs_an_energy_input");
      return false;
    }
    ItemStack source = entity.inventory.getStackInSlot(8);
    ItemStack blank = entity.inventory.getStackInSlot(11);
    boolean satellite = source.is(OrbitalRegistry.SATELLITE.get());
    boolean compatible =
        satellite
            ? blank.is(OrbitalRegistry.SATELLITE_CHIP.get()) && source.has(ModComponents.SATELLITE)
            : source.getItem() == blank.getItem()
                && (source.is(OrbitalRegistry.SATELLITE_CHIP.get())
                    || source.is(OrbitalRegistry.STATION_CHIP.get())
                    || source.is(OrbitalRegistry.PLANET_CHIP.get())
                    || source.is(OrbitalRegistry.ORE_SCANNER.get()))
                && (source.has(ModComponents.SATELLITE_LINK)
                    || source.has(ModComponents.STATION_LINK)
                    || source.has(ModComponents.PLANET));
    if (!compatible
        || !ModComponents.blank(blank)
        || !entity.inventory.getStackInSlot(9).isEmpty()
        || !entity.inventory.getStackInSlot(10).isEmpty()) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.supply_a_programmed_source_and_matching");
      entity.setChanged();
      return false;
    }
    ItemStack copy = entity.inventory.extractItem(11, 1, false);
    if (satellite)
      copy.set(
          ModComponents.SATELLITE_LINK,
          new SatelliteLink(source.get(ModComponents.SATELLITE).id()));
    else
      copy.copyFrom(
          source,
          ModComponents.SATELLITE_LINK,
          ModComponents.STATION_LINK,
          ModComponents.STATION_DESTINATIONS,
          ModComponents.PLANET);
    entity.inventory.setStackInSlot(10, copy);
    entity.builderProgress = 1;
    entity.status = Component.translatable("status.adv_rocketry.orbital.copying_id_chip");
    entity.setChanged();
    return true;
  }

  public static void tick(OrbitalBlockEntity entity) {
    if (entity.builderProgress == 0 || entity.getLevel() == null || entity.getLevel().isClientSide)
      return;
    PortBlockEntity power = powerBelow(entity);
    if (power == null) {
      entity.status =
          Component.translatable(
              "status.adv_rocketry.orbital.assembly_paused_energy_input_missing");
      return;
    }
    int internalPower = entity.energy.consume(10, true);
    if (internalPower + power.energyStorage.consume(10 - internalPower, true) < 10) {
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.assembly_paused_needs_10_fe_tick");
      return;
    }
    entity.energy.consume(internalPower, false);
    power.energyStorage.consume(10 - internalPower, false);
    if (entity.builderProgress++ < 100) {
      entity.setChanged();
      return;
    }
    ItemStack completed = entity.inventory.getStackInSlot(10);
    entity.inventory.setStackInSlot(9, completed);
    entity.inventory.setStackInSlot(10, ItemStack.EMPTY);
    entity.builderProgress = 0;
    entity.status = Component.translatable("status.adv_rocketry.orbital.builder_job_complete");
    entity.setChanged();
  }

  private static PortBlockEntity powerBelow(OrbitalBlockEntity entity) {
    PlacedMultiblock structure = PlacedMultiblock.of(entity, "satellite_builder");
    return structure != null && structure.complete()
        ? structure.entity('P', PortBlockEntity.class)
        : null;
  }
}

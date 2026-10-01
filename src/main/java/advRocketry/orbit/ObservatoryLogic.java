// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original observatory scan selection, service ports, and asteroid-chip programming. */
public final class ObservatoryLogic {
  private ObservatoryLogic() {}

  private static List<PortBlockEntity> ports(OrbitalBlockEntity controller) {
    PlacedMultiblock structure = ObservatoryStructure.structure(controller);
    return structure == null ? new ArrayList<>() : structure.entities('*', PortBlockEntity.class);
  }

  private static int power(OrbitalBlockEntity controller, int amount, boolean simulate) {
    int taken = controller.energy.consume(amount, simulate);
    for (PortBlockEntity port : ports(controller)) {
      if (taken == amount) break;
      if (port.kind() == MachinePorts.Kind.ENERGY_INPUT
          || port.kind() == MachinePorts.Kind.CREATIVE_ENERGY_INPUT)
        taken += port.energyStorage.consume(amount - taken, simulate);
    }
    return taken;
  }

  private static int data(
      ItemStackHandler inventory, ResearchType type, int amount, boolean commit) {
    int taken = 0;
    for (int slot = 0; slot < inventory.getSlots() && taken < amount; slot++) {
      ItemStack stack = inventory.getStackInSlot(slot);
      if (!stack.is(OrbitalRegistry.DATA_UNIT.get()) || stack.getCount() != 1) continue;
      StoredResearch research = DataUnitItem.research(stack);
      if (research.type() != type) continue;
      int used = Math.min(amount - taken, Math.max(0, research.amount()));
      taken += used;
      if (commit && used > 0) {
        ItemStack updated = stack.copy();
        DataUnitItem.store(updated, type, research.amount() - used);
        inventory.setStackInSlot(slot, updated);
      }
    }
    return taken;
  }

  private static int data(
      OrbitalBlockEntity controller, ResearchType type, int amount, boolean commit) {
    int taken = data(controller.inventory, type, amount, commit);
    for (PortBlockEntity port : ports(controller)) {
      if (taken == amount) break;
      if (port.kind() == MachinePorts.Kind.DATA_BUS)
        taken += data(port.inventory, type, amount - taken, commit);
    }
    return taken;
  }

  public static int researchData(OrbitalBlockEntity controller, ResearchType type) {
    return controller.observatory() && ObservatoryStructure.complete(controller)
        ? data(controller, type, 2000, false)
        : 0;
  }

  private static void synchronize(OrbitalBlockEntity controller) {
    controller.setChanged();
    if (controller.getLevel() instanceof ServerLevel level)
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 2);
  }

  public static boolean clearSky(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return false;
    return StationLogic.inSpace(GalaxyData.get(level.getServer()), level)
        || !level.isDay()
            && !level.isRaining()
            && level.canSeeSky(ObservatoryStructure.skyPosition(controller));
  }

  public static boolean scan(OrbitalBlockEntity controller) {
    if (!controller.observatory()
        || !(controller.getLevel() instanceof ServerLevel level)
        || !ObservatoryStructure.complete(controller)) return false;
    if (data(controller, ResearchType.DISTANCE, 100, false) < 100) {
      controller.status =
          Component.translatable("status.adv_rocketry.observatory.scan_needs_100_distance_data");
      return false;
    }
    data(controller, ResearchType.DISTANCE, 100, true);
    controller.asteroidSeed = level.getGameTime() / 100;
    findAsteroids(controller);
    controller.status =
        Texts.translate(
            "status.adv_rocketry.observatory.found_asteroid_targets",
            controller.asteroidOptions.length);
    synchronize(controller);
    return true;
  }

  private static void findAsteroids(OrbitalBlockEntity controller) {
    int range = ObservatoryStructure.range(controller);
    List<AsteroidCatalog.Asteroid> types =
        AsteroidCatalog.types(controller.getLevel().registryAccess());
    float total = 0;
    for (AsteroidCatalog.Asteroid type : types)
      if (type.distance() <= range) total += type.probability();
    Random random = new Random(controller.asteroidSeed);
    List<Integer> options = new ArrayList<>();
    if (total > 0)
      for (int index = 0; index < types.size(); index++) {
        AsteroidCatalog.Asteroid type = types.get(index);
        if (type.distance() > range) continue;
        for (int trial = 0; trial < 10; trial++)
          if (type.probability() / total >= random.nextFloat()) options.add(index);
      }
    controller.asteroidOptions = options.stream().mapToInt(Integer::intValue).toArray();
    controller.asteroidCatalogHash = types.hashCode();
    controller.asteroidOptionIndex = -1;
    controller.asteroidCandidate = -1;
  }

  public static void selectNext(OrbitalBlockEntity controller) {
    validateScan(controller);
    if (!controller.observatory() || controller.asteroidOptions.length == 0) return;
    controller.asteroidOptionIndex =
        (controller.asteroidOptionIndex + 1) % controller.asteroidOptions.length;
    controller.asteroidCandidate = controller.asteroidOptions[controller.asteroidOptionIndex];
    synchronize(controller);
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return;
    validateScan(controller);
    boolean open = controller.observatoryStructure() && clearSky(controller);
    int previous = controller.observatoryOpenProgress;
    controller.observatoryOpenProgress = Math.clamp(previous + (open ? 1 : -1), 0, 100);
    if (previous != controller.observatoryOpenProgress) {
      controller.setChanged();
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 2);
    }
    if (controller.observationTicks > 0 && ObservatoryStructure.complete(controller)) {
      controller.observationTicks = 0;
      controller.asteroidSeed = level.getGameTime() / 100;
      findAsteroids(controller);
      controller.setChanged();
    }
  }

  public static boolean program(OrbitalBlockEntity controller) {
    validateScan(controller);
    if (!controller.observatory()
        || !(controller.getLevel() instanceof ServerLevel)
        || !controller.observatoryStructure()
        || !clearSky(controller)
        || controller.observatoryOpenProgress == 0
        || controller.asteroidCandidate < 0
        || controller.asteroidCandidate
            >= AsteroidCatalog.types(controller.getLevel().registryAccess()).size()
        || !controller.inventory.getStackInSlot(0).is(OrbitalRegistry.ASTEROID_CHIP.get())
        || !controller.inventory.getStackInSlot(4).isEmpty()
        || power(controller, 500, true) < 500) return false;
    AsteroidCatalog.Asteroid asteroid =
        AsteroidCatalog.types(controller.getLevel().registryAccess())
            .get(controller.asteroidCandidate);
    if (asteroid.distance() > controller.observatoryRange()) return false;
    ItemStack chip = controller.inventory.extractItem(0, 1, false);
    AsteroidChipItem.program(chip, asteroid.id(), controller.asteroidSeed);
    data(controller, ResearchType.COMPOSITION, 1000, true);
    data(controller, ResearchType.MASS, 1000, true);
    power(controller, 500, false);
    controller.inventory.setStackInSlot(4, chip);
    controller.status =
        Component.translatable("status.adv_rocketry.observatory.asteroid_chip_programmed");
    controller.setChanged();
    return true;
  }

  private static void validateScan(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel)) return;
    int current = AsteroidCatalog.types(controller.getLevel().registryAccess()).hashCode();
    if (controller.asteroidCatalogHash == current) return;
    controller.asteroidCatalogHash = current;
    controller.asteroidOptions = new int[0];
    controller.asteroidOptionIndex = -1;
    controller.asteroidCandidate = -1;
    controller.setChanged();
  }
}

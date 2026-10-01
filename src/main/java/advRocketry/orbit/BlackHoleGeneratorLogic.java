// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.DataMaps;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Original five-layer structure and item-fueled 500 FE/t generation in black-hole orbit. */
public final class BlackHoleGeneratorLogic {
  private BlackHoleGeneratorLogic() {}

  public static boolean complete(OrbitalBlockEntity controller) {
    return PlacedMultiblock.complete(controller, "black_hole_generator");
  }

  private static List<PortBlockEntity> ports(OrbitalBlockEntity controller) {
    PlacedMultiblock structure = PlacedMultiblock.of(controller, "black_hole_generator");
    return structure == null ? List.of() : structure.entities('#', PortBlockEntity.class);
  }

  private static boolean blackHoleOrbit(OrbitalBlockEntity controller, ServerLevel level) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, controller.getBlockPos());
    if (station == null || station.warpEta != null) return false;
    Planet planet = galaxy.planets.get(station.orbitPlanet);
    Star star = planet == null ? null : galaxy.stars.get(planet.star);
    return star != null && star.blackHole();
  }

  private static int burnTime(ItemStack stack) {
    DataMaps.BlackHoleFuel fuel = stack.getItemHolder().getData(DataMaps.BLACK_HOLE_FUEL);
    return fuel == null ? AdvancedRocketryConfig.blackHoleDefaultBurnTime() : fuel.burnTime();
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return;
    controller.powerMadeLastTick = 0;
    if (!complete(controller)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.black_hole_generator.black_hole_generator_structure_incomplete");
      return;
    }
    if (!blackHoleOrbit(controller, level)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.black_hole_generator.requires_a_station_orbiting_a_black");
      return;
    }
    List<PortBlockEntity> ports = ports(controller);
    long now = level.getGameTime();
    if (controller.blackHoleBurnUntil <= now
        && controller.energy.getEnergyStored() < controller.energy.getMaxEnergyStored()) {
      for (PortBlockEntity port : ports) {
        if (port.kind() != MachinePorts.Kind.ITEM_INPUT) continue;
        for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
          ItemStack stack = port.inventory.getStackInSlot(slot);
          if (stack.isEmpty()) continue;
          controller.blackHoleBurnUntil = now + burnTime(stack);
          port.inventory.extractItem(slot, 1, false);
          controller.setChanged();
          break;
        }
        if (controller.blackHoleBurnUntil > now) break;
      }
    }
    if (controller.blackHoleBurnUntil <= now) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.black_hole_generator.insert_fuel_into_an_item_input");
      return;
    }
    int output = 500 * AdvancedRocketryConfig.blackHolePowerMultiplier();
    int accepted = controller.addGeneratedEnergy(output);
    controller.powerMadeLastTick = accepted;
    controller.status =
        Texts.translate("status.adv_rocketry.black_hole_generator.generating_fe_t", accepted);
    for (PortBlockEntity port : ports)
      if (port.kind() == MachinePorts.Kind.ENERGY_OUTPUT)
        controller.energy.extractEnergy(
            port.injectGeneratedEnergy(controller.energy.getEnergyStored()), false);
  }
}

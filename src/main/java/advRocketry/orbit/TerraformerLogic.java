// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import advRocketry.space.TerraformBiomeLogic;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Original atmosphere density process with its full twelve-layer multiblock and power ports. */
public final class TerraformerLogic {
  private static final int ENERGY_PER_TICK = 1000;

  record Ports(List<BlockPos> power, List<BlockPos> fluid) {}

  private TerraformerLogic() {}

  private static Ports ports(OrbitalBlockEntity controller) {
    PlacedMultiblock structure = PlacedMultiblock.of(controller, "atmosphere_terraformer");
    return new Ports(List.copyOf(structure.positions('P')), List.copyOf(structure.positions('L')));
  }

  public static boolean complete(OrbitalBlockEntity controller) {
    return controller.atmosphereTerraformer()
        && PlacedMultiblock.complete(controller, "atmosphere_terraformer");
  }

  public static void control(OrbitalBlockEntity controller, int button) {
    if (!controller.atmosphereTerraformer()) return;
    if (button == 29) {
      controller.terraformingDirection = 1;
      controller.terraformingEnabled = true;
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.increasing_atmospheric_pressure");
    } else if (button == 30) {
      controller.terraformingDirection = -1;
      controller.terraformingEnabled = true;
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.decreasing_atmospheric_pressure");
    } else if (button == 31) {
      controller.terraformingEnabled = !controller.terraformingEnabled;
      if (!controller.terraformingEnabled) controller.setTerraformerRunning(false);
      controller.status =
          controller.terraformingEnabled
              ? Component.translatable("status.adv_rocketry.terraformer.terraforming_enabled")
              : Component.translatable("status.adv_rocketry.terraformer.terraforming_paused");
    }
    controller.setChanged();
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!AdvancedRocketryConfig.enableTerraforming()
        || !controller.terraformingEnabled
        || !(controller.getLevel() instanceof ServerLevel level)) {
      controller.setTerraformerRunning(false);
      return;
    }
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Planet planet = galaxy.byDimension(level.dimension().location());
    if (planet == null && AdvancedRocketryConfig.allowTerraformNonAR())
      planet = galaxy.ensureExternalPlanet(level);
    if (!TerraformBiomeLogic.eligible(planet, level)) {
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.terraformer_must_be_on_a_planet");
      controller.setTerraformerRunning(false);
      return;
    }
    ItemStack remote = controller.inventory.getStackInSlot(0);
    long id = SatelliteLink.read(remote);
    Satellite satellite = galaxy.satellites.get(id);
    if (!remote.is(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get())
        || satellite == null
        || !satellite.orbits(SatelliteType.BIOME_CHANGER, planet.id)) {
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.need_a_linked_biome_changer_in");
      controller.terraformingTicks = 0;
      controller.setTerraformerRunning(false);
      return;
    }
    if (controller.terraformingTicks % 20 == 0 || controller.terraformerPorts == null) {
      if (!complete(controller)) {
        controller.terraformerPorts = null;
        controller.status =
            Component.translatable(
                "status.adv_rocketry.terraformer.terraformer_structure_incomplete");
        controller.setTerraformerRunning(false);
        return;
      }
      controller.terraformerPorts = ports(controller);
    }
    if (planet.atmosphere <= 0 && controller.terraformingDirection < 0
        || planet.atmosphere >= 1600 && controller.terraformingDirection > 0) {
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.atmospheric_pressure_at_limit");
      controller.setTerraformerRunning(false);
      return;
    }
    List<PortBlockEntity> power = new ArrayList<>();
    for (BlockPos pos : controller.terraformerPorts.power())
      if (level.getBlockEntity(pos) instanceof PortBlockEntity port) power.add(port);
    int remaining = ENERGY_PER_TICK;
    for (PortBlockEntity port : power)
      remaining -= Math.min(remaining, port.energyStorage.getEnergyStored());
    if (remaining > 0) {
      controller.status =
          Component.translatable("status.adv_rocketry.terraformer.terraformer_needs_1_000_fe_tick");
      controller.setTerraformerRunning(false);
      return;
    }
    int fluidRate = AdvancedRocketryConfig.terraformFluidRate();
    if (AdvancedRocketryConfig.terraformRequiresFluid()
        && (fluidAvailable(controller, "nitrogen", fluidRate, IFluidHandler.FluidAction.SIMULATE)
                < fluidRate
            || fluidAvailable(controller, "oxygen", fluidRate, IFluidHandler.FluidAction.SIMULATE)
                < fluidRate)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.terraformer.terraformer_needs_nitrogen_and_oxygen");
      controller.setTerraformerRunning(false);
      return;
    }
    remaining = ENERGY_PER_TICK;
    for (PortBlockEntity port : power) remaining -= port.energyStorage.consume(remaining, false);
    if (AdvancedRocketryConfig.terraformRequiresFluid()) {
      fluidAvailable(controller, "nitrogen", fluidRate, IFluidHandler.FluidAction.EXECUTE);
      fluidAvailable(controller, "oxygen", fluidRate, IFluidHandler.FluidAction.EXECUTE);
    }
    controller.setTerraformerRunning(true);
    controller.terraformingTicks++;
    controller.status =
        Texts.translate(
            "status.adv_rocketry.terraformer.terraforming",
            controller.terraformingTicks,
            AdvancedRocketryConfig.terraformDuration());
    if (controller.terraformingTicks >= AdvancedRocketryConfig.terraformDuration()) {
      controller.terraformingTicks = 0;
      int previousClass = TerraformBiomeLogic.atmosphereClass(planet.atmosphere);
      TerraformBiomeLogic.changeAtmosphere(
          planet,
          Math.clamp(planet.atmosphere + controller.terraformingDirection, 0, 1600),
          level.getSeed());
      if (previousClass != TerraformBiomeLogic.atmosphereClass(planet.atmosphere))
        TerraformBiomeLogic.queueLoaded(level);
      galaxy.setDirty();
      for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        GalaxySync.send(player);
      controller.status =
          Texts.translate(
              "status.adv_rocketry.terraformer.atmospheric_pressure_100", planet.atmosphere);
    }
    if (controller.terraformingTicks % 20 == 0) controller.setChanged();
  }

  private static int fluidAvailable(
      OrbitalBlockEntity controller, String name, int amount, IFluidHandler.FluidAction action) {
    if (!(controller.getLevel() instanceof ServerLevel level)
        || controller.terraformerPorts == null) return 0;
    int remaining = amount;
    for (BlockPos pos : controller.terraformerPorts.fluid()) {
      if (remaining <= 0) break;
      if (level.getBlockEntity(pos) instanceof PortBlockEntity port) {
        FluidStack request =
            new FluidStack(ProcessingFluids.DEFINITIONS.get(name).source.get(), remaining);
        remaining -= port.myTank.drain(request, action).getAmount();
      }
    }
    return amount - remaining;
  }
}

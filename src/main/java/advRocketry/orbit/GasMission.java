// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.GasGiantGases;
import advRocketry.space.Planet;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Original gas-giant mission timing and packed rocket tank filling. */
public final class GasMission {
  private GasMission() {}

  public static boolean possible(ServerLevel level, RocketEntity rocket) {
    Planet orbit = giant(level, rocket);
    return orbit != null
        && !GasGiantGases.harvestable(orbit).isEmpty()
        && rocket.structure().intakePower > 0
        && rocket.structure().seats == 0
        && capacity(rocket.structure()) > 0;
  }

  public static Planet giant(ServerLevel level, RocketEntity rocket) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    if (galaxy.planet(level).id != GalaxyData.SPACE_ID) return null;
    Station station = StationLogic.at(galaxy, level, rocket.missionOrigin());
    if (station == null) return null;
    Planet orbit = galaxy.planets.get(station.orbitPlanet);
    return orbit != null && orbit.gasGiant ? orbit : null;
  }

  public static int capacity(RocketStructure ship) {
    int total = 0;
    for (RocketStructure.Cell cell : ship.cells) total += cellCapacity(cell);
    return total;
  }

  public static int cellCapacity(RocketStructure.Cell cell) {
    if (cell.state().is(MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get())
        || cell.state().is(MachinePorts.BLOCK_FLUID_OUTPUT_BLOCK.get()))
      return PortBlockEntity.FLUID_CAPACITY;
    if (cell.state().is(OrbitalRegistry.FLUID_TANK.get()))
      return AdvancedRocketryConfig.blockTankCapacity();
    return 0;
  }

  public static boolean begin(ServerLevel level, RocketEntity rocket) {
    if (!possible(level, rocket)) return false;
    Planet giant = giant(level, rocket);
    List<ResourceLocation> gases = GasGiantGases.harvestable(giant);
    ResourceLocation gas = gases.get(Math.floorMod(rocket.selectedGas(), gases.size()));
    if (!BuiltInRegistries.FLUID.containsKey(gas)) return false;
    MiningMission.schedule(
        level,
        rocket,
        Mission.Kind.GAS,
        Math.round(
            2d
                * capacity(rocket.structure())
                / rocket.structure().intakePower
                * AdvancedRocketryConfig.gasMissionMultiplier()),
        mission -> mission.gas = gas);
    return true;
  }

  public static boolean finish(ServerLevel level, Mission mission) {
    return MiningMission.deliver(level, createReturn(level, mission));
  }

  public static RocketEntity createReturn(ServerLevel level, Mission mission) {
    ResourceLocation gas = mission.gas;
    if (gas == null || !BuiltInRegistries.FLUID.containsKey(gas)) return null;
    var fluid = BuiltInRegistries.FLUID.get(gas);
    if (fluid == Fluids.EMPTY) return null;
    RocketStructure ship = RocketStructure.load(mission.rocket.getCompound("ship"));
    if (ship.intakePower <= 0) return null;
    for (RocketStructure.Cell cell : ship.cells) {
      int capacity = cellCapacity(cell);
      if (capacity == 0) continue;
      FluidTank tank = new FluidTank(capacity);
      tank.readFromNBT(level.registryAccess(), cell.data().getCompound("fluid"));
      tank.fill(new FluidStack(fluid, capacity), IFluidHandler.FluidAction.EXECUTE);
      cell.data().put("fluid", tank.writeToNBT(level.registryAccess(), new CompoundTag()));
    }
    CompoundTag returning = mission.rocket.copy();
    returning.put("ship", ship.save(true));
    returning.putInt("propellant", 0);
    returning.putInt("oxidizer", 0);
    returning.putDouble("primary_fuel_carry", 0);
    returning.putDouble("oxidizer_fuel_carry", 0);
    return RocketEntity.returnFromMission(level, returning, List.of());
  }
}

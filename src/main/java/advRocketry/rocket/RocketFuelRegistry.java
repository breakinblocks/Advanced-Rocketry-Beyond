// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.DataMaps;
import advRocketry.Main;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

/** Parses the original per-engine fuel fluid lists and fuel-point multipliers. */
public final class RocketFuelRegistry {
  private RocketFuelRegistry() {}

  public static ResourceLocation fluidId(FluidStack stack) {
    return BuiltInRegistries.FLUID.getKey(stack.getFluid());
  }

  public static double multiplier(RocketPartBlock.Fuel type, ResourceLocation fluid) {
    return BuiltInRegistries.FLUID
        .getHolder(fluid)
        .map(holder -> multiplier(type, holder.getData(DataMaps.ROCKET_PROPELLANT)))
        .orElse(0d);
  }

  private static double multiplier(RocketPartBlock.Fuel type, DataMaps.Propellant propellant) {
    if (propellant == null) return 0;
    Optional<Float> value =
        switch (type) {
          case MONOPROPELLANT -> propellant.monopropellant();
          case BIPROPELLANT -> propellant.bipropellant();
          case OXIDIZER -> propellant.oxidizer();
          case NUCLEAR -> propellant.nuclear();
        };
    return value.orElse(0f);
  }

  public static ResourceLocation defaultFluid(RocketPartBlock.Fuel type) {
    return ResourceLocation.fromNamespaceAndPath(
        Main.MODID,
        switch (type) {
          case MONOPROPELLANT -> "rocket_fuel";
          case BIPROPELLANT, NUCLEAR -> "hydrogen";
          case OXIDIZER -> "oxygen";
        });
  }

  public static double maximumMultiplier(RocketPartBlock.Fuel type) {
    double maximum = 0;
    for (DataMaps.Propellant propellant :
        BuiltInRegistries.FLUID.getDataMap(DataMaps.ROCKET_PROPELLANT).values())
      maximum = Math.max(maximum, multiplier(type, propellant));
    return maximum;
  }

  public static int cost(int fuelPoints, RocketPartBlock.Fuel type, ResourceLocation fluid) {
    double multiplier = multiplier(type, fluid);
    return (int) Math.ceil(fuelPoints / (multiplier > 0 ? multiplier : 1));
  }

  public static double availablePoints(
      int millibuckets, double carry, RocketPartBlock.Fuel type, ResourceLocation fluid) {
    double multiplier = multiplier(type, fluid);
    return (millibuckets - carry) * (multiplier > 0 ? multiplier : 1);
  }

  public static double unroundedCost(
      int fuelPoints, double carry, RocketPartBlock.Fuel type, ResourceLocation fluid) {
    double multiplier = multiplier(type, fluid);
    return fuelPoints / (multiplier > 0 ? multiplier : 1) + carry;
  }

  public static boolean allowed(RocketPartBlock.Fuel type, FluidStack stack) {
    return !stack.isEmpty() && multiplier(type, fluidId(stack)) > 0;
  }
}

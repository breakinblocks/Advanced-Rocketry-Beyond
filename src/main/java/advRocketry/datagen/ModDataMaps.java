package advRocketry.datagen;

import advRocketry.DataMaps;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;

final class ModDataMaps extends DataMapProvider {
  ModDataMaps(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
    super(output, lookup);
  }

  private static ResourceLocation id(String id) {
    return ResourceLocation.parse(id);
  }

  @Override
  protected void gather() {
    var fuel = builder(DataMaps.BLACK_HOLE_FUEL);
    for (String item :
        new String[] {
          "minecraft:stone", "minecraft:dirt", "minecraft:netherrack", "minecraft:cobblestone"
        }) fuel.add(id(item), new DataMaps.BlackHoleFuel(1), false);
    builder(DataMaps.ROCKET_PROPELLANT)
        .add(
            id("adv_rocketry:rocket_fuel"),
            new DataMaps.Propellant(
                Optional.of(2f), Optional.empty(), Optional.empty(), Optional.empty()),
            false)
        .add(
            id("adv_rocketry:hydrogen"),
            new DataMaps.Propellant(
                Optional.empty(), Optional.of(1f), Optional.empty(), Optional.of(1f)),
            false)
        .add(
            id("adv_rocketry:oxygen"),
            new DataMaps.Propellant(
                Optional.empty(), Optional.empty(), Optional.of(1f), Optional.empty()),
            false);
    builder(DataMaps.GAS_GIANT_GAS)
        .add(id("adv_rocketry:hydrogen"), new DataMaps.GasGiantGas(1.25f, 16, 1), false)
        .add(id("adv_rocketry:helium"), new DataMaps.GasGiantGas(1.25f, 16, .9f), false)
        .add(id("adv_rocketry:helium3"), new DataMaps.GasGiantGas(1.75f, 16, .2f), false)
        .add(id("adv_rocketry:oxygen"), new DataMaps.GasGiantGas(0, 1.24f, 1), false)
        .add(id("adv_rocketry:nitrogen"), new DataMaps.GasGiantGas(0, 1.24f, 1), false)
        .add(id("adv_rocketry:ammonia"), new DataMaps.GasGiantGas(0, 1.24f, .75f), false)
        .add(id("adv_rocketry:methane"), new DataMaps.GasGiantGas(0, 1.24f, .25f), false);
    builder(DataMaps.SATELLITE_MODULE)
        .add(
            id("adv_rocketry:basic_satellite_solar_panel"),
            new DataMaps.SatelliteModule(4, 0, 0),
            false)
        .add(
            id("adv_rocketry:large_satellite_solar_panel"),
            new DataMaps.SatelliteModule(40, 0, 0),
            false)
        .add(id("adv_rocketry:battery"), new DataMaps.SatelliteModule(0, 10000, 0), false)
        .add(id("adv_rocketry:advanced_battery"), new DataMaps.SatelliteModule(0, 40000, 0), false)
        .add(id("adv_rocketry:item_data_storage"), new DataMaps.SatelliteModule(0, 0, 1000), false);
  }
}

package advRocketry.datagen;

import advRocketry.DataMaps;
import advRocketry.processing.ProcessingFluids;
import advRocketry.space.BiomeMaterials;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

final class ModDataMaps extends DataMapProvider {
  ModDataMaps(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
    super(output, lookup);
  }

  private static ResourceLocation id(String id) {
    return ResourceLocation.parse(id);
  }

  @Override
  protected void gather() {
    var compost = builder(NeoForgeDataMaps.COMPOSTABLES);
    BiomeMaterials.PLANTS.forEach(
        (name, habitat) -> {
          if (!habitat.equals("heat") && !habitat.equals("mineral"))
            compost.add(BiomeMaterials.id(name), new Compostable(.3f), false);
        });
    compost.add(BiomeMaterials.id("rootwood_leaves"), new Compostable(.3f), false);
    compost.add(BiomeMaterials.id("rootwood_sapling"), new Compostable(.3f), false);
    compost.add(BiomeMaterials.id("alien_leaves"), new Compostable(.3f), false);
    compost.add(BiomeMaterials.id("alien_sapling"), new Compostable(.3f), false);
    builder(NeoForgeDataMaps.FURNACE_FUELS)
        .add(BiomeMaterials.id("charcoal_log"), new FurnaceFuel(1600), false);
    builder(DataMaps.PROCESSING_TIME)
        .add(id("adv_rocketry:gold_coil"), .9f, false)
        .add(id("adv_rocketry:aluminum_coil"), .8f, false)
        .add(id("adv_rocketry:titanium_coil"), .75f, false)
        .add(id("adv_rocketry:iridium_coil"), .5f, false);
    var fuel = builder(DataMaps.BLACK_HOLE_FUEL);
    for (String item :
        new String[] {
          "minecraft:stone", "minecraft:dirt", "minecraft:netherrack", "minecraft:cobblestone"
        }) fuel.add(id(item), new DataMaps.BlackHoleFuel(1), false);
    builder(DataMaps.ROCKET_PROPELLANT)
        .add(
            ProcessingFluids.ROCKET_FUEL,
            new DataMaps.Propellant(
                Optional.of(2f), Optional.empty(), Optional.empty(), Optional.empty()),
            false)
        .add(
            ProcessingFluids.HYDROGEN,
            new DataMaps.Propellant(
                Optional.empty(), Optional.of(1f), Optional.empty(), Optional.of(1f)),
            false)
        .add(
            ProcessingFluids.OXYGEN,
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
            new DataMaps.SatelliteModule(32, 0, 0),
            false)
        .add(
            id("adv_rocketry:large_satellite_solar_panel"),
            new DataMaps.SatelliteModule(160, 0, 0),
            false)
        .add(
            id("adv_rocketry:horizon_satellite_module"),
            new DataMaps.SatelliteModule(0, 0, 0, 15),
            false)
        .add(id("adv_rocketry:battery"), new DataMaps.SatelliteModule(0, 10000, 0), false)
        .add(id("adv_rocketry:advanced_battery"), new DataMaps.SatelliteModule(0, 40000, 0), false)
        .add(id("adv_rocketry:item_data_storage"), new DataMaps.SatelliteModule(0, 0, 1000), false);
  }
}

package advRocketry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class DataMaps {
  private static final Codec<Float> POSITIVE = Codec.floatRange(Float.MIN_NORMAL, Float.MAX_VALUE);

  public record BlackHoleFuel(int burnTime) {
    public static final Codec<BlackHoleFuel> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ExtraCodecs.POSITIVE_INT
                            .fieldOf("burn_time")
                            .forGetter(BlackHoleFuel::burnTime))
                    .apply(instance, BlackHoleFuel::new));
  }

  public record Propellant(
      Optional<Float> monopropellant,
      Optional<Float> bipropellant,
      Optional<Float> oxidizer,
      Optional<Float> nuclear) {
    public static final Codec<Propellant> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        POSITIVE
                            .optionalFieldOf("monopropellant")
                            .forGetter(Propellant::monopropellant),
                        POSITIVE
                            .optionalFieldOf("bipropellant")
                            .forGetter(Propellant::bipropellant),
                        POSITIVE.optionalFieldOf("oxidizer").forGetter(Propellant::oxidizer),
                        POSITIVE.optionalFieldOf("nuclear").forGetter(Propellant::nuclear))
                    .apply(instance, Propellant::new));
  }

  public record GasGiantGas(float minGravity, float maxGravity, float chance) {
    public static final Codec<GasGiantGas> CODEC =
        RecordCodecBuilder.<GasGiantGas>create(
                instance ->
                    instance
                        .group(
                            Codec.floatRange(0, Float.MAX_VALUE)
                                .optionalFieldOf("min_gravity", 0f)
                                .forGetter(GasGiantGas::minGravity),
                            Codec.floatRange(0, Float.MAX_VALUE)
                                .optionalFieldOf("max_gravity", 16f)
                                .forGetter(GasGiantGas::maxGravity),
                            Codec.floatRange(0, 1)
                                .optionalFieldOf("chance", 1f)
                                .forGetter(GasGiantGas::chance))
                        .apply(instance, GasGiantGas::new))
            .validate(
                gas ->
                    gas.maxGravity < gas.minGravity
                        ? DataResult.error(() -> "max_gravity is below min_gravity")
                        : DataResult.success(gas));
  }

  public record SatelliteModule(int powerGeneration, int energyStorage, int dataStorage) {
    public static final Codec<SatelliteModule> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("power_generation", 0)
                            .forGetter(SatelliteModule::powerGeneration),
                        ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("energy_storage", 0)
                            .forGetter(SatelliteModule::energyStorage),
                        ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("data_storage", 0)
                            .forGetter(SatelliteModule::dataStorage))
                    .apply(instance, SatelliteModule::new));
  }

  public static final DataMapType<Item, BlackHoleFuel> BLACK_HOLE_FUEL =
      type("black_hole_fuel", Registries.ITEM, BlackHoleFuel.CODEC);
  public static final DataMapType<Fluid, Propellant> ROCKET_PROPELLANT =
      type("rocket_propellant", Registries.FLUID, Propellant.CODEC);
  public static final DataMapType<Fluid, GasGiantGas> GAS_GIANT_GAS =
      type("gas_giant_gas", Registries.FLUID, GasGiantGas.CODEC);
  public static final DataMapType<Item, SatelliteModule> SATELLITE_MODULE =
      type("satellite_module", Registries.ITEM, SatelliteModule.CODEC);

  private DataMaps() {}

  private static <R, T> DataMapType<R, T> type(
      String name, ResourceKey<Registry<R>> registry, Codec<T> codec) {
    return DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(Main.MODID, name), registry, codec)
        .synced(codec, false)
        .build();
  }

  public static void register(IEventBus bus) {
    bus.addListener(DataMaps::types);
  }

  private static void types(RegisterDataMapTypesEvent event) {
    event.register(BLACK_HOLE_FUEL);
    event.register(ROCKET_PROPELLANT);
    event.register(GAS_GIANT_GAS);
    event.register(SATELLITE_MODULE);
  }
}

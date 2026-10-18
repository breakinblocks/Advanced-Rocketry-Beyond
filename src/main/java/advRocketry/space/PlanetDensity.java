package advRocketry.space;

import advRocketry.Main;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Serializable, immutable planet field. Explicit seed survives dynamic-dimension reloads. */
public record PlanetDensity(
    long seed,
    String profile,
    int sea,
    boolean channels,
    boolean caves,
    String field,
    boolean watersheds,
    double gravity,
    boolean hostileGeology)
    implements DensityFunction.SimpleFunction {
  public PlanetDensity(
      long seed, String profile, int sea, boolean channels, boolean caves, String field) {
    this(seed, profile, sea, channels, caves, field, false, 1, false);
  }

  public PlanetDensity(
      long seed,
      String profile,
      int sea,
      boolean channels,
      boolean caves,
      String field,
      boolean watersheds,
      double gravity) {
    this(seed, profile, sea, channels, caves, field, watersheds, gravity, false);
  }

  public static final Codec<String> PROFILE_CODEC =
      names("temperate", "volcanic", "lunar", "fractured", "cavern", "arid", "storm", "glacial");

  private static Codec<String> names(String... allowed) {
    return Codec.STRING.validate(
        value ->
            List.of(allowed).contains(value)
                ? DataResult.success(value)
                : DataResult.error(() -> "Unknown planet field option: " + value));
  }

  public static final MapCodec<PlanetDensity> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(
                      Codec.LONG.fieldOf("seed").forGetter(PlanetDensity::seed),
                      PROFILE_CODEC.fieldOf("profile").forGetter(PlanetDensity::profile),
                      Codec.intRange(-63, 319).fieldOf("sea_level").forGetter(PlanetDensity::sea),
                      Codec.BOOL.fieldOf("channels").forGetter(PlanetDensity::channels),
                      Codec.BOOL.fieldOf("caves").forGetter(PlanetDensity::caves),
                      names(
                              "temperature",
                              "moisture",
                              "continents",
                              "erosion",
                              "depth",
                              "ridges",
                              "surface",
                              "density")
                          .fieldOf("field")
                          .forGetter(PlanetDensity::field),
                      Codec.BOOL
                          .optionalFieldOf("watersheds", false)
                          .forGetter(PlanetDensity::watersheds),
                      Codec.doubleRange(0, Double.MAX_VALUE)
                          .optionalFieldOf("gravity", 1d)
                          .forGetter(PlanetDensity::gravity),
                      Codec.BOOL
                          .optionalFieldOf("hostile_geology", false)
                          .forGetter(PlanetDensity::hostileGeology))
                  .apply(i, PlanetDensity::new));
  private static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
      DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Main.MODID);

  static {
    TYPES.register("planet_field", () -> CODEC);
  }

  public static void register(IEventBus bus) {
    TYPES.register(bus);
  }

  @Override
  public double compute(FunctionContext c) {
    int x = c.blockX(), y = c.blockY(), z = c.blockZ();
    return switch (field) {
      case "temperature" -> PlanetLandforms.temperature(seed, x, z);
      case "moisture" -> PlanetLandforms.moisture(seed, x, z);
      case "continents" -> PlanetLandforms.continents(seed, x, z);
      case "erosion" -> PlanetLandforms.field(seed, 29, x, z, 370);
      case "ridges" -> PlanetLandforms.channel(seed, x, z);
      case "depth" ->
          (hostileGeology && profile.equals("fractured")
                  ? PlanetFractures.depth(seed, sea, x, y, z)
                  : height(x, z) - y)
              / 80;
      case "surface" -> (height(x, z) - y) / 22;
      default ->
          hostileGeology
              ? PlanetGeology.density(seed, profile, sea, gravity, caves, x, y, z)
              : watersheds
                  ? PlanetWatershed.density(seed, sea, gravity, caves, x, y, z)
                  : PlanetLandforms.density(seed, profile, sea, channels, caves, x, y, z);
    };
  }

  private double height(int x, int z) {
    return hostileGeology
        ? PlanetGeology.height(seed, profile, sea, gravity, x, z)
        : watersheds
            ? PlanetWatershed.height(seed, sea, gravity, x, z)
            : PlanetLandforms.height(seed, profile, sea, channels, x, z);
  }

  @Override
  public double minValue() {
    return -64;
  }

  @Override
  public double maxValue() {
    return 64;
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return KeyDispatchDataCodec.of(CODEC);
  }
}

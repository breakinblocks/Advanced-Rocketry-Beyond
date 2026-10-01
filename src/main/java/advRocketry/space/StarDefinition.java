package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;

public record StarDefinition(
    String name,
    int temperature,
    float size,
    int x,
    int z,
    boolean blackHole,
    List<Companion> companions,
    int terrestrialPlanets,
    int gasGiants) {
  private static final Codec<Float> SIZE = Codec.floatRange(Float.MIN_NORMAL, Float.MAX_VALUE);

  public record Companion(
      Optional<String> name, int temperature, float size, boolean blackHole, float separation) {
    public static final Codec<Companion> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.STRING.optionalFieldOf("name").forGetter(Companion::name),
                        ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("temperature", 100)
                            .forGetter(Companion::temperature),
                        SIZE.optionalFieldOf("size", 1f).forGetter(Companion::size),
                        Codec.BOOL
                            .optionalFieldOf("black_hole", false)
                            .forGetter(Companion::blackHole),
                        Codec.floatRange(0, Float.MAX_VALUE)
                            .optionalFieldOf("separation", 5f)
                            .forGetter(Companion::separation))
                    .apply(instance, Companion::new));
  }

  public static final Codec<StarDefinition> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("name").forGetter(StarDefinition::name),
                      ExtraCodecs.NON_NEGATIVE_INT
                          .optionalFieldOf("temperature", 100)
                          .forGetter(StarDefinition::temperature),
                      SIZE.optionalFieldOf("size", 1f).forGetter(StarDefinition::size),
                      Codec.INT.optionalFieldOf("x", 0).forGetter(StarDefinition::x),
                      Codec.INT.optionalFieldOf("z", 0).forGetter(StarDefinition::z),
                      Codec.BOOL
                          .optionalFieldOf("black_hole", false)
                          .forGetter(StarDefinition::blackHole),
                      Companion.CODEC
                          .listOf()
                          .optionalFieldOf("companions", List.of())
                          .forGetter(StarDefinition::companions),
                      ExtraCodecs.NON_NEGATIVE_INT
                          .optionalFieldOf("generated_planets", 0)
                          .forGetter(StarDefinition::terrestrialPlanets),
                      ExtraCodecs.NON_NEGATIVE_INT
                          .optionalFieldOf("generated_gas_giants", 0)
                          .forGetter(StarDefinition::gasGiants))
                  .apply(instance, StarDefinition::new));

  public Star create(int id) {
    List<Star.Companion> resolved = new ArrayList<>();
    for (Companion companion : companions)
      resolved.add(
          new Star.Companion(
              companion.name().orElse(name + "-" + (resolved.size() + 1)),
              companion.temperature(),
              companion.size(),
              companion.blackHole(),
              companion.separation()));
    return new Star(id, name, temperature, size, x, z, blackHole, resolved);
  }
}

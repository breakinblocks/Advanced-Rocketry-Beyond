package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AsteroidResearch(int optical, int composition, int mass) {
  public static final AsteroidResearch EMPTY = new AsteroidResearch(0, 0, 0);
  public static final Codec<AsteroidResearch> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.fieldOf("optical").forGetter(AsteroidResearch::optical),
                      Codec.INT.fieldOf("composition").forGetter(AsteroidResearch::composition),
                      Codec.INT.fieldOf("mass").forGetter(AsteroidResearch::mass))
                  .apply(instance, AsteroidResearch::new));
  public static final StreamCodec<ByteBuf, AsteroidResearch> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          AsteroidResearch::optical,
          ByteBufCodecs.VAR_INT,
          AsteroidResearch::composition,
          ByteBufCodecs.VAR_INT,
          AsteroidResearch::mass,
          AsteroidResearch::new);

  public int get(ResearchType type) {
    return switch (type) {
      case OPTICAL -> optical;
      case COMPOSITION -> composition;
      case MASS -> mass;
      default -> 0;
    };
  }

  public AsteroidResearch with(ResearchType type, int value) {
    return switch (type) {
      case OPTICAL -> new AsteroidResearch(value, composition, mass);
      case COMPOSITION -> new AsteroidResearch(optical, value, mass);
      case MASS -> new AsteroidResearch(optical, composition, value);
      default -> this;
    };
  }
}

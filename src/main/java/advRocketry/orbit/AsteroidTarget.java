package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AsteroidTarget(String type, long seed) {
  public static final AsteroidTarget NONE = new AsteroidTarget("", 0);
  public static final Codec<AsteroidTarget> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("type").forGetter(AsteroidTarget::type),
                      Codec.LONG.fieldOf("seed").forGetter(AsteroidTarget::seed))
                  .apply(instance, AsteroidTarget::new));
  public static final StreamCodec<ByteBuf, AsteroidTarget> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8,
          AsteroidTarget::type,
          ByteBufCodecs.VAR_LONG,
          AsteroidTarget::seed,
          AsteroidTarget::new);
}

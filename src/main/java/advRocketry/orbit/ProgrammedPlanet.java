package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProgrammedPlanet(int id, String name) {
  public static final Codec<ProgrammedPlanet> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.fieldOf("id").forGetter(ProgrammedPlanet::id),
                      Codec.STRING.fieldOf("name").forGetter(ProgrammedPlanet::name))
                  .apply(instance, ProgrammedPlanet::new));
  public static final StreamCodec<ByteBuf, ProgrammedPlanet> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          ProgrammedPlanet::id,
          ByteBufCodecs.STRING_UTF8,
          ProgrammedPlanet::name,
          ProgrammedPlanet::new);
}

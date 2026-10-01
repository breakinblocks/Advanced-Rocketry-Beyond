package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SatelliteProperties(
    long id, SatelliteType type, int powerGeneration, int powerStorage, int maxData) {
  public static final Codec<SatelliteProperties> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("id").forGetter(SatelliteProperties::id),
                      SatelliteType.CODEC.fieldOf("type").forGetter(SatelliteProperties::type),
                      Codec.INT
                          .fieldOf("power_generation")
                          .forGetter(SatelliteProperties::powerGeneration),
                      Codec.INT
                          .fieldOf("power_storage")
                          .forGetter(SatelliteProperties::powerStorage),
                      Codec.INT.fieldOf("max_data").forGetter(SatelliteProperties::maxData))
                  .apply(instance, SatelliteProperties::new));
  public static final StreamCodec<ByteBuf, SatelliteProperties> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_LONG,
          SatelliteProperties::id,
          SatelliteType.STREAM_CODEC,
          SatelliteProperties::type,
          ByteBufCodecs.VAR_INT,
          SatelliteProperties::powerGeneration,
          ByteBufCodecs.VAR_INT,
          SatelliteProperties::powerStorage,
          ByteBufCodecs.VAR_INT,
          SatelliteProperties::maxData,
          SatelliteProperties::new);

  public static SatelliteProperties of(Satellite satellite) {
    return new SatelliteProperties(
        satellite.id,
        satellite.type,
        satellite.powerGeneration,
        satellite.powerStorage,
        satellite.maxData);
  }
}

package advRocketry.life;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record JetpackSettings(boolean enabled, boolean hover) {
  public static final JetpackSettings OFF = new JetpackSettings(false, false);
  public static final Codec<JetpackSettings> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.fieldOf("enabled").forGetter(JetpackSettings::enabled),
                      Codec.BOOL.fieldOf("hover").forGetter(JetpackSettings::hover))
                  .apply(instance, JetpackSettings::new));
  public static final StreamCodec<ByteBuf, JetpackSettings> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL,
          JetpackSettings::enabled,
          ByteBufCodecs.BOOL,
          JetpackSettings::hover,
          JetpackSettings::new);

  public JetpackSettings toggled(boolean hoverMode) {
    return hoverMode ? new JetpackSettings(enabled, !hover) : new JetpackSettings(!enabled, hover);
  }

  public boolean get(boolean hoverMode) {
    return hoverMode ? hover : enabled;
  }
}

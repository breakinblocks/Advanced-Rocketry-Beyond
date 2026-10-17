package advRocketry.client.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Resource-pack controls; geometry budgets cannot be raised beyond the client limits. */
public record EffectProfile(boolean enabled, int color, float opacity, float speed, int segments) {
  public static final EffectProfile DEFAULT = new EffectProfile(true, 0x70efda, .65f, 1, 32);
  public static final Codec<EffectProfile> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.optionalFieldOf("enabled", true).forGetter(EffectProfile::enabled),
                      Codec.intRange(0, 0xffffff)
                          .optionalFieldOf("color", 0x70efda)
                          .forGetter(EffectProfile::color),
                      Codec.floatRange(0, 1)
                          .optionalFieldOf("opacity", .65f)
                          .forGetter(EffectProfile::opacity),
                      Codec.floatRange(0, 4)
                          .optionalFieldOf("speed", 1f)
                          .forGetter(EffectProfile::speed),
                      Codec.intRange(8, 64)
                          .optionalFieldOf("segments", 32)
                          .forGetter(EffectProfile::segments))
                  .apply(instance, EffectProfile::new));

  public int argb(float strength) {
    return Math.round(Math.clamp(strength * opacity, 0f, 1f) * 255) << 24 | color;
  }
}

package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HexFormat;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/** Cosmetic planet settings; never used by atmosphere or energy simulation. */
public record SkySettings(
    Profile profile,
    Clouds clouds,
    float haze,
    float sunset,
    float stars,
    float aurora,
    float nebula,
    int seed,
    float starDensity,
    float twinkle,
    float distortion,
    float galaxy) {
  public SkySettings(
      Profile profile,
      Clouds clouds,
      float haze,
      float sunset,
      float stars,
      float aurora,
      float nebula,
      int seed) {
    this(profile, clouds, haze, sunset, stars, aurora, nebula, seed, 1, .6f, .5f, .65f);
  }

  public enum Profile implements StringRepresentable {
    AUTO,
    CLEAR,
    TEMPERATE,
    DENSE,
    FROZEN,
    ASH;

    public static final Codec<Profile> CODEC = StringRepresentable.fromEnum(Profile::values);

    @Override
    public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  private static Codec<Float> range(float min, float max) {
    return Codec.FLOAT.validate(
        value ->
            Float.isFinite(value) && value >= min && value <= max
                ? DataResult.success(value)
                : DataResult.error(
                    () -> "Sky value must be finite and between " + min + " and " + max));
  }

  private static Codec<Integer> color() {
    return Codec.withAlternative(
        Codec.intRange(-1, 0xffffff),
        Codec.STRING.comapFlatMap(
            text -> {
              String hex = text.startsWith("#") ? text.substring(1) : text;
              try {
                return hex.length() == 6
                    ? DataResult.success(HexFormat.fromHexDigits(hex))
                    : DataResult.error(() -> "Cloud color must be #RRGGBB");
              } catch (IllegalArgumentException invalid) {
                return DataResult.error(() -> "Cloud color must be #RRGGBB");
              }
            },
            value -> String.format(Locale.ROOT, "#%06x", value)));
  }

  public record Clouds(float coverage, int color, float height, float speed, float direction) {
    public static final Clouds DEFAULT = new Clouds(-1, -1, 192, .35f, 45);
    public static final Codec<Clouds> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        range(-1, 1).optionalFieldOf("coverage", -1f).forGetter(Clouds::coverage),
                        SkySettings.color().optionalFieldOf("color", -1).forGetter(Clouds::color),
                        range(64, 512).optionalFieldOf("height", 192f).forGetter(Clouds::height),
                        range(0, 4).optionalFieldOf("speed", .35f).forGetter(Clouds::speed),
                        range(0, 360)
                            .optionalFieldOf("direction", 45f)
                            .forGetter(Clouds::direction))
                    .apply(instance, Clouds::new));
  }

  public static final SkySettings DEFAULT =
      new SkySettings(Profile.AUTO, Clouds.DEFAULT, 1, 1, 1, 0, 0, 0);
  public static final Codec<SkySettings> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Profile.CODEC
                          .optionalFieldOf("profile", Profile.AUTO)
                          .forGetter(SkySettings::profile),
                      Clouds.CODEC
                          .optionalFieldOf("clouds", Clouds.DEFAULT)
                          .forGetter(SkySettings::clouds),
                      range(0, 2).optionalFieldOf("haze", 1f).forGetter(SkySettings::haze),
                      range(0, 2).optionalFieldOf("sunset", 1f).forGetter(SkySettings::sunset),
                      range(0, 2).optionalFieldOf("stars", 1f).forGetter(SkySettings::stars),
                      range(0, 1).optionalFieldOf("aurora", 0f).forGetter(SkySettings::aurora),
                      range(0, 1).optionalFieldOf("nebula", 0f).forGetter(SkySettings::nebula),
                      Codec.INT.optionalFieldOf("seed", 0).forGetter(SkySettings::seed),
                      range(0, 4)
                          .optionalFieldOf("star_density", 1f)
                          .forGetter(SkySettings::starDensity),
                      range(0, 1).optionalFieldOf("twinkle", .6f).forGetter(SkySettings::twinkle),
                      range(0, 1)
                          .optionalFieldOf("distortion", .5f)
                          .forGetter(SkySettings::distortion),
                      range(0, 2).optionalFieldOf("galaxy", .65f).forGetter(SkySettings::galaxy))
                  .apply(instance, SkySettings::new));
}

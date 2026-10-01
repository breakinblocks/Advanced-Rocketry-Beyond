package advRocketry.orbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum SatelliteType implements StringRepresentable {
  OPTICAL("optical", ResearchType.OPTICAL),
  COMPOSITION("composition", ResearchType.COMPOSITION),
  MASS("mass", ResearchType.MASS),
  DENSITY("density", ResearchType.ATMOSPHERE_DENSITY),
  SOLAR_ENERGY("solar_energy", null),
  ORE_SCANNER("ore_scanner", null),
  BIOME_CHANGER("biome_changer", null);

  public static final Codec<SatelliteType> CODEC =
      StringRepresentable.fromEnum(SatelliteType::values);
  public static final StreamCodec<ByteBuf, SatelliteType> STREAM_CODEC =
      ByteBufCodecs.idMapper(index -> values()[index], Enum::ordinal);

  private final String name;
  private final ResearchType research;

  SatelliteType(String name, ResearchType research) {
    this.name = name;
    this.research = research;
  }

  public ResearchType research() {
    return research;
  }

  public boolean producesData() {
    return research != null;
  }

  public Component displayName() {
    return Component.translatable("satellite_type.adv_rocketry." + name);
  }

  public static SatelliteType byName(String name) {
    for (SatelliteType type : values()) if (type.name.equals(name)) return type;
    return null;
  }

  @Override
  public String getSerializedName() {
    return name;
  }
}

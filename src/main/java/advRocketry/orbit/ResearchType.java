package advRocketry.orbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ResearchType implements StringRepresentable {
  OPTICAL("optical"),
  COMPOSITION("composition"),
  MASS("mass"),
  ATMOSPHERE_DENSITY("atmosphere_density"),
  HUMIDITY("humidity"),
  TEMPERATURE("temperature");

  public static final ResearchType DISTANCE = OPTICAL;
  public static final int UNIT_CAPACITY = 1000;
  public static final List<ResearchType> ASTEROID_TYPES = List.of(OPTICAL, COMPOSITION, MASS);
  public static final List<ResearchType> TRANSCEIVER_TYPES =
      List.of(HUMIDITY, TEMPERATURE, COMPOSITION, ATMOSPHERE_DENSITY, MASS, OPTICAL);
  public static final Codec<ResearchType> CODEC =
      StringRepresentable.fromEnum(ResearchType::values);
  public static final StreamCodec<ByteBuf, ResearchType> STREAM_CODEC =
      ByteBufCodecs.idMapper(index -> values()[index], Enum::ordinal);

  private final String id;

  ResearchType(String id) {
    this.id = id;
  }

  public Component displayName() {
    return Component.translatable("research_type.adv_rocketry." + id);
  }

  public static ResearchType byId(String id) {
    for (ResearchType type : values()) if (type.id.equals(id)) return type;
    return null;
  }

  @Override
  public String getSerializedName() {
    return id;
  }
}

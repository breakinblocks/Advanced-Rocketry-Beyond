package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

public enum TerrainType implements StringRepresentable {
  TERRESTRIAL("terrestrial"),
  ASTEROID("asteroid"),
  CAVE("cave"),
  MOON("moon"),
  VOLCANIC("volcanic"),
  SPACE("space");

  public static final Codec<TerrainType> CODEC = StringRepresentable.fromEnum(TerrainType::values);
  public static final Codec<TerrainType> DEFINITION_CODEC =
      CODEC.validate(
          type ->
              type == SPACE
                  ? DataResult.error(() -> "Planets cannot use the space terrain")
                  : DataResult.success(type));

  private final String id;

  TerrainType(String id) {
    this.id = id;
  }

  public static TerrainType byId(String id) {
    for (TerrainType type : values()) if (type.id.equals(id)) return type;
    return TERRESTRIAL;
  }

  @Override
  public String getSerializedName() {
    return id;
  }
}

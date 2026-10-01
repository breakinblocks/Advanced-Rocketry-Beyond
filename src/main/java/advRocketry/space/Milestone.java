package advRocketry.space;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum Milestone implements StringRepresentable {
  ROCKET_LAUNCH,
  MOON_LANDING,
  FIRST_MOON_LANDING,
  APOLLO_SITE,
  WARP_FLIGHT,
  FIRST_WARP_FLIGHT,
  SEALED_ROOM,
  PLANET_DISCOVERY,
  SEAT_ON_TNT;

  public static final Codec<Milestone> CODEC = StringRepresentable.fromEnum(Milestone::values);

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ROOT);
  }
}

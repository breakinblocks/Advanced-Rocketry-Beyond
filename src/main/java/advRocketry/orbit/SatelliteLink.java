package advRocketry.orbit;

import advRocketry.ModComponents;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SatelliteLink(long id) {
  public static final Codec<SatelliteLink> CODEC =
      Codec.LONG.xmap(SatelliteLink::new, SatelliteLink::id);
  public static final StreamCodec<ByteBuf, SatelliteLink> STREAM_CODEC =
      ByteBufCodecs.VAR_LONG.map(SatelliteLink::new, SatelliteLink::id);

  public static long read(ItemStack stack) {
    SatelliteLink link = stack.get(ModComponents.SATELLITE_LINK);
    return link == null ? 0 : link.id();
  }
}

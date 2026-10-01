package advRocketry.orbit;

import advRocketry.ModComponents;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record StationLink(long id) {
  public static final Codec<StationLink> CODEC = Codec.LONG.xmap(StationLink::new, StationLink::id);
  public static final StreamCodec<ByteBuf, StationLink> STREAM_CODEC =
      ByteBufCodecs.VAR_LONG.map(StationLink::new, StationLink::id);

  public static long read(ItemStack stack) {
    StationLink link = stack.get(ModComponents.STATION_LINK);
    return link == null ? 0 : link.id();
  }
}

package advRocketry.orbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BiomeSelection(ResourceLocation biome) {
  public static final Codec<BiomeSelection> CODEC =
      ResourceLocation.CODEC.xmap(BiomeSelection::new, BiomeSelection::biome);
  public static final StreamCodec<ByteBuf, BiomeSelection> STREAM_CODEC =
      ResourceLocation.STREAM_CODEC.map(BiomeSelection::new, BiomeSelection::biome);
}

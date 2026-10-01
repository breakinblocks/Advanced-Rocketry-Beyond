package advRocketry.processing;

import advRocketry.multiblock.Multiblocks;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ProjectorChoice(ResourceLocation machine) {
  public static final ProjectorChoice DEFAULT = new ProjectorChoice(Multiblocks.id("lathe"));
  public static final Codec<ProjectorChoice> CODEC =
      ResourceLocation.CODEC.xmap(ProjectorChoice::new, ProjectorChoice::machine);
  public static final StreamCodec<ByteBuf, ProjectorChoice> STREAM_CODEC =
      ResourceLocation.STREAM_CODEC.map(ProjectorChoice::new, ProjectorChoice::machine);
}

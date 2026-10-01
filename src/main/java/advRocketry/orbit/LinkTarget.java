package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LinkTarget(Optional<GlobalPos> position, Optional<UUID> rocket) {
  public static final LinkTarget EMPTY = new LinkTarget(Optional.empty(), Optional.empty());
  public static final Codec<LinkTarget> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      GlobalPos.CODEC.optionalFieldOf("position").forGetter(LinkTarget::position),
                      UUIDUtil.CODEC.optionalFieldOf("rocket").forGetter(LinkTarget::rocket))
                  .apply(instance, LinkTarget::new));
  public static final StreamCodec<ByteBuf, LinkTarget> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
          LinkTarget::position,
          ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
          LinkTarget::rocket,
          LinkTarget::new);

  public LinkTarget withPosition(GlobalPos value) {
    return new LinkTarget(Optional.of(value), rocket);
  }

  public LinkTarget withoutPosition() {
    return new LinkTarget(Optional.empty(), rocket);
  }

  public LinkTarget withRocket(UUID value) {
    return new LinkTarget(position, Optional.of(value));
  }

  public LinkTarget withoutRocket() {
    return new LinkTarget(position, Optional.empty());
  }
}

package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StoredResearch(ResearchType type, int amount) {
  public static final StoredResearch EMPTY = new StoredResearch(null, 0);
  public static final Codec<StoredResearch> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      ResearchType.CODEC
                          .optionalFieldOf("type")
                          .forGetter(research -> Optional.ofNullable(research.type)),
                      Codec.INT.fieldOf("amount").forGetter(StoredResearch::amount))
                  .apply(
                      instance, (type, amount) -> new StoredResearch(type.orElse(null), amount)));
  public static final StreamCodec<ByteBuf, StoredResearch> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.optional(ResearchType.STREAM_CODEC),
          research -> Optional.ofNullable(research.type),
          ByteBufCodecs.VAR_INT,
          StoredResearch::amount,
          (type, amount) -> new StoredResearch(type.orElse(null), amount));

  public boolean empty() {
    return type == null;
  }

  public int amount(ResearchType requested) {
    return type == requested ? amount : 0;
  }
}

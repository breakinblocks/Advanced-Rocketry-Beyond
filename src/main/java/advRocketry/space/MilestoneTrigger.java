package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public final class MilestoneTrigger
    extends SimpleCriterionTrigger<MilestoneTrigger.TriggerInstance> {
  @Override
  public Codec<TriggerInstance> codec() {
    return TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer player, Milestone milestone) {
    trigger(player, instance -> instance.milestone() == milestone);
  }

  public record TriggerInstance(Optional<ContextAwarePredicate> player, Milestone milestone)
      implements SimpleCriterionTrigger.SimpleInstance {
    public static final Codec<TriggerInstance> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.ADVANCEMENT_CODEC
                            .optionalFieldOf("player")
                            .forGetter(TriggerInstance::player),
                        Milestone.CODEC.fieldOf("milestone").forGetter(TriggerInstance::milestone))
                    .apply(instance, TriggerInstance::new));
  }
}

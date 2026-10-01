// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.Main;
import advRocketry.rocket.RocketRegistry;
import java.util.function.Supplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Awards the original travel milestones from completed gameplay events. */
public final class AdvancementLogic {
  private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
      DeferredRegister.create(Registries.TRIGGER_TYPE, Main.MODID);
  public static final Supplier<MilestoneTrigger> MILESTONE =
      TRIGGERS.register("milestone", MilestoneTrigger::new);

  private AdvancementLogic() {}

  public static void register(IEventBus bus) {
    TRIGGERS.register(bus);
  }

  public static void grant(ServerPlayer player, Milestone milestone) {
    MILESTONE.get().trigger(player, milestone);
  }

  public static void seatOnTnt(PlayerInteractEvent.RightClickBlock event) {
    if (!(event.getEntity() instanceof ServerPlayer player)
        || !event.getLevel().getBlockState(event.getPos()).is(Blocks.TNT)
        || !event.getItemStack().is(RocketRegistry.PARTS.get("seat").get().asItem())) return;
    grant(player, Milestone.SEAT_ON_TNT);
  }
}

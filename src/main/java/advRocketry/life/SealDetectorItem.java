// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Reports the same airtightness predicate used by the room flood-fill. */
public final class SealDetectorItem extends Item {
  public SealDetectorItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (context.getPlayer() instanceof ServerPlayer player) {
      boolean sealed = SealedRooms.seals(context.getLevel(), context.getClickedPos());
      player.displayClientMessage(
          Component.translatable(
              sealed ? "message.adv_rocketry.sealed" : "message.adv_rocketry.unsealed"),
          true);
    }
    return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
  }
}

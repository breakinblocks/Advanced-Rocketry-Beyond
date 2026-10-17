// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Runs before vanilla sneak-use bypasses block interaction and tries to empty the bucket. */
public final class TransportInteraction {
  private TransportInteraction() {}

  public static void bucket(PlayerInteractEvent.RightClickBlock event) {
    var player = event.getEntity();
    var level = event.getLevel();
    var stack = event.getItemStack();
    if (!player.isSpectator()
        && player.mayBuild()
        && level.getBlockEntity(event.getPos()) instanceof TransportBlockEntity configured) {
      boolean wrench = stack.is(Tags.Items.TOOLS_WRENCH);
      boolean dye = stack.getItem() instanceof DyeItem;
      boolean clear = player.isShiftKeyDown() && stack.is(Items.WATER_BUCKET);
      if (wrench || dye || clear && configured.kind() != TransportRegistry.Kind.FLUID) {
        if (!level.isClientSide) {
          if (wrench) {
            var side = event.getFace();
            if (side != null)
              configured.modes[side.ordinal()] =
                  configured.modes[side.ordinal()] == TransportBlockEntity.DISABLED
                      ? TransportBlockEntity.INSERT
                      : TransportBlockEntity.DISABLED;
          } else configured.channel = dye ? ((DyeItem) stack.getItem()).getDyeColor().getId() : -1;
          configured.changed();
          for (var side : Direction.values())
            if (level.hasChunkAt(event.getPos().relative(side))
                && level.getBlockEntity(event.getPos().relative(side))
                    instanceof TransportBlockEntity other) other.refreshConnections();
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
        return;
      }
    }
    if (!player.isShiftKeyDown()
        || player.isSpectator()
        || !player.mayBuild()
        || !(level.getBlockEntity(event.getPos()) instanceof TransportBlockEntity pipe)
        || pipe.kind() != TransportRegistry.Kind.FLUID) return;
    boolean empty = stack.is(Items.BUCKET);
    var fluid = FluidUtil.getFluidContained(stack);
    if (!empty && (!(stack.getItem() instanceof BucketItem) || fluid.isEmpty())) return;
    if (!level.isClientSide) {
      if (empty) TransportNetworks.voidFluid(pipe, player);
      else TransportNetworks.lockFluid(pipe, fluid.orElseThrow(), player);
    }
    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    event.setCanceled(true);
  }
}

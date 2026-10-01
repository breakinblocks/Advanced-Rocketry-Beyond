// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Runs before vanilla sneak-use bypasses block interaction and tries to empty the bucket. */
public final class TransportInteraction {
  private TransportInteraction() {}

  public static void bucket(PlayerInteractEvent.RightClickBlock event) {
    var player = event.getEntity();
    var level = event.getLevel();
    var stack = event.getItemStack();
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

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Named station landing location with a return-flight linker slot. */
public final class LandingPadBlock extends Block implements EntityBlock {
  public LandingPadBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LandingPadBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == OrbitalRegistry.LANDING_PAD_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((LandingPadBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(stack.getItem() instanceof LinkerItem) && !stack.is(Items.NAME_TAG))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (!level.isClientSide && level.getBlockEntity(pos) instanceof LandingPadBlockEntity pad) {
      if (stack.getItem() instanceof LinkerItem && pad.linker.getStackInSlot(0).isEmpty()) {
        pad.linker.setStackInSlot(0, stack.copyWithCount(1));
        if (!player.getAbilities().instabuild) stack.shrink(1);
      } else if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
        pad.rename(stack.getHoverName().getString());
      }
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof LandingPadBlockEntity pad) {
      if (player.isShiftKeyDown()) {
        ItemStack removed = pad.linker.extractItem(0, 1, false);
        if (!removed.isEmpty() && !player.addItem(removed)) player.drop(removed, false);
      } else
        serverPlayer.displayClientMessage(
            Texts.translate(
                "message.adv_rocketry.landing_pad_block.landing_pad",
                pad.name.isBlank()
                    ? Component.translatable("message.adv_rocketry.landing_pad.unnamed")
                    : Component.literal(pad.name)),
            true);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
    if (!state.is(next.getBlock())
        && level.getBlockEntity(pos) instanceof LandingPadBlockEntity pad) {
      pad.unregister();
      if (!level.isClientSide) popResource(level, pos, pad.linker.extractItem(0, 1, false));
    }
    super.onRemove(state, level, pos, next, moving);
  }
}

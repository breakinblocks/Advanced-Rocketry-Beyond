// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** A vent-adjacent cartridge hatch from the original oxygen system. */
public final class CarbonScrubberBlock extends Block implements EntityBlock {
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

  public CarbonScrubberBlock(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(POWERED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(POWERED);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CarbonScrubberBlockEntity(pos, state);
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
    if (!stack.is(ProcessingRegistry.PART_ITEMS.get("carbon_scrubber_cartridge").get()))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof CarbonScrubberBlockEntity scrubber) {
      if (scrubber.cartridge.getStackInSlot(0).isEmpty()) {
        scrubber.cartridge.setStackInSlot(0, stack.copyWithCount(1));
        if (!player.getAbilities().instabuild) stack.shrink(1);
      }
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer
        && level.getBlockEntity(pos) instanceof CarbonScrubberBlockEntity scrubber) {
      ItemStack removed = scrubber.cartridge.extractItem(0, 1, false);
      if (!removed.isEmpty() && !player.addItem(removed)) player.drop(removed, false);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
    if (!state.is(next.getBlock())
        && !level.isClientSide
        && level.getBlockEntity(pos) instanceof CarbonScrubberBlockEntity scrubber)
      popResource(level, pos, scrubber.cartridge.extractItem(0, 1, false));
    super.onRemove(state, level, pos, next, moving);
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof CarbonScrubberBlockEntity scrubber
        ? scrubber.comparatorOutput()
        : 0;
  }
}

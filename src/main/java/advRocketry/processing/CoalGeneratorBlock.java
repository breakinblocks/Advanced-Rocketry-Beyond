// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Single-slot original LibVulpes solid-fuel generator. */
public final class CoalGeneratorBlock extends Block implements EntityBlock {
  public CoalGeneratorBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CoalGeneratorBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == MachinePorts.COAL_GENERATOR_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((CoalGeneratorBlockEntity) entity).tick()
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
    if (!(level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity generator)
        || !generator.accepts(stack))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (!level.isClientSide) {
      ItemStack offered = stack.copyWithCount(1);
      if (generator.fuel.insertItem(0, offered, true).isEmpty()) {
        generator.fuel.insertItem(0, offered, false);
        if (!player.getAbilities().instabuild) stack.shrink(1);
      }
    }
    return ItemInteractionResult.SUCCESS;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity generator) {
      if (player instanceof ServerPlayer serverPlayer)
        serverPlayer.openMenu(generator, buffer -> buffer.writeBlockPos(pos));
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    return InteractionResult.PASS;
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity generator
        ? generator.energyStored() * 15 / 10000
        : 0;
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
    if (state.getBlock() != replacement.getBlock()
        && level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity generator) {
      ItemStack fuel = generator.fuel.getStackInSlot(0).copy();
      generator.fuel.setStackInSlot(0, ItemStack.EMPTY);
      popResource(level, pos, fuel);
    }
    super.onRemove(state, level, pos, replacement, moved);
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;

/** Original 5-bucket rocket fueling station, powered at 30 FE per operation. */
public final class FuelingStationBlock extends RocketInfrastructureBlock {
  public FuelingStationBlock(Properties properties) {
    super(properties, RocketRegistry.FUELING_STATION_ENTITY);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(POWERED);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new FuelingStationBlockEntity(pos, state);
  }

  @Override
  protected IItemHandler contents(BlockEntity entity) {
    return entity instanceof FuelingStationBlockEntity station ? station.inventory : null;
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
    return FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())
        ? ItemInteractionResult.SUCCESS
        : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof FuelingStationBlockEntity entity)
      serverPlayer.openMenu(entity, buffer -> buffer.writeBlockPos(pos));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected boolean isSignalSource(BlockState state) {
    return true;
  }

  @Override
  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    return state.getValue(POWERED) ? 15 : 0;
  }
}

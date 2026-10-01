// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Original directional wireless data node with an in-world mode control. */
public final class WirelessTransceiverBlock extends Block implements EntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

  public WirelessTransceiverBlock(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new WirelessTransceiverBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == OrbitalRegistry.WIRELESS_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((WirelessTransceiverBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof WirelessTransceiverBlockEntity node) {
      node.cycleMode();
      serverPlayer.displayClientMessage(
          Texts.translate(
              "message.adv_rocketry.wireless_transceiver_block.wireless_data", node.mode()),
          true);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
    if (!state.is(next.getBlock())
        && level.getBlockEntity(pos) instanceof WirelessTransceiverBlockEntity node) node.unlink();
    super.onRemove(state, level, pos, next, moving);
  }
}

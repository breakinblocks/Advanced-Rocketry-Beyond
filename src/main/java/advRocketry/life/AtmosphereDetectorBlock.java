// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Selectable atmosphere detector with the original ten-tick redstone cadence. */
public final class AtmosphereDetectorBlock extends Block implements EntityBlock {
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

  public AtmosphereDetectorBlock(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(POWERED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(POWERED);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new AtmosphereDetectorBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == LifeSupportRegistry.DETECTOR_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((AtmosphereDetectorBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof AtmosphereDetectorBlockEntity detector) {
      detector.cycle();
      serverPlayer.displayClientMessage(
          Texts.translate(
              "message.adv_rocketry.atmosphere_detector_block.detecting", detector.target()),
          true);
    }
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

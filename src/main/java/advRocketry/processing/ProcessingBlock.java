// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class ProcessingBlock extends Block implements EntityBlock {
  public static final BooleanProperty FORMED = BooleanProperty.create("state");
  private final MachineType machine;

  public ProcessingBlock(MachineType machine, Properties properties) {
    super(properties);
    this.machine = machine;
    registerDefaultState(
        stateDefinition
            .any()
            .setValue(FORMED, false)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
  }

  public MachineType machine() {
    return machine;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FORMED, BlockStateProperties.HORIZONTAL_FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState()
        .setValue(
            BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof ProcessingBlockEntity machine) {
      if (machine.scanStructure())
        serverPlayer.openMenu(
            machine,
            buffer -> MachineMenu.writeOpeningData(buffer, pos, machine.menuPorts(), level));
      else
        serverPlayer.displayClientMessage(
            Texts.translate("message.adv_rocketry.processing_block.incomplete", getName()), true);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
    if (state.getBlock() != replacement.getBlock()
        && level.getBlockEntity(pos) instanceof ProcessingBlockEntity machine) machine.unform();
    super.onRemove(state, level, pos, replacement, moved);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ProcessingBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return type == ProcessingRegistry.BLOCK_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((ProcessingBlockEntity) entity).tick()
        : null;
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Native state used to hide the constituent blocks beneath the original machine model. */
public class MachinePartBlock extends Block {
  public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

  public MachinePartBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(ASSEMBLED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(ASSEMBLED);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return state.getValue(ASSEMBLED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
  }

  @Override
  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!state.getValue(ASSEMBLED)) return;
    if (MultiblockMachine.isPartClaimed(level, pos)) level.scheduleTick(pos, this, 40);
    else level.setBlock(pos, state.setValue(ASSEMBLED, false), Block.UPDATE_CLIENTS);
  }
}

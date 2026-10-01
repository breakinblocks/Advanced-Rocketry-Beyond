// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Original LibVulpes motor tiers multiply a formed machine's recipe time. */
public final class MotorBlock extends MachinePartBlock {
  public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
  private final float timeMultiplier;

  public MotorBlock(Properties properties, float timeMultiplier) {
    super(properties);
    this.timeMultiplier = timeMultiplier;
    registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  public float timeMultiplier() {
    return timeMultiplier;
  }
}

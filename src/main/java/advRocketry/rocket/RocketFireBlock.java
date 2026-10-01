// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

/** Short-lived exhaust fire with the original fast lateral ignition. */
public final class RocketFireBlock extends Block {
  public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);

  public RocketFireBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(AGE, 0));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE);
  }

  @Override
  protected void onPlace(
      BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
    if (!previous.is(this)) level.scheduleTick(pos, this, 2);
  }

  @Override
  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    int age = state.getValue(AGE);
    if (age >= 15) {
      level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
      return;
    }
    for (Direction direction : Direction.values()) {
      BlockPos neighbor = pos.relative(direction);
      if (!level.hasChunkAt(neighbor)) continue;
      BlockState target = level.getBlockState(neighbor);
      int chance = direction.getAxis().isHorizontal() ? 100 : 75;
      if (target.getBlock() instanceof IBlockExtension extension
          && random.nextInt(chance)
              < extension.getFlammability(target, level, neighbor, direction.getOpposite()))
        level.setBlock(neighbor, Blocks.FIRE.defaultBlockState(), 3);
    }
    level.setBlock(pos, state.setValue(AGE, age + 1), 2);
    level.scheduleTick(pos, this, 1 + random.nextInt(5));
  }
}

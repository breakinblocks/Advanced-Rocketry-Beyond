// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.space.AlienTree;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** The original lightwood sapling's two growth stages and 45% bonemeal response. */
public final class AlienSaplingBlock extends BushBlock implements BonemealableBlock {
  public static final MapCodec<AlienSaplingBlock> CODEC = simpleCodec(AlienSaplingBlock::new);
  public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

  public AlienSaplingBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
  }

  @Override
  protected MapCodec<? extends BushBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(STAGE);
  }

  @Override
  protected void randomTick(
      BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0)
      advance(level, pos, state, random);
  }

  private void advance(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
    if (state.getValue(STAGE) == 0) level.setBlock(pos, state.setValue(STAGE, 1), 2);
    else AlienTree.grow(level, pos, random);
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public boolean isBonemealSuccess(
      Level level, RandomSource random, BlockPos pos, BlockState state) {
    return random.nextFloat() < 0.45f;
  }

  @Override
  public void performBonemeal(
      ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    advance(level, pos, state, random);
  }
}

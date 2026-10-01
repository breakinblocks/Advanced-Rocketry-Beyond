// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** The original downward piston press: one material block above an obsidian anvil. */
public final class PlatePressBlock extends Block {
  public static final DirectionProperty FACING = BlockStateProperties.FACING;
  public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

  public PlatePressBlock(Properties properties) {
    super(properties);
    registerDefaultState(
        stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(EXTENDED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, EXTENDED);
  }

  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
    if (!old.is(this)) neighborChanged(state, level, pos, this, pos, moved);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, Direction.DOWN).setValue(EXTENDED, false);
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block neighbor,
      BlockPos neighborPos,
      boolean movedByPiston) {
    if (level.isClientSide) return;
    state = level.getBlockState(pos);
    if (!state.is(this)) return;
    boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    if (!powered && state.getValue(EXTENDED))
      level.setBlock(pos, state.setValue(EXTENDED, false), Block.UPDATE_ALL);
    else if (powered && !state.getValue(EXTENDED)) press(level, pos);
  }

  private boolean press(Level level, BlockPos pos) {
    if (!level.getBlockState(pos.below(2)).is(Blocks.OBSIDIAN)) return false;
    ItemStack input = new ItemStack(level.getBlockState(pos.below()).getBlock());
    if (input.isEmpty()) return false;
    ProcessingInput recipeInput =
        new ProcessingInput(MachineType.PLATE_PRESS, List.of(input), List.of());
    for (var holder :
        level.getRecipeManager().getAllRecipesFor(ProcessingRegistry.RECIPE_TYPE.get())) {
      ProcessingRecipe recipe = holder.value();
      if (recipe.inputs().size() != 1
          || recipe.inputs().getFirst().count() != 1
          || !recipe.fluidInputs().isEmpty()
          || !recipe.matches(recipeInput, level)) continue;
      level.setBlock(pos, level.getBlockState(pos).setValue(EXTENDED, true), Block.UPDATE_ALL);
      level.removeBlock(pos.below(), false);
      for (ItemStack output : recipe.roll(level.random).items())
        Block.popResource(level, pos.below(), output);
      return true;
    }
    return false;
  }
}

package advRocketry.processing;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Faces the generator controls toward the player placing it. */
public abstract class GeneratorBlock extends Block {
  public GeneratorBlock(Properties properties) {
    super(properties.noOcclusion());
    registerDefaultState(
        defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(HorizontalDirectionalBlock.FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState()
        .setValue(
            HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(
        HorizontalDirectionalBlock.FACING,
        rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
  }

  @Override
  protected BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
  }
}

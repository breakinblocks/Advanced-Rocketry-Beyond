package advRocketry.orbit;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class DirectionalOrbitalBlock extends OrbitalBlock {
  public static final DirectionProperty FACING = DirectionalBlock.FACING;

  protected DirectionalOrbitalBlock(Kind kind, Properties properties) {
    super(kind, properties);
    registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
  }

  protected Direction placementFacing(BlockPlaceContext context) {
    return context.getNearestLookingDirection().getOpposite();
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, placementFacing(context));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }
}

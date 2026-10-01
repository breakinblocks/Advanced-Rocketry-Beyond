package advRocketry.rocket;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class RocketInfrastructureBlock extends Block implements EntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
  private final Supplier<? extends BlockEntityType<?>> type;

  protected RocketInfrastructureBlock(
      Properties properties, Supplier<? extends BlockEntityType<?>> type) {
    super(properties);
    this.type = type;
    BlockState state = defaultBlockState().setValue(FACING, Direction.NORTH);
    registerDefaultState(state.hasProperty(POWERED) ? state.setValue(POWERED, false) : state);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == this.type.get()
        ? (world, pos, blockState, entity) -> ((RocketInfrastructure) entity).tick()
        : null;
  }

  protected IItemHandler contents(BlockEntity entity) {
    return null;
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
    if (!state.is(next.getBlock()) && !level.isClientSide) {
      BlockEntity entity = level.getBlockEntity(pos);
      IItemHandler contents = entity == null ? null : contents(entity);
      if (contents != null)
        for (int slot = 0; slot < contents.getSlots(); slot++)
          popResource(level, pos, contents.extractItem(slot, 64, false));
    }
    super.onRemove(state, level, pos, next, moving);
  }
}

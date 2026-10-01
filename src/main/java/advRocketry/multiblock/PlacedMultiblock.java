package advRocketry.multiblock;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record PlacedMultiblock(
    Multiblock multiblock, Level level, BlockPos controller, Direction facing) {
  public static PlacedMultiblock of(BlockEntity entity, String id) {
    Level level = entity.getLevel();
    if (level == null) return null;
    Multiblock multiblock = Multiblocks.get(level, id);
    if (multiblock == null) return null;
    var state = entity.getBlockState();
    Direction facing =
        state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
            : Direction.NORTH;
    return new PlacedMultiblock(multiblock, level, entity.getBlockPos(), facing);
  }

  public static boolean complete(BlockEntity entity, String id) {
    PlacedMultiblock placed = of(entity, id);
    return placed != null && placed.complete();
  }

  public boolean complete() {
    return multiblock.complete(level, controller, facing);
  }

  public boolean loaded() {
    return multiblock.loaded(level, controller, facing);
  }

  public List<Multiblock.Placed> cells() {
    return multiblock.place(controller, facing);
  }

  public List<BlockPos> positions(char symbol) {
    return multiblock.positions(controller, facing, symbol);
  }

  public BlockPos first(char symbol) {
    List<BlockPos> positions = positions(symbol);
    return positions.isEmpty() ? null : positions.getFirst();
  }

  public BlockPos position(int layer, int row, int column) {
    return multiblock.position(controller, facing, layer, row, column);
  }

  public <T> List<T> entities(char symbol, Class<T> type) {
    List<T> found = new ArrayList<>();
    for (BlockPos pos : positions(symbol))
      if (type.isInstance(level.getBlockEntity(pos)))
        found.add(type.cast(level.getBlockEntity(pos)));
    return found;
  }

  public <T> T entity(char symbol, Class<T> type) {
    BlockPos pos = first(symbol);
    return pos != null && type.isInstance(level.getBlockEntity(pos))
        ? type.cast(level.getBlockEntity(pos))
        : null;
  }
}

package advRocketry;

import advRocketry.multiblock.Multiblock;
import advRocketry.multiblock.Multiblocks;
import advRocketry.multiblock.PlacedMultiblock;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class TestMultiblocks {
  private TestMultiblocks() {}

  public static PlacedMultiblock at(Level level, String id, BlockPos controller, Direction facing) {
    return new PlacedMultiblock(Multiblocks.require(level, id), level, controller, facing);
  }

  public static PlacedMultiblock at(BlockEntity entity, String id) {
    return PlacedMultiblock.of(entity, id);
  }

  public static void build(Level level, String id, BlockPos controller, Direction facing) {
    build(level, id, controller, facing, Map.of());
  }

  public static void build(
      Level level,
      String id,
      BlockPos controller,
      Direction facing,
      Map<Character, Block> overrides) {
    PlacedMultiblock structure = at(level, id, controller, facing);
    for (Multiblock.Placed cell : structure.cells()) {
      if (cell.symbol() == Multiblock.CONTROLLER) {
        if (level.getBlockState(cell.pos()).is(structure.multiblock().controller())) continue;
        BlockState state = structure.multiblock().controller().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
          state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        level.setBlock(cell.pos(), state, 2);
        continue;
      }
      Block override = overrides.get(cell.symbol());
      List<Block> blocks = cell.key().blocks();
      Block block = override != null ? override : blocks.isEmpty() ? Blocks.AIR : blocks.getFirst();
      level.setBlock(cell.pos(), block.defaultBlockState(), 2);
    }
  }
}

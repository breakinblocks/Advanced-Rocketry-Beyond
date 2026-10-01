package advRocketry.processing;

import advRocketry.Main;
import advRocketry.orbit.FormedState;
import advRocketry.orbit.OrbitalBlockEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlueprintPlacer {
  private BlueprintPlacer() {}

  public static void place(Level level, List<ProjectorBlueprint.Cell> cells, Direction facing) {
    Map<Object, BlockState> chosen = new HashMap<>();
    int flexible = 0;
    for (var cell : cells) {
      BlockState state =
          flexible(cell)
              ? switch (flexible++) {
                case 0, 1 -> MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState();
                case 2 -> MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState();
                case 3 -> MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState();
                default ->
                    first(cell, candidate -> !(candidate.getBlock() instanceof PortBlock))
                        .orElseGet(() -> choose(cell, chosen));
              }
              : choose(cell, chosen);
      level.setBlock(cell.position(), facing(state, facing), Block.UPDATE_ALL);
    }
  }

  public static boolean checked(BlockEntity controller) {
    return controller instanceof ProcessingBlockEntity
        || controller instanceof OrbitalBlockEntity orbital && FormedState.checked(orbital);
  }

  public static boolean formed(BlockEntity controller) {
    return controller instanceof ProcessingBlockEntity machine
        ? machine.scanStructure()
        : controller instanceof OrbitalBlockEntity orbital && FormedState.check(orbital);
  }

  public static BlockState facing(BlockState state, Direction facing) {
    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
      return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    if (state.hasProperty(BlockStateProperties.FACING))
      return state.setValue(BlockStateProperties.FACING, facing);
    return state;
  }

  private static boolean flexible(ProjectorBlueprint.Cell cell) {
    return cell.matches().test(MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState())
        && cell.matches().test(MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState());
  }

  private static BlockState choose(ProjectorBlueprint.Cell cell, Map<Object, BlockState> chosen) {
    return chosen.computeIfAbsent(
        cell.key(),
        key ->
            first(cell, state -> !state.isAir())
                .orElseGet(
                    () ->
                        cell.matches().test(Blocks.AIR.defaultBlockState())
                            ? Blocks.AIR.defaultBlockState()
                            : MachinePorts.BLOCK_STRUCTURE.get().defaultBlockState()));
  }

  private static Optional<BlockState> first(
      ProjectorBlueprint.Cell cell, Predicate<BlockState> preferred) {
    return Stream.concat(
            BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(Main.MODID))
                .map(Map.Entry::getValue),
            BuiltInRegistries.BLOCK.stream())
        .map(Block::defaultBlockState)
        .filter(preferred)
        .filter(cell.matches())
        .findFirst();
  }
}

package advRocketry.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class Multiblock {
  public static final char CONTROLLER = 'c';
  public static final char EMPTY = ' ';

  public record Key(List<HolderSet<Block>> any, boolean air) {
    public static final Codec<Key> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        RegistryCodecs.homogeneousList(Registries.BLOCK)
                            .listOf()
                            .optionalFieldOf("any", List.of())
                            .forGetter(Key::any),
                        Codec.BOOL.optionalFieldOf("air", false).forGetter(Key::air))
                    .apply(instance, Key::new));

    public boolean test(BlockState state) {
      if (air && state.isAir()) return true;
      for (HolderSet<Block> set : any) if (state.is(set)) return true;
      return false;
    }

    public List<Block> blocks() {
      List<Block> blocks = new ArrayList<>();
      for (HolderSet<Block> set : any)
        for (var holder : set) if (!blocks.contains(holder.value())) blocks.add(holder.value());
      return blocks;
    }
  }

  public record Cell(int layer, int row, int column, char symbol) {}

  public record Placed(BlockPos pos, char symbol, Key key) {}

  private static final Codec<Character> SYMBOL =
      Codec.STRING.comapFlatMap(
          value ->
              value.length() == 1
                  ? DataResult.success(value.charAt(0))
                  : DataResult.error(() -> "Multiblock key must be one character: " + value),
          String::valueOf);

  public static final Codec<Multiblock> CODEC =
      RecordCodecBuilder.<Multiblock>create(
              instance ->
                  instance
                      .group(
                          BuiltInRegistries.BLOCK
                              .byNameCodec()
                              .fieldOf("controller")
                              .forGetter(Multiblock::controller),
                          Codec.BOOL
                              .optionalFieldOf("rotates", true)
                              .forGetter(Multiblock::rotates),
                          Codec.INT.optionalFieldOf("order", 1000).forGetter(Multiblock::order),
                          Codec.STRING
                              .listOf()
                              .listOf()
                              .fieldOf("layers")
                              .forGetter(Multiblock::layers),
                          Codec.unboundedMap(SYMBOL, Key.CODEC)
                              .fieldOf("keys")
                              .forGetter(Multiblock::keys))
                      .apply(instance, Multiblock::new))
          .validate(Multiblock::validate);

  private final Block controller;
  private final boolean rotates;
  private final int order;
  private final List<List<String>> layers;
  private final Map<Character, Key> keys;
  private final List<Cell> cells;
  private final Cell origin;

  public Multiblock(
      Block controller,
      boolean rotates,
      int order,
      List<List<String>> layers,
      Map<Character, Key> keys) {
    this.controller = controller;
    this.rotates = rotates;
    this.order = order;
    this.layers = List.copyOf(layers.stream().map(List::copyOf).toList());
    this.keys = Collections.unmodifiableMap(new LinkedHashMap<>(keys));
    List<Cell> found = new ArrayList<>();
    Cell controllerCell = null;
    for (int layer = 0; layer < layers.size(); layer++)
      for (int row = 0; row < layers.get(layer).size(); row++) {
        String line = layers.get(layer).get(row);
        for (int column = 0; column < line.length(); column++) {
          char symbol = line.charAt(column);
          if (symbol == EMPTY) continue;
          Cell cell = new Cell(layer, row, column, symbol);
          found.add(cell);
          if (symbol == CONTROLLER) controllerCell = cell;
        }
      }
    this.cells = List.copyOf(found);
    this.origin = controllerCell;
  }

  private static DataResult<Multiblock> validate(Multiblock multiblock) {
    if (multiblock.origin == null)
      return DataResult.error(() -> "Multiblock has no controller cell 'c'");
    for (Cell cell : multiblock.cells)
      if (cell.symbol() != CONTROLLER && !multiblock.keys.containsKey(cell.symbol()))
        return DataResult.error(() -> "Multiblock uses undefined key '" + cell.symbol() + "'");
    return DataResult.success(multiblock);
  }

  public Block controller() {
    return controller;
  }

  public boolean rotates() {
    return rotates;
  }

  public int order() {
    return order;
  }

  public List<List<String>> layers() {
    return layers;
  }

  public Map<Character, Key> keys() {
    return keys;
  }

  public List<Cell> cells() {
    return cells;
  }

  public Key key(char symbol) {
    return symbol == CONTROLLER
        ? new Key(List.of(HolderSet.direct(controller.builtInRegistryHolder())), false)
        : keys.get(symbol);
  }

  public BlockPos position(BlockPos controller, Direction facing, Cell cell) {
    return position(controller, facing, cell.layer(), cell.row(), cell.column());
  }

  public BlockPos position(BlockPos controller, Direction facing, int layer, int row, int column) {
    Direction back =
        (rotates && facing.getAxis().isHorizontal() ? facing : Direction.NORTH).getOpposite();
    return controller
        .above(origin.layer() - layer)
        .relative(back.getClockWise(), column - origin.column())
        .relative(back, row - origin.row());
  }

  public List<Placed> place(BlockPos controller, Direction facing) {
    List<Placed> placed = new ArrayList<>(cells.size());
    for (Cell cell : cells)
      placed.add(new Placed(position(controller, facing, cell), cell.symbol(), key(cell.symbol())));
    return placed;
  }

  public List<BlockPos> positions(BlockPos controller, Direction facing, char symbol) {
    List<BlockPos> positions = new ArrayList<>();
    for (Cell cell : cells)
      if (cell.symbol() == symbol) positions.add(position(controller, facing, cell));
    return positions;
  }

  public boolean loaded(Level level, BlockPos controller, Direction facing) {
    for (Cell cell : cells) if (!level.isLoaded(position(controller, facing, cell))) return false;
    return true;
  }

  public boolean complete(Level level, BlockPos controller, Direction facing) {
    for (Cell cell : cells) {
      BlockPos pos = position(controller, facing, cell);
      if (!level.isLoaded(pos) || !key(cell.symbol()).test(level.getBlockState(pos))) return false;
    }
    return true;
  }
}

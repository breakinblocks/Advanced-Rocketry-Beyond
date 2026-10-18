package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Plants retain their attachment and fluid rules when harvested and replanted. */
public final class HabitatPlantBlock extends Block
    implements SimpleWaterloggedBlock, BonemealableBlock {
  public static final MapCodec<HabitatPlantBlock> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(Codec.STRING.fieldOf("habitat").forGetter(b -> b.habitat), propertiesCodec())
                  .apply(i, HabitatPlantBlock::new));
  private final String habitat;

  public HabitatPlantBlock(String habitat, Properties properties) {
    super(properties);
    this.habitat = habitat;
    registerDefaultState(
        stateDefinition
            .any()
            .setValue(BlockStateProperties.FACING, Direction.UP)
            .setValue(BlockStateProperties.WATERLOGGED, false));
  }

  @Override
  public MapCodec<HabitatPlantBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
    b.add(BlockStateProperties.FACING, BlockStateProperties.WATERLOGGED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext c) {
    Direction facing =
        habitat.equals("ceiling")
            ? Direction.DOWN
            : habitat.equals("wall") ? c.getClickedFace() : Direction.UP;
    BlockState state =
        defaultBlockState()
            .setValue(BlockStateProperties.FACING, facing)
            .setValue(
                BlockStateProperties.WATERLOGGED,
                c.getLevel().getFluidState(c.getClickedPos()).is(FluidTags.WATER));
    return state.canSurvive(c.getLevel(), c.getClickedPos()) ? state : null;
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    if (!habitat.equals("aquatic") && state.getValue(BlockStateProperties.WATERLOGGED))
      return false;
    Direction face = state.getValue(BlockStateProperties.FACING);
    BlockPos support = pos.relative(face.getOpposite());
    BlockState below = level.getBlockState(support);
    if (habitat.equals("floating"))
      return face == Direction.UP && below.getFluidState().is(FluidTags.WATER);
    if (habitat.equals("aquatic") && !state.getValue(BlockStateProperties.WATERLOGGED))
      return false;
    if (habitat.equals("ceiling"))
      return face == Direction.DOWN
          && (below.isFaceSturdy(level, support, Direction.DOWN)
              || below.is(BiomeMaterials.block("hanging_roots")));
    if (habitat.equals("wall"))
      return face.getAxis().isHorizontal() && below.isFaceSturdy(level, support, face);
    if (face != Direction.UP) return false;
    if (habitat.equals("mineral") || habitat.equals("heat") || habitat.equals("shore"))
      return below.isFaceSturdy(level, support, Direction.UP);
    return below.is(BlockTags.DIRT)
        || below.is(Blocks.MOSS_BLOCK)
        || below.is(Blocks.MUD)
        || below.is(BiomeMaterials.block("rooted_loam"))
        || below.is(BiomeMaterials.block("alluvial_silt"))
        || below.is(BiomeMaterials.block("luminous_moss"))
        || habitat.equals("aquatic") && below.isFaceSturdy(level, support, Direction.UP);
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
    return findGrowthPosition(level, pos, state, 0) != null;
  }

  @Override
  public boolean isBonemealSuccess(
      Level level, RandomSource random, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(
      ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    BlockPos target = findGrowthPosition(level, pos, state, random.nextInt(8));
    if (target != null) level.setBlockAndUpdate(target, state);
  }

  /** One nearby offspring per application, without replacing plants or other occupied blocks. */
  private BlockPos findGrowthPosition(
      LevelReader level, BlockPos pos, BlockState state, int start) {
    if (!state.canSurvive(level, pos)) return null;
    Direction face = state.getValue(BlockStateProperties.FACING);
    for (int i = 0; i < 8; i++) {
      int index = (start + i) % 8;
      if (index >= 4) index++; // Skip the parent at the center of the 3x3 plane.
      int dx = index % 3 - 1;
      int dz = index / 3 - 1;
      BlockPos target =
          face.getAxis() == Direction.Axis.X
              ? pos.offset(0, dx, dz)
              : face.getAxis() == Direction.Axis.Z ? pos.offset(dx, dz, 0) : pos.offset(dx, 0, dz);
      if (!level.hasChunkAt(target)) continue;
      BlockState existing = level.getBlockState(target);
      boolean space =
          habitat.equals("aquatic")
              ? existing.is(Blocks.WATER) && existing.getFluidState().isSource()
              : existing.isAir();
      if (space && state.canSurvive(level, target) && fertileSupport(level, target, face))
        return target;
    }
    return null;
  }

  private boolean fertileSupport(LevelReader level, BlockPos pos, Direction face) {
    BlockState support = level.getBlockState(pos.relative(face.getOpposite()));
    return switch (habitat) {
      case "heat" ->
          support.is(Blocks.MAGMA_BLOCK)
              || support.is(BiomeMaterials.block("cooling_crust"))
              || support.is(BiomeMaterials.block("weathered_basalt"));
      case "mineral" ->
          support.is(BiomeMaterials.block("mineral_crust"))
              || support.is(Blocks.CALCITE)
              || support.is(Blocks.AMETHYST_BLOCK);
      case "shore" ->
          support.is(BlockTags.SAND) || support.is(BiomeMaterials.block("alluvial_silt"));
      default -> true;
    };
  }

  @Override
  protected BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighbor,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    if (!state.canSurvive(level, pos))
      return state.getValue(BlockStateProperties.WATERLOGGED)
          ? Blocks.WATER.defaultBlockState()
          : Blocks.AIR.defaultBlockState();
    if (state.getValue(BlockStateProperties.WATERLOGGED))
      level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
  }

  @Override
  protected FluidState getFluidState(BlockState state) {
    return state.getValue(BlockStateProperties.WATERLOGGED)
        ? Fluids.WATER.getSource(false)
        : super.getFluidState(state);
  }

  @Override
  public boolean canPlaceLiquid(
      Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
    return habitat.equals("aquatic") && fluid == Fluids.WATER;
  }

  @Override
  public boolean placeLiquid(
      LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
    return habitat.equals("aquatic")
        && SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext c) {
    return habitat.equals("floating")
        ? Block.box(0, 0, 0, 16, 2, 16)
        : Block.box(2, 0, 2, 14, 15, 14);
  }
}

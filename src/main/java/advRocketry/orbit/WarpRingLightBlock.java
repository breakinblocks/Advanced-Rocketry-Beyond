package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A replaceable light source that expires if its Warp Core is no longer formed. */
final class WarpRingLightBlock extends Block {
    private static final IntegerProperty ANCHOR = IntegerProperty.create("anchor", 0, 2);
    private static final int CHECK_INTERVAL = 24;

    WarpRingLightBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .replaceable()
                .noLootTable()
                .lightLevel(state -> 11));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalOrbitalBlock.FACING, ANCHOR);
    }

    BlockState state(Direction facing, WarpCoreLighting.Anchor anchor) {
        return defaultBlockState()
                .setValue(HorizontalOrbitalBlock.FACING, facing)
                .setValue(ANCHOR, anchor.ordinal());
    }

    static boolean belongsTo(BlockState state, Direction facing, WarpCoreLighting.Anchor anchor) {
        return state.is(OrbitalRegistry.WARP_RING_LIGHT.get())
                && state.getValue(HorizontalOrbitalBlock.FACING) == facing
                && state.getValue(ANCHOR) == anchor.ordinal();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide && !old.is(this)) level.scheduleTick(pos, this, CHECK_INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(HorizontalOrbitalBlock.FACING);
        var anchor = WarpCoreLighting.Anchor.values()[state.getValue(ANCHOR)];
        BlockPos controller = anchor.controller(pos, facing);
        if (!level.isLoaded(controller)) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
            return;
        }
        BlockState core = level.getBlockState(controller);
        if (core.is(OrbitalRegistry.WARP_CORE.get())
                && core.getValue(OrbitalBlock.FORMED)
                && core.getValue(HorizontalOrbitalBlock.FACING) == facing) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        } else {
            level.removeBlock(pos, false);
        }
    }
}

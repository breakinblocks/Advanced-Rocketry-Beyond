// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.multiblock.FormedMachineParts;
import advRocketry.orbit.OrbitalAssembly;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Native state used to hide the constituent blocks beneath the original machine model. */
public class MachinePartBlock extends Block {
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public MachinePartBlock(Properties properties) {
        super(properties.noOcclusion().dynamicShape());
        registerDefaultState(stateDefinition.any().setValue(ASSEMBLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ASSEMBLED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(ASSEMBLED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) && !adjacent.getValue(ASSEMBLED) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape warp = warpShape(state, level, pos);
        return warp == null ? super.getShape(state, level, pos, context) : warp;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape warp = warpShape(state, level, pos);
        return warp == null ? Shapes.block() : warp;
    }

    private static VoxelShape warpShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(ASSEMBLED) && level instanceof Level world
                ? FormedMachineParts.warpPartShape(world, pos)
                : null;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ASSEMBLED)) return;
        if (MultiblockMachine.isPartClaimed(level, pos) || OrbitalAssembly.isClaimed(level, pos))
            level.scheduleTick(pos, this, 40);
        else level.setBlock(pos, state.setValue(ASSEMBLED, false), Block.UPDATE_CLIENTS);
    }
}

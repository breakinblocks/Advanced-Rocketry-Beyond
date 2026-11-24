package advRocketry.orbit;

import advRocketry.multiblock.FormedMachineParts;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FormedCoverBlock extends BaseEntityBlock {
    public static final MapCodec<FormedCoverBlock> CODEC = simpleCodec(properties -> new FormedCoverBlock());

    FormedCoverBlock() {
        super(BlockBehaviour.Properties.of().strength(5, 6).noOcclusion().dynamicShape());
    }

    public static BlockState visibleState(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FormedCoverBlock
                && level.getBlockEntity(pos) instanceof FormedCoverBlockEntity cover) return cover.original();
        return state;
    }

    static void cover(Level level, BlockPos pos, BlockState original) {
        level.setBlock(pos, OrbitalRegistry.FORMED_COVER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof FormedCoverBlockEntity cover) cover.original(original);
    }

    static void uncover(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof FormedCoverBlockEntity cover)) return;
        BlockState original = cover.original();
        level.setBlock(pos, original, Block.UPDATE_ALL);
    }

    private static BlockState original(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FormedCoverBlockEntity cover ? cover.original() : null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FormedCoverBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape formed = level instanceof Level world ? FormedMachineParts.warpPartShape(world, pos) : null;
        if (formed != null) return formed;
        BlockState original = original(level, pos);
        return original == null || original.isAir() || original.getBlock() instanceof FormedCoverBlock
                ? Shapes.empty()
                : original.getShape(level, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState original = original(level, pos);
        return original == null || original.isAir()
                ? super.getDestroyProgress(state, player, level, pos)
                : original.getDestroyProgress(player, level, pos);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FormedCoverBlockEntity cover
                && !cover.original().isAir()) return cover.original().getDrops(params);
        return List.of();
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockState original = original(level, pos);
        return original == null || original.isAir()
                ? ItemStack.EMPTY
                : original.getCloneItemStack(target, level, pos, player);
    }
}

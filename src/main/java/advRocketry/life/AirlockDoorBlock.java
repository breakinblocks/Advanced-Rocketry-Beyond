package advRocketry.life;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AirlockDoorBlock extends DoorBlock implements EntityBlock {
    public static final MapCodec<AirlockDoorBlock> CODEC = simpleCodec(AirlockDoorBlock::new);
    public static final BlockSetType TYPE = new BlockSetType(
            "adv_rocketry_airlock",
            false,
            false,
            false,
            BlockSetType.PressurePlateSensitivity.MOBS,
            SoundType.METAL,
            LifeSupportRegistry.AIRLOCK_CLOSE_EVENT,
            LifeSupportRegistry.AIRLOCK_OPEN_EVENT,
            LifeSupportRegistry.AIRLOCK_CLOSE_EVENT,
            LifeSupportRegistry.AIRLOCK_OPEN_EVENT,
            BlockSetType.IRON.pressurePlateClickOff(),
            BlockSetType.IRON.pressurePlateClickOn(),
            BlockSetType.IRON.buttonClickOff(),
            BlockSetType.IRON.buttonClickOn());

    private static final VoxelShape[] LOWER_OPEN = new VoxelShape[4];
    private static final VoxelShape[] UPPER_OPEN = new VoxelShape[4];

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape posts = Shapes.or(box(facing, 0, 0, 3, 16, 0, 1), box(facing, 0, 0, 3, 16, 15, 16));
            LOWER_OPEN[facing.get2DDataValue()] = posts;
            UPPER_OPEN[facing.get2DDataValue()] = Shapes.or(posts, box(facing, 0, 13, 3, 16, 1, 15));
        }
    }

    public AirlockDoorBlock(Properties properties) {
        super(TYPE, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    private static VoxelShape box(
            Direction facing,
            double depthFrom,
            double yFrom,
            double depthTo,
            double yTo,
            double sideFrom,
            double sideTo) {
        return switch (facing) {
            case NORTH -> Block.box(sideFrom, yFrom, 16 - depthTo, sideTo, yTo, 16 - depthFrom);
            case SOUTH -> Block.box(16 - sideTo, yFrom, depthFrom, 16 - sideFrom, yTo, depthTo);
            case WEST -> Block.box(16 - depthTo, yFrom, 16 - sideTo, 16 - depthFrom, yTo, 16 - sideFrom);
            default -> Block.box(depthFrom, yFrom, sideFrom, depthTo, yTo, sideTo);
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(OPEN)) return super.getShape(state, level, pos, context);
        int index = state.getValue(FACING).get2DDataValue();
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_OPEN[index] : LOWER_OPEN[index];
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirlockDoorBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != LifeSupportRegistry.AIRLOCK_ENTITY.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<AirlockDoorBlockEntity>) AirlockDoorBlockEntity::tick;
    }
}

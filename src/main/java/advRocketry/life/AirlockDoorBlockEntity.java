package advRocketry.life;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AirlockDoorBlockEntity extends BlockEntity {
    private static final float STEP = 1 / 8f;
    private float progress = -1;
    private float previous;

    public AirlockDoorBlockEntity(BlockPos pos, BlockState state) {
        super(LifeSupportRegistry.AIRLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AirlockDoorBlockEntity door) {
        float target = state.getValue(DoorBlock.OPEN) ? 1 : 0;
        if (door.progress < 0) door.progress = target;
        door.previous = door.progress;
        door.progress = target > door.progress
                ? Math.min(target, door.progress + STEP)
                : Math.max(target, door.progress - STEP);
    }

    public float openness(float partialTick) {
        if (progress < 0) return getBlockState().getValue(DoorBlock.OPEN) ? 1 : 0;
        float t = Mth.lerp(partialTick, previous, progress);
        return t * t * (3 - 2 * t);
    }
}

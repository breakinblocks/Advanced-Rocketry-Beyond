package advRocketry.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ClientSync {
    private static final int INTERVAL = 10;

    private long synced = Long.MIN_VALUE;
    private long next;

    public void update(BlockEntity entity, long value) {
        Level level = entity.getLevel();
        if (level == null || level.isClientSide || value == synced) return;
        long now = level.getGameTime();
        if (now < next) return;
        synced = value;
        next = now + INTERVAL;
        level.sendBlockUpdated(
                entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
    }
}

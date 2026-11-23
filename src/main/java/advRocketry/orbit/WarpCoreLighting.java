package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/** Steady block light for the formed core and timing shared by its visual rings. */
public final class WarpCoreLighting {
    public static final int RING_COUNT = 4;
    private static final double RING_TRAVEL_TICKS = 50;
    private static final double RING_MIDPOINT = 2.38;

    enum Anchor {
        LOWER(2, 1, 1),
        MIDDLE(3, 0, 2),
        UPPER(1, 0, 3);

        final int back;
        final int side;
        final int up;

        Anchor(int back, int side, int up) {
            this.back = back;
            this.side = side;
            this.up = up;
        }

        BlockPos position(BlockPos controller, Direction facing) {
            Direction rear = facing.getOpposite();
            return controller
                    .relative(rear, back)
                    .relative(rear.getClockWise(), side)
                    .above(up);
        }

        BlockPos controller(BlockPos light, Direction facing) {
            return light.relative(facing, back)
                    .relative(facing.getClockWise(), side)
                    .below(up);
        }
    }

    private static final Anchor[] ANCHORS = Anchor.values();

    private WarpCoreLighting() {}

    public static double ringHeight(double time, int ring) {
        double distance = 1.58 - 1.54 * ringProgress(time, ring);
        return RING_MIDPOINT + (ring % 2 == 0 ? -distance : distance);
    }

    public static double ringPulse(double time, int ring) {
        double progress = ringProgress(time, ring);
        return Math.min(1, Math.sin(progress * Math.PI) * 1.5);
    }

    private static double ringProgress(double time, int ring) {
        // Consecutive rings are mirrored partners. The second pair starts half a cycle later.
        return (time / RING_TRAVEL_TICKS + (ring / 2) * .5) % 1;
    }

    static void ensure(OrbitalBlockEntity core) {
        if (!(core.getLevel() instanceof ServerLevel level)) return;
        BlockPos controller = core.getBlockPos();
        Direction facing = core.getBlockState().getValue(HorizontalOrbitalBlock.FACING);
        var marker = OrbitalRegistry.WARP_RING_LIGHT.get();
        PlacedMultiblock layout = null;
        for (Anchor anchor : ANCHORS) {
            BlockPos pos = anchor.position(controller, facing);
            if (!level.isLoaded(pos) || !level.getBlockState(pos).isAir()) continue;
            if (layout == null) layout = PlacedMultiblock.of(core, "warp_core");
            if (layout == null
                    || layout.cells().stream().anyMatch(cell -> cell.pos().equals(pos))) continue;
            level.setBlock(pos, marker.state(facing, anchor), Block.UPDATE_CLIENTS);
        }
    }

    static void clear(OrbitalBlockEntity core) {
        if (!(core.getLevel() instanceof ServerLevel level)) return;
        Direction facing = core.getBlockState().getValue(HorizontalOrbitalBlock.FACING);
        for (Anchor anchor : ANCHORS) {
            BlockPos pos = anchor.position(core.getBlockPos(), facing);
            if (level.isLoaded(pos) && WarpRingLightBlock.belongsTo(level.getBlockState(pos), facing, anchor))
                level.removeBlock(pos, false);
        }
    }
}

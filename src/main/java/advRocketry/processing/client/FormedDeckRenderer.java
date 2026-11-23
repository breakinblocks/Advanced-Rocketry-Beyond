package advRocketry.processing.client;

import advRocketry.Main;
import advRocketry.multiblock.PlacedMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Joins the formed deck across recipe slabs, leaving trim around exposed edges and the console. */
final class FormedDeckRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/formed_deck.png");
    private static final int LOWER = 1, UPPER = 2;
    private static final float[] TRIM = {0, 1 / 16f, 15 / 16f, 1};
    // The formed machine hull can share the slab boundary. Keep the deck finish just outside it.
    private static final float SURFACE_CLEARANCE = .006f;

    private enum Finish {
        FIELD(.125f),
        RIM(.375f),
        GASKET(.625f),
        WALL(.875f);

        final float u;

        Finish(float u) {
            this.u = u;
        }
    }

    private FormedDeckRenderer() {}

    static void render(PlacedMultiblock layout, PoseStack pose, MultiBufferSource buffers, int overlay) {
        Map<BlockPos, Integer> slabs = new HashMap<>();
        for (BlockPos pos : layout.positions('s')) {
            layout.level()
                    .getBlockState(pos)
                    .getOptionalValue(BlockStateProperties.SLAB_TYPE)
                    .ifPresent(type -> slabs.put(
                            pos,
                            switch (type) {
                                case BOTTOM -> LOWER;
                                case TOP -> UPPER;
                                case DOUBLE -> LOWER | UPPER;
                            }));
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        slabs.forEach((pos, halves) -> {
            BlockPos offset = pos.subtract(layout.controller());
            pose.pushPose();
            pose.translate(offset.getX(), offset.getY(), offset.getZ());
            DeckFace face = new DeckFace(
                    pose.last(), vertices, LevelRenderer.getLightColor(layout.level(), pos.above()), overlay);
            int above = slabs.getOrDefault(pos.above(), 0);
            int below = slabs.getOrDefault(pos.below(), 0);
            if (halves == LOWER || (above & LOWER) == 0) {
                top(slabs, layout.controller(), pos, halves, face);
            }
            if (halves == UPPER || (below & UPPER) == 0) {
                float bottom = halves == UPPER ? .5f : .002f;
                face.draw(Direction.DOWN, Finish.WALL, 0, bottom, 0, 1, bottom, 1);
            }
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(side);
                int exposed = halves & ~slabs.getOrDefault(neighbor, 0);
                if (exposed == 0 || neighbor.equals(layout.controller())) continue;
                float from = (exposed & LOWER) == 0 ? .5f : 0;
                float to = (exposed & UPPER) == 0 ? .5f : 1;
                if ((exposed & Integer.highestOneBit(halves)) != 0) to += SURFACE_CLEARANCE;
                wall(face, side, from, to);
            }
            pose.popPose();
        });
    }

    private static void top(
            Map<BlockPos, Integer> slabs, BlockPos controller, BlockPos pos, int halves, DeckFace face) {
        float y = ((halves & UPPER) == 0 ? .5f : 1) + SURFACE_CLEARANCE;
        float west = outside(slabs, controller, pos.west(), halves) ? -SURFACE_CLEARANCE : 0;
        float east = outside(slabs, controller, pos.east(), halves) ? 1 + SURFACE_CLEARANCE : 1;
        float north = outside(slabs, controller, pos.north(), halves) ? -SURFACE_CLEARANCE : 0;
        float south = outside(slabs, controller, pos.south(), halves) ? 1 + SURFACE_CLEARANCE : 1;
        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 3; x++) {
                int stepX = x - 1;
                int stepZ = z - 1;
                BlockPos across = pos.offset(stepX, 0, 0);
                BlockPos along = pos.offset(0, 0, stepZ);
                Finish finish = Finish.FIELD;
                if (across.equals(controller)
                        || along.equals(controller)
                        || pos.offset(stepX, 0, stepZ).equals(controller)) {
                    finish = Finish.GASKET;
                } else if (!sameHeight(slabs, across, halves) || !sameHeight(slabs, along, halves)) {
                    finish = Finish.RIM;
                }
                face.draw(
                        Direction.UP,
                        finish,
                        x == 0 ? west : TRIM[x],
                        y,
                        z == 0 ? north : TRIM[z],
                        x == 2 ? east : TRIM[x + 1],
                        y,
                        z == 2 ? south : TRIM[z + 1]);
            }
        }
    }

    private static boolean sameHeight(Map<BlockPos, Integer> slabs, BlockPos pos, int halves) {
        return Integer.highestOneBit(slabs.getOrDefault(pos, 0)) == Integer.highestOneBit(halves);
    }

    private static boolean outside(Map<BlockPos, Integer> slabs, BlockPos controller, BlockPos pos, int halves) {
        return !pos.equals(controller) && (slabs.getOrDefault(pos, 0) & Integer.highestOneBit(halves)) == 0;
    }

    private static void wall(DeckFace face, Direction side, float from, float to) {
        float lower = -SURFACE_CLEARANCE;
        float upper = 1 + SURFACE_CLEARANCE;
        switch (side) {
            case NORTH -> face.draw(side, Finish.WALL, 0, from, lower, 1, to, lower);
            case SOUTH -> face.draw(side, Finish.WALL, 0, from, upper, 1, to, upper);
            case WEST -> face.draw(side, Finish.WALL, lower, from, 0, lower, to, 1);
            case EAST -> face.draw(side, Finish.WALL, upper, from, 0, upper, to, 1);
            default -> throw new IllegalArgumentException("Expected a horizontal face");
        }
    }

    private record DeckFace(PoseStack.Pose pose, VertexConsumer vertices, int light, int overlay) {
        void draw(Direction normal, Finish finish, float x0, float y0, float z0, float x1, float y1, float z1) {
            vertex(normal, finish, x0, y0, z0);
            switch (normal.getAxis()) {
                case X, Z -> vertex(normal, finish, x0, y1, z0);
                case Y -> vertex(normal, finish, x0, y0, z1);
            }
            vertex(normal, finish, x1, y1, z1);
            switch (normal.getAxis()) {
                case X, Z -> vertex(normal, finish, x1, y0, z1);
                case Y -> vertex(normal, finish, x1, y1, z0);
            }
        }

        private void vertex(Direction normal, Finish finish, float x, float y, float z) {
            vertices.addVertex(pose, x, y, z)
                    .setColor(-1)
                    .setUv(finish.u, .5f)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(pose, normal.getStepX(), normal.getStepY(), normal.getStepZ());
        }
    }
}

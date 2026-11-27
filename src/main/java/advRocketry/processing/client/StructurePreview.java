// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.processing.HoloProjectorItem;
import advRocketry.processing.ProjectorBlueprint;
import advRocketry.util.Texts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class StructurePreview {
    private static final int REFRESH_TICKS = 10;
    private static final int CYCLE_TICKS = 20;
    private static final float PULSE_TICKS = 40;
    private static final float GHOST_SCALE = .5f;
    private static final float GHOST_ALPHA = .2f;
    private static final RenderType GHOST = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
    private static List<ProjectorBlueprint.Cell> cells = List.of();
    private static List<ProjectorBlueprint.Cell> ghosts = List.of();
    private static List<BlockPos> invalid = List.of();
    private static long refreshedAt = Long.MIN_VALUE;
    private static int refreshedLayer = -2;
    private static List<Integer> layers = List.of();
    private static int selectedLayer = -1;
    private static ResourceKey<Level> dimension;

    private StructurePreview() {}

    public static void clear() {
        cells = List.of();
        ghosts = List.of();
        invalid = List.of();
        refreshedAt = Long.MIN_VALUE;
        layers = List.of();
        selectedLayer = -1;
        dimension = null;
    }

    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static boolean inSelectedDimension(Minecraft minecraft) {
        if (cells.isEmpty()) return false;
        if (minecraft.level != null && minecraft.level.dimension() == dimension) return true;
        clear();
        return false;
    }

    public static void select(ResourceLocation id, BlockPos anchor, Direction facing) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        dimension = level.dimension();
        cells = ProjectorBlueprint.cells(level.registryAccess(), id, anchor, facing);
        layers = cells.stream()
                .map(cell -> cell.position().getY())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        selectedLayer = -1;
        refreshedAt = Long.MIN_VALUE;
    }

    public static void scroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!inSelectedDimension(minecraft)
                || minecraft.player == null
                || minecraft.screen != null
                || !minecraft.player.isShiftKeyDown()
                || !(minecraft.player.getMainHandItem().getItem() instanceof HoloProjectorItem)
                        && !(minecraft.player.getOffhandItem().getItem() instanceof HoloProjectorItem)
                || event.getScrollDeltaY() == 0) return;
        int count = layers.size();
        if (count == 0) return;
        int step = event.getScrollDeltaY() < 0 ? 1 : -1;
        selectedLayer = Math.floorMod(selectedLayer + step + 1, count + 1) - 1;
        minecraft.player.displayClientMessage(
                selectedLayer < 0
                        ? Component.translatable("gui.adv_rocketry.structure_preview.projector_all_layers")
                        : Texts.translate(
                                "gui.adv_rocketry.structure_preview.projector_layer", (selectedLayer + 1), count),
                true);
        event.setCanceled(true);
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
                || !inSelectedDimension(minecraft)
                || minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof HoloProjectorItem)
                        && !(minecraft.player.getOffhandItem().getItem() instanceof HoloProjectorItem)) return;
        var camera = event.getCamera().getPosition();
        long gameTime = minecraft.level.getGameTime();
        if (refreshedAt == Long.MIN_VALUE
                || gameTime - refreshedAt >= REFRESH_TICKS
                || gameTime < refreshedAt
                || refreshedLayer != selectedLayer) refresh(minecraft, camera);
        if (ghosts.isEmpty() && invalid.isEmpty()) return;
        float time = gameTime + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float pulse = .5f + .5f * (float) Math.sin(time * Math.PI * 2 / PULSE_TICKS);
        int cycle = (int) (gameTime / CYCLE_TICKS);
        var profile = EffectProfiles.get("diagnostics");
        boolean exploded =
                AdvancedRocketryConfig.advancedVfx() && profile.enabled() && minecraft.player.isShiftKeyDown();
        int bottom = layers.isEmpty() ? 0 : layers.getLast();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer translucent = new AlphaVertexConsumer(buffers.getBuffer(GHOST), GHOST_ALPHA);
        for (ProjectorBlueprint.Cell cell : ghosts) {
            BlockState state =
                    cell.options().get(Math.floorMod(cycle, cell.options().size()));
            BlockPos pos = cell.position();
            pose.pushPose();
            pose.translate(pos.getX() + .5, pos.getY() + .5 + lift(exploded, pos, bottom), pos.getZ() + .5);
            pose.scale(GHOST_SCALE, GHOST_SCALE, GHOST_SCALE);
            pose.translate(-.5, -.5, -.5);
            dispatcher
                    .getModelRenderer()
                    .renderModel(
                            pose.last(),
                            translucent,
                            state,
                            dispatcher.getBlockModel(state),
                            1,
                            1,
                            1,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            null);
            pose.popPose();
        }
        buffers.endBatch(GHOST);
        if (!invalid.isEmpty()) {
            VertexConsumer filled = buffers.getBuffer(RenderType.debugFilledBox());
            for (BlockPos pos : invalid) {
                double lift = lift(exploded, pos, bottom);
                LevelRenderer.addChainedFilledBoxVertices(
                        pose,
                        filled,
                        pos.getX() - .005,
                        pos.getY() - .005 + lift,
                        pos.getZ() - .005,
                        pos.getX() + 1.005,
                        pos.getY() + 1.005 + lift,
                        pos.getZ() + 1.005,
                        1,
                        .1f,
                        .1f,
                        .15f + .3f * pulse);
            }
            buffers.endBatch(RenderType.debugFilledBox());
            VertexConsumer lines = buffers.getBuffer(RenderType.lines());
            for (BlockPos pos : invalid) {
                double lift = lift(exploded, pos, bottom);
                LevelRenderer.renderLineBox(
                        pose,
                        lines,
                        pos.getX() - .005,
                        pos.getY() - .005 + lift,
                        pos.getZ() - .005,
                        pos.getX() + 1.005,
                        pos.getY() + 1.005 + lift,
                        pos.getZ() + 1.005,
                        1,
                        .15f,
                        .15f,
                        .6f + .4f * pulse);
            }
            buffers.endBatch(RenderType.lines());
        }
        pose.popPose();
    }

    private static double lift(boolean exploded, BlockPos pos, int bottom) {
        return exploded ? (pos.getY() - bottom) * .75 : 0;
    }

    private static void refresh(Minecraft minecraft, Vec3 camera) {
        refreshedAt = minecraft.level.getGameTime();
        refreshedLayer = selectedLayer;
        List<ProjectorBlueprint.Cell> nextGhosts = new ArrayList<>();
        List<BlockPos> nextInvalid = new ArrayList<>();
        int drawn = 0;
        for (ProjectorBlueprint.Cell cell : cells) {
            if (++drawn > 4096) break;
            BlockPos pos = cell.position();
            if (selectedLayer >= 0 && pos.getY() != layers.get(selectedLayer)) continue;
            if (!minecraft.level.hasChunkAt(pos) || pos.distToCenterSqr(camera) > 64 * 64) continue;
            BlockState state = minecraft.level.getBlockState(pos);
            if (cell.matches().test(state)) continue;
            if ((state.isAir() || state.canBeReplaced()) && !cell.options().isEmpty()) nextGhosts.add(cell);
            else nextInvalid.add(pos);
        }
        ghosts = List.copyOf(nextGhosts);
        invalid = List.copyOf(nextInvalid);
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alphaValue) {
            delegate.setColor(red, green, blue, Math.round(alphaValue * alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.processing.HoloProjectorItem;
import advRocketry.processing.ProjectorBlueprint;
import advRocketry.util.Texts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Colored outlines show every required cell without modifying the world. */
public final class StructurePreview {
  private static List<ProjectorBlueprint.Cell> cells = List.of();
  private static List<Integer> layers = List.of();
  private static int selectedLayer = -1;
  private static ResourceKey<Level> dimension;

  private StructurePreview() {}

  public static void clear() {
    cells = List.of();
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
    layers =
        cells.stream()
            .map(cell -> cell.position().getY())
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();
    selectedLayer = -1;
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
    var pose = event.getPoseStack();
    var buffer = minecraft.renderBuffers().bufferSource();
    var vertices = buffer.getBuffer(RenderType.lines());
    pose.pushPose();
    pose.translate(-camera.x, -camera.y, -camera.z);
    for (ProjectorBlueprint.Cell cell : cells) {
      if (selectedLayer >= 0 && cell.position().getY() != layers.get(selectedLayer)) continue;
      BlockState state = minecraft.level.getBlockState(cell.position());
      boolean matches = cell.matches().test(state);
      if (state.isAir() && matches) continue;
      draw(pose, vertices, cell.position(), matches);
    }
    pose.popPose();
    buffer.endBatch(RenderType.lines());
  }

  private static void draw(PoseStack pose, VertexConsumer vertices, BlockPos pos, boolean matches) {
    LevelRenderer.renderLineBox(
        pose,
        vertices,
        pos.getX() + .01,
        pos.getY() + .01,
        pos.getZ() + .01,
        pos.getX() + .99,
        pos.getY() + .99,
        pos.getZ() + .99,
        matches ? .1f : .8f,
        matches ? 1f : .3f,
        1f,
        .7f);
  }
}

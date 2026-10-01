// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.Main;
import advRocketry.processing.MachineType;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/** Original model coordinates and animation transforms, using modern vertex buffers. */
public final class ProcessingRenderer implements BlockEntityRenderer<ProcessingBlockEntity> {
  private static final Map<MachineType, ObjModel> MODELS = new HashMap<>();

  public ProcessingRenderer(BlockEntityRendererProvider.Context context) {}

  public static void clearModels() {
    MODELS.clear();
  }

  private static ResourceLocation modelPath(String id) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/" + id + ".obj");
  }

  private static ResourceLocation texturePath(String id) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/" + id + ".png");
  }

  @Override
  public AABB getRenderBoundingBox(ProcessingBlockEntity tile) {
    return new AABB(tile.getBlockPos()).inflate(6);
  }

  @Override
  public void render(
      ProcessingBlockEntity tile,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    MachineType machine = tile.machine();
    if (machine == MachineType.ELECTRIC_ARC_FURNACE
        || !tile.getBlockState().getValue(ProcessingBlock.FORMED)) return;
    ObjModel model = MODELS.computeIfAbsent(machine, type -> new ObjModel(modelPath(type.id())));
    ResourceLocation texture = texturePath(machine.id());
    VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    Direction facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    // The opaque controller's interior has no light. Sample its exposed front instead.
    light = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
    float progress = tile.progress(partialTick);
    pose.pushPose();
    pose.translate(0.5, 0, 0.5);
    float angle;
    if (machine == MachineType.CHEMICAL_REACTOR
        || machine == MachineType.CENTRIFUGE
        || machine == MachineType.ELECTROLYZER) {
      angle = (facing.getStepZ() == 1 ? 180 : 0) - facing.getStepX() * 90;
    } else if (machine == MachineType.LATHE || machine == MachineType.PRECISION_LASER_ETCHER) {
      angle = (facing.getStepX() == 1 ? 0 : 180) + facing.getStepZ() * 90;
    } else {
      angle = (facing.getStepX() == 1 ? 180 : 0) + facing.getStepZ() * 90;
    }
    pose.mulPose(Axis.YP.rotationDegrees(angle));
    switch (machine) {
      case LATHE -> pose.translate(-0.5, -1, -2.5);
      case ROLLING_MACHINE, CRYSTALLIZER -> pose.translate(-0.5, 0, -1.5);
      case CHEMICAL_REACTOR -> pose.translate(1.5, -1, -0.5);
      case CENTRIFUGE -> pose.translate(-0.5, -1, 1.5);
      case PRECISION_LASER_ETCHER -> pose.translate(0.5, 0, 1.5);
      case CUTTING_MACHINE -> pose.translate(-0.5, 0, -1.5);
      case ELECTROLYZER -> pose.translate(1.5, 0, -0.5);
      default -> pose.translate(-0.5, 0, -0.5);
    }
    switch (machine) {
      case LATHE -> {
        render(model, "Hull", pose, vertices, light, overlay);
        pose.pushPose();
        if (tile.running())
          pose.translate(0, 0, progress < 0.95 ? -(progress / 0.85) : -((1 - progress) / 0.05));
        render(model, "Tool", pose, vertices, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.375, 0.9375, 0);
        if (tile.running()) pose.mulPose(Axis.ZP.rotationDegrees(progress * 1500));
        render(model, "Shaft", pose, vertices, light, overlay);
        pose.popPose();
        if (tile.running()) {
          pose.pushPose();
          pose.translate(.375, 1.1875, 0);
          pose.mulPose(Axis.ZP.rotationDegrees(progress * 1500));
          int color = Minecraft.getInstance().getItemColors().getColor(tile.displayedOutput(), 0);
          model.renderPart("Rod", pose, vertices, light, overlay, color | 0xff000000);
          pose.popPose();
        }
      }
      case ROLLING_MACHINE -> {
        render(model, "Hull", pose, vertices, light, overlay);
        roller(model, "Roller_1", 1.375, 0.6875, -progress * 720, pose, vertices, light, overlay);
        roller(model, "Roller_2", 1.9375, 0.6875, -progress * 720, pose, vertices, light, overlay);
        roller(model, "Roller_3", 1.65625, 1.125, progress * 720, pose, vertices, light, overlay);
      }
      case CENTRIFUGE -> {
        render(model, "Hull", pose, vertices, light, overlay);
        pose.pushPose();
        if (tile.running())
          pose.mulPose(
              Axis.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * -100));
        render(model, "Cylinder", pose, vertices, light, overlay);
        pose.popPose();
      }
      case PRECISION_ASSEMBLER -> {
        render(model, "Hull", pose, vertices, light, overlay);
        pose.pushPose();
        if (tile.running()) pose.translate(0, 0, 3 * progress);
        render(model, "Tray", pose, vertices, light, overlay);
        pose.popPose();
        if (tile.running())
          movingItem(
              tile,
              tile.displayedOutput(),
              1,
              1.2,
              3 * progress + .75,
              pose,
              buffers,
              light,
              overlay);
        vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        int stage = 0;
        for (String group : new String[] {"ProcessA", "ProcessB", "ProcessC"}) {
          pose.pushPose();
          float step = 18 * progress - 6 * stage;
          if (tile.running()) {
            if (stage < 2 && step > 2 && step < 4)
              pose.translate(0, -.25 * (1 - Math.abs(step - 3)), 0);
            if (stage == 2 && step > 1 && step < 3) {
              pose.translate(1.55, 1.47, 0);
              pose.mulPose(Axis.ZP.rotationDegrees(90 * (1 - Math.abs(step - 2))));
              pose.translate(-1.55, -1.47, 0);
            }
          }
          render(model, group, pose, vertices, light, overlay);
          pose.popPose();
          stage++;
        }
      }
      case PRECISION_LASER_ETCHER -> {
        render(model, "Hull", pose, vertices, light, overlay);
        pose.pushPose();
        float sweep = (progress * 16) % 1;
        if (tile.running())
          pose.translate(
              0,
              0,
              progress < .875
                  ? progress - sweep / 16 + (sweep > .875 ? (sweep - .875) / 2 : 0)
                  : (1 - progress) / .15);
        render(model, "Mount", pose, vertices, light, overlay);
        pose.pushPose();
        if (tile.running() && progress < .875)
          pose.translate(-(sweep < .875 ? sweep : (1 - sweep) / .15), 0, 0);
        render(model, "Laser", pose, vertices, light, overlay);
        pose.popPose();
        pose.popPose();
      }
      case CUTTING_MACHINE -> {
        render(model, "Hull", pose, vertices, light, overlay);
        pose.pushPose();
        if (tile.running()) {
          pose.translate(1, 1, 1.5);
          pose.mulPose(Axis.XP.rotationDegrees(-6 * (tile.progressTicks() + partialTick)));
          pose.translate(-1, -1, -1.5);
        }
        render(model, "Saw", pose, vertices, light, overlay);
        pose.popPose();
        if (tile.running())
          movingItem(
              tile,
              progress < .65 ? tile.displayedInput() : tile.displayedOutput(),
              1,
              1.05,
              2.2 * progress + .45,
              pose,
              buffers,
              light,
              overlay);
      }
      case CRYSTALLIZER -> {
        render(model, "Hull", pose, vertices, light, overlay);
        ItemStack output = tile.displayedOutput();
        if (tile.running() && !output.isEmpty()) {
          for (double z : new double[] {.7, 1.5, 2.3}) {
            pose.pushPose();
            pose.translate(1, 1.2, z);
            pose.mulPose(
                Axis.YP.rotationDegrees((tile.getLevel().getGameTime() + partialTick) % 360));
            pose.scale(progress, progress, progress);
            item(tile, output, pose, buffers, light, overlay);
            pose.popPose();
          }
          int tint = Minecraft.getInstance().getItemColors().getColor(output, 0);
          int liquid =
              0xe4000000
                  | ((tint & 255) / 2 << 16)
                  | (((tint >> 8) & 255) / 2 << 8)
                  | ((tint >> 16) & 255) / 2;
          pose.pushPose();
          pose.translate(0, 1.1, 0);
          pose.scale(1, Math.max(0, progress < .05 ? 20 * progress : 1.1f - progress * 1.111f), 1);
          pose.translate(0, -1.1, 0);
          model.renderPart(
              "Liquid", pose, buffers.getBuffer(RenderType.debugQuads()), light, overlay, liquid);
          pose.popPose();
        }
      }
      case ELECTROLYZER -> {
        model.renderAll(pose, vertices, light, overlay, -1);
        if (tile.running()) {
          float time = tile.getLevel().getGameTime() & 0xffff;
          float y = .1f * (float) Math.sin(time * 2), z = .1f * (float) Math.sin((time + 200) * 3);
          float[] ys = {0, y, -y, y, 0}, zs = {0, z, -z, z, 0};
          VertexConsumer arc = buffers.getBuffer(RenderType.lightning());
          for (int segment = 0; segment < 4; segment++) {
            float x0 = -1.8f + segment * .15f, x1 = x0 + .15f;
            for (int plane = 0; plane < 2; plane++) {
              float dy = plane == 0 ? .01f : 0, dz = plane == 1 ? .01f : 0;
              arc.addVertex(pose.last(), x0, 1.4f + ys[segment] - dy, 1 + zs[segment] - dz)
                  .setColor(163, 163, 255, 102);
              arc.addVertex(pose.last(), x1, 1.4f + ys[segment + 1] - dy, 1 + zs[segment + 1] - dz)
                  .setColor(163, 163, 255, 102);
              arc.addVertex(pose.last(), x1, 1.4f + ys[segment + 1] + dy, 1 + zs[segment + 1] + dz)
                  .setColor(163, 163, 255, 102);
              arc.addVertex(pose.last(), x0, 1.4f + ys[segment] + dy, 1 + zs[segment] + dz)
                  .setColor(163, 163, 255, 102);
            }
          }
        }
      }
      default -> model.renderAll(pose, vertices, light, overlay, 0xffffffff);
    }
    pose.popPose();
  }

  private static void movingItem(
      ProcessingBlockEntity tile,
      ItemStack stack,
      double x,
      double y,
      double z,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    pose.pushPose();
    pose.translate(x, y, z);
    pose.mulPose(Axis.XP.rotationDegrees(90));
    item(tile, stack, pose, buffers, light, overlay);
    pose.popPose();
  }

  private static void item(
      ProcessingBlockEntity tile,
      ItemStack stack,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (stack.isEmpty()) return;
    pose.scale(.5f, .5f, .5f);
    Minecraft.getInstance()
        .getItemRenderer()
        .renderStatic(
            stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, tile.getLevel(), 0);
  }

  private static void render(
      ObjModel model,
      String group,
      PoseStack pose,
      VertexConsumer vertices,
      int light,
      int overlay) {
    model.renderPart(group, pose, vertices, light, overlay);
  }

  private static void roller(
      ObjModel model,
      String group,
      double x,
      double y,
      float angle,
      PoseStack pose,
      VertexConsumer vertices,
      int light,
      int overlay) {
    pose.pushPose();
    pose.translate(x, y, 0);
    pose.mulPose(Axis.ZP.rotationDegrees(angle));
    render(model, group, pose, vertices, light, overlay);
    pose.popPose();
  }
}

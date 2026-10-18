// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.Main;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.client.effects.MachineIndicators;
import advRocketry.client.effects.ShaderCompatibility;
import advRocketry.orbit.HorizontalOrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.RailgunLogic;
import advRocketry.processing.client.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Renders the original MIT solar array OBJ at its controller. */
public final class OrbitalRenderer implements BlockEntityRenderer<OrbitalBlockEntity> {
  private static final ResourceLocation MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/solar_array.obj");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/solar_array.png");
  private static final ResourceLocation OBSERVATORY_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/observatory.obj");
  private static final ResourceLocation OBSERVATORY_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/observatory.png");
  private static final ResourceLocation ELEVATOR_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/space_elevator.obj");
  private static final ResourceLocation ELEVATOR_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/space_elevator.png");
  private static final ResourceLocation AREA_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/area_gravity_controller.obj");
  private static final ResourceLocation AREA_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/machine/area_gravity_controller.png");
  private static final ResourceLocation LASER_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/orbital_laser.obj");
  private static final ResourceLocation LASER_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/orbital_laser.png");
  private static final ResourceLocation SCANNER_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/biome_scanner.obj");
  private static final ResourceLocation SCANNER_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/biome_scanner.png");
  private static final ResourceLocation BEACON_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/beacon.obj");
  private static final ResourceLocation BEACON_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/beacon.png");
  private static final ResourceLocation RAILGUN_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/railgun.obj");
  private static final ResourceLocation RAILGUN_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/railgun.png");
  private static final ResourceLocation PROCESSOR_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/astrobody_data_processor.obj");
  private static final ResourceLocation PROCESSOR_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/machine/astrobody_data_processor.png");
  private static final ResourceLocation TERRAFORMER_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/atmosphere_terraformer.obj");
  private static final ResourceLocation TERRAFORMER_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/machine/atmosphere_terraformer.png");
  private static final ResourceLocation BLACK_HOLE_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/black_hole_generator.obj");
  private static final ResourceLocation BLACK_HOLE_GLASS =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "multiblock/black_hole_generator_glass.obj");
  private static final ResourceLocation BLACK_HOLE_LIGHTS =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "multiblock/black_hole_generator_lights.obj");
  private static final ResourceLocation BLACK_HOLE_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/machine/black_hole_generator.png");
  private static final ResourceLocation MICROWAVE_MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/microwave_receiver.obj");
  private static final ResourceLocation MICROWAVE_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/microwave_receiver.png");
  private static final RenderType CONTAINMENT_GLASS =
      EffectRenderTypes.containmentGlass(BLACK_HOLE_TEXTURE);
  private static final Map<ResourceLocation, ObjModel> MODELS = new HashMap<>();

  public OrbitalRenderer(BlockEntityRendererProvider.Context context) {}

  public static void clearModels() {
    MODELS.clear();
    HologramBodyRenderer.clearModel();
  }

  private static ObjModel model(ResourceLocation id) {
    return MODELS.computeIfAbsent(id, ObjModel::new);
  }

  private static float yaw(Direction front) {
    return (front.getStepX() == 1 ? 180 : 0) + front.getStepZ() * 90f;
  }

  private static Direction front(OrbitalBlockEntity entity) {
    return entity.getBlockState().getValue(HorizontalOrbitalBlock.FACING);
  }

  @Override
  public AABB getRenderBoundingBox(OrbitalBlockEntity entity) {
    if (entity.holographicSelector()) return new AABB(entity.getBlockPos()).inflate(20);
    if (entity.spaceElevator()) return new AABB(entity.getBlockPos()).inflate(6, 3000, 6);
    if (entity.microwaveReceiver()) return new AABB(entity.getBlockPos()).inflate(3, 65, 3);
    if (entity.orbitalLaser()) return new AABB(entity.getBlockPos()).inflate(12);
    if (entity.railgun()) return new AABB(entity.getBlockPos()).inflate(11);
    if (entity.astrobodyProcessor()) return new AABB(entity.getBlockPos()).inflate(3);
    if (entity.atmosphereTerraformer()) return new AABB(entity.getBlockPos()).inflate(16);
    if (entity.blackHoleGenerator()) return new AABB(entity.getBlockPos()).inflate(5);
    return new AABB(entity.getBlockPos()).inflate(entity.observatory() ? 6 : 3);
  }

  @Override
  public void render(
      OrbitalBlockEntity entity,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    renderMachine(entity, partialTick, pose, buffers, light, overlay);
    Direction exposed =
        entity.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)
            : Direction.UP;
    if (exposed.getAxis().isHorizontal())
      MachineIndicators.render(
          pose,
          buffers,
          exposed,
          entity.formed
              && (entity.powerMadeLastTick > 0
                  || entity.terraformerRunning
                  || entity.laser.running),
          1,
          Vec3.atCenterOf(entity.getBlockPos()));
  }

  private void renderMachine(
      OrbitalBlockEntity entity,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    OrbitalEffects.render(entity, partialTick, pose, buffers);
    Direction exposed =
        entity.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)
            : Direction.UP;
    light = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(exposed));
    if (entity.holographicSelector()) {

      return;
    }
    if (entity.blackHoleGenerator()) {
      if (!entity.formed) return;
      pose.pushPose();
      pose.translate(.5, -1, .5);
      pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity)) + 90));
      pose.translate(0, 0, 1);
      model(BLACK_HOLE_MODEL)
          .renderAll(
              pose,
              buffers.getBuffer(RenderType.entityCutoutNoCull(BLACK_HOLE_TEXTURE)),
              light,
              overlay,
              -1);
      boolean active = entity.powerMadeLastTick > 0;
      if (!ShaderCompatibility.shadowPass()) {
        model(BLACK_HOLE_GLASS)
            .renderAll(pose, buffers.getBuffer(CONTAINMENT_GLASS), light, overlay, -1);
        model(BLACK_HOLE_LIGHTS)
            .renderAll(
                pose,
                buffers.getBuffer(
                    active
                        ? RenderType.entityTranslucentEmissive(BLACK_HOLE_TEXTURE)
                        : RenderType.entityCutoutNoCull(BLACK_HOLE_TEXTURE)),
                active ? LightTexture.FULL_BRIGHT : light,
                overlay,
                active ? -1 : 0xff53606a);
      }
      pose.popPose();
      return;
    }
    if (entity.microwaveReceiver()) {
      renderMicrowaveReceiver(entity, pose, buffers, light, overlay);
      return;
    }
    if (entity.observatory()) {
      renderObservatory(entity, pose, buffers, light, overlay);
      return;
    }
    if (entity.biomeScanner()) {
      if (!entity.formed) return;
      pose.pushPose();
      pose.translate(0, 0, 1);
      model(SCANNER_MODEL)
          .renderAll(
              pose,
              buffers.getBuffer(RenderType.entityCutoutNoCull(SCANNER_TEXTURE)),
              light,
              overlay,
              -1);
      pose.popPose();
      return;
    }
    if (entity.beacon()) {
      if (!entity.beaconEnabled || !entity.formed) return;
      pose.pushPose();
      pose.translate(-.5, 0, 1.5);
      model(BEACON_MODEL)
          .renderAll(
              pose,
              buffers.getBuffer(RenderType.entityCutoutNoCull(BEACON_TEXTURE)),
              light,
              overlay,
              -1);
      pose.popPose();
      return;
    }
    if (entity.railgun()) {
      if (!entity.formed) return;
      pose.pushPose();
      var offset = RailgunLogic.emitter(entity).subtract(entity.getBlockPos());
      pose.translate(offset.getX() + .5, offset.getY() + .002, offset.getZ() + .5);
      pose.scale(1.002f, 1.002f, 1.002f);
      pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity))));
      model(RAILGUN_MODEL)
          .renderAll(
              pose,
              buffers.getBuffer(RenderType.entityCutoutNoCull(RAILGUN_TEXTURE)),
              light,
              overlay,
              -1);
      pose.popPose();
      return;
    }
    if (entity.astrobodyProcessor()) {
      if (!entity.formed) return;
      pose.pushPose();
      pose.translate(.5, 0, .5);
      pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity))));
      pose.translate(-.5, -1, -1.5);
      model(PROCESSOR_MODEL)
          .renderAll(
              pose,
              buffers.getBuffer(RenderType.entityCutoutNoCull(PROCESSOR_TEXTURE)),
              light,
              overlay,
              -1);
      pose.popPose();
      return;
    }
    if (entity.atmosphereTerraformer()) {
      if (!entity.formed) return;
      ObjModel terraformerModel = model(TERRAFORMER_MODEL);
      pose.pushPose();
      pose.translate(.5, 0, .5);
      pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity))));
      pose.translate(1, 0, 0);
      var terraformer = buffers.getBuffer(RenderType.entityCutoutNoCull(TERRAFORMER_TEXTURE));
      for (String group : new String[] {"Fan", "Body", "DarkBody", "Floor", "Tubes", "BlueRing"})
        terraformerModel.renderPart(group, pose, terraformer, light, overlay);
      pose.popPose();
      return;
    }
    if (entity.spaceElevator()) {
      renderElevator(entity, partialTick, pose, buffers, light, overlay);
      return;
    }
    if (entity.areaGravityController()) {
      renderAreaGravity(entity, partialTick, pose, buffers, light, overlay);
      return;
    }
    if (entity.orbitalLaser()) {
      renderLaser(entity, pose, buffers, light, overlay);
      return;
    }
    if (!entity.solarArray() || !entity.formed) return;
    pose.pushPose();
    pose.translate(.5, 0, .5);
    pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity)) + 180));
    pose.translate(-.5, .002, .5);
    // Expand around the horizontal center: scaling from x=0 leaves the front trim
    // coplanar with the output-port blocks beneath the formed model.
    pose.translate(.5, 0, -.5);
    pose.scale(1.004f, 1.002f, 1.004f);
    pose.translate(-.5, 0, .5);
    model(MODEL)
        .renderAll(
            pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay, -1);
    SolarArrayEffects.render(entity, partialTick, pose, buffers);
    pose.popPose();
  }

  private static void renderLaser(
      OrbitalBlockEntity entity,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!entity.formed) return;
    pose.pushPose();
    pose.translate(.5, 0, .5);
    pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity))));
    pose.translate(2.5, 0, 4.5);
    model(LASER_MODEL)
        .renderAll(
            pose,
            buffers.getBuffer(RenderType.entityCutoutNoCull(LASER_TEXTURE)),
            light,
            overlay,
            -1);
    pose.popPose();
  }

  private static void renderAreaGravity(
      OrbitalBlockEntity entity,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!entity.formed) return;
    ObjModel areaModel = model(AREA_MODEL);
    pose.pushPose();
    // Separate the hull from coplanar floor and support-block faces.
    pose.translate(.5, -.498, .5);
    pose.scale(1.002f, 1.002f, 1.002f);
    var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(AREA_TEXTURE));
    areaModel.renderPart("Hull", pose, vertices, light, overlay);
    pose.mulPose(
        Axis.YP.rotationDegrees(
            (entity.getLevel().getGameTime() + partialTick) * 10f * entity.areaCurrentGravity));
    for (int arm = 0; arm < 5; arm++) {
      pose.mulPose(Axis.YP.rotationDegrees(72));
      areaModel.renderPart("Arm", pose, vertices, light, overlay);
    }
    pose.popPose();
  }

  private static void renderElevator(
      OrbitalBlockEntity entity,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!entity.formed) return;
    ObjModel elevatorModel = model(ELEVATOR_MODEL);
    Direction front = front(entity);
    pose.pushPose();
    pose.translate(.5, 0, .5);
    boolean station = entity.getLevel().dimension() == OrbitalRegistry.SPACE_DIMENSION;
    if (station)
      pose.mulPose((front.getAxis() == Direction.Axis.X ? Axis.XP : Axis.ZP).rotationDegrees(180));
    pose.mulPose(Axis.YP.rotationDegrees(yaw(front)));
    pose.translate(4.5, station ? -1 : 0, .5);
    var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(ELEVATOR_TEXTURE));
    elevatorModel.renderPart("Anchor", pose, vertices, light, overlay);
    if (!entity.elevatorDimension.isEmpty())
      elevatorModel.renderPart("Tether", pose, vertices, light, overlay);
    var profile = EffectProfiles.get("elevator");
    if (!entity.elevatorDimension.isEmpty()
        && EffectGeometry.visible(Vec3.atCenterOf(entity.getBlockPos()), profile)
        && EffectGeometry.reserve()) {
      // Tether mesh spans x=0..1 and z=-1..0 in model space.
      pose.translate(.5, 0, -.5);
      VertexConsumer glow = buffers.getBuffer(EffectRenderTypes.GLOW);
      double shift = EffectGeometry.time(entity.getLevel(), partialTick, profile) % 256;
      for (int ribbon = 0; ribbon < 4; ribbon++) {
        double angle = Math.PI / 4 + ribbon * Math.PI / 2;
        double x = Math.cos(angle) * .95, z = Math.sin(angle) * .95;
        EffectGeometry.beam(
            pose, glow, new Vec3(x, 1, z), new Vec3(x, 260, z), .055, profile.argb(.35f));
      }
      for (int index = 0; index < 16; index++) {
        double height = 4 + (index * 16 + shift) % 256;
        EffectGeometry.ring(
            pose, glow, 1.05, height, .06, profile.argb(.5f), EffectGeometry.segments(profile));
      }
    }
    pose.popPose();
  }

  private static void renderMicrowaveReceiver(
      OrbitalBlockEntity entity,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!entity.formed) return;
    pose.pushPose();
    // The exported Blockbench model is already in blocks, centered over the controller.
    // Raise its plinth clear of the controller and constituent panel surfaces.
    pose.translate(0, 1.01, 0);
    model(MICROWAVE_MODEL)
        .renderAll(
            pose,
            buffers.getBuffer(RenderType.entityCutoutNoCull(MICROWAVE_TEXTURE)),
            light,
            overlay,
            -1);
    pose.popPose();
    Matrix4f matrix = pose.last().pose();
    if (entity.powerMadeLastTick <= 0) return;
    VertexConsumer beam = buffers.getBuffer(EffectRenderTypes.GLOW);
    for (int i = 0; i < 8; i++) {
      double angle = i * Math.PI / 4;
      double next = (i + 1) * Math.PI / 4;
      float x0 = (float) (Math.cos(angle) * 1.7 + .5);
      float z0 = (float) (Math.sin(angle) * 1.7 + .5);
      float x1 = (float) (Math.cos(next) * 1.7 + .5);
      float z1 = (float) (Math.sin(next) * 1.7 + .5);
      beam.addVertex(matrix, x0, 3.0f, z0).setColor(85, 100, 110, 40);
      beam.addVertex(matrix, x1, 3.0f, z1).setColor(85, 100, 110, 40);
      beam.addVertex(matrix, .5f, 65, .5f).setColor(85, 100, 110, 0);
      beam.addVertex(matrix, .5f, 65, .5f).setColor(85, 100, 110, 0);
    }
  }

  private static void renderObservatory(
      OrbitalBlockEntity entity,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!entity.formed) return;
    ObjModel observatoryModel = model(OBSERVATORY_MODEL);
    pose.pushPose();
    pose.translate(.5, 0, .5);
    pose.mulPose(Axis.YP.rotationDegrees(yaw(front(entity))));
    pose.translate(2, -1, 0);
    var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(OBSERVATORY_TEXTURE));
    observatoryModel.renderPart("Base", pose, vertices, light, overlay);
    float opening = entity.observation.openProgress / 100f;
    if (opening > 0) {
      observatoryModel.renderPart("Axis", pose, vertices, light, overlay);
      observatoryModel.renderPart("Scope", pose, vertices, light, overlay);
    }
    pose.pushPose();
    pose.translate(0, 0, -opening * 1.125f);
    observatoryModel.renderPart("CasingXMinus", pose, vertices, light, overlay);
    pose.popPose();
    pose.pushPose();
    pose.translate(0, 0, opening * 1.125f);
    observatoryModel.renderPart("CasingXPlus", pose, vertices, light, overlay);
    pose.popPose();
    pose.popPose();
  }
}

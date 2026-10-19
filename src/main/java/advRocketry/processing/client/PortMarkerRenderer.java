package advRocketry.processing.client;

import advRocketry.Main;
import advRocketry.processing.MachinePartBlock;
import advRocketry.processing.PortBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** A visible role marker remains on every accessible face of a formed machine port. */
public final class PortMarkerRenderer implements BlockEntityRenderer<PortBlockEntity> {
  private static final float LOW = 1f / 16f;
  private static final float HIGH = 15f / 16f;
  private static final float OUTSIDE = 1f / 32f;

  public PortMarkerRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public AABB getRenderBoundingBox(PortBlockEntity port) {
    return new AABB(port.getBlockPos()).inflate(OUTSIDE);
  }

  @Override
  public void render(
      PortBlockEntity port,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    if (!port.getBlockState().getValue(MachinePartBlock.ASSEMBLED)) return;
    ResourceLocation texture =
        ResourceLocation.fromNamespaceAndPath(
            Main.MODID, "textures/block/port_marker_" + port.kind().name().toLowerCase() + ".png");
    VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    for (Direction face : Direction.values())
      drawFace(
          vertices,
          pose,
          face,
          LevelRenderer.getLightColor(port.getLevel(), port.getBlockPos().relative(face)),
          overlay);
  }

  private static void drawFace(
      VertexConsumer vertices, PoseStack pose, Direction face, int light, int overlay) {
    float near = -OUTSIDE;
    float far = 1 + OUTSIDE;
    float[][] corners =
        switch (face) {
          case NORTH ->
              new float[][] {
                {HIGH, HIGH, near}, {LOW, HIGH, near}, {LOW, LOW, near}, {HIGH, LOW, near}
              };
          case SOUTH ->
              new float[][] {
                {LOW, HIGH, far}, {HIGH, HIGH, far}, {HIGH, LOW, far}, {LOW, LOW, far}
              };
          case WEST ->
              new float[][] {
                {near, HIGH, LOW}, {near, HIGH, HIGH}, {near, LOW, HIGH}, {near, LOW, LOW}
              };
          case EAST ->
              new float[][] {
                {far, HIGH, HIGH}, {far, HIGH, LOW}, {far, LOW, LOW}, {far, LOW, HIGH}
              };
          case UP ->
              new float[][] {
                {LOW, far, LOW}, {HIGH, far, LOW}, {HIGH, far, HIGH}, {LOW, far, HIGH}
              };
          case DOWN ->
              new float[][] {
                {LOW, near, HIGH}, {HIGH, near, HIGH}, {HIGH, near, LOW}, {LOW, near, LOW}
              };
        };
    float[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
    for (int i = 0; i < 4; i++)
      vertices
          .addVertex(pose.last(), corners[i][0], corners[i][1], corners[i][2])
          .setColor(-1)
          .setUv(uv[i][0], uv[i][1])
          .setOverlay(overlay)
          .setLight(light)
          .setNormal(pose.last(), face.getStepX(), face.getStepY(), face.getStepZ());
  }
}

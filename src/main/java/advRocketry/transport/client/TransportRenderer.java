package advRocketry.transport.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.transport.TransportBlock;
import advRocketry.transport.TransportBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Cosmetic activity streaks share world-space phase across connected fiber segments. */
public final class TransportRenderer implements BlockEntityRenderer<TransportBlockEntity> {
  public TransportRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      TransportBlockEntity pipe,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    var profile = EffectProfiles.get("transport");
    if (pipe.getLevel() == null
        || !EffectGeometry.visible(Vec3.atCenterOf(pipe.getBlockPos()), profile)
        || !EffectGeometry.reserve()) return;
    boolean active = pipe.getBlockState().getValue(TransportBlock.ACTIVE);
    boolean moving = active && !AdvancedRocketryConfig.reducedMotion();
    double time = EffectGeometry.time(pipe.getLevel(), partialTick, profile) * .06;
    pose.pushPose();
    pose.translate(.5, .5, .5);
    var vertices = buffers.getBuffer(EffectRenderTypes.EMISSIVE);
    for (Direction.Axis axis : Direction.Axis.values()) {
      Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
      boolean forwards = pipe.getBlockState().getValue(TransportBlock.SIDES[positive.ordinal()]);
      boolean backwards =
          pipe.getBlockState().getValue(TransportBlock.SIDES[positive.getOpposite().ordinal()]);
      if (!forwards && !backwards) continue;
      double low = backwards ? -.5 : -.125, high = forwards ? .5 : .125;
      double origin =
          axis.choose(
                  pipe.getBlockPos().getX(), pipe.getBlockPos().getY(), pipe.getBlockPos().getZ())
              + .5;
      for (int strand = 0; strand < 4; strand++) {
        double u = (strand % 2 == 0 ? -1 : 1) * .052;
        double v = (strand < 2 ? -1 : 1) * .052;
        EffectGeometry.entityBeam(
            pose,
            vertices,
            point(axis, low, u, v),
            point(axis, high, u, v),
            .008,
            profile.argb(active ? .7f : .22f));
        if (!moving) continue;
        double phase = time + strand * .43;
        double head = Math.floor((origin + low - phase) / 2) * 2 + phase - origin;
        for (int packet = 0; packet < 2; packet++, head += 2) {
          streak(
              pose,
              vertices,
              axis,
              u,
              v,
              Math.max(low, head - .26),
              Math.min(high, head),
              .012,
              profile.argb(.35f));
          streak(
              pose,
              vertices,
              axis,
              u,
              v,
              Math.max(low, head - .12),
              Math.min(high, head),
              .007,
              profile.argb(1));
        }
      }
    }
    pose.popPose();
  }

  private static Vec3 point(Direction.Axis axis, double along, double u, double v) {
    return switch (axis) {
      case X -> new Vec3(along, u, v);
      case Y -> new Vec3(u, along, v);
      case Z -> new Vec3(u, v, along);
    };
  }

  private static void streak(
      PoseStack pose,
      VertexConsumer vertices,
      Direction.Axis axis,
      double u,
      double v,
      double from,
      double to,
      double width,
      int color) {
    if (to > from)
      EffectGeometry.entityBeam(
          pose, vertices, point(axis, from, u, v), point(axis, to, u, v), width, color);
  }
}

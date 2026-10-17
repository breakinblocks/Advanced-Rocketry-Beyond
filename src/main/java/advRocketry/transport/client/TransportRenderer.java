package advRocketry.transport.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.transport.TransportBlock;
import advRocketry.transport.TransportBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Activity halos use the existing synchronized ACTIVE state; no new packet traffic. */
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
        || !pipe.getBlockState().getValue(TransportBlock.ACTIVE)
        || !EffectGeometry.visible(Vec3.atCenterOf(pipe.getBlockPos()), profile)) return;
    if (!EffectGeometry.reserve()) return;
    double time = EffectGeometry.time(pipe.getLevel(), partialTick, profile);
    double phase =
        AdvancedRocketryConfig.reducedMotion()
            ? .35
            : (time * .04 + Math.floorMod(pipe.getBlockPos().hashCode(), 16) / 16d) % 1;
    pose.pushPose();
    pose.translate(.5, .5, .5);
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    for (Direction.Axis axis : Direction.Axis.values()) {
      Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
      Direction negative = positive.getOpposite();
      if (!pipe.getBlockState().getValue(TransportBlock.SIDES[positive.ordinal()])
          && !pipe.getBlockState().getValue(TransportBlock.SIDES[negative.ordinal()])) continue;
      pose.pushPose();
      if (axis == Direction.Axis.X) pose.mulPose(Axis.ZP.rotationDegrees(90));
      else if (axis == Direction.Axis.Z) pose.mulPose(Axis.XP.rotationDegrees(90));
      EffectGeometry.ring(
          pose,
          vertices,
          .29 + phase * .08,
          0,
          .014,
          profile.argb((float) (1 - phase) * .75f),
          EffectGeometry.segments(profile));
      pose.popPose();
    }
    pose.popPose();
  }
}

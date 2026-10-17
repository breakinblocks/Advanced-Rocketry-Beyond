package advRocketry.transport.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.transport.TransportBlock;
import advRocketry.transport.TransportBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
    double phase = (time * .04 + Math.floorMod(pipe.getBlockPos().hashCode(), 16) / 16d) % 1;
    pose.pushPose();
    pose.translate(.5, .5, .5);
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    EffectGeometry.ring(
        pose, vertices, .23 + phase * .12, 0, .012, profile.argb((float) (1 - phase) * .6f), 16);
    pose.popPose();
  }
}

package advRocketry.orbit.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfile;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.client.effects.SceneDistortion;
import advRocketry.client.effects.ShaderCompatibility;
import advRocketry.orbit.HorizontalOrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** A low singularity with a warped accretion flow that remains visible under shader packs. */
final class BlackHoleContainment {
  private static final ResourceLocation WHITE =
      ResourceLocation.fromNamespaceAndPath("neoforge", "textures/white.png");

  private BlackHoleContainment() {}

  static void render(
      OrbitalBlockEntity entity,
      PoseStack pose,
      MultiBufferSource buffers,
      EffectProfile profile,
      double time) {
    if (ShaderCompatibility.shadowPass()) return;
    var back = entity.getBlockState().getValue(HorizontalOrbitalBlock.FACING).getOpposite();
    Vec3 offset = new Vec3(back.getStepX(), .05, back.getStepZ());
    SceneDistortion.submit(Vec3.atCenterOf(entity.getBlockPos()).add(offset), .24f);
    pose.pushPose();
    pose.translate(offset.x, offset.y, offset.z);
    var core = buffers.getBuffer(RenderType.entitySolid(WHITE));
    int segments = EffectGeometry.segments(profile);
    for (int row = 0; row < 12; row++) {
      double a = Math.PI * row / 12, b = Math.PI * (row + 1) / 12;
      for (int i = 0; i < segments; i++) {
        double u = i * Math.PI * 2 / segments, v = (i + 1) * Math.PI * 2 / segments;
        sphereVertex(pose, core, a, u);
        sphereVertex(pose, core, b, u);
        sphereVertex(pose, core, b, v);
        sphereVertex(pose, core, a, v);
      }
    }
    var glow = buffers.getBuffer(EffectRenderTypes.GLOW);
    for (int band = 0; band < 7; band++) {
      double radius = .28 + band * .065;
      for (int i = 0; i < segments; i++) {
        double a = i * Math.PI * 2 / segments, b = (i + 1) * Math.PI * 2 / segments;
        double pulse = .55 + .25 * Math.sin(a * 3 - time * .075 + band);
        int rgb = band < 3 ? profile.color() : 0x9480d8;
        int color = ((int) (pulse * profile.opacity() * 255) << 24) | rgb;
        EffectGeometry.beam(
            pose, glow, flow(radius, a, band, time), flow(radius, b, band, time), .018, color);
      }
    }
    // The vertical lensed arc exposes the lower singularity even at a shallow viewing angle.
    Vec3 towardCamera =
        Minecraft.getInstance()
            .gameRenderer
            .getMainCamera()
            .getPosition()
            .subtract(Vec3.atCenterOf(entity.getBlockPos()).add(offset));
    Vec3 right = new Vec3(towardCamera.z, 0, -towardCamera.x).normalize();
    if (right.lengthSqr() < .001) right = new Vec3(1, 0, 0);
    for (int i = 0; i < segments; i++) {
      double a = i * Math.PI * 2 / segments, b = (i + 1) * Math.PI * 2 / segments;
      EffectGeometry.beam(
          pose,
          glow,
          right.scale(Math.cos(a) * .275).add(0, Math.sin(a) * .275, 0),
          right.scale(Math.cos(b) * .275).add(0, Math.sin(b) * .275, 0),
          .009,
          profile.argb(.8f));
    }
    for (int strand = 0; strand < 3; strand++) {
      for (int step = 0; step < 12; step++) {
        EffectGeometry.beam(
            pose,
            glow,
            feed(step / 12d, strand, time),
            feed((step + 1) / 12d, strand, time),
            .012,
            profile.argb(.55f));
      }
    }
    pose.popPose();
  }

  private static Vec3 flow(double radius, double angle, int band, double time) {
    double warp = Math.sin(angle * 2 + time * .025 + band * .4) * .025;
    return new Vec3(
        Math.cos(angle) * radius, warp + Math.sin(angle) * .035, Math.sin(angle) * radius);
  }

  private static Vec3 feed(double t, int strand, double time) {
    double a = t * 5 + strand * Math.PI * 2 / 3 - time * .035;
    double radius = .04 + (1 - t) * .13;
    return new Vec3(Math.cos(a) * radius, .24 + t * .47, Math.sin(a) * radius);
  }

  private static void sphereVertex(
      PoseStack pose, VertexConsumer out, double latitude, double angle) {
    float x = (float) (Math.cos(angle) * Math.sin(latitude));
    float y = (float) Math.cos(latitude);
    float z = (float) (Math.sin(angle) * Math.sin(latitude));
    out.addVertex(pose.last(), x * .23f, y * .23f, z * .23f)
        .setColor(0xff010207)
        .setUv(.5f, .5f)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(LightTexture.FULL_BRIGHT)
        .setNormal(pose.last(), x, y, z);
  }
}

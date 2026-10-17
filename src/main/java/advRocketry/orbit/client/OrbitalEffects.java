package advRocketry.orbit.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.SingularityLens;
import advRocketry.orbit.HorizontalOrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

/** Effects use authoritative operating state and deterministic local animation clocks. */
public final class OrbitalEffects {
  private OrbitalEffects() {}

  public static void render(OrbitalBlockEntity entity, float partialTick, PoseStack pose,
      MultiBufferSource buffers) {
    if (!entity.formed || entity.getLevel() == null) return;
    String id = entity.atmosphereTerraformer() ? "terraformer"
        : entity.areaGravityController() ? "gravity"
        : entity.blackHoleGenerator() ? "blackhole" : entity.railgun() ? "railgun" : "materials";
    var profile = EffectProfiles.get(id);
    if (!EffectGeometry.visible(Vec3.atCenterOf(entity.getBlockPos()), profile)) return;
    if (!EffectGeometry.reserve()) return;
    double time = EffectGeometry.time(entity.getLevel(), partialTick, profile);
    pose.pushPose();
    pose.translate(.5, .5, .5);
    var vertices = buffers.getBuffer(RenderType.lightning());
    if (entity.atmosphereTerraformer() && entity.terraformerRunning) {
      for (int strand = 0; strand < 3; strand++) {
        for (int i = 0; i < 24; i++) {
          double y = 8 + (i * .25 + time * .025) % 6;
          double a = y * 2 + strand * Math.PI * 2 / 3 + time * .03;
          Vec3 point = new Vec3(Math.cos(a) * .9, y, Math.sin(a) * .9);
          EffectGeometry.beam(pose, vertices, point, point.add(0, .1, 0), .025,
              profile.argb((float) ((14 - y) / 6)));
        }
      }
    } else if (entity.areaGravityController() && entity.areaCurrentGravity > .02f) {
      for (int i = 0; i < 4; i++) {
        double phase = (time * .008 + i * .25) % 1;
        EffectGeometry.ring(pose, vertices, .6 + phase * 2, .15 + i * .2, .012,
            profile.argb((float) (1 - phase) * .45f), profile.segments());
      }
    } else if (entity.blackHoleGenerator() && entity.powerMadeLastTick > 0) {
      SingularityLens.submit(Vec3.atLowerCornerOf(entity.getBlockPos()).add(.5, 2, .5), .35f);
      pose.translate(0, 1.5, 0);
      pose.mulPose(Axis.XP.rotationDegrees(14));
      for (int i = 0; i < 12; i++) {
        double radius = .45 + i * .075;
        float glow = (float) (.55 + .15 * Math.sin(time * .04 - i));
        EffectGeometry.ring(pose, vertices, radius, .03 * Math.sin(time * .02 + i),
            .025, profile.argb(glow), profile.segments());
      }
    } else if (entity.railgun()) {
      double age = entity.getLevel().getGameTime() + partialTick - entity.railgunFiredAt;
      if (age >= 0 && age < 10) {
        var facing = entity.getBlockState().getValue(HorizontalOrbitalBlock.FACING);
        pose.translate(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2);
        for (int i = 0; i < 5; i++) {
          float strength = (float) Math.max(0, 1 - Math.abs(age - i * 1.5) / 3);
          EffectGeometry.ring(pose, vertices, .6, i + .5, .035,
              profile.argb(strength), profile.segments());
        }
      }
    }
    pose.popPose();
  }
}

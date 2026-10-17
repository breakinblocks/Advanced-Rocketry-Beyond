package advRocketry.orbit.client;

import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.client.effects.SceneDistortion;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.RailgunLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

/** Effects use authoritative operating state and deterministic local animation clocks. */
public final class OrbitalEffects {
  private OrbitalEffects() {}

  public static void render(
      OrbitalBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers) {
    if (!entity.formed || entity.getLevel() == null) return;
    String id =
        entity.atmosphereTerraformer()
            ? "terraformer"
            : entity.areaGravityController()
                ? "gravity"
                : entity.blackHoleGenerator()
                    ? "blackhole"
                    : entity.railgun() ? "railgun" : "materials";
    var profile = EffectProfiles.get(id);
    if (!EffectGeometry.visible(Vec3.atCenterOf(entity.getBlockPos()), profile)) return;
    if (!EffectGeometry.reserve()) return;
    double time = EffectGeometry.time(entity.getLevel(), partialTick, profile);
    pose.pushPose();
    pose.translate(.5, .5, .5);
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    if (entity.atmosphereTerraformer() && entity.terraformerRunning) {
      // Broad, slowly expanding ripples rise clear of the tower's emitter.
      for (int ripple = 0; ripple < 5; ripple++) {
        double phase = (time / 160 + ripple / 5d) % 1;
        float strength = (float) (Math.sin(phase * Math.PI) * .65);
        EffectGeometry.ring(
            pose,
            vertices,
            1.2 + phase * 3.8,
            8.2 + phase * 5,
            .045,
            profile.argb(strength),
            EffectGeometry.segments(profile));
      }
    } else if (entity.areaGravityController() && entity.areaCurrentGravity > .02f) {
      for (int i = 0; i < 4; i++) {
        double phase = (time * .008 + i * .25) % 1;
        EffectGeometry.ring(
            pose,
            vertices,
            .6 + phase * 2,
            .65 + i * .25,
            .025,
            profile.argb((float) (1 - phase) * .7f),
            EffectGeometry.segments(profile));
      }
    } else if (entity.blackHoleGenerator() && entity.powerMadeLastTick > 0) {
      SceneDistortion.submit(Vec3.atLowerCornerOf(entity.getBlockPos()).add(.5, 2, .5), .35f);
      pose.translate(0, 1.5, 0);
      EffectGeometry.sphere(pose, buffers.getBuffer(RenderType.debugQuads()), .35, 0xff020406);
      vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
      pose.mulPose(Axis.XP.rotationDegrees(14));
      for (int i = 0; i < 12; i++) {
        double radius = .45 + i * .075;
        float glow = (float) (.55 + .15 * Math.sin(time * .04 - i));
        EffectGeometry.ring(
            pose, vertices, radius, 0, .045, profile.argb(glow), EffectGeometry.segments(profile));
      }
    } else if (entity.railgun()) {
      double age = entity.getLevel().getGameTime() + partialTick - entity.railgunFiredAt;
      if (age >= 0 && age < 12) {
        var offset = RailgunLogic.emitter(entity).subtract(entity.getBlockPos());
        pose.translate(offset.getX(), offset.getY(), offset.getZ());
        RailgunArcs.render(
            pose, vertices, profile, age, entity.railgunFiredAt, entity.getBlockPos().asLong());
        for (int i = 0; i < 9; i++) {
          float strength = (float) Math.max(0, 1 - Math.abs(age - i) / 3);
          EffectGeometry.ring(
              pose,
              vertices,
              2.05,
              i + 2,
              .11,
              profile.argb(strength),
              EffectGeometry.segments(profile));
        }
      }
    }
    pose.popPose();
  }
}

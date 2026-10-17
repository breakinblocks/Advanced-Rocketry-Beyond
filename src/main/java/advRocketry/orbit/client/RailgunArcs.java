package advRocketry.orbit.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

/** Short cosmetic discharges follow the energized coils; no entities or per-frame random state. */
final class RailgunArcs {
  private RailgunArcs() {}

  static void render(
      PoseStack pose,
      VertexConsumer vertices,
      EffectProfile profile,
      double age,
      long firedAt,
      long position) {
    if (AdvancedRocketryConfig.reducedMotion()) return;
    int arcs = 1 + AdvancedRocketryConfig.effectDetail();
    long seed = firedAt ^ position ^ ((long) (age / 2) * 0x9e3779b97f4a7c15L);
    float fade = (float) Math.min(1, Math.min(age + .25, (12 - age) / 3));
    double height = 2 + Math.min(8, Math.floor(age));
    for (int arc = 0; arc < arcs; arc++) {
      long key = seed + arc * 97L;
      double angle = arc * Math.PI * 2 / arcs + sample(key) * 1.2;
      Vec3 radial = new Vec3(Math.cos(angle), 0, Math.sin(angle));
      Vec3 tangent = new Vec3(-radial.z, 0, radial.x);
      Vec3 start = radial.scale(2.0).add(0, height, 0);
      Vec3 previous = start;
      double length = 1.6 + sample(key + 1) * 1.2;
      for (int step = 1; step <= 6; step++) {
        double fraction = step / 6d;
        Vec3 next =
            start
                .add(radial.scale(length * fraction))
                .add(tangent.scale((sample(key + step * 7) - .5) * .8))
                .add(0, (sample(key + step * 11) - .5) * .9, 0);
        bolt(pose, vertices, previous, next, profile, fade * (float) (1 - fraction * .5));
        if (step == 3) {
          Vec3 fork = next.add(radial.scale(.5)).add(tangent.scale(.6)).add(0, .45, 0);
          bolt(pose, vertices, next, fork, profile, fade * .5f);
          bolt(
              pose,
              vertices,
              fork,
              fork.add(radial.scale(.4)).add(0, -.15, 0),
              profile,
              fade * .25f);
        }
        previous = next;
      }
    }
  }

  private static void bolt(
      PoseStack pose,
      VertexConsumer vertices,
      Vec3 from,
      Vec3 to,
      EffectProfile profile,
      float strength) {
    EffectGeometry.beam(pose, vertices, from, to, .065, profile.argb(strength * .45f));
    int core = (profile.argb(strength) & 0xff000000) | 0xe9ffff;
    EffectGeometry.beam(pose, vertices, from, to, .018, core);
  }

  private static double sample(long value) {
    value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
    value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
    return ((value ^ (value >>> 31)) >>> 11) * 0x1.0p-53;
  }
}

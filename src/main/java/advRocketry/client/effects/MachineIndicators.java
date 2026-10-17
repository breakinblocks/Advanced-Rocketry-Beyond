package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class MachineIndicators {
  private MachineIndicators() {}

  public static void render(PoseStack pose, MultiBufferSource buffers, Direction front,
      boolean active, float progress, Vec3 position) {
    var profile = EffectProfiles.get("materials");
    if (!EffectGeometry.visible(position, profile)) return;
    var glow = buffers.getBuffer(RenderType.lightning());
    Vec3 center = new Vec3(.5 + front.getStepX() * .505, .78, .5 + front.getStepZ() * .505);
    Vec3 tangent = new Vec3(front.getStepZ(), 0, -front.getStepX());
    for (int i = 0; i < 5; i++) {
      Vec3 a = center.add(tangent.scale(-.32 + i * .14));
      Vec3 b = a.add(tangent.scale(.09));
      float strength = active && progress >= i / 5f ? .8f : .16f;
      EffectGeometry.beam(pose, glow, a, b, .012, profile.argb(strength));
    }
  }
}

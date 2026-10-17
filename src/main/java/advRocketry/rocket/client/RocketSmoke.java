package advRocketry.rocket.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Bounded world-space billows, retained after the rocket has left the pad. */
public final class RocketSmoke {
  private record Puff(Vec3 origin, Vec3 velocity, long born, float seed) {
    Vec3 position(double age) {
      return origin.add(velocity.scale(age));
    }
  }

  private static final ArrayList<Puff> PUFFS = new ArrayList<>();
  private static ClientLevel level;

  private RocketSmoke() {}

  public static void tick(ClientLevel current) {
    if (current != level) {
      PUFFS.clear();
      level = current;
    }
    if (level == null
        || !AdvancedRocketryConfig.advancedVfx()
        || AdvancedRocketryConfig.reducedMotion()
        || !EffectProfiles.get("rocket").enabled()) {
      PUFFS.clear();
      return;
    }
    PUFFS.removeIf(puff -> level.getGameTime() - puff.born() >= 90);
  }

  public static void emit(Vec3 origin, boolean ground) {
    if (level == null) return;
    int limit = 32 << AdvancedRocketryConfig.effectDetail();
    while (PUFFS.size() >= limit) PUFFS.removeFirst();
    double angle = level.random.nextDouble() * Math.PI * 2;
    double speed = ground ? .12 : .035;
    PUFFS.add(
        new Puff(
            origin,
            new Vec3(Math.cos(angle) * speed, ground ? .025 : -.035, Math.sin(angle) * speed),
            level.getGameTime(),
            level.random.nextFloat()));
  }

  public static void quad(
      PoseStack pose,
      VertexConsumer vertices,
      float halfWidth,
      float top,
      float bottom,
      int color) {
    var matrix = pose.last().pose();
    vertices.addVertex(matrix, -halfWidth, top, 0).setUv(0, 0).setColor(color);
    vertices.addVertex(matrix, halfWidth, top, 0).setUv(1, 0).setColor(color);
    vertices.addVertex(matrix, halfWidth, bottom, 0).setUv(1, 1).setColor(color);
    vertices.addVertex(matrix, -halfWidth, bottom, 0).setUv(0, 1).setColor(color);
  }

  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
    var client = Minecraft.getInstance();
    if (client.level != level
        || level == null
        || !RocketEffectShader.ready()
        || AdvancedRocketryConfig.reducedMotion()) return;
    var profile = EffectProfiles.get("rocket");
    var camera = event.getCamera().getPosition();
    double now = level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
    PUFFS.sort(
        Comparator.comparingDouble((Puff p) -> p.position(now - p.born()).distanceToSqr(camera))
            .reversed());
    var pose = event.getPoseStack();
    var buffers = client.renderBuffers().bufferSource();
    var vertices = buffers.getBuffer(RocketEffectShader.SMOKE);
    for (var puff : PUFFS) {
      double age = now - puff.born();
      Vec3 position = puff.position(age);
      if (!EffectGeometry.visible(position, profile)) continue;
      float radius = (float) (1.4 + age * .045);
      float fade = (float) Math.min(1, age / 5) * (float) Math.min(1, (90 - age) / 25);
      int color =
          ((int) (255 * Math.clamp(fade * profile.opacity() * 1.45, 0, 1)) << 24)
              | ((int) (puff.seed() * 255) << 16)
              | 0xffff;
      pose.pushPose();
      var relative = position.subtract(camera);
      pose.translate(relative.x, relative.y, relative.z);
      pose.mulPose(event.getCamera().rotation());
      quad(pose, vertices, radius, radius, -radius, color);
      pose.popPose();
    }
    buffers.endBatch(RocketEffectShader.SMOKE);
  }
}

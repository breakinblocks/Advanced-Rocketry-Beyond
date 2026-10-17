package advRocketry.rocket.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.EffectRenderTypes;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.space.GalaxySync;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Industrial clustered jets; particles are generated once per client tick, never per frame. */
public final class RocketExhaust {
  private RocketExhaust() {}

  public static void render(
      RocketEntity rocket, float partialTick, PoseStack pose, MultiBufferSource buffers) {
    var profile = EffectProfiles.get("rocket");
    if (!rocket.enginesActive() || !EffectGeometry.visible(rocket.position(), profile)) return;
    if (!EffectGeometry.reserve()) return;
    var planet = GalaxySync.clientPlanet(rocket.level().dimension().location());
    double spread =
        planet == null ? .2 : .15 + .45 * (1 - Math.clamp(planet.atmosphere / 100d, 0, 1));
    double time = EffectGeometry.time(rocket.level(), partialTick, profile);
    var vertices = buffers.getBuffer(EffectRenderTypes.GLOW);
    int engines = 0;
    for (var cell : rocket.structure().cells) {
      if (!(cell.state().getBlock() instanceof RocketPartBlock part)
          || part.kind != RocketPartBlock.Kind.ENGINE) continue;
      if (++engines > 32) break;
      pose.pushPose();
      pose.translate(
          cell.position().getX() + .5, cell.position().getY(), cell.position().getZ() + .5);
      for (int jet = 0; jet < 3; jet++) {
        Vec3 previous = new Vec3(Math.cos(jet * 2.094) * .12, 0, Math.sin(jet * 2.094) * .12);
        for (int step = 1; step <= 12; step++) {
          double f = step / 12d, angle = time * .4 + jet * 2.094 + step * .7;
          Vec3 next = new Vec3(Math.cos(angle) * spread * f, -f * 4, Math.sin(angle) * spread * f);
          EffectGeometry.beam(
              pose,
              vertices,
              previous,
              next,
              .13 * (1 - f) + .025,
              profile.argb((float) (1 - f) * .65f));
          previous = next;
        }
      }
      EffectGeometry.beam(pose, vertices, Vec3.ZERO, new Vec3(0, -.8, 0), .09, 0xc0fff1d5);
      pose.popPose();
    }
  }

  public static void tick(ClientTickEvent.Post event) {
    var client = Minecraft.getInstance();
    var profile = EffectProfiles.get("rocket");
    if (client.level == null || client.isPaused() || AdvancedRocketryConfig.reducedMotion()) return;
    int budget = 16;
    for (var entity : client.level.entitiesForRendering()) {
      if (!(entity instanceof RocketEntity rocket)
          || !rocket.enginesActive()
          || !EffectGeometry.visible(rocket.position(), profile)) continue;
      for (var cell : rocket.structure().cells) {
        if (!(cell.state().getBlock() instanceof RocketPartBlock part)
            || part.kind != RocketPartBlock.Kind.ENGINE) continue;
        if (--budget < 0) return;
        Vec3 nozzle =
            rocket
                .position()
                .add(
                    cell.position().getX() + .5 - rocket.structure().width / 2d,
                    cell.position().getY(),
                    cell.position().getZ() + .5 - rocket.structure().depth / 2d);
        var planet = GalaxySync.clientPlanet(client.level.dimension().location());
        if (planet != null && planet.atmosphere <= 0) continue;
        client.level.addParticle(
            ParticleTypes.CAMPFIRE_COSY_SMOKE, nozzle.x, nozzle.y - .5, nozzle.z, 0, -.08, 0);
        Vec3 end = nozzle.add(0, -8, 0);
        if (!client.level.hasChunkAt(BlockPos.containing(end))) continue;
        var hit =
            client.level.clip(
                new ClipContext(
                    nozzle, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rocket));
        if (hit.getType() == HitResult.Type.BLOCK) {
          double angle = client.level.random.nextDouble() * Math.PI * 2;
          Vec3 point = hit.getLocation().add(0, .15, 0);
          client.level.addParticle(
              ParticleTypes.CLOUD,
              point.x,
              point.y,
              point.z,
              Math.cos(angle) * .3,
              .03,
              Math.sin(angle) * .3);
        }
      }
    }
  }
}

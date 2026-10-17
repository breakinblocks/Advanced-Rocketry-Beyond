package advRocketry.rocket.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.client.effects.EffectGeometry;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.space.GalaxySync;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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
    if (!RocketEffectShader.ready()) return;
    var vertices = buffers.getBuffer(RocketEffectShader.PLUME);
    double time = EffectGeometry.time(rocket.level(), partialTick, profile);
    int color =
        ((int) (profile.opacity() * 255) << 24)
            | ((int) ((Math.sin(time * .2) * .5 + .5) * 255) << 16)
            | 0xffff;
    int engines = 0;
    for (var cell : rocket.structure().cells) {
      if (!(cell.state().getBlock() instanceof RocketPartBlock part)
          || part.kind != RocketPartBlock.Kind.ENGINE) continue;
      if (++engines > 32) break;
      pose.pushPose();
      pose.translate(
          cell.position().getX() + .5, cell.position().getY(), cell.position().getZ() + .5);
      RocketSmoke.quad(pose, vertices, 2.5f, 0, -10, color);
      pose.mulPose(Axis.YP.rotationDegrees(90));
      RocketSmoke.quad(pose, vertices, 2.5f, 0, -10, color);
      pose.popPose();
    }
  }

  public static void tick(ClientTickEvent.Post event) {
    var client = Minecraft.getInstance();
    var profile = EffectProfiles.get("rocket");
    RocketSmoke.tick(client.level);
    if (client.level == null || client.isPaused() || AdvancedRocketryConfig.reducedMotion()) return;
    int budget = 16;
    for (var entity : client.level.entitiesForRendering()) {
      if (!(entity instanceof RocketEntity rocket)
          || !rocket.enginesActive()
          || !EffectGeometry.visible(rocket.position(), profile)) continue;
      for (var cell : rocket.structure().cells) {
        if (!(cell.state().getBlock() instanceof RocketPartBlock part)
            || part.kind != RocketPartBlock.Kind.ENGINE) continue;
        // Spread emission work across ticks; smoke remains in world space.
        if ((client.level.getGameTime() + cell.position().hashCode()) % 4 != 0) continue;
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
        Vec3 end = nozzle.add(0, -12, 0);
        if (!client.level.hasChunkAt(BlockPos.containing(end))) continue;
        var hit =
            client.level.clip(
                new ClipContext(
                    nozzle, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rocket));
        boolean ground = hit.getType() == HitResult.Type.BLOCK;
        Vec3 point = ground ? hit.getLocation().add(0, 1.5, 0) : nozzle.add(0, -7, 0);
        RocketSmoke.emit(point, ground);
      }
    }
  }
}

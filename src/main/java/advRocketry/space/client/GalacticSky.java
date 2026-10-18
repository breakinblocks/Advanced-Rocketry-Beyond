package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.space.GalaxySync;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Random;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Cached stellar catalog and procedural galactic light, oriented in the shared system-map plane.
 */
public final class GalacticSky {
  private static VertexBuffer stars, dome;
  private static int cacheKey;
  private static boolean cached;
  private static final int[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

  private GalacticSky() {}

  public static void render(
      Matrix4f view,
      Planet planet,
      Star observer,
      PlanetSkyState state,
      float visibility,
      double ticks) {
    if (visibility <= .001) return;
    var systems = GalaxySync.clientStars();
    int detail = AdvancedRocketryConfig.effectDetail();
    int key =
        31 * (31 * systems.hashCode() + (observer == null ? 0 : observer.hashCode()))
            + Float.floatToIntBits(planet.sky.starDensity())
            + detail;
    if (!cached || key != cacheKey) {
      clear();
      cacheKey = key;
      cached = true;
      var builder =
          Tesselator.getInstance()
              .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Random random = new Random(10842);
      int count =
          (int) ((detail == 0 ? 10000 : detail == 1 ? 24000 : 48000) * planet.sky.starDensity());
      for (int i = 0; i < count; i++) {
        double longitude = random.nextDouble() * Math.PI * 2;
        double y =
            i % 3 == 0
                ? Math.clamp(random.nextGaussian() * .085, -.9, .9)
                : random.nextDouble() * 2 - 1;
        double radius = Math.sqrt(1 - y * y);
        Vec3 direction = new Vec3(Math.cos(longitude) * radius, y, Math.sin(longitude) * radius);
        double bright = Math.pow(random.nextDouble(), 5);
        float[] color = Star.color(65 + random.nextInt(220));
        star(builder, direction, .085 + bright * .22, color, (float) (.4 + bright * .6));
      }
      if (observer != null && planet.sky.starDensity() > 0) {
        for (Star system : systems) {
          if (system.id() == observer.id() || system.blackHole()) continue;
          Vec3 delta =
              new Vec3((double) system.x() - observer.x(), 0, (double) system.z() - observer.z());
          if (delta.lengthSqr() < .001) continue;
          double strength = Math.clamp(system.size() * 30 / Math.sqrt(delta.lengthSqr()), .15, 1);
          star(
              builder,
              delta.normalize(),
              .12 + strength * .16,
              Star.color(system.temperature()),
              (float) strength);
        }
      }
      var mesh = builder.build();
      if (mesh != null) {
        stars = new VertexBuffer(VertexBuffer.Usage.STATIC);
        stars.bind();
        stars.upload(mesh);
        VertexBuffer.unbind();
      }
    }
    Matrix4f galacticView = new Matrix4f(view).rotateZ(.72f);
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    var gas = SkyLayerShader.galaxy;
    if (gas != null
        && AdvancedRocketryConfig.advancedVfx()
        && detail > 0
        && planet.sky.galaxy() + planet.sky.nebula() > 0) {
      double cx = 0, cz = 0;
      for (Star system : systems) {
        cx += system.x();
        cz += system.z();
      }
      if (!systems.isEmpty()) {
        cx /= systems.size();
        cz /= systems.size();
      }
      double dx = cx - (observer == null ? 0 : observer.x()),
          dz = cz - (observer == null ? 0 : observer.z());
      double angle = Math.atan2(dz, dx);
      gas.safeGetUniform("Center").set((float) Math.cos(angle), 0f, (float) Math.sin(angle));
      gas.safeGetUniform("Strength").set(planet.sky.galaxy(), planet.sky.nebula(), visibility);
      RenderSystem.setShaderColor(1, 1, 1, 1);
      dome().bind();
      dome.drawWithShader(galacticView, RenderSystem.getProjectionMatrix(), gas);
      VertexBuffer.unbind();
    }
    if (stars != null) {
      var shader = SkyLayerShader.stars;
      if (shader != null) {
        boolean motion = !AdvancedRocketryConfig.reducedMotion();
        shader.safeGetUniform("Time").set((float) (ticks / 20));
        shader.safeGetUniform("Twinkle").set(motion ? planet.sky.twinkle() : 0);
        shader
            .safeGetUniform("Distortion")
            .set(motion ? planet.sky.distortion() * Math.min(1, state.density()) : 0);
        shader.safeGetUniform("Atmosphere").set(Math.min(1, state.density()));
        RenderSystem.setShaderColor(1, 1, 1, visibility);
        stars.bind();
        stars.drawWithShader(galacticView, RenderSystem.getProjectionMatrix(), shader);
        VertexBuffer.unbind();
      }
    }
    RenderSystem.setShaderColor(1, 1, 1, 1);
    RenderSystem.defaultBlendFunc();
  }

  private static void star(
      BufferBuilder b, Vec3 direction, double size, float[] color, float brightness) {
    Vec3 center = direction.scale(100);
    Vec3 tangent =
        direction
            .cross(Math.abs(direction.y) > .9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0))
            .normalize()
            .scale(size);
    Vec3 up = direction.cross(tangent);
    for (int[] corner : CORNERS) {
      Vec3 p = center.add(tangent.scale(corner[0])).add(up.scale(corner[1]));
      b.addVertex((float) p.x, (float) p.y, (float) p.z)
          .setUv((corner[0] + 1) * .5f, (corner[1] + 1) * .5f)
          .setColor(color[0], color[1], color[2], brightness);
    }
  }

  private static VertexBuffer dome() {
    if (dome != null) return dome;
    var b =
        Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    for (int y = 0; y < 24; y++)
      for (int x = 0; x < 48; x++) {
        vertex(b, x, y);
        vertex(b, x + 1, y);
        vertex(b, x + 1, y + 1);
        vertex(b, x, y + 1);
      }
    dome = new VertexBuffer(VertexBuffer.Usage.STATIC);
    dome.bind();
    dome.upload(b.buildOrThrow());
    VertexBuffer.unbind();
    return dome;
  }

  private static void vertex(BufferBuilder b, int x, int y) {
    double a = x * Math.PI / 24, e = (y / 24d - .5) * Math.PI;
    b.addVertex(
            (float) (Math.cos(a) * Math.cos(e) * 100),
            (float) (Math.sin(e) * 100),
            (float) (Math.sin(a) * Math.cos(e) * 100))
        .setColor(255, 255, 255, 255);
  }

  public static void clear() {
    cached = false;
    if (stars != null) {
      stars.close();
      stars = null;
    }
    if (dome != null) {
      dome.close();
      dome = null;
    }
  }
}

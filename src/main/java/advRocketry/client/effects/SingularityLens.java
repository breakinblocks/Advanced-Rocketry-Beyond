package advRocketry.client.effects;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;

/** 1.21.1 port of Nautec's bounded, depth-aware scene lens. Render-thread owned. */
public final class SingularityLens {
  private record Source(Vec3 center, float radius) {}
  private static final List<Source> pending = new ArrayList<>();
  private static ShaderInstance shader;
  private static TextureTarget scene;
  private static int idleFrames;

  private SingularityLens() {}

  public static void register(RegisterShadersEvent event) {
    release();
    shader = null;
    try {
      event.registerShader(new ShaderInstance(event.getResourceProvider(),
          ResourceLocation.fromNamespaceAndPath(Main.MODID, "singularity_lens"),
          DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
    } catch (IOException error) {
      LogUtils.getLogger().warn("Singularity shader unavailable; retaining geometry effects", error);
    }
  }

  public static void submit(Vec3 center, float radius) {
    if (pending.size() < 64) pending.add(new Source(center, radius));
  }

  public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
    release();
  }

  private static void release() {
    pending.clear();
    idleFrames = 0;
    if (scene != null) {
      scene.destroyBuffers();
      scene = null;
    }
  }

  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
    if (pending.isEmpty() || shader == null || !AdvancedRocketryConfig.singularityDistortion()
        || !AdvancedRocketryConfig.advancedVfx() || AdvancedRocketryConfig.reducedMotion()
        || AdvancedRocketryConfig.effectDetail() < 2
        || ModList.get().isLoaded("iris")) {
      pending.clear();
      if (++idleFrames > 120) release();
      return;
    }
    idleFrames = 0;
    var client = Minecraft.getInstance();
    var target = client.getMainRenderTarget();
    Vec3 camera = event.getCamera().getPosition();
    pending.sort(Comparator.comparingDouble(source -> source.center.distanceToSqr(camera)));
    var chosen = List.copyOf(pending.subList(0, Math.min(4, pending.size())));
    pending.clear();
    if (target.width <= 0 || target.height <= 0) return;
    if (scene == null) scene = new TextureTarget(target.width, target.height, true, Minecraft.ON_OSX);
    else if (scene.width != target.width || scene.height != target.height)
      scene.resize(target.width, target.height, Minecraft.ON_OSX);
    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, target.frameBufferId);
    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
    GL30.glBlitFramebuffer(0, 0, target.width, target.height, 0, 0, scene.width, scene.height,
        GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);
    target.bindWrite(true);
    shader.setSampler("SceneSampler", scene.getColorTextureId());
    shader.setSampler("DepthSampler", scene.getDepthTextureId());
    shader.safeGetUniform("ScreenSize").set((float) target.width, (float) target.height);
    var projection = event.getProjectionMatrix();
    for (int i = 0; i < 4; i++) {
      Vector4f lens = new Vector4f(-10, -10, 0, 0);
      if (i < chosen.size()) {
        var source = chosen.get(i);
        Vec3 relative = source.center.subtract(camera);
        Vector4f view = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1);
        event.getModelViewMatrix().transform(view);
        if (view.z < -source.radius * 1.5f) {
          Vector4f center = projection.transform(new Vector4f(view));
          Vector4f edge = projection.transform(new Vector4f(view).add(source.radius, 0, 0, 0));
          Vector4f front = projection.transform(new Vector4f(view).add(0, 0, source.radius, 0));
          lens.set(center.x / center.w * .5f + .5f, center.y / center.w * .5f + .5f,
              Math.abs(edge.x / edge.w - center.x / center.w) * .5f,
              front.z / front.w * .5f + .5f);
        }
      }
      shader.safeGetUniform("Lens" + i).set(lens);
    }
    boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    ShaderInstance previousShader = RenderSystem.getShader();
    RenderSystem.disableDepthTest();
    RenderSystem.depthMask(false);
    RenderSystem.disableBlend();
    RenderSystem.setShader(() -> shader);
    try {
      var quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      quad.addVertex(-1, -1, 0).setUv(0, 0);
      quad.addVertex(1, -1, 0).setUv(1, 0);
      quad.addVertex(1, 1, 0).setUv(1, 1);
      quad.addVertex(-1, 1, 0).setUv(0, 1);
      BufferUploader.drawWithShader(quad.buildOrThrow());
    } finally {
      RenderSystem.depthMask(depthWrite);
      if (depthTest) RenderSystem.enableDepthTest();
      if (blend) RenderSystem.enableBlend();
      RenderSystem.setShader(() -> previousShader);
    }
  }
}

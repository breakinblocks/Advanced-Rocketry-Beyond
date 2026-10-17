package advRocketry.client.effects;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/** Procedural scanning surfaces; no baked texture or per-frame texture uploads. */
public final class HologramShader extends RenderType {
  private static ShaderInstance shader;
  private static final RenderType SURFACE =
      create(
          "adv_rocketry_hologram",
          DefaultVertexFormat.NEW_ENTITY,
          VertexFormat.Mode.QUADS,
          1536,
          false,
          true,
          CompositeState.builder()
              .setShaderState(
                  new ShaderStateShard(
                      () -> {
                        if (shader != null)
                          shader
                              .safeGetUniform("Motion")
                              .set(
                                  AdvancedRocketryConfig.reducedMotion()
                                      ? 0f
                                      : EffectProfiles.get("hologram").speed());
                        return shader;
                      }))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
              .setCullState(NO_CULL)
              .setWriteMaskState(COLOR_WRITE)
              .createCompositeState(false));

  private HologramShader() {
    super(
        "adv_rocketry_hologram",
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        () -> {},
        () -> {});
  }

  public static boolean active() {
    return shader != null
        && !ShaderCompatibility.shaderPackActive()
        && AdvancedRocketryConfig.advancedVfx()
        && EffectProfiles.get("hologram").enabled();
  }

  public static RenderType surface(ResourceLocation fallback) {
    return active() ? SURFACE : RenderType.entityTranslucent(fallback);
  }

  public static void register(RegisterShadersEvent event) {
    shader = null;
    try {
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "hologram"),
              DefaultVertexFormat.NEW_ENTITY),
          loaded -> shader = loaded);
    } catch (IOException error) {
      LogUtils.getLogger().warn("Hologram shader unavailable; using translucent surface", error);
    }
  }
}

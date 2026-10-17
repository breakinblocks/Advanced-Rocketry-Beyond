package advRocketry.rocket.client;

import advRocketry.Main;
import advRocketry.client.effects.EffectProfiles;
import advRocketry.client.effects.ShaderCompatibility;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/** Texture-free exhaust surfaces with depth testing and no depth writes. */
public final class RocketEffectShader extends RenderType {
  private static ShaderInstance smoke;
  private static ShaderInstance plume;
  public static final RenderType SMOKE = surface("rocket_smoke", false);
  public static final RenderType PLUME = surface("rocket_plume", true);

  private RocketEffectShader() {
    super(
        "rocket_effect",
        DefaultVertexFormat.POSITION_TEX_COLOR,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        () -> {},
        () -> {});
  }

  private static RenderType surface(String name, boolean hot) {
    return create(
        name,
        DefaultVertexFormat.POSITION_TEX_COLOR,
        VertexFormat.Mode.QUADS,
        4096,
        false,
        true,
        CompositeState.builder()
            .setShaderState(
                new ShaderStateShard(
                    () -> {
                      if (hot && plume != null) {
                        var profile = EffectProfiles.get("rocket");
                        int rgb = profile.color();
                        plume
                            .safeGetUniform("PlumeColor")
                            .set(
                                ((rgb >> 16) & 255) / 255f,
                                ((rgb >> 8) & 255) / 255f,
                                (rgb & 255) / 255f);
                      }
                      return hot ? plume : smoke;
                    }))
            .setTransparencyState(hot ? LIGHTNING_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));
  }

  public static boolean ready() {
    return smoke != null && plume != null;
  }

  public static boolean needsCompatibilityGeometry() {
    return ShaderCompatibility.shaderPackActive() || !ready();
  }

  public static void register(RegisterShadersEvent event) {
    smoke = null;
    plume = null;
    try {
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "rocket_smoke"),
              DefaultVertexFormat.POSITION_TEX_COLOR),
          loaded -> smoke = loaded);
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "rocket_plume"),
              DefaultVertexFormat.POSITION_TEX_COLOR),
          loaded -> plume = loaded);
    } catch (IOException error) {
      LogUtils.getLogger().warn("Rocket effect shaders unavailable", error);
    }
  }
}

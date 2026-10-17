package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Emissive surfaces need both faces and must not occlude later translucent geometry. */
public final class EffectRenderTypes extends RenderType {
  public static final RenderType GLOW =
      create(
          "adv_rocketry_effect_glow",
          DefaultVertexFormat.POSITION_COLOR,
          VertexFormat.Mode.QUADS,
          1536,
          false,
          true,
          CompositeState.builder()
              .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
              .setTransparencyState(LIGHTNING_TRANSPARENCY)
              .setCullState(NO_CULL)
              .setWriteMaskState(COLOR_WRITE)
              .setDepthTestState(LEQUAL_DEPTH_TEST)
              .createCompositeState(false));

  private EffectRenderTypes() {
    super(
        "adv_rocketry_effects",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        () -> {},
        () -> {});
  }
}

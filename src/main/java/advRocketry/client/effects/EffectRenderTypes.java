package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

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

  /** Entity translucency target carries depth into Fabulous compositing behind water and glass. */
  public static final RenderType FIBER =
      create(
          "adv_rocketry_fiber",
          DefaultVertexFormat.NEW_ENTITY,
          VertexFormat.Mode.QUADS,
          1536,
          true,
          true,
          CompositeState.builder()
              .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
              .setTextureState(
                  new TextureStateShard(
                      ResourceLocation.fromNamespaceAndPath("neoforge", "textures/white.png"),
                      false,
                      false))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
              .setCullState(NO_CULL)
              .setOverlayState(OVERLAY)
              .setWriteMaskState(COLOR_DEPTH_WRITE)
              .setDepthTestState(LEQUAL_DEPTH_TEST)
              .setOutputState(ITEM_ENTITY_TARGET)
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

package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Shared glow and translucent surfaces, with depth-aware Fabulous compositing. */
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
  public static final RenderType EMISSIVE = surface("adv_rocketry_emissive", true);

  public static final RenderType SMOKE = surface("adv_rocketry_smoke_compatible", false);

  private static RenderType surface(String name, boolean emissive) {
    return create(
        name,
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        1536,
        true,
        true,
        CompositeState.builder()
            .setShaderState(
                emissive
                    ? RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER
                    : RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
            .setLightmapState(emissive ? NO_LIGHTMAP : LIGHTMAP)
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
  }

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

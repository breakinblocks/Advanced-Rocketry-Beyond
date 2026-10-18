package advRocketry.space.client;

import advRocketry.Main;
import advRocketry.client.effects.ShaderCompatibility;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/**
 * Dedicated sky programs preserve planet colors when a shader pack replaces vanilla sky shaders.
 */
public final class SkyLayerShader {
  private static ShaderInstance color, texture;
  static ShaderInstance stars, galaxy;

  private SkyLayerShader() {}

  public static void register(RegisterShadersEvent event) {
    color = null;
    texture = null;
    stars = null;
    galaxy = null;
    try {
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "sky_color"),
              DefaultVertexFormat.POSITION_COLOR),
          loaded -> color = ShaderCompatibility.allowPlanetShader(loaded) ? loaded : null);
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "sky_texture"),
              DefaultVertexFormat.POSITION_TEX_COLOR),
          loaded -> texture = ShaderCompatibility.allowPlanetShader(loaded) ? loaded : null);
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "sky_stars"),
              DefaultVertexFormat.POSITION_TEX_COLOR),
          loaded -> stars = ShaderCompatibility.allowPlanetShader(loaded) ? loaded : null);
      event.registerShader(
          new ShaderInstance(
              event.getResourceProvider(),
              ResourceLocation.fromNamespaceAndPath(Main.MODID, "sky_galaxy"),
              DefaultVertexFormat.POSITION_COLOR),
          loaded -> galaxy = ShaderCompatibility.allowPlanetShader(loaded) ? loaded : null);
    } catch (IOException error) {
      LogUtils.getLogger()
          .warn("Planet sky programs unavailable; using standard sky layers", error);
    }
  }

  public static ShaderInstance color() {
    return color == null ? GameRenderer.getPositionColorShader() : color;
  }

  public static ShaderInstance texture() {
    return texture == null ? GameRenderer.getPositionTexColorShader() : texture;
  }
}

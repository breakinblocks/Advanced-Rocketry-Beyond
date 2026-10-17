package advRocketry.client.effects;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.fml.ModList;

/** Optional Iris public API bridge without a mandatory shader-loader dependency. */
public final class ShaderCompatibility {
  private record Api(Object instance, Method active, Method shadow) {}

  private static final Api API = findApi();
  private static boolean failed;

  private ShaderCompatibility() {}

  private static Api findApi() {
    if (!ModList.get().isLoaded("iris")) return null;
    try {
      Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
      return new Api(
          type.getMethod("getInstance").invoke(null),
          type.getMethod("isShaderPackInUse"),
          type.getMethod("isRenderingShadowPass"));
    } catch (ReflectiveOperationException error) {
      LogUtils.getLogger().warn("Iris rendering API unavailable", error);
      return null;
    }
  }

  public static boolean shaderPackActive() {
    return API != null && query(API.active());
  }

  public static boolean shadowPass() {
    return API != null && query(API.shadow());
  }

  /**
   * Iris 1.8's NeoForge bridge otherwise suppresses non-vanilla ShaderInstances. Opt in only our
   * planet programs, which render into Iris's default color target and never into its shadow pass.
   * This optional internal hook is isolated here; it does not enable unknown shaders globally.
   */
  public static boolean allowPlanetShader(ShaderInstance shader) {
    if (!ModList.get().isLoaded("iris")) return true;
    if (!shader.getName().startsWith("adv_rocketry:sky_")
        && !shader.getName().equals("adv_rocketry:planet_clouds")) return false;
    try {
      Class<?> hooks = Class.forName("net.irisshaders.iris.mixinterface.ShaderInstanceInterface");
      Class<?> skips = Class.forName("net.irisshaders.iris.compat.SkipList");
      hooks
          .getMethod("setShouldSkip", MethodHandle.class)
          .invoke(shader, skips.getField("NONE_FORCE").get(null));
      return true;
    } catch (ReflectiveOperationException | LinkageError error) {
      LogUtils.getLogger()
          .warn("Iris planet shader bridge unavailable for {}", shader.getName(), error);
      return false;
    }
  }

  private static boolean query(Method method) {
    if (failed) return false;
    try {
      return (boolean) method.invoke(API.instance());
    } catch (ReflectiveOperationException error) {
      failed = true;
      LogUtils.getLogger().warn("Cannot query Iris rendering state", error);
      return false;
    }
  }
}

package advRocketry.client.effects;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
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

package advRocketry.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Texts {
  private Texts() {}

  public static MutableComponent translate(String key, Object... args) {
    Object[] safe = new Object[args.length];
    for (int index = 0; index < args.length; index++) {
      Object arg = args[index];
      safe[index] =
          arg instanceof Component
                  || arg instanceof Number
                  || arg instanceof Boolean
                  || arg instanceof String
              ? arg
              : String.valueOf(arg);
    }
    return Component.translatable(key, safe);
  }
}

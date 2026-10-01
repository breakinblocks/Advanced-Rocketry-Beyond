package advRocketry.util;

import com.google.gson.JsonParseException;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

public final class ComponentText {
  private ComponentText() {}

  public static Component read(String json, HolderLookup.Provider registries) {
    if (json.isEmpty()) return Component.empty();
    try {
      Component component = Component.Serializer.fromJson(json, registries);
      return component == null ? Component.literal(json) : component;
    } catch (JsonParseException invalid) {
      return Component.literal(json);
    }
  }
}

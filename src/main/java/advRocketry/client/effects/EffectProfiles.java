package advRocketry.client.effects;

import advRocketry.Main;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/** Replaced atomically at resource reload; no mutable world objects or network state. */
public final class EffectProfiles implements ResourceManagerReloadListener {
  private static final String[] IDS = {
    "hologram", "rocket", "laser", "railgun", "blackhole", "terraformer", "gravity", "etcher",
    "planet", "celestial", "elevator", "materials", "diagnostics", "hud", "transport", "audio", "solar_array"
  };
  private static volatile Map<String, EffectProfile> profiles = Map.of();

  public static EffectProfile get(String id) {
    return profiles.getOrDefault(id, EffectProfile.DEFAULT);
  }

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    Map<String, EffectProfile> loaded = new HashMap<>();
    for (String id : IDS) {
      var path = ResourceLocation.fromNamespaceAndPath(Main.MODID, "effects/" + id + ".json");
      var resource = manager.getResource(path);
      if (resource.isEmpty()) continue;
      try (var reader = resource.get().openAsReader()) {
        var result = EffectProfile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
        result
            .resultOrPartial(
                error -> LogUtils.getLogger().warn("Invalid effect {}: {}", path, error))
            .ifPresent(profile -> loaded.put(id, profile));
      } catch (IOException | RuntimeException error) {
        LogUtils.getLogger().warn("Cannot load effect {}", path, error);
      }
    }
    profiles = Map.copyOf(loaded);
  }
}

package advRocketry.space;

import advRocketry.Main;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

/** Reuse installed biome surface materials without importing dimension terrain or bedrock roofs. */
public final class PlanetBiomeSurfaces {
  private PlanetBiomeSurfaces() {}

  public static List<SurfaceRules.RuleSource> rules(
      RegistryAccess registries, List<Holder<Biome>> biomes) {
    var settings = registries.registryOrThrow(Registries.NOISE_SETTINGS);
    var ops = registries.createSerializationContext(JsonOps.INSTANCE);
    Map<ResourceKey<NoiseGeneratorSettings>, List<ResourceKey<Biome>>> groups =
        new LinkedHashMap<>();
    List<SurfaceRules.RuleSource> result = new ArrayList<>();
    for (Holder<Biome> biome : biomes) {
      var key = biome.unwrapKey().orElseThrow();
      if (key.location().getNamespace().equals(Main.MODID)) continue;
      var dimension =
          biome.is(BiomeTags.IS_NETHER)
              ? NoiseGeneratorSettings.NETHER
              : biome.is(BiomeTags.IS_END)
                  ? NoiseGeneratorSettings.END
                  : NoiseGeneratorSettings.OVERWORLD;
      groups.computeIfAbsent(dimension, id -> new ArrayList<>()).add(key);
    }
    for (var group : groups.entrySet()) {
      JsonElement encoded =
          SurfaceRules.RuleSource.CODEC
              .encodeStart(ops, settings.getHolderOrThrow(group.getKey()).value().surfaceRule())
              .getOrThrow();
      JsonElement filtered = withoutBedrock(encoded);
      if (filtered == null) continue;
      SurfaceRules.RuleSource source =
          SurfaceRules.RuleSource.CODEC.parse(ops, filtered).getOrThrow();
      @SuppressWarnings("unchecked")
      ResourceKey<Biome>[] keys = group.getValue().toArray(ResourceKey[]::new);
      result.add(SurfaceRules.ifTrue(SurfaceRules.isBiome(keys), source));
    }
    return List.copyOf(result);
  }

  private static JsonElement withoutBedrock(JsonElement value) {
    if (value.isJsonArray()) {
      JsonArray array = new JsonArray();
      for (JsonElement child : value.getAsJsonArray()) {
        JsonElement filtered = withoutBedrock(child);
        if (filtered != null) array.add(filtered);
      }
      return array;
    }
    if (!value.isJsonObject()) return value;
    JsonObject rule = value.getAsJsonObject().deepCopy();
    if (rule.has("result_state")
        && rule.getAsJsonObject("result_state").has("Name")
        && rule.getAsJsonObject("result_state")
            .get("Name")
            .getAsString()
            .equals("minecraft:bedrock")) return null;
    if (rule.has("then_run")) {
      JsonElement child = withoutBedrock(rule.get("then_run"));
      if (child == null) return null;
      rule.add("then_run", child);
    }
    if (rule.has("sequence")) {
      JsonArray children = new JsonArray();
      for (JsonElement child : rule.getAsJsonArray("sequence")) {
        JsonElement filtered = withoutBedrock(child);
        if (filtered != null) children.add(filtered);
      }
      if (children.isEmpty()) return null;
      rule.add("sequence", children);
    }
    // TerraBlender and other providers can wrap rule maps under their own codec fields.
    for (String key : List.copyOf(rule.keySet())) {
      if (key.equals("then_run") || key.equals("sequence")) continue;
      JsonElement child = withoutBedrock(rule.get(key));
      if (child == null) {
        if (rule.has("type")) return null;
        rule.remove(key);
      } else rule.add(key, child);
    }
    return rule;
  }
}

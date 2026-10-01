// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.DataRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Original asteroid definitions, seeded surveys and research-weighted harvests. */
public final class AsteroidCatalog {
  public record Ore(ResourceLocation item, float weight) {
    public static final Codec<Ore> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ResourceLocation.CODEC.fieldOf("item").forGetter(Ore::item),
                        ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(Ore::weight))
                    .apply(instance, Ore::new));
  }

  public record SurveyEntry(ResourceLocation item, int count, int midpoint, int variability) {}

  public record Definition(
      int distance,
      int mass,
      float massVariability,
      float richness,
      float richnessVariability,
      float probability,
      float timeMultiplier,
      ResourceLocation base,
      List<Ore> ores) {
    private static final Codec<Float> NON_NEGATIVE = Codec.floatRange(0, Float.MAX_VALUE);
    public static final Codec<Definition> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ExtraCodecs.NON_NEGATIVE_INT
                            .fieldOf("distance")
                            .forGetter(Definition::distance),
                        ExtraCodecs.POSITIVE_INT.fieldOf("mass").forGetter(Definition::mass),
                        NON_NEGATIVE
                            .optionalFieldOf("mass_variability", 0f)
                            .forGetter(Definition::massVariability),
                        NON_NEGATIVE
                            .optionalFieldOf("richness", 0f)
                            .forGetter(Definition::richness),
                        NON_NEGATIVE
                            .optionalFieldOf("richness_variability", 0f)
                            .forGetter(Definition::richnessVariability),
                        NON_NEGATIVE
                            .optionalFieldOf("probability", 0f)
                            .forGetter(Definition::probability),
                        NON_NEGATIVE
                            .optionalFieldOf("time_multiplier", 1f)
                            .forGetter(Definition::timeMultiplier),
                        ResourceLocation.CODEC
                            .optionalFieldOf(
                                "base", ResourceLocation.withDefaultNamespace("cobblestone"))
                            .forGetter(Definition::base),
                        Ore.CODEC
                            .listOf()
                            .optionalFieldOf("ores", List.of())
                            .forGetter(Definition::ores))
                    .apply(instance, Definition::new));

    public Asteroid named(ResourceLocation id) {
      return new Asteroid(
          id.toString(),
          id.toLanguageKey("asteroid"),
          distance,
          mass,
          massVariability,
          richness,
          richnessVariability,
          probability,
          timeMultiplier,
          base,
          ores);
    }
  }

  public record Asteroid(
      String id,
      String name,
      int distance,
      int mass,
      float massVariability,
      float richness,
      float richnessVariability,
      float probability,
      float timeMultiplier,
      ResourceLocation base,
      List<Ore> ores) {
    public Component displayName() {
      return Component.translatable(name);
    }
  }

  private AsteroidCatalog() {}

  public static List<Asteroid> types(RegistryAccess registries) {
    return registries
        .registry(DataRegistries.ASTEROID)
        .map(
            registry ->
                registry.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().location()))
                    .map(entry -> entry.getValue().named(entry.getKey().location()))
                    .toList())
        .orElse(List.of());
  }

  public static Asteroid get(RegistryAccess registries, String id) {
    ResourceLocation key = ResourceLocation.tryParse(id);
    if (key == null) return null;
    return registries
        .registry(DataRegistries.ASTEROID)
        .flatMap(registry -> registry.getOptional(key))
        .map(definition -> definition.named(key))
        .orElse(null);
  }

  public static CompoundTag save(Asteroid asteroid) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", asteroid.id());
    tag.putString("name", asteroid.name());
    tag.putInt("distance", asteroid.distance());
    tag.putInt("mass", asteroid.mass());
    tag.putFloat("mass_variability", asteroid.massVariability());
    tag.putFloat("richness", asteroid.richness());
    tag.putFloat("richness_variability", asteroid.richnessVariability());
    tag.putFloat("probability", asteroid.probability());
    tag.putFloat("time_multiplier", asteroid.timeMultiplier());
    tag.putString("base", asteroid.base().toString());
    ListTag ores = new ListTag();
    for (Ore ore : asteroid.ores()) {
      CompoundTag data = new CompoundTag();
      data.putString("item", ore.item().toString());
      data.putFloat("weight", ore.weight());
      ores.add(data);
    }
    tag.put("ores", ores);
    return tag;
  }

  public static Asteroid load(CompoundTag tag) {
    List<Ore> ores = new ArrayList<>();
    for (Tag entry : tag.getList("ores", Tag.TAG_COMPOUND)) {
      CompoundTag ore = (CompoundTag) entry;
      ores.add(new Ore(ResourceLocation.parse(ore.getString("item")), ore.getFloat("weight")));
    }
    return new Asteroid(
        tag.getString("id"),
        tag.getString("name"),
        tag.getInt("distance"),
        tag.getInt("mass"),
        tag.getFloat("mass_variability"),
        tag.getFloat("richness"),
        tag.getFloat("richness_variability"),
        tag.getFloat("probability"),
        tag.getFloat("time_multiplier"),
        ResourceLocation.parse(tag.getString("base")),
        List.copyOf(ores));
  }

  public static List<ItemStack> harvest(
      Asteroid asteroid, long seed, int compositionData, int massData) {
    Random bonusRandom = new Random(seed ^ 0x9e3779b97f4a7c15L);
    List<ItemStack> results = new ArrayList<>();
    for (SurveyEntry entry : survey(asteroid, seed, 0, 0)) {
      int count = entry.count();
      if (bonusRandom.nextFloat() < compositionData / (float) AsteroidChipItem.MAX_DATA)
        count = (int) (count * 1.25f);
      if (bonusRandom.nextFloat() < massData / (float) AsteroidChipItem.MAX_DATA)
        count = (int) (count * 1.25f);
      add(results, entry.item(), count);
    }
    return results;
  }

  public static List<SurveyEntry> survey(
      Asteroid asteroid, long seed, int compositionData, int massData) {
    Random random = new Random(seed);
    float uncertainty =
        1 - (Math.clamp(compositionData, 0, 2000) + Math.clamp(massData, 0, 2000)) / 4000f;
    int mass =
        Math.max(
            1,
            (int)
                (asteroid.mass()
                    + (random.nextFloat() * asteroid.massVariability()
                            - asteroid.massVariability() / 2)
                        * asteroid.mass()));
    int oreCount =
        (int)
            Math.clamp(
                mass
                    * (asteroid.richness()
                        + random.nextFloat() * asteroid.richnessVariability()
                        - asteroid.richnessVariability() / 2),
                0,
                mass);
    List<SurveyEntry> results = new ArrayList<>();
    if (asteroid.ores().isEmpty()) oreCount = 0;
    int baseCount = mass - oreCount;
    int baseVariance = (int) (uncertainty * baseCount);
    int baseOffset = (int) (baseVariance * random.nextFloat() - uncertainty * baseVariance / 2);
    results.add(
        new SurveyEntry(
            asteroid.base(),
            baseCount,
            Math.max(baseVariance, baseCount + baseOffset),
            baseVariance));
    float totalWeight = 0;
    for (Ore ore : asteroid.ores()) totalWeight += ore.weight();
    int[] counts = new int[asteroid.ores().size()];
    for (int i = 0; i < oreCount; i++) {
      float choice = random.nextFloat();
      float lower = 0;
      for (int ore = 0; ore < counts.length; ore++) {
        float upper = lower + asteroid.ores().get(ore).weight() / totalWeight;
        if (choice >= lower && choice <= upper) {
          counts[ore]++;
          break;
        }
        lower = upper;
      }
    }
    for (int i = 0; i < counts.length; i++) {
      int count = counts[i];
      if (count <= 0) continue;
      int variance = (int) (uncertainty * random.nextFloat() * count);
      int offset = (int) (variance * random.nextFloat() - variance / 2f);
      results.add(
          new SurveyEntry(
              asteroid.ores().get(i).item(), count, Math.max(variance, count + offset), variance));
    }
    return results;
  }

  private static void add(List<ItemStack> results, ResourceLocation id, int amount) {
    Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
    if (item == Items.AIR || amount <= 0) return;
    int maximum = item.getDefaultMaxStackSize();
    while (amount > 0) {
      int count = Math.min(amount, maximum);
      results.add(new ItemStack(item, count));
      amount -= count;
    }
  }
}

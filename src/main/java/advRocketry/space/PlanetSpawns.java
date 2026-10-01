// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Original planet spawnable entries, expressed as native spawn tables and entity data. */
public final class PlanetSpawns {
  public record Entry(ResourceLocation entity, int weight, int min, int max, CompoundTag data) {
    private static final Codec<Entry> UNCHECKED =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ResourceLocation.CODEC.fieldOf("entity").forGetter(Entry::entity),
                        Codec.intRange(1, 1000000)
                            .optionalFieldOf("weight", 100)
                            .forGetter(Entry::weight),
                        Codec.intRange(1, 128).optionalFieldOf("min", 1).forGetter(Entry::min),
                        Codec.intRange(1, 128)
                            .optionalFieldOf("max")
                            .forGetter(entry -> Optional.of(entry.max)),
                        CompoundTag.CODEC
                            .optionalFieldOf("data", new CompoundTag())
                            .forGetter(Entry::data))
                    .apply(
                        instance,
                        (entity, weight, min, max, data) ->
                            new Entry(entity, weight, min, max.orElse(min), data)));
    public static final Codec<Entry> CODEC =
        UNCHECKED.validate(
            entry ->
                entry.max < entry.min
                    ? DataResult.error(() -> "Spawn max is below min for " + entry.entity)
                    : !BuiltInRegistries.ENTITY_TYPE.containsKey(entry.entity)
                            || BuiltInRegistries.ENTITY_TYPE.get(entry.entity) == EntityType.PLAYER
                        ? DataResult.error(() -> "Unknown planet spawn entity: " + entry.entity)
                        : DataResult.success(entry));

    public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("entity", entity.toString());
      tag.putInt("weight", weight);
      tag.putInt("min", min);
      tag.putInt("max", max);
      tag.put("data", data.copy());
      return tag;
    }

    public static Entry load(CompoundTag tag) {
      return new Entry(
          ResourceLocation.parse(tag.getString("entity")),
          tag.getInt("weight"),
          tag.getInt("min"),
          tag.getInt("max"),
          tag.getCompound("data").copy());
    }

    public MobSpawnSettings.SpawnerData spawner() {
      return new MobSpawnSettings.SpawnerData(
          BuiltInRegistries.ENTITY_TYPE.get(entity), weight, min, max);
    }
  }

  private static final Map<Mob, CompoundTag> PENDING =
      Collections.synchronizedMap(new WeakHashMap<>());

  private PlanetSpawns() {}

  public static ListTag save(List<Entry> entries) {
    ListTag list = new ListTag();
    entries.forEach(entry -> list.add(entry.save()));
    return list;
  }

  public static void potential(LevelEvent.PotentialSpawns event) {
    if (!(event.getLevel() instanceof ServerLevelAccessor access)) return;
    Planet planet = GalaxyData.get(access.getLevel().getServer()).find(access.getLevel());
    if (planet == null) return;
    for (Entry entry : planet.spawns) {
      EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entry.entity);
      if (event.getMobCategory() != MobCategory.MONSTER
          && type.getCategory() == event.getMobCategory()) event.addSpawnerData(entry.spawner());
    }
  }

  public static void finalizeSpawn(FinalizeSpawnEvent event) {
    if (event.getSpawnType() != MobSpawnType.NATURAL) return;
    ServerLevel level = event.getLevel().getLevel();
    Planet planet = GalaxyData.get(level.getServer()).find(level);
    if (planet == null) return;
    Mob mob = event.getEntity();
    ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
    List<Entry> matching =
        planet.spawns.stream().filter(entry -> entry.entity.equals(type)).toList();
    if (matching.isEmpty()) return;
    long total = matching.stream().mapToLong(Entry::weight).sum();
    long roll = (long) (mob.getRandom().nextDouble() * total);
    for (Entry entry : matching) {
      roll -= entry.weight;
      if (roll < 0) {
        if (!entry.data.isEmpty()) PENDING.put(mob, entry.data.copy());
        return;
      }
    }
  }

  public static void joined(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof Mob mob)) return;
    CompoundTag data = PENDING.remove(mob);
    if (data != null && !event.loadedFromDisk()) applyData(mob, data);
  }

  public static void applyData(Mob mob, CompoundTag data) {
    CompoundTag saved = mob.saveWithoutId(new CompoundTag());
    CompoundTag merged = saved.copy().merge(data);
    merged.putUUID("UUID", mob.getUUID());
    merged.put("Pos", saved.get("Pos").copy());
    mob.load(merged);
  }
}

package advRocketry.orbit;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class Mission {
  public enum Kind {
    ASTEROID,
    GAS
  }

  public final Kind kind;
  public final CompoundTag rocket;
  public ResourceKey<Level> dimension;
  public long startTick;
  public long duration;
  public String asteroidId;
  public AsteroidCatalog.Asteroid asteroid;
  public long asteroidSeed;
  public final Map<ResearchType, Integer> research = new EnumMap<>(ResearchType.class);
  public ResourceLocation gas;

  public Mission(Kind kind, CompoundTag rocket) {
    this.kind = kind;
    this.rocket = rocket;
  }

  public UUID sourceRocket() {
    return rocket.hasUUID("source_rocket") ? rocket.getUUID("source_rocket") : null;
  }

  public boolean due(long now) {
    return now - startTick >= duration;
  }

  public int research(ResearchType type) {
    return research.getOrDefault(type, 0);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putString("kind", kind.name());
    tag.put("rocket", rocket.copy());
    if (dimension != null) tag.putString("dimension", dimension.location().toString());
    tag.putLong("start_tick", startTick);
    tag.putLong("duration", duration);
    if (asteroidId != null) tag.putString("asteroid_type", asteroidId);
    if (asteroid != null) tag.put("asteroid_definition", AsteroidCatalog.save(asteroid));
    tag.putLong("asteroid_seed", asteroidSeed);
    CompoundTag data = new CompoundTag();
    research.forEach((type, amount) -> data.putInt(type.getSerializedName(), amount));
    tag.put("research", data);
    if (gas != null) tag.putString("gas", gas.toString());
    return tag;
  }

  public static Mission load(CompoundTag tag) {
    Kind kind;
    try {
      kind = Kind.valueOf(tag.getString("kind"));
    } catch (IllegalArgumentException exception) {
      return null;
    }
    Mission mission = new Mission(kind, tag.getCompound("rocket").copy());
    ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
    if (dimension != null) mission.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
    mission.startTick = tag.getLong("start_tick");
    mission.duration = tag.getLong("duration");
    if (tag.contains("asteroid_type")) mission.asteroidId = tag.getString("asteroid_type");
    if (tag.contains("asteroid_definition"))
      mission.asteroid = AsteroidCatalog.load(tag.getCompound("asteroid_definition"));
    mission.asteroidSeed = tag.getLong("asteroid_seed");
    CompoundTag data = tag.getCompound("research");
    for (String key : data.getAllKeys()) {
      ResearchType type = ResearchType.byId(key);
      if (type != null) mission.research.put(type, data.getInt(key));
    }
    if (tag.contains("gas")) mission.gas = ResourceLocation.tryParse(tag.getString("gas"));
    return mission;
  }
}

package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class Satellite {
  public long id;
  public SatelliteType type;
  public int powerGeneration;
  public int powerStorage;
  public int maxData;
  public boolean deployed;
  public int orbitPlanet = -1;
  public long deployedTime;
  public long lastTick;
  public int energy;
  public int data;
  public BiomeJob biomeJob;

  public Satellite() {}

  public Satellite(SatelliteType type, int powerGeneration, int powerStorage, int maxData) {
    this.type = type;
    this.powerGeneration = powerGeneration;
    this.powerStorage = powerStorage;
    this.maxData = maxData;
  }

  public boolean orbits(SatelliteType expected, int planet) {
    return deployed && type == expected && orbitPlanet == planet;
  }

  public Satellite copy() {
    return load(save());
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putLong("satellite_id", id);
    if (type != null) tag.putString("satellite_type", type.getSerializedName());
    tag.putInt("power_generation", powerGeneration);
    tag.putInt("power_storage", powerStorage);
    tag.putInt("max_data", maxData);
    tag.putBoolean("deployed", deployed);
    tag.putInt("orbit_planet", orbitPlanet);
    tag.putLong("deployed_time", deployedTime);
    tag.putLong("last_tick", lastTick);
    tag.putInt("energy", energy);
    tag.putInt("data", data);
    if (biomeJob != null) tag.put("biome_job", biomeJob.save());
    return tag;
  }

  public static Satellite load(CompoundTag tag) {
    Satellite satellite =
        new Satellite(
            SatelliteType.byName(tag.getString("satellite_type")),
            tag.getInt("power_generation"),
            tag.getInt("power_storage"),
            tag.getInt("max_data"));
    satellite.id = tag.getLong("satellite_id");
    satellite.deployed = tag.getBoolean("deployed");
    if (tag.contains("orbit_planet")) satellite.orbitPlanet = tag.getInt("orbit_planet");
    satellite.deployedTime = tag.getLong("deployed_time");
    satellite.lastTick = tag.getLong("last_tick");
    satellite.energy = tag.getInt("energy");
    satellite.data = tag.getInt("data");
    if (tag.contains("biome_job")) satellite.biomeJob = BiomeJob.load(tag.getCompound("biome_job"));
    return satellite;
  }

  public static final class BiomeJob {
    public final ResourceLocation dimension;
    public final ResourceLocation biome;
    public final int x;
    public final int z;
    public int index;

    public BiomeJob(ResourceLocation dimension, ResourceLocation biome, int x, int z) {
      this.dimension = dimension;
      this.biome = biome;
      this.x = x;
      this.z = z;
    }

    public BlockPos cell(int minY) {
      return new BlockPos(x + index % 8 * 4, minY, z + index / 8 * 4);
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("dimension", dimension.toString());
      tag.putString("biome", biome.toString());
      tag.putInt("x", x);
      tag.putInt("z", z);
      tag.putInt("index", index);
      return tag;
    }

    static BiomeJob load(CompoundTag tag) {
      ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
      ResourceLocation biome = ResourceLocation.tryParse(tag.getString("biome"));
      if (dimension == null || biome == null) return null;
      BiomeJob job = new BiomeJob(dimension, biome, tag.getInt("x"), tag.getInt("z"));
      job.index = tag.getInt("index");
      return job;
    }
  }
}

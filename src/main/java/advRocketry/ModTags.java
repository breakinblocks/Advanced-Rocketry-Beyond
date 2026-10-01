package advRocketry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class ModTags {
  public static final TagKey<Item> LASER_DRILL_ORES = tag(Registries.ITEM, "laser_drill_ores");
  public static final TagKey<Block> GEODE_ORES = tag(Registries.BLOCK, "geode_ores");
  public static final TagKey<Block> MOTORS = tag(Registries.BLOCK, "motors");
  public static final TagKey<Block> COILS = tag(Registries.BLOCK, "coils");
  public static final TagKey<Block> MACHINE_PORTS = tag(Registries.BLOCK, "machine_ports");
  public static final TagKey<Block> ROCKET_BLACKLIST = tag(Registries.BLOCK, "rocket_blacklist");
  public static final TagKey<Block> TORCHES = tag(Registries.BLOCK, "torches");
  public static final TagKey<Block> SEALABLE = tag(Registries.BLOCK, "sealable");
  public static final TagKey<Block> UNSEALABLE = tag(Registries.BLOCK, "unsealable");
  public static final TagKey<EntityType<?>> ATMOSPHERE_IMMUNE =
      tag(Registries.ENTITY_TYPE, "atmosphere_immune");
  public static final TagKey<Fluid> HARVESTABLE_GASES = tag(Registries.FLUID, "harvestable_gases");
  public static final TagKey<Biome> EXCLUDED_PLANET_BIOMES =
      tag(Registries.BIOME, "planet_biomes/excluded");
  public static final TagKey<Biome> HIGH_PRESSURE_BIOMES =
      tag(Registries.BIOME, "planet_biomes/high_pressure");
  public static final TagKey<Biome> SINGLE_BIOMES = tag(Registries.BIOME, "planet_biomes/single");
  public static final TagKey<Biome> AIRLESS_BIOMES = tag(Registries.BIOME, "planet_biomes/airless");
  public static final TagKey<Biome> SCORCHED_BIOMES =
      tag(Registries.BIOME, "planet_biomes/scorched");

  private ModTags() {}

  private static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String path) {
    return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath(Main.MODID, path));
  }
}

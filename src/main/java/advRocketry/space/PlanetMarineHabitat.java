package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** Serialized ocean constraints shared by native and optional mod biomes. */
public record PlanetMarineHabitat(
    boolean water, Holder<Biome> landFallback, int minimumDepth, int deepDepth) {
  private static final TagKey<Biome> OCEAN = tag("c:is_ocean");
  private static final TagKey<Biome> DEEP = tag("c:is_deep_ocean");
  private static final TagKey<Biome> SHALLOW = tag("c:is_shallow_ocean");
  private static final TagKey<Biome> PLANET_OCEAN = tag("adv_rocketry:planet_biomes/ocean");
  private static final TagKey<Biome> PLANET_DEEP = tag("adv_rocketry:planet_biomes/deep_ocean");
  private static final TagKey<Biome> PLANET_SHALLOW =
      tag("adv_rocketry:planet_biomes/shallow_ocean");
  public static final Codec<PlanetMarineHabitat> CODEC =
      RecordCodecBuilder.create(
          i ->
              i.group(
                      Codec.BOOL.fieldOf("water").forGetter(PlanetMarineHabitat::water),
                      Biome.CODEC
                          .fieldOf("land_fallback")
                          .forGetter(PlanetMarineHabitat::landFallback),
                      Codec.intRange(1, 128)
                          .optionalFieldOf("minimum_depth", 3)
                          .forGetter(PlanetMarineHabitat::minimumDepth),
                      Codec.intRange(1, 256)
                          .optionalFieldOf("deep_depth", 24)
                          .forGetter(PlanetMarineHabitat::deepDepth))
                  .apply(i, PlanetMarineHabitat::new));

  public static boolean supportsWater(Planet planet) {
    return !planet.gasGiant
        && planet.terrain != TerrainType.SPACE
        && planet.atmosphere > 25
        && planet.temperature >= 175
        && planet.temperature <= 450
        && PlanetLandforms.profile(planet).equals("temperate")
        && planet.ocean.equals(ResourceLocation.withDefaultNamespace("water"));
  }

  public static boolean ocean(Holder<Biome> biome) {
    return biome.is(BiomeTags.IS_OCEAN)
        || biome.is(OCEAN)
        || biome.is(PLANET_OCEAN)
        || deep(biome)
        || shallow(biome);
  }

  public static boolean deep(Holder<Biome> biome) {
    return biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(DEEP) || biome.is(PLANET_DEEP);
  }

  public static boolean shallow(Holder<Biome> biome) {
    return biome.is(SHALLOW) || biome.is(PLANET_SHALLOW);
  }

  private static TagKey<Biome> tag(String id) {
    return TagKey.create(Registries.BIOME, ResourceLocation.parse(id));
  }

  public boolean allows(Holder<Biome> biome, double waterDepth, double continents) {
    return water
        && continents < -.25
        && waterDepth >= minimumDepth
        && (!deep(biome) || waterDepth >= deepDepth)
        && (!shallow(biome) || waterDepth < deepDepth);
  }
}

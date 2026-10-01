package advRocketry.space;

import advRocketry.util.IdOrTag;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record PlanetDefinition(
    Identity identity,
    Physical physical,
    Orbit orbit,
    Sky sky,
    Terrain terrain,
    Features features,
    Resources resources) {
  private static final Codec<Integer> COLOR =
      Codec.withAlternative(
          Codec.STRING.comapFlatMap(
              text -> {
                String hex = text.startsWith("#") ? text.substring(1) : text;
                try {
                  return hex.length() == 6
                      ? DataResult.success(HexFormat.fromHexDigits(hex))
                      : DataResult.error(() -> "Color must be #RRGGBB: " + text);
                } catch (IllegalArgumentException invalid) {
                  return DataResult.error(() -> "Color must be #RRGGBB: " + text);
                }
              },
              color -> String.format("#%06x", color & 0xffffff)),
          Codec.intRange(0, 0xffffff));
  private static final Codec<Float> FREQUENCY = Codec.floatRange(0, Float.MAX_VALUE);

  public record Identity(
      String name,
      ResourceLocation star,
      Optional<ResourceLocation> parent,
      Optional<ResourceLocation> dimension,
      boolean known,
      String icon) {
    static final MapCodec<Identity> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("name").forGetter(Identity::name),
                        ResourceLocation.CODEC.fieldOf("star").forGetter(Identity::star),
                        ResourceLocation.CODEC
                            .optionalFieldOf("parent")
                            .forGetter(Identity::parent),
                        ResourceLocation.CODEC
                            .optionalFieldOf("dimension")
                            .forGetter(Identity::dimension),
                        Codec.BOOL.optionalFieldOf("known", true).forGetter(Identity::known),
                        Codec.STRING.optionalFieldOf("icon", "").forGetter(Identity::icon))
                    .apply(instance, Identity::new));
  }

  public record Physical(
      boolean gasGiant,
      int atmosphere,
      boolean oxygen,
      Optional<Integer> temperature,
      float gravity,
      List<ResourceLocation> gases) {
    static final MapCodec<Physical> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        Codec.BOOL
                            .optionalFieldOf("gas_giant", false)
                            .forGetter(Physical::gasGiant),
                        Codec.intRange(0, 1600)
                            .optionalFieldOf("atmosphere", 100)
                            .forGetter(Physical::atmosphere),
                        Codec.BOOL.optionalFieldOf("oxygen", true).forGetter(Physical::oxygen),
                        Codec.INT.optionalFieldOf("temperature").forGetter(Physical::temperature),
                        Codec.floatRange(Float.MIN_NORMAL, Float.MAX_VALUE)
                            .optionalFieldOf("gravity", 1f)
                            .forGetter(Physical::gravity),
                        ResourceLocation.CODEC
                            .listOf()
                            .optionalFieldOf("gases", List.of())
                            .forGetter(Physical::gases))
                    .apply(instance, Physical::new));
  }

  public record Orbit(
      int distance, double theta, double phi, long rotationPeriod, boolean retrograde) {
    static final MapCodec<Orbit> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("orbital_distance", 100)
                            .forGetter(Orbit::distance),
                        Codec.DOUBLE.optionalFieldOf("orbital_theta", 0d).forGetter(Orbit::theta),
                        Codec.DOUBLE.optionalFieldOf("orbital_phi", 0d).forGetter(Orbit::phi),
                        Codec.LONG
                            .validate(
                                value ->
                                    value > 0
                                        ? DataResult.success(value)
                                        : DataResult.error(
                                            () -> "Rotation period must be positive"))
                            .optionalFieldOf("rotation_period", 24000L)
                            .forGetter(Orbit::rotationPeriod),
                        Codec.BOOL
                            .optionalFieldOf("retrograde", false)
                            .forGetter(Orbit::retrograde))
                    .apply(instance, Orbit::new));
  }

  public record Sky(
      int skyColor,
      int fogColor,
      boolean rings,
      int ringColor,
      boolean renderOverride,
      boolean colorOverride,
      Optional<Boolean> shading) {
    static final MapCodec<Sky> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        COLOR.optionalFieldOf("sky_color", 0xffffff).forGetter(Sky::skyColor),
                        COLOR.optionalFieldOf("fog_color", 0xc0d8ff).forGetter(Sky::fogColor),
                        Codec.BOOL.optionalFieldOf("rings", false).forGetter(Sky::rings),
                        COLOR.optionalFieldOf("ring_color", 0xffffff).forGetter(Sky::ringColor),
                        Codec.BOOL
                            .optionalFieldOf("sky_render_override", false)
                            .forGetter(Sky::renderOverride),
                        Codec.BOOL
                            .optionalFieldOf("color_override", false)
                            .forGetter(Sky::colorOverride),
                        Codec.BOOL.optionalFieldOf("shading").forGetter(Sky::shading))
                    .apply(instance, Sky::new));
  }

  public record WeightedBiome(ResourceLocation biome, Optional<Integer> weight) {
    private static final Codec<WeightedBiome> FULL =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ResourceLocation.CODEC.fieldOf("biome").forGetter(WeightedBiome::biome),
                        Codec.intRange(1, 1000000)
                            .optionalFieldOf("weight")
                            .forGetter(WeightedBiome::weight))
                    .apply(instance, WeightedBiome::new));
    static final Codec<WeightedBiome> CODEC =
        Codec.withAlternative(
            FULL,
            ResourceLocation.CODEC.xmap(
                biome -> new WeightedBiome(biome, Optional.empty()), WeightedBiome::biome));
  }

  public record Terrain(
      TerrainType type,
      int seaLevel,
      ResourceLocation filler,
      ResourceLocation ocean,
      boolean caves,
      boolean structures,
      boolean forceRivers,
      List<WeightedBiome> biomes,
      Map<ResourceLocation, Integer> craterBiomes) {
    static final MapCodec<Terrain> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        TerrainType.DEFINITION_CODEC
                            .optionalFieldOf("terrain", TerrainType.TERRESTRIAL)
                            .forGetter(Terrain::type),
                        Codec.intRange(-63, 319)
                            .optionalFieldOf("sea_level", 63)
                            .forGetter(Terrain::seaLevel),
                        ResourceLocation.CODEC
                            .optionalFieldOf(
                                "filler_block", ResourceLocation.withDefaultNamespace("stone"))
                            .forGetter(Terrain::filler),
                        ResourceLocation.CODEC
                            .optionalFieldOf(
                                "ocean_block", ResourceLocation.withDefaultNamespace("water"))
                            .forGetter(Terrain::ocean),
                        Codec.BOOL.optionalFieldOf("caves", false).forGetter(Terrain::caves),
                        Codec.BOOL
                            .optionalFieldOf("structures", false)
                            .forGetter(Terrain::structures),
                        Codec.BOOL
                            .optionalFieldOf("force_rivers", false)
                            .forGetter(Terrain::forceRivers),
                        WeightedBiome.CODEC
                            .listOf()
                            .optionalFieldOf("biomes", List.of())
                            .forGetter(Terrain::biomes),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.intRange(0, 100))
                            .optionalFieldOf("crater_biomes", Map.of())
                            .forGetter(Terrain::craterBiomes))
                    .apply(instance, Terrain::new));
  }

  public record Features(
      boolean craters,
      float craterFrequency,
      boolean volcanoes,
      float volcanoFrequency,
      boolean geodes,
      float geodeFrequency) {
    static final MapCodec<Features> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        Codec.BOOL.optionalFieldOf("craters", false).forGetter(Features::craters),
                        FREQUENCY
                            .optionalFieldOf("crater_frequency", 1f)
                            .forGetter(Features::craterFrequency),
                        Codec.BOOL
                            .optionalFieldOf("volcanoes", false)
                            .forGetter(Features::volcanoes),
                        FREQUENCY
                            .optionalFieldOf("volcano_frequency", 1f)
                            .forGetter(Features::volcanoFrequency),
                        Codec.BOOL.optionalFieldOf("geodes", false).forGetter(Features::geodes),
                        FREQUENCY
                            .optionalFieldOf("geode_frequency", 1f)
                            .forGetter(Features::geodeFrequency))
                    .apply(instance, Features::new));
  }

  public record Resources(
      List<PlanetOres.Entry> ores,
      List<PlanetSpawns.Entry> spawns,
      List<IdOrTag> laserDrillOres,
      boolean laserDrillable,
      List<IdOrTag> geodeOres,
      List<IdOrTag> craterOres,
      List<IdOrTag> warpArtifacts) {
    static final MapCodec<Resources> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        PlanetOres.Entry.CODEC
                            .listOf()
                            .optionalFieldOf("ores", List.of())
                            .forGetter(Resources::ores),
                        PlanetSpawns.Entry.CODEC
                            .listOf()
                            .optionalFieldOf("spawns", List.of())
                            .forGetter(Resources::spawns),
                        IdOrTag.CODEC
                            .listOf()
                            .optionalFieldOf("laser_drill_ores", List.of())
                            .forGetter(Resources::laserDrillOres),
                        Codec.BOOL
                            .optionalFieldOf("laser_drillable", true)
                            .forGetter(Resources::laserDrillable),
                        IdOrTag.CODEC
                            .listOf()
                            .optionalFieldOf("geode_ores", List.of())
                            .forGetter(Resources::geodeOres),
                        IdOrTag.CODEC
                            .listOf()
                            .optionalFieldOf("crater_ores", List.of())
                            .forGetter(Resources::craterOres),
                        IdOrTag.CODEC
                            .listOf()
                            .optionalFieldOf("warp_artifacts", List.of())
                            .forGetter(Resources::warpArtifacts))
                    .apply(instance, Resources::new));
  }

  public static final Codec<PlanetDefinition> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identity.CODEC.forGetter(PlanetDefinition::identity),
                      Physical.CODEC.forGetter(PlanetDefinition::physical),
                      Orbit.CODEC.forGetter(PlanetDefinition::orbit),
                      Sky.CODEC.forGetter(PlanetDefinition::sky),
                      Terrain.CODEC.forGetter(PlanetDefinition::terrain),
                      Features.CODEC.forGetter(PlanetDefinition::features),
                      Resources.CODEC.forGetter(PlanetDefinition::resources))
                  .apply(instance, PlanetDefinition::new));

  public Planet create(int id, ResourceLocation key, int star, int parent) {
    Planet planet = new Planet(id, identity.name());
    planet.location = key;
    planet.dimension = key;
    planet.star = star;
    planet.parent = parent;
    identity.dimension().ifPresent(dimension -> planet.dimension = dimension);
    planet.known = identity.known();
    planet.customIcon = identity.icon();
    planet.gasGiant = physical.gasGiant();
    planet.atmosphere = physical.atmosphere();
    planet.oxygen = physical.oxygen();
    physical.temperature().ifPresent(temperature -> planet.temperature = temperature);
    planet.gravity = physical.gravity();
    planet.gases.addAll(physical.gases());
    planet.orbitalDistance = orbit.distance();
    planet.orbitalTheta = Math.toRadians(orbit.theta());
    planet.orbitalPhi = orbit.phi();
    planet.rotationPeriod = orbit.rotationPeriod();
    planet.retrograde = orbit.retrograde();
    planet.skyColor = sky.skyColor();
    planet.fogColor = sky.fogColor();
    planet.rings = sky.rings();
    planet.ringColor = sky.ringColor();
    planet.skyRenderOverride = sky.renderOverride();
    planet.colorOverride = sky.colorOverride();
    planet.shading = sky.shading().orElse(null);
    planet.terrain = terrain.type();
    planet.seaLevel = terrain.seaLevel();
    planet.filler = terrain.filler();
    planet.ocean = terrain.ocean();
    planet.caves = terrain.caves();
    planet.structures = terrain.structures();
    planet.forceRivers = terrain.forceRivers();
    for (WeightedBiome biome : terrain.biomes()) {
      if (!planet.biomes.contains(biome.biome())) planet.biomes.add(biome.biome());
      biome
          .weight()
          .ifPresent(weight -> planet.biomeWeights.merge(biome.biome(), weight, Integer::sum));
    }
    planet.craterBiomeWeights.putAll(terrain.craterBiomes());
    planet.craters = features.craters();
    planet.craterFrequency = features.craterFrequency();
    planet.volcanoes = features.volcanoes();
    planet.volcanoFrequency = features.volcanoFrequency();
    planet.geodes = features.geodes();
    planet.geodeFrequency = features.geodeFrequency();
    planet.ores.addAll(resources.ores());
    planet.spawns.addAll(resources.spawns());
    planet.laserOres.addAll(resources.laserDrillOres());
    planet.laserDrillable = resources.laserDrillable();
    planet.geodeOres.addAll(resources.geodeOres());
    planet.craterOres.addAll(resources.craterOres());
    planet.artifacts.addAll(resources.warpArtifacts());
    if (planet.atmosphere == 0 && planet.terrain == TerrainType.TERRESTRIAL)
      planet.terrain = TerrainType.MOON;
    if (planet.volcanoes) planet.terrain = TerrainType.VOLCANIC;
    return planet;
  }
}

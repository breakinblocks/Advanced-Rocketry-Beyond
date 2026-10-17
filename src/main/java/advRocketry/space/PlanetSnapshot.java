package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * Explicit client schema. World generation, ore tables and spawn data never cross this boundary.
 */
public record PlanetSnapshot(
    Identity identity, Orbit orbit, Environment environment, Visuals visuals) {
  public static final Codec<PlanetSnapshot> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identity.CODEC.fieldOf("identity").forGetter(PlanetSnapshot::identity),
                      Orbit.CODEC.fieldOf("orbit").forGetter(PlanetSnapshot::orbit),
                      Environment.CODEC
                          .fieldOf("environment")
                          .forGetter(PlanetSnapshot::environment),
                      Visuals.CODEC.fieldOf("visuals").forGetter(PlanetSnapshot::visuals))
                  .apply(instance, PlanetSnapshot::new));

  public record Identity(
      int id, String name, ResourceLocation dimension, int star, int parent, boolean known) {
    public static final Codec<Identity> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.INT.fieldOf("id").forGetter(Identity::id),
                        Codec.string(0, 256).fieldOf("name").forGetter(Identity::name),
                        ResourceLocation.CODEC.fieldOf("dimension").forGetter(Identity::dimension),
                        Codec.INT.fieldOf("star").forGetter(Identity::star),
                        Codec.INT.fieldOf("parent").forGetter(Identity::parent),
                        Codec.BOOL.fieldOf("known").forGetter(Identity::known))
                    .apply(instance, Identity::new));
  }

  public record Orbit(
      float gravity,
      int orbitalDistance,
      double orbitalTheta,
      double orbitalPhi,
      long rotationPeriod,
      boolean retrograde) {
    public static final Codec<Orbit> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.floatRange(0, 1000).fieldOf("gravity").forGetter(Orbit::gravity),
                        Codec.INT.fieldOf("orbitalDistance").forGetter(Orbit::orbitalDistance),
                        Codec.DOUBLE.fieldOf("orbitalTheta").forGetter(Orbit::orbitalTheta),
                        Codec.DOUBLE.fieldOf("orbitalPhi").forGetter(Orbit::orbitalPhi),
                        Codec.LONG
                            .validate(
                                value ->
                                    value > 0
                                        ? DataResult.success(value)
                                        : DataResult.error(
                                            () -> "Rotation period must be positive"))
                            .fieldOf("rotationPeriod")
                            .forGetter(Orbit::rotationPeriod),
                        Codec.BOOL.fieldOf("retrograde").forGetter(Orbit::retrograde))
                    .apply(instance, Orbit::new));
  }

  public record Environment(
      int temperature,
      int atmosphere,
      boolean oxygen,
      boolean gasGiant,
      TerrainType terrain,
      List<ResourceLocation> biomes) {
    public static final Codec<Environment> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.INT.fieldOf("temperature").forGetter(Environment::temperature),
                        Codec.INT.fieldOf("atmosphere").forGetter(Environment::atmosphere),
                        Codec.BOOL.fieldOf("oxygen").forGetter(Environment::oxygen),
                        Codec.BOOL.fieldOf("gasGiant").forGetter(Environment::gasGiant),
                        TerrainType.CODEC.fieldOf("terrain").forGetter(Environment::terrain),
                        ResourceLocation.CODEC
                            .listOf(0, 1024)
                            .fieldOf("biomes")
                            .forGetter(Environment::biomes))
                    .apply(instance, Environment::new));

    public Environment {
      biomes = List.copyOf(biomes);
    }
  }

  public record Visuals(
      boolean rings,
      boolean skyRenderOverride,
      boolean colorOverride,
      boolean shading,
      String customIcon,
      int skyColor,
      int fogColor,
      int ringColor,
      SkySettings sky) {
    public static final Codec<Visuals> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.BOOL.fieldOf("rings").forGetter(Visuals::rings),
                        Codec.BOOL
                            .fieldOf("skyRenderOverride")
                            .forGetter(Visuals::skyRenderOverride),
                        Codec.BOOL.fieldOf("colorOverride").forGetter(Visuals::colorOverride),
                        Codec.BOOL.fieldOf("shading").forGetter(Visuals::shading),
                        Codec.string(0, 256).fieldOf("customIcon").forGetter(Visuals::customIcon),
                        Codec.INT.fieldOf("skyColor").forGetter(Visuals::skyColor),
                        Codec.INT.fieldOf("fogColor").forGetter(Visuals::fogColor),
                        Codec.INT.fieldOf("ringColor").forGetter(Visuals::ringColor),
                        SkySettings.CODEC.optionalFieldOf("sky", SkySettings.DEFAULT).forGetter(Visuals::sky))
                    .apply(instance, Visuals::new));
  }

  public static PlanetSnapshot of(Planet planet) {
    return new PlanetSnapshot(
        new Identity(
            planet.id, planet.name, planet.dimension, planet.star, planet.parent, planet.known),
        new Orbit(
            planet.gravity,
            planet.orbitalDistance,
            planet.orbitalTheta,
            planet.orbitalPhi,
            planet.rotationPeriod,
            planet.retrograde),
        new Environment(
            planet.temperature,
            planet.atmosphere,
            planet.oxygen,
            planet.gasGiant,
            planet.terrain,
            planet.biomes),
        new Visuals(
            planet.rings,
            planet.skyRenderOverride,
            planet.colorOverride,
            planet.hasShading(),
            planet.customIcon,
            planet.skyColor,
            planet.fogColor,
            planet.ringColor,
            planet.sky));
  }

  public Planet planet() {
    Planet planet = new Planet(identity.id, identity.name);
    planet.dimension = identity.dimension;
    planet.star = identity.star;
    planet.parent = identity.parent;
    planet.known = identity.known;
    planet.gravity = orbit.gravity;
    planet.orbitalDistance = orbit.orbitalDistance;
    planet.orbitalTheta = orbit.orbitalTheta;
    planet.orbitalPhi = orbit.orbitalPhi;
    planet.rotationPeriod = orbit.rotationPeriod;
    planet.retrograde = orbit.retrograde;
    planet.temperature = environment.temperature;
    planet.atmosphere = environment.atmosphere;
    planet.oxygen = environment.oxygen;
    planet.gasGiant = environment.gasGiant;
    planet.terrain = environment.terrain;
    planet.biomes.addAll(environment.biomes);
    planet.rings = visuals.rings;
    planet.skyRenderOverride = visuals.skyRenderOverride;
    planet.colorOverride = visuals.colorOverride;
    planet.shading = visuals.shading;
    planet.customIcon = visuals.customIcon;
    planet.skyColor = visuals.skyColor;
    planet.fogColor = visuals.fogColor;
    planet.ringColor = visuals.ringColor;
    planet.sky = visuals.sky;
    return planet;
  }
}

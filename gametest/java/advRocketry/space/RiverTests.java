package advRocketry.space;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class RiverTests {
  @GameTest(template = "empty")
  public static void terrainKeepsPrivatePlanetSnapshot(GameTestHelper helper) {
    var registries = helper.getLevel().registryAccess();
    var biomes = registries.registryOrThrow(Registries.BIOME);
    var plains = biomes.getHolderOrThrow(ResourceKey.create(Registries.BIOME,
        ResourceLocation.withDefaultNamespace("plains")));
    Planet planet = new Planet(9088, "Snapshot");
    planet.biomes.add(ResourceLocation.withDefaultNamespace("plains"));
    var generator = new PlanetTerrain(new PlanetBiomeSource(List.of(plains), 1),
        registries.registryOrThrow(Registries.NOISE_SETTINGS)
            .getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD), planet, 1);
    planet.atmosphere = 0;
    planet.biomes.clear();
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    var saved = (CompoundTag) PlanetTerrain.CODEC.codec().encodeStart(ops, generator).getOrThrow();
    var snapshot = Planet.load(saved.getCompound("planet"));
    helper.assertTrue(snapshot.atmosphere == 100 && snapshot.biomes.size() == 1,
        "Worldgen retained mutable gameplay planet state");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalRiverClimateRulesAndNativeValleys(GameTestHelper helper) {
    Planet planet = new Planet(1004, "River rules");
    helper.assertTrue(planet.hasRivers(), "Temperate planets lost their original default rivers");
    planet.atmosphere = 25;
    helper.assertTrue(!planet.hasRivers(), "Vacuum boundary enabled natural rivers");
    planet.atmosphere = 26;
    planet.temperature = 250;
    helper.assertTrue(!planet.hasRivers(), "Frigid boundary enabled natural rivers");
    planet.temperature = 251;
    helper.assertTrue(planet.hasRivers(), "Cold planets with enough pressure lost rivers");
    planet.temperature = 451;
    helper.assertTrue(!planet.hasRivers(), "Very hot planets enabled natural rivers");
    planet.forceRivers = true;
    helper.assertTrue(
        Planet.load(planet.save()).hasRivers(), "Forced rivers did not survive saving");
    var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
    var plains =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    var snowy =
        registry.getHolderOrThrow(
            ResourceKey.create(
                Registries.BIOME, ResourceLocation.withDefaultNamespace("snowy_plains")));
    var ocean =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("ocean")));
    var river =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("river")));
    var frozen =
        registry.getHolderOrThrow(
            ResourceKey.create(
                Registries.BIOME, ResourceLocation.withDefaultNamespace("frozen_river")));
    var source =
        new PlanetBiomeSource(
            List.of(plains), List.of(), 7L, Optional.of(river), Optional.of(frozen));
    helper.assertTrue(
        source.getNoiseBiome(0, 0, 0, climate(.5, 0)).is(river),
        "Native valley did not select a river biome");
    helper.assertTrue(
        source.getNoiseBiome(0, 0, 0, climate(.5, .4)).is(plains),
        "River replaced non-valley land");
    helper.assertTrue(
        source.getNoiseBiome(0, 0, 0, climate(-.5, 0)).is(plains),
        "River replaced offshore terrain");
    var cold =
        new PlanetBiomeSource(
            List.of(snowy), List.of(), 7L, Optional.of(river), Optional.of(frozen));
    helper.assertTrue(
        cold.getNoiseBiome(0, 0, 0, climate(.5, 0)).is(frozen),
        "Cold valley did not select a frozen river");
    var sea =
        new PlanetBiomeSource(
            List.of(ocean), List.of(), 7L, Optional.of(river), Optional.of(frozen));
    helper.assertTrue(
        sea.getNoiseBiome(0, 0, 0, climate(.5, 0)).is(ocean),
        "River overlay replaced an ocean biome");
    var ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
    var encoded = PlanetBiomeSource.CODEC.codec().encodeStart(ops, source).getOrThrow();
    var decoded = PlanetBiomeSource.CODEC.codec().parse(ops, encoded).getOrThrow();
    helper.assertTrue(
        decoded.getNoiseBiome(0, 0, 0, climate(.5, 0)).is(river),
        "River source did not survive generator reload");
    helper.succeed();
  }

  private static Climate.Sampler climate(double continent, double weirdness) {
    var zero = DensityFunctions.zero();
    return new Climate.Sampler(
        zero,
        zero,
        DensityFunctions.constant(continent),
        zero,
        zero,
        DensityFunctions.constant(weirdness),
        List.of());
  }
}

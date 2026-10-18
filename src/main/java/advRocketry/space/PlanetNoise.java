package advRocketry.space;

import advRocketry.processing.ProcessingRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/** Independent planet terrain: no dependencies on overridden Overworld/Nether noise graphs. */
public final class PlanetNoise {
  private PlanetNoise() {}

  public static boolean channels(Planet p) {
    return p.hasRivers()
        || PlanetLandforms.profile(p).equals("volcanic")
        || PlanetLandforms.profile(p).equals("lunar");
  }

  public static NoiseGeneratorSettings settings(Planet p, long seed) {
    return settings(p, seed, List.of());
  }

  public static NoiseGeneratorSettings settings(
      Planet p, long seed, List<SurfaceRules.RuleSource> importedSurfaces) {
    String profile = PlanetLandforms.profile(p);
    boolean dryLunar = p.generationVersion >= 5 && profile.equals("lunar") && p.atmosphere <= 25;
    DensityFunction zero = DensityFunctions.zero();
    NoiseRouter router =
        new NoiseRouter(
            zero,
            DensityFunctions.constant(-.15),
            zero,
            zero,
            field(p, seed, "temperature"),
            field(p, seed, "moisture"),
            field(p, seed, "continents"),
            field(p, seed, "erosion"),
            field(p, seed, "depth"),
            field(p, seed, "ridges"),
            field(p, seed, "surface"),
            DensityFunctions.interpolated(field(p, seed, "density")),
            zero,
            zero,
            zero);
    return new NoiseGeneratorSettings(
        NoiseSettings.create(-64, 384, 1, 2),
        p.fillerState(),
        p.atmosphere == 0 || dryLunar || profile.equals("fractured")
            ? Blocks.AIR.defaultBlockState()
            : BuiltInRegistries.BLOCK.get(p.ocean).defaultBlockState(),
        router,
        surface(p, importedSurfaces),
        List.of(),
        p.seaLevel,
        !p.breathable(),
        p.atmosphere > 0 && !dryLunar && !profile.equals("fractured"),
        false,
        false);
  }

  private static DensityFunction field(Planet p, long seed, String kind) {
    return new PlanetDensity(
        seed,
        PlanetLandforms.profile(p),
        p.seaLevel,
        channels(p),
        p.caves,
        kind,
        PlanetWatershed.enabled(p),
        p.gravity,
        PlanetGeology.enabled(p.generationVersion, PlanetLandforms.profile(p)));
  }

  private static SurfaceRules.RuleSource state(Block b) {
    return SurfaceRules.state(b.defaultBlockState());
  }

  private static SurfaceRules.RuleSource biome(String id, SurfaceRules.RuleSource rule) {
    return SurfaceRules.ifTrue(
        SurfaceRules.isBiome(ResourceKey.create(Registries.BIOME, BiomeMaterials.id(id))), rule);
  }

  private static SurfaceRules.RuleSource patch(String main, String accent, double threshold) {
    return SurfaceRules.sequence(
        SurfaceRules.ifTrue(
            SurfaceRules.noiseCondition(Noises.SURFACE, threshold, 2),
            state(BiomeMaterials.block(accent))),
        state(BiomeMaterials.block(main)));
  }

  public static SurfaceRules.RuleSource surface(Planet p) {
    return surface(p, List.of());
  }

  private static SurfaceRules.RuleSource surface(
      Planet p, List<SurfaceRules.RuleSource> importedSurfaces) {
    List<SurfaceRules.RuleSource> rules = new ArrayList<>();
    if (p.terrain != TerrainType.ASTEROID)
      rules.add(
          SurfaceRules.ifTrue(
              SurfaceRules.verticalGradient(
                  "adv_rocketry:bedrock", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
              state(Blocks.BEDROCK)));
    if (PlanetLandforms.profile(p).equals("glacial"))
      rules.add(
          SurfaceRules.ifTrue(
              SurfaceRules.yBlockCheck(VerticalAnchor.absolute(p.seaLevel - 12), 0),
              SurfaceRules.ifTrue(
                  SurfaceRules.not(
                      SurfaceRules.yBlockCheck(VerticalAnchor.absolute(p.seaLevel + 7), 0)),
                  biome("crystalchasms", state(Blocks.PACKED_ICE)))));
    List<SurfaceRules.RuleSource> top = new ArrayList<>();
    top.add(biome("moon", state(ProcessingRegistry.part("moon_turf"))));
    top.add(biome("moondark", state(BiomeMaterials.block("dark_regolith"))));
    top.add(biome("lunar_tubes", state(BiomeMaterials.block("lunar_breccia"))));
    top.add(biome("hotdryrock", patch("banded_stone", "mineral_crust", .25)));
    top.add(biome("volcanic", patch("cooling_crust", "volcanic_ash", .15)));
    top.add(biome("volcanicbarren", patch("weathered_basalt", "porous_stone", .2)));
    top.add(biome("lava_tubes", state(BiomeMaterials.block("weathered_basalt"))));
    top.add(
        biome(
            "crystalchasms",
            PlanetLandforms.profile(p).equals("glacial")
                ? SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.steep(), state(Blocks.PACKED_ICE)),
                    state(Blocks.SNOW_BLOCK))
                : state(Blocks.SNOW_BLOCK)));
    top.add(
        biome(
            "subglacial_caves",
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                    SurfaceRules.noiseCondition(Noises.SURFACE, .2, 2), state(Blocks.PACKED_ICE)),
                state(BiomeMaterials.block("glacial_stone")))));
    top.add(biome("mineral_caves", patch("banded_stone", "mineral_crust", .1)));
    top.add(biome("root_caverns", patch("rooted_loam", "luminous_moss", .35)));
    top.add(biome("fungal_caverns", state(Blocks.MYCELIUM)));
    top.add(biome("alien_river", state(BiomeMaterials.block("alluvial_silt"))));
    top.add(biome("oceanspires", state(BiomeMaterials.block("wet_slate"))));
    top.add(
        biome(
            "stormland",
            p.generationVersion >= 5 && PlanetLandforms.profile(p).equals("storm")
                ? SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.steep(), patch("banded_stone", "strike_glass", .65)),
                    state(BiomeMaterials.block("rooted_loam")))
                : state(BiomeMaterials.block("rooted_loam"))));
    top.add(biome("marsh", state(BiomeMaterials.block("alluvial_silt"))));
    top.add(biome("deepswamp", state(Blocks.MUD)));
    top.addAll(importedSurfaces);
    top.add(SurfaceRules.ifTrue(SurfaceRules.steep(), state(BiomeMaterials.block("banded_stone"))));
    top.add(SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), state(Blocks.GRASS_BLOCK)));
    top.add(state(BiomeMaterials.block("alluvial_silt")));
    rules.add(
        SurfaceRules.ifTrue(
            SurfaceRules.ON_FLOOR,
            SurfaceRules.sequence(top.toArray(SurfaceRules.RuleSource[]::new))));
    rules.add(
        SurfaceRules.ifTrue(
            SurfaceRules.UNDER_FLOOR,
            SurfaceRules.sequence(
                biome("moon", state(BiomeMaterials.block("lunar_breccia"))),
                biome("moondark", state(BiomeMaterials.block("lunar_breccia"))),
                biome("crystalchasms", state(Blocks.PACKED_ICE)),
                biome("volcanic", state(BiomeMaterials.block("weathered_basalt"))),
                biome("volcanicbarren", patch("weathered_basalt", "porous_stone", .2)),
                biome("alien_forest", state(BiomeMaterials.block("rooted_loam"))),
                importedSurfaces.isEmpty()
                    ? state(p.fillerState().getBlock())
                    : SurfaceRules.sequence(
                        SurfaceRules.sequence(
                            importedSurfaces.toArray(SurfaceRules.RuleSource[]::new)),
                        state(p.fillerState().getBlock())))));
    return SurfaceRules.sequence(rules.toArray(SurfaceRules.RuleSource[]::new));
  }
}

package advRocketry.space;

import advRocketry.Main;
import advRocketry.processing.ProcessingRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Habitat-aware, bounded features. All shapes and distributions are authored for AR. */
public final class PlanetVegetation extends Feature<NoneFeatureConfiguration> {
  private static final DeferredRegister<Feature<?>> FEATURES =
      DeferredRegister.create(Registries.FEATURE, Main.MODID);
  public static final Supplier<PlanetVegetation> ECOSYSTEM =
      FEATURES.register("planet_ecosystem", () -> new PlanetVegetation(false));
  public static final Supplier<PlanetVegetation> ROOTWOOD =
      FEATURES.register("rootwood_tree", () -> new PlanetVegetation(true));
  private final boolean sapling;

  private PlanetVegetation(boolean sapling) {
    super(NoneFeatureConfiguration.CODEC);
    this.sapling = sapling;
  }

  public static void register(IEventBus bus) {
    FEATURES.register(bus);
  }

  @Override
  public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
    WorldGenLevel level = context.level();
    RandomSource random = context.random();
    if (sapling) return tree(level, context.origin(), random, true, false);
    Planet planet =
        context.chunkGenerator() instanceof PlanetTerrain terrain ? terrain.environment() : null;
    if (planet != null && (planet.generationVersion < 2 || planet.terrain == TerrainType.SPACE))
      return false;
    int ox = context.origin().getX() & ~15, oz = context.origin().getZ() & ~15;
    boolean living =
        planet == null
            || planet.atmosphere > 25 && planet.temperature > 175 && planet.temperature <= 450;
    boolean watershed = planet != null && PlanetWatershed.enabled(planet);
    long seed =
        watershed && context.chunkGenerator() instanceof PlanetTerrain terrain
            ? terrain.terrainSeed()
            : level.getSeed();
    // Sparse mature trees, younger forms and clearings share a smooth coverage field.
    for (int attempt = 0; attempt < 11; attempt++) {
      BlockPos pos = surface(level, ox + random.nextInt(16), oz + random.nextInt(16));
      String biome = biome(level, pos);
      double cover = PlanetLandforms.field(seed, 151, pos.getX(), pos.getZ(), 100);
      if (watershed
          && living
          && (biome.equals("alien_forest")
              || biome.equals("deepswamp")
              || biome.equals("marsh")
              || biome.equals("alien_river"))) {
        var habitat = PlanetWatershed.sample(seed, planet.gravity, pos.getX(), pos.getZ());
        double wetland =
            PlanetWatershed.wetlandStrength(seed, planet.seaLevel, pos.getX(), pos.getZ(), habitat);
        // Rootwood stands occupy raised islands; open marsh remains mostly reeds and pools.
        boolean island =
            wetland > .3
                && pos.getY() >= planet.seaLevel
                && natural(level.getBlockState(pos.below()));
        if (cover > -.3
            && (!biome.equals("marsh")
                || island && random.nextBoolean()
                || random.nextInt(4) == 0)) {
          boolean roots = island || habitat.wetness() > .62 || biome.equals("deepswamp");
          if (random.nextInt(12) == 0) fallenTree(level, pos, random, roots);
          else if (random.nextDouble() < .65 + Math.max(0, cover) * .3) {
            double slope =
                Math.abs(
                    PlanetWatershed.height(
                            seed, planet.seaLevel, planet.gravity, pos.getX() + 4, pos.getZ())
                        - PlanetWatershed.height(
                            seed, planet.seaLevel, planet.gravity, pos.getX() - 4, pos.getZ()));
            woodlandTree(level, pos, random, roots, slope > 5 || cover < -.1);
          }
        }
      } else if (living
          && (biome.equals("alien_forest") || biome.equals("deepswamp"))
          && cover > -.25
          && random.nextInt(3) != 0) tree(level, pos, random, biome.equals("deepswamp"), false);
      if (living && biome.equals("stormland") && random.nextBoolean())
        tree(level, pos, random, false, true);
    }
    for (int attempt = 0; attempt < 55; attempt++) {
      BlockPos pos = surface(level, ox + random.nextInt(16), oz + random.nextInt(16));
      String biome = biome(level, pos);
      double cover = PlanetLandforms.field(seed, 157, pos.getX(), pos.getZ(), 45);
      if (watershed && living && riparian(biome) && riverbank(level, pos, random, seed, planet))
        continue;
      if (living && cover > -.35) {
        switch (biome) {
          case "alien_forest" ->
              plant(
                  level,
                  pos,
                  waterNear(level, pos)
                      ? "jointed_reeds"
                      : random.nextInt(5) == 0
                          ? "spiral_seedling"
                          : random.nextInt(3) == 0
                              ? "forest_groundcover"
                              : random.nextInt(8) == 0 ? "fallen_foliage" : "fan_fern",
                  Direction.UP);
          case "marsh", "deepswamp", "alien_river" -> {
            if (waterNear(level, pos))
              plant(
                  level, pos, random.nextBoolean() ? "jointed_reeds" : "marsh_sedge", Direction.UP);
            else if (random.nextInt(4) == 0) plant(level, pos, "spiral_seedling", Direction.UP);
          }
          case "stormland" ->
              plant(
                  level,
                  pos,
                  random.nextInt(4) == 0 ? "spark_mushroom" : "storm_blades",
                  Direction.UP);
          case "oceanspires" -> {
            if (random.nextInt(7) == 0) plant(level, pos, "salt_shrub", Direction.UP);
          }
          default -> {}
        }
      }
      if ((biome.equals("volcanic") || biome.equals("hotdryrock"))
          && random.nextInt(16) == 0
          && planet != null
          && planet.atmosphere > 25)
        plant(level, pos, biome.equals("volcanic") ? "vent_growth" : "ember_curl", Direction.UP);
      if (living
          && (biome.equals("marsh")
              || biome.equals("deepswamp")
              || biome.equals("alien_river")
              || biome.equals("oceanspires"))) {
        BlockPos floor = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos);
        if (level.getFluidState(floor).is(FluidTags.WATER)) {
          int waterDepth = pos.getY() - floor.getY();
          double meadow = PlanetLandforms.field(seed, 277, floor.getX(), floor.getZ(), 28);
          if (!biome.equals("oceanspires") || waterDepth <= 18 && meadow > -.15)
            plant(level, floor, "aquatic_frond", Direction.UP);
          if (!biome.equals("oceanspires") && random.nextInt(4) == 0 && waterDepth < 5)
            plant(level, pos, "floating_leaves", Direction.UP);
        }
      }
    }
    // Search every exposed layer in selected columns, not just the top heightmap.
    for (int attempt = 0; attempt < 26; attempt++) {
      int x = ox + random.nextInt(16), z = oz + random.nextInt(16);
      int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 9;
      for (int y = level.getMinBuildHeight() + 7; y < top; y++) {
        BlockPos pos = new BlockPos(x, y, z);
        if (watershed
            && living
            && level.getFluidState(pos).is(FluidTags.WATER)
            && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
            && Math.abs(y - PlanetWatershed.waterLevel(planet.seaLevel)) < 6
            && random.nextInt(3) == 0) plant(level, pos, "aquatic_frond", Direction.UP);
        if (!level.isEmptyBlock(pos)) continue;
        String biome = biome(level, pos);
        boolean lush =
            living
                && (biome.equals("root_caverns")
                    || biome.equals("fungal_caverns")
                    || watershed && biome.equals("lush_caves"));
        if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
          if (lush && random.nextInt(3) != 0) {
            if (natural(level.getBlockState(pos.below())))
              level.setBlock(
                  pos.below(),
                  BiomeMaterials.block(random.nextInt(9) == 0 ? "luminous_moss" : "rooted_loam")
                      .defaultBlockState(),
                  2);
            plant(
                level,
                pos,
                biome.equals("fungal_caverns")
                    ? "cave_blossom"
                    : random.nextInt(5) == 0 ? "spiral_seedling" : "fan_fern",
                Direction.UP);
          } else if (biome.equals("mineral_caves")
              || biome.equals("subglacial_caves")
              || biome.equals("lunar_tubes")) {
            if (random.nextInt(9) == 0) plant(level, pos, "mineral_bloom", Direction.UP);
          } else if (biome.equals("lava_tubes")
              && planet != null
              && planet.atmosphere > 25
              && random.nextInt(12) == 0) {
            plant(level, pos, "vent_growth", Direction.UP);
          }
          if (lush && random.nextInt(50) == 0) tree(level, pos, random, true, false);
        }
        if (lush
            && level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)
            && random.nextBoolean()) {
          int length = 1 + random.nextInt(5);
          for (int down = 0; down < length; down++) {
            BlockPos root = pos.below(down);
            if (!plant(level, root, "hanging_roots", Direction.DOWN)) break;
            if (down == length - 1 && random.nextInt(3) == 0)
              plant(level, root.below(), "luminous_seed_cluster", Direction.DOWN);
          }
        }
        if (lush && random.nextInt(13) == 0)
          for (Direction direction : Direction.Plane.HORIZONTAL)
            if (plant(level, pos, "shelf_fungus", direction)) break;
      }
    }
    BlockPos center = surface(level, ox + 8, oz + 8);
    String biome = biome(level, center);
    if (random.nextInt(4) == 0) geology(level, center, random, biome);
    return true;
  }

  private static String biome(WorldGenLevel level, BlockPos pos) {
    return level
        .getBiome(pos)
        .unwrapKey()
        .filter(
            k ->
                k.location().getNamespace().equals(Main.MODID)
                    || k.location().toString().equals("minecraft:lush_caves"))
        .map(k -> k.location().getPath())
        .orElse("");
  }

  private static BlockPos surface(WorldGenLevel level, int x, int z) {
    // Ignore leaves already placed by neighboring trees.
    BlockPos pos =
        level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
    for (int i = 0;
        i < 32
            && (level.getBlockState(pos.below()).is(BlockTags.LOGS)
                || level.getBlockState(pos.below()).is(BlockTags.LEAVES));
        i++) pos = pos.below();
    return pos;
  }

  private static boolean waterNear(WorldGenLevel level, BlockPos pos) {
    for (Direction d : Direction.Plane.HORIZONTAL)
      if (level.getFluidState(pos.below().relative(d)).is(FluidTags.WATER)) return true;
    return false;
  }

  private static boolean riparian(String biome) {
    return biome.equals("alien_forest")
        || biome.equals("marsh")
        || biome.equals("deepswamp")
        || biome.equals("alien_river");
  }

  private static boolean riverbank(
      WorldGenLevel level, BlockPos pos, RandomSource random, long seed, Planet planet) {
    var habitat = PlanetWatershed.sample(seed, planet.gravity, pos.getX(), pos.getZ());
    if (habitat.banks() < .35 || pos.getY() > planet.seaLevel + 7) return false;
    BlockPos floor = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos);
    int depth = pos.getY() - floor.getY();
    double wetland =
        PlanetWatershed.wetlandStrength(seed, planet.seaLevel, pos.getX(), pos.getZ(), habitat);
    if (depth > 0 && level.getFluidState(floor).is(FluidTags.WATER)) {
      if (depth <= 4 && random.nextInt(3) != 0) {
        plant(level, floor, "aquatic_frond", Direction.UP);
        if (depth <= 2 && habitat.wetness() > .65 && random.nextInt(wetland > .3 ? 2 : 4) == 0)
          plant(level, pos, "floating_leaves", Direction.UP);
      }
      return true;
    }
    // Smooth patches leave exposed bars between banks of sedges and ferns.
    double patch = PlanetLandforms.field(seed, 239, pos.getX(), pos.getZ(), 13);
    if (patch < (wetland > .3 ? -.55 : -.25) || !natural(level.getBlockState(pos.below())))
      return true;
    if (random.nextInt(75) == 0 && level.isEmptyBlock(pos)) {
      level.setBlock(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
      Direction direction = Direction.from2DDataValue(random.nextInt(4));
      BlockPos side = pos.relative(direction);
      if (level.isEmptyBlock(side) && natural(level.getBlockState(side.below())))
        level.setBlock(side, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
      return true;
    }
    if (random.nextInt(90) == 0 && fallenTree(level, pos, random, true)) return true;
    String plant =
        waterNear(level, pos)
            ? "jointed_reeds"
            : habitat.wetness() > .6
                ? "marsh_sedge"
                : random.nextInt(4) == 0 ? "spiral_seedling" : "forest_groundcover";
    plant(level, pos, plant, Direction.UP);
    if (random.nextInt(3) == 0)
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        BlockPos neighbor =
            surface(level, pos.getX() + direction.getStepX(), pos.getZ() + direction.getStepZ());
        if (Math.abs(neighbor.getY() - pos.getY()) <= 1)
          plant(level, neighbor, plant, Direction.UP);
      }
    return true;
  }

  private static boolean natural(BlockState state) {
    return state.is(Blocks.STONE)
        || state.is(Blocks.DEEPSLATE)
        || state.is(BlockTags.DIRT)
        || state.is(Blocks.MOSS_BLOCK)
        || state.is(Blocks.MUD)
        || state.is(BiomeMaterials.block("rooted_loam"))
        || state.is(BiomeMaterials.block("alluvial_silt"))
        || state.is(BiomeMaterials.block("banded_stone"));
  }

  public static boolean plant(WorldGenLevel level, BlockPos pos, String name, Direction direction) {
    if (!level.isEmptyBlock(pos) && !level.getBlockState(pos).is(Blocks.WATER)) return false;
    boolean water = level.getFluidState(pos).is(FluidTags.WATER);
    String habitat = BiomeMaterials.PLANTS.get(name);
    if (water && !habitat.equals("aquatic")) return false;
    BlockState state =
        BiomeMaterials.block(name)
            .defaultBlockState()
            .setValue(BlockStateProperties.FACING, direction)
            .setValue(BlockStateProperties.WATERLOGGED, water);
    if (!state.canSurvive(level, pos)) return false;
    return level.setBlock(pos, state, 2);
  }

  public static boolean tree(
      WorldGenLevel level, BlockPos base, RandomSource random, boolean rootwood, boolean dead) {
    int height = dead ? 5 + random.nextInt(5) : 5 + random.nextInt(6);
    return buildTree(level, base, random, rootwood, dead, dead, height, dead ? 2 : 3);
  }

  public static boolean woodlandTree(
      WorldGenLevel level, BlockPos base, RandomSource random, boolean rootwood, boolean exposed) {
    int age = exposed ? 0 : random.nextInt(10);
    boolean dead = age == 9 && random.nextInt(3) == 0;
    int height =
        age < 3 ? 4 + random.nextInt(3) : age < 7 ? 7 + random.nextInt(4) : 12 + random.nextInt(5);
    int radius = dead ? 2 : age < 3 ? 2 : age < 7 ? 3 : 4;
    boolean placed = buildTree(level, base, random, rootwood, dead, false, height, radius);
    if (placed) {
      for (int i = 0; i < 8; i++) {
        BlockPos litter =
            surface(
                level, base.getX() + random.nextInt(7) - 3, base.getZ() + random.nextInt(7) - 3);
        if (Math.abs(litter.getY() - base.getY()) <= 2)
          plant(
              level,
              litter,
              random.nextInt(4) == 0 ? "spiral_seedling" : "fallen_foliage",
              Direction.UP);
      }
    }
    return placed;
  }

  private static boolean buildTree(
      WorldGenLevel level,
      BlockPos base,
      RandomSource random,
      boolean rootwood,
      boolean dead,
      boolean burned,
      int height,
      int radius) {
    BlockState ground = level.getBlockState(base.below());
    if (!natural(ground)) return false;
    if (base.getY() + height + 3 >= level.getMaxBuildHeight()) return false;
    // Validate the whole footprint before writing; no half trees or clipped crowns.
    for (int y = 0; y <= height + 2; y++)
      for (int x = -radius - 3; x <= radius + 3; x++)
        for (int z = -radius - 3; z <= radius + 3; z++) {
          if (y < Math.max(1, height - 6) && (x != 0 || z != 0)) continue;
          BlockState s = level.getBlockState(base.offset(x, y, z));
          if (!s.isAir() && !s.canBeReplaced() && !s.is(BlockTags.LEAVES)) return false;
          if (!s.getFluidState().isEmpty()) return false;
        }
    Block log =
        burned
            ? ProcessingRegistry.part("charcoal_log")
            : rootwood
                ? BiomeMaterials.block("rootwood_log")
                : ProcessingRegistry.part("alien_log");
    BlockState wood = log.defaultBlockState();
    List<BlockPos> logs = new ArrayList<>();
    List<BlockPos> crowns = new ArrayList<>();
    for (int y = 0; y < height; y++) {
      level.setBlock(base.above(y), wood, 2);
      logs.add(base.above(y));
    }
    for (Direction d : Direction.Plane.HORIZONTAL) {
      int length = rootwood ? radius : Math.max(1, radius - 1),
          by = Math.max(1, height - (rootwood ? 4 : 3) - random.nextInt(2));
      for (int step = 1; step <= length; step++) {
        BlockPos branch = base.above(by + (rootwood ? step / 3 : step / 2)).relative(d, step);
        BlockState axis =
            wood.hasProperty(RotatedPillarBlock.AXIS)
                ? wood.setValue(RotatedPillarBlock.AXIS, d.getAxis())
                : wood;
        if (step % (rootwood ? 3 : 2) == 0) {
          BlockPos joint = branch.below();
          level.setBlock(joint, wood, 2);
          logs.add(joint);
        }
        level.setBlock(branch, axis, 2);
        logs.add(branch);
      }
      crowns.add(base.above(by + (rootwood ? length / 3 : length / 2)).relative(d, length));
      if (rootwood && !dead) {
        BlockPos root = base.relative(d);
        if (level.isEmptyBlock(root) && level.getBlockState(root.below()).isSolid()) {
          level.setBlock(root, wood, 2);
          if (radius >= 3) {
            BlockPos tip = base.relative(d, 2);
            if (level.isEmptyBlock(tip) && natural(level.getBlockState(tip.below())))
              level.setBlock(tip, wood.setValue(RotatedPillarBlock.AXIS, d.getAxis()), 2);
          }
        }
      }
    }
    if (!dead) {
      BlockState leaves =
          (rootwood
                  ? BiomeMaterials.block("rootwood_leaves")
                  : ProcessingRegistry.part("alien_leaves"))
              .defaultBlockState()
              .setValue(LeavesBlock.DISTANCE, 1);
      crowns.add(base.above(height - 1));
      // Rootwood spreads into low, overlapping lobes; bluewood keeps stepped, rising crowns.
      for (BlockPos crown : crowns) {
        int width = rootwood ? Math.min(3, radius) : Math.min(2, radius);
        for (int dy = -1; dy <= 2; dy++) {
          int r = dy == 2 ? 1 : width;
          for (int x = -r; x <= r; x++)
            for (int z = -r; z <= r; z++) {
              if (x * x + z * z > r * r + (dy == 0 ? 1 : 0)) continue;
              BlockPos leaf = crown.offset(x, dy, z);
              int distance = logs.stream().mapToInt(at -> at.distManhattan(leaf)).min().orElse(7);
              if (distance < 7 && level.isEmptyBlock(leaf))
                level.setBlock(leaf, leaves.setValue(LeavesBlock.DISTANCE, distance), 2);
            }
        }
      }
      if (rootwood)
        for (BlockPos crown : crowns) {
          BlockPos tip = crown.below();
          while (logs.contains(tip)) tip = tip.below();
          int length = 2 + random.nextInt(3);
          for (int down = 0; down < length; down++) {
            BlockPos root = tip.below(down);
            // Overlapping crown lobes can cover the underside of a branch.
            if (level.getBlockState(root).is(BlockTags.LEAVES))
              level.setBlock(root, Blocks.AIR.defaultBlockState(), 2);
            if (!plant(level, root, "hanging_roots", Direction.DOWN)) break;
          }
        }
    }
    return true;
  }

  public static boolean fallenTree(
      WorldGenLevel level, BlockPos base, RandomSource random, boolean rootwood) {
    Direction direction = Direction.from2DDataValue(random.nextInt(4));
    int length = 3 + random.nextInt(4);
    List<BlockPos> trunk = new ArrayList<>();
    for (int step = 0; step < length; step++) {
      BlockPos at = base.relative(direction, step);
      if (!level.isEmptyBlock(at) || !natural(level.getBlockState(at.below()))) return false;
      trunk.add(at);
    }
    BlockState wood =
        (rootwood ? BiomeMaterials.block("rootwood_log") : ProcessingRegistry.part("alien_log"))
            .defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS, direction.getAxis());
    for (BlockPos at : trunk) {
      level.setBlock(at, wood, 2);
    }
    // Place the complete trunk before its dependent fungi and ground-level young growth.
    for (BlockPos at : trunk)
      for (Direction side : List.of(direction.getClockWise(), direction.getCounterClockWise())) {
        BlockPos beside = at.relative(side);
        if (random.nextInt(3) == 0) plant(level, beside, "shelf_fungus", side);
        BlockPos growth = at.relative(side, 2);
        if (!level.isEmptyBlock(growth) || random.nextBoolean()) continue;
        String name =
            rootwood && waterNear(level, growth)
                ? random.nextBoolean() ? "jointed_reeds" : "marsh_sedge"
                : random.nextInt(4) == 0
                    ? "spiral_seedling"
                    : rootwood ? "fan_fern" : "fallen_foliage";
        plant(level, growth, name, Direction.UP);
      }
    return true;
  }

  private static void geology(
      WorldGenLevel level, BlockPos base, RandomSource random, String biome) {
    if (biome.equals("crystalchasms")) {
      Block crystal =
          ProcessingRegistry.part(
              List.of("amethyst", "sapphire", "emerald", "ruby", "citrine", "wulfentite")
                      .get(random.nextInt(6))
                  + "_crystal_block");
      int height = 5 + random.nextInt(13);
      for (int y = 0; y < height; y++) {
        int radius = y < height - 3 ? 2 : 1;
        for (int x = -radius; x <= radius; x++)
          for (int z = -radius; z <= radius; z++)
            if (Math.abs(x) + Math.abs(z) <= radius + 1)
              replaceAir(level, base.offset(x + y / 7, y, z), crystal);
      }
      return;
    }
    if (biome.equals("oceanspires")) {
      int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, base.getX(), base.getZ());
      for (int x = -3; x <= 3; x++)
        for (int z = -3; z <= 3; z++)
          if (x * x + z * z <= 9)
            floorY =
                Math.max(
                    floorY,
                    level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG, base.getX() + x, base.getZ() + z));
      oceanSpire(level, new BlockPos(base.getX(), floorY, base.getZ()), base.getY(), random);
      return;
    }
    String material =
        switch (biome) {
          case "moon", "moondark" -> "lunar_breccia";
          case "volcanic", "volcanicbarren" -> "weathered_basalt";
          case "stormland" -> "strike_glass";
          case "hotdryrock" -> "banded_stone";
          default -> null;
        };
    if (material == null) return;
    int r = 1 + random.nextInt(3);
    for (int x = -r; x <= r; x++)
      for (int z = -r; z <= r; z++)
        for (int y = 0; y <= r; y++)
          if (x * x + z * z + y * y <= r * r)
            replaceAir(level, base.offset(x, y, z), BiomeMaterials.block(material));
  }

  /** Preflight the entire outcrop so obstacles never leave a partial or floating formation. */
  public static boolean oceanSpire(
      WorldGenLevel level, BlockPos floor, int waterSurface, RandomSource random) {
    int depth = waterSurface - floor.getY();
    if (depth < 4 || depth > 48 || !level.getFluidState(floor).is(FluidTags.WATER)) return false;
    int height =
        Math.min(
            42, random.nextInt(4) == 0 ? Math.max(5, depth - 3) : depth + 3 + random.nextInt(6));
    Direction lean = Direction.from2DDataValue(random.nextInt(4));
    List<BlockPos> shape = new ArrayList<>();
    // Every foundation column must meet a natural seabed within four blocks.
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 3; z++) {
        if (x * x + z * z > 9) continue;
        BlockPos at = floor.offset(x, 0, z);
        int drop = 0;
        while (!seabed(level.getBlockState(at.below())) && drop < 4) {
          if (!oceanSpace(level, at)) return false;
          at = at.below();
          drop++;
        }
        if (!seabed(level.getBlockState(at.below()))) return false;
        for (int y = at.getY(); y < floor.getY(); y++)
          shape.add(new BlockPos(at.getX(), y, at.getZ()));
      }
    for (int y = 0; y < height; y++) {
      int radius = y < height / 3 ? 3 : y < height * 3 / 4 ? 2 : 1;
      int shift = Math.min(2, y / 10);
      for (int x = -radius; x <= radius; x++)
        for (int z = -radius; z <= radius; z++) {
          if (x * x + z * z > radius * radius) continue;
          shape.add(floor.relative(lean, shift).offset(x, y, z));
        }
    }
    for (BlockPos at : shape)
      if (at.getY() >= level.getMaxBuildHeight() || !oceanSpace(level, at)) return false;
    for (BlockPos at : shape)
      level.setBlock(
          at,
          BiomeMaterials.block(at.getY() < waterSurface - 2 ? "wet_slate" : "banded_stone")
              .defaultBlockState(),
          2);
    BlockPos tip = floor.relative(lean, Math.min(2, (height - 1) / 10)).above(height);
    if (tip.getY() > waterSurface && random.nextBoolean())
      plant(level, tip, "salt_shrub", Direction.UP);
    return true;
  }

  private static boolean seabed(BlockState state) {
    return natural(state)
        || state.is(BlockTags.SAND)
        || state.is(Blocks.GRAVEL)
        || state.is(BiomeMaterials.block("wet_slate"));
  }

  private static boolean oceanSpace(WorldGenLevel level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    return state.isAir()
        || state.is(Blocks.WATER)
        || state.is(Blocks.SEAGRASS)
        || state.is(Blocks.TALL_SEAGRASS)
        || state.is(BiomeMaterials.block("aquatic_frond"));
  }

  private static void replaceAir(WorldGenLevel level, BlockPos pos, Block block) {
    if (level.isEmptyBlock(pos) || level.getBlockState(pos).is(Blocks.WATER))
      level.setBlock(pos, block.defaultBlockState(), 2);
  }
}

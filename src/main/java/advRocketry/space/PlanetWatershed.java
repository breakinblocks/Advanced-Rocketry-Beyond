package advRocketry.space;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Shared river, floodplain and subterranean-water fields, evaluated in world coordinates. */
public final class PlanetWatershed {
  private PlanetWatershed() {}

  public static boolean enabled(Planet planet) {
    return planet.generationVersion >= 3
        && PlanetLandforms.profile(planet).equals("temperate")
        && planet.hasRivers()
        && planet.atmosphere > 25
        && planet.temperature >= 250
        && planet.temperature <= 450
        && planet.ocean.equals(ResourceLocation.withDefaultNamespace("water"));
  }

  /** A bounded design rule in Earth-gravity units, not a sediment transport simulation. */
  public static double incision(double gravity) {
    return 3 + 5 * Math.sqrt(Math.clamp(gravity, .15, 4));
  }

  public record Sample(
      double channel,
      double banks,
      double wetness,
      double underground,
      double clearing,
      double sediment,
      double opening) {
    public double sinkholeShape() {
      return Math.max(underground / .035 - 1, (.62 - opening) * 8);
    }

    public boolean river() {
      return channel < .035;
    }

    public boolean floodplain() {
      return !river() && banks > .25 && wetness > .35;
    }

    public boolean woodedBank() {
      return !river() && banks > .08;
    }

    public double caveShape(int sea, double y) {
      double horizontal = underground / .065;
      double vertical = (y - (waterLevel(sea) + 4)) / 8;
      return horizontal * horizontal + vertical * vertical - 1;
    }
  }

  public static int waterLevel(int sea) {
    return sea - 26;
  }

  public static Sample sample(long seed, double gravity, double x, double z) {
    double warp = 55 * PlanetLandforms.field(seed, 19, x, z, 310);
    double main = PlanetLandforms.field(seed, 23, x + warp, z - warp, 240);
    // Tributaries fade into hills away from the main valley instead of cutting every contour.
    double branch = Math.abs(PlanetLandforms.field(seed, 211, x - warp, z + warp, 125));
    double channel = Math.min(Math.abs(main), branch + Math.max(0, Math.abs(main) - .09) * .65);
    double bankWidth = .24 / (.7 + .3 * Math.sqrt(Math.clamp(gravity, .15, 4)));
    double banks = 1 - PlanetLandforms.smooth(Math.clamp((channel - .035) / bankWidth, 0, 1));
    double wetness =
        Math.clamp(.35 + PlanetLandforms.moisture(seed, x, z) * .55 + banks * .4, 0, 1);
    double underground = Math.abs(main + .12 + .025 * PlanetLandforms.field(seed, 223, x, z, 150));
    return new Sample(
        channel,
        banks,
        wetness,
        underground,
        PlanetLandforms.field(seed, 151, x, z, 100),
        PlanetLandforms.field(seed, 227, x, z, 38),
        PlanetLandforms.field(seed, 229, x, z, 85));
  }

  /** Wet lowlands taper into uplands and leave the main channel's gravity incision intact. */
  public static double wetlandStrength(long seed, int sea, double x, double z, Sample sample) {
    return wetlandStrength(PlanetLandforms.height(seed, "temperate", sea, false, x, z), sea, sample);
  }

  private static double wetlandStrength(double original, int sea, Sample sample) {
    double lowland = 1 - PlanetLandforms.smooth(Math.clamp((original - sea - 4) / 12, 0, 1));
    double inland = PlanetLandforms.smooth(Math.clamp((original - sea + 2) / 4, 0, 1));
    double moisture = PlanetLandforms.smooth(Math.clamp((sample.wetness() - .5) / .22, 0, 1));
    double margin = PlanetLandforms.smooth(Math.clamp((sample.channel() - .035) / .035, 0, 1));
    return lowland * inland * moisture * margin * sample.banks();
  }

  public static double height(long seed, int sea, double gravity, double x, double z) {
    return height(seed, sea, gravity, x, z, sample(seed, gravity, x, z));
  }

  private static double height(
      long seed, int sea, double gravity, double x, double z, Sample sample) {
    double original = PlanetLandforms.height(seed, "temperate", sea, false, x, z);
    double bed = sea - incision(gravity) + sample.sediment() * .8;
    double edge = PlanetLandforms.smooth(Math.clamp(sample.channel() / .035, 0, 1));
    double channelTop = PlanetLandforms.mix(bed, sea - 1, edge);
    double bank = PlanetLandforms.smooth(Math.clamp((sample.channel() - .035) / .075, 0, 1));
    double floodplain = sea + 1.5 + sample.sediment() * 1.2;
    double valley = PlanetLandforms.mix(channelTop, floodplain, bank);
    double result = PlanetLandforms.mix(original, Math.min(original, valley), sample.banks());
    double wetland = wetlandStrength(original, sea, sample);
    if (wetland > 0) {
      double pools = PlanetLandforms.field(seed, 251, x, z, 24);
      double islands = PlanetLandforms.field(seed, 257, x, z, 9);
      double marsh = sea - 1.6 + pools * 4 + islands * .7;
      result = PlanetLandforms.mix(result, marsh, wetland);
    }
    // Keep the entire mapped river submerged, including the quart-biome sampling margin.
    if (sample.river()) result = Math.min(original, channelTop);
    return Math.clamp(result, -40, 280);
  }

  public static double density(
      long seed, int sea, double gravity, boolean caves, double x, double y, double z) {
    Sample sample = sample(seed, gravity, x, z);
    double top = height(seed, sea, gravity, x, z, sample);
    double warp = PlanetLandforms.noise(seed + 73, x / 65, y / 43, z / 65);
    double rock =
        (top - y) / 22
            + warp * .42 * Math.min(1, Math.abs(top - sea) / 35) * (1 - sample.banks() * .9);
    if (caves && y > -55 && y < top - 7) {
      double rooms = PlanetLandforms.noise(seed + 101, x / 59, y / 31, z / 59);
      double tunnels =
          Math.max(
              Math.abs(PlanetLandforms.noise(seed + 109, x / 78, y / 39, z / 78)),
              Math.abs(PlanetLandforms.noise(seed + 113, x / 78, y / 53, z / 78)));
      rock = Math.min(rock, Math.min((.49 - rooms) * 3, (tunnels - .085) * 5));
      if (y < top - 12 && PlanetLandforms.continents(seed, x, z) > -.35)
        rock = Math.min(rock, sample.caveShape(sea, y));
    }
    if (caves && top > sea + 3 && top < sea + 35 && y > waterLevel(sea) + 4 && y < top + 3) {
      rock = Math.min(rock, sample.sinkholeShape());
    }
    return Math.clamp(Math.max(rock, (-58 - y) / 3), -32, 32);
  }

  /** Chunk-local finishing before vegetation: keep water in the channel and dry walking shelves. */
  public static void finish(ChunkAccess chunk, Planet planet, long seed) {
    if (!enabled(planet)) return;
    int sea = planet.seaLevel, water = waterLevel(sea);
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int dx = 0; dx < 16; dx++)
      for (int dz = 0; dz < 16; dz++) {
        int x = chunk.getPos().getMinBlockX() + dx, z = chunk.getPos().getMinBlockZ() + dz;
        Sample sample = sample(seed, planet.gravity, x, z);
        double top = height(seed, sea, planet.gravity, x, z);
        if (sample.banks() > .35) {
          int floor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);
          if (floor > sea - 20 && floor < sea + 6) {
            pos.set(x, floor, z);
            BlockState current = chunk.getBlockState(pos);
            if (current.is(BlockTags.DIRT)
                || current.is(Blocks.STONE)
                || current.is(Blocks.GRAVEL)
                || current.is(Blocks.MUD)
                || current.is(BiomeMaterials.block("alluvial_silt"))) {
              double slope =
                  Math.max(
                      Math.abs(height(seed, sea, planet.gravity, x + 2, z) - top),
                      Math.abs(height(seed, sea, planet.gravity, x, z + 2) - top));
              double patch = PlanetLandforms.field(seed, 239, x, z, 13);
              BlockState surface;
              if (slope > 2.5 || sample.sediment() > .45 && patch < .15)
                surface = Blocks.GRAVEL.defaultBlockState();
              else if (wetlandStrength(seed, sea, x, z, sample) > .3 && floor < sea + 2)
                surface =
                    floor < sea || patch < .1
                        ? Blocks.MUD.defaultBlockState()
                        : BiomeMaterials.block("rooted_loam").defaultBlockState();
              else if (floor >= sea && sample.wetness() > .55 && patch > -.25)
                surface = BiomeMaterials.block("rooted_loam").defaultBlockState();
              else surface = BiomeMaterials.block("alluvial_silt").defaultBlockState();
              chunk.setBlockState(pos, surface, false);
            }
          }
        }
        if (top < sea && PlanetLandforms.continents(seed, x, z) >= -.35) {
          int bed = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);
          for (int y = Math.max(bed + 1, chunk.getMinBuildHeight());
              y < Math.min(sea, chunk.getMaxBuildHeight());
              y++) {
            pos.set(x, y, z);
            if (chunk.getBlockState(pos).isAir()) {
              chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
              chunk.markPosForPostprocessing(pos);
            }
          }
        }
        if (!planet.caves
            || top < water + 18
            || sample.underground() >= .065
            || PlanetLandforms.continents(seed, x, z) <= -.35) continue;
        int floor = Integer.MAX_VALUE;
        boolean opening = sample.sinkholeShape() < -.08 && top > sea + 3 && top < sea + 35;
        for (int y = Math.max(chunk.getMinBuildHeight() + 6, water - 4);
            y
                <= Math.min(
                    chunk.getMaxBuildHeight() - 1,
                    opening ? top + 2 : Math.min(top - 13, water + 12));
            y++) {
          if (sample.caveShape(sea, y) >= -.08 && !(opening && y > water + 4)) continue;
          pos.set(x, y, z);
          if (chunk.getBlockState(pos).is(Blocks.BEDROCK)) continue;
          floor = Math.min(floor, y - 1);
          chunk.setBlockState(
              pos,
              y <= water ? Blocks.WATER.defaultBlockState() : Blocks.CAVE_AIR.defaultBlockState(),
              false);
          if (y <= water) chunk.markPosForPostprocessing(pos);
        }
        if (floor != Integer.MAX_VALUE) {
          pos.set(x, floor, z);
          if (!chunk.getBlockState(pos).is(Blocks.BEDROCK))
            chunk.setBlockState(
                pos,
                BiomeMaterials.block(floor < water ? "alluvial_silt" : "rooted_loam")
                    .defaultBlockState(),
                false);
        }
      }
  }
}

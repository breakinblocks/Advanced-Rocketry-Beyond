package advRocketry.space;

import advRocketry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Small exposed resource pockets, restricted to tagged habitats, substrates and the current chunk.
 */
final class PlanetHabitatDeposits {
  private PlanetHabitatDeposits() {}

  static void generate(ChunkAccess chunk, Planet planet, long seed) {
    if (planet.generationVersion < 9 || planet.terrain == TerrainType.SPACE) return;
    RandomSource random =
        RandomSource.create(
            PlanetSeeds.column(seed ^ 0x48AB17D3L, chunk.getPos().x, chunk.getPos().z));
    // Four narrow probes per chunk keep cave searches bounded without favoring surface biomes.
    for (int attempt = 0; attempt < 4; attempt++) {
      int x = chunk.getPos().getMinBlockX() + 2 + random.nextInt(12);
      int z = chunk.getPos().getMinBlockZ() + 2 + random.nextInt(12);
      for (int y = chunk.getMaxBuildHeight() - 2; y > chunk.getMinBuildHeight(); y--) {
        BlockPos center = new BlockPos(x, y, z);
        if (chunk.getBlockState(center).isAir() || !exposed(chunk, center)) continue;
        BlockState reward = reward(chunk, center);
        if (reward == null || random.nextInt(6) != 0) continue;
        for (int dx = -1; dx <= 1; dx++)
          for (int dz = -1; dz <= 1; dz++) {
            if (Math.abs(dx) + Math.abs(dz) == 2 && random.nextBoolean()) continue;
            BlockPos pos = center.offset(dx, 0, dz);
            if (exposed(chunk, pos) && reward.equals(reward(chunk, pos)))
              chunk.setBlockState(pos, reward, false);
          }
        break; // At most one small deposit per probe.
      }
    }
  }

  private static boolean exposed(ChunkAccess chunk, BlockPos pos) {
    BlockState above = chunk.getBlockState(pos.above());
    return above.isAir() || above.is(Blocks.WATER);
  }

  private static BlockState reward(ChunkAccess chunk, BlockPos pos) {
    var biome = chunk.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
    BlockState host = chunk.getBlockState(pos);
    // Explicit priority makes overlapping datapack tags deterministic.
    if (biome.is(ModTags.CLAY_DEPOSIT_BIOMES) && host.is(ModTags.CLAY_DEPOSIT_HOSTS))
      return Blocks.CLAY.defaultBlockState();
    if (biome.is(ModTags.CALCITE_DEPOSIT_BIOMES) && host.is(ModTags.CALCITE_DEPOSIT_HOSTS))
      return Blocks.CALCITE.defaultBlockState();
    if (biome.is(ModTags.GLASS_DEPOSIT_BIOMES) && host.is(ModTags.GLASS_DEPOSIT_HOSTS))
      return Blocks.GLASS.defaultBlockState();
    if (biome.is(ModTags.MAGMA_DEPOSIT_BIOMES) && host.is(ModTags.MAGMA_DEPOSIT_HOSTS))
      return Blocks.MAGMA_BLOCK.defaultBlockState();
    return null;
  }
}

package advRocketry.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class PlanetReservoirs {
    private static final long SALT = 0x6F11D3A5C2E8B907L;

    private PlanetReservoirs() {}

    public record Entry(
            ResourceLocation block,
            float chance,
            int spacing,
            int minRadius,
            int maxRadius,
            int minDepth,
            int maxDepth,
            int minTop,
            int maxTop) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.<Entry>create(instance -> instance.group(
                                ResourceLocation.CODEC.fieldOf("block").forGetter(Entry::block),
                                Codec.floatRange(0, 1)
                                        .optionalFieldOf("chance", .5f)
                                        .forGetter(Entry::chance),
                                Codec.intRange(4, 64)
                                        .optionalFieldOf("spacing", 10)
                                        .forGetter(Entry::spacing),
                                Codec.intRange(4, 48)
                                        .optionalFieldOf("min_radius", 26)
                                        .forGetter(Entry::minRadius),
                                Codec.intRange(4, 48)
                                        .optionalFieldOf("max_radius", 32)
                                        .forGetter(Entry::maxRadius),
                                Codec.intRange(2, 32)
                                        .optionalFieldOf("min_depth", 8)
                                        .forGetter(Entry::minDepth),
                                Codec.intRange(2, 32)
                                        .optionalFieldOf("max_depth", 10)
                                        .forGetter(Entry::maxDepth),
                                Codec.intRange(-60, 300)
                                        .optionalFieldOf("min_top", 0)
                                        .forGetter(Entry::minTop),
                                Codec.intRange(-60, 300)
                                        .optionalFieldOf("max_top", 30)
                                        .forGetter(Entry::maxTop))
                        .apply(instance, Entry::new))
                .validate(entry -> entry.minRadius > entry.maxRadius
                                || entry.minDepth > entry.maxDepth
                                || entry.minTop > entry.maxTop
                        ? DataResult.error(() -> "Reservoir minimums must not exceed maximums")
                        : entry.maxRadius * 2 >= entry.spacing * 16
                                ? DataResult.error(() -> "Reservoir radius must fit inside its spacing")
                                : DataResult.success(entry));
    }

    private record Pocket(int x, int z, int top, int radiusX, int radiusZ, int depth) {
        boolean contains(int px, int py, int pz) {
            double dx = (px - x) / (double) radiusX;
            double dz = (pz - z) / (double) radiusZ;
            double edge = dx * dx + dz * dz;
            if (edge >= 1 || py > top) return false;
            return py > top - depth * Math.sqrt(1 - edge);
        }
    }

    static void generate(ChunkAccess chunk, Planet planet, long seed) {
        if (planet.reservoirs.isEmpty() || planet.terrain == TerrainType.SPACE) return;
        BlockState shell = BuiltInRegistries.BLOCK.get(planet.filler).defaultBlockState();
        for (int index = 0; index < planet.reservoirs.size(); index++) {
            Entry entry = planet.reservoirs.get(index);
            BlockState fluid = BuiltInRegistries.BLOCK.get(entry.block()).defaultBlockState();
            if (fluid.isAir()) continue;
            for (Pocket pocket : pockets(chunk, entry, seed ^ SALT * (index + 1))) fill(chunk, pocket, fluid, shell);
        }
    }

    private static List<Pocket> pockets(ChunkAccess chunk, Entry entry, long seed) {
        int cellSize = entry.spacing() * 16;
        int minX = chunk.getPos().getMinBlockX() - entry.maxRadius() - 1;
        int maxX = chunk.getPos().getMaxBlockX() + entry.maxRadius() + 1;
        int minZ = chunk.getPos().getMinBlockZ() - entry.maxRadius() - 1;
        int maxZ = chunk.getPos().getMaxBlockZ() + entry.maxRadius() + 1;
        List<Pocket> pockets = new ArrayList<>();
        for (int cellX = Math.floorDiv(minX, cellSize); cellX <= Math.floorDiv(maxX, cellSize); cellX++)
            for (int cellZ = Math.floorDiv(minZ, cellSize); cellZ <= Math.floorDiv(maxZ, cellSize); cellZ++) {
                RandomSource random = RandomSource.create(PlanetSeeds.column(seed, cellX, cellZ));
                if (random.nextFloat() >= entry.chance()) continue;
                int radiusX = Mth.nextInt(random, entry.minRadius(), entry.maxRadius());
                int radiusZ = Mth.nextInt(random, entry.minRadius(), entry.maxRadius());
                int margin = Math.max(radiusX, radiusZ) + 1;
                int x = cellX * cellSize + Mth.nextInt(random, margin, cellSize - margin);
                int z = cellZ * cellSize + Mth.nextInt(random, margin, cellSize - margin);
                int top = Mth.nextInt(random, entry.minTop(), entry.maxTop());
                int depth = Mth.nextInt(random, entry.minDepth(), entry.maxDepth());
                pockets.add(new Pocket(x, z, top, radiusX, radiusZ, depth));
            }
        return pockets;
    }

    private static void fill(ChunkAccess chunk, Pocket pocket, BlockState fluid, BlockState shell) {
        int startX = Math.max(chunk.getPos().getMinBlockX(), pocket.x() - pocket.radiusX() - 1);
        int endX = Math.min(chunk.getPos().getMaxBlockX(), pocket.x() + pocket.radiusX() + 1);
        int startZ = Math.max(chunk.getPos().getMinBlockZ(), pocket.z() - pocket.radiusZ() - 1);
        int endZ = Math.min(chunk.getPos().getMaxBlockZ(), pocket.z() + pocket.radiusZ() + 1);
        if (startX > endX || startZ > endZ) return;
        int bottom = Math.max(chunk.getMinBuildHeight() + 1, pocket.top() - pocket.depth() - 1);
        int roof = Math.min(chunk.getMaxBuildHeight() - 1, pocket.top() + 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = startX; x <= endX; x++)
            for (int z = startZ; z <= endZ; z++)
                for (int y = bottom; y <= roof; y++) {
                    pos.set(x, y, z);
                    if (pocket.contains(x, y, z)) {
                        chunk.setBlockState(pos, fluid, false);
                    } else if (borders(pocket, x, y, z) && open(chunk.getBlockState(pos))) {
                        chunk.setBlockState(pos, shell, false);
                    }
                }
    }

    private static boolean borders(Pocket pocket, int x, int y, int z) {
        for (Direction side : Direction.values())
            if (pocket.contains(x + side.getStepX(), y + side.getStepY(), z + side.getStepZ())) return true;
        return false;
    }

    private static boolean open(BlockState state) {
        return !state.isSolid() && !state.is(Blocks.BEDROCK);
    }
}

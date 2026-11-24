// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import advRocketry.transport.TransportBlock;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Bounded original vent flood-fill: unloaded chunks and open boundaries are leaks. */
public final class SealedRooms {
    private static final Map<Level, Map<BlockPos, OxygenBlockEntity>> VENTS = new WeakHashMap<>();
    private static final Set<Holder<GameEvent>> BLOCK_EVENTS = Set.of(
            GameEvent.BLOCK_OPEN,
            GameEvent.BLOCK_CLOSE,
            GameEvent.BLOCK_CHANGE,
            GameEvent.BLOCK_PLACE,
            GameEvent.BLOCK_DESTROY,
            GameEvent.FLUID_PLACE,
            GameEvent.FLUID_PICKUP);
    private static final Direction[] DIRECTIONS = Direction.values();

    private SealedRooms() {}

    public record Scan(LongOpenHashSet room, BoundingBox explored, BlockPos escape, BlockPos gap, int visited) {
        public boolean leaked() {
            return escape != null;
        }

        public BlockPos leak() {
            return gap != null ? gap : escape;
        }
    }

    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) VENTS.remove(event.getLevel());
    }

    public static void register(OxygenBlockEntity vent) {
        if (!(vent.getLevel() instanceof ServerLevel)) return;
        VENTS.computeIfAbsent(vent.getLevel(), level -> new HashMap<>()).put(vent.getBlockPos(), vent);
    }

    public static void remove(OxygenBlockEntity vent) {
        if (!(vent.getLevel() instanceof ServerLevel)) return;
        var vents = VENTS.get(vent.getLevel());
        if (vents != null) vents.remove(vent.getBlockPos());
    }

    public static boolean breathable(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) return false;
        var vents = VENTS.get(level);
        if (vents == null) return false;
        long key = pos.asLong();
        for (OxygenBlockEntity vent : vents.values()) if (!vent.isRemoved() && vent.supplies(pos, key)) return true;
        return false;
    }

    public static void neighborChanged(BlockEvent.NeighborNotifyEvent event) {
        changed(event.getLevel(), event.getPos());
    }

    public static void gameEvent(VanillaGameEvent event) {
        if (BLOCK_EVENTS.contains(event.getVanillaEvent()))
            changed(event.getLevel(), BlockPos.containing(event.getEventPosition()));
    }

    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ChunkPos chunk = event.getChunk().getPos();
        level.getServer().execute(() -> {
            var vents = VENTS.get(level);
            if (vents != null) for (OxygenBlockEntity vent : vents.values()) vent.chunkLoaded(chunk);
        });
    }

    private static void changed(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide()) return;
        var vents = VENTS.get(level);
        if (vents != null) for (OxygenBlockEntity vent : vents.values()) vent.blockChanged(pos);
    }

    public static boolean seals(Level level, BlockPos pos) {
        return new Reader(level).seals(pos.mutable(), level.getBlockState(pos));
    }

    public static Set<BlockPos> scan(Level level, BlockPos origin) {
        return scan(level, origin, origin);
    }

    public static Set<BlockPos> scan(Level level, BlockPos origin, BlockPos center) {
        return positions(scan(level, origin, center, new LongOpenHashSet(), new LongOpenHashSet())
                .room());
    }

    static Set<BlockPos> scan(Level level, BlockPos origin, BlockPos center, int radius, boolean volumeBased) {
        return positions(scan(level, origin, center, radius, volumeBased, new LongOpenHashSet(), new LongOpenHashSet())
                .room());
    }

    static Set<BlockPos> positions(LongSet keys) {
        Set<BlockPos> result = new HashSet<>(keys.size());
        for (long key : keys) result.add(BlockPos.of(key));
        return result;
    }

    static Scan scan(Level level, BlockPos origin, BlockPos center, LongSet seen, LongSet leaked) {
        return scan(
                level,
                origin,
                center,
                AdvancedRocketryConfig.oxygenVentSize(),
                AdvancedRocketryConfig.oxygenVentVolumeBased(),
                seen,
                leaked);
    }

    private static Scan scan(
            Level level,
            BlockPos origin,
            BlockPos center,
            int radius,
            boolean volumeBased,
            LongSet seen,
            LongSet leaked) {
        long start = origin.asLong();
        if (leaked.contains(start)) return new Scan(new LongOpenHashSet(), null, origin, null, 0);
        if (!seen.add(start)) return new Scan(new LongOpenHashSet(), null, null, null, 0);
        int maximumVolume = (int) (4d / 3d * Math.PI * radius * radius * radius);
        double radiusSquared = (double) radius * radius;
        Reader reader = new Reader(level);
        LongOpenHashSet room = new LongOpenHashSet();
        Long2LongOpenHashMap parent = new Long2LongOpenHashMap();
        LongArrayFIFOQueue pending = new LongArrayFIFOQueue();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        pending.enqueue(start);
        int minX = origin.getX(), minY = origin.getY(), minZ = origin.getZ();
        int maxX = minX, maxY = minY, maxZ = minZ;
        int visited = 0;
        while (!pending.isEmpty()) {
            long key = pending.dequeueLong();
            visited++;
            cursor.set(key);
            int x = cursor.getX(), y = cursor.getY(), z = cursor.getZ();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
            LevelChunk chunk = level.isInWorldBounds(cursor) ? reader.chunk(x >> 4, z >> 4) : null;
            boolean escaped = chunk == null;
            if (!escaped) {
                BlockState state = chunk.getBlockState(cursor);
                if (reader.seals(cursor, state)) continue;
                escaped = !volumeBased && cursor.distSqr(center) > radiusSquared || reader.openAbove(chunk, x, y, z);
            }
            if (!escaped) {
                room.add(key);
                escaped = volumeBased && room.size() > maximumVolume;
            }
            if (escaped)
                return leaked(
                        reader,
                        room,
                        leaked,
                        parent,
                        start,
                        key,
                        new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ),
                        visited);
            for (Direction direction : DIRECTIONS) {
                long neighbor = BlockPos.offset(key, direction);
                if (leaked.contains(neighbor)) {
                    parent.put(neighbor, key);
                    return leaked(
                            reader,
                            room,
                            leaked,
                            parent,
                            start,
                            neighbor,
                            new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ),
                            visited);
                }
                if (seen.add(neighbor)) {
                    parent.put(neighbor, key);
                    pending.enqueue(neighbor);
                }
            }
        }
        return new Scan(room, new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ), null, null, visited);
    }

    private static Scan leaked(
            Reader reader,
            LongOpenHashSet room,
            LongSet leaked,
            Long2LongOpenHashMap parent,
            long start,
            long escape,
            BoundingBox explored,
            int visited) {
        leaked.addAll(room);
        leaked.add(escape);
        LongArrayList path = new LongArrayList();
        long step = escape;
        while (true) {
            path.add(step);
            if (step == start || !parent.containsKey(step)) break;
            step = parent.get(step);
        }
        return new Scan(new LongOpenHashSet(), explored, BlockPos.of(escape), gap(reader, path), visited);
    }

    private static BlockPos gap(Reader reader, LongArrayList path) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos side = new BlockPos.MutableBlockPos();
        long edge = Long.MIN_VALUE;
        for (int index = 0; index < path.size(); index++) {
            long key = path.getLong(index);
            cursor.set(key);
            Direction.Axis axis = index + 1 < path.size()
                    ? axis(key, path.getLong(index + 1))
                    : index > 0 ? axis(path.getLong(index - 1), key) : Direction.Axis.Y;
            int sealed = 0;
            for (Direction direction : DIRECTIONS) {
                if (direction.getAxis() == axis) continue;
                if (reader.seals(side.setWithOffset(cursor, direction))) sealed++;
            }
            if (sealed >= 3) return cursor.immutable();
            if (sealed >= 1 && edge == Long.MIN_VALUE) edge = key;
        }
        return edge == Long.MIN_VALUE ? null : BlockPos.of(edge);
    }

    private static Direction.Axis axis(long from, long to) {
        if (BlockPos.getX(from) != BlockPos.getX(to)) return Direction.Axis.X;
        if (BlockPos.getY(from) != BlockPos.getY(to)) return Direction.Axis.Y;
        return Direction.Axis.Z;
    }

    private static final class Reader {
        private final Level level;
        private final Long2ObjectOpenHashMap<LevelChunk> chunks = new Long2ObjectOpenHashMap<>();
        private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        Reader(Level level) {
            this.level = level;
        }

        LevelChunk chunk(int chunkX, int chunkZ) {
            long key = ChunkPos.asLong(chunkX, chunkZ);
            LevelChunk chunk = chunks.get(key);
            if (chunk == null && !chunks.containsKey(key)) {
                chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                chunks.put(key, chunk);
            }
            return chunk;
        }

        BlockState state(BlockPos pos) {
            if (!level.isInWorldBounds(pos)) return null;
            LevelChunk chunk = chunk(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? null : chunk.getBlockState(pos);
        }

        boolean seals(BlockPos pos) {
            BlockState state = state(pos);
            return state != null && seals(pos, state);
        }

        boolean seals(BlockPos pos, BlockState state) {
            if (state.is(ModTags.UNSEALABLE)) return false;
            if (state.is(ModTags.SEALABLE)) return true;
            if (state.isCollisionShapeFullBlock(level, pos)) return true;
            if (state.is(LifeSupportRegistry.AIRLOCK.get())) return !state.getValue(BlockStateProperties.OPEN);
            if (!state.isAir() && !(state.getBlock() instanceof TransportBlock)) return false;
            return frame(pos, Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST)
                    || frame(pos, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH)
                    || frame(pos, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        }

        private boolean frame(BlockPos center, Direction... sides) {
            for (Direction side : sides) {
                BlockState state = state(probe.setWithOffset(center, side));
                if (state == null || !state.is(LifeSupportRegistry.PIPE_SEALER.get())) return false;
            }
            return true;
        }

        boolean openAbove(LevelChunk chunk, int x, int y, int z) {
            if (y <= chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15)) return false;
            BlockPos.MutableBlockPos column = new BlockPos.MutableBlockPos(x, y, z);
            int top = level.getMaxBuildHeight();
            for (int height = y + 1; height < top; height++) {
                LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(height));
                if (section.hasOnlyAir()) {
                    height |= 15;
                    continue;
                }
                BlockState state = section.getBlockState(x & 15, height & 15, z & 15);
                if (seals(column.setY(height), state)) return false;
            }
            return true;
        }
    }
}

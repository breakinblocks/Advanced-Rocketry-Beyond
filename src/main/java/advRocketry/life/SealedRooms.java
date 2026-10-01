// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Bounded original vent flood-fill: unloaded chunks and open boundaries are leaks. */
public final class SealedRooms {
  private static final Map<Level, Map<BlockPos, OxygenBlockEntity>> VENTS = new WeakHashMap<>();
  private static final Set<Holder<GameEvent>> BLOCK_EVENTS =
      Set.of(
          GameEvent.BLOCK_OPEN,
          GameEvent.BLOCK_CLOSE,
          GameEvent.BLOCK_CHANGE,
          GameEvent.BLOCK_PLACE,
          GameEvent.BLOCK_DESTROY,
          GameEvent.FLUID_PLACE,
          GameEvent.FLUID_PICKUP);

  private SealedRooms() {}

  public static void unload(LevelEvent.Unload event) {
    VENTS.remove(event.getLevel());
  }

  public static void register(OxygenBlockEntity vent) {
    VENTS.computeIfAbsent(vent.getLevel(), level -> new HashMap<>()).put(vent.getBlockPos(), vent);
  }

  public static void remove(OxygenBlockEntity vent) {
    var vents = VENTS.get(vent.getLevel());
    if (vents != null) vents.remove(vent.getBlockPos());
  }

  public static boolean breathable(Level level, BlockPos pos) {
    var vents = VENTS.get(level);
    if (vents == null) return false;
    for (OxygenBlockEntity vent : vents.values())
      if (!vent.isRemoved() && vent.supplies(pos)) return true;
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
    var vents = VENTS.get(event.getLevel());
    if (vents == null) return;
    ChunkPos chunk = event.getChunk().getPos();
    for (OxygenBlockEntity vent : vents.values()) vent.chunkLoaded(chunk);
  }

  private static void changed(LevelAccessor level, BlockPos pos) {
    if (level.isClientSide()) return;
    var vents = VENTS.get(level);
    if (vents != null) for (OxygenBlockEntity vent : vents.values()) vent.blockChanged(pos);
  }

  public static boolean seals(Level level, BlockPos pos) {
    var state = level.getBlockState(pos);
    if (state.is(ModTags.UNSEALABLE)) return false;
    if (state.is(ModTags.SEALABLE)) return true;
    return state.isCollisionShapeFullBlock(level, pos)
        || PipeSealerLogic.seals(level, pos)
        || state.is(LifeSupportRegistry.AIRLOCK.get())
            && !state.getValue(BlockStateProperties.OPEN);
  }

  public static Set<BlockPos> scan(Level level, BlockPos origin) {
    return scan(level, origin, origin);
  }

  public static Set<BlockPos> scan(Level level, BlockPos origin, BlockPos center) {
    return scan(level, origin, center, new HashSet<>());
  }

  static Set<BlockPos> scan(Level level, BlockPos origin, BlockPos center, Set<BlockPos> seen) {
    return scan(
        level,
        origin,
        center,
        AdvancedRocketryConfig.oxygenVentSize(),
        AdvancedRocketryConfig.oxygenVentVolumeBased(),
        seen);
  }

  static Set<BlockPos> scan(
      Level level, BlockPos origin, BlockPos center, int radius, boolean volumeBased) {
    return scan(level, origin, center, radius, volumeBased, new HashSet<>());
  }

  private static Set<BlockPos> scan(
      Level level,
      BlockPos origin,
      BlockPos center,
      int radius,
      boolean volumeBased,
      Set<BlockPos> seen) {
    int maximumVolume = (int) (4d / 3d * Math.PI * radius * radius * radius);
    Set<BlockPos> room = new HashSet<>();
    ArrayDeque<BlockPos> pending = new ArrayDeque<>();
    pending.add(origin);
    seen.add(origin);
    while (!pending.isEmpty()) {
      BlockPos pos = pending.removeFirst();
      if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) return Set.of();
      if (seals(level, pos)) continue;
      if (!volumeBased && pos.distSqr(center) > (double) radius * radius) return Set.of();
      room.add(pos);
      if (volumeBased && room.size() > maximumVolume) return Set.of();
      for (Direction direction : Direction.values()) {
        BlockPos neighbor = pos.relative(direction);
        if (seen.add(neighbor)) pending.add(neighbor);
      }
    }
    return room;
  }
}

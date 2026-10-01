// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.life.SealedRooms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

/** Original breathable-bed and optional planet-respawn policy. */
public final class PlanetRespawn {
  private PlanetRespawn() {}

  static boolean allowed(ServerLevel level, Planet planet, BlockPos position) {
    return AdvancedRocketryConfig.allowPlanetRespawn() && safeAtmosphere(level, planet, position);
  }

  private static boolean safeAtmosphere(ServerLevel level, Planet planet, BlockPos position) {
    return AdvancedRocketryConfig.forcePlanetRespawn()
        || planet.breathable()
        || SealedRooms.breathable(level, position);
  }

  public static void sleep(CanPlayerSleepEvent event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    Planet planet = GalaxyData.get(level.getServer()).byDimension(level.dimension().location());
    if (planet == null || planet.id == GalaxyData.EARTH_ID) return;
    if (!safeAtmosphere(level, planet, event.getPos())) {
      event.setProblem(BedSleepingProblem.NOT_POSSIBLE_HERE);
      return;
    }
    // Non-natural native dimensions reject sleep before checking the bed itself.
    // Restore the original provider's permission while retaining physical sleep checks.
    if (level.dimensionType().natural()
        || event.getProblem() != BedSleepingProblem.NOT_POSSIBLE_HERE) return;
    var player = event.getEntity();
    BlockPos head = event.getPos();
    BlockPos foot =
        event.getState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? head.relative(
                event.getState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite())
            : head;
    if (!player.isAlive() || player.isSleeping())
      event.setProblem(BedSleepingProblem.OTHER_PROBLEM);
    else if (!player.canInteractWithBlock(head, 0) && !player.canInteractWithBlock(foot, 0))
      event.setProblem(BedSleepingProblem.TOO_FAR_AWAY);
    else if (!level.noCollision(new AABB(head.above()))
        || !level.noCollision(new AABB(foot.above())))
      event.setProblem(BedSleepingProblem.OBSTRUCTED);
    else {
      player.setRespawnPosition(level.dimension(), head, player.getYRot(), false, true);
      if (level.isDay()) event.setProblem(BedSleepingProblem.NOT_POSSIBLE_NOW);
      else if (!player.isCreative()
          && !level
              .getEntitiesOfClass(
                  Monster.class,
                  new AABB(head).inflate(8, 5, 8),
                  monster -> monster.isPreventingPlayerRest(player))
              .isEmpty()) event.setProblem(BedSleepingProblem.NOT_SAFE);
      else event.setProblem(null);
    }
  }

  public static void setSpawn(PlayerSetSpawnEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)
        || event.isForced()
        || event.getNewSpawn() == null) return;
    ServerLevel level = player.server.getLevel(event.getSpawnLevel());
    if (level == null) return;
    Planet planet = GalaxyData.get(level.getServer()).byDimension(level.dimension().location());
    if (planet != null
        && planet.id != GalaxyData.EARTH_ID
        && !allowed(level, planet, event.getNewSpawn())) event.setCanceled(true);
  }

  public static void respawn(PlayerRespawnPositionEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;
    if (player.isRespawnForced() || player.getRespawnPosition() == null) return;
    ServerLevel level = player.server.getLevel(player.getRespawnDimension());
    if (level == null) return;
    Planet planet = GalaxyData.get(player.server).byDimension(level.dimension().location());
    if (planet == null
        || planet.id == GalaxyData.EARTH_ID
        || allowed(level, planet, player.getRespawnPosition())) return;
    ServerLevel overworld = player.server.overworld();
    event.setDimensionTransition(
        new DimensionTransition(
            overworld,
            Vec3.atBottomCenterOf(overworld.getSharedSpawnPos()),
            Vec3.ZERO,
            0,
            0,
            DimensionTransition.DO_NOTHING));
    event.setCopyOriginalSpawnPosition(false);
  }
}

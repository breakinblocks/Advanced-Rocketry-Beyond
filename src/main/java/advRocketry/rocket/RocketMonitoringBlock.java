// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Redstone launch controller and rocket telemetry display. */
public final class RocketMonitoringBlock extends RocketInfrastructureBlock {
  public RocketMonitoringBlock(Properties properties) {
    super(properties, RocketRegistry.MONITORING_STATION_ENTITY);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new RocketMonitoringBlockEntity(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof RocketMonitoringBlockEntity monitor)
      serverPlayer.openMenu(monitor, buffer -> buffer.writeBlockPos(pos));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof RocketMonitoringBlockEntity monitor
        ? monitor.comparatorSignal()
        : 0;
  }
}

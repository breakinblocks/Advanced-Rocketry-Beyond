// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.Mission;
import advRocketry.space.GalaxyData;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Original persistent rocket/mission monitor and launch control. */
public final class RocketMonitoringBlockEntity extends BlockEntity
    implements MenuProvider, RocketInfrastructure {
  private final RocketInfrastructureLink link = new RocketInfrastructureLink(this, 300000);
  private long missionId = -1;
  private RocketEntity activeRocket;
  private Mission activeMission;
  private InfrastructureRedstone.Mode mode = InfrastructureRedstone.Mode.ON;
  private boolean signal;
  private int comparator;

  public RocketMonitoringBlockEntity(BlockPos pos, BlockState state) {
    super(RocketRegistry.MONITORING_STATION_ENTITY.get(), pos, state);
  }

  public void link(UUID id) {
    if (level instanceof ServerLevel server && server.getEntity(id) instanceof RocketEntity rocket)
      link(rocket);
  }

  @Override
  public boolean link(RocketEntity rocket) {
    if (!link.link(rocket)) return false;
    clearTracking();
    refresh();
    return true;
  }

  @Override
  public boolean linkBuilder(RocketBlockEntity builder) {
    if (!link.linkBuilder(builder)) return false;
    clearTracking();
    refresh();
    return true;
  }

  @Override
  public void unlink() {
    link.unlink();
    clearTracking();
  }

  private void clearTracking() {
    missionId = -1;
    activeRocket = null;
    activeMission = null;
    setChanged();
  }

  public UUID linkedRocket() {
    return link.rocketId();
  }

  public void refresh() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    activeRocket = null;
    activeMission = null;
    var server = serverLevel.getServer();
    UUID rocketId = link.rocketId();
    if (rocketId != null) {
      RocketEntity loaded = RocketEntity.loaded(server, rocketId);
      if (loaded != null) {
        activeRocket = loaded;
        missionId = -1;
        return;
      }
      var missions = GalaxyData.get(server).miningMissions;
      var saved = missions.get(missionId);
      if (saved != null && rocketId.equals(saved.sourceRocket())) {
        activeMission = saved;
        return;
      }
      for (var entry : missions.entrySet())
        if (rocketId.equals(entry.getValue().sourceRocket())) {
          missionId = entry.getKey();
          activeMission = entry.getValue();
          setChanged();
          return;
        }
      RocketEntity returning = RocketEntity.returning(server, rocketId);
      if (returning != null) {
        activeRocket = returning;
        link.follow(returning);
        missionId = -1;
        setChanged();
        return;
      }
    }
    activeRocket = link.builderRocket();
  }

  public boolean launch() {
    refresh();
    return activeRocket != null
        && activeRocket.level() == level
        && activeRocket.flight() == 0
        && activeRocket.launchFromMonitor();
  }

  public void cycleMode() {
    mode = InfrastructureRedstone.Mode.values()[(mode.ordinal() + 1) % 3];
    signal = false;
    setChanged();
  }

  public int value(int index) {
    return switch (index) {
      case 0 -> activeRocket == null ? 0 : (int) activeRocket.getY();
      case 1 -> activeRocket == null ? 0 : activeRocket.monitorVelocity();
      case 2 -> activeRocket == null ? 0 : activeRocket.monitorFuel();
      case 3 -> activeRocket == null ? 0 : activeRocket.monitorCapacity();
      case 4 -> activeRocket == null ? -1 : activeRocket.flight();
      case 5 ->
          activeMission == null
              ? 0
              : (int)
                  Math.min(Integer.MAX_VALUE, Math.max(0, activeMission.duration - elapsed()) / 20);
      case 6 ->
          activeMission == null
              ? 0
              : (int) Math.clamp(elapsed() * 1000d / Math.max(1, activeMission.duration), 0, 1000);
      case 7 -> activeMission == null ? 0 : activeMission.kind == Mission.Kind.GAS ? 2 : 1;
      case 8 -> mode.ordinal();
      default -> activeRocket != null || activeMission != null ? 1 : 0;
    };
  }

  private long elapsed() {
    return level.getServer().overworld().getGameTime() - activeMission.startTick;
  }

  public int comparatorSignal() {
    return comparator;
  }

  public void tick() {
    if (!(level instanceof ServerLevel)) return;
    if (level.getGameTime() % 20 == 0 || activeRocket != null && activeRocket.isRemoved())
      refresh();
    boolean powered = mode.active(level.hasNeighborSignal(worldPosition));
    if (powered && !signal) launch();
    if (powered != signal) {
      signal = powered;
      setChanged();
    }
    int next = activeRocket == null ? 0 : (int) (15 * activeRocket.relativeHeight());
    if (next != comparator) {
      comparator = next;
      level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    refresh();
    return new RocketMonitoringMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    link.save(tag);
    tag.putLong("mission", missionId);
    tag.putInt("redstone_mode", mode.ordinal());
    tag.putBoolean("signal", signal);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    link.load(tag);
    missionId = tag.contains("mission") ? tag.getLong("mission") : -1;
    mode = InfrastructureRedstone.Mode.values()[Math.clamp(tag.getInt("redstone_mode"), 0, 2)];
    signal = tag.getBoolean("signal");
    activeRocket = null;
    activeMission = null;
  }
}

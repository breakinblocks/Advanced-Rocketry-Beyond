// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

/** Planet-local day length and weather without changing the overworld clock. */
public final class PlanetLevelData extends DerivedLevelData {
  private final Planet planet;
  private final ServerLevelData base;
  private BlockPos spawn = new BlockPos(0, 80, 0);
  private float angle;

  public PlanetLevelData(WorldData world, Planet planet) {
    super(world, world.overworldData());
    this.planet = planet;
    base = world.overworldData();
  }

  @Override
  public long getDayTime() {
    return (long) (base.getDayTime() * 24000d / planet.rotationPeriod);
  }

  @Override
  public BlockPos getSpawnPos() {
    return spawn;
  }

  @Override
  public float getSpawnAngle() {
    return angle;
  }

  @Override
  public void setSpawn(BlockPos pos, float angle) {
    spawn = pos;
    this.angle = angle;
  }

  @Override
  public boolean isRaining() {
    return planet.breathable() && super.isRaining();
  }

  @Override
  public boolean isThundering() {
    return planet.atmosphere > 200 && super.isThundering();
  }
}

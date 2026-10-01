// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Client-visible target for the original orbital laser shaft. */
public final class LaserNodeEntity extends Entity {
  private long lastRefresh;

  public LaserNodeEntity(EntityType<? extends LaserNodeEntity> type, Level level) {
    super(type, level);
    noPhysics = true;
  }

  public void refresh(double x, double y, double z) {
    setPos(x, y, z);
    lastRefresh = level().getGameTime();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  @Override
  public boolean isPickable() {
    return false;
  }

  @Override
  public void tick() {
    super.tick();
    if (level().isClientSide) {
      if (tickCount % 2 == 0)
        for (int index = 0; index < 4; index++)
          level()
              .addParticle(
                  ParticleTypes.FIREWORK,
                  getX() + (random.nextDouble() - .5) * 2,
                  getY() + random.nextDouble() * 2,
                  getZ() + (random.nextDouble() - .5) * 2,
                  (random.nextDouble() - .5) * .2,
                  random.nextDouble() * .4,
                  (random.nextDouble() - .5) * .2);
    } else if (level().getGameTime() - lastRefresh > 40) discard();
  }
}

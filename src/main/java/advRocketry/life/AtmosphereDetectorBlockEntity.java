// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Original atmosphere classes inferred from native planet state and sealed rooms. */
public final class AtmosphereDetectorBlockEntity extends BlockEntity {
  public enum Target {
    AIR,
    PRESSURIZED_AIR,
    VACUUM,
    LOW_OXYGEN,
    HIGH_PRESSURE,
    SUPER_HIGH_PRESSURE,
    VERY_HOT,
    SUPERHEATED,
    NO_OXYGEN,
    HIGH_PRESSURE_NO_OXYGEN,
    SUPER_HIGH_PRESSURE_NO_OXYGEN,
    VERY_HOT_NO_OXYGEN,
    SUPERHEATED_NO_OXYGEN
  }

  private Target target = Target.AIR;

  public AtmosphereDetectorBlockEntity(BlockPos pos, BlockState state) {
    super(LifeSupportRegistry.DETECTOR_ENTITY.get(), pos, state);
  }

  public Target target() {
    return target;
  }

  public void cycle() {
    target = Target.values()[(target.ordinal() + 1) % Target.values().length];
    setChanged();
  }

  public static Target atmosphere(ServerLevel level, BlockPos pos) {
    if (SealedRooms.breathable(level, pos)) return Target.PRESSURIZED_AIR;
    return atmosphere(GalaxyData.get(level.getServer()).planet(level));
  }

  public static Target atmosphere(Planet planet) {
    int pressure = planet.atmosphere;
    if (pressure <= 25) return Target.VACUUM;
    if (planet.temperature >= 900)
      return planet.oxygen ? Target.SUPERHEATED : Target.SUPERHEATED_NO_OXYGEN;
    if (planet.temperature > 450)
      return planet.oxygen ? Target.VERY_HOT : Target.VERY_HOT_NO_OXYGEN;
    if (pressure > 800)
      return planet.oxygen ? Target.SUPER_HIGH_PRESSURE : Target.SUPER_HIGH_PRESSURE_NO_OXYGEN;
    if (pressure > 200)
      return planet.oxygen ? Target.HIGH_PRESSURE : Target.HIGH_PRESSURE_NO_OXYGEN;
    if (!planet.oxygen) return Target.NO_OXYGEN;
    return pressure <= 75 ? Target.LOW_OXYGEN : Target.AIR;
  }

  public void tick() {
    if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 10 != 0) return;
    sample(serverLevel);
  }

  void sample(ServerLevel serverLevel) {
    boolean detected = false;
    for (Direction direction : Direction.values()) {
      BlockPos next = worldPosition.relative(direction);
      if (!level.getBlockState(next).isCollisionShapeFullBlock(level, next)
          && atmosphere(serverLevel, next) == target) {
        detected = true;
        break;
      }
    }
    BlockState state = getBlockState();
    if (state.getValue(AtmosphereDetectorBlock.POWERED) != detected)
      serverLevel.setBlock(
          worldPosition, state.setValue(AtmosphereDetectorBlock.POWERED, detected), 3);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putString("target", target.name());
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    try {
      target = Target.valueOf(tag.getString("target"));
    } catch (IllegalArgumentException ignored) {
      target = Target.AIR;
    }
  }
}

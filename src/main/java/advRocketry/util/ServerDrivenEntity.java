package advRocketry.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Server-authoritative vehicles interpolate network updates, including for local passengers. */
public abstract class ServerDrivenEntity extends Entity {
  private int interpolationSteps;
  private double targetX, targetY, targetZ;
  private float targetYaw, targetPitch;

  protected ServerDrivenEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  /** Server-owned flight needs interpolation even when the local player is a passenger. */
  @Override
  public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
    if (!level().isClientSide || position().distanceToSqr(x, y, z) > 64 * 64) {
      interpolationSteps = 0;
      super.lerpTo(x, y, z, yaw, pitch, steps);
      setOldPosAndRot();
      return;
    }
    targetX = x;
    targetY = y;
    targetZ = z;
    targetYaw = yaw;
    targetPitch = pitch;
    interpolationSteps = Math.clamp(steps, 1, 5);
  }

  @Override
  public double lerpTargetX() {
    return interpolationSteps > 0 ? targetX : getX();
  }

  @Override
  public double lerpTargetY() {
    return interpolationSteps > 0 ? targetY : getY();
  }

  @Override
  public double lerpTargetZ() {
    return interpolationSteps > 0 ? targetZ : getZ();
  }

  @Override
  public float lerpTargetYRot() {
    return interpolationSteps > 0 ? targetYaw : getYRot();
  }

  @Override
  public float lerpTargetXRot() {
    return interpolationSteps > 0 ? targetPitch : getXRot();
  }

  @Override
  public void tick() {
    super.tick();
    if (level().isClientSide && interpolationSteps > 0) {
      lerpPositionAndRotationStep(
          interpolationSteps, targetX, targetY, targetZ, targetYaw, targetPitch);
      interpolationSteps--;
    }
  }
}

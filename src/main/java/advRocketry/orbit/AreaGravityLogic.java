// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.util.Texts;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Original cross-frame area gravity controller with smooth directional acceleration. */
public final class AreaGravityLogic {
  private AreaGravityLogic() {}

  public static boolean complete(OrbitalBlockEntity controller) {
    return AdvancedRocketryConfig.enableGravityController()
        && controller.areaGravityController()
        && PlacedMultiblock.complete(controller, "area_gravity_controller");
  }

  public static void adjust(OrbitalBlockEntity controller, int button) {
    if (!controller.areaGravityController()) return;
    switch (button) {
      case 9 -> controller.areaGravity = Math.max(0, controller.areaGravity - 10);
      case 10 -> controller.areaGravity = Math.min(200, controller.areaGravity + 10);
      case 11 ->
          controller.areaRadius = controller.areaRadius == 25 ? 15 : controller.areaRadius + 5;
      case 12 ->
          controller.areaDirection = (controller.areaDirection + 1) % Direction.values().length;
      default -> {
        return;
      }
    }
    controller.status =
        Texts.translate(
            "status.adv_rocketry.area_gravity.gravity_toward_within_blocks",
            controller.areaGravity,
            Direction.values()[controller.areaDirection],
            controller.areaRadius);
    controller.setChanged();
    if (controller.getLevel() instanceof ServerLevel level)
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 3);
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return;
    boolean formed = complete(controller);
    if (formed
        && level.getBlockEntity(controller.getBlockPos().below()) instanceof PortBlockEntity port)
      controller.pullFrom(port, 200);
    boolean powered = formed && controller.energy.consume(20, true) == 20;
    float previous = controller.areaCurrentGravity;
    float target = powered ? controller.areaGravity / 100f : 0;
    float step = powered ? .001f : .01f;
    if (controller.areaCurrentGravity < target)
      controller.areaCurrentGravity = Math.min(target, controller.areaCurrentGravity + step);
    else if (controller.areaCurrentGravity > target)
      controller.areaCurrentGravity = Math.max(target, controller.areaCurrentGravity - step);
    if (controller.areaCurrentGravity != previous) controller.setChanged();
    if (level.getGameTime() % 20 == 0
        && controller.areaCurrentGravity != controller.areaSyncedGravity) {
      controller.areaSyncedGravity = controller.areaCurrentGravity;
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 3);
    }
    if (!powered || controller.areaCurrentGravity <= 0) return;
    controller.energy.consume(20, false);
    Direction direction = Direction.values()[controller.areaDirection];
    int radius = controller.areaRadius;
    AABB region = new AABB(controller.getBlockPos()).inflate(radius);
    for (Entity entity :
        level.getEntities((Entity) null, region, entity -> !entity.isSpectator())) {
      if (entity instanceof Player player && player.getAbilities().flying) continue;
      double force = (entity instanceof LivingEntity ? .08 : .04) * controller.areaCurrentGravity;
      Vec3 motion = entity.getDeltaMovement();
      entity.setDeltaMovement(
          motion.add(
              direction.getStepX() * force,
              direction.getStepY() * force,
              direction.getStepZ() * force));
      entity.fallDistance = 0;
    }
  }
}

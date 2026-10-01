// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

/** Native adapter for the original enriched-lava material's movement and item buoyancy. */
public final class EnrichedLavaFluidType extends FluidType {
  public EnrichedLavaFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public double motionScale(Entity entity) {
    return NeoForgeMod.LAVA_TYPE.value().motionScale(entity);
  }

  @Override
  public void setItemMovement(ItemEntity entity) {
    NeoForgeMod.LAVA_TYPE.value().setItemMovement(entity);
  }

  @Override
  public boolean move(FluidState state, LivingEntity entity, Vec3 input, double gravity) {
    entity.moveRelative(.02f, input);
    entity.move(MoverType.SELF, entity.getDeltaMovement());
    Vec3 velocity = entity.getDeltaMovement().scale(.5);
    if (!entity.isNoGravity()) velocity = velocity.add(0, -gravity * .25, 0);
    if (entity.horizontalCollision
        && entity
            .level()
            .noCollision(entity, entity.getBoundingBox().move(velocity.x, .6, velocity.z)))
      velocity = new Vec3(velocity.x, .3, velocity.z);
    entity.setDeltaMovement(velocity);
    return true;
  }
}

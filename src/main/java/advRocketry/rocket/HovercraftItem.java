// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Places the original hovercraft at a targeted surface. */
public final class HovercraftItem extends Item {
  public HovercraftItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    BlockHitResult hit =
        level.clip(
            new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(5)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
    HovercraftEntity craft = HovercraftRegistry.ENTITY.get().create(level);
    if (craft == null) return InteractionResultHolder.fail(stack);
    craft.setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
    craft.setYRot(player.getYRot());
    if (!level.noCollision(craft, craft.getBoundingBox()))
      return InteractionResultHolder.fail(stack);
    if (!level.isClientSide && level.addFreshEntity(craft) && !player.getAbilities().instabuild)
      stack.shrink(1);
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
  }
}

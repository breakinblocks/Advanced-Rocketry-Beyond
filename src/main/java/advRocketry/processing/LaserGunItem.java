// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Original 50-block laser: a short held beam mines a fixed block or strikes an entity. */
public final class LaserGunItem extends Item {
  private static final int REACH = 50;
  private final Map<LivingEntity, BlockPos> lockedTargets = new WeakHashMap<>();

  public LaserGunItem(Properties properties) {
    super(
        properties
            .stacksTo(1)
            .component(
                DataComponents.TOOL,
                new Tool(
                    List.of(
                        Tool.Rule.deniesDrops(Tiers.DIAMOND.getIncorrectBlocksForDrops()),
                        new Tool.Rule(
                            BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.MINEABLE_WITH_PICKAXE),
                            Optional.empty(),
                            Optional.of(true))),
                    0,
                    0)));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) lockedTargets.remove(player);
    player.startUsingItem(hand);
    return InteractionResultHolder.consume(player.getItemInHand(hand));
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 16;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.NONE;
  }

  @Override
  public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
    if (!(user instanceof Player player) || level.isClientSide) return;
    Vec3 start = player.getEyePosition();
    Vec3 end = start.add(player.getLookAngle().scale(REACH));
    HitResult block = player.pick(REACH, 1, false);
    double limit =
        block.getType() == HitResult.Type.BLOCK
            ? start.distanceToSqr(block.getLocation())
            : REACH * REACH;
    Entity target = null;
    Vec3 impact = block.getLocation();
    double nearest = limit;
    for (Entity candidate : level.getEntities(player, new AABB(start, end).inflate(1))) {
      if (!candidate.isPickable() || candidate.isSpectator()) continue;
      var hit = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(start, end);
      if (hit.isPresent() && start.distanceToSqr(hit.get()) < nearest) {
        target = candidate;
        nearest = start.distanceToSqr(hit.get());
        impact = hit.get();
      }
    }
    if (target != null) {
      if (player instanceof ServerPlayer shooter) LaserBeamSync.send(shooter, impact);
      target.hurt(player.damageSources().playerAttack(player), 1);
      level.playSound(
          null,
          player.blockPosition(),
          ProcessingRegistry.LASER_GUN_SOUND.get(),
          SoundSource.PLAYERS,
          1,
          1);
      player.stopUsingItem();
      lockedTargets.remove(player);
      return;
    }
    if (block.getType() == HitResult.Type.BLOCK && block instanceof BlockHitResult hit) {
      if (player instanceof ServerPlayer shooter) LaserBeamSync.send(shooter, impact);
      if (remaining % 5 == 0)
        level.playSound(
            null,
            player.blockPosition(),
            ProcessingRegistry.LASER_GUN_SOUND.get(),
            SoundSource.PLAYERS,
            1,
            1);
      BlockPos previous = lockedTargets.putIfAbsent(player, hit.getBlockPos());
      if (previous != null && !previous.equals(hit.getBlockPos())) {
        player.stopUsingItem();
        lockedTargets.remove(player);
      }
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
    if (level.isClientSide) return stack;
    BlockPos target = lockedTargets.remove(user);
    if (target != null && user instanceof ServerPlayer player) {
      HitResult hit = player.pick(REACH, 1, false);
      if (hit instanceof BlockHitResult block
          && block.getBlockPos().equals(target)
          && level.mayInteract(player, target)
          && level.getBlockState(target).getDestroySpeed(level, target) >= 0)
        player.gameMode.destroyBlock(target);
    }
    return stack;
  }

  @Override
  public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
    if (!level.isClientSide) lockedTargets.remove(user);
  }

  BlockPos lockedTarget(LivingEntity user) {
    return lockedTargets.get(user);
  }
}

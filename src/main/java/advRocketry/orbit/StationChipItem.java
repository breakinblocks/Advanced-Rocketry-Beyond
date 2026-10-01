// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Programs a guidance computer with a specific deployed station. */
public final class StationChipItem extends Item {
  public StationChipItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    long id = StationLink.read(stack);
    tooltip.add(
        id > 0
            ? Texts.translate("message.adv_rocketry.station_chip.station", id)
            : Component.translatable("message.adv_rocketry.station_chip.unprogrammed"));
    tooltip.add(
        Component.translatable(
            "message.adv_rocketry.station_chip.crouch_use_to_manage_return_destinations"));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    var chip = player.getItemInHand(hand);
    if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(chip);
    if (player instanceof ServerPlayer serverPlayer) StationChipMenu.open(serverPlayer, hand);
    return InteractionResultHolder.sidedSuccess(chip, level.isClientSide);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      if (context.getPlayer() instanceof ServerPlayer player)
        StationChipMenu.open(player, context.getHand());
      return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    return useOn(context);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (!(context.getLevel().getBlockEntity(context.getClickedPos())
            instanceof RocketBlockEntity guidance)
        || guidance.kind() != RocketPartBlock.Kind.GUIDANCE) return InteractionResult.PASS;
    if (context.getLevel() instanceof ServerLevel level) {
      long id = StationLink.read(context.getItemInHand());
      GalaxyData galaxy = GalaxyData.get(level.getServer());
      int source = StationLogic.orbitPlanet(galaxy, level, context.getClickedPos());
      if (StationLogic.destination(galaxy, source, id) == null) {
        if (context.getPlayer() != null)
          context
              .getPlayer()
              .displayClientMessage(
                  Component.translatable(
                      "message.adv_rocketry.station_chip.this_station_is_not_deployed_in"),
                  true);
        return InteractionResult.FAIL;
      }
      guidance.stationId = id;
      guidance.destination = GalaxyData.SPACE_ID;
      guidance.setChanged();
      level.sendBlockUpdated(
          guidance.getBlockPos(), guidance.getBlockState(), guidance.getBlockState(), 3);
      if (context.getPlayer() != null)
        context
            .getPlayer()
            .displayClientMessage(
                Texts.translate("message.adv_rocketry.station_chip.guidance_set_to_station", id),
                true);
    }
    return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
  }
}

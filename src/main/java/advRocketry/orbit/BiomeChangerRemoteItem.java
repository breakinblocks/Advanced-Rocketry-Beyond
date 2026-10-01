// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Linked satellite remote: sneak-use samples a biome; use schedules its orbital change. */
public final class BiomeChangerRemoteItem extends Item {
  public BiomeChangerRemoteItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  private static String selectedBiome(ItemStack stack) {
    BiomeSelection selection = stack.get(ModComponents.BIOME_SELECTION);
    return selection == null ? "" : selection.biome().toString();
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
    ItemStack stack = context.getItemInHand();
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      ResourceLocation biome =
          level.getBiome(context.getClickedPos()).unwrapKey().orElseThrow().location();
      stack.set(ModComponents.BIOME_SELECTION, new BiomeSelection(biome));
      context
          .getPlayer()
          .displayClientMessage(
              Texts.translate("message.adv_rocketry.biome_changer_remote.selected_biome", biome),
              true);
      return InteractionResult.SUCCESS;
    }
    Component status =
        BiomeChangerLogic.start(
            level, SatelliteLink.read(stack), context.getClickedPos(), selectedBiome(stack));
    if (context.getPlayer() != null) context.getPlayer().displayClientMessage(status, true);
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level instanceof ServerLevel serverLevel && !player.isShiftKeyDown()) {
      Component status =
          BiomeChangerLogic.start(
              serverLevel, SatelliteLink.read(stack), player.blockPosition(), selectedBiome(stack));
      player.displayClientMessage(status, true);
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    if (stack.has(ModComponents.SATELLITE_LINK))
      tooltip.add(
          Texts.translate(
              "message.adv_rocketry.biome_changer_remote.satellite", SatelliteLink.read(stack)));
    if (stack.has(ModComponents.BIOME_SELECTION))
      tooltip.add(
          Texts.translate("message.adv_rocketry.biome_changer_remote.biome", selectedBiome(stack)));
    tooltip.add(
        Component.translatable(
            "message.adv_rocketry.biome_changer_remote.sneak_use_to_sample_use_to"));
  }
}

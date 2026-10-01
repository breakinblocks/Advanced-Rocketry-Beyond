// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.util.Texts;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Original handheld atmosphere type, pressure and breathability readout. */
public final class AtmosphereAnalyzerItem extends Item {
  public AtmosphereAnalyzerItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack analyzer = player.getItemInHand(hand);
    if (level instanceof ServerLevel serverLevel) {
      BlockPos position = player.blockPosition();
      AtmosphereDetectorBlockEntity.Target type =
          AtmosphereDetectorBlockEntity.atmosphere(serverLevel, position);
      Planet planet = GalaxyData.get(serverLevel.getServer()).planet(serverLevel);
      boolean pressurized = type == AtmosphereDetectorBlockEntity.Target.PRESSURIZED_AIR;
      float pressure = pressurized ? 1f : planet.atmosphere / 100f;
      for (Component line : readout(type, pressure)) player.displayClientMessage(line, false);
    }
    return InteractionResultHolder.sidedSuccess(analyzer, level.isClientSide);
  }

  public static List<Component> readout(AtmosphereDetectorBlockEntity.Target type, float pressure) {
    boolean breathable =
        type == AtmosphereDetectorBlockEntity.Target.PRESSURIZED_AIR
            || type == AtmosphereDetectorBlockEntity.Target.AIR;
    return List.of(
        Texts.translate(
            "message.adv_rocketry.atmosphere_readout",
            Component.translatable(
                "atmosphere.adv_rocketry." + type.name().toLowerCase(Locale.ROOT)),
            String.format(Locale.ROOT, "%.2f", pressure)),
        Texts.translate(
            "message.adv_rocketry.breathable_readout",
            Component.translatable(
                breathable ? "message.adv_rocketry.yes" : "message.adv_rocketry.no")));
  }
}

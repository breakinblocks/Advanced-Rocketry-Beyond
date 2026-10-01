// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Original satellite ID chip role, with server-authoritative orbit status. */
public final class SatelliteChipItem extends Item {
  public SatelliteChipItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!(level instanceof ServerLevel serverLevel))
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    long id = SatelliteLink.read(stack);
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    Satellite record = galaxy.satellites.get(id);
    if (id == 0 || record == null) {
      player.displayClientMessage(
          Component.translatable("message.adv_rocketry.satellite_chip.no_satellite_programmed"),
          true);
      return InteractionResultHolder.success(stack);
    }
    SatelliteLogic.advance(galaxy, id, serverLevel.getGameTime());
    record = galaxy.satellites.get(id);
    if (!record.deployed)
      player.displayClientMessage(
          Texts.translate("message.adv_rocketry.satellite_chip.satellite_awaiting_launch", id),
          false);
    else {
      var planet = galaxy.planets.get(record.orbitPlanet);
      player.displayClientMessage(
          Texts.translate(
              "message.adv_rocketry.satellite_chip.satellite_orbiting_fe_data",
              id,
              record.type == null ? "" : record.type.displayName(),
              (planet == null ? "unknown" : planet.name),
              record.energy,
              record.data),
          false);
    }
    return InteractionResultHolder.success(stack);
  }
}

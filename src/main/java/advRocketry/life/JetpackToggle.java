// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.Main;
import advRocketry.ModComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Original X-key toggle and sneak+X hover-mode selection for the equipped jetpack. */
public record JetpackToggle(boolean mode) implements CustomPacketPayload {
  public static final Type<JetpackToggle> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "jetpack_toggle"));
  public static final StreamCodec<RegistryFriendlyByteBuf, JetpackToggle> CODEC =
      StreamCodec.of(
          (buffer, payload) -> buffer.writeBoolean(payload.mode()),
          buffer -> new JetpackToggle(buffer.readBoolean()));

  @Override
  public Type<JetpackToggle> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              if (context.player() instanceof ServerPlayer player) toggle(player, payload.mode());
            });
  }

  static boolean toggle(Player player, boolean mode) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (!(chest.getItem() instanceof SpaceSuitItem suit)) return false;
    ItemStack module = SpaceSuitItem.findModule(chest, JetpackControl::jetpack).copy();
    if (module.isEmpty()) return false;
    if (mode) {
      ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
      if (!(helmet.getItem() instanceof SpaceSuitItem head)
          || head.countModule(helmet, "hover_upgrade") == 0) return false;
    }
    JetpackSettings settings =
        module.getOrDefault(ModComponents.JETPACK, JetpackSettings.OFF).toggled(mode);
    module.set(ModComponents.JETPACK, settings);
    suit.replaceModule(chest, JetpackControl::jetpack, module);
    player.displayClientMessage(
        Component.translatable(
            mode ? "message.adv_rocketry.jetpack.hover" : "message.adv_rocketry.jetpack.thrust",
            Component.translatable(
                settings.get(mode)
                    ? "message.adv_rocketry.jetpack.on"
                    : "message.adv_rocketry.jetpack.off")),
        true);
    return true;
  }
}

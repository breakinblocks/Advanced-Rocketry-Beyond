// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Bounded editor commands validated against the currently open server menu. */
public record StationChipAction(int menuId, int action, int index, String name)
    implements CustomPacketPayload {
  public static final Type<StationChipAction> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "station_chip_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf, StationChipAction> CODEC =
      StreamCodec.of(
          (buffer, payload) -> {
            buffer.writeVarInt(payload.menuId());
            buffer.writeVarInt(payload.action());
            buffer.writeVarInt(payload.index());
            buffer.writeUtf(payload.name(), 32);
          },
          buffer ->
              new StationChipAction(
                  buffer.readVarInt(),
                  buffer.readVarInt(),
                  buffer.readVarInt(),
                  buffer.readUtf(32)));

  @Override
  public Type<StationChipAction> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              var player = context.player();
              if (player.containerMenu instanceof StationChipMenu menu
                  && menu.containerId == payload.menuId())
                menu.apply(player, payload.action(), payload.index(), payload.name());
            });
  }
}

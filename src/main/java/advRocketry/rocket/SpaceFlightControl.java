// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Steering intent only; position and arrival decisions remain on the server. */
public record SpaceFlightControl(int forward, int turn, int vertical, boolean toggleRcs)
    implements CustomPacketPayload {
  public static final Type<SpaceFlightControl> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "space_flight_control"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SpaceFlightControl> CODEC =
      StreamCodec.of(
          (buffer, value) -> {
            buffer.writeByte(value.forward());
            buffer.writeByte(value.turn());
            buffer.writeByte(value.vertical());
            buffer.writeBoolean(value.toggleRcs());
          },
          buffer ->
              new SpaceFlightControl(
                  buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readBoolean()));

  @Override
  public Type<SpaceFlightControl> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              if (context.player().getVehicle() instanceof RocketEntity rocket)
                rocket.spaceControls(
                    context.player(),
                    payload.forward(),
                    payload.turn(),
                    payload.vertical(),
                    payload.toggleRcs());
            });
  }
}

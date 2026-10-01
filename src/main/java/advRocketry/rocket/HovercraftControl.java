// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Steering input is accepted only from the hovercraft's current rider. */
public record HovercraftControl(float forward, float turn, boolean up, boolean down)
    implements CustomPacketPayload {
  public static final Type<HovercraftControl> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "hovercraft_control"));
  public static final StreamCodec<RegistryFriendlyByteBuf, HovercraftControl> CODEC =
      StreamCodec.of(
          (buffer, input) -> {
            buffer.writeFloat(input.forward());
            buffer.writeFloat(input.turn());
            buffer.writeBoolean(input.up());
            buffer.writeBoolean(input.down());
          },
          buffer ->
              new HovercraftControl(
                  buffer.readFloat(),
                  buffer.readFloat(),
                  buffer.readBoolean(),
                  buffer.readBoolean()));

  @Override
  public Type<HovercraftControl> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              if (context.player().getVehicle() instanceof HovercraftEntity craft)
                craft.control(
                    context.player(),
                    payload.forward(),
                    payload.turn(),
                    payload.up(),
                    payload.down());
            });
  }
}

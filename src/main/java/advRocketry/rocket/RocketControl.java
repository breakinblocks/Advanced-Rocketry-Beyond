// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Flight requests are accepted only from the rocket's seated pilot. */
public record RocketControl() implements CustomPacketPayload {
  public static final Type<RocketControl> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "rocket_launch"));
  public static final StreamCodec<RegistryFriendlyByteBuf, RocketControl> CODEC =
      StreamCodec.unit(new RocketControl());

  @Override
  public Type<RocketControl> type() {
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
                rocket.launch(context.player());
            });
  }
}

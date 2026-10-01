// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.Main;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Original laser impact packet, sent to the shooter and tracking observers. */
public final class LaserBeamSync {
  public static Consumer<Payload> client = payload -> {};

  private LaserBeamSync() {}

  public record Payload(ResourceLocation dimension, int shooter, Vec3 impact)
      implements CustomPacketPayload {
    public static final Type<Payload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "laser_beam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
              buffer.writeResourceLocation(payload.dimension());
              buffer.writeVarInt(payload.shooter());
              buffer.writeVec3(payload.impact());
            },
            buffer ->
                new Payload(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readVec3()));

    @Override
    public Type<Payload> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToClient(Payload.TYPE, Payload.CODEC, (payload, context) -> client.accept(payload));
  }

  public static void send(ServerPlayer player, Vec3 impact) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(
        player, new Payload(player.level().dimension().location(), player.getId(), impact));
  }
}

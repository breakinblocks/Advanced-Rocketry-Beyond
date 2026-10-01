// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.Main;
import advRocketry.life.AtmosphereDetectorBlockEntity.Target;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Original local-pressure synchronization, including breathable vent rooms. */
public final class AtmosphereSync {
  private static final Map<ServerPlayer, Payload> LAST = new WeakHashMap<>();
  private static Payload client;
  private static WarningPayload warning;
  private static int warningUntil;

  private AtmosphereSync() {}

  public record Payload(ResourceLocation dimension, int pressure, Target atmosphere)
      implements CustomPacketPayload {
    public static final Type<Payload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "local_pressure"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
              buffer.writeResourceLocation(payload.dimension());
              buffer.writeVarInt(payload.pressure());
              buffer.writeEnum(payload.atmosphere());
            },
            buffer ->
                new Payload(
                    buffer.readResourceLocation(),
                    buffer.readVarInt(),
                    buffer.readEnum(Target.class)));

    @Override
    public Type<Payload> type() {
      return TYPE;
    }
  }

  public record WarningPayload(ResourceLocation dimension, Target atmosphere)
      implements CustomPacketPayload {
    public static final Type<WarningPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "atmosphere_warning"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WarningPayload> CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
              buffer.writeResourceLocation(payload.dimension());
              buffer.writeEnum(payload.atmosphere());
            },
            buffer ->
                new WarningPayload(buffer.readResourceLocation(), buffer.readEnum(Target.class)));

    @Override
    public Type<WarningPayload> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToClient(Payload.TYPE, Payload.CODEC, (payload, context) -> client = payload);
    event
        .registrar("1")
        .playToClient(
            WarningPayload.TYPE,
            WarningPayload.CODEC,
            (payload, context) -> {
              warning = payload;
              warningUntil = context.player().tickCount + 100;
            });
  }

  public static void warn(ServerPlayer player) {
    PacketDistributor.sendToPlayer(
        player,
        new WarningPayload(
            player.level().dimension().location(),
            AtmosphereDetectorBlockEntity.atmosphere(
                player.serverLevel(), player.blockPosition())));
  }

  public static Target clientWarning(ResourceLocation dimension, int tick) {
    return warning != null
            && warning.dimension().equals(dimension)
            && tick < warningUntil
            && tick >= warningUntil - 100
        ? warning.atmosphere()
        : null;
  }

  public static int pressure(ServerPlayer player) {
    return SealedRooms.breathable(player.level(), player.blockPosition())
        ? 100
        : GalaxyData.get(player.server).planet(player.level()).atmosphere;
  }

  public static void tick(ServerPlayer player) {
    if (player.tickCount % 10 != 0) return;
    Payload state =
        new Payload(
            player.level().dimension().location(),
            pressure(player),
            AtmosphereDetectorBlockEntity.atmosphere(player.serverLevel(), player.blockPosition()));
    if (state.equals(LAST.get(player))) return;
    LAST.put(player, state);
    PacketDistributor.sendToPlayer(player, state);
  }

  public static int clientPressure(ResourceLocation dimension, int fallback) {
    return client != null && client.dimension().equals(dimension) ? client.pressure() : fallback;
  }

  public static Target clientAtmosphere(ResourceLocation dimension, Planet fallback) {
    return client != null && client.dimension().equals(dimension)
        ? client.atmosphere()
        : fallback == null ? Target.AIR : AtmosphereDetectorBlockEntity.atmosphere(fallback);
  }

  public static void clearClient() {
    client = null;
    warning = null;
    warningUntil = 0;
  }
}

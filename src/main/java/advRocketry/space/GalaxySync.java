// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.Main;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server-authoritative planet descriptions shared with sky and navigation screens. */
public final class GalaxySync {
  private static final Map<ResourceLocation, Planet> CLIENT_PLANETS = new ConcurrentHashMap<>();
  private static final Map<Integer, Star> CLIENT_STARS = new ConcurrentHashMap<>();
  private static final Map<Long, Station> CLIENT_STATIONS = new ConcurrentHashMap<>();

  private static final Map<MinecraftServer, CompoundTag> LAST_STATIONS = new WeakHashMap<>();

  private GalaxySync() {}

  public record StationPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<StationPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "station_visuals"));
    public static final StreamCodec<ByteBuf, StationPayload> CODEC =
        ByteBufCodecs.COMPOUND_TAG.map(StationPayload::new, StationPayload::data);

    @Override
    public Type<StationPayload> type() {
      return TYPE;
    }
  }

  /**
   * Only properties consumed by navigation and rendering; never packed station blocks or
   * inventories.
   */
  public static CompoundTag stationSnapshot(GalaxyData galaxy) {
    CompoundTag result = new CompoundTag();
    galaxy.stations.forEach((id, station) -> result.put(Long.toString(id), station.snapshot()));
    return result;
  }

  public static void syncStations(MinecraftServer server) {
    if (server.getPlayerList().getPlayers().isEmpty()) return;
    CompoundTag stations = stationSnapshot(GalaxyData.get(server));
    if (stations.equals(LAST_STATIONS.get(server))) return;
    LAST_STATIONS.put(server, stations.copy());
    for (ServerPlayer player : server.getPlayerList().getPlayers())
      PacketDistributor.sendToPlayer(player, new StationPayload(stations));
  }

  private static void installStations(CompoundTag stations) {
    CLIENT_STATIONS.clear();
    for (String key : stations.getAllKeys())
      CLIENT_STATIONS.put(Long.parseLong(key), Station.load(stations.getCompound(key)));
  }

  public record Payload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<Payload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "galaxy"));
    public static final StreamCodec<ByteBuf, Payload> CODEC =
        ByteBufCodecs.COMPOUND_TAG.map(Payload::new, Payload::data);

    @Override
    public Type<Payload> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToClient(
            StationPayload.TYPE,
            StationPayload.CODEC,
            (payload, context) -> installStations(payload.data()));
    event
        .registrar("1")
        .playToClient(
            Payload.TYPE,
            Payload.CODEC,
            (payload, context) -> {
              clearClient();
              for (Tag entry : payload.data().getList("stars", Tag.TAG_COMPOUND)) {
                Star star = Star.load((CompoundTag) entry);
                CLIENT_STARS.put(star.id(), star);
              }
              for (Tag entry : payload.data().getList("planets", Tag.TAG_COMPOUND)) {
                Planet planet = Planet.load((CompoundTag) entry);
                CLIENT_PLANETS.put(planet.dimension, planet);
              }
              installStations(payload.data().getCompound("stations"));
            });
  }

  public static void clearClient() {
    CLIENT_PLANETS.clear();
    CLIENT_STARS.clear();
    CLIENT_STATIONS.clear();
  }

  public static Planet clientPlanet(ResourceLocation dimension) {
    return CLIENT_PLANETS.get(dimension);
  }

  public static Star clientStar(int id) {
    return CLIENT_STARS.get(id);
  }

  public static Planet clientPlanet(int id) {
    return CLIENT_PLANETS.values().stream()
        .filter(planet -> planet.id == id)
        .findFirst()
        .orElse(null);
  }

  public static List<Planet> clientPlanets() {
    return List.copyOf(CLIENT_PLANETS.values());
  }

  public static List<Star> clientStars() {
    return List.copyOf(CLIENT_STARS.values());
  }

  public static Map<Long, Station> clientStations() {
    return Map.copyOf(CLIENT_STATIONS);
  }

  public static Station clientStationAt(BlockPos pos) {
    return StationLogic.at(CLIENT_STATIONS.values(), pos);
  }

  public static void send(ServerPlayer player) {
    GalaxyData galaxy = GalaxyData.get(player.server);
    CompoundTag data = new CompoundTag();
    ListTag stars = new ListTag();
    galaxy.stars.values().forEach(star -> stars.add(star.save()));
    data.put("stars", stars);
    ListTag planets = new ListTag();
    galaxy.planets.values().forEach(planet -> planets.add(planet.save()));
    data.put("planets", planets);
    data.put("stations", stationSnapshot(galaxy));
    PacketDistributor.sendToPlayer(player, new Payload(data));
  }

  public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) send(player);
  }

  public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) send(player);
  }
}

package advRocketry.util;

import advRocketry.Main;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

/** Ticking transit tickets survive saves, follow the vehicle, and obey server controls. */
public final class TicketedTransit<T extends Entity> {
  private final Set<Tracker> active = Collections.newSetFromMap(new IdentityHashMap<>());
  private final Map<ServerLevel, Map<UUID, Set<ChunkPos>>> recovered = new HashMap<>();
  private final Map<ServerLevel, Integer> recoveryDeadlines = new HashMap<>();
  private final TicketController tickets;
  private final Predicate<T> moving;
  private final BooleanSupplier enabled;

  public TicketedTransit(String name, Predicate<T> moving, BooleanSupplier enabled) {
    this.tickets =
        new TicketController(
            ResourceLocation.fromNamespaceAndPath(Main.MODID, name), this::restore);
    this.moving = moving;
    this.enabled = enabled;
  }

  public void register(RegisterTicketControllersEvent event) {
    event.register(tickets);
  }

  private void restore(ServerLevel level, TicketHelper helper) {
    recoveryDeadlines.put(level, level.getServer().getTickCount() + 200);
    helper.getBlockTickets().keySet().forEach(helper::removeAllTickets);
    helper
        .getEntityTickets()
        .forEach(
            (id, saved) -> {
              if (!enabled.getAsBoolean()) {
                helper.removeAllTickets(id);
                return;
              }
              saved.nonTicking().forEach((long chunk) -> helper.removeTicket(id, chunk, false));
              Set<ChunkPos> chunks = new HashSet<>();
              saved.ticking().forEach((long chunk) -> chunks.add(new ChunkPos(chunk)));
              recovered.computeIfAbsent(level, ignored -> new HashMap<>()).put(id, chunks);
            });
  }

  private void releaseRecovered(ServerLevel level, UUID id) {
    var saved = recovered.get(level);
    var chunks = saved == null ? null : saved.remove(id);
    if (chunks != null)
      for (ChunkPos chunk : chunks) tickets.forceChunk(level, id, chunk.x, chunk.z, false, true);
  }

  public Tracker track(T entity) {
    return new Tracker(entity);
  }

  public Stream<T> active() {
    return active.stream().map(tracker -> tracker.entity);
  }

  public void tickUnscheduled(MinecraftServer server) {
    int now = server.getTickCount();
    for (Tracker tracker : new ArrayList<>(active)) {
      if (tracker.entity.level().getServer() != server) continue;
      tracker.update();
      if (!enabled.getAsBoolean()) continue;
      T entity = tracker.entity;
      if (!entity.isRemoved()
          && moving.test(entity)
          && entity.level() instanceof ServerLevel level
          && tracker.lastTick != now) entity.tick();
    }
    // The forced entity chunks get a startup grace period to deserialize their vehicles.
    if (now % 20 == 0)
      for (var entry : new ArrayList<>(recovered.entrySet())) {
        if (entry.getKey().getServer() != server
            || now <= recoveryDeadlines.getOrDefault(entry.getKey(), now)) continue;
        for (UUID id : new ArrayList<>(entry.getValue().keySet()))
          releaseRecovered(entry.getKey(), id);
      }
  }

  public void clear() {
    active.clear();
    recovered.clear();
    recoveryDeadlines.clear();
  }

  public final class Tracker {
    private final T entity;
    private ChunkPos chunk;
    private ServerLevel ticketLevel;
    private int lastTick = Integer.MIN_VALUE;

    private Tracker(T entity) {
      this.entity = entity;
    }

    public void added() {
      if (entity.level() instanceof ServerLevel level) {
        active.add(this);
        releaseRecovered(level, entity.getUUID());
      }
      update();
    }

    public void removed() {
      active.remove(this);
      // Tracking removal during shutdown can have no removal reason. Preserve save/unload
      // tickets so a moving vehicle can restore itself next startup.
      if ((entity.getRemovalReason() != null
              && entity.getRemovalReason() != Entity.RemovalReason.UNLOADED_TO_CHUNK)
          || !enabled.getAsBoolean()
          || !moving.test(entity)) release();
    }

    public void ticked(ServerLevel level) {
      lastTick = level.getServer().getTickCount();
    }

    public void update() {
      if (!(entity.level() instanceof ServerLevel level)) return;
      ChunkPos current =
          enabled.getAsBoolean() && !entity.isRemoved() && moving.test(entity)
              ? new ChunkPos(entity.blockPosition())
              : null;
      if (current != null && current.equals(chunk) && ticketLevel == level) return;
      release();
      chunk = current;
      ticketLevel = level;
      if (current != null)
        tickets.forceChunk(level, entity.getUUID(), current.x, current.z, true, true);
    }

    public void release() {
      if (chunk != null && ticketLevel != null)
        tickets.forceChunk(ticketLevel, entity.getUUID(), chunk.x, chunk.z, false, true);
      chunk = null;
      ticketLevel = null;
    }
  }
}

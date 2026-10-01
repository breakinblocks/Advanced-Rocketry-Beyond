package advRocketry.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public final class TicketedTransit<T extends Entity> {
  private final Set<Tracker> active = Collections.newSetFromMap(new IdentityHashMap<>());
  private final TicketType<UUID> ticket;
  private final Predicate<T> moving;

  public TicketedTransit(String name, Predicate<T> moving) {
    this.ticket = TicketType.create(name, Comparator.<UUID>naturalOrder());
    this.moving = moving;
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
      T entity = tracker.entity;
      if (!entity.isRemoved()
          && moving.test(entity)
          && entity.level() instanceof ServerLevel level
          && level.getServer() == server
          && tracker.lastTick != now) entity.tick();
    }
  }

  public void clear() {
    active.clear();
  }

  public final class Tracker {
    private final T entity;
    private ChunkPos chunk;
    private int lastTick = Integer.MIN_VALUE;

    private Tracker(T entity) {
      this.entity = entity;
    }

    public void added() {
      if (entity.level() instanceof ServerLevel) active.add(this);
      update();
    }

    public void removed() {
      active.remove(this);
      release();
    }

    public void ticked(ServerLevel level) {
      lastTick = level.getServer().getTickCount();
    }

    public void update() {
      if (!(entity.level() instanceof ServerLevel level)) return;
      ChunkPos current = moving.test(entity) ? new ChunkPos(entity.blockPosition()) : null;
      if (current != null && current.equals(chunk)) return;
      if (chunk != null)
        level.getChunkSource().removeRegionTicket(ticket, chunk, 2, entity.getUUID());
      chunk = current;
      if (current != null)
        level.getChunkSource().addRegionTicket(ticket, current, 2, entity.getUUID());
    }

    public void release() {
      if (chunk != null && entity.level() instanceof ServerLevel level)
        level.getChunkSource().removeRegionTicket(ticket, chunk, 2, entity.getUUID());
      chunk = null;
    }
  }
}

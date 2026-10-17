// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import advRocketry.util.Texts;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Cached loaded-only topology. One endpoint performs bounded work per network tick. */
public final class TransportNetworks {
  public static final int MAX_NODES = 4096;
  private static final Map<Level, Map<BlockPos, Network>> CACHE = new WeakHashMap<>();
  private static final Map<Level, Set<BlockPos>> DIRTY = new WeakHashMap<>();
  private static final Map<Level, DiscoveryBudget> BUDGETS = new WeakHashMap<>();
  private static final Set<Level> OVERSIZED = Collections.newSetFromMap(new WeakHashMap<>());

  private static final class DiscoveryBudget {
    long tick = Long.MIN_VALUE;
    int remaining;
  }

  public static void capabilityChanged(TransportBlockEntity pipe) {
    if (!(pipe.getLevel() instanceof ServerLevel)) return;
    pipe.connectionsDirty = true;
    pipe.wake();
    invalidate(pipe.getLevel(), pipe.getBlockPos());
    DIRTY.computeIfAbsent(pipe.getLevel(), level -> new HashSet<>()).add(pipe.getBlockPos());
  }

  /** Policy edits preserve topology and hop distances; external capabilities stay authoritative. */
  public static void routesChanged(TransportBlockEntity pipe, boolean reorder) {
    var cache = CACHE.get(pipe.getLevel());
    var network = cache == null ? null : cache.get(pipe.getBlockPos());
    pipe.wake();
    if (network == null) return;
    for (var worker : network.workers) worker.wake();
    if (reorder)
      network.outputs.sort(
          Comparator.comparingInt((End end) -> end.pipe.side(end.side).priority).reversed());
  }

  public static void levelTick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    var positions = DIRTY.remove(level);
    if (positions == null) return;
    for (var pos : positions)
      if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof TransportBlockEntity pipe)
        pipe.refreshConnections();
  }

  public static final class Network {
    public final List<TransportBlockEntity> nodes = new ArrayList<>();
    final Set<BlockPos> members = new HashSet<>();
    final List<End> ends = new ArrayList<>();
    final List<End> outputs = new ArrayList<>();
    final Map<BlockPos, Map<BlockPos, Integer>> distances = new LinkedHashMap<>();
    long distanceTick = Long.MIN_VALUE;
    final List<TransportBlockEntity> workers = new ArrayList<>();
    Fluid configuredFluid = Fluids.EMPTY;
    boolean conflictingLocks;
    long lastPulse = Long.MIN_VALUE;
    boolean invalid;
    public boolean oversized;
    public boolean roundRobin;
    int probesRemaining;
    long lastTick = Long.MIN_VALUE;

    public boolean conflicted() {
      if (conflictingLocks) return true;
      Fluid known = configuredFluid;
      for (var pipe : workers) {
        Fluid fluid = pipe.pendingFluid.getFluid();
        if (fluid != Fluids.EMPTY) {
          if (known != Fluids.EMPTY && known != fluid) return true;
          known = fluid;
        }
      }
      return false;
    }

    Fluid acceptedFluid() {
      if (configuredFluid != Fluids.EMPTY) return configuredFluid;
      for (var pipe : workers)
        if (!pipe.pendingFluid.isEmpty()) return pipe.pendingFluid.getFluid();
      return Fluids.EMPTY;
    }
  }

  private record End(TransportBlockEntity pipe, Direction side, BlockPos target) {
    End(TransportBlockEntity pipe, Direction side) {
      this(pipe, side, pipe.getBlockPos().relative(side));
    }

    boolean sameTarget(End other) {
      return target().equals(other.target());
    }

    Object handler() {
      return capability(pipe, side);
    }

    int mode() {
      return pipe.modes[side.ordinal()];
    }
  }

  private TransportNetworks() {}

  public static void unload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof Level level && !level.isClientSide) {
      invalidate(level);
      DIRTY.remove(level);
      BUDGETS.remove(level);
    }
  }

  public static void invalidate(Level level) {
    if (level != null && !level.isClientSide) {
      var previous = CACHE.remove(level);
      if (previous != null)
        for (var network : new HashSet<>(previous.values()))
          for (var node : network.nodes) node.wake();
      if (previous != null) for (var network : previous.values()) network.invalid = true;
      OVERSIZED.remove(level);
    }
  }

  public static void invalidate(Level level, BlockPos pos) {
    if (level == null || level.isClientSide) return;
    // Oversized components are cached in bounded fragments; a topology change must clear all.
    if (OVERSIZED.contains(level)) {
      invalidate(level);
      return;
    }
    var cache = CACHE.get(level);
    if (cache == null) return;
    forget(cache, cache.get(pos));
    for (Direction side : Direction.values()) forget(cache, cache.get(pos.relative(side)));
  }

  private static void invalidate(TransportBlockEntity pipe, Network network) {
    var cache = CACHE.get(pipe.getLevel());
    if (cache != null) forget(cache, network);
  }

  private static void forget(Map<BlockPos, Network> cache, Network network) {
    if (network != null) {
      network.invalid = true;
      for (var node : network.nodes) node.wake();
      for (BlockPos member : network.members) cache.remove(member, network);
    }
  }

  public static Object capability(TransportBlockEntity pipe, Direction direction) {
    Level level = pipe.getLevel();
    BlockPos next = pipe.getBlockPos().relative(direction);
    if (level == null
        || (level.hasChunkAt(next)
            && level.getBlockState(next).getBlock() instanceof TransportBlock)) return null;
    return pipe.endpointCapability(direction);
  }

  public static Network network(TransportBlockEntity start) {
    Level level = start.getLevel();
    if (!(level instanceof ServerLevel)) {
      Network unavailable = new Network();
      unavailable.oversized = true;
      return unavailable;
    }
    var cache = CACHE.computeIfAbsent(level, key -> new HashMap<>());
    Network cached = cache.get(start.getBlockPos());
    if (cached != null) return cached;
    Network network = new Network();
    DiscoveryBudget budget = BUDGETS.computeIfAbsent(level, ignored -> new DiscoveryBudget());
    if (budget.tick != level.getGameTime()) {
      budget.tick = level.getGameTime();
      budget.remaining = MAX_NODES * 4;
    }
    // Incomplete discovery never exposes a functioning subnet or enters the cache.
    if (budget.remaining < MAX_NODES + 1) {
      network.oversized = true;
      return network;
    }
    var seen = network.members;
    var queue = new ArrayDeque<TransportBlockEntity>();
    queue.add(start);
    seen.add(start.getBlockPos());
    while (!queue.isEmpty()) {
      budget.remaining--;
      var pipe = queue.remove();
      if (!network.oversized) {
        network.nodes.add(pipe);
        network.oversized = network.nodes.size() > MAX_NODES;
      }
      for (Direction side : Direction.values()) {
        if (pipe.modes[side.ordinal()] == TransportBlockEntity.DISABLED) continue;
        BlockPos next = pipe.getBlockPos().relative(side);
        if (!level.hasChunkAt(next)) continue;
        if (level.getBlockEntity(next) instanceof TransportBlockEntity other) {
          if (other.kind() == start.kind()
              && other.channel == start.channel
              && other.modes[side.getOpposite().ordinal()] != TransportBlockEntity.DISABLED
              && seen.add(next)) {
            if (seen.size() > MAX_NODES || (cache.get(next) != null && cache.get(next).oversized)) {
              network.oversized = true;
              break;
            }
            queue.add(other);
          }
        } else if (!network.oversized
            && (capability(pipe, side) != null || pipe.side(side).disposal))
          network.ends.add(new End(pipe, side));
      }
      if (network.oversized) break;
    }
    if (network.oversized) {
      OVERSIZED.add(level);
      for (BlockPos member : seen) cache.put(member, network);
      return network;
    }
    for (var end : network.ends)
      if (end.mode() == TransportBlockEntity.INSERT) network.outputs.add(end);
    network.outputs.sort(
        Comparator.comparingInt((End end) -> end.pipe.side(end.side).priority).reversed());
    network.nodes.sort(Comparator.comparing(TransportBlockEntity::getBlockPos));
    var endpointPositions = new HashSet<BlockPos>();
    for (var end : network.ends) endpointPositions.add(end.pipe.getBlockPos());
    for (var pipe : network.nodes) {
      pipe.networkSize = network.nodes.size();
      network.roundRobin |= pipe.roundRobin;
      if (endpointPositions.contains(pipe.getBlockPos())
          || !pipe.pendingFluid.isEmpty()
          || !pipe.pendingItem.isEmpty()
          || pipe.pendingEnergy > 0) network.workers.add(pipe);
      if (pipe.lock != Fluids.EMPTY) {
        if (network.configuredFluid != Fluids.EMPTY && network.configuredFluid != pipe.lock)
          network.conflictingLocks = true;
        network.configuredFluid = pipe.lock;
      }
    }
    for (BlockPos member : seen) cache.put(member, network);
    return network;
  }

  public static void tick(TransportBlockEntity endpoint) {
    if (!(endpoint.getLevel() instanceof ServerLevel level)) return;
    Network network = network(endpoint);
    long now = level.getGameTime();
    if (network.lastTick == now || network.oversized) return;
    network.lastTick = now;
    network.probesRemaining = 2048;
    if (endpoint.kind() == TransportRegistry.Kind.FLUID && network.conflicted()) return;
    if (endpoint.kind() == TransportRegistry.Kind.ITEM && now % 20 != 0) return;
    int used = 0;
    for (var pipe : network.workers) {
      if (pipe.budgetTick != now) {
        pipe.budgetTick = now;
        pipe.spent = 0;
      }
      used = Math.max(used, pipe.spent);
    }
    int budget =
        endpoint.kind() == TransportRegistry.Kind.ENERGY
            ? 5000 * Math.max(1, network.ends.size())
            : endpoint.kind().rate - used;
    if (budget <= 0) return;
    var leader = network.nodes.getFirst();
    int initial = budget;
    // Retry retained payloads before extracting anything else.
    for (var pipe : network.workers) {
      if (budget <= 0) break;
      if (pipe.pendingItem.isEmpty() && pipe.pendingFluid.isEmpty() && pipe.pendingEnergy == 0)
        continue;
      int moved = deliver(network, pipe, destinations(network, null), budget);
      budget -= moved;
      pipe.refreshConnections();
    }
    int count = network.ends.size();
    int sourceStart = Math.floorMod(leader.sourceCursor, Math.max(1, count));
    for (int i = 0; i < count && budget > 0; i++) {
      End source = network.ends.get((sourceStart + i) % count);
      if (source.mode() != TransportBlockEntity.EXTRACT || !source.pipe.extracting(source.side))
        continue;
      if (!source.pipe.pendingItem.isEmpty()
          || !source.pipe.pendingFluid.isEmpty()
          || source.pipe.pendingEnergy > 0) continue;
      var config = source.pipe.side(source.side);
      if (!config.retry.ready(now)) {
        source.pipe.blockedReason = 1;
        continue;
      }
      if (endpoint.kind() == TransportRegistry.Kind.ENERGY) {
        source.pipe.resetEnergyBudget();
        if (source.pipe.energyIn[source.side.ordinal()] >= 5000) continue;
      }
      var targets = destinations(network, source);
      if (targets.isEmpty()) {
        source.pipe.blockedReason = 2;
        continue;
      }
      if (endpoint.kind() == TransportRegistry.Kind.ENERGY) {
        boolean capacity = false;
        for (var target : targets) {
          target.pipe.resetEnergyBudget();
          capacity |= target.pipe.energyOut[target.side.ordinal()] < 5000;
        }
        if (!capacity) continue;
      }
      Object handler = source.handler();
      if (handler == null) continue;
      if (network.probesRemaining <= 0) break;
      config.scanIncomplete = false;
      int allowance = Math.min(budget, config.extractionLimit);
      int moved =
          switch (endpoint.kind()) {
            case ENERGY -> energy(network, source, handler, targets, allowance);
            case FLUID -> fluid(network, source, handler, targets, allowance);
            case ITEM -> item(network, source, handler, targets, allowance);
          };
      budget -= moved;
      if (moved > 0 || !config.scanIncomplete)
        config.retry.attempted(
            now, moved, endpoint.kind() == TransportRegistry.Kind.ITEM ? 20 : 1, config.retryCap);
      leader.sourceCursor = (sourceStart + i + 1) % Math.max(1, count);
      if (moved == 0) source.pipe.blockedReason = 3;
      if (moved > 0) {
        source.pipe.transferred(source.side, moved);
        config.cursor = (config.cursor + 1) & Integer.MAX_VALUE;
        leader.sourceCursor = (sourceStart + i + 1) % Math.max(1, count);
        leader.setChanged();
      }
    }
    int moved = initial - budget;
    if (moved > 0) for (var pipe : network.workers) pipe.spent += moved;
    if (moved > 0 && (network.lastPulse == Long.MIN_VALUE || now - network.lastPulse >= 4)) {
      network.lastPulse = now;
      for (var pipe : network.nodes) {
        pipe.activeUntil = now + 8;
        var state = pipe.getBlockState();
        if (!state.getValue(TransportBlock.ACTIVE))
          level.setBlock(
              pipe.getBlockPos(),
              state.setValue(TransportBlock.ACTIVE, true),
              Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (!level.getBlockTicks().hasScheduledTick(pipe.getBlockPos(), state.getBlock()))
          level.scheduleTick(pipe.getBlockPos(), state.getBlock(), 8);
      }
    }
  }

  private static List<End> destinations(Network network, End source) {
    var result = new ArrayList<End>();
    for (var end : network.outputs)
      if (end.pipe.inserting(end.side) && (source == null || !end.sameTarget(source)))
        result.add(end);
    if (source == null) return result;
    var config = source.pipe.side(source.side);
    if (config.routing == TransportSideConfig.Routing.NEAREST
        || config.routing == TransportSideConfig.Routing.FURTHEST) {
      var distances = distances(network, source.pipe);
      if (distances == null) return List.of();
      int sign = config.routing == TransportSideConfig.Routing.NEAREST ? 1 : -1;
      result.sort(
          Comparator.comparingInt((End end) -> -end.pipe.side(end.side).priority)
              .thenComparingInt(
                  end -> sign * distances.getOrDefault(end.pipe.getBlockPos(), MAX_NODES))
              .thenComparing(end -> end.pipe.getBlockPos())
              .thenComparingInt(end -> end.side.ordinal()));
    } else if (config.routing == TransportSideConfig.Routing.RANDOM
        || config.routing == TransportSideConfig.Routing.ROUND_ROBIN) {
      for (int start = 0; start < result.size(); ) {
        int end = start + 1;
        while (end < result.size()
            && result.get(end).pipe.side(result.get(end).side).priority
                == result.get(start).pipe.side(result.get(start).side).priority) end++;
        int rotation =
            config.routing == TransportSideConfig.Routing.RANDOM
                ? source.pipe.getLevel().random.nextInt(end - start)
                : config.cursor;
        Collections.rotate(result.subList(start, end), -Math.floorMod(rotation, end - start));
        start = end;
      }
    }
    return result;
  }

  private static Map<BlockPos, Integer> distances(Network network, TransportBlockEntity source) {
    BlockPos origin = source.getBlockPos();
    var cached = network.distances.get(origin);
    if (cached != null) return cached;
    long now = source.getLevel().getGameTime();
    if (network.distanceTick == now) return null;
    network.distanceTick = now;
    var result = new HashMap<BlockPos, Integer>();
    var queue = new ArrayDeque<BlockPos>();
    queue.add(origin);
    result.put(origin, 0);
    while (!queue.isEmpty()) {
      var pos = queue.remove();
      if (!source.getLevel().hasChunkAt(pos)
          || !(source.getLevel().getBlockEntity(pos) instanceof TransportBlockEntity pipe))
        continue;
      for (var side : Direction.values()) {
        var next = pos.relative(side);
        if (pipe.modes[side.ordinal()] == TransportBlockEntity.DISABLED
            || !network.members.contains(next)
            || result.containsKey(next)) continue;
        if (!source.getLevel().hasChunkAt(next)
            || !(source.getLevel().getBlockEntity(next) instanceof TransportBlockEntity other)
            || other.modes[side.getOpposite().ordinal()] == TransportBlockEntity.DISABLED) continue;
        result.put(next, result.get(pos) + 1);
        queue.add(next);
      }
    }
    if (network.distances.size() >= 64)
      network.distances.remove(network.distances.keySet().iterator().next());
    network.distances.put(origin, result);
    return result;
  }

  private static int energy(
      Network network, End source, Object handler, List<End> targets, int budget) {
    if (!(handler instanceof IEnergyStorage input) || !input.canExtract()) return 0;
    source.pipe.resetEnergyBudget();
    int available =
        input.extractEnergy(
            Math.min(budget, 5000 - source.pipe.energyIn[source.side.ordinal()]), true);
    if (available <= 0) return 0;
    int accepted = 0;
    for (var target : targets)
      if (target.handler() instanceof IEnergyStorage output
          && output != input
          && output.canReceive()) {
        target.pipe.resetEnergyBudget();
        accepted +=
            output.receiveEnergy(
                Math.min(available - accepted, 5000 - target.pipe.energyOut[target.side.ordinal()]),
                true);
        if (accepted >= available) break;
      }
    if (accepted < available && targets.stream().anyMatch(end -> end.pipe.side(end.side).disposal))
      accepted = available;
    if (accepted <= 0) return 0;
    if (network.invalid) return 0;
    source.pipe.pendingEnergy = input.extractEnergy(Math.min(available, accepted), false);
    source.pipe.energyIn[source.side.ordinal()] += source.pipe.pendingEnergy;
    source.pipe.setChanged();
    return deliver(
        network,
        source.pipe,
        targets,
        budget,
        network.roundRobin
            && source.pipe.side(source.side).routing == TransportSideConfig.Routing.LEGACY);
  }

  private static int fluid(
      Network network, End source, Object handler, List<End> targets, int budget) {
    if (!(handler instanceof IFluidHandler input)) return 0;
    Fluid lock = network.acceptedFluid();
    var config = source.pipe.side(source.side);
    int tanks = input.getTanks();
    int count = Math.min(tanks, Math.min(256, network.probesRemaining));
    int start = tanks == 0 ? 0 : Math.floorMod(config.scanCursor, tanks);
    config.scanIncomplete = count < tanks;
    for (int i = 0; i < count; i++) {
      int tank = (start + i) % tanks;
      config.scanCursor = (tank + 1) % tanks;
      network.probesRemaining--;
      FluidStack candidate = input.getFluidInTank(tank).copy();
      if (candidate.isEmpty()
          || !source.pipe.side(source.side).accepts(candidate)
          || (lock != Fluids.EMPTY && candidate.getFluid() != lock)) continue;
      candidate.setAmount(Math.min(candidate.getAmount(), budget));
      candidate = input.drain(candidate, FluidAction.SIMULATE);
      if (candidate.isEmpty()) continue;
      int accepted = 0;
      for (var target : targets)
        if (target.pipe.side(target.side).accepts(candidate)
            && target.handler() instanceof IFluidHandler output
            && output != input) {
          accepted +=
              output.fill(
                  candidate.copyWithAmount(candidate.getAmount() - accepted), FluidAction.SIMULATE);
          if (accepted >= candidate.getAmount()) break;
        }
      for (var target : targets)
        if (target.pipe.side(target.side).disposal
            && target.pipe.side(target.side).accepts(candidate)) {
          accepted = candidate.getAmount();
          break;
        }
      if (accepted <= 0) continue;
      if (network.invalid) return 0;
      source.pipe.pendingFluid =
          input.drain(
              candidate.copyWithAmount(Math.min(accepted, candidate.getAmount())),
              FluidAction.EXECUTE);
      source.pipe.setChanged();
      config.scannedSinceProgress = 0;
      return deliver(
          network,
          source.pipe,
          targets,
          budget,
          network.roundRobin
              && source.pipe.side(source.side).routing == TransportSideConfig.Routing.LEGACY);
    }
    config.scannedSinceProgress += count;
    config.scanIncomplete = config.scannedSinceProgress < tanks;
    if (!config.scanIncomplete) config.scannedSinceProgress = 0;
    return 0;
  }

  private static int item(
      Network network, End source, Object handler, List<End> targets, int budget) {
    if (!(handler instanceof IItemHandler input)) return 0;
    int total = 0;
    var config = source.pipe.side(source.side);
    boolean roundRobin = network.roundRobin && config.routing == TransportSideConfig.Routing.LEGACY;
    int slots = input.getSlots();
    int limit = Math.min(256, network.probesRemaining);
    int slot = slots == 0 ? 0 : Math.floorMod(config.scanCursor, slots);
    int visited = 0, probes = 0;
    while (visited < slots && probes < limit) {
      int currentSlot = slot;
      slot = (slot + 1) % slots;
      config.scanCursor = slot;
      visited++;
      probes++;
      network.probesRemaining--;
      ItemStack candidate = input.extractItem(currentSlot, roundRobin ? 1 : budget, true);
      if (candidate.isEmpty() || !source.pipe.accepts(source.side, candidate)) continue;
      int accepted = 0;
      for (var target : targets)
        if (target.pipe.accepts(target.side, candidate)
            && target.handler() instanceof IItemHandler output
            && output != input) {
          accepted +=
              candidate.getCount()
                  - accepted
                  - ItemHandlerHelper.insertItemStacked(
                          output, candidate.copyWithCount(candidate.getCount() - accepted), true)
                      .getCount();
          if (accepted >= candidate.getCount()) break;
        }
      for (var target : targets)
        if (target.pipe.side(target.side).disposal && target.pipe.accepts(target.side, candidate)) {
          accepted = candidate.getCount();
          break;
        }
      if (accepted <= 0) continue;
      if (network.invalid) return total;
      source.pipe.pendingItem =
          input.extractItem(currentSlot, Math.min(accepted, candidate.getCount()), false);
      source.pipe.setChanged();
      int moved =
          deliver(
              network,
              source.pipe,
              targets,
              budget,
              network.roundRobin
                  && source.pipe.side(source.side).routing == TransportSideConfig.Routing.LEGACY);
      total += moved;
      budget -= moved;
      config.scanCursor = 0;
      config.scannedSinceProgress = 0;
      if (!roundRobin || moved == 0 || budget == 0 || !source.pipe.pendingItem.isEmpty())
        return total;
      slot = 0;
      visited = 0;
    }
    config.scannedSinceProgress += probes;
    config.scanIncomplete = total == 0 && config.scannedSinceProgress < slots;
    if (!config.scanIncomplete) {
      config.scanCursor = 0;
      config.scannedSinceProgress = 0;
    }
    return total;
  }

  private static int deliver(
      Network network, TransportBlockEntity pipe, List<End> targets, int budget) {
    return deliver(network, pipe, targets, budget, network.roundRobin);
  }

  private static int deliver(
      Network network, TransportBlockEntity pipe, List<End> targets, int budget, boolean rotate) {
    if (targets.isEmpty() || network.invalid) return 0;
    var leader = network.nodes.getFirst();
    int start = 0;
    if (rotate) {
      targets = new ArrayList<>(targets);
      for (int group = 0; group < targets.size(); ) {
        int end = group + 1;
        while (end < targets.size()
            && targets.get(end).pipe.side(targets.get(end).side).priority
                == targets.get(group).pipe.side(targets.get(group).side).priority) end++;
        Collections.rotate(targets.subList(group, end), -Math.floorMod(leader.cursor, end - group));
        group = end;
      }
    }
    int moved = 0;
    for (int i = 0; i < targets.size() && moved < budget; i++) {
      var target = targets.get((start + i) % targets.size());
      if (network.invalid) break;
      if (target.mode() != TransportBlockEntity.INSERT || !target.pipe.inserting(target.side))
        continue;
      Object handler = target.handler();
      int amount = 0;
      if (!pipe.pendingItem.isEmpty()
          && target.pipe.accepts(target.side, pipe.pendingItem)
          && handler instanceof IItemHandler output) {
        ItemStack offer =
            pipe.pendingItem.copyWithCount(Math.min(budget - moved, pipe.pendingItem.getCount()));
        amount =
            offer.getCount() - ItemHandlerHelper.insertItemStacked(output, offer, false).getCount();
        pipe.pendingItem.shrink(amount);
      } else if (!pipe.pendingFluid.isEmpty()
          && target.pipe.side(target.side).accepts(pipe.pendingFluid)
          && handler instanceof IFluidHandler output) {
        amount =
            output.fill(
                pipe.pendingFluid.copyWithAmount(
                    Math.min(budget - moved, pipe.pendingFluid.getAmount())),
                FluidAction.EXECUTE);
        pipe.pendingFluid.shrink(amount);
      } else if (pipe.pendingEnergy > 0 && handler instanceof IEnergyStorage output) {
        target.pipe.resetEnergyBudget();
        amount =
            output.receiveEnergy(
                Math.min(
                    Math.min(budget - moved, pipe.pendingEnergy),
                    5000 - target.pipe.energyOut[target.side.ordinal()]),
                false);
        pipe.pendingEnergy -= amount;
        target.pipe.energyOut[target.side.ordinal()] += amount;
      }
      if (amount > 0) {
        moved += amount;
        target.pipe.transferred(target.side, amount);
        leader.cursor = (leader.cursor + i + 1) % targets.size();
        leader.setChanged();
        pipe.setChanged();
      }
    }
    // Disposal is a final pass, irrespective of destination priority.
    for (var target : targets) {
      if (network.invalid) break;
      if (moved >= budget
          || !target.pipe.side(target.side).disposal
          || !target.pipe.inserting(target.side)) continue;
      int amount = 0;
      if (!pipe.pendingItem.isEmpty() && target.pipe.accepts(target.side, pipe.pendingItem)) {
        amount = Math.min(budget - moved, pipe.pendingItem.getCount());
        pipe.pendingItem.shrink(amount);
      } else if (!pipe.pendingFluid.isEmpty()
          && target.pipe.side(target.side).accepts(pipe.pendingFluid)) {
        amount = Math.min(budget - moved, pipe.pendingFluid.getAmount());
        pipe.pendingFluid.shrink(amount);
      } else if (pipe.pendingEnergy > 0) {
        target.pipe.resetEnergyBudget();
        amount =
            Math.min(
                Math.min(budget - moved, pipe.pendingEnergy),
                5000 - target.pipe.energyOut[target.side.ordinal()]);
        pipe.pendingEnergy -= amount;
        target.pipe.energyOut[target.side.ordinal()] += amount;
      }
      if (amount > 0) {
        moved += amount;
        pipe.setChanged();
      }
    }
    return moved;
  }

  public static void voidFluid(TransportBlockEntity pipe, Player player) {
    Network network = network(pipe);
    if (network.oversized) {
      message(player, Component.translatable("message.adv_rocketry.transport.network_too_large"));
      return;
    }
    long amount = 0;
    for (var node : network.nodes) {
      amount += node.pendingFluid.getAmount();
      node.pendingFluid = FluidStack.EMPTY;
      node.setChanged();
      node.refreshConnections();
    }
    message(player, Texts.translate("message.adv_rocketry.transport.voided", amount));
  }

  public static boolean lockFluid(TransportBlockEntity pipe, FluidStack fluid, Player player) {
    Network network = network(pipe);
    if (network.oversized) {
      message(player, Component.translatable("message.adv_rocketry.transport.network_too_large"));
      return false;
    }
    for (var node : network.nodes)
      if (!node.pendingFluid.isEmpty() && node.pendingFluid.getFluid() != fluid.getFluid()) {
        message(
            player, Component.translatable("message.adv_rocketry.transport.void_before_changing"));
        return false;
      }
    for (var node : network.nodes) {
      node.lock = fluid.getFluid();
      node.setChanged();
    }
    invalidate(pipe, network);
    message(
        player, Texts.translate("message.adv_rocketry.transport.fluid_lock", fluid.getHoverName()));
    return true;
  }

  public static void unlockFluid(TransportBlockEntity pipe) {
    Network network = network(pipe);
    if (!network.oversized)
      for (var node : network.nodes) {
        node.lock = Fluids.EMPTY;
        node.setChanged();
      }
    invalidate(pipe, network);
  }

  public static void roundRobin(TransportBlockEntity pipe) {
    Network network = network(pipe);
    if (!network.oversized) {
      boolean next = !network.roundRobin;
      network.roundRobin = next;
      for (var node : network.nodes) {
        node.roundRobin = next;
        node.setChanged();
      }
    }
  }

  public record StorageAccess(TransportBlockEntity pipe, Direction side, IItemHandler handler) {
    public boolean accepts(ItemStack stack) {
      return pipe.accepts(side, stack);
    }
  }

  /** Fresh sided access; callers must still validate each actual handler result. */
  public static List<StorageAccess> storage(TransportBlockEntity pipe, boolean insert) {
    if (pipe.kind() != TransportRegistry.Kind.ITEM) return List.of();
    var network = network(pipe);
    if (network.oversized) return List.of();
    var result = new ArrayList<StorageAccess>();
    for (var end : insert ? network.outputs : network.ends) {
      if (insert
          ? end.mode() != TransportBlockEntity.INSERT || !end.pipe.inserting(end.side)
          : end.mode() != TransportBlockEntity.EXTRACT || !end.pipe.extracting(end.side)) continue;
      if (!end.pipe.getLevel().hasChunkAt(end.target) || end.pipe.isRemoved()) continue;
      if (end.handler() instanceof IItemHandler handler)
        result.add(new StorageAccess(end.pipe, end.side, handler));
    }
    return result;
  }

  private static void message(Player player, Component text) {
    if (player != null) player.displayClientMessage(text, true);
  }
}

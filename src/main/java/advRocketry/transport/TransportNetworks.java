// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import advRocketry.util.Texts;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

  public static void capabilityChanged(TransportBlockEntity pipe) {
    invalidate(pipe.getLevel(), pipe.getBlockPos());
    DIRTY.computeIfAbsent(pipe.getLevel(), level -> new HashSet<>()).add(pipe.getBlockPos());
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
    final List<TransportBlockEntity> workers = new ArrayList<>();
    Fluid configuredFluid = Fluids.EMPTY;
    boolean conflictingLocks;
    long lastPulse = Long.MIN_VALUE;
    public boolean oversized;
    public boolean roundRobin;
    long lastTick = Long.MIN_VALUE;

    public boolean conflicted() {
      if (conflictingLocks) return true;
      Fluid known = configuredFluid;
      for (var pipe : workers) {
        for (Fluid fluid : new Fluid[] {pipe.pendingFluid.getFluid()})
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

  private record End(TransportBlockEntity pipe, Direction side) {
    BlockPos target() {
      return pipe.getBlockPos().relative(side);
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
    }
  }

  public static void invalidate(Level level) {
    if (level != null && !level.isClientSide) CACHE.remove(level);
  }

  public static void invalidate(Level level, BlockPos pos) {
    if (level == null || level.isClientSide) return;
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
    if (network != null) for (BlockPos member : network.members) cache.remove(member, network);
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
    var cache = CACHE.computeIfAbsent(level, key -> new HashMap<>());
    Network cached = cache.get(start.getBlockPos());
    if (cached != null) return cached;
    Network network = new Network();
    var seen = network.members;
    var queue = new ArrayDeque<TransportBlockEntity>();
    queue.add(start);
    seen.add(start.getBlockPos());
    while (!queue.isEmpty()) {
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
              && other.modes[side.getOpposite().ordinal()] != TransportBlockEntity.DISABLED
              && seen.add(next)) queue.add(other);
        } else if (!network.oversized && capability(pipe, side) != null)
          network.ends.add(new End(pipe, side));
      }
    }
    network.nodes.sort(Comparator.comparing(TransportBlockEntity::getBlockPos));
    var endpointPositions = new HashSet<BlockPos>();
    for (var end : network.ends) endpointPositions.add(end.pipe.getBlockPos());
    for (var pipe : network.nodes) {
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
    if (network.lastTick == now || network.oversized || network.conflicted()) return;
    network.lastTick = now;
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
      int moved = deliver(network, pipe, null, budget);
      budget -= moved;
      pipe.refreshConnections();
    }
    int count = network.ends.size();
    int sourceStart = Math.floorMod(leader.sourceCursor, Math.max(1, count));
    for (int i = 0; i < count && budget > 0; i++) {
      End source = network.ends.get((sourceStart + i) % count);
      if (source.mode() != TransportBlockEntity.EXTRACT || !source.pipe.extracting()) continue;
      if (!source.pipe.pendingItem.isEmpty()
          || !source.pipe.pendingFluid.isEmpty()
          || source.pipe.pendingEnergy > 0) continue;
      Object handler = source.handler();
      int moved =
          switch (endpoint.kind()) {
            case ENERGY -> energy(network, source, handler, budget);
            case FLUID -> fluid(network, source, handler, budget);
            case ITEM -> item(network, source, handler, budget);
          };
      budget -= moved;
      if (moved > 0) {
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

  private static List<End> destinations(Network network, TransportBlockEntity pipe, End source) {
    var result = new ArrayList<End>();
    for (var end : network.ends)
      if (end.mode() == TransportBlockEntity.INSERT && (source == null || !end.sameTarget(source)))
        result.add(end);
    return result;
  }

  private static int energy(Network network, End source, Object handler, int budget) {
    if (!(handler instanceof IEnergyStorage input) || !input.canExtract()) return 0;
    source.pipe.resetEnergyBudget();
    int available =
        input.extractEnergy(
            Math.min(budget, 5000 - source.pipe.energyIn[source.side.ordinal()]), true);
    if (available <= 0) return 0;
    int accepted = 0;
    for (var target : destinations(network, source.pipe, source))
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
    if (accepted <= 0) return 0;
    source.pipe.pendingEnergy = input.extractEnergy(Math.min(available, accepted), false);
    source.pipe.energyIn[source.side.ordinal()] += source.pipe.pendingEnergy;
    source.pipe.setChanged();
    return deliver(network, source.pipe, source, budget);
  }

  private static int fluid(Network network, End source, Object handler, int budget) {
    if (!(handler instanceof IFluidHandler input)) return 0;
    Fluid lock = network.acceptedFluid();
    for (int tank = 0; tank < input.getTanks(); tank++) {
      FluidStack candidate = input.getFluidInTank(tank).copy();
      if (candidate.isEmpty() || (lock != Fluids.EMPTY && candidate.getFluid() != lock)) continue;
      candidate.setAmount(Math.min(candidate.getAmount(), budget));
      candidate = input.drain(candidate, FluidAction.SIMULATE);
      if (candidate.isEmpty()) continue;
      int accepted = 0;
      for (var target : destinations(network, source.pipe, source))
        if (target.handler() instanceof IFluidHandler output && output != input) {
          accepted +=
              output.fill(
                  candidate.copyWithAmount(candidate.getAmount() - accepted), FluidAction.SIMULATE);
          if (accepted >= candidate.getAmount()) break;
        }
      if (accepted <= 0) continue;
      source.pipe.pendingFluid =
          input.drain(
              candidate.copyWithAmount(Math.min(accepted, candidate.getAmount())),
              FluidAction.EXECUTE);
      source.pipe.setChanged();
      return deliver(network, source.pipe, source, budget);
    }
    return 0;
  }

  private static int item(Network network, End source, Object handler, int budget) {
    if (!(handler instanceof IItemHandler input)) return 0;
    var leader = network.nodes.getFirst();
    for (int slot = 0; slot < input.getSlots(); slot++) {
      ItemStack candidate = input.extractItem(slot, network.roundRobin ? 1 : budget, true);
      if (candidate.isEmpty() || !source.pipe.accepts(candidate)) continue;
      int accepted = 0;
      for (var target : destinations(network, source.pipe, source))
        if (target.pipe.accepts(candidate)
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
      if (accepted <= 0) continue;
      source.pipe.pendingItem =
          input.extractItem(slot, Math.min(accepted, candidate.getCount()), false);
      source.pipe.setChanged();
      int moved = deliver(network, source.pipe, source, budget);
      if (network.roundRobin && moved > 0 && moved < budget && source.pipe.pendingItem.isEmpty())
        return moved + item(network, source, handler, budget - moved);
      return moved;
    }
    return 0;
  }

  private static int deliver(Network network, TransportBlockEntity pipe, End source, int budget) {
    var targets = destinations(network, pipe, source);
    if (targets.isEmpty()) return 0;
    var leader = network.nodes.getFirst();
    int start = network.roundRobin ? Math.floorMod(leader.cursor, targets.size()) : 0;
    int moved = 0;
    for (int i = 0; i < targets.size() && moved < budget; i++) {
      var target = targets.get((start + i) % targets.size());
      Object handler = target.handler();
      int amount = 0;
      if (!pipe.pendingItem.isEmpty()
          && target.pipe.accepts(pipe.pendingItem)
          && handler instanceof IItemHandler output) {
        ItemStack offer =
            pipe.pendingItem.copyWithCount(Math.min(budget - moved, pipe.pendingItem.getCount()));
        amount =
            offer.getCount() - ItemHandlerHelper.insertItemStacked(output, offer, false).getCount();
        pipe.pendingItem.shrink(amount);
      } else if (!pipe.pendingFluid.isEmpty() && handler instanceof IFluidHandler output) {
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
        leader.cursor = (start + i + 1) % targets.size();
        leader.setChanged();
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

  private static void message(Player player, Component text) {
    if (player != null) player.displayClientMessage(text, true);
  }
}

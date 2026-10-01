// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Native upward gas flow for the original negative-density fluid registrations. */
public final class RisingGasFluid extends BaseFlowingFluid {
  private final boolean source;

  public RisingGasFluid(Properties properties, boolean source) {
    super(properties);
    this.source = source;
    registerDefaultState(stateDefinition.any().setValue(LEVEL, 8).setValue(FALLING, false));
  }

  @Override
  protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
    super.createFluidStateDefinition(builder);
    builder.add(LEVEL);
  }

  @Override
  public boolean isSource(FluidState state) {
    return source;
  }

  @Override
  public int getAmount(FluidState state) {
    return source ? 8 : state.getValue(LEVEL);
  }

  private boolean connected(Level level, BlockPos pos, Direction side) {
    BlockPos other = pos.relative(side);
    if (!level.hasChunkAt(other) || level.isOutsideBuildHeight(other)) return false;
    return !Shapes.mergedFaceOccludes(
        level.getBlockState(pos).getCollisionShape(level, pos),
        level.getBlockState(other).getCollisionShape(level, other),
        side);
  }

  private boolean same(BlockGetter level, BlockPos pos) {
    return isSame(level.getFluidState(pos).getType());
  }

  private boolean canEnter(Level level, BlockPos from, Direction side) {
    BlockPos to = from.relative(side);
    if (!level.hasChunkAt(to) || level.isOutsideBuildHeight(to)) return false;
    FluidState existing = level.getFluidState(to);
    if (isSame(existing.getType())) return !existing.isSource() && connected(level, from, side);
    return canSpreadTo(
        level,
        from,
        level.getBlockState(from),
        side,
        to,
        level.getBlockState(to),
        existing,
        getSource());
  }

  private FluidState suppliedState(Level level, BlockPos pos) {
    if (connected(level, pos, Direction.DOWN) && same(level, pos.below()))
      return getFlowing(8, true);
    int supply = 0;
    for (Direction side : Direction.Plane.HORIZONTAL) {
      if (!connected(level, pos, side)) continue;
      FluidState neighbor = level.getFluidState(pos.relative(side));
      if (isSame(neighbor.getType())) supply = Math.max(supply, neighbor.getAmount() - 1);
    }
    return supply > 0 ? getFlowing(supply, false) : null;
  }

  private void offer(Level level, BlockPos from, Direction side, FluidState incoming) {
    if (!canEnter(level, from, side)) return;
    BlockPos to = from.relative(side);
    FluidState existing = level.getFluidState(to);
    if (isSame(existing.getType())) {
      if (existing.getAmount() >= incoming.getAmount()) return;
      level.setBlock(to, incoming.createLegacyBlock(), 3);
    } else spreadTo(level, to, level.getBlockState(to), side, incoming);
  }

  private record Route(BlockPos pos, int distance) {}

  /** Search nearby ceiling openings without loading chunks or revisiting cells. */
  private int escapeDistance(Level level, BlockPos start, BlockPos origin) {
    var visited = new HashSet<BlockPos>();
    var queue = new ArrayDeque<Route>();
    visited.add(origin);
    visited.add(start);
    queue.add(new Route(start, 0));
    while (!queue.isEmpty()) {
      Route route = queue.removeFirst();
      if (canEnter(level, route.pos(), Direction.UP)) return route.distance();
      if (route.distance() == 4) continue;
      for (Direction side : Direction.Plane.HORIZONTAL) {
        BlockPos next = route.pos().relative(side);
        if (canEnter(level, route.pos(), side) && visited.add(next))
          queue.addLast(new Route(next, route.distance() + 1));
      }
    }
    return Integer.MAX_VALUE;
  }

  @Override
  public void tick(Level level, BlockPos pos, FluidState state) {
    if (!state.isSource()) {
      FluidState supplied = suppliedState(level, pos);
      if (supplied == null) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return;
      }
      if (!supplied.equals(state)) {
        level.setBlock(pos, supplied.createLegacyBlock(), 3);
        level.scheduleTick(pos, supplied.getType(), getTickDelay(level));
      }
      state = supplied;
    }
    if (!same(level, pos.above()) && canEnter(level, pos, Direction.UP)) {
      offer(level, pos, Direction.UP, getFlowing(8, true));
      return;
    }
    if (!state.isSource() && same(level, pos.above())) return;
    int amount = state.getValue(FALLING) ? 7 : state.getAmount() - 1;
    if (amount <= 0) return;
    var routes = new EnumMap<Direction, Integer>(Direction.class);
    int shortest = Integer.MAX_VALUE;
    for (Direction side : Direction.Plane.HORIZONTAL) {
      if (!canEnter(level, pos, side)) continue;
      int distance = escapeDistance(level, pos.relative(side), pos);
      routes.put(side, distance);
      shortest = Math.min(shortest, distance);
    }
    for (var route : routes.entrySet())
      if (route.getValue() == shortest)
        offer(level, pos, route.getKey(), getFlowing(amount, false));
  }

  @Override
  public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
    Vec3 flow = super.getFlow(level, pos, state);
    return new Vec3(flow.x, -flow.y, flow.z);
  }
}

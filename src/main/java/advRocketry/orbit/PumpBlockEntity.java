// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.util.MachineEnergy;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** One-thousand-FE pump, sixteen-bucket buffer and bounded upward flood search. */
public final class PumpBlockEntity extends BlockEntity {
  public final MachineEnergy energy = MachineEnergy.consumer(1000, this::setChanged);
  public final FluidTank tank =
      new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };
  private final ArrayDeque<BlockPos> cache = new ArrayDeque<>();

  public PumpBlockEntity(BlockPos pos, BlockState state) {
    super(OrbitalRegistry.PUMP_ENTITY.get(), pos, state);
  }

  public void tick() {
    if (!(level instanceof ServerLevel server)) return;
    for (Direction direction : Direction.values()) {
      if (tank.isEmpty()) break;
      IFluidHandler output =
          server.getCapability(
              Capabilities.FluidHandler.BLOCK,
              worldPosition.relative(direction),
              direction.getOpposite());
      if (output == null) continue;
      FluidStack offered = tank.getFluid().copyWithAmount(Math.min(1000, tank.getFluidAmount()));
      int accepted = output.fill(offered, IFluidHandler.FluidAction.SIMULATE);
      if (accepted > 0) {
        FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
      }
    }
    int frequency = energy.getEnergyStored() > 500 ? 1 : 10;
    if (server.getGameTime() % frequency != 0
        || energy.consume(100, true) < 100
        || tank.getCapacity() - tank.getFluidAmount() < 1000) return;
    BlockPos source = nextSource(server);
    if (source == null) return;
    BlockState state = server.getBlockState(source);
    if (!(state.getBlock() instanceof BucketPickup pickup)) return;
    Fluid fluid = server.getFluidState(source).getType();
    if (tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE) < 1000) return;
    FluidStack taken =
        FluidUtil.getFluidContained(pickup.pickupBlock(null, server, source, state))
            .orElse(FluidStack.EMPTY);
    if (taken.isEmpty()) return;
    tank.fill(taken, IFluidHandler.FluidAction.EXECUTE);
    energy.consume(100, false);
    setChanged();
  }

  private BlockPos nextSource(ServerLevel server) {
    while (!cache.isEmpty()) {
      BlockPos next = cache.removeFirst();
      if (server.hasChunkAt(next)
          && server.getFluidState(next).isSource()
          && server.getBlockState(next).getBlock() instanceof BucketPickup
          && (tank.isEmpty()
              || tank.getFluid().getFluid().isSame(server.getFluidState(next).getType())))
        return next;
    }
    BlockPos first = worldPosition.below();
    while (first.getY() > server.getMinBuildHeight()
        && server.hasChunkAt(first)
        && server.isEmptyBlock(first)) first = first.below();
    if (!server.hasChunkAt(first) || server.getFluidState(first).isEmpty()) return null;
    Fluid fluid = server.getFluidState(first).getType();
    if (!tank.isEmpty() && !tank.getFluid().getFluid().isSame(fluid)) return null;
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    Set<BlockPos> visited = new HashSet<>();
    queue.add(first);
    while (!queue.isEmpty() && visited.size() < 4096) {
      BlockPos pos = queue.removeFirst();
      if (pos.distSqr(first) > 64 * 64 || !visited.add(pos) || !server.hasChunkAt(pos)) continue;
      var state = server.getFluidState(pos);
      if (state.isEmpty() || !state.getType().isSame(fluid)) continue;
      if (state.isSource() && server.getBlockState(pos).getBlock() instanceof BucketPickup)
        cache.addFirst(pos);
      queue.add(pos.west());
      queue.add(pos.east());
      queue.add(pos.north());
      queue.add(pos.south());
      queue.add(pos.above());
    }
    return cache.isEmpty() ? null : cache.removeFirst();
  }

  public IFluidHandler outputTank() {
    return new IFluidHandler() {
      @Override
      public int getTanks() {
        return 1;
      }

      @Override
      public FluidStack getFluidInTank(int slot) {
        return tank.getFluid();
      }

      @Override
      public int getTankCapacity(int slot) {
        return tank.getCapacity();
      }

      @Override
      public boolean isFluidValid(int slot, FluidStack stack) {
        return false;
      }

      @Override
      public int fill(FluidStack stack, FluidAction action) {
        return 0;
      }

      @Override
      public FluidStack drain(FluidStack stack, FluidAction action) {
        return tank.drain(stack, action);
      }

      @Override
      public FluidStack drain(int maximum, FluidAction action) {
        return tank.drain(maximum, action);
      }
    };
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("energy", energy.serializeNBT(registries));
    tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
    if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
  }
}

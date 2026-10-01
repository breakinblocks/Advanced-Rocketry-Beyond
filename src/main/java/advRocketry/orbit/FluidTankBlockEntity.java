// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Bottom-up fill and top-down drain across a vertical stack of matching tanks. */
public final class FluidTankBlockEntity extends BlockEntity {
  public final FluidTank tank =
      new FluidTank(AdvancedRocketryConfig.blockTankCapacity()) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };
  private final IFluidHandler handler =
      new IFluidHandler() {
        @Override
        public int getTanks() {
          return 1;
        }

        @Override
        public FluidStack getFluidInTank(int slot) {
          return tank.getFluid().copy();
        }

        @Override
        public int getTankCapacity(int slot) {
          return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int slot, FluidStack stack) {
          return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
          return fillStack(stack, action);
        }

        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
          return drainStack(stack, action);
        }

        @Override
        public FluidStack drain(int maximum, FluidAction action) {
          return tank.isEmpty()
              ? FluidStack.EMPTY
              : drainStack(tank.getFluid().copyWithAmount(maximum), action);
        }
      };

  public FluidTankBlockEntity(BlockPos pos, BlockState state) {
    super(OrbitalRegistry.FLUID_TANK_ENTITY.get(), pos, state);
  }

  public IFluidHandler handler() {
    return handler;
  }

  private List<FluidTankBlockEntity> column() {
    List<FluidTankBlockEntity> tanks = new ArrayList<>();
    if (level == null) return tanks;
    BlockPos bottom = worldPosition;
    for (int step = 0; step < 64; step++) {
      BlockPos next = bottom.below();
      if (!level.hasChunkAt(next) || !(level.getBlockEntity(next) instanceof FluidTankBlockEntity))
        break;
      bottom = next;
    }
    for (int step = 0; step < 64; step++) {
      BlockPos next = bottom.above(step);
      if (!level.hasChunkAt(next)
          || !(level.getBlockEntity(next) instanceof FluidTankBlockEntity tank)) break;
      tanks.add(tank);
    }
    return tanks;
  }

  private int fillStack(FluidStack stack, IFluidHandler.FluidAction action) {
    if (stack.isEmpty()) return 0;
    int remaining = stack.getAmount();
    for (FluidTankBlockEntity cell : column()) {
      if (remaining <= 0) break;
      if (!cell.tank.isEmpty() && !cell.tank.getFluid().is(stack.getFluid())) break;
      remaining -= cell.tank.fill(stack.copyWithAmount(remaining), action);
    }
    return stack.getAmount() - remaining;
  }

  private FluidStack drainStack(FluidStack request, IFluidHandler.FluidAction action) {
    if (request.isEmpty() || tank.isEmpty() || !tank.getFluid().is(request.getFluid()))
      return FluidStack.EMPTY;
    List<FluidTankBlockEntity> tanks = column();
    int index = tanks.indexOf(this), remaining = request.getAmount();
    if (index < 0) return FluidStack.EMPTY;
    for (int above = index + 1; above < tanks.size(); above++)
      if (tanks.get(above).tank.isEmpty()
          || !tanks.get(above).tank.getFluid().is(request.getFluid())) {
        tanks = tanks.subList(0, above);
        break;
      }
    for (int step = tanks.size() - 1; step >= index && remaining > 0; step--)
      remaining -=
          tanks.get(step).tank.drain(request.copyWithAmount(remaining), action).getAmount();
    return request.copyWithAmount(request.getAmount() - remaining);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("fluid", tank.writeToNBT(registries, new CompoundTag()));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    tank.readFromNBT(registries, tag.getCompound("fluid"));
  }

  @Override
  protected void applyImplicitComponents(DataComponentInput input) {
    super.applyImplicitComponents(input);
    tank.setFluid(
        input.getOrDefault(ModComponents.TANK_CONTENT.get(), SimpleFluidContent.EMPTY).copy());
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    if (!tank.isEmpty())
      components.set(ModComponents.TANK_CONTENT.get(), SimpleFluidContent.copyOf(tank.getFluid()));
  }

  @Override
  public void removeComponentsFromTag(CompoundTag tag) {
    tag.remove("fluid");
  }
}

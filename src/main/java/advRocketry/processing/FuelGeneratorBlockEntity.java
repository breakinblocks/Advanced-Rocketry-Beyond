package advRocketry.processing;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Consumes a recipe batch once, then preserves its remaining burn across saves and reloads. */
public final class FuelGeneratorBlockEntity extends BlockEntity implements MenuProvider {
  public static final int CAPACITY = 1000000;
  public static final int TANK_CAPACITY = 16000;
  private int energy;
  private int burnTime;
  private int burnDuration;
  private int fePerTick;
  private final NeighbourEnergy neighbours = new NeighbourEnergy(this);
  public final FluidTank tank =
      new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };
  public final IEnergyStorage output =
      new IEnergyStorage() {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
          return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
          int extracted = Math.min(Math.max(0, amount), energy);
          if (!simulate && extracted > 0) {
            energy -= extracted;
            setChanged();
          }
          return extracted;
        }

        @Override
        public int getEnergyStored() {
          return energy;
        }

        @Override
        public int getMaxEnergyStored() {
          return CAPACITY;
        }

        @Override
        public boolean canExtract() {
          return true;
        }

        @Override
        public boolean canReceive() {
          return false;
        }
      };

  public FuelGeneratorBlockEntity(BlockPos pos, BlockState state) {
    super(MachinePorts.FUEL_GENERATOR_ENTITY.get(), pos, state);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    neighbours.load();
  }

  public int energyStored() {
    return energy;
  }

  public int burnTime() {
    return burnTime;
  }

  public int burnDuration() {
    return burnDuration;
  }

  public int fePerTick() {
    return burnTime > 0 ? fePerTick : 0;
  }

  public void tick() {
    if (level == null || level.isClientSide) return;
    if (burnTime == 0 && !tank.isEmpty()) {
      // Stable selection for overlapping datapack recipes. No cache survives a recipe reload.
      var recipe =
          level
              .getRecipeManager()
              .getAllRecipesFor(ProcessingRegistry.LIQUID_FUEL_TYPE.get())
              .stream()
              .filter(holder -> holder.value().fuel().test(tank.getFluid()))
              .min(Comparator.comparing(holder -> holder.id()))
              .orElse(null);
      if (recipe != null && energy <= CAPACITY - recipe.value().fePerTick()) {
        var fuel = recipe.value();
        tank.drain(fuel.fuel().amount(), FluidAction.EXECUTE);
        burnTime = burnDuration = fuel.ticks();
        fePerTick = fuel.fePerTick();
        setChanged();
      }
    }
    if (burnTime > 0 && energy <= CAPACITY - fePerTick) {
      energy += fePerTick;
      burnTime--;
      setChanged();
    }
    neighbours.push(output);
    if (level.getGameTime() % 20 == 0)
      level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.adv_rocketry.fuel_generator");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new FuelGeneratorMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("energy", energy);
    tag.putInt("burn_time", burnTime);
    tag.putInt("burn_duration", burnDuration);
    tag.putInt("fe_per_tick", fePerTick);
    tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    energy = Math.clamp(tag.getInt("energy"), 0, CAPACITY);
    burnDuration = Math.clamp(tag.getInt("burn_duration"), 0, 1000000);
    burnTime = Math.clamp(tag.getInt("burn_time"), 0, burnDuration);
    fePerTick = Math.clamp(tag.getInt("fe_per_tick"), 0, CAPACITY);
    if (fePerTick == 0) burnTime = 0;
    tank.readFromNBT(registries, tag.getCompound("tank"));
    if (tank.getFluidAmount() > TANK_CAPACITY) tank.getFluid().setAmount(TANK_CAPACITY);
  }
}

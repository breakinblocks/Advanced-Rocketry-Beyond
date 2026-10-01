package advRocketry.util;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.energy.EnergyStorage;

public class MachineEnergy extends EnergyStorage {
  private final Runnable changed;

  public MachineEnergy(int capacity, int maxReceive, int maxExtract, Runnable changed) {
    super(capacity, maxReceive, maxExtract);
    this.changed = changed;
  }

  public static MachineEnergy consumer(int capacity, Runnable changed) {
    return new MachineEnergy(capacity, capacity, 0, changed);
  }

  public static MachineEnergy generator(int capacity, Runnable changed) {
    return new MachineEnergy(capacity, 0, capacity, changed);
  }

  public static MachineEnergy buffer(int capacity, Runnable changed) {
    return new MachineEnergy(capacity, capacity, capacity, changed);
  }

  @Override
  public int receiveEnergy(int amount, boolean simulate) {
    int received = super.receiveEnergy(amount, simulate);
    if (!simulate && received > 0) changed.run();
    return received;
  }

  @Override
  public int extractEnergy(int amount, boolean simulate) {
    int extracted = super.extractEnergy(amount, simulate);
    if (!simulate && extracted > 0) changed.run();
    return extracted;
  }

  public int consume(int amount, boolean simulate) {
    int taken = Math.min(energy, Math.max(0, amount));
    if (!simulate && taken > 0) {
      energy -= taken;
      changed.run();
    }
    return taken;
  }

  public int generate(int amount, boolean simulate) {
    int added = Math.min(capacity - energy, Math.max(0, amount));
    if (!simulate && added > 0) {
      energy += added;
      changed.run();
    }
    return added;
  }

  public void setEnergy(int value) {
    int clamped = Mth.clamp(value, 0, capacity);
    if (clamped == energy) return;
    energy = clamped;
    changed.run();
  }
}

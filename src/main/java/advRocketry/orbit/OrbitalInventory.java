// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketStructure;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Orbital inventories, including the builder's seven slots inside its satellite chassis. */
public final class OrbitalInventory extends ItemStackHandler {
  private final OrbitalBlockEntity owner;

  public static final int SIZE = 12;

  public OrbitalInventory(OrbitalBlockEntity owner) {
    super(SIZE);
    this.owner = owner;
  }

  private static ItemStackHandler hatchInventory(
      RocketStructure.Cell hatch, HolderLookup.Provider registries) {
    CompoundTag saved = hatch.data().getCompound("inventory").copy();
    saved.putInt("Size", SIZE);
    ItemStackHandler inventory = new ItemStackHandler(SIZE);
    inventory.deserializeNBT(registries, saved);
    return inventory;
  }

  public static ItemStack hatchPayload(
      RocketStructure.Cell hatch, HolderLookup.Provider registries) {
    return hatchInventory(hatch, registries).getStackInSlot(0);
  }

  public static void setHatchPayload(
      RocketStructure.Cell hatch, HolderLookup.Provider registries, ItemStack payload) {
    ItemStackHandler inventory = hatchInventory(hatch, registries);
    inventory.setStackInSlot(0, payload);
    hatch.data().put("inventory", inventory.serializeNBT(registries));
  }

  private boolean hasChassis() {
    ItemStack chassis = super.getStackInSlot(7);
    return owner.builder()
        && chassis.getItem() instanceof SatelliteItem
        && !SatelliteItem.assembled(chassis);
  }

  private boolean embedded(int slot) {
    return slot >= 0 && slot < 7 && hasChassis() && super.getStackInSlot(slot).isEmpty();
  }

  private ComponentItemHandler modules() {
    return SatelliteItem.modules(super.getStackInSlot(7));
  }

  /** Only physical stacks are dropped; embedded modules travel inside their chassis. */
  public ItemStack storedStackInSlot(int slot) {
    return super.getStackInSlot(slot);
  }

  public boolean hasLooseModules() {
    if (!owner.builder()) return false;
    for (int slot = 0; slot < 7; slot++) if (!super.getStackInSlot(slot).isEmpty()) return true;
    return false;
  }

  private void moveLooseModulesIntoChassis() {
    if (!hasChassis()) return;
    ComponentItemHandler modules = modules();
    for (int slot = 0; slot < 7; slot++) {
      ItemStack loose = super.getStackInSlot(slot);
      if (loose.isEmpty()
          || !modules.getStackInSlot(slot).isEmpty()
          || !modules.isItemValid(slot, loose)) continue;
      ItemStack remainder = modules.insertItem(slot, loose, false);
      stacks.set(slot, remainder);
    }
  }

  @Override
  protected void onLoad() {
    moveLooseModulesIntoChassis();
  }

  @Override
  protected void onContentsChanged(int slot) {
    if (slot == 7) moveLooseModulesIntoChassis();
    owner.setChanged();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return embedded(slot) ? modules().getStackInSlot(slot) : super.getStackInSlot(slot);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    if (embedded(slot)) {
      modules().setStackInSlot(slot, stack);
      owner.setChanged();
    } else if (!owner.builder() || slot >= 7 || !super.getStackInSlot(slot).isEmpty()) {
      super.setStackInSlot(slot, stack);
    }
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (!embedded(slot)) return super.insertItem(slot, stack, simulate);
    ItemStack remainder = modules().insertItem(slot, stack, simulate);
    if (!simulate && remainder.getCount() != stack.getCount()) owner.setChanged();
    return remainder;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (slot == 10) return ItemStack.EMPTY;
    if (!embedded(slot)) return super.extractItem(slot, amount, simulate);
    ItemStack extracted = modules().extractItem(slot, amount, simulate);
    if (!simulate && !extracted.isEmpty()) owner.setChanged();
    return extracted;
  }

  @Override
  public int getSlotLimit(int slot) {
    return owner.builder() ? 1 : super.getSlotLimit(slot);
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    if (owner.hatch())
      return slot == 0
          && (SatelliteItem.assembled(stack)
              || stack.is(OrbitalRegistry.STATION.get()) && stack.has(ModComponents.STATION_LINK));
    if (owner.terminal())
      return slot == 0
          ? stack.is(OrbitalRegistry.SATELLITE_CHIP.get())
          : slot == 1 && stack.is(OrbitalRegistry.DATA_UNIT.get());
    if (owner.microwaveReceiver())
      return slot == 0 && stack.is(OrbitalRegistry.SATELLITE_CHIP.get());
    if (owner.solarArray()
        || owner.stationGravityController()
        || owner.stationAltitudeController()
        || owner.stationOrientationController()
        || owner.forceFieldProjector()
        || owner.areaGravityController()
        || owner.orbitalLaser()
        || owner.biomeScanner()
        || owner.beacon()
        || owner.dockingPort()
        || owner.warpCore()) return false;
    if (owner.railgun()) return slot == 0 && stack.getItem() instanceof LinkerItem;
    if (owner.astrobodyProcessor()) return slot == 0 && stack.getItem() instanceof AsteroidChipItem;
    if (owner.atmosphereTerraformer())
      return slot == 0 && stack.is(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get());
    if (owner.observatory())
      return slot == 0
          ? stack.is(OrbitalRegistry.ASTEROID_CHIP.get())
          : slot >= 1 && slot <= 3 && stack.is(OrbitalRegistry.DATA_UNIT.get());
    if (owner.warpController())
      return slot == 0
          ? stack.is(OrbitalRegistry.PLANET_CHIP.get())
          : slot >= 1 && slot <= 3
              ? stack.is(OrbitalRegistry.DATA_UNIT.get())
              : slot >= 4 && slot <= 8;
    if (!owner.builder()) return false;
    return switch (slot) {
      case 0, 1, 2, 3, 4, 5, 6 -> hasChassis() && modules().isItemValid(slot, stack);
      case 7 -> stack.is(OrbitalRegistry.SATELLITE.get()) && !SatelliteItem.assembled(stack);
      case 8 ->
          stack.is(OrbitalRegistry.SATELLITE_CHIP.get())
              || stack.is(OrbitalRegistry.SATELLITE.get())
              || stack.is(OrbitalRegistry.STATION_CHIP.get())
              || stack.is(OrbitalRegistry.PLANET_CHIP.get())
              || stack.is(OrbitalRegistry.ORE_SCANNER.get())
              || stack.is(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get());
      case 11 ->
          stack.is(OrbitalRegistry.SATELLITE_CHIP.get())
              || stack.is(OrbitalRegistry.STATION_CHIP.get())
              || stack.is(OrbitalRegistry.PLANET_CHIP.get())
              || stack.is(OrbitalRegistry.ORE_SCANNER.get());
      default -> false;
    };
  }
}

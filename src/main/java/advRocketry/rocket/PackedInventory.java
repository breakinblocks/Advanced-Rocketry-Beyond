// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.life.CarbonScrubberBlockEntity;
import advRocketry.life.SuitWorkstationBlockEntity;
import advRocketry.life.SuitWorkstationMenu;
import advRocketry.orbit.LandingPadBlockEntity;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.processing.CoalGeneratorBlockEntity;
import advRocketry.processing.FluidContainers;
import advRocketry.processing.PortBlockEntity;
import java.util.Set;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** Edits native detached inventories, preserving their slot rules and the packed block data. */
public final class PackedInventory implements IItemHandlerModifiable {
  private final RocketStructure.Cell cell;
  private final HolderLookup.Provider registries;
  private final BlockEntity detached;
  private final IItemHandlerModifiable inventory;
  private final IItemHandler rules;
  private final int[] slots;

  public static PackedInventory create(
      RocketStructure.Cell cell, HolderLookup.Provider registries) {
    if (!(cell.state().getBlock() instanceof EntityBlock block)) return null;
    var entity = block.newBlockEntity(cell.position(), cell.state());
    if (entity == null) return null;
    IItemHandlerModifiable inventory;
    IItemHandler rules;
    int[] slots;
    if (entity instanceof PortBlockEntity port) {
      if (!port.hasItems()) return null;
      inventory = port.inventory;
      rules = port.automationInventory();
      slots = sequence(rules.getSlots());
    } else if (entity instanceof OrbitalBlockEntity orbital) {
      inventory = orbital.inventory;
      rules = inventory;
      slots =
          orbital.builder()
              ? new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11}
              : orbital.hatch()
                      || orbital.microwaveReceiver()
                      || orbital.railgun()
                      || orbital.astrobodyProcessor()
                      || orbital.atmosphereTerraformer()
                      || orbital.warpCore()
                  ? sequence(1)
                  : orbital.terminal()
                      ? sequence(2)
                      : orbital.observatory()
                          ? sequence(5)
                          : orbital.warpController() ? sequence(9) : new int[0];
    } else if (entity instanceof RocketBlockEntity component) {
      if (component.kind() == RocketPartBlock.Kind.GUIDANCE)
        inventory = component.guidanceInventory;
      else if (component.kind() == RocketPartBlock.Kind.STATION_BUILDER)
        inventory = component.stationInventory;
      else return null;
      rules = inventory;
      slots = sequence(inventory.getSlots());
    } else if (entity instanceof SuitWorkstationBlockEntity workstation) {
      inventory = SuitWorkstationMenu.packedInventory(workstation);
      rules = inventory;
      slots = sequence(7);
    } else if (entity instanceof RocketCargoPortBlockEntity cargo) {
      if (cargo.kind() == RocketCargoPortBlock.Kind.GUIDANCE) return null;
      inventory = cargo.inventory;
      boolean fluid = ((RocketCargoPortBlock) cell.state().getBlock()).fluid();
      rules = fluid ? FluidContainers.automation(cargo.inventory) : inventory;
      slots = sequence(fluid ? 2 : 4);
    } else if (entity instanceof CarbonScrubberBlockEntity scrubber) {
      inventory = scrubber.cartridge;
      rules = inventory;
      slots = sequence(1);
    } else if (entity instanceof CoalGeneratorBlockEntity generator) {
      inventory = generator.fuel;
      rules = inventory;
      slots = sequence(1);
    } else if (entity instanceof LandingPadBlockEntity pad) {
      inventory = pad.linker;
      rules = inventory;
      slots = sequence(1);
    } else if (entity instanceof FuelingStationBlockEntity fueling) {
      inventory = fueling.inventory;
      rules = fueling.automationItems(null);
      slots = sequence(2);
    } else if (entity instanceof Container container) {
      boolean shulker =
          entity instanceof ShulkerBoxBlockEntity
              || cell.state().getBlock() instanceof ShulkerBoxBlock;
      inventory =
          new InvWrapper(container) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
              return super.isItemValid(slot, stack)
                  && (!(container instanceof WorldlyContainer worldly)
                      || worldly.canPlaceItemThroughFace(slot, stack, null))
                  && (!shulker || stack.getItem().canFitInsideContainerItems());
            }
          };
      rules = inventory;
      slots = sequence(Math.min(54, inventory.getSlots()));
    } else return null;
    return slots.length == 0
        ? null
        : new PackedInventory(cell, registries, entity, inventory, rules, slots);
  }

  private static int[] sequence(int count) {
    int[] result = new int[count];
    for (int i = 0; i < count; i++) result[i] = i;
    return result;
  }

  private PackedInventory(
      RocketStructure.Cell cell,
      HolderLookup.Provider registries,
      BlockEntity detached,
      IItemHandlerModifiable inventory,
      IItemHandler rules,
      int[] slots) {
    this.cell = cell;
    this.registries = registries;
    this.detached = detached;
    this.inventory = inventory;
    this.rules = rules;
    this.slots = slots;
    refresh();
  }

  public void refresh() {
    detached.loadWithComponents(cell.data().copy(), registries);
  }

  public void commit() {
    var saved = detached.saveWithFullMetadata(registries);
    // Empty native containers omit Items; merging would retain their old contents.
    for (String key : Set.copyOf(cell.data().getAllKeys())) cell.data().remove(key);
    cell.data().merge(saved);
  }

  public void unpackLoot(ServerLevel level, Player player, Vec3 origin) {
    if (!(detached instanceof RandomizableContainer container) || container.getLootTable() == null)
      return;
    var key = container.getLootTable();
    long seed = container.getLootTableSeed();
    var table = level.getServer().reloadableRegistries().getLootTable(key);
    container.setLootTable(null);
    var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin);
    if (player != null)
      params.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
    table.fill(container, params.create(LootContextParamSets.CHEST), seed);
    commit();
    if (player instanceof ServerPlayer serverPlayer)
      CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, key);
  }

  @Override
  public int getSlots() {
    return slots.length;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return inventory.getStackInSlot(slots[slot]);
  }

  @Override
  public int getSlotLimit(int slot) {
    return inventory.getSlotLimit(slots[slot]);
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return rules.isItemValid(slots[slot], stack);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    inventory.setStackInSlot(slots[slot], stack);
    commit();
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (!isItemValid(slot, stack)) return stack;
    var remainder = inventory.insertItem(slots[slot], stack, simulate);
    if (!simulate) commit();
    return remainder;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    var result = inventory.extractItem(slots[slot], amount, simulate);
    if (!simulate) commit();
    return result;
  }
}

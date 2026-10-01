// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.StationAssembly;
import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class RocketMenu extends ModMenu {
  public final RocketBlockEntity entity;
  private final SplitIntData data;
  private final int machineSlots;

  public RocketMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, readOpening(buffer), SplitIntData.client(7));
  }

  private static RocketBlockEntity readOpening(RegistryFriendlyByteBuf buffer) {
    return new RocketBlockEntity(buffer.readBlockPos(), Block.stateById(buffer.readVarInt()));
  }

  public static void writeOpeningData(RegistryFriendlyByteBuf buffer, RocketBlockEntity entity) {
    buffer.writeBlockPos(entity.getBlockPos());
    buffer.writeVarInt(Block.getId(entity.getBlockState()));
  }

  public RocketMenu(int id, Inventory inventory, RocketBlockEntity entity) {
    this(
        id,
        inventory,
        entity,
        SplitIntData.server(
            7,
            index ->
                switch (index) {
                  case 0 -> entity.destination;
                  case 1 -> entity.mass;
                  case 2 -> entity.thrust;
                  case 3 -> entity.fuel;
                  case 4 -> entity.energy.getEnergyStored() / 100;
                  case 5 -> entity.buildRemaining;
                  default -> entity.buildTotal;
                }));
  }

  private RocketMenu(int id, Inventory inventory, RocketBlockEntity entity, SplitIntData data) {
    super(RocketRegistry.MENU.get(), id);
    this.entity = entity;
    this.data = data;
    boolean stationBuilder = entity.kind() == RocketPartBlock.Kind.STATION_BUILDER;
    boolean guidance = entity.kind() == RocketPartBlock.Kind.GUIDANCE;
    if (stationBuilder || guidance) {
      if (stationBuilder) {
        addSlot(new SlotItemHandler(entity.stationInventory, 0, 8, 54));
        addSlot(new SlotItemHandler(entity.stationInventory, 1, 32, 54));
        addSlot(new SlotItemHandler(entity.stationInventory, 2, 124, 54));
        addSlot(new SlotItemHandler(entity.stationInventory, 3, 148, 54));
      } else addSlot(new SlotItemHandler(entity.guidanceInventory, 0, 8, 54));
      addPlayerInventory(inventory, 8, 116);
    }
    machineSlots = stationBuilder ? 4 : guidance ? 1 : 0;
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  @Override
  public boolean stillValid(Player player) {
    return entity != null
        && !entity.isRemoved()
        && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (machineSlots == 0 || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
    if (index < machineSlots) return quickMove(player, index, machineSlots);
    ItemStack stack = slots.get(index).getItem();
    var handler =
        entity.kind() == RocketPartBlock.Kind.GUIDANCE
            ? entity.guidanceInventory
            : entity.stationInventory;
    for (int target = 0; target < (machineSlots == 4 ? 2 : 1); target++)
      if (handler.isItemValid(target, stack)) {
        ItemStack moved = quickMove(player, index, target, target + 1, machineSlots, slots.size());
        if (!moved.isEmpty()) return moved;
      }
    return ItemStack.EMPTY;
  }

  @Override
  public boolean clickMenuButton(Player player, int button) {
    if (!stillValid(player) || player.level().isClientSide) return false;
    if (button == 3 && entity.kind() == RocketPartBlock.Kind.GUIDANCE) {
      entity.cycleDestination();
      return true;
    }
    if (button == 4
        && entity.kind() == RocketPartBlock.Kind.STATION_BUILDER
        && player instanceof ServerPlayer serverPlayer) {
      StationAssembly.pack(entity, serverPlayer);
      return true;
    }
    if (entity.kind() != RocketPartBlock.Kind.BUILDER
        && entity.kind() != RocketPartBlock.Kind.DEPLOYABLE_BUILDER) return false;
    if (button == 0 || button == 1) {
      if (entity.kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER) {
        if (button == 0) DeployableAssembly.scan(entity);
        else entity.beginAssembly();
      } else if (button == 0) RocketAssembly.scan(entity);
      else entity.beginAssembly();
      return true;
    }
    if (button == 2) {
      RocketAssembly.launch(entity);
      return true;
    }
    return false;
  }
}

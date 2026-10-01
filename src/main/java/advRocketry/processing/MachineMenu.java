// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MachineMenu extends ModMenu {
  private final BlockPos controller;
  private final List<PortBlockEntity> ports;
  private final SplitIntData data;
  private final int machineSlots;
  public final int storageRows;
  public final int inventoryY;
  private int page;

  private record OpeningData(BlockPos controller, List<PortBlockEntity> ports) {}

  public MachineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, readOpeningData(buffer, inventory.player.level()));
  }

  public MachineMenu(int id, Inventory inventory, BlockPos controller, List<BlockPos> positions) {
    this(
        id,
        inventory,
        new OpeningData(controller, resolvePorts(inventory.player.level(), positions)));
  }

  private static List<PortBlockEntity> resolvePorts(Level level, List<BlockPos> positions) {
    return positions.stream()
        .map(level::getBlockEntity)
        .filter(entity -> entity instanceof PortBlockEntity)
        .map(entity -> (PortBlockEntity) entity)
        .toList();
  }

  private static OpeningData readOpeningData(RegistryFriendlyByteBuf buffer, Level level) {
    BlockPos controller = buffer.readBlockPos();
    List<PortBlockEntity> ports =
        buffer.readList(
            input -> {
              BlockPos position = input.readBlockPos();
              var state = Block.stateById(input.readVarInt());
              if (!(state.getBlock() instanceof PortBlock))
                throw new IllegalArgumentException("Invalid machine port definition");
              PortBlockEntity port = new PortBlockEntity(position, state);
              port.setLevel(level);
              return port;
            });
    return new OpeningData(controller, ports);
  }

  private MachineMenu(int id, Inventory inventory, OpeningData opening) {
    super(ProcessingRegistry.MENU.get(), id);
    this.controller = opening.controller();
    this.ports = opening.ports();
    int row = 0;
    for (int portIndex = 0; portIndex < ports.size(); portIndex++) {
      var port = ports.get(portIndex);
      int portPage = portIndex / 4;
      int column = portIndex % 2;
      int cardRow = portIndex % 4 / 2;
      if (!port.hasItems()) continue;
      for (int slot = 0; slot < port.automationInventory().getSlots(); slot++) {
        addSlot(
            new SlotItemHandler(
                port.inventory,
                slot,
                12 + column * 146 + slot * (port.hasFluid() ? 36 : 18),
                38 + cardRow * 44) {
              @Override
              public boolean isActive() {
                return !inventory.player.level().isClientSide || page == portPage;
              }

              @Override
              public boolean mayPlace(ItemStack stack) {
                return port.automationInventory().isItemValid(getSlotIndex(), stack);
              }
            });
      }
      row++;
    }
    storageRows = row;
    machineSlots = slots.size();
    inventoryY = 150;
    addPlayerInventory(inventory, 69, inventoryY);
    int values = 3 + ports.size() * 2;
    Level level = inventory.player.level();
    data =
        level.isClientSide
            ? SplitIntData.client(values)
            : SplitIntData.server(values, index -> value(level, index));
    addDataSlots(data);
  }

  private int value(Level level, int index) {
    if (index < 3) {
      if (!(level.getBlockEntity(controller) instanceof ProcessingBlockEntity machine)) return 0;
      return switch (index) {
        case 0 -> machine.progressTicks();
        case 1 -> machine.durationTicks();
        default -> machine.running() ? 1 : 0;
      };
    }
    var port = ports.get((index - 3) / 2);
    if ((index - 3) % 2 == 0)
      return port.hasEnergy() ? port.energyStorage.getEnergyStored() : port.myTank.getFluidAmount();
    return port.hasFluid() ? BuiltInRegistries.FLUID.getId(port.myTank.getFluid().getFluid()) : 0;
  }

  public static void writeOpeningData(
      RegistryFriendlyByteBuf buffer, BlockPos controller, List<BlockPos> positions, Level level) {
    buffer.writeBlockPos(controller);
    buffer.writeCollection(
        resolvePorts(level, positions),
        (output, port) -> {
          output.writeBlockPos(port.getBlockPos());
          output.writeVarInt(Block.getId(port.getBlockState()));
        });
  }

  public int page() {
    return page;
  }

  public int pageCount() {
    return Math.max(1, (ports.size() + 3) / 4);
  }

  public void page(int value) {
    page = Math.clamp(value, 0, pageCount() - 1);
  }

  public List<PortBlockEntity> ports() {
    return ports;
  }

  public BlockPos controller() {
    return controller;
  }

  public float progress() {
    return (float) data.value(0) / Math.max(1, data.value(1));
  }

  public int stored(int port) {
    return data.value(3 + port * 2);
  }

  public int fluid(int port) {
    return data.value(4 + port * 2);
  }

  @Override
  public boolean stillValid(Player player) {
    Level level = player.level();
    if (player.distanceToSqr(
                controller.getX() + 0.5, controller.getY() + 0.5, controller.getZ() + 0.5)
            > 64
        || !(level.getBlockEntity(controller) instanceof ProcessingBlockEntity machine
                && machine.getBlockState().getValue(ProcessingBlock.FORMED)
            || level.getBlockEntity(controller) instanceof PortBlockEntity)) return false;
    if (level.isClientSide) return true;
    for (PortBlockEntity port : ports)
      if (port.isRemoved() || level.getBlockEntity(port.getBlockPos()) != port) return false;
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return quickMove(player, index, machineSlots);
  }
}

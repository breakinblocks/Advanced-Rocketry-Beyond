// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.processing.FluidContainers;
import advRocketry.util.ModMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class RocketCargoPortMenu extends ModMenu {
  private final RocketCargoPortBlockEntity entity;
  private final ContainerData data;
  private final int machineSlots;

  public RocketCargoPortMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, readOpeningData(buffer, inventory.player.level()));
  }

  public static void writeOpeningData(
      RegistryFriendlyByteBuf buffer, RocketCargoPortBlockEntity entity) {
    buffer.writeBlockPos(entity.getBlockPos());
    buffer.writeVarInt(Block.getId(entity.getBlockState()));
  }

  private static RocketCargoPortBlockEntity readOpeningData(
      RegistryFriendlyByteBuf buffer, Level level) {
    var pos = buffer.readBlockPos();
    var state = Block.stateById(buffer.readVarInt());
    if (!(state.getBlock() instanceof RocketCargoPortBlock))
      throw new IllegalArgumentException("Invalid rocket cargo port definition");
    var entity = new RocketCargoPortBlockEntity(pos, state);
    entity.setLevel(level);
    return entity;
  }

  public RocketCargoPortMenu(int id, Inventory inventory, RocketCargoPortBlockEntity entity) {
    super(RocketRegistry.CARGO_PORT_MENU.get(), id);
    this.entity = entity;
    data =
        entity.getLevel().isClientSide
            ? new SimpleContainerData(13)
            : new ContainerData() {
              @Override
              public int get(int index) {
                return index == 0
                    ? entity.tank.getFluidAmount()
                    : index < 9
                        ? entity.redstone.value(index - 1)
                        : entity.ejectOption(index - 9) ? 1 : 0;
              }

              @Override
              public void set(int index, int value) {}

              @Override
              public int getCount() {
                return 13;
              }
            };
    addDataSlots(data);
    machineSlots =
        ((RocketCargoPortBlock) entity.getBlockState().getBlock()).fluid()
            ? 2
            : entity.kind() == RocketCargoPortBlock.Kind.GUIDANCE ? 1 : 4;
    for (int slot = 0; slot < machineSlots; slot++)
      addSlot(
          new SlotItemHandler(
              entity.kind() == RocketCargoPortBlock.Kind.GUIDANCE
                  ? entity.guidance()
                  : entity.inventory,
              slot,
              32 + slot * (fluid() ? 36 : 18),
              30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
              return fluid()
                  ? FluidContainers.automation(entity.inventory).isItemValid(getSlotIndex(), stack)
                  : super.mayPlace(stack);
            }
          });
    addPlayerInventory(inventory, 32, 144);
  }

  public boolean fluid() {
    return ((RocketCargoPortBlock) entity.getBlockState().getBlock()).fluid();
  }

  public boolean guidance() {
    return entity.kind() == RocketCargoPortBlock.Kind.GUIDANCE;
  }

  public boolean ejectOption(int index) {
    return data.get(index + 9) != 0;
  }

  public int fluidAmount() {
    return data.get(0) & 65535;
  }

  public int redstoneValue(int index) {
    return data.get(index + 1);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (id < 0 || !stillValid(player)) return false;
    if (guidance()) {
      if (id == 1) entity.redstone.cycle(1);
      else if (id >= 8 && id < 12) entity.toggleEjectOption(id - 8);
      else return false;
    } else {
      if (id >= 8) return false;
      entity.redstone.cycle(id);
    }
    entity.setChanged();
    entity.getLevel().updateNeighborsAt(entity.getBlockPos(), entity.getBlockState().getBlock());
    broadcastChanges();
    return true;
  }

  @Override
  public boolean stillValid(Player player) {
    return entity != null
        && !entity.isRemoved()
        && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return quickMove(player, index, machineSlots);
  }
}

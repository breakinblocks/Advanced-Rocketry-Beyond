// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class TransportMenu extends ModMenu {
  public final TransportRegistry.Kind kind;
  private final BlockPos pos;
  private final TransportBlockEntity pipe;
  private final SplitIntData data;
  private final ItemStackHandler filter = new ItemStackHandler(1);

  public TransportMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, buffer.readBlockPos(), buffer.readEnum(TransportRegistry.Kind.class), null);
  }

  public TransportMenu(int id, Inventory inventory, TransportBlockEntity pipe) {
    this(id, inventory, pipe.getBlockPos(), pipe.kind(), pipe);
  }

  private TransportMenu(
      int id,
      Inventory inventory,
      BlockPos pos,
      TransportRegistry.Kind kind,
      TransportBlockEntity pipe) {
    super(TransportRegistry.MENU.get(), id);
    this.pos = pos;
    this.kind = kind;
    this.pipe = pipe;
    if (pipe != null) filter.setStackInSlot(0, pipe.filter.copy());
    if (kind == TransportRegistry.Kind.ITEM)
      addSlot(
          new SlotItemHandler(filter, 0, 260, 118) {
            @Override
            public boolean mayPickup(Player player) {
              return false;
            }
          });
    addPlayerInventory(inventory, 69, 150);
    data =
        pipe == null
            ? SplitIntData.client(13)
            : SplitIntData.server(
                13,
                index -> {
                  if (index < 6) return pipe.modes[index];
                  var network = TransportNetworks.network(pipe);
                  return switch (index) {
                    case 6 -> network.roundRobin ? 1 : 0;
                    case 7 -> pipe.redstone;
                    case 8 -> BuiltInRegistries.FLUID.getId(network.configuredFluid);
                    case 9 -> network.nodes.size();
                    case 10 -> network.oversized ? 2 : network.conflicted() ? 1 : 0;
                    case 11 ->
                        pipe.kind() == TransportRegistry.Kind.FLUID
                            ? pipe.pendingFluid.getAmount()
                            : pipe.kind() == TransportRegistry.Kind.ENERGY
                                ? pipe.pendingEnergy
                                : pipe.pendingItem.getCount();
                    case 12 -> pipe.getBlockState().getValue(TransportBlock.ACTIVE) ? 1 : 0;
                    default -> 0;
                  };
                });
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  @Override
  public boolean stillValid(Player player) {
    return player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64
        && player.level().getBlockEntity(pos) instanceof TransportBlockEntity current
        && current.kind() == kind
        && (pipe == null || current == pipe);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (pipe == null || !stillValid(player)) return false;
    if (id >= 0 && id < 6) {
      pipe.modes[id] = (pipe.modes[id] + 1) % 3;
      pipe.changed();
      for (var side : Direction.values())
        if (player.level().hasChunkAt(pos.relative(side))
            && player.level().getBlockEntity(pos.relative(side))
                instanceof TransportBlockEntity other) other.refreshConnections();
    } else if (id == 6 && kind == TransportRegistry.Kind.ITEM) TransportNetworks.roundRobin(pipe);
    else if (id == 7) {
      pipe.redstone = (pipe.redstone + 1) % 3;
      pipe.setChanged();
    } else if (id == 8 && kind == TransportRegistry.Kind.FLUID) TransportNetworks.unlockFluid(pipe);
    else return false;
    broadcastChanges();
    return true;
  }

  @Override
  public void clicked(int slot, int button, ClickType type, Player player) {
    if (kind == TransportRegistry.Kind.ITEM && slot == 0) {
      if (pipe != null
          && stillValid(player)
          && (type == ClickType.PICKUP || type == ClickType.QUICK_MOVE)) {
        pipe.filter = getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copyWithCount(1);
        filter.setStackInSlot(0, pipe.filter.copy());
        pipe.setChanged();
        broadcastChanges();
      }
      return;
    }
    super.clicked(slot, button, type, player);
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return !(slot instanceof SlotItemHandler);
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return !(slot instanceof SlotItemHandler);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}

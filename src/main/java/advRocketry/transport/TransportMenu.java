// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class TransportMenu extends ModMenu {
  public final TransportRegistry.Kind kind;
  private final BlockPos pos;
  private final TransportBlockEntity pipe;
  private final SplitIntData data;
  private int selected;
  private TransportNetworks.Network snapshot;
  private int snapshotConflict;
  private long snapshotTick = Long.MIN_VALUE;
  private final ItemStackHandler ghosts = new ItemStackHandler(9);
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
    for (int i = 0; i < 9; i++)
      addSlot(
          new SlotItemHandler(ghosts, i, 69 + i * 18, 218) {
            @Override
            public boolean mayPickup(Player player) {
              return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
              return false;
            }
          });
    refreshGhosts();
    addPlayerInventory(inventory, 69, 280);
    data =
        pipe == null
            ? SplitIntData.client(27)
            : SplitIntData.server(
                27,
                index -> {
                  if (index < 6) return pipe.modes[index];
                  var network = snapshot();
                  return switch (index) {
                    case 6 -> network.roundRobin ? 1 : 0;
                    case 7 -> pipe.redstone;
                    case 8 -> BuiltInRegistries.FLUID.getId(network.configuredFluid);
                    case 9 -> network.nodes.size();
                    case 10 -> network.oversized ? 2 : snapshotConflict;
                    case 11 ->
                        pipe.kind() == TransportRegistry.Kind.FLUID
                            ? pipe.pendingFluid.getAmount()
                            : pipe.kind() == TransportRegistry.Kind.ENERGY
                                ? pipe.pendingEnergy
                                : pipe.pendingItem.getCount();
                    case 13 -> selected;
                    case 14 -> pipe.sides[selected].priority;
                    case 15 -> pipe.sides[selected].routing.ordinal();
                    case 16 -> pipe.sides[selected].extractRedstone;
                    case 17 -> pipe.sides[selected].insertRedstone;
                    case 18 -> pipe.sides[selected].extractionLimit;
                    case 19 -> pipe.sides[selected].retryCap;
                    case 20 -> pipe.channel;
                    case 21 -> pipe.sides[selected].configured ? 1 : 0;
                    case 22 -> pipe.sides[selected].deny ? 1 : 0;
                    case 23 -> pipe.sides[selected].exact ? 1 : 0;
                    case 24 -> pipe.sides[selected].sensor ? 1 : 0;
                    case 25 -> pipe.sides[selected].disposal ? 1 : 0;
                    case 26 -> pipe.blockedReason;
                    case 12 -> pipe.getBlockState().getValue(TransportBlock.ACTIVE) ? 1 : 0;
                    default -> 0;
                  };
                });
    addDataSlots(data);
  }

  private TransportNetworks.Network snapshot() {
    if (snapshot == null || snapshotTick != pipe.getLevel().getGameTime()) {
      snapshot = TransportNetworks.network(pipe);
      snapshotConflict = snapshot.conflicted() ? 1 : 0;
      snapshotTick = pipe.getLevel().getGameTime();
    }
    return snapshot;
  }

  private void refreshGhosts() {
    if (pipe == null) return;
    for (int i = 0; i < 9; i++) {
      var entry = pipe.sides[selected].entries[i];
      ItemStack icon = entry == null ? ItemStack.EMPTY : entry.icon().copy();
      if (entry != null && entry.tag() != null) {
        icon = new ItemStack(Items.PAPER);
        icon.set(DataComponents.CUSTOM_NAME, Component.literal("#" + entry.tag()));
      }
      ghosts.setStackInSlot(i, icon);
    }
  }

  public void setTag(Player player, int side, int slot, String text) {
    if (pipe == null
        || !stillValid(player)
        || !player.mayBuild()
        || player.isSpectator()
        || side != selected
        || slot < 0
        || slot >= 9) return;
    var id = ResourceLocation.tryParse(text.startsWith("#") ? text.substring(1) : text);
    if (id == null) return;
    pipe.sides[selected].configured = true;
    pipe.sides[selected].entries[slot] =
        new TransportSideConfig.Entry(ItemStack.EMPTY, FluidStack.EMPTY, id);
    pipe.changed();
    refreshGhosts();
    broadcastChanges();
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
    if (pipe == null || !stillValid(player) || !player.mayBuild() || player.isSpectator())
      return false;
    if (id >= 0 && id < 6) {
      pipe.modes[id] = (pipe.modes[id] + 1) % 3;
      pipe.changed();
      for (var side : Direction.values())
        if (player.level().hasChunkAt(pos.relative(side))
            && player.level().getBlockEntity(pos.relative(side))
                instanceof TransportBlockEntity other) other.refreshConnections();
    } else if (id >= 10 && id < 16) {
      selected = id - 10;
      refreshGhosts();
    } else if (id >= 20 && id <= 32) {
      var config = pipe.sides[selected];
      switch (id) {
        case 20 -> config.priority = Math.max(-1000, config.priority - 1);
        case 21 -> config.priority = Math.min(1000, config.priority + 1);
        case 22 ->
            config.routing =
                TransportSideConfig.Routing.values()[(config.routing.ordinal() + 1) % 5];
        case 23 -> config.extractRedstone = (config.extractRedstone + 1) % 3;
        case 24 -> config.insertRedstone = (config.insertRedstone + 1) % 3;
        case 25 ->
            config.extractionLimit =
                config.extractionLimit == Integer.MAX_VALUE
                    ? 1
                    : config.extractionLimit >= kind.rate
                        ? Integer.MAX_VALUE
                        : Math.min(kind.rate, config.extractionLimit * 2);
        case 26 ->
            config.retryCap =
                config.retryCap == 0
                    ? (kind == TransportRegistry.Kind.ITEM
                        ? 80
                        : kind == TransportRegistry.Kind.FLUID ? 20 : 4)
                    : 0;
        case 27 -> pipe.channel = pipe.channel == 15 ? -1 : pipe.channel + 1;
        case 28 -> config.configured = !config.configured;
        case 29 -> config.deny = !config.deny;
        case 30 -> config.exact = !config.exact;
        case 31 -> config.sensor = !config.sensor;
        case 32 -> config.disposal = !config.disposal;
        default -> {}
      }
      pipe.changed();
      for (var side : Direction.values())
        if (player.level().hasChunkAt(pos.relative(side))
            && player.level().getBlockEntity(pos.relative(side))
                instanceof TransportBlockEntity other) other.refreshConnections();
    } else if (id == 6 && kind == TransportRegistry.Kind.ITEM) TransportNetworks.roundRobin(pipe);
    else if (id == 7) {
      pipe.redstone = (pipe.redstone + 1) % 3;
      pipe.changed();
    } else if (id == 8 && kind == TransportRegistry.Kind.FLUID) TransportNetworks.unlockFluid(pipe);
    else return false;
    snapshotTick = Long.MIN_VALUE;
    broadcastChanges();
    return true;
  }

  @Override
  public void clicked(int slot, int button, ClickType type, Player player) {
    int ghost = slot - (kind == TransportRegistry.Kind.ITEM ? 1 : 0);
    if (ghost >= 0 && ghost < 9) {
      if (pipe != null
          && stillValid(player)
          && player.mayBuild()
          && !player.isSpectator()
          && type == ClickType.PICKUP) {
        pipe.sides[selected].configured = true;
        pipe.sides[selected].entries[ghost] =
            getCarried().isEmpty() ? null : TransportSideConfig.Entry.stack(getCarried());
        pipe.changed();
        refreshGhosts();
        broadcastChanges();
      }
      return;
    }
    if (kind == TransportRegistry.Kind.ITEM && slot == 0) {
      if (pipe != null
          && stillValid(player)
          && player.mayBuild()
          && !player.isSpectator()
          && (type == ClickType.PICKUP || type == ClickType.QUICK_MOVE)) {
        pipe.filter = getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copyWithCount(1);
        filter.setStackInSlot(0, pipe.filter.copy());
        pipe.changed();
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

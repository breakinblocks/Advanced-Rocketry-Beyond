// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.StationLink;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original assembled-rocket inventory, destination, landing-pad and disassembly controls. */
public final class RocketConsoleMenu extends ModMenu {
  public record Pad(BlockPos position, String name, boolean occupied) {}

  private record Opening(
      int entityId,
      int page,
      int pages,
      RocketStructure.Cell cell,
      List<Pad> pads,
      BlockPos selected) {}

  public final int entityId, page, pages;
  public final Component inventoryName;
  public final List<Pad> pads;
  public final BlockPos selectedPad;
  private final RocketEntity rocket;
  private final PackedInventory packed;
  private final boolean guidance;
  private final SplitIntData data;
  public final int machineSlots, inventoryY;

  private static Opening opening(RocketEntity rocket, int requested) {
    var inventories = rocket.inventoryCells();
    int page = Math.floorMod(requested, Math.max(1, inventories.size()));
    var chip = rocket.guidanceChip();
    List<Pad> pads = new ArrayList<>();
    BlockPos selected = null;
    if (rocket.level() instanceof ServerLevel level
        && chip.is(OrbitalRegistry.STATION_CHIP.get())) {
      long id = StationLink.read(chip);
      var galaxy = GalaxyData.get(level.getServer());
      var station = galaxy.stations.get(id);
      if (station != null && station.deployed) {
        var space =
            PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
        var saved = station.landingPads;
        saved.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .limit(128)
            .forEach(
                entry -> {
                  var pos = BlockPos.of(entry.getKey());
                  pads.add(
                      new Pad(
                          pos, entry.getValue(), !LandingPadLogic.available(space, pos, rocket)));
                });
        selected = rocket.selectedLandingPad(id);
      }
    }
    return new Opening(
        rocket.getId(),
        page,
        inventories.size(),
        inventories.isEmpty() ? null : inventories.get(page),
        List.copyOf(pads),
        selected);
  }

  public static void open(ServerPlayer player, RocketEntity rocket, int page) {
    if (!accessible(player, rocket)) return;
    var opening = opening(rocket, page);
    player.openMenu(
        new SimpleMenuProvider(
            (id, inventory, owner) -> new RocketConsoleMenu(id, inventory, rocket, opening),
            Component.translatable("message.adv_rocketry.rocket_console.rocket_controls")),
        buffer -> writeOpening(buffer, opening));
  }

  public static void writeOpeningData(
      RegistryFriendlyByteBuf buffer, RocketEntity rocket, int page) {
    writeOpening(buffer, opening(rocket, page));
  }

  private static void writeOpening(RegistryFriendlyByteBuf buffer, Opening opening) {
    buffer.writeVarInt(opening.entityId());
    buffer.writeVarInt(opening.page());
    buffer.writeVarInt(opening.pages());
    buffer.writeBoolean(opening.cell() != null);
    if (opening.cell() != null) {
      buffer.writeBlockPos(opening.cell().position());
      buffer.writeVarInt(Block.getId(opening.cell().state()));
      // Contents use native menu slot synchronization; this packet defines slot layout only.
    }
    buffer.writeCollection(
        opening.pads(),
        (output, pad) -> {
          output.writeBlockPos(pad.position());
          output.writeUtf(pad.name(), 32);
          output.writeBoolean(pad.occupied());
        });
    buffer.writeBoolean(opening.selected() != null);
    if (opening.selected() != null) buffer.writeBlockPos(opening.selected());
  }

  private static Opening readOpening(RegistryFriendlyByteBuf buffer) {
    int entityId = buffer.readVarInt(), page = buffer.readVarInt(), pages = buffer.readVarInt();
    RocketStructure.Cell cell =
        buffer.readBoolean()
            ? new RocketStructure.Cell(
                buffer.readBlockPos(), Block.stateById(buffer.readVarInt()), new CompoundTag())
            : null;
    var pads =
        buffer.readList(
            input -> new Pad(input.readBlockPos(), input.readUtf(32), input.readBoolean()));
    return new Opening(
        entityId, page, pages, cell, pads, buffer.readBoolean() ? buffer.readBlockPos() : null);
  }

  public RocketConsoleMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, null, readOpening(buffer));
  }

  public RocketConsoleMenu(int id, Inventory inventory, RocketEntity rocket, int page) {
    this(id, inventory, rocket, opening(rocket, page));
  }

  private RocketConsoleMenu(int id, Inventory inventory, RocketEntity rocket, Opening opening) {
    super(RocketRegistry.CONSOLE_MENU.get(), id);
    this.rocket = rocket;
    entityId = opening.entityId();
    page = opening.page();
    pages = opening.pages();
    pads = opening.pads();
    selectedPad = opening.selected();
    inventoryName =
        opening.cell() == null
            ? Component.translatable("message.adv_rocketry.rocket_console.no_onboard_inventory")
            : opening.cell().state().getBlock().getName();
    guidance =
        opening.cell() != null
            && opening.cell().state().is(RocketRegistry.PARTS.get("guidance_computer").get());
    packed =
        opening.cell() == null
            ? null
            : PackedInventory.create(opening.cell(), inventory.player.registryAccess());
    if (rocket != null && packed != null && rocket.level() instanceof ServerLevel serverLevel)
      packed.unpackLoot(
          serverLevel,
          inventory.player,
          rocket.origin().offset(opening.cell().position()).getCenter());
    machineSlots = packed == null ? 0 : packed.getSlots();
    for (int slot = 0; slot < machineSlots; slot++)
      addSlot(
          new SlotItemHandler(packed, slot, 16 + slot % 9 * 18, 68 + slot / 9 * 18) {
            @Override
            public void setChanged() {
              if (RocketConsoleMenu.this.rocket != null) packed.commit();
              super.setChanged();
            }
          });
    inventoryY = 80 + Math.max(1, (machineSlots + 8) / 9) * 18;
    addPlayerInventory(inventory, 16, inventoryY);
    data =
        rocket == null
            ? SplitIntData.client(4)
            : SplitIntData.server(
                4,
                index ->
                    switch (index) {
                      case 0 -> rocket.monitorFuel();
                      case 1 -> rocket.monitorCapacity();
                      case 2 -> rocket.structure().destination;
                      default -> rocket.guidanceChip().isEmpty() ? 1 : 0;
                    });
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  public static boolean accessible(Player player, RocketEntity rocket) {
    return rocket != null
        && !rocket.isRemoved()
        && rocket.level() == player.level()
        && rocket.flight() == 0
        && (player.getVehicle() == rocket
            || rocket.getBoundingBox().inflate(8).contains(player.position()));
  }

  @Override
  public boolean stillValid(Player player) {
    return player.level().isClientSide || accessible(player, rocket);
  }

  private void refresh() {
    if (rocket != null && packed != null) packed.refresh();
  }

  private void commit() {
    if (rocket != null && packed != null) {
      packed.commit();
      if (guidance) rocket.setGuidanceChip(rocket.guidanceChip());
    }
  }

  @Override
  public void broadcastChanges() {
    refresh();
    super.broadcastChanges();
  }

  @Override
  public void clicked(int slot, int button, ClickType type, Player player) {
    if (!stillValid(player)) return;
    refresh();
    super.clicked(slot, button, type, player);
    commit();
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (!stillValid(player)) return ItemStack.EMPTY;
    refresh();
    ItemStack moved = quickMove(player, index, machineSlots);
    if (!moved.isEmpty()) commit();
    return moved;
  }

  @Override
  public boolean clickMenuButton(Player player, int button) {
    if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) return false;
    if (button == 0 || button == 1) {
      open(serverPlayer, rocket, page + (button == 0 ? -1 : 1));
      return true;
    }
    if (button == 2) {
      rocket.disassemble(player);
      if (rocket.isRemoved()) player.closeContainer();
      return true;
    }
    if (button == 3) {
      boolean launched =
          player.getVehicle() == rocket ? rocket.launch(player) : rocket.launchFromMonitor();

      if (launched) player.closeContainer();
      return launched;
    }
    if (button == 4) {
      rocket.cycleConsoleDestination();
      return true;
    }
    if (button == 5 || button >= 10 && button < 10 + pads.size()) {
      var pos = button == 5 ? null : pads.get(button - 10).position();
      if (!rocket.selectLandingPad(pos)) return false;
      open(serverPlayer, rocket, page);
      return true;
    }
    return false;
  }
}

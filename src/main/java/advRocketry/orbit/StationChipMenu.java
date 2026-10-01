// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.space.GalaxyData;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Held-chip editor; all mutations require the original hand, item, and effective planet. */
public final class StationChipMenu extends AbstractContainerMenu {
  private final InteractionHand hand;
  private final ItemStack chip;
  public final int planet;
  public final boolean canAdd;
  public final List<StationChipLocations.Location> locations;
  public final int selected;

  public StationChipMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        buffer.readEnum(InteractionHand.class),
        buffer.readInt(),
        buffer.readBoolean(),
        snapshot(buffer));
  }

  private static ItemStack snapshot(RegistryFriendlyByteBuf buffer) {
    var chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
    chip.set(ModComponents.STATION_DESTINATIONS, StationDestinations.STREAM_CODEC.decode(buffer));
    return chip;
  }

  public static void writeOpeningData(
      RegistryFriendlyByteBuf buffer,
      InteractionHand hand,
      int planet,
      boolean canAdd,
      ItemStack chip) {
    buffer.writeEnum(hand);
    buffer.writeInt(planet);
    buffer.writeBoolean(canAdd);
    StationDestinations.STREAM_CODEC.encode(
        buffer, chip.getOrDefault(ModComponents.STATION_DESTINATIONS, StationDestinations.EMPTY));
  }

  public StationChipMenu(int id, InteractionHand hand, int planet, boolean canAdd, ItemStack chip) {
    super(OrbitalRegistry.STATION_CHIP_MENU.get(), id);
    this.hand = hand;
    this.chip = chip;
    this.planet = planet;
    this.canAdd = canAdd;
    locations = List.copyOf(StationChipLocations.list(chip, planet));
    selected = StationChipLocations.selection(chip, planet);
  }

  public static void open(ServerPlayer player, InteractionHand hand) {
    var chip = player.getItemInHand(hand);
    if (!chip.is(OrbitalRegistry.STATION_CHIP.get())) return;
    GalaxyData galaxy = GalaxyData.get(player.server);
    int planet = StationLogic.orbitPlanet(galaxy, player.serverLevel(), player.blockPosition());
    boolean canAdd =
        planet != GalaxyData.SPACE_ID && galaxy.planet(player.serverLevel()).id == planet;
    player.openMenu(
        new SimpleMenuProvider(
            (id, inventory, owner) -> new StationChipMenu(id, hand, planet, canAdd, chip),
            Component.translatable("message.adv_rocketry.station_chip.station_chip_destinations")),
        buffer -> writeOpeningData(buffer, hand, planet, canAdd, chip));
  }

  public boolean apply(Player player, int action, int index, String name) {
    if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) return false;
    boolean changed =
        switch (action) {
          case 0 -> StationChipLocations.clear(chip, planet);
          case 1 -> StationChipLocations.delete(chip, planet);
          case 2 -> canAdd && StationChipLocations.add(chip, planet, player.blockPosition(), name);
          case 3 -> StationChipLocations.select(chip, planet, index);
          default -> false;
        };
    if (changed) {
      player.getInventory().setChanged();
      open(serverPlayer, hand);
    }
    return changed;
  }

  @Override
  public boolean stillValid(Player player) {
    return player.level().isClientSide
        || (player.getItemInHand(hand) == chip
            && chip.is(OrbitalRegistry.STATION_CHIP.get())
            && player.level() instanceof ServerLevel level
            && StationLogic.orbitPlanet(
                    GalaxyData.get(level.getServer()), level, player.blockPosition())
                == planet);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}

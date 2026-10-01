// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original generator fuel slot and synced power/burn readout. */
public final class CoalGeneratorMenu extends ModMenu {
  private final BlockPos pos;
  private final CoalGeneratorBlockEntity generator;
  private final SplitIntData data;

  public CoalGeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        new CoalGeneratorBlockEntity(
            buffer.readBlockPos(), MachinePorts.COAL_GENERATOR.get().defaultBlockState()),
        true);
  }

  public CoalGeneratorMenu(int id, Inventory inventory, BlockPos pos) {
    this(
        id,
        inventory,
        (CoalGeneratorBlockEntity) inventory.player.level().getBlockEntity(pos),
        false);
  }

  private CoalGeneratorMenu(
      int id, Inventory inventory, CoalGeneratorBlockEntity generator, boolean remote) {
    super(MachinePorts.COAL_GENERATOR_MENU.get(), id);
    this.pos = generator.getBlockPos();
    this.generator = remote ? null : generator;
    addSlot(new SlotItemHandler(generator.fuel, 0, 80, 35));
    addPlayerInventory(inventory, 8, 84);
    data =
        remote
            ? SplitIntData.client(2)
            : SplitIntData.server(
                2, index -> index == 0 ? generator.energyStored() : generator.burnTime());
    addDataSlots(data);
  }

  public int energy() {
    return data.value(0);
  }

  public int burnTime() {
    return data.value(1);
  }

  @Override
  public boolean stillValid(Player player) {
    return player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64
        && player.level().getBlockEntity(pos) instanceof CoalGeneratorBlockEntity current
        && (generator == null || current == generator && !generator.isRemoved());
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return quickMove(player, index, 1);
  }
}

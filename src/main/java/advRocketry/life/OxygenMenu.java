// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.processing.ProcessingFluids;
import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class OxygenMenu extends ModMenu {
  private static final int VALUES = 5;
  private final OxygenBlockEntity entity;
  private final SplitIntData data;

  public OxygenMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        new OxygenBlockEntity(buffer.readBlockPos(), Block.stateById(buffer.readVarInt())),
        SplitIntData.client(VALUES));
  }

  public static void writeOpeningData(RegistryFriendlyByteBuf buffer, OxygenBlockEntity entity) {
    buffer.writeBlockPos(entity.getBlockPos());
    buffer.writeVarInt(Block.getId(entity.getBlockState()));
  }

  public OxygenMenu(int id, Inventory inventory, OxygenBlockEntity entity) {
    this(
        id,
        entity,
        SplitIntData.server(
            VALUES,
            index ->
                switch (index) {
                  case 0 -> entity.energy.getEnergyStored();
                  case 1 -> entity.tank.getFluidAmount();
                  case 2 -> entity.room().size();
                  case 4 -> entity.tank.getFluid().is(ProcessingFluids.hydrogen()) ? 1 : 0;
                  default -> entity.active ? 1 : 0;
                }));
  }

  private OxygenMenu(int id, OxygenBlockEntity entity, SplitIntData data) {
    super(LifeSupportRegistry.OXYGEN_MENU.get(), id);
    this.entity = entity;
    this.data = data;
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  public boolean charger() {
    return entity != null && entity.charger();
  }

  @Override
  public boolean stillValid(Player player) {
    return entity != null
        && !entity.isRemoved()
        && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return quickMove(player, index, 0);
  }
}

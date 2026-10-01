// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Original two-layer processor: one data point per channel per 20 powered ticks. */
public final class AstrobodyProcessorLogic {
  private static final ResearchType[] CHANNELS = {
    ResearchType.COMPOSITION, ResearchType.OPTICAL, ResearchType.MASS
  };

  private AstrobodyProcessorLogic() {}

  public static boolean complete(OrbitalBlockEntity controller) {
    return controller.astrobodyProcessor()
        && PlacedMultiblock.complete(controller, "astrobody_data_processor");
  }

  static PlacedMultiblock structure(OrbitalBlockEntity controller) {
    return PlacedMultiblock.of(controller, "astrobody_data_processor");
  }

  public static void control(OrbitalBlockEntity controller, int button) {
    if (!controller.astrobodyProcessor() || button < 24 || button > 26) return;
    controller.researchChannels ^= 1 << (button - 24);
    controller.status =
        Texts.translate(
            "status.adv_rocketry.astrobody_processor.research_channels",
            controller.researchChannels);
    controller.setChanged();
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level) || !complete(controller)) return;
    PlacedMultiblock structure = structure(controller);
    controller.pullFrom(structure.entity('P', PortBlockEntity.class), 1000);
    PortBlockEntity input = structure.entity('I', PortBlockEntity.class);
    PortBlockEntity output = structure.entity('O', PortBlockEntity.class);
    List<PortBlockEntity> buses = structure.entities('d', PortBlockEntity.class);
    if (controller.inventory.getStackInSlot(0).isEmpty())
      for (int slot = 0; slot < input.inventory.getSlots(); slot++)
        if (AsteroidChipItem.programmed(input.inventory.getStackInSlot(slot))) {
          controller.inventory.setStackInSlot(0, input.inventory.extractItem(slot, 1, false));
          break;
        }
    ItemStack chip = controller.inventory.getStackInSlot(0);
    if (chip.isEmpty()) return;
    if (full(chip)) {
      for (int slot = 0; slot < output.inventory.getSlots(); slot++)
        if (output.inventory.insertItem(slot, chip, true).isEmpty()) {
          output.inventory.insertItem(slot, chip.copy(), false);
          controller.inventory.setStackInSlot(0, ItemStack.EMPTY);
          controller.status =
              Component.translatable(
                  "status.adv_rocketry.astrobody_processor.asteroid_research_complete");
          return;
        }
      return;
    }
    if (controller.energy.consume(100, true) < 100) return;
    boolean active = false;
    for (int channel = 0; channel < 3; channel++) {
      if ((controller.researchChannels & (1 << channel)) == 0
          || AsteroidChipItem.data(chip, CHANNELS[channel]) >= AsteroidChipItem.MAX_DATA) continue;
      PortBlockEntity bus = buses.get(channel);
      int slot = dataSlot(bus, CHANNELS[channel]);
      if (slot < 0) continue;
      active = true;
      if (++controller.researchProgress[channel] < 20) continue;
      controller.researchProgress[channel] = 0;
      ItemStack unit = bus.inventory.getStackInSlot(slot).copy();
      StoredResearch research = DataUnitItem.research(unit);
      DataUnitItem.store(unit, research.type(), research.amount() - 1);
      bus.inventory.setStackInSlot(slot, unit);
      AsteroidChipItem.addData(chip, CHANNELS[channel], 1);
      controller.inventory.setStackInSlot(0, chip);
    }
    if (active) controller.energy.consume(100, false);
    controller.setChanged();
  }

  private static int dataSlot(PortBlockEntity bus, ResearchType type) {
    for (int slot = 0; slot < bus.inventory.getSlots(); slot++) {
      ItemStack unit = bus.inventory.getStackInSlot(slot);
      if (unit.is(OrbitalRegistry.DATA_UNIT.get()) && DataUnitItem.research(unit).amount(type) > 0)
        return slot;
    }
    return -1;
  }

  private static boolean full(ItemStack chip) {
    for (ResearchType channel : CHANNELS)
      if (AsteroidChipItem.data(chip, channel) < AsteroidChipItem.MAX_DATA) return false;
    return true;
  }
}

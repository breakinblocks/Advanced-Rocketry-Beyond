// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.OutputSimulation;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original eleven-layer railgun footprint and linked, powered stack transfer. */
public final class RailgunLogic {
  private RailgunLogic() {}

  public static final int MUZZLE_HEIGHT = 11;

  /** The central motor anchors the model, coil field and cosmetic cargo path. */
  public static BlockPos emitter(OrbitalBlockEntity controller) {
    var placed = PlacedMultiblock.of(controller, "railgun");
    BlockPos motor = placed == null ? null : placed.first('M');
    return motor != null ? motor : controller.getBlockPos();
  }

  public static boolean complete(OrbitalBlockEntity controller) {
    return controller.railgun() && PlacedMultiblock.complete(controller, "railgun");
  }

  public static void control(OrbitalBlockEntity controller, int button) {
    if (!controller.railgun()) return;
    switch (OrbitalAction.fromId(button)) {
      case RAILGUN_MINIMUM_DOWN ->
          controller.railgunMinimum = Math.max(1, controller.railgunMinimum - 1);
      case RAILGUN_MINIMUM_UP ->
          controller.railgunMinimum = Math.min(64, controller.railgunMinimum + 1);
      case RAILGUN_REDSTONE -> controller.railgunRedstone = (controller.railgunRedstone + 1) % 3;
      default -> {
        return;
      }
    }
    controller.status =
        Texts.translate(
            "status.adv_rocketry.railgun.minimum_redstone",
            controller.railgunMinimum,
            controller.railgunRedstone);
    controller.setChanged();
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel source) || !complete(controller)) return;
    for (PortBlockEntity port :
        PlacedMultiblock.of(controller, "railgun").entities('P', PortBlockEntity.class))
      controller.pullFrom(port, 2000);
    if (++controller.railgunTicks < 10) return;
    controller.railgunTicks = 0;
    if (controller.railgunRedstone == 1 && !source.hasNeighborSignal(controller.getBlockPos())
        || controller.railgunRedstone == 2 && source.hasNeighborSignal(controller.getBlockPos()))
      return;
    ItemStack linker = controller.inventory.getStackInSlot(0);
    GlobalPos link = LinkerItem.target(linker).position().orElse(null);
    if (!(linker.getItem() instanceof LinkerItem) || link == null) return;
    ServerLevel destination = source.getServer().getLevel(link.dimension());
    if (destination == null) return;
    BlockPos destinationPos = link.pos();
    if (!destination.isLoaded(destinationPos)
        || !sameSystem(source, controller.getBlockPos(), destination, destinationPos)
        || !(destination.getBlockEntity(destinationPos) instanceof OrbitalBlockEntity receiver)
        || !receiver.railgun()
        || !complete(receiver)) return;
    int horizontalDistance =
        (int)
            Math.hypot(
                destinationPos.getX() - controller.getBlockPos().getX(),
                destinationPos.getZ() - controller.getBlockPos().getZ());
    int cost =
        Math.min(
            100000, source == destination ? horizontalDistance * 10 + 50000 : horizontalDistance);
    if (controller.energy.consume(cost, true) < cost) return;
    PortBlockEntity input =
        PlacedMultiblock.of(controller, "railgun").entity('I', PortBlockEntity.class);
    PortBlockEntity output =
        PlacedMultiblock.of(receiver, "railgun").entity('O', PortBlockEntity.class);
    for (int slot = input.inventory.getSlots() - 1; slot >= 0; slot--) {
      ItemStack stack = input.inventory.getStackInSlot(slot);
      if (stack.isEmpty()
          || stack.getCount() < controller.railgunMinimum
          || !canReceive(output.inventory, stack)) continue;
      ItemStack moved = input.inventory.extractItem(slot, stack.getCount(), false);
      ItemStack remaining = moved;
      for (int target = 0; target < output.inventory.getSlots() && !remaining.isEmpty(); target++)
        remaining = output.inventory.insertItem(target, remaining, false);
      controller.energy.consume(cost, false);
      BlockPos emitter = emitter(controller);
      RailgunCargoEntity cargo = OrbitalRegistry.RAILGUN_CARGO.get().create(source);
      if (cargo != null) {
        cargo.setDisplay(moved);
        cargo.setPos(emitter.getX() + .5, emitter.getY() + MUZZLE_HEIGHT, emitter.getZ() + .5);
        source.addFreshEntity(cargo);
      }
      controller.railgunFiredAt = source.getGameTime();
      source.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 3);
      source.playSound(
          null,
          emitter,
          ProcessingRegistry.RAILGUN_SOUND.get(),
          SoundSource.BLOCKS,
          1f,
          .975f + source.random.nextFloat() * .05f);
      controller.status =
          Texts.translate("status.adv_rocketry.railgun.transferred_items", moved.getCount());
      controller.setChanged();
      return;
    }
  }

  private static boolean canReceive(ItemStackHandler inventory, ItemStack stack) {
    return OutputSimulation.canInsertAllItems(List.of(inventory), List.of(stack));
  }

  private static boolean sameSystem(
      ServerLevel source, BlockPos sourcePos, ServerLevel destination, BlockPos destinationPos) {
    GalaxyData galaxy = GalaxyData.get(source.getServer());
    Planet one = body(galaxy, source, sourcePos);
    Planet two = body(galaxy, destination, destinationPos);
    return one != null && two != null && one.id >= 0 && two.id >= 0 && one.star == two.star;
  }

  private static Planet body(GalaxyData galaxy, ServerLevel level, BlockPos pos) {
    Planet planet = galaxy.find(level);
    return planet == null || planet.id != GalaxyData.SPACE_ID
        ? planet
        : galaxy.planets.get(StationLogic.orbitPlanet(galaxy, level, pos));
  }
}

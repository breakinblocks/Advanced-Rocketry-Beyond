// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import java.util.function.IntUnaryOperator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class OrbitalMenu extends ModMenu {
  private static final int COUNT = 39;
  public final OrbitalBlockEntity entity;
  private final SplitIntData data;
  private final Level level;
  private final int machineSlots;

  public OrbitalMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, readOpening(inventory, buffer), SplitIntData.client(COUNT));
  }

  private static OrbitalBlockEntity readOpening(
      Inventory inventory, RegistryFriendlyByteBuf buffer) {
    var pos = buffer.readBlockPos();
    var state = Block.stateById(buffer.readVarInt());
    var existing = inventory.player.level().getBlockEntity(pos);
    var entity =
        existing instanceof OrbitalBlockEntity orbital && orbital.getBlockState().equals(state)
            ? orbital
            : new OrbitalBlockEntity(pos, state);
    entity.asteroidSeed = buffer.readLong();
    entity.dockingId = buffer.readUtf(32);
    entity.dockingTarget = buffer.readUtf(32);
    return entity;
  }

  public static void writeOpeningData(RegistryFriendlyByteBuf buffer, OrbitalBlockEntity entity) {
    buffer.writeBlockPos(entity.getBlockPos());
    buffer.writeVarInt(Block.getId(entity.getBlockState()));
    buffer.writeLong(entity.asteroidSeed);
    buffer.writeUtf(entity.dockingId, 32);
    buffer.writeUtf(entity.dockingTarget, 32);
  }

  public long asteroidSeed() {
    var current = level.getBlockEntity(entity.getBlockPos());
    return current instanceof OrbitalBlockEntity orbital
        ? orbital.asteroidSeed
        : entity.asteroidSeed;
  }

  public OrbitalMenu(int id, Inventory inventory, OrbitalBlockEntity entity) {
    this(id, inventory, entity, SplitIntData.server(COUNT, snapshot(entity)));
  }

  private static IntUnaryOperator snapshot(OrbitalBlockEntity entity) {
    int[] values = new int[COUNT];
    long[] sampled = {Long.MIN_VALUE};
    return index -> {
      long time = entity.getLevel() == null ? 0 : entity.getLevel().getGameTime();
      if (sampled[0] != time) {
        sampled[0] = time;
        for (int i = 0; i < COUNT; i++) values[i] = value(entity, i);
      }
      return values[index];
    };
  }

  private static int value(OrbitalBlockEntity entity, int index) {
    return switch (index) {
      case 0 -> entity.energy.getEnergyStored();
      case 1 ->
          entity.stationGravityController()
              ? StationMotionLogic.targetGravity(entity)
              : entity.warpDestination;
      case 2 -> entity.stationGravityController() ? StationMotionLogic.currentGravity(entity) : 100;
      case 3 -> entity.observationTicks;
      case 4 -> entity.asteroidCandidate;
      case 5 -> entity.areaGravity;
      case 6 -> entity.areaRadius;
      case 7 -> entity.areaDirection;
      case 8 -> entity.laserX;
      case 9 -> entity.laserZ;
      case 10 -> entity.laserRunning ? 1 : 0;
      case 11 -> entity.laserJammed ? 1 : 0;
      case 12 -> entity.laserTerrain ? 1 : 0;
      case 13 -> entity.biomeScanner() ? BiomeScannerLogic.scannedPlanet(entity) : -3;
      case 14 -> entity.beaconEnabled ? 1 : 0;
      case 15 -> entity.railgunMinimum;
      case 16 -> entity.railgunRedstone;
      case 17 -> entity.researchChannels;
      case 18 -> entity.discoveryTicks;
      case 19 ->
          entity.atmosphereTerraformer()
              ? (int)
                  (entity.terraformingTicks * 1000L / AdvancedRocketryConfig.terraformDuration())
              : 0;
      case 20 -> entity.terraformingDirection;
      case 21 -> entity.stationAltitudeController() ? StationMotionLogic.altitude(entity) : 4;
      case 22 -> entity.stationAltitudeController() ? StationMotionLogic.targetAltitude(entity) : 4;
      case 23, 24, 25 ->
          entity.stationOrientationController()
              ? StationMotionLogic.targetRotation(entity, index - 23)
              : 0;
      case 27 -> entity.holoTarget;
      case 28 -> entity.holoStar;
      case 29 -> entity.holoCenter;
      case 30 -> entity.holoSize;
      case 31 -> entity.holoRedstone;
      case 32 -> entity.warpCore() ? WarpCoreLogic.stationFuel(entity) : 0;
      case 33 -> entity.warpCore() && WarpCoreLogic.complete(entity) ? 1 : 0;
      case 34 -> entity.builderProgress;
      case 35 -> entity.asteroidOptions.length;
      case 36 -> entity.asteroidOptionIndex;
      case 37 ->
          entity.observatory()
              ? ObservatoryLogic.researchData(entity, ResearchType.COMPOSITION)
              : 0;
      case 38 ->
          entity.observatory() ? ObservatoryLogic.researchData(entity, ResearchType.MASS) : 0;
      default -> 0;
    };
  }

  private OrbitalMenu(int id, Inventory inventory, OrbitalBlockEntity entity, SplitIntData data) {
    super(OrbitalRegistry.MENU.get(), id);
    this.entity = entity;
    this.level = inventory.player.level();
    this.data = data;
    if (entity.hatch()
        || entity.microwaveReceiver()
        || entity.railgun()
        || entity.astrobodyProcessor()
        || entity.atmosphereTerraformer())
      addSlot(new SlotItemHandler(entity.inventory, 0, 26, 42));
    else if (entity.terminal()) {
      addSlot(new SlotItemHandler(entity.inventory, 0, 26, 42));
      addSlot(new SlotItemHandler(entity.inventory, 1, 80, 42));
    } else if (entity.observatory()) {
      for (int slot = 0; slot <= 4; slot++)
        addSlot(new SlotItemHandler(entity.inventory, slot, 8 + slot * 30, 42));
    } else if (entity.warpController()) {
      for (int slot = 0; slot < 4; slot++)
        addSlot(new SlotItemHandler(entity.inventory, slot, 8 + slot * 46, 40));
      for (int slot = 4; slot <= 8; slot++)
        addSlot(new SlotItemHandler(entity.inventory, slot, 8 + (slot - 4) * 34, 68));
    } else if (entity.builder()) {
      addSlot(new ItemHandlerCopySlot(entity.inventory, 0, 8, 42));
      for (int slot = 1; slot <= 6; slot++)
        addSlot(new ItemHandlerCopySlot(entity.inventory, slot, 34 + (slot - 1) * 18, 42));
      addSlot(new SlotItemHandler(entity.inventory, 7, 8, 68));
      addSlot(new SlotItemHandler(entity.inventory, 8, 34, 68));
      addSlot(new SlotItemHandler(entity.inventory, 9, 154, 54));
      addSlot(new SlotItemHandler(entity.inventory, 11, 60, 68));
    }
    machineSlots = slots.size();
    int inventoryTop =
        entity.warpController()
            ? 165
            : entity.builder() || entity.observatory() || entity.stationOrientationController()
                ? 155
                : entity.areaGravityController()
                        || entity.orbitalLaser()
                        || entity.biomeScanner()
                        || entity.astrobodyProcessor()
                        || entity.atmosphereTerraformer()
                        || entity.selectorControls()
                    ? 135
                    : 105;
    addPlayerInventory(inventory, 8, inventoryTop);
    addDataSlots(data);
  }

  public int energy() {
    return data.value(0);
  }

  public int warpDestination() {
    return data.value(1);
  }

  public int hologramTarget() {
    return data.value(27);
  }

  public int hologramStar() {
    return data.value(28);
  }

  public int hologramCenter() {
    return data.value(29);
  }

  public int hologramSize() {
    return data.value(30);
  }

  public int hologramRedstone() {
    return data.value(31);
  }

  public int stationFuel() {
    return data.value(32);
  }

  public boolean warpCoreFormed() {
    return data.value(33) != 0;
  }

  public int builderProgress() {
    return data.value(34);
  }

  public int asteroidOptions() {
    return data.value(35);
  }

  public int asteroidOptionIndex() {
    return data.value(36);
  }

  public int compositionData() {
    return data.value(37);
  }

  public int massData() {
    return data.value(38);
  }

  public int discoveryTicks() {
    return data.value(18);
  }

  public int terraformingTicks() {
    return data.value(19);
  }

  public int terraformingDirection() {
    return data.value(20);
  }

  public int stationAltitude() {
    return data.value(21);
  }

  public int stationTargetAltitude() {
    return data.value(22);
  }

  public int targetRotation(int axis) {
    return data.value(23 + axis);
  }

  public int stationTargetGravity() {
    return data.value(1);
  }

  public int stationCurrentGravity() {
    return data.value(2);
  }

  public int observationTicks() {
    return data.value(3);
  }

  public int asteroidCandidate() {
    return data.value(4);
  }

  public int areaGravity() {
    return data.value(5);
  }

  public int areaRadius() {
    return data.value(6);
  }

  public int areaDirection() {
    return data.value(7);
  }

  public int laserX() {
    return data.value(8);
  }

  public int laserZ() {
    return data.value(9);
  }

  public boolean laserRunning() {
    return data.value(10) != 0;
  }

  public boolean laserJammed() {
    return data.value(11) != 0;
  }

  public boolean laserTerrain() {
    return data.value(12) != 0;
  }

  public int scannedPlanet() {
    return data.value(13);
  }

  public boolean beaconEnabled() {
    return data.value(14) != 0;
  }

  public int railgunMinimum() {
    return data.value(15);
  }

  public int railgunRedstone() {
    return data.value(16);
  }

  public int researchChannels() {
    return data.value(17);
  }

  @Override
  public boolean stillValid(Player player) {
    return !entity.isRemoved() && player.distanceToSqr(entity.getBlockPos().getCenter()) <= 64;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (!slot.hasItem()) return ItemStack.EMPTY;
    ItemStack stack = slot.getItem(), copy = stack.copy();
    if (index < machineSlots) {
      if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
    } else {
      boolean moved = false;
      for (int target = 0; target < machineSlots; target++)
        if (slots.get(target).mayPlace(stack)
            && moveItemStackTo(stack, target, target + 1, false)) {
          moved = true;
          break;
        }
      if (!moved) return ItemStack.EMPTY;
    }
    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
    return copy;
  }

  @Override
  public boolean clickMenuButton(Player player, int button) {
    if (!stillValid(player) || player.level().isClientSide) return false;
    if (button == 0 && entity.builder()) SatelliteBuilderLogic.build(entity);
    else if (button == 1 && entity.terminal()) SatelliteTerminalLogic.transferData(entity);
    else if (button == 2 && entity.warpController()) WarpLogic.cycleDestination(entity);
    else if (button == 3 && entity.warpController()) WarpLogic.warp(entity);
    else if (button == 4 && entity.stationGravityController())
      StationMotionLogic.adjustGravity(entity, -10);
    else if (button == 5 && entity.stationGravityController())
      StationMotionLogic.adjustGravity(entity, 10);
    else if (button == 6 && entity.observatory()) entity.startObservation();
    else if (button == 7 && entity.observatory()) entity.programAsteroidChip();
    else if (button == 8 && entity.spaceElevator() && player instanceof ServerPlayer passenger) {
      if (SpaceElevatorLogic.travel(entity, passenger)) passenger.closeContainer();
    } else if (button >= 9 && button <= 12 && entity.areaGravityController())
      AreaGravityLogic.adjust(entity, button);
    else if ((button >= 13 && button <= 19) && entity.orbitalLaser())
      OrbitalLaserLogic.control(entity, button);
    else if (button == 20 && entity.beacon()) entity.toggleBeacon();
    else if (button >= 21 && button <= 23 && entity.railgun()) RailgunLogic.control(entity, button);
    else if (button >= 24 && button <= 26 && entity.astrobodyProcessor())
      AstrobodyProcessorLogic.control(entity, button);
    else if (button == 27 && entity.warpController()) PlanetDiscoveryLogic.start(entity);
    else if (button == 28 && entity.warpController()) PlanetDiscoveryLogic.importChip(entity);
    else if (button >= 29 && button <= 31 && entity.atmosphereTerraformer())
      TerraformerLogic.control(entity, button);
    else if (button == 32 && entity.stationAltitudeController())
      StationMotionLogic.adjustAltitude(entity, -10);
    else if (button == 33 && entity.stationAltitudeController())
      StationMotionLogic.adjustAltitude(entity, 10);
    else if (button >= 34 && button <= 39 && entity.stationOrientationController())
      StationMotionLogic.adjustRotation(entity, (button - 34) / 2, button % 2 == 0 ? -10 : 10);
    else if (button == 40 && entity.stationOrientationController())
      StationMotionLogic.resetRotation(entity);
    else if (button >= 41 && button <= 48 && entity.selectorControls())
      HolographicSelectorLogic.control(entity, button);
    else if (button == 49 && entity.builder()) SatelliteBuilderLogic.copyChip(entity);
    else if (button == 50 && entity.observatory()) ObservatoryLogic.selectNext(entity);
    else return false;
    player.displayClientMessage(entity.status, false);
    return true;
  }
}

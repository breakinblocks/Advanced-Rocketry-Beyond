package advRocketry.rocket;

import advRocketry.ModComponents;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.life.OxygenMenu;
import advRocketry.life.SuitWorkstationMenu;
import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.OrbitalMenu;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLink;
import advRocketry.processing.CoalGeneratorMenu;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import advRocketry.util.SplitIntData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class RocketConsoleTests {
  @GameTest(template = "empty")
  public static void packedChestsClearEmptyContentsAndRollLootOnlyOnce(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(5, 4, 5));
    var cell =
        new RocketStructure.Cell(
            BlockPos.ZERO, Blocks.CHEST.defaultBlockState(), new CompoundTag());
    var inventory = PackedInventory.create(cell, level.registryAccess());
    inventory.insertItem(0, new ItemStack(Items.DIAMOND, 5), false);
    inventory.refresh();
    helper.assertTrue(
        inventory.extractItem(0, 5, false).getCount() == 5, "Packed chest lost stored diamonds");
    inventory.refresh();
    helper.assertTrue(
        inventory.getStackInSlot(0).isEmpty()
            && cell.data().getList("Items", Tag.TAG_COMPOUND).isEmpty(),
        "Empty chest resurrected old items");
    var loot = new CompoundTag();
    loot.putString("LootTable", "minecraft:chests/simple_dungeon");
    loot.putLong("LootTableSeed", 1234);
    var lootCell = new RocketStructure.Cell(BlockPos.ZERO, Blocks.CHEST.defaultBlockState(), loot);
    var packed = PackedInventory.create(lootCell, level.registryAccess());
    packed.unpackLoot(level, helper.makeMockPlayer(GameType.SURVIVAL), pos.getCenter());
    helper.assertTrue(
        !lootCell.data().contains("LootTable")
            && !lootCell.data().getList("Items", Tag.TAG_COMPOUND).isEmpty(),
        "Loot chest did not generate its contents in packed menu");
    int total = 0;
    for (int slot = 0; slot < packed.getSlots(); slot++)
      total += packed.extractItem(slot, 64, false).getCount();
    helper.assertTrue(total > 0, "Generated chest was empty");
    var restored = PackedInventory.create(lootCell, level.registryAccess());
    restored.unpackLoot(level, helper.makeMockPlayer(GameType.SURVIVAL), pos.getCenter());
    for (int slot = 0; slot < restored.getSlots(); slot++)
      helper.assertTrue(
          restored.getStackInSlot(slot).isEmpty(), "Reload rerolled or duplicated generated loot");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void orbitalOxygenAndGeneratorMenusOpenBeforeTheirChunks(GameTestHelper helper) {
    var level = helper.getLevel();
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var pos = helper.absolutePos(new BlockPos(5, 4, 5));
    var orbital =
        new OrbitalBlockEntity(pos, OrbitalRegistry.OBSERVATORY.get().defaultBlockState());
    orbital.asteroidSeed = 123456789123L;
    orbital.dockingId = "Alpha";
    orbital.dockingTarget = "Beta";
    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    OrbitalMenu.writeOpeningData(buffer, orbital);
    var client = new OrbitalMenu(0, player.getInventory(), buffer);
    helper.assertTrue(
        client.entity.observatory()
            && client.slots.size() == 41
            && client.asteroidSeed() == 123456789123L
            && client.entity.dockingId.equals("Alpha")
            && client.entity.dockingTarget.equals("Beta"),
        "Orbital opening lost its role or initial fields");
    buffer.release();
    level.setBlock(pos, OrbitalRegistry.OBSERVATORY.get().defaultBlockState(), 3);
    var live = (OrbitalBlockEntity) level.getBlockEntity(pos);
    live.asteroidSeed = 987654321987L;
    helper.assertTrue(
        client.asteroidSeed() == 987654321987L,
        "Late chunk update did not refresh observatory survey seed");
    level.removeBlock(pos, false);
    var oxygen = new OxygenBlockEntity(pos, LifeSupportRegistry.CHARGER.get().defaultBlockState());
    buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    OxygenMenu.writeOpeningData(buffer, oxygen);
    helper.assertTrue(
        new OxygenMenu(1, player.getInventory(), buffer).charger(),
        "Oxygen charger lost role before chunk arrival");
    buffer.release();
    buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    buffer.writeBlockPos(pos);
    helper.assertTrue(
        new CoalGeneratorMenu(2, player.getInventory(), buffer).slots.size() == 37,
        "Coal generator requires client block entity to open");
    buffer.release();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void packedSuitModulesAndCargoPortsRetainTheirNativeRules(GameTestHelper helper) {
    var level = helper.getLevel();
    var cell =
        new RocketStructure.Cell(
            BlockPos.ZERO,
            LifeSupportRegistry.SUIT_WORKSTATION.get().defaultBlockState(),
            new CompoundTag());
    var inventory = PackedInventory.create(cell, level.registryAccess());
    var chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    var tank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    var jetpack = new ItemStack(ProcessingRegistry.PART_ITEMS.get("jetpack").get());
    helper.assertTrue(
        inventory != null
            && inventory.getSlots() == 7
            && inventory.insertItem(0, chest, false).isEmpty(),
        "Packed suit workstation missing armor/module slots");
    helper.assertTrue(
        !inventory.isItemValid(1, jetpack)
            && inventory.insertItem(1, tank, false).isEmpty()
            && inventory.insertItem(3, jetpack, false).isEmpty(),
        "Packed suit bypassed module slot rules");
    var restored = PackedInventory.create(cell, level.registryAccess());
    helper.assertTrue(
        restored.getStackInSlot(1).is(tank.getItem())
            && restored.getStackInSlot(3).is(jetpack.getItem()),
        "Embedded modules did not survive packed reload");
    var removed = restored.extractItem(0, 1, false);
    helper.assertTrue(
        LifeSupportRegistry.CHEST.get().inventory(removed).getStackInSlot(2).is(jetpack.getItem())
            && restored.getStackInSlot(3).isEmpty(),
        "Armor removal duplicated embedded module");
    for (var block :
        new RocketCargoPortBlock[] {
          RocketRegistry.ITEM_LOADER.get(),
          RocketRegistry.ITEM_UNLOADER.get(),
          RocketRegistry.FLUID_LOADER.get(),
          RocketRegistry.FLUID_UNLOADER.get()
        }) {
      var cargo =
          PackedInventory.create(
              new RocketStructure.Cell(BlockPos.ZERO, block.defaultBlockState(), new CompoundTag()),
              level.registryAccess());
      helper.assertTrue(
          cargo != null && cargo.getSlots() == (block.fluid() ? 2 : 4),
          "Packed infrastructure inventory missing");
      if (block.fluid())
        helper.assertTrue(
            cargo.isItemValid(0, new ItemStack(Items.WATER_BUCKET))
                && !cargo.isItemValid(1, new ItemStack(Items.WATER_BUCKET)),
            "Packed fluid container output accepted insertion");
    }
    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    buffer.writeBlockPos(helper.absolutePos(new BlockPos(5, 4, 5)));
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var menu = new SuitWorkstationMenu(0, player.getInventory(), buffer);
    helper.assertTrue(
        menu.slots.size() == 43, "Suit workstation opening depends on client block entity");
    buffer.release();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void builderMenuOpensBeforeChunksAndPreservesLargeScanValues(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var pos = helper.absolutePos(new BlockPos(5, 4, 5));
    for (String part :
        new String[] {
          "rocket_builder", "deployable_rocket_builder", "station_builder", "guidance_computer"
        }) {
      var entity =
          new RocketBlockEntity(pos, RocketRegistry.PARTS.get(part).get().defaultBlockState());
      entity.setLevel(level);
      entity.destination = GalaxyData.SPACE_ID;
      entity.mass = 50000;
      entity.thrust = 200000;
      entity.fuel = 123456;
      entity.buildRemaining = 70000;
      entity.buildTotal = 90000;
      var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
      RocketMenu.writeOpeningData(buffer, entity);
      var client = new RocketMenu(1, player.getInventory(), buffer);
      var server = new RocketMenu(1, player.getInventory(), entity);
      helper.assertTrue(
          client.entity.kind() == entity.kind(),
          "Opening packet depends on absent chunk block entity");
      var split = SplitIntData.server(7, server::value);
      for (int index = 0; index < split.getCount(); index++)
        client.setData(index, (short) split.get(index));
      helper.assertTrue(
          client.value(0) == -2
              && client.value(1) == 50000
              && client.value(2) == 200000
              && client.value(3) == 123456
              && client.value(5) == 70000
              && client.value(6) == 90000,
          "Native short synchronization truncated builder values");
      buffer.release();
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void packedMenusMergeStacksAndObserveInfrastructureChanges(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(5, 4, 5));
    var ship = new RocketStructure();
    var storage = new ItemStackHandler(4);
    storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 63));
    for (int i = 1; i < 4; i++) storage.setStackInSlot(i, new ItemStack(Items.STONE, 64));
    var data = new CompoundTag();
    data.put("inventory", storage.serializeNBT(level.registryAccess()));
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO, MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), data));
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, pos);
    level.addFreshEntity(rocket);
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(rocket.position());
    player.getInventory().setItem(9, new ItemStack(Items.IRON_INGOT, 64));
    var menu = new RocketConsoleMenu(0, player.getInventory(), rocket, 0);
    helper.assertTrue(
        menu.machineSlots == 4 && menu.stillValid(player), "Packed cargo not exposed");
    menu.quickMoveStack(player, 4);
    storage.deserializeNBT(
        level.registryAccess(),
        rocket.structure().cells.getFirst().data().getCompound("inventory"));
    helper.assertTrue(
        storage.getStackInSlot(0).getCount() == 64
            && player.getInventory().getItem(9).getCount() == 63,
        "Shift-merge lost items or did not persist");
    var unload = new ItemStackHandler(1);
    helper.assertTrue(
        rocket.moveCargoItems(unload, true) == 64, "Infrastructure failed while menu open");
    menu.broadcastChanges();
    helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "Menu cached cargo already unloaded");
    menu.quickMoveStack(player, 0);
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 63
            && unload.getStackInSlot(0).getCount() == 64,
        "Menu duplicated externally transferred cargo");
    menu.quickMoveStack(player, 4);
    storage.deserializeNBT(
        level.registryAccess(),
        rocket.structure().cells.getFirst().data().getCompound("inventory"));
    helper.assertTrue(
        storage.getStackInSlot(0).getCount() == 63 && player.getInventory().getItem(9).isEmpty(),
        "Refilled cargo did not persist");
    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    RocketConsoleMenu.writeOpeningData(buffer, rocket, 0);
    var opening = new RocketConsoleMenu(1, player.getInventory(), buffer);
    helper.assertTrue(
        opening.machineSlots == 4 && opening.pages == 1,
        "Client menu depends on tracked rocket entity");
    buffer.release();
    player.setPos(rocket.getX() + 100, rocket.getY(), rocket.getZ());
    helper.assertTrue(
        !menu.stillValid(player) && menu.quickMoveStack(player, 0).isEmpty(),
        "Remote cargo edit accepted");
    rocket.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void selectedStationPadsPersistAndRejectDescendingOccupants(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    var space = PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    var origin = helper.absolutePos(new BlockPos(5, 4, 5));
    var first =
        new BlockPos(
            11000000 + Math.floorMod(origin.getX(), 100000),
            130,
            11000000 + Math.floorMod(origin.getZ(), 100000));
    var second = first.east(12);
    space.setBlock(first, OrbitalRegistry.LANDING_PAD.get().defaultBlockState(), 3);
    space.setBlock(second, OrbitalRegistry.LANDING_PAD.get().defaultBlockState(), 3);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = first.getX();
    station.y = first.getY();
    station.z = first.getZ();
    station.landingPads.put(first.asLong(), "Alpha");
    station.landingPads.put(second.asLong(), "Beta");
    long id = galaxy.newStation(station);
    var ship = new RocketStructure();
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, origin);
    level.addFreshEntity(rocket);
    var chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
    chip.set(ModComponents.STATION_LINK, new StationLink(id));
    rocket.setGuidanceChip(chip);
    helper.assertTrue(
        rocket.selectLandingPad(second) && !rocket.selectLandingPad(second.east()),
        "Pad selection accepted an unregistered position");
    var saved = new CompoundTag();
    rocket.saveWithoutId(saved);
    var restored = RocketRegistry.ROCKET.get().create(level);
    restored.load(saved);
    helper.assertTrue(
        second.equals(restored.selectedLandingPad(id)), "Rocket reload lost selected pad");
    var component = new RocketBlockEntity(BlockPos.ZERO, ship.cells.getFirst().state());
    component.loadWithComponents(
        rocket.structure().cells.getFirst().data(), level.registryAccess());
    helper.assertTrue(
        component
                .saveWithFullMetadata(level.registryAccess())
                .getCompound("station_landing_pads")
                .getLong(Long.toString(id))
            == second.asLong(),
        "Unpacked guidance computer dropped pad mapping");
    helper.assertTrue(
        LandingPadLogic.arrivalPad(space, station, second, rocket).equals(second),
        "Arrival ignored selected pad");
    var occupied = RocketRegistry.ROCKET.get().create(space);
    occupied.initialize(new RocketStructure(), second.above(100));
    space.addFreshEntity(occupied);
    helper.assertTrue(
        !rocket.selectLandingPad(second)
            && LandingPadLogic.arrivalPad(space, station, second, rocket).equals(first),
        "Descending rocket did not occupy its landing column");
    helper.assertTrue(
        rocket.selectLandingPad(null) && rocket.selectedLandingPad(id) == null,
        "Automatic pad did not clear selection");
    occupied.discard();
    rocket.discard();
    restored.discard();
    space.removeBlock(first, false);
    space.removeBlock(second, false);
    galaxy.stations.remove(id);
    galaxy.setDirty();
    helper.succeed();
  }
}

package advRocketry.rocket;

import advRocketry.ModComponents;
import advRocketry.orbit.LinkTarget;
import advRocketry.orbit.LinkerItem;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.PlanetIdChipItem;
import advRocketry.orbit.Satellite;
import advRocketry.orbit.SatelliteItem;
import advRocketry.orbit.SatelliteLink;
import advRocketry.orbit.SatelliteLogic;
import advRocketry.orbit.SatelliteType;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationChipLocations;
import advRocketry.orbit.StationLink;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class GuidanceTests {
  @GameTest(template = "empty")
  public static void stationChipRetainsOriginalGroundReturnCoordinates(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    var space = PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    var origin = helper.absolutePos(new BlockPos(5, 4, 5));
    var stationPos =
        new BlockPos(
            9000000 + Math.floorMod(origin.getX(), 1000000),
            120,
            9000000 + Math.floorMod(origin.getZ(), 1000000));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = stationPos.getX();
    station.y = stationPos.getY();
    station.z = stationPos.getZ();
    long id = galaxy.newStation(station);
    var chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
    chip.set(ModComponents.STATION_LINK, new StationLink(id));
    var out = GuidanceComputer.route(level, chip, origin);
    helper.assertTrue(
        out != null && out.planet() == GalaxyData.SPACE_ID && out.stationId() == id,
        "Station chip failed to select orbital station");
    GuidanceComputer.rememberDeparture(level, chip, origin);
    var saved = chip.save(level.registryAccess());
    var restored = ItemStack.parse(level.registryAccess(), saved).orElseThrow();
    var home = GuidanceComputer.route(space, restored, stationPos);
    helper.assertTrue(
        home != null
            && home.planet() == GalaxyData.EARTH_ID
            && home.x() == origin.getX() + .5
            && home.z() == origin.getZ() + .5,
        "Station chip returned to station coordinates instead of saved ground pad");
    StationChipLocations.add(restored, 0, origin.east(200), "Secondary pad");
    StationChipLocations.select(restored, 0, 1);
    var selected = GuidanceComputer.route(space, restored, stationPos);
    helper.assertTrue(
        selected != null && selected.x() == origin.getX() + 200.5,
        "Station return ignored selected saved destination");
    galaxy.stations.remove(id);
    galaxy.setDirty();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void satelliteRecoveryPreservesIdentityAndCannotDuplicatePayload(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    Satellite record = new Satellite();
    record.type = SatelliteType.byName("optical");
    record.deployed = true;
    record.orbitPlanet = GalaxyData.EARTH_ID;
    record.powerGeneration = 20;
    record.powerStorage = 1000;
    record.data = 125;
    long id = galaxy.newSatellite(record);
    var chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    var ship = new RocketStructure();
    var inventory = new ItemStackHandler(10);
    inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
    var data = new CompoundTag();
    data.put("inventory", inventory.serializeNBT(level.registryAccess()));
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO, OrbitalRegistry.HATCH.get().defaultBlockState(), data));
    helper.assertTrue(
        !SatelliteLogic.recoverCargo(level, ship, chip) && galaxy.satellites.get(id).deployed,
        "Full satellite hatch removed orbital record");
    inventory.setStackInSlot(0, ItemStack.EMPTY);
    data.put("inventory", inventory.serializeNBT(level.registryAccess()));
    helper.assertTrue(
        SatelliteLogic.recoverCargo(level, ship, chip), "Empty hatch could not recover satellite");
    var restored = RocketStructure.load(ship.save(true));
    inventory.deserializeNBT(
        level.registryAccess(), restored.cells.getFirst().data().getCompound("inventory"));
    var payload = inventory.getStackInSlot(0);
    helper.assertTrue(
        SatelliteItem.assembled(payload)
            && payload.get(ModComponents.SATELLITE).id() == id
            && galaxy.satellites.get(id).data == 125
            && !galaxy.satellites.get(id).deployed,
        "Recovered satellite lost ID, data or deployment state");
    var second = new RocketStructure();
    second.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO, OrbitalRegistry.HATCH.get().defaultBlockState(), new CompoundTag()));
    helper.assertTrue(
        !SatelliteLogic.recoverCargo(level, second, chip),
        "Recovered satellite could be claimed twice");
    helper.assertTrue(
        SatelliteLogic.deployCargo(level, restored, helper.absolutePos(new BlockPos(5, 4, 5))) == 1
            && galaxy.satellites.get(id).deployed
            && galaxy.satellites.get(id).data == 125,
        "Recovered satellite could not be redeployed with preserved data");
    helper.assertTrue(
        SatelliteLogic.deployCargo(level, restored, helper.absolutePos(new BlockPos(5, 4, 5))) == 0,
        "Recovered payload was deployed twice");
    galaxy.satellites.remove(id);
    galaxy.setDirty();
    helper.succeed();
  }

  private static RocketEntity rocket(GameTestHelper helper, BlockPos pos) {
    var ship = new RocketStructure();
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    var rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    rocket.initialize(ship, pos);
    helper.getLevel().addFreshEntity(rocket);
    return rocket;
  }

  @GameTest(template = "empty")
  public static void guidanceEjectionWaitsForSpaceAndRunsOncePerArrival(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.GUIDANCE_ACCESS.get().defaultBlockState(), 3);
    level.setBlock(pos.west(), MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 3);
    var port = (RocketCargoPortBlockEntity) level.getBlockEntity(pos);
    var output = (PortBlockEntity) level.getBlockEntity(pos.west());
    for (int slot = 0; slot < 4; slot++)
      output.inventory.setStackInSlot(slot, new ItemStack(Items.STONE, 64));
    var rocket = rocket(helper, pos.east(48));
    var chip = new ItemStack(OrbitalRegistry.PLANET_CHIP.get());
    PlanetIdChipItem.program(
        chip, GalaxyData.get(level.getServer()).planets.get(GalaxyData.EARTH_ID));
    rocket.setGuidanceChip(chip);
    helper.assertTrue(
        port.link(rocket), "Guidance hatch lost original sixty-four-block link range");
    // The range probe is outside the template's ticking chunks. Return the entity
    // to the test area before asserting server UUID lookup and reload behavior.
    rocket.setPos(pos.getX() + 3.5, pos.getY(), pos.getZ() + .5);
    helper.assertTrue(level.getEntity(rocket.getUUID()) == rocket, "Test rocket is not tracked");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(pos.getX(), pos.getY(), pos.getZ());
    var menu = new RocketCargoPortMenu(0, player.getInventory(), port);
    helper.assertTrue(menu.guidance() && menu.slots.size() == 37, "Guidance menu lost chip slot");
    menu.clickMenuButton(player, 8); // auto-eject
    menu.clickMenuButton(player, 10); // planet filter
    port.tick();
    helper.assertTrue(!rocket.guidanceChip().isEmpty(), "Blocked ejection lost chip");
    var saved = port.saveWithFullMetadata(level.registryAccess());
    level.removeBlockEntity(pos);
    var restored =
        new RocketCargoPortBlockEntity(
            pos, RocketRegistry.GUIDANCE_ACCESS.get().defaultBlockState());
    restored.loadWithComponents(saved, level.registryAccess());
    level.setBlockEntity(restored);
    output.inventory.setStackInSlot(0, ItemStack.EMPTY);
    restored.tick();
    helper.assertTrue(
        rocket.guidanceChip().isEmpty() && output.inventory.getStackInSlot(0).is(chip.getItem()),
        "Blocked ejection did not resume after reload");
    helper.assertTrue(
        level.getSignal(pos, Direction.NORTH) == 15,
        "Empty guidance hatch lost omnidirectional completion output");
    rocket.setGuidanceChip(chip);
    restored.tick();
    helper.assertTrue(
        !rocket.guidanceChip().isEmpty() && output.inventory.getStackInSlot(0).getCount() == 1,
        "Guidance hatch ejected twice for same arrival");
    restored.unlink();
    restored.link(rocket);
    output.inventory.setStackInSlot(1, ItemStack.EMPTY);
    restored.tick();
    helper.assertTrue(
        rocket.guidanceChip().isEmpty() && output.inventory.getStackInSlot(1).is(chip.getItem()),
        "New arrival failed to reset ejection state");
    var satellite = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    helper.assertTrue(rocket.setGuidanceChip(satellite), "Satellite chip not accepted");
    restored.unlink();
    restored.link(rocket);
    output.inventory.setStackInSlot(2, ItemStack.EMPTY);
    restored.tick();
    helper.assertTrue(
        rocket.guidanceChip().is(satellite.getItem()), "Planet filter ejected satellite chip");
    restored.toggleEjectOption(1);
    restored.tick();
    helper.assertTrue(
        rocket.guidanceChip().isEmpty()
            && output.inventory.getStackInSlot(2).is(satellite.getItem()),
        "Satellite filter did not eject selected chip");
    rocket.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void satelliteAndLinkerGuidanceResolveSavedTargets(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    var rocket = rocket(helper, pos);
    long id = 978654321L;
    var record = new Satellite();
    record.deployed = true;
    record.orbitPlanet = 1;
    galaxy.satellites.put(id, record);
    var satellite = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    satellite.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    helper.assertTrue(
        rocket.setGuidanceChip(satellite) && rocket.structure().destination == 1,
        "Satellite ID did not select its orbit planet");
    var linker = new ItemStack(ProcessingRegistry.PART_ITEMS.get("linker").get());
    LinkerItem.setTarget(
        linker, LinkTarget.EMPTY.withPosition(GlobalPos.of(level.dimension(), pos.east(100))));
    helper.assertTrue(
        rocket.setGuidanceChip(linker), "Position linker rejected from guidance slot");
    var route = GuidanceComputer.route(level, linker, pos);
    helper.assertTrue(
        route != null && route.planet() == GalaxyData.EARTH_ID && route.x() == pos.getX() + 100.5,
        "Linker target lost dimension or coordinates");
    LinkerItem.setTarget(
        linker,
        LinkTarget.EMPTY.withPosition(
            GlobalPos.of(
                ResourceKey.create(
                    Registries.DIMENSION, ResourceLocation.parse("missing:dimension")),
                pos.east(100))));
    rocket.setGuidanceChip(linker);
    helper.assertTrue(
        !rocket.launchFromMonitor(), "Missing linker destination used a stale planet");
    record.deployed = false;
    helper.assertTrue(
        GuidanceComputer.route(level, satellite, pos) == null,
        "Undeployed satellite resolved as destination");
    galaxy.satellites.remove(id);
    galaxy.setDirty();
    rocket.discard();
    helper.succeed();
  }
}

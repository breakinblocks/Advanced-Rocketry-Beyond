package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import advRocketry.TestMultiblocks;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import advRocketry.util.IdOrTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class StationTests {
  @GameTest(template = "empty")
  public static void holographicSelectorTargetsStationWarpAndHonorsRedstone(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    int coordinate = 800000 + (int) stationId * 256;
    station.x = coordinate;
    station.y = 120;
    station.z = coordinate;
    BlockPos pos = new BlockPos(coordinate, 120, coordinate);
    space.setBlock(pos, OrbitalRegistry.HOLOGRAPHIC_SELECTOR.get().defaultBlockState(), 2);
    OrbitalBlockEntity selector = (OrbitalBlockEntity) space.getBlockEntity(pos);
    HolographicSelectorLogic.control(selector, 41);
    helper.assertTrue(selector.holoTarget >= 0, "Hologram found no planet in Sol");
    HolographicSelectorLogic.control(selector, 43);
    HolographicSelectorLogic.control(selector, 41);
    HolographicSelectorLogic.control(selector, 41);
    helper.assertTrue(selector.holoTarget == 1, "Hologram did not navigate from Earth to Luna");
    HolographicSelectorLogic.control(selector, 42);
    helper.assertTrue(station.warpTarget == 1, "Hologram did not set the station's warp target");
    space.setBlock(pos.east(), OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
    BlockPos corePos = pos.east(4);
    buildWarpCore(space, corePos);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos.east());
    PortBlockEntity input = WarpCoreLogic.input((OrbitalBlockEntity) space.getBlockEntity(corePos));
    controller.energy.receiveEnergy(1000, false);
    input.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("dilithium_crystal").get()));
    ((OrbitalBlockEntity) space.getBlockEntity(corePos)).tick();
    controller.warpDestination = GalaxyData.EARTH_ID;
    helper.assertTrue(
        WarpLogic.warp(controller),
        "Warp controller ignored holographic target: " + controller.status);
    helper.assertTrue(
        galaxy.stations.get(stationId).warpDestination == 1,
        "Station warped to the controller's stale target");
    HolographicSelectorLogic.control(selector, 48);
    helper.assertTrue(
        !HolographicSelectorLogic.enabled(selector),
        "Signal-controlled hologram stayed active without power");
    space.setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    helper.assertTrue(
        HolographicSelectorLogic.enabled(selector),
        "Signal-controlled hologram did not light when powered");
    BlockPos panelPos = pos.south(2);
    space.setBlock(panelPos, OrbitalRegistry.PLANET_SELECTOR.get().defaultBlockState(), 2);
    OrbitalBlockEntity panel = (OrbitalBlockEntity) space.getBlockEntity(panelPos);
    helper.assertTrue(
        panel.selectorControls() && !panel.holographicSelector(),
        "Standalone planet selector was not registered separately from the hologram");
    HolographicSelectorLogic.control(panel, 41);
    helper.assertTrue(panel.holoTarget >= 0, "Planet selector could not navigate the star system");
    panel.holoTarget = 0;
    HolographicSelectorLogic.control(panel, 42);
    helper.assertTrue(
        galaxy.stations.get(stationId).warpTarget == 0,
        "Planet selector did not set the station warp target");
    galaxy.stations.remove(stationId);
    galaxy.setDirty();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void orientationControllerStoresRotationAndHonorsAnchor(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    int coordinate = 600000 + (int) stationId * 256;
    station.x = coordinate;
    station.z = coordinate;
    BlockPos pos = new BlockPos(coordinate, 120, coordinate);
    space.setBlock(
        pos, OrbitalRegistry.STATION_ORIENTATION_CONTROLLER.get().defaultBlockState(), 2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
    StationMotionLogic.adjustRotation(controller, 1, 10);
    StationMotionLogic.tickOrientation(controller);
    double angle = galaxy.stations.get(stationId).rotationAngle[1];
    helper.assertTrue(
        Math.abs(angle - .05) < .000001 && StationMotionLogic.targetRotation(controller, 1) == 10,
        "Station rotation target did not advance its angle");
    galaxy.stations.get(stationId).anchored = true;
    StationMotionLogic.tickOrientation(controller);
    helper.assertTrue(
        galaxy.stations.get(stationId).rotationAngle[1] == angle, "Anchored station kept rotating");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void altitudeControllerMovesPersistentStation(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    int coordinate = 500000 + (int) stationId * 256;
    station.x = coordinate;
    station.z = coordinate;
    BlockPos pos = new BlockPos(coordinate, 120, coordinate);
    space.setBlock(pos, OrbitalRegistry.STATION_ALTITUDE_CONTROLLER.get().defaultBlockState(), 2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
    StationMotionLogic.adjustAltitude(controller, 10);
    helper.assertTrue(
        StationMotionLogic.targetAltitude(controller) == 14,
        "Station target altitude did not change");
    for (int tick = 0; tick < 20; tick++) StationMotionLogic.tickAltitude(controller);
    helper.assertTrue(
        galaxy.stations.get(stationId).altitude > 4,
        "Station altitude did not advance toward its target");
    float anchoredAltitude = galaxy.stations.get(stationId).altitude;
    galaxy.stations.get(stationId).anchored = true;
    StationMotionLogic.tickAltitude(controller);
    helper.assertTrue(
        galaxy.stations.get(stationId).altitude == anchoredAltitude,
        "Anchored station changed altitude");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void verticalDockingPortsAttachModule(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    BlockPos destination = new BlockPos(300000 + (int) stationId * 256, 130, 300256);
    station.x = destination.getX();
    station.z = destination.getZ();
    space.setBlock(
        destination,
        OrbitalRegistry.DOCKING_PORT
            .get()
            .defaultBlockState()
            .setValue(DockingPortBlock.FACING, Direction.DOWN),
        2);
    OrbitalBlockEntity port = (OrbitalBlockEntity) space.getBlockEntity(destination);
    String label = "vertical_" + stationId;
    helper.assertTrue(
        DockingPortLogic.configure(port, label, ""), "Vertical station port could not be labeled");
    RocketStructure module = new RocketStructure();
    CompoundTag portData = new CompoundTag();
    portData.putString("docking_target", label);
    module.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            OrbitalRegistry.DOCKING_PORT
                .get()
                .defaultBlockState()
                .setValue(DockingPortBlock.FACING, Direction.UP),
            portData));
    module.cells.add(
        new RocketStructure.Cell(
            new BlockPos(0, -1, 0), Blocks.STONE.defaultBlockState(), new CompoundTag()));
    helper.assertTrue(
        DockingPortLogic.attach(space, galaxy, module, 0) == stationId,
        "Vertical docking did not match opposite up/down faces");
    helper.assertTrue(
        space.getBlockState(destination.below()).is(OrbitalRegistry.DOCKING_PORT.get())
            && space.getBlockState(destination.below(2)).is(Blocks.STONE),
        "Vertical module was not placed below the station port");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void matchingDockingPortsAttachRotatedModule(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    var space = PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    BlockPos destination = new BlockPos(300000 + (int) stationId * 256, 120, 300000);
    station.x = destination.getX();
    station.z = destination.getZ();
    space.setBlock(
        destination,
        OrbitalRegistry.DOCKING_PORT
            .get()
            .defaultBlockState()
            .setValue(DockingPortBlock.FACING, Direction.EAST),
        2);
    OrbitalBlockEntity port = (OrbitalBlockEntity) space.getBlockEntity(destination);
    String label = "cargo_" + stationId;
    helper.assertTrue(
        DockingPortLogic.configure(port, label, ""), "Station port could not be labeled");
    RocketStructure module = new RocketStructure();
    CompoundTag portData = new CompoundTag();
    portData.putString("docking_id", "module");
    portData.putString("docking_target", label);
    module.cells.add(
        new RocketStructure.Cell(
            new BlockPos(1, 0, 0),
            OrbitalRegistry.DOCKING_PORT
                .get()
                .defaultBlockState()
                .setValue(DockingPortBlock.FACING, Direction.EAST),
            portData));
    module.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO, Blocks.STONE.defaultBlockState(), new CompoundTag()));
    Station packedRecord = new Station();
    packedRecord.deployed = false;
    packedRecord.structure = module.save(true);
    long moduleId = galaxy.newStation(packedRecord);
    ItemStack packed = new ItemStack(OrbitalRegistry.STATION.get());
    packed.set(ModComponents.STATION_LINK, new StationLink(moduleId));
    BlockPos hatchPos = helper.absolutePos(new BlockPos(4, 3, 4));
    level.setBlock(hatchPos, OrbitalRegistry.HATCH.get().defaultBlockState(), 2);
    OrbitalBlockEntity hatch = (OrbitalBlockEntity) level.getBlockEntity(hatchPos);
    hatch.inventory.setStackInSlot(0, packed);
    RocketStructure rocket = new RocketStructure();
    rocket.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            OrbitalRegistry.HATCH.get().defaultBlockState(),
            hatch.saveWithFullMetadata(level.registryAccess())));
    helper.assertTrue(
        StationLogic.deployCargo(level, rocket, hatchPos) == 1,
        "Module did not attach to matching station port");
    helper.assertTrue(
        galaxy.stations.get(moduleId).attachedTo == stationId
            && space.getBlockState(destination.east()).is(OrbitalRegistry.DOCKING_PORT.get())
            && space.getBlockState(destination.east(2)).is(Blocks.STONE)
            && space.getBlockState(destination).is(OrbitalRegistry.DOCKING_PORT.get()),
        "Rotated module mismatch: attached="
            + galaxy.stations.get(moduleId).attachedTo
            + " expected="
            + stationId
            + " first="
            + space.getBlockState(destination.east())
            + " second="
            + space.getBlockState(destination.east(2)));
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void warpConsumesDilithiumAndChangesOrbit(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    int stationCoordinate = 200000 + (galaxy.stations.size() + 1) * 256;
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = stationCoordinate;
    station.y = 120;
    station.z = stationCoordinate;
    long id = galaxy.newStation(station);
    var space = PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos corePos = new BlockPos(stationCoordinate, 120, stationCoordinate);
    BlockPos controlPos = corePos.north(40);
    buildWarpCore(space, corePos);
    space.setBlock(controlPos, OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
    OrbitalBlockEntity core = (OrbitalBlockEntity) space.getBlockEntity(corePos);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(controlPos);
    PortBlockEntity input = WarpCoreLogic.input(core);
    input.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("dilithium_crystal").get(), 2));
    core.tick();
    controller.energy.receiveEnergy(1000, false);
    controller.warpDestination = 1;
    helper.assertTrue(
        WarpLogic.cost(galaxy.planets.get(GalaxyData.EARTH_ID), galaxy.planets.get(1)) == 1,
        "Parent-moon warp cost differs from original rules");
    var target = galaxy.planets.get(1);
    IdOrTag requirement = new IdOrTag("minecraft:diamond", 2);
    IdOrTag batteryRequirement = IdOrTag.of("adv_rocketry:advanced_battery");
    target.artifacts.add(requirement);
    target.artifacts.add(batteryRequirement);
    try {
      helper.assertTrue(
          !WarpLogic.warp(controller), "Warp ignored the destination's required artifact");
      controller.inventory.setStackInSlot(4, new ItemStack(Items.DIAMOND, 2));
      controller.inventory.setStackInSlot(
          5, new ItemStack(ProcessingRegistry.PART_ITEMS.get("battery").get()));
      helper.assertTrue(
          !WarpLogic.warp(controller),
          "Warp accepted a basic battery for an advanced battery artifact");
      controller.inventory.setStackInSlot(
          5, new ItemStack(ProcessingRegistry.PART_ITEMS.get("advanced_battery").get()));
      helper.assertTrue(WarpLogic.warp(controller), "Warp did not start: " + controller.status);
      helper.assertTrue(
          galaxy.stations.get(id).orbitPlanet == -1, "Station did not enter warp transit");
      helper.assertTrue(
          input.inventory.getStackInSlot(0).getCount() == 0
              && galaxy.stations.get(id).fuel
                  == AdvancedRocketryConfig.fuelPointsPerDilithium() * 2 - 1
              && controller.energy.getEnergyStored() == 0,
          "Warp fuel or controller energy was not charged");
      helper.assertTrue(!WarpLogic.warp(controller), "Station warped to its current planet");
      WarpLogic.complete(galaxy, galaxy.stations.get(id).warpEta);
      helper.assertTrue(
          galaxy.stations.get(id).orbitPlanet == 1, "Station did not reach Luna orbit");
      helper.succeed();
    } finally {
      target.artifacts.remove(requirement);
      target.artifacts.remove(batteryRequirement);
    }
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void rocketCanLandAtDeployedStation(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = 1024;
    station.y = 120;
    station.z = 1024;
    long stationId = galaxy.newStation(station);
    var space = PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    space.setBlock(new BlockPos(1024, 120, 1024), Blocks.IRON_BLOCK.defaultBlockState(), 2);
    RocketStructure structure = new RocketStructure();
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("rocket_motor").get().defaultBlockState(),
            new CompoundTag()));
    structure.thrust = 1000;
    structure.consumption = 1;
    structure.capacity = 10000;
    structure.destination = GalaxyData.SPACE_ID;
    structure.stationId = stationId;
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(structure, helper.absolutePos(new BlockPos(8, 990, 8)));
    rocket.fluids.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 10000),
        IFluidHandler.FluidAction.EXECUTE);
    CompoundTag saved = rocket.saveWithoutId(new CompoundTag());
    saved.putInt("flight", 1);
    saved.putDouble("vertical_speed", 5);
    rocket.load(saved);
    level.addFreshEntity(rocket);
    var uuid = rocket.getUUID();
    helper.succeedWhen(
        () -> {
          var landed = space.getEntity(uuid);
          helper.assertTrue(landed instanceof RocketEntity, "Rocket has not reached space station");
          RocketEntity ship = (RocketEntity) landed;
          helper.assertTrue(
              ship.flight() == 0,
              "Rocket has not landed at station: y=" + ship.getY() + ", flight=" + ship.flight());
          helper.assertTrue(
              ship.structure().destination == GalaxyData.EARTH_ID,
              "Station flight did not preserve Earth as return destination");
          helper.assertTrue(
              Math.abs(ship.getX() - 1024) < 32, "Rocket landed away from its destination station");
          ship.discard();
        });
  }

  private static void buildWarpCore(Level level, BlockPos controller) {
    TestMultiblocks.build(level, "warp_core", controller, Direction.NORTH);
  }

  @GameTest(template = "empty")
  public static void warpCoreRequiresFrameAndCapsStationFuel(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    int coordinate = 900000 + (galaxy.stations.size() + 1) * 256;
    BlockPos corePos = new BlockPos(coordinate, 120, coordinate);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = coordinate;
    station.z = coordinate;
    long stationId = galaxy.newStation(station);
    buildWarpCore(space, corePos);
    OrbitalBlockEntity core = (OrbitalBlockEntity) space.getBlockEntity(corePos);
    helper.assertTrue(WarpCoreLogic.complete(core), "Original warp-core frame did not form");
    helper.assertTrue(
        WarpCoreLogic.input(core).getBlockPos().getY() == corePos.getY() + 2,
        "Warp-core input hatch was mirrored below its controller");
    PortBlockEntity input = WarpCoreLogic.input(core);
    input.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("dilithium_crystal").get(), 3));
    WarpCoreLogic.refuel(core);
    helper.assertTrue(
        station.fuel == WarpCoreLogic.MAX_FUEL && input.inventory.getStackInSlot(0).getCount() == 1,
        "Warp core exceeded station fuel cap or consumed an extra crystal");
    CompoundTag savedGalaxy = galaxy.save(new CompoundTag(), space.registryAccess());
    GalaxyData restoredGalaxy = GalaxyData.load(savedGalaxy, space.registryAccess());
    helper.assertTrue(
        restoredGalaxy.stations.get(stationId).fuel == WarpCoreLogic.MAX_FUEL
            && restoredGalaxy.stations.get(stationId).warpCores.length == 1,
        "Warp fuel or formed-core location was lost on station reload");
    BlockPos rim =
        TestMultiblocks.at(space, "warp_core", corePos, Direction.NORTH).position(2, 0, 0);
    space.removeBlock(rim, false);
    helper.assertTrue(!WarpCoreLogic.complete(core), "Broken warp-core frame stayed formed");
    core.tick();
    helper.assertTrue(
        station.warpCores.length == 0, "Broken warp core remained registered on the station");
    space.setBlock(rim, ProcessingRegistry.part("titanium_block").defaultBlockState(), 2);
    core.tick();
    helper.assertTrue(station.warpCores.length == 1, "Repaired warp core did not register again");
    space.removeBlock(corePos, false);
    helper.assertTrue(
        station.warpCores.length == 0,
        "Removed warp-core controller remained registered on the station");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void packedStationDeploysIntoSpace(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos origin = helper.absolutePos(new BlockPos(3, 3, 3));
    for (int x = 0; x < 3; x++)
      for (int z = 0; z < 3; z++)
        level.setBlock(
            origin.offset(x, -1, z),
            RocketRegistry.PARTS.get("launch_pad").get().defaultBlockState(),
            2);
    for (int y = 0; y < 5; y++)
      level.setBlock(
          origin.offset(-1, y, 0), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 2);
    level.setBlock(origin, OrbitalRegistry.HATCH.get().defaultBlockState(), 2);
    level.setBlock(origin.offset(1, 0, 0), Blocks.IRON_BLOCK.defaultBlockState(), 2);
    BlockPos builderPos = origin.offset(-1, 0, 1);
    level.setBlock(
        builderPos, RocketRegistry.PARTS.get("station_builder").get().defaultBlockState(), 2);
    RocketBlockEntity builder = (RocketBlockEntity) level.getBlockEntity(builderPos);
    builder.energy.receiveEnergy(1000, false);
    builder.stationInventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.HATCH.get()));
    builder.stationInventory.setStackInSlot(1, new ItemStack(OrbitalRegistry.STATION_CHIP.get()));
    StationAssembly.pack(builder, FakePlayerFactory.getMinecraft(level));
    ItemStack module = builder.stationInventory.getStackInSlot(2);
    helper.assertTrue(
        !module.isEmpty(), "Station assembler did not pack its pad contents: " + builder.status);
    helper.assertTrue(
        builder.stationInventory.getStackInSlot(0).isEmpty()
            && builder.stationInventory.getStackInSlot(1).isEmpty()
            && StationLink.read(builder.stationInventory.getStackInSlot(3)) > 0,
        "Station assembler did not consume inputs and program its output chip");
    long id = StationLink.read(module);
    helper.assertTrue(
        id > 0 && level.isEmptyBlock(origin) && level.isEmptyBlock(origin.offset(1, 0, 0)),
        "Station packing did not remove the captured blocks");
    BlockPos hatchPos = helper.absolutePos(new BlockPos(12, 3, 3));
    level.setBlock(hatchPos, OrbitalRegistry.HATCH.get().defaultBlockState(), 2);
    OrbitalBlockEntity hatch = (OrbitalBlockEntity) level.getBlockEntity(hatchPos);
    hatch.inventory.setStackInSlot(0, module.copy());
    RocketStructure rocket = new RocketStructure();
    rocket.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            OrbitalRegistry.HATCH.get().defaultBlockState(),
            hatch.saveWithFullMetadata(level.registryAccess())));
    RocketStructure restored = RocketStructure.load(rocket.save(true));
    helper.assertTrue(
        StationLogic.deployCargo(level, restored, hatchPos) == 1,
        "Rocket could not deploy packed station module");
    helper.assertTrue(
        StationLogic.deployCargo(level, restored, hatchPos) == 0,
        "Station module was deployed twice");
    var record = GalaxyData.get(level.getServer()).stations.get(id);
    var space =
        PlanetRuntime.create(
            level.getServer(), GalaxyData.get(level.getServer()).planets.get(GalaxyData.SPACE_ID));
    BlockPos stationPos = new BlockPos(record.x, 120, record.z);
    helper.assertTrue(record.deployed && record.orbitPlanet == 0, "Station orbit was not saved");
    helper.assertTrue(
        record.radius == AdvancedRocketryConfig.stationBuildRadius()
            && StationLogic.at(
                    GalaxyData.get(level.getServer()), space, stationPos.offset(500, 0, 500))
                == record,
        "New station did not retain its configurable build radius");
    helper.assertTrue(
        space.getBlockState(stationPos).is(OrbitalRegistry.HATCH.get())
            && space.getBlockState(stationPos.east()).is(Blocks.IRON_BLOCK),
        "Station blocks were not restored in space");
    helper.succeed();
  }
}

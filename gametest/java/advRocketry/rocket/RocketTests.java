package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.GasMission;
import advRocketry.orbit.Mission;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.PlanetIdChipItem;
import advRocketry.orbit.Station;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import advRocketry.util.AssemblyFailure;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class RocketTests {
  @GameTest(template = "empty")
  public static void assemblyHonorsProtectionBeforeRemovingBlocks(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos origin = helper.absolutePos(new BlockPos(2, 3, 2));
    level.setBlock(origin, Blocks.STONE.defaultBlockState(), 3);
    var builder = new RocketBlockEntity(origin.below(), RocketRegistry.PARTS.get("rocket_builder").get().defaultBlockState());
    builder.setLevel(level);
    RocketStructure structure = new RocketStructure();
    structure.cells.add(new RocketStructure.Cell(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), new CompoundTag()));
    Consumer<BlockEvent.BreakEvent> deny = event -> {
      if (event.getLevel() == level && event.getPos().equals(origin)) event.setCanceled(true);
    };
    NeoForge.EVENT_BUS.addListener(deny);
    try {
      boolean denied = false;
      try {
        RocketAssembly.commit(builder, level, structure, origin, rocket -> rocket.initialize(structure, origin), Component.literal("Rocket"));
      } catch (AssemblyFailure expected) {
        denied = true;
      }
      helper.assertTrue(denied && level.getBlockState(origin).is(Blocks.STONE)
          && builder.lastAssembledRocket == null, "Protected assembly removed blocks or spawned a rocket");
    } finally {
      NeoForge.EVENT_BUS.unregister(deny);
    }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 40)
  public static void placedSeatMountReleasesRiderWhenBroken(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(2, 3, 2));
    level.setBlock(pos, RocketRegistry.PARTS.get("seat").get().defaultBlockState(), 3);
    SeatEntity mount = RocketRegistry.SEAT_MOUNT.get().create(level);
    var rider = EntityType.PIG.create(level);
    helper.assertTrue(mount != null && rider != null, "Seat mount or test rider was absent");
    mount.setPos(pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5);
    rider.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
    level.addFreshEntity(mount);
    level.addFreshEntity(rider);
    helper.assertTrue(rider.startRiding(mount), "Placed seat did not accept a rider");
    helper.runAfterDelay(
        2,
        () -> {
          helper.assertTrue(
              !mount.isRemoved() && rider.getVehicle() == mount, "Seat lost its rider");
          level.removeBlock(pos, false);
          helper.runAfterDelay(
              2,
              () -> {
                helper.assertTrue(
                    mount.isRemoved() && rider.getVehicle() == null,
                    "Broken seat retained its mount");
                helper.succeed();
              });
        });
  }

  @GameTest(template = "empty")
  public static void originalMonopropellantMultiplier(GameTestHelper helper) {
    var fuel =
        RocketFuelRegistry.fluidId(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 1));
    helper.assertTrue(
        RocketFuelRegistry.multiplier(RocketPartBlock.Fuel.MONOPROPELLANT, fuel) == 2
            && RocketFuelRegistry.cost(7, RocketPartBlock.Fuel.MONOPROPELLANT, fuel) == 4
            && RocketFuelRegistry.unroundedCost(1, 0, RocketPartBlock.Fuel.MONOPROPELLANT, fuel)
                == .5
            && RocketFuelRegistry.availablePoints(1, .5, RocketPartBlock.Fuel.MONOPROPELLANT, fuel)
                == 1
            && RocketFuelRegistry.multiplier(
                    RocketPartBlock.Fuel.NUCLEAR,
                    ResourceLocation.fromNamespaceAndPath("adv_rocketry", "hydrogen"))
                == 1
            && RocketFuelRegistry.multiplier(
                    RocketPartBlock.Fuel.MONOPROPELLANT,
                    ResourceLocation.withDefaultNamespace("water"))
                == 0
            && RocketFuelRegistry.maximumMultiplier(RocketPartBlock.Fuel.MONOPROPELLANT) == 2,
        "Original rocket-fuel multiplier did not convert fuel points to millibuckets");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalTransBodyBurnTapersWithMoonDistance(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var earth = galaxy.planets.get(GalaxyData.EARTH_ID);
    var moon = galaxy.planets.get(1);
    helper.assertTrue(
        RocketTrajectory.injectionBurn(earth, moon, false, 100, 1, 10) == 122,
        "Moon-distance taper did not match the original square-root burn");
    helper.assertTrue(
        RocketTrajectory.injectionBurn(earth, earth, false, 100, 1, 10) == 0,
        "Same-body flight incorrectly needs a trans-body burn");
    helper.assertTrue(
        RocketTrajectory.injectionBurn(earth, moon, true, 100, 4, 10) == 200,
        "Asteroid burn multiplier was ignored");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void orbitThresholdChangesFuelRequirement(GameTestHelper helper) {
    RocketStructure structure = new RocketStructure();
    structure.thrust = 1000;
    structure.consumption = 10;
    int defaultFuel = structure.requiredFuel(64, 1f);
    int higherOrbitFuel = structure.requiredFuel(64, 1f, 1200);
    helper.assertTrue(
        AdvancedRocketryConfig.orbitHeight() == 1000
            && AdvancedRocketryConfig.stationClearanceHeight() == 1000
            && defaultFuel > 0
            && higherOrbitFuel > defaultFuel,
        "Configured orbit threshold did not affect rocket fuel requirement");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void exhaustFireAgesAndScorchConvertsBlocks(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
    level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
    RocketFireBlock fire = RocketRegistry.ROCKET_FIRE.get();
    level.setBlock(pos, fire.defaultBlockState().setValue(RocketFireBlock.AGE, 14), 3);
    fire.tick(level.getBlockState(pos), level, pos, RandomSource.create(1));
    helper.assertTrue(
        level.getBlockState(pos).getValue(RocketFireBlock.AGE) == 15,
        "Rocket exhaust did not age toward ordinary fire");
    helper.assertTrue(
        RocketScorch.damaged(Blocks.STONE.defaultBlockState()).is(Blocks.COBBLESTONE),
        "Rocket exhaust did not damage stone into cobblestone");
    helper.assertTrue(
        RocketScorch.damaged(Blocks.COBBLESTONE.defaultBlockState())
            .is(ProcessingRegistry.part("basalt")),
        "Rocket exhaust did not damage cobblestone into basalt");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void hovercraftSteersAscendsAndDropsForPickup(GameTestHelper helper) {
    var level = helper.getLevel();
    HovercraftEntity craft = HovercraftRegistry.ENTITY.get().create(level);
    helper.assertTrue(craft != null, "Hovercraft entity was not registered");
    BlockPos start = helper.absolutePos(new BlockPos(5, 4, 5));
    craft.setPos(start.getX() + .5, start.getY(), start.getZ() + .5);
    helper.assertTrue(level.addFreshEntity(craft), "Hovercraft could not be placed");
    var pilot = helper.makeMockPlayer(GameType.SURVIVAL);
    helper.assertTrue(pilot.startRiding(craft), "Hovercraft rejected its pilot");
    double startY = craft.getY(), startZ = craft.getZ();
    craft.control(pilot, 1, 0, true, false);
    for (int tick = 0; tick < 10; tick++) craft.tick();
    helper.assertTrue(
        craft.getY() > startY && craft.getZ() > startZ,
        "Hovercraft did not ascend and move forward");
    float originalYaw = craft.getYRot();
    craft.control(pilot, 0, -1, false, true);
    double raisedY = craft.getY();
    for (int tick = 0; tick < 10; tick++) craft.tick();
    helper.assertTrue(
        craft.getYRot() < originalYaw && craft.getY() < raisedY && pilot.getVehicle() == craft,
        "Hovercraft did not steer left and descend with its rider");
    craft.control(pilot, Float.NaN, Float.POSITIVE_INFINITY, true, false);
    craft.tick();
    helper.assertTrue(
        Double.isFinite(craft.getX()) && Float.isFinite(craft.getYRot()),
        "Invalid steering poisoned hovercraft position");
    pilot.stopRiding();
    double cruisingY = craft.getY();
    for (int tick = 0; tick < 10; tick++) craft.tick();
    helper.assertTrue(craft.getY() < cruisingY, "Unoccupied hovercraft did not descend");
    helper.assertTrue(
        craft.hurt(pilot.damageSources().playerAttack(pilot), 1),
        "Hovercraft could not be picked up by its owner");
    helper.assertTrue(craft.isRemoved(), "Picked-up hovercraft remained in the world");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void deployableFrameLaunchesHorizontalGasRocket(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    GalaxyData galaxy = GalaxyData.get(server);
    var giant =
        galaxy.planets.values().stream()
            .filter(planet -> planet.gasGiant)
            .findFirst()
            .orElseThrow();
    var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos testOrigin = helper.absolutePos(new BlockPos(6, 4, 6));
    BlockPos frame =
        new BlockPos(
            2_000_000 + Math.floorMod(testOrigin.getX(), 1_000_000),
            120,
            2_000_000 + Math.floorMod(testOrigin.getZ(), 1_000_000));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = giant.id;
    station.x = frame.getX();
    station.y = frame.getY();
    station.z = frame.getZ();
    long stationId = galaxy.newStation(station);
    space.setBlock(frame.below(), Blocks.IRON_BLOCK.defaultBlockState(), 3);
    space.setBlock(
        frame,
        RocketRegistry.PARTS
            .get("deployable_rocket_builder")
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.EAST),
        3);
    for (int y = 1; y <= 3; y++)
      space.setBlock(frame.above(y), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 3);
    for (int d = 1; d <= 2; d++)
      space.setBlock(
          frame.east(d).above(3), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 3);
    space.setBlock(frame.north(), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 3);
    space.setBlock(frame.south(), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 3);
    space.setBlock(
        frame.east(),
        RocketRegistry.PARTS
            .get("rocket_motor")
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.EAST),
        3);
    space.setBlock(
        frame.east(2), RocketRegistry.PARTS.get("fuel_tank").get().defaultBlockState(), 3);
    space.setBlock(
        frame.east().above(), ProcessingRegistry.part("gas_intake").defaultBlockState(), 3);
    space.setBlock(
        frame.east(2).above(), MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(), 3);
    RocketBlockEntity tank = (RocketBlockEntity) space.getBlockEntity(frame.east(2));
    tank.tank.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 1000),
        IFluidHandler.FluidAction.EXECUTE);
    RocketBlockEntity builder = (RocketBlockEntity) space.getBlockEntity(frame);
    builder.energy.receiveEnergy(10000, false);
    DeployableAssembly.scan(builder);
    helper.assertTrue(
        builder.ready, "Horizontal frame did not scan: " + builder.status.getString());
    builder.beginAssembly();
    helper.assertTrue(builder.buildRemaining > 0, "Deployable assembler did not start its timer");
    for (int tick = 0, duration = builder.buildTotal; tick < duration; tick++) builder.tick();
    RocketEntity rocket = builder.lastAssembledRocket;
    helper.assertTrue(rocket != null, "Horizontal frame did not assemble: " + builder.status);
    helper.assertTrue(
        rocket.structure().intakePower == 10 && GasMission.capacity(rocket.structure()) == 16000,
        "Assembled deployable rocket lost its intake or tank");
    helper.assertTrue(rocket.launchFrom(builder), "Deployable rocket did not launch from station");
    Vec3 berth = rocket.position();
    double initialX = rocket.getX();
    for (int tick = 0; tick < 20; tick++) rocket.tickDeployable(space);
    helper.assertTrue(
        rocket.getX() == initialX && rocket.getY() < berth.y && !rocket.enginesActive(),
        "Gas rocket did not lower clear of its frame before firing horizontally");
    CompoundTag outbound = rocket.saveWithoutId(new CompoundTag());
    rocket.discard();
    rocket = RocketRegistry.ROCKET.get().create(space);
    rocket.load(outbound);
    space.addFreshEntity(rocket);
    for (int tick = 0; tick < 2000 && !rocket.isRemoved(); tick++) rocket.tickDeployable(space);
    helper.assertTrue(
        rocket.getX() > initialX, "Deployable rocket did not fly along its horizontal frame");
    var missionIds =
        galaxy.miningMissions.entrySet().stream()
            .filter(
                entry ->
                    entry.getValue().kind == Mission.Kind.GAS
                        && Math.abs(entry.getValue().rocket.getDouble("launch_x") - frame.getX())
                            < 8)
            .map(entry -> entry.getKey())
            .toList();
    helper.assertTrue(!missionIds.isEmpty(), "Horizontal flight did not start gas collection");
    Mission mission = galaxy.miningMissions.get(missionIds.getFirst());
    RocketEntity returning = GasMission.createReturn(space, mission);
    helper.assertTrue(
        returning != null
            && returning.propellant() == 0
            && returning.saveWithoutId(new CompoundTag()).getDouble("primary_fuel_carry") == 0,
        "Gas mission did not create an empty-propellant return rocket");
    helper.assertTrue(
        Math.abs(returning.getX() - berth.x - 64) < .001
            && Math.abs(returning.getY() - (berth.y - 12)) < .001,
        "Gas rocket did not return 64 blocks forward at its lowered clearance height");
    space.addFreshEntity(returning);
    for (int tick = 0; tick < 100; tick++) returning.tick();
    helper.assertTrue(
        returning.getX() > berth.x
            && returning.getX() < berth.x + 64
            && Math.abs(returning.getY() - (berth.y - 12)) < .001,
        "Return rocket descended instead of approaching its frame horizontally");
    CompoundTag approach = returning.saveWithoutId(new CompoundTag());
    returning.discard();
    returning = RocketRegistry.ROCKET.get().create(space);
    returning.load(approach);
    space.addFreshEntity(returning);
    for (int tick = 0; tick < 1000 && returning.flight() != 0; tick++) returning.tick();
    helper.assertTrue(
        returning.flight() == 0 && returning.position().distanceToSqr(berth) < .000001,
        "Reloaded gas rocket did not lift back into the exact launch berth");
    FluidTank recovered = new FluidTank(64000);
    helper.assertTrue(
        returning.moveCargoFluid(recovered, true) == 16000
            && returning.moveCargoFluid(new FluidTank(64000), true) == 0,
        "Returned gas cargo was lost or duplicated");
    returning.discard();
    missionIds.forEach(galaxy.miningMissions::remove);
    galaxy.stations.remove(stationId);
    galaxy.setDirty();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void emergencyLandingFloatCoversLava(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 3; z++)
        level.setBlock(center.offset(x, 0, z), Blocks.LAVA.defaultBlockState(), 3);
    helper.assertTrue(
        LandingFloatLogic.placeIfNeeded(
            level, center.getX() + .5, center.getZ() + .5, center.getY() + 1, 1, 1),
        "Rocket did not form a float over lava");
    helper.assertTrue(
        level.getBlockState(center).is(RocketRegistry.LANDING_FLOAT.get()),
        "Emergency float did not replace the landing surface");
    helper.assertTrue(
        !LandingFloatLogic.placeIfNeeded(
            level, center.getX() + .5, center.getZ() + .5, center.getY() + 1, 1, 1),
        "A safe landing attempted to create another float");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void rocketCargoPortsTransferPackedHatchContents(GameTestHelper helper) {
    var level = helper.getLevel();
    RocketStructure structure = new RocketStructure();
    CompoundTag itemData = new CompoundTag(), fluidData = new CompoundTag();
    itemData.put("inventory", new ItemStackHandler(4).serializeNBT(level.registryAccess()));
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(),
            itemData));
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO.above(),
            MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(),
            fluidData));
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO.above(2),
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    helper.assertTrue(rocket != null, "Rocket entity is missing");
    rocket.initialize(structure, helper.absolutePos(new BlockPos(8, 4, 8)));
    ItemStackHandler input = new ItemStackHandler(4), output = new ItemStackHandler(4);
    input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
    helper.assertTrue(
        rocket.moveCargoItems(input, false) == 5 && input.getStackInSlot(0).isEmpty(),
        "Item loader did not place cargo in the packed hatch");
    helper.assertTrue(
        rocket.moveCargoItems(output, true) == 5 && output.getStackInSlot(0).getCount() == 5,
        "Item unloader did not remove cargo from the packed hatch");
    var inputFluid = new FluidTank(64000);
    var outputFluid = new FluidTank(64000);
    inputFluid.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 1200),
        IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(
        rocket.moveCargoFluid(inputFluid, false) == 1200
            && rocket.moveCargoFluid(outputFluid, true) == 1200
            && outputFluid.getFluidAmount() == 1200,
        "Fluid loader or unloader lost the packed hatch fluid");
    level.addFreshEntity(rocket);
    BlockPos loaderPos = helper.absolutePos(new BlockPos(5, 4, 8));
    level.setBlock(loaderPos, RocketRegistry.ITEM_LOADER.get().defaultBlockState(), 2);
    RocketCargoPortBlockEntity loader =
        (RocketCargoPortBlockEntity) level.getBlockEntity(loaderPos);
    loader.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 2));
    helper.assertTrue(loader.link(rocket), "Item loader rejected rocket link");
    loader.tick();
    ItemStackHandler received = new ItemStackHandler(4);
    helper.assertTrue(
        loader.inventory.getStackInSlot(0).isEmpty()
            && rocket.moveCargoItems(received, true) == 2
            && received.getStackInSlot(0).is(Items.IRON_INGOT),
        "Registered item loader did not transfer into the rocket hatch");
    BlockPos guidancePos = helper.absolutePos(new BlockPos(5, 4, 9));
    level.setBlock(guidancePos, RocketRegistry.GUIDANCE_ACCESS.get().defaultBlockState(), 2);
    RocketCargoPortBlockEntity access =
        (RocketCargoPortBlockEntity) level.getBlockEntity(guidancePos);
    helper.assertTrue(access.link(rocket), "Guidance hatch rejected rocket link");
    ItemStack planetChip = new ItemStack(OrbitalRegistry.PLANET_CHIP.get());
    PlanetIdChipItem.program(
        planetChip, GalaxyData.get(level.getServer()).planets.get(GalaxyData.EARTH_ID));
    access.guidance().setStackInSlot(0, planetChip);
    helper.assertTrue(
        rocket.guidanceChip().is(OrbitalRegistry.PLANET_CHIP.get())
            && rocket.structure().destination == GalaxyData.EARTH_ID,
        "Guidance access hatch did not program the packed rocket destination");
    rocket.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void monitoringStationLaunchesLinkedRocketOnRedstoneEdge(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.MONITORING_STATION.get().defaultBlockState(), 2);
    RocketMonitoringBlockEntity monitor = (RocketMonitoringBlockEntity) level.getBlockEntity(pos);
    RocketStructure structure = new RocketStructure();
    structure.capacity = 10000;
    structure.thrust = 1000;
    structure.consumption = 1;
    structure.destination = 1;
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    helper.assertTrue(rocket != null, "Rocket entity is missing");
    rocket.initialize(structure, pos.east(3));
    rocket.fluids.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 10000),
        IFluidHandler.FluidAction.EXECUTE);
    level.addFreshEntity(rocket);
    monitor.link(rocket.getUUID());
    helper.assertTrue(
        monitor.value(2) == 10000 && monitor.value(3) == 10000,
        "Monitor did not report linked rocket fuel");
    level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    monitor.tick();
    helper.assertTrue(rocket.flight() == 1, "Redstone did not launch the linked rocket");
    rocket.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void fuelingStationConsumesPowerAndSignalsWhenRocketIsFull(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.FUELING_STATION.get().defaultBlockState(), 2);
    FuelingStationBlockEntity station = (FuelingStationBlockEntity) level.getBlockEntity(pos);
    station.tank.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 100),
        IFluidHandler.FluidAction.EXECUTE);
    station.energy.receiveEnergy(1000, false);
    RocketStructure structure = new RocketStructure();
    structure.capacity = 10;
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    helper.assertTrue(rocket != null, "Rocket entity is missing");
    rocket.initialize(structure, pos.east(3));
    level.addFreshEntity(rocket);
    helper.assertTrue(station.link(rocket), "Fueling station rejected rocket link");
    station.tick();
    helper.assertTrue(
        rocket.propellant() == 10
            && station.tank.getFluidAmount() == 90
            && station.energy.getEnergyStored() == 970,
        "Fueling station did not transfer ten mB for 30 FE");
    station.tick();
    helper.assertTrue(
        level.getBlockState(pos).getValue(FuelingStationBlock.POWERED),
        "Full rocket did not trigger the fueling station redstone signal");
    rocket.discard();
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 4000)
  public static void rocketCompletesPlanetTransferAfterReload(GameTestHelper helper) {
    var level = helper.getLevel();
    RocketStructure structure = new RocketStructure();
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("rocket_motor").get().defaultBlockState(),
            new CompoundTag()));
    structure.thrust = 1000;
    structure.consumption = 1;
    structure.capacity = 10000;
    structure.destination = 1;
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(structure, helper.absolutePos(new BlockPos(8, 990, 8)));
    helper.assertTrue(
        rocket.fluids.fill(
                new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 10000),
                IFluidHandler.FluidAction.EXECUTE)
            == 10000,
        "Empty rocket tanks rejected their propellant");
    CompoundTag saved = rocket.saveWithoutId(new CompoundTag());
    saved.putInt("flight", 1);
    saved.putDouble("vertical_speed", 5);
    rocket.load(saved);
    level.addFreshEntity(rocket);
    var uuid = rocket.getUUID();
    var moon =
        PlanetRuntime.create(level.getServer(), GalaxyData.get(level.getServer()).planets.get(1));
    moon.getChunkAt(rocket.blockPosition());
    helper.succeedWhen(
        () -> {
          var landed = moon.getEntity(uuid);
          helper.assertTrue(
              landed instanceof RocketEntity,
              "Rocket has not reached Luna; ticks="
                  + rocket.tickCount
                  + ", height="
                  + rocket.getY()
                  + ", flight="
                  + rocket.flight()
                  + ", removed="
                  + rocket.isRemoved()
                  + ", level="
                  + rocket.level().dimension().location()
                  + ", target="
                  + rocket.structure().destination
                  + ", fuel="
                  + rocket.propellant());
          RocketEntity transferred = (RocketEntity) landed;
          helper.assertTrue(
              transferred.flight() == 0,
              "Rocket has not completed landing: y="
                  + transferred.getY()
                  + ", ticks="
                  + transferred.tickCount
                  + ", flight="
                  + transferred.flight()
                  + ", moonTime="
                  + moon.getGameTime()
                  + ", earthTime="
                  + level.getGameTime()
                  + ", chunk="
                  + moon.getChunkSource()
                      .hasChunk(transferred.chunkPosition().x, transferred.chunkPosition().z)
                  + ", worldTick="
                  + (level.getServer().getTickTime(moon.dimension()) != null)
                  + ", entityTicking="
                  + moon.getChunkSource()
                      .chunkMap
                      .getDistanceManager()
                      .inEntityTickingRange(transferred.chunkPosition().toLong()));
          helper.assertTrue(
              transferred.structure().destination == GalaxyData.EARTH_ID,
              "Return destination was not retained");
          helper.assertTrue(
              transferred.propellant() < 10000 && transferred.propellant() > 9000,
              "Flight fuel consumption was lost or excessive");
          transferred.discard();
        });
  }

  @GameTest(template = "empty")
  public static void invalidStoredFuelRejectsScanBeforeAssemblyEnergy(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos origin = helper.absolutePos(new BlockPos(5, 3, 5));
    for (int x = 0; x < 3; x++)
      for (int z = 0; z < 3; z++)
        level.setBlock(
            origin.offset(x, -1, z),
            RocketRegistry.PARTS.get("launch_pad").get().defaultBlockState(),
            2);
    for (int y = 0; y < 10; y++)
      level.setBlock(
          origin.offset(-1, y, 0), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 2);
    level.setBlock(
        origin, RocketRegistry.PARTS.get("advanced_rocket_motor").get().defaultBlockState(), 2);
    for (int y = 1; y <= 6; y++) {
      BlockPos pos = origin.above(y);
      level.setBlock(pos, RocketRegistry.PARTS.get("fuel_tank").get().defaultBlockState(), 2);
      ((RocketBlockEntity) level.getBlockEntity(pos))
          .tank.setFluid(
              new FluidStack(
                  ProcessingFluids.DEFINITIONS.get(y == 6 ? "oxygen" : "rocket_fuel").source.get(),
                  1000));
    }
    level.setBlock(
        origin.above(7),
        RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
        2);
    BlockPos pos = origin.offset(-1, 0, 1);
    level.setBlock(pos, RocketRegistry.PARTS.get("rocket_builder").get().defaultBlockState(), 2);
    RocketBlockEntity builder = (RocketBlockEntity) level.getBlockEntity(pos);
    builder.energy.receiveEnergy(50000, false);
    builder.beginAssembly();
    helper.assertTrue(
        builder.status.getContents() instanceof TranslatableContents contents
            && contents.getKey().endsWith("rocket_tank_contains_an_unconfigured_fuel")
            && builder.buildRemaining == 0,
        "Invalid stored propellant started an assembly job: " + builder.status);
    builder.tick();
    helper.assertTrue(
        builder.energy.getEnergyStored() == 50000 && builder.lastAssembledRocket == null,
        "Rejected scan spent energy or created a rocket");
    for (int y = 1; y <= 6; y++)
      helper.assertTrue(
          ((RocketBlockEntity) level.getBlockEntity(origin.above(y))).tank.getFluidAmount() == 1000,
          "Rejected scan changed tank contents");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void assembledRocketPreservesCargoAndFuel(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos origin = helper.absolutePos(new BlockPos(5, 3, 5));
    for (int x = 0; x < 3; x++)
      for (int z = 0; z < 3; z++)
        level.setBlock(
            origin.offset(x, -1, z),
            RocketRegistry.PARTS.get("launch_pad").get().defaultBlockState(),
            2);
    for (int y = 0; y < 10; y++)
      level.setBlock(
          origin.offset(-1, y, 0), MachinePorts.STRUCTURE_TOWER.get().defaultBlockState(), 2);
    level.setBlock(
        origin, RocketRegistry.PARTS.get("advanced_rocket_motor").get().defaultBlockState(), 2);
    for (int y = 1; y <= 6; y++) {
      BlockPos pos = origin.offset(0, y, 0);
      level.setBlock(pos, RocketRegistry.PARTS.get("fuel_tank").get().defaultBlockState(), 2);
      ((RocketBlockEntity) level.getBlockEntity(pos))
          .tank.fill(
              new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 1000),
              IFluidHandler.FluidAction.EXECUTE);
    }
    level.setBlock(
        origin.offset(0, 7, 0),
        RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
        2);
    level.setBlock(
        origin.offset(1, 0, 0), RocketRegistry.PARTS.get("seat").get().defaultBlockState(), 2);
    level.setBlock(
        origin.offset(2, 0, 0), ProcessingRegistry.part("gas_intake").defaultBlockState(), 2);
    BlockPos cargo = origin.offset(1, 1, 0);
    level.setBlock(cargo, MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 2);
    ((PortBlockEntity) level.getBlockEntity(cargo))
        .inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
    BlockPos builderPos = origin.offset(-1, 0, 1);
    level.setBlock(
        builderPos, RocketRegistry.PARTS.get("rocket_builder").get().defaultBlockState(), 2);
    RocketBlockEntity builder = (RocketBlockEntity) level.getBlockEntity(builderPos);
    builder.energy.receiveEnergy(12000, false);
    builder.beginAssembly();
    helper.assertTrue(builder.buildRemaining > 0, "Rocket build timer did not start");
    int buildTicks = builder.buildTotal;
    helper.assertTrue(
        builder.saveWithFullMetadata(level.registryAccess()).getInt("build_remaining")
            == buildTicks,
        "Rocket build timer was not persisted");
    for (int tick = 0; tick < buildTicks; tick++) builder.tick();
    var rockets = level.getEntitiesOfClass(RocketEntity.class, new AABB(origin).inflate(16));
    helper.assertTrue(rockets.size() == 1, "Assembly failed: " + builder.status);
    RocketEntity rocket = rockets.getFirst();
    helper.assertTrue(level.isEmptyBlock(cargo), "Cargo hatch was not removed from world");
    helper.assertTrue(rocket.propellant() == 6000, "Filled tanks were lost during assembly");
    helper.assertTrue(
        rocket.structure().intakePower == 10,
        "Original gas intake power was not captured in the packed rocket");
    helper.assertTrue(
        rocket.fluids.fill(
                new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 1000),
                IFluidHandler.FluidAction.EXECUTE)
            == 0,
        "Monopropellant rocket accepted oxygen");
    helper.assertTrue(
        builder.energy.getEnergyStored() == 12000 - buildTicks * 100,
        "Timed assembly did not charge 100 FE per tick");
    CompoundTag saved = rocket.saveWithoutId(new CompoundTag());
    rocket.discard();
    RocketEntity restored = RocketRegistry.ROCKET.get().create(level);
    restored.load(saved);
    level.addFreshEntity(restored);
    helper.assertTrue(
        restored.propellant() == 6000
            && restored
                .fluids
                .getFluidInTank(0)
                .is(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get())
            && restored.structure().cells.size() == 11
            && restored.structure().intakePower == 10,
        "Rocket state changed on reload");
    helper.assertTrue(
        restored.launchFrom(builder),
        "Builder could not launch a linked uncrewed rocket after reload");
    helper.assertTrue(restored.flight() == 1, "Uncrewed launch did not start ascent");
    restored.tick();
    CompoundTag inFlight = restored.saveWithoutId(new CompoundTag());
    helper.assertTrue(
        inFlight.getDouble("primary_fuel_carry") == .5,
        "A partial millibucket of rocket-fuel consumption was rounded away");
    RocketEntity reloadedFlight = RocketRegistry.ROCKET.get().create(level);
    reloadedFlight.load(inFlight);
    helper.assertTrue(
        reloadedFlight.saveWithoutId(new CompoundTag()).getDouble("primary_fuel_carry") == .5,
        "Partial fuel consumption did not survive a flight reload");
    restored.discard();
    restored = RocketRegistry.ROCKET.get().create(level);
    restored.load(saved);
    level.addFreshEntity(restored);
    restored.disassemble(helper.makeMockPlayer(GameType.SURVIVAL));
    helper.assertTrue(restored.isRemoved(), "Rocket did not disassemble");
    helper.assertTrue(
        ((PortBlockEntity) level.getBlockEntity(cargo)).inventory.getStackInSlot(0).getCount() == 3,
        "Cargo was lost or duplicated on disassembly");
    helper.assertTrue(
        ((RocketBlockEntity) level.getBlockEntity(origin.above())).tank.getFluidAmount() == 1000,
        "Propellant was lost during disassembly");
    helper.succeed();
  }
}

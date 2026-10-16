package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import advRocketry.TestMultiblocks;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import advRocketry.space.Star;
import advRocketry.space.TerraformBiomeLogic;
import advRocketry.util.IdOrTag;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.QuartPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class OrbitalTests {
  @GameTest(template = "empty")
  public static void externalTerraformingGetsDistinctSavedPlanet(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
    Planet existing = galaxy.byDimension(nether.dimension().location());
    Planet external = galaxy.ensureExternalPlanet(nether);
    helper.assertTrue(
        external.id <= -3
            && external.dimension.equals(nether.dimension().location())
            && galaxy.planet(nether) == external,
        "External dimension reused Earth instead of getting its own terraforming state");
    GalaxyData loaded =
        GalaxyData.load(
            galaxy.save(new CompoundTag(), helper.getLevel().registryAccess()),
            helper.getLevel().registryAccess());
    helper.assertTrue(
        loaded.byDimension(nether.dimension().location()) != null
            && loaded.byDimension(nether.dimension().location()).id == external.id,
        "External dimension terraforming identity was not saved with the galaxy");
    if (existing == null) {
      galaxy.planets.remove(external.id);
      galaxy.setDirty();
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void nonArTerraformingDefaultsToDisabled(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    helper.assertTrue(
        !TerraformBiomeLogic.eligible(galaxy.planets.get(GalaxyData.EARTH_ID), helper.getLevel())
            && TerraformBiomeLogic.eligible(
                galaxy.planets.get(1),
                PlanetRuntime.create(helper.getLevel().getServer(), galaxy.planets.get(1))),
        "Default terraforming policy allowed the Overworld or rejected an AR planet");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void poweredMicrowaveBeamIgnitesEntitiesInPanelColumn(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos center = helper.absolutePos(new BlockPos(7, 4, 7));
    ItemEntity inside =
        new ItemEntity(
            level,
            center.getX() + .5,
            center.getY() + 3,
            center.getZ() + .5,
            new ItemStack(Items.STICK));
    ItemEntity outside =
        new ItemEntity(
            level, center.getX() + 5, center.getY() + 3, center.getZ(), new ItemStack(Items.STICK));
    level.addFreshEntity(inside);
    level.addFreshEntity(outside);
    MicrowaveReceiverLogic.burnBeam(level, center, 50);
    helper.assertTrue(
        inside.getRemainingFireTicks() == 100 && outside.getRemainingFireTicks() <= 0,
        "Microwave receiver did not confine its original heat hazard to the active panel");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void observatoryClosesAndSavesOpeningProgress(GameTestHelper helper) {
    BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
    helper.getLevel().setBlock(pos, OrbitalRegistry.OBSERVATORY.get().defaultBlockState(), 2);
    OrbitalBlockEntity observatory = (OrbitalBlockEntity) helper.getLevel().getBlockEntity(pos);
    observatory.observation.openProgress = 100;
    observatory.tick();
    helper.assertTrue(
        observatory.observation.openProgress == 99,
        "Incomplete observatory did not close its original moving casing");
    CompoundTag saved = observatory.saveWithFullMetadata(helper.getLevel().registryAccess());
    observatory.observation.openProgress = 0;
    observatory.loadWithComponents(saved, helper.getLevel().registryAccess());
    helper.assertTrue(
        observatory.observation.openProgress == 99,
        "Observatory casing position was lost on reload");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void blackHoleGeneratorBurnsItemsOnlyInBlackHoleOrbit(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    galaxy.stars.put(42, new Star(42, "Test Black Hole", 100, 1, 0, 0, true));
    Planet planet = new Planet(42, "Black Hole Orbit");
    planet.star = 42;
    galaxy.planets.put(42, planet);
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5)).atY(120);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = 42;
    station.x = pos.getX();
    station.z = pos.getZ();
    galaxy.newStation(station);
    space.setBlock(pos, OrbitalRegistry.BLACK_HOLE_GENERATOR.get().defaultBlockState(), 2);
    BlockPos back = pos.south();
    for (BlockPos cell :
        new BlockPos[] {
          back.below(), back, pos.above(), back.above(), back.above(2), back.above(3), back.south()
        }) space.setBlock(cell, MachinePorts.BLOCK_ADVANCED_STRUCTURE.get().defaultBlockState(), 2);
    space.setBlock(back.east(), MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 2);
    space.setBlock(
        back.west(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    PortBlockEntity input = (PortBlockEntity) space.getBlockEntity(back.east());
    PortBlockEntity output = (PortBlockEntity) space.getBlockEntity(back.west());
    input.inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
    OrbitalBlockEntity generator = (OrbitalBlockEntity) space.getBlockEntity(pos);
    helper.assertTrue(
        BlackHoleGeneratorLogic.complete(generator),
        "Original black-hole generator frame did not form");
    generator.tick();
    helper.assertTrue(
        generator.powerMadeLastTick == 500
            && output.energyStorage.getEnergyStored() == 500
            && input.inventory.getStackInSlot(0).getCount() == 1,
        "Black-hole generator did not burn one stone for 500 FE: generated="
            + generator.powerMadeLastTick
            + ", output="
            + output.energyStorage.getEnergyStored()
            + ", fuel="
            + input.inventory.getStackInSlot(0).getCount()
            + ", status="
            + generator.status);
    station.orbitPlanet = GalaxyData.EARTH_ID;
    generator.tick();
    helper.assertTrue(
        generator.powerMadeLastTick == 0 && input.inventory.getStackInSlot(0).getCount() == 1,
        "Black-hole generator kept burning after leaving black-hole orbit");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void stackedLiquidTanksFillBottomAndDrainTop(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos bottomPos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(bottomPos, OrbitalRegistry.FLUID_TANK.get().defaultBlockState(), 2);
    level.setBlock(bottomPos.above(), OrbitalRegistry.FLUID_TANK.get().defaultBlockState(), 2);
    FluidTankBlockEntity bottom = (FluidTankBlockEntity) level.getBlockEntity(bottomPos);
    FluidTankBlockEntity top = (FluidTankBlockEntity) level.getBlockEntity(bottomPos.above());
    int capacity = AdvancedRocketryConfig.blockTankCapacity();
    helper.assertTrue(capacity == 64000, "Pressurized tank lost its original default capacity");
    int filled =
        top.handler()
            .fill(new FluidStack(Fluids.WATER, capacity + 4000), IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(
        filled == capacity + 4000
            && bottom.tank.getFluidAmount() == capacity
            && top.tank.getFluidAmount() == 4000,
        "Stacked tanks did not fill from the bottom");
    FluidStack drained = bottom.handler().drain(1000, IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(
        drained.getAmount() == 1000
            && bottom.tank.getFluidAmount() == capacity
            && top.tank.getFluidAmount() == 3000,
        "Stacked tanks did not drain from the top");
    List<ItemStack> drops = Block.getDrops(top.getBlockState(), level, top.getBlockPos(), top);
    SimpleFluidContent carried =
        drops.size() == 1 ? drops.getFirst().get(ModComponents.TANK_CONTENT) : null;
    helper.assertTrue(
        carried != null && carried.is(Fluids.WATER) && carried.copy().getAmount() == 3000,
        "Broken tank did not keep its fluid on the dropped item");
    BlockPos placedPos = bottomPos.east(3);
    level.setBlock(placedPos, OrbitalRegistry.FLUID_TANK.get().defaultBlockState(), 2);
    FluidTankBlockEntity placed = (FluidTankBlockEntity) level.getBlockEntity(placedPos);
    placed.applyComponentsFromItemStack(drops.getFirst());
    helper.assertTrue(
        placed.tank.getFluid().is(Fluids.WATER) && placed.tank.getFluidAmount() == 3000,
        "Placed tank did not restore the fluid carried by its item");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void fluidPumpDrainsSourceAndExportsToPort(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(7, 5, 7));
    level.setBlock(pos, OrbitalRegistry.PUMP.get().defaultBlockState(), 2);
    level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 2);
    level.setBlock(pos.east(), MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(), 2);
    PumpBlockEntity pump = (PumpBlockEntity) level.getBlockEntity(pos);
    PortBlockEntity output = (PortBlockEntity) level.getBlockEntity(pos.east());
    pump.energy.receiveEnergy(1000, false);
    pump.tick();
    helper.assertTrue(
        pump.tank.getFluidAmount() == 1000
            && pump.energy.getEnergyStored() == 900
            && level.isEmptyBlock(pos.below()),
        "Pump did not consume one water source for 100 FE");
    pump.tick();
    helper.assertTrue(
        output.myTank.getFluidAmount() == 1000,
        "Pump did not export its stored fluid to the adjacent port");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void forceFieldProjectorExtendsAndRetracts(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
    level.setBlock(
        pos,
        OrbitalRegistry.FORCE_FIELD_PROJECTOR
            .get()
            .defaultBlockState()
            .setValue(ForceFieldProjectorBlock.FACING, Direction.UP),
        2);
    level.setBlock(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    OrbitalBlockEntity projector = (OrbitalBlockEntity) level.getBlockEntity(pos);
    ForceFieldLogic.advance(projector);
    ForceFieldLogic.advance(projector);
    helper.assertTrue(
        projector.fieldLength == 2
            && level.getBlockState(pos.above(1)).is(OrbitalRegistry.FORCE_FIELD.get())
            && level.getBlockState(pos.above(2)).is(OrbitalRegistry.FORCE_FIELD.get()),
        "Powered projector did not extend the force field");
    level.removeBlock(pos.below(), false);
    ForceFieldLogic.advance(projector);
    helper.assertTrue(
        projector.fieldLength == 1 && level.isEmptyBlock(pos.above(2)),
        "Unpowered projector did not retract the last segment");
    level.removeBlock(pos, false);
    helper.assertTrue(
        level.isEmptyBlock(pos.above()), "Breaking the projector left an owned field segment");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void terraformerUsesOriginalFrameAndChangesPressure(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    var moon = galaxy.planets.get(1);
    ServerLevel level = PlanetRuntime.create(helper.getLevel().getServer(), moon);
    BlockPos center = new BlockPos(12288, 120, 12288);
    level.setBlock(center, OrbitalRegistry.ATMOSPHERE_TERRAFORMER.get().defaultBlockState(), 2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) level.getBlockEntity(center);
    TestMultiblocks.build(level, "atmosphere_terraformer", center, Direction.NORTH);
    helper.assertTrue(
        TerraformerLogic.complete(controller), "Original terraformer frame did not form");
    Satellite satellite = new Satellite();
    satellite.type = SatelliteType.byName("biome_changer");
    satellite.deployed = true;
    satellite.orbitPlanet = 1;
    long id = galaxy.newSatellite(satellite);
    ItemStack remote =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("biome_changer_remote").get());
    remote.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    controller.inventory.setStackInSlot(0, remote);
    var terraformer = TestMultiblocks.at(controller, "atmosphere_terraformer");
    for (PortBlockEntity port : terraformer.entities('P', PortBlockEntity.class))
      port.energyStorage.receiveEnergy(1000, false);
    int filled = 0;
    for (PortBlockEntity port : terraformer.entities('L', PortBlockEntity.class))
      if (filled < 2) {
        String fluid = filled++ == 0 ? "nitrogen" : "oxygen";
        port.myTank.fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get(fluid).source.get(), 40),
            IFluidHandler.FluidAction.EXECUTE);
      }
    int before = moon.atmosphere;
    TerraformerLogic.control(controller, 29);
    controller.terraformingTicks = 17999;
    TerraformerLogic.tick(controller);
    helper.assertTrue(
        moon.atmosphere == before + 1
            && controller.terraformerRunning
            && controller.getUpdateTag(level.registryAccess()).getBoolean("terraformer_running"),
        "Terraformer did not raise pressure or synchronize active operation");
    TerraformerLogic.control(controller, 31);
    helper.assertTrue(!controller.terraformerRunning, "Paused terraformer kept its sound active");
    moon.atmosphere = before;
    galaxy.setDirty();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void warpControllerDiscoversAndImportsPlanets(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    int stationCoordinate = 400000 + (int) stationId * 256;
    station.x = stationCoordinate;
    station.z = stationCoordinate;
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = new BlockPos(stationCoordinate, 120, stationCoordinate);
    space.setBlock(pos, OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
    ItemStack chip = new ItemStack(OrbitalRegistry.PLANET_CHIP.get());
    controller.inventory.setStackInSlot(0, chip);
    helper.assertTrue(
        !PlanetDiscoveryLogic.importChip(controller),
        "Blank planet chip was accepted as an Earth discovery");
    ResearchType[] channels = {ResearchType.DISTANCE, ResearchType.MASS, ResearchType.COMPOSITION};
    for (int slot = 1; slot <= 3; slot++) {
      ItemStack unit = new ItemStack(OrbitalRegistry.DATA_UNIT.get());
      DataUnitItem.store(unit, channels[slot - 1], 100);
      controller.inventory.setStackInSlot(slot, unit);
    }
    helper.assertTrue(PlanetDiscoveryLogic.start(controller), "Station discovery did not start");
    for (int tick = 0; tick < 999; tick++) PlanetDiscoveryLogic.tick(controller);
    helper.assertTrue(controller.discoveryTicks == 0, "Discovery did not complete");
    for (int slot = 1; slot <= 3; slot++)
      helper.assertTrue(
          DataUnitItem.research(controller.inventory.getStackInSlot(slot)).amount() == 0,
          "Discovery did not consume all three data channels");
    chip = new ItemStack(OrbitalRegistry.PLANET_CHIP.get());
    PlanetIdChipItem.program(chip, galaxy.planets.get(1));
    controller.inventory.setStackInSlot(0, chip);
    helper.assertTrue(PlanetDiscoveryLogic.importChip(controller), "Planet chip import failed");
    helper.assertTrue(
        PlanetDiscoveryLogic.known(galaxy.stations.get(stationId), 1),
        "Planet discovery was not stored on the station");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void astrobodyProcessorConsumesThreeDataChannels(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    level.setBlock(pos, OrbitalRegistry.ASTROBODY_PROCESSOR.get().defaultBlockState(), 2);
    OrbitalBlockEntity processor = (OrbitalBlockEntity) level.getBlockEntity(pos);
    helper.assertTrue(
        TestMultiblocks.at(processor, "astrobody_data_processor")
            .position(1, 0, 0)
            .equals(pos.east().below()),
        "North-facing processor power hatch was mirrored across its controller");
    TestMultiblocks.build(level, "astrobody_data_processor", pos, Direction.NORTH);
    helper.assertTrue(
        AstrobodyProcessorLogic.complete(processor),
        "Original astrobody processor frame did not form");
    ItemStack chip = new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get());
    AsteroidChipItem.program(chip, "adv_rocketry:small", 12345);
    for (ResearchType channel : ResearchType.ASTEROID_TYPES)
      AsteroidChipItem.addData(chip, channel, 999);
    PortBlockEntity input =
        (PortBlockEntity)
            level.getBlockEntity(
                TestMultiblocks.at(processor, "astrobody_data_processor").position(1, 0, 1));
    PortBlockEntity output =
        (PortBlockEntity)
            level.getBlockEntity(
                TestMultiblocks.at(processor, "astrobody_data_processor").position(1, 0, 2));
    input.inventory.setStackInSlot(0, chip);
    ResearchType[] channels = {ResearchType.COMPOSITION, ResearchType.OPTICAL, ResearchType.MASS};
    for (int channel = 0; channel < 3; channel++) {
      PortBlockEntity bus =
          (PortBlockEntity)
              level.getBlockEntity(
                  TestMultiblocks.at(processor, "astrobody_data_processor")
                      .position(1, 1, channel));
      ItemStack unit = new ItemStack(OrbitalRegistry.DATA_UNIT.get());
      DataUnitItem.store(unit, channels[channel], 1);
      bus.inventory.setStackInSlot(0, unit);
    }
    processor.energy.receiveEnergy(3000, false);
    for (int button = 24; button <= 26; button++)
      AstrobodyProcessorLogic.control(processor, button);
    for (int tick = 0; tick < 22; tick++) processor.tick();
    ItemStack result = output.inventory.getStackInSlot(0);
    helper.assertTrue(
        !result.isEmpty()
            && input.inventory.getStackInSlot(0).isEmpty()
            && processor.inventory.getStackInSlot(0).isEmpty()
            && AsteroidChipItem.data(result, ResearchType.COMPOSITION) == 1000
            && AsteroidChipItem.data(result, ResearchType.OPTICAL) == 1000
            && AsteroidChipItem.data(result, ResearchType.MASS) == 1000
            && processor.energy.getEnergyStored() < 3000,
        "Research data was not consumed into a completed asteroid chip");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void railgunsTransferOnlyWhenPoweredAndOutputHasRoom(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos first = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    BlockPos second = first.east(32);
    OrbitalBlockEntity source = railgunFrame(level, first);
    OrbitalBlockEntity receiver = railgunFrame(level, second);
    helper.assertTrue(
        RailgunLogic.complete(source) && RailgunLogic.complete(receiver),
        "Original eleven-layer railgun frame did not form");
    level.setBlock(
        TestMultiblocks.at(source, "railgun").position(10, 4, 4),
        MachinePorts.ADVANCED_MOTOR.get().defaultBlockState(),
        2);
    helper.assertTrue(
        RailgunLogic.complete(source), "Railgun rejected an original upgraded motor tier");
    ItemStack linker = new ItemStack(ProcessingRegistry.PART_ITEMS.get("linker").get());
    LinkerItem.setTarget(
        linker, LinkTarget.EMPTY.withPosition(GlobalPos.of(level.dimension(), second)));
    source.inventory.setStackInSlot(0, linker);
    PortBlockEntity input =
        (PortBlockEntity)
            level.getBlockEntity(TestMultiblocks.at(source, "railgun").position(10, 1, 3));
    PortBlockEntity output =
        (PortBlockEntity)
            level.getBlockEntity(TestMultiblocks.at(receiver, "railgun").position(10, 1, 5));
    input.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 16));
    source.railgunMinimum = 16;
    for (int tick = 0; tick < 12; tick++) source.tick();
    helper.assertTrue(
        input.inventory.getStackInSlot(0).getCount() == 16, "Unpowered railgun consumed input");
    source.energy.receiveEnergy(100000, false);
    for (int tick = 0; tick < 12; tick++) source.tick();
    helper.assertTrue(
        input.inventory.getStackInSlot(0).isEmpty()
            && output.inventory.getStackInSlot(0).getCount() == 16
            && source.energy.getEnergyStored() < 100000,
        "Linked railguns did not transfer one stack and charge FE");
    for (int slot = 0; slot < output.inventory.getSlots(); slot++)
      output.inventory.setStackInSlot(slot, new ItemStack(Items.DIAMOND, 64));
    input.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 16));
    source.energy.receiveEnergy(100000, false);
    for (int tick = 0; tick < 12; tick++) source.tick();
    helper.assertTrue(
        input.inventory.getStackInSlot(0).getCount() == 16, "Full receiver consumed railgun cargo");
    helper.runAfterDelay(
        2,
        () -> {
          helper.assertTrue(
              level
                  .getEntitiesOfClass(RailgunCargoEntity.class, new AABB(first).inflate(16, 64, 16))
                  .stream()
                  .anyMatch(cargo -> cargo.display().is(Items.IRON_INGOT) && !cargo.isPickable()),
              "Transferred railgun cargo had no non-collectible launch visual");
          helper.succeed();
        });
  }

  private static OrbitalBlockEntity railgunFrame(ServerLevel level, BlockPos pos) {
    level.setBlock(pos, OrbitalRegistry.RAILGUN.get().defaultBlockState(), 2);
    OrbitalBlockEntity gun = (OrbitalBlockEntity) level.getBlockEntity(pos);
    TestMultiblocks.build(level, "railgun", pos, Direction.NORTH);
    return gun;
  }

  @GameTest(template = "empty")
  public static void activeBeaconRegistersAndFinderResolvesNearest(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    level.setBlock(pos, OrbitalRegistry.BEACON.get().defaultBlockState(), 2);
    BlockPos column = pos.south();
    for (int y = 0; y <= 3; y++)
      level.setBlock(column.above(y), MachinePorts.BLOCK_STRUCTURE.get().defaultBlockState(), 2);
    level.setBlock(column.above(4), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    for (BlockPos arm : new BlockPos[] {column.east(), column.west(), column.south()})
      level.setBlock(arm, MachinePorts.BLOCK_STRUCTURE.get().defaultBlockState(), 2);
    OrbitalBlockEntity beacon = (OrbitalBlockEntity) level.getBlockEntity(pos);
    helper.assertTrue(BeaconLogic.complete(beacon), "Original beacon column was not formed");
    beacon.tick();
    helper.assertTrue(
        BeaconLogic.active(level).contains(pos), "Helmet beacon list omitted active beacon");
    helper.assertTrue(
        Math.abs(BeaconFinderItem.angle(pos.getX() + 20, pos.getZ(), 90, pos)) < .001,
        "Forward beacon was not centered for westward view");
    helper.assertTrue(
        Math.abs(Math.abs(BeaconFinderItem.angle(pos.getX() + 20, pos.getZ(), -90, pos)) - 180)
            < .001,
        "Rear beacon was not outside the forward HUD");
    helper.assertTrue(
        pos.equals(BeaconLogic.nearest(level, pos.east(20))),
        "Active beacon was absent from planet navigation marks");
    beacon.toggleBeacon();
    helper.assertTrue(
        !BeaconLogic.active(level).contains(pos), "Disabled beacon remained in helmet markers");
    helper.assertTrue(
        BeaconLogic.nearest(level, pos.east(20)) == null, "Disabled beacon remained navigable");
    beacon.toggleBeacon();
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    helper.assertTrue(
        BeaconLogic.nearest(level, pos.east(20)) == null,
        "Broken beacon left a stale navigation mark");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void biomeScannerReadsStationOrbitThroughOriginalDishFrame(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(180);
    space.setBlock(pos, OrbitalRegistry.BIOME_SCANNER.get().defaultBlockState(), 2);
    space.setBlock(pos.below(), MachinePorts.BLOCK_MOTOR.get().defaultBlockState(), 2);
    space.setBlock(pos.below(3), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++) {
        if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
        var block =
            x == 0 && z == 0
                ? MachinePorts.BLOCK_STRUCTURE.get()
                : Math.abs(x) + Math.abs(z) == 1
                    ? MachinePorts.STRUCTURE_TOWER.get()
                    : ProcessingRegistry.part("aluminum_block");
        space.setBlock(pos.offset(x, -2, z), block.defaultBlockState(), 2);
      }
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = 1;
    station.x = pos.getX();
    station.z = pos.getZ();
    galaxy.newStation(station);
    OrbitalBlockEntity scanner = (OrbitalBlockEntity) space.getBlockEntity(pos);
    helper.assertTrue(
        BiomeScannerLogic.structure(scanner) && BiomeScannerLogic.scannedPlanet(scanner) == 1,
        "Original biome scanner dish did not resolve the planet below");
    space.setBlock(pos.below(4), Blocks.STONE.defaultBlockState(), 2);
    helper.assertTrue(
        BiomeScannerLogic.scannedPlanet(scanner) == -3,
        "Blocked scanner shaft still reported planetary biomes");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void warpStationCompletesTransitAfterOriginalTravelTime(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(180);
    space.setBlock(pos, OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
    BlockPos corePos = pos.east(3);
    buildWarpCore(space, corePos);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = pos.getX();
    station.z = pos.getZ();
    long id = galaxy.newStation(station);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
    OrbitalBlockEntity core = (OrbitalBlockEntity) space.getBlockEntity(corePos);
    PortBlockEntity input = WarpCoreLogic.input(core);
    input.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("dilithium_crystal").get()));
    core.tick();
    controller.energy.receiveEnergy(1000, false);
    controller.warpDestination = 1;
    helper.assertTrue(WarpLogic.warp(controller), "Powered station could not start warp");
    Station transit = galaxy.stations.get(id);
    helper.assertTrue(
        transit.orbitPlanet == -1 && transit.warpDestination == 1,
        "Station did not enter persistent warp transit");
    helper.assertTrue(!WarpLogic.warp(controller), "Station started a second warp in transit");
    WarpLogic.complete(galaxy, transit.warpEta - 1);
    helper.assertTrue(
        galaxy.stations.get(id).orbitPlanet == -1, "Station arrived before its travel time");
    WarpLogic.complete(galaxy, transit.warpEta);
    helper.assertTrue(
        galaxy.stations.get(id).orbitPlanet == 1 && galaxy.stations.get(id).warpEta == null,
        "Station never arrived at Luna");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void orbitalLaserProducesOriginalVoidDrillOutput(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    Planet palettePlanet = new Planet(2600, "Laser Palette Test");
    helper.assertTrue(
        OrbitalLaserLogic.orePalette(palettePlanet).size() == 6,
        "The original six void-drill ores were not available");
    palettePlanet.laserOres.add(new IdOrTag("adv_rocketry:dilithium_ore", 3));
    helper.assertTrue(
        OrbitalLaserLogic.orePalette(palettePlanet).stream()
            .anyMatch(
                ore ->
                    ore.is(ProcessingRegistry.part("dilithium_ore").asItem())
                        && ore.getCount() == 3),
        "Planet-specific laser ore and stack size were ignored");
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(180);
    space.setBlock(pos, OrbitalRegistry.ORBITAL_LASER.get().defaultBlockState(), 2);
    OrbitalBlockEntity laser = (OrbitalBlockEntity) space.getBlockEntity(pos);
    TestMultiblocks.build(space, "orbital_laser", pos, Direction.NORTH);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = pos.getX();
    station.y = pos.getY();
    station.z = pos.getZ();
    galaxy.newStation(station);
    helper.assertTrue(
        OrbitalLaserLogic.complete(laser), "Original orbital laser frame was not valid");
    helper.assertTrue(
        TestMultiblocks.at(laser, "orbital_laser").position(0, 4, 1).getY() == pos.getY() + 2,
        "Orbital-laser upper layer was mirrored below its controller");
    helper.assertTrue(
        TestMultiblocks.at(laser, "orbital_laser").position(2, 2, 0).equals(pos.east()),
        "North-facing orbital-laser output hatch was mirrored");
    PortBlockEntity input =
        (PortBlockEntity)
            space.getBlockEntity(TestMultiblocks.at(laser, "orbital_laser").position(1, 1, 10));
    input.energyStorage.receiveEnergy(100000, false);
    for (int tick = 0; tick < 20; tick++) laser.tick();
    OrbitalLaserLogic.control(laser, 13);
    helper.assertTrue(laser.laser.running, "Powered orbital laser could not start over Earth");
    for (int tick = 0; tick < 20; tick++) laser.tick();
    PortBlockEntity output =
        (PortBlockEntity)
            space.getBlockEntity(TestMultiblocks.at(laser, "orbital_laser").position(2, 2, 0));
    helper.assertTrue(
        !output.inventory.getStackInSlot(0).isEmpty(),
        "Original void drill did not deliver ore or cobblestone to its hatch");
    PortBlockEntity otherOutput =
        (PortBlockEntity)
            space.getBlockEntity(TestMultiblocks.at(laser, "orbital_laser").position(2, 2, 2));
    for (PortBlockEntity hatch : new PortBlockEntity[] {output, otherOutput})
      for (int slot = 0; slot < hatch.inventory.getSlots(); slot++)
        hatch.inventory.setStackInSlot(slot, new ItemStack(Items.DIAMOND, 64));
    for (int tick = 0; tick < 20; tick++) laser.tick();
    helper.assertTrue(
        laser.laser.jammed && !laser.laser.running, "Full output hatches did not stop laser");
    for (PortBlockEntity hatch : new PortBlockEntity[] {output, otherOutput})
      for (int slot = 0; slot < hatch.inventory.getSlots(); slot++)
        hatch.inventory.setStackInSlot(slot, ItemStack.EMPTY);
    ServerLevel planet =
        PlanetRuntime.create(space.getServer(), galaxy.planets.get(GalaxyData.EARTH_ID));
    laser.laser.x = 320;
    laser.laser.z = 320;
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++)
        planet.setBlock(new BlockPos(320 + x, 200, 320 + z), Blocks.STONE.defaultBlockState(), 2);
    OrbitalLaserLogic.control(laser, 19);
    OrbitalLaserLogic.control(laser, 18);
    OrbitalLaserLogic.control(laser, 13);
    helper.assertTrue(laser.laser.terrain && laser.laser.running, "Terrain drill could not start");
    for (int tick = 0; tick < 20; tick++) laser.tick();
    helper.assertTrue(
        planet.getBlockState(new BlockPos(320, 200, 320)).is(OrbitalRegistry.LASER_LIGHT.get())
            && planet.getBlockState(new BlockPos(320, 200, 320)).getLightEmission() == 15
            && output.inventory.getStackInSlot(0).is(Items.COBBLESTONE)
            && output.inventory.getStackInSlot(0).getCount() >= 9,
        "Terrain drill did not mine the original 3x3 shaft into output hatches");
    helper.assertTrue(
        laser.laser.nodeId != null && laser.laser.node != null && !laser.laser.node.isRemoved(),
        "Terrain drill did not keep its target node: " + laser.laser.nodeId);
    BlockPos untouched = new BlockPos(322, 200, 322);
    planet.setBlock(untouched, Blocks.STONE.defaultBlockState(), 2);
    OrbitalLaserLogic.control(laser, 13);
    helper.assertTrue(
        planet.getBlockState(new BlockPos(320, 200, 320)).isAir()
            && planet.getBlockState(untouched).is(Blocks.STONE)
            && laser.laser.nodeId == null
            && laser.laser.node == null,
        "Stopping the terrain drill left light markers or removed other blocks");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void biomeChangerConsumesOrbitalEnergyAndChangesRealBiome(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Satellite satellite = new Satellite();
    satellite.type = SatelliteType.byName("biome_changer");
    satellite.deployed = true;
    satellite.orbitPlanet = GalaxyData.EARTH_ID;
    satellite.powerStorage = 2500;
    satellite.energy = 2500;
    satellite.lastTick = level.getGameTime();
    long id = galaxy.newSatellite(satellite);
    BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    helper.assertTrue(
        BiomeChangerLogic.start(level, id, center, "minecraft:desert").getContents()
                instanceof TranslatableContents contents
            && contents.getKey().endsWith(".queued"),
        "Linked biome changer would not queue a valid job");
    BiomeChangerLogic.run(level.getServer());
    Satellite record = galaxy.satellites.get(id);
    int x = record.biomeJob.x, z = record.biomeJob.z;
    helper.assertTrue(
        record.biomeJob.index == 1 && record.energy == 580,
        "Biome changer did not charge 120 FE per original block");
    helper.assertTrue(
        level
            .getChunk(x >> 4, z >> 4)
            .getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(center.getY()), QuartPos.fromBlock(z))
            .unwrapKey()
            .orElseThrow()
            .location()
            .toString()
            .equals("minecraft:desert"),
        "Biome changer did not alter the world biome");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void areaGravityControllerAcceleratesEntitiesInSelectedDirection(
      GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    level.setBlock(pos, OrbitalRegistry.AREA_GRAVITY_CONTROLLER.get().defaultBlockState(), 2);
    level.setBlock(pos.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    for (Direction direction : Direction.Plane.HORIZONTAL)
      level.setBlock(
          pos.below().relative(direction),
          MachinePorts.BLOCK_ADVANCED_STRUCTURE.get().defaultBlockState(),
          2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) level.getBlockEntity(pos);
    helper.assertTrue(AreaGravityLogic.complete(controller), "Original cross frame was not valid");
    controller.energy.receiveEnergy(10000, false);
    ItemEntity item =
        new ItemEntity(
            level, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, new ItemStack(Items.DIRT));
    level.addFreshEntity(item);
    for (int tick = 0; tick < 160; tick++) controller.tick();
    helper.assertTrue(
        item.getDeltaMovement().y < 0 && controller.energy.getEnergyStored() < 10000,
        "Area gravity did not pull an entity downward or consume FE");
    AreaGravityLogic.adjust(controller, 12);
    item.setDeltaMovement(Vec3.ZERO);
    controller.tick();
    helper.assertTrue(item.getDeltaMovement().y > 0, "Direction selector did not reverse gravity");
    item.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void spaceElevatorLinksOnlyCompleteGeostationaryFrames(GameTestHelper helper) {
    var ground = helper.getLevel();
    var galaxy = GalaxyData.get(ground.getServer());
    var space = PlanetRuntime.create(ground.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long stationId = galaxy.newStation(station);
    BlockPos pos = new BlockPos(1000000 + (int) stationId * 256, 180, 1000000);
    station.x = pos.getX();
    station.y = pos.getY();
    station.z = pos.getZ();
    OrbitalBlockEntity groundAnchor = elevatorFrame(ground, pos);
    OrbitalBlockEntity stationAnchor = elevatorFrame(space, pos);
    helper.assertTrue(SpaceElevatorLogic.complete(groundAnchor), "Ground frame is incomplete");
    helper.assertTrue(SpaceElevatorLogic.complete(stationAnchor), "Station frame is incomplete");
    helper.assertTrue(
        !SpaceElevatorLogic.link(groundAnchor, stationAnchor),
        "Low-orbit station accepted an elevator tether");
    station.altitude = 179;
    station.rotationVelocity[1] = .1;
    helper.assertTrue(
        !SpaceElevatorLogic.link(groundAnchor, stationAnchor),
        "Rotating station accepted an elevator tether");
    station.rotationVelocity[1] = 0;
    station.rotationAngle[0] = 11;
    helper.assertTrue(
        !SpaceElevatorLogic.link(groundAnchor, stationAnchor),
        "Tilted station accepted an elevator tether");
    station.rotationAngle[0] = 0;
    helper.assertTrue(
        SpaceElevatorLogic.link(groundAnchor, stationAnchor),
        "Complete geostationary anchors did not link");
    helper.assertTrue(galaxy.stations.get(stationId).anchored, "Tether did not anchor station");
    var port =
        (PortBlockEntity)
            ground.getBlockEntity(
                TestMultiblocks.at(groundAnchor, "space_elevator").position(0, 0, 3));
    port.energyStorage.receiveEnergy(60000, false);
    for (int tick = 0; tick < 60; tick++) SpaceElevatorLogic.collectEnergy(groundAnchor);
    helper.assertTrue(
        groundAnchor.energy.getEnergyStored() >= 50000, "Tether did not draw travel FE");
    ElevatorCapsule capsule = OrbitalRegistry.ELEVATOR_CAPSULE.get().create(ground);
    helper.assertTrue(capsule != null, "Capsule entity was not registered");
    capsule.setPos(pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
    capsule.depart(stationAnchor);
    ground.addFreshEntity(capsule);
    for (int tick = 0; tick < 400 && !capsule.isRemoved(); tick++) capsule.tick();
    helper.assertTrue(capsule.isRemoved(), "Capsule did not reach orbital transfer height");
    AABB arrivalColumn =
        new AABB(pos.getX() - 3, 0, pos.getZ() - 3, pos.getX() + 3, 1000, pos.getZ() + 3);
    helper.assertTrue(
        !space.getEntitiesOfClass(ElevatorCapsule.class, arrivalColumn).isEmpty(),
        "Capsule did not spawn in station dimension");
    SpaceElevatorLogic.unlink(groundAnchor);
    helper.assertTrue(
        !galaxy.stations.get(stationId).anchored && stationAnchor.elevatorDimension.isEmpty(),
        "Unlink left a stale station anchor");
    helper.assertTrue(
        SpaceElevatorLogic.link(groundAnchor, stationAnchor), "Tether could not be relinked");
    ground.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    helper.assertTrue(
        !galaxy.stations.get(stationId).anchored && stationAnchor.elevatorDimension.isEmpty(),
        "Breaking an elevator left the station anchored");
    helper.succeed();
  }

  private static OrbitalBlockEntity elevatorFrame(ServerLevel level, BlockPos pos) {
    level.setBlock(pos, OrbitalRegistry.SPACE_ELEVATOR.get().defaultBlockState(), 2);
    OrbitalBlockEntity elevator = (OrbitalBlockEntity) level.getBlockEntity(pos);
    TestMultiblocks.build(level, "space_elevator", pos, Direction.NORTH);
    return elevator;
  }

  @GameTest(template = "empty")
  public static void oreMapperBuildsScannerAndReadsActualOre(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos builderPos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(builderPos, OrbitalRegistry.BUILDER.get().defaultBlockState(), 2);
    OrbitalBlockEntity builder = (OrbitalBlockEntity) level.getBlockEntity(builderPos);
    builder.inventory.setStackInSlot(7, new ItemStack(OrbitalRegistry.SATELLITE.get()));
    builder.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.ORE_MAPPER.get()));
    builder.inventory.setStackInSlot(1, new ItemStack(OrbitalRegistry.LARGE_SOLAR_PANEL.get()));
    builder.inventory.setStackInSlot(8, new ItemStack(OrbitalRegistry.ORE_SCANNER.get()));
    helper.assertTrue(
        !SatelliteBuilderLogic.build(builder),
        "Standalone satellite builder ignored its missing power hatch");
    level.setBlock(
        builderPos.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    PortBlockEntity mapperPower = (PortBlockEntity) level.getBlockEntity(builderPos.below());
    mapperPower.energyStorage.receiveEnergy(1000, false);
    helper.assertTrue(
        SatelliteBuilderLogic.build(builder), "Ore mapper satellite did not assemble");
    helper.assertTrue(
        builder.inventory.getStackInSlot(9).isEmpty(),
        "Satellite appeared before assembly finished");
    for (int tick = 0; tick < 100; tick++) builder.tick();
    helper.assertTrue(
        SatelliteItem.assembled(builder.inventory.getStackInSlot(9)),
        "Ore mapper satellite did not finish assembly");
    helper.assertTrue(
        SatelliteLink.read(builder.inventory.getStackInSlot(8)) > 0,
        "Handheld ore scanner was not linked to its satellite");
    BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    BlockPos ore = center.offset(-16, 0, -16);
    level.setBlock(ore, ProcessingRegistry.part("tin_ore").defaultBlockState(), 2);
    helper.assertTrue(
        OreScannerItem.scan(level, center)[0] > 0,
        "Ore map did not detect a real ore block in its column");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void builderCopiesStationPlanetAndScannerIds(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, OrbitalRegistry.BUILDER.get().defaultBlockState(), 2);
    level.setBlock(pos.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    OrbitalBlockEntity builder = (OrbitalBlockEntity) level.getBlockEntity(pos);
    PortBlockEntity power = (PortBlockEntity) level.getBlockEntity(pos.below());
    ItemStack[] source = {
      new ItemStack(OrbitalRegistry.STATION_CHIP.get()),
      new ItemStack(OrbitalRegistry.PLANET_CHIP.get()),
      new ItemStack(OrbitalRegistry.ORE_SCANNER.get())
    };
    for (int index = 0; index < source.length; index++) {
      if (index == 0) source[index].set(ModComponents.STATION_LINK, new StationLink(index + 19));
      else if (index == 1)
        source[index].set(ModComponents.PLANET, new ProgrammedPlanet(index + 19, "Copied"));
      else source[index].set(ModComponents.SATELLITE_LINK, new SatelliteLink(index + 19));
      builder.inventory.setStackInSlot(8, source[index]);
      builder.inventory.setStackInSlot(11, new ItemStack(source[index].getItem()));
      power.energyStorage.receiveEnergy(1000, false);
      helper.assertTrue(
          SatelliteBuilderLogic.copyChip(builder), "Builder rejected a valid legacy ID type");
      for (int tick = 0; tick < 100; tick++) builder.tick();
      ItemStack result = builder.inventory.getStackInSlot(9);
      long copied =
          index == 0
              ? StationLink.read(result)
              : index == 1 ? PlanetIdChipItem.planet(result) : SatelliteLink.read(result);
      helper.assertTrue(copied == index + 19, "Copied legacy identifier changed its ID");
      builder.inventory.setStackInSlot(9, ItemStack.EMPTY);
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void satelliteChassisCarriesModulesWithoutDuplicatingDrops(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, OrbitalRegistry.BUILDER.get().defaultBlockState(), 2);
    OrbitalBlockEntity builder = (OrbitalBlockEntity) level.getBlockEntity(pos);
    ItemStack sensor = new ItemStack(OrbitalRegistry.ORE_MAPPER.get());
    helper.assertTrue(
        !builder.inventory.insertItem(0, sensor, false).isEmpty(),
        "Builder accepted a component without a chassis");
    builder.inventory.setStackInSlot(7, new ItemStack(OrbitalRegistry.SATELLITE.get()));
    helper.assertTrue(
        builder.inventory.insertItem(0, sensor, false).isEmpty(), "Controller did not fit chassis");
    ItemStack panels = new ItemStack(OrbitalRegistry.LARGE_SOLAR_PANEL.get(), 2);
    helper.assertTrue(
        builder.inventory.insertItem(1, panels, false).getCount() == 1,
        "Chassis module slot accepted multiple components");
    ItemStack removed = builder.inventory.extractItem(7, 1, false);
    helper.assertTrue(
        builder.inventory.getStackInSlot(0).isEmpty()
            && SatelliteItem.modules(removed).getStackInSlot(0).is(sensor.getItem()),
        "Removing the chassis left its components in the machine");
    ItemStack restored =
        ItemStack.parse(level.registryAccess(), removed.save(level.registryAccess())).orElseThrow();
    builder.inventory.setStackInSlot(7, restored);
    helper.assertTrue(
        builder.inventory.getStackInSlot(1).is(OrbitalRegistry.LARGE_SOLAR_PANEL.get()),
        "Chassis components did not survive item serialization");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    OrbitalMenu menu = new OrbitalMenu(0, player.getInventory(), builder);
    menu.quickMoveStack(player, 1);
    helper.assertTrue(
        SatelliteItem.modules(restored).getStackInSlot(1).isEmpty(),
        "Shift-click duplicated an embedded module");
    builder.inventory.insertItem(1, new ItemStack(OrbitalRegistry.LARGE_SOLAR_PANEL.get()), false);
    builder.inventory.setStackInSlot(8, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    player.getInventory().setItem(0, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    menu.quickMoveStack(player, menu.slots.size() - 9);
    helper.assertTrue(
        builder.inventory.getStackInSlot(11).is(OrbitalRegistry.SATELLITE_CHIP.get()),
        "Shift-click did not reach the dedicated blank-chip slot");
    level.destroyBlock(pos, false);
    var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
    helper.assertTrue(
        drops.stream().filter(item -> item.getItem().is(OrbitalRegistry.SATELLITE.get())).count()
                == 1
            && drops.stream()
                .noneMatch(
                    item ->
                        item.getItem().is(sensor.getItem())
                            || item.getItem().is(OrbitalRegistry.LARGE_SOLAR_PANEL.get())),
        "Breaking the builder duplicated or lost its embedded modules");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void observatoryProgramsAnAsteroidChipFromResearch(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8)).atY(180);
    space.setBlock(pos, OrbitalRegistry.OBSERVATORY.get().defaultBlockState(), 2);
    buildObservatory(space, pos);
    OrbitalBlockEntity observatory = (OrbitalBlockEntity) space.getBlockEntity(pos);
    helper.assertTrue(
        observatory.observatoryRange() == 50,
        "Original three-lens and motor observatory range was not restored");
    ItemStack dataUnit = new ItemStack(OrbitalRegistry.DATA_UNIT.get());
    DataUnitItem.store(dataUnit, ResearchType.OPTICAL, 100);
    observatory.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get()));
    BlockPos powerPos =
        TestMultiblocks.at(space, "observatory", pos, Direction.NORTH).position(3, 0, 1);
    BlockPos dataPos =
        TestMultiblocks.at(space, "observatory", pos, Direction.NORTH).position(3, 0, 3);
    space.setBlock(powerPos, MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    space.setBlock(dataPos, MachinePorts.DATA_BUS.get().defaultBlockState(), 2);
    PortBlockEntity powerPort = (PortBlockEntity) space.getBlockEntity(powerPos);
    PortBlockEntity dataPort = (PortBlockEntity) space.getBlockEntity(dataPos);
    dataPort.inventory.setStackInSlot(0, dataUnit);
    powerPort.energyStorage.receiveEnergy(500, false);
    helper.assertTrue(
        observatory.startObservation(), "Observatory did not accept distance data from its bus");
    helper.assertTrue(
        observatory.observation.candidate == -1
            && observatory.observation.options.length == 10
            && powerPort.energyStorage.getEnergyStored() == 500,
        "Scan did not produce selectable targets at the original data-only cost");
    helper.assertTrue(
        !observatory.programAsteroidChip(), "Observatory programmed an unselected target");
    ObservatoryLogic.selectNext(observatory);
    CompoundTag savedScan = observatory.saveWithFullMetadata(space.registryAccess());
    observatory.observation.options = new int[0];
    observatory.loadWithComponents(savedScan, space.registryAccess());
    helper.assertTrue(observatory.observation.options.length == 10, "Scan results were lost on reload");
    for (int tick = 0; tick < 100; tick++) observatory.tick();
    helper.assertTrue(
        observatory.observation.candidate >= 0
            && AsteroidCatalog.types(space.registryAccess())
                .get(observatory.observation.candidate)
                .id()
                .equals("adv_rocketry:small"),
        "Observatory did not discover the visible original asteroid type");
    helper.assertTrue(observatory.programAsteroidChip(), "Observatory did not program chip");
    ItemStack chip = observatory.inventory.getStackInSlot(4);
    helper.assertTrue(
        AsteroidChipItem.programmed(chip)
            && AsteroidChipItem.target(chip).type().equals("adv_rocketry:small")
            && AsteroidChipItem.data(chip, ResearchType.OPTICAL) == 0
            && powerPort.energyStorage.getEnergyStored() == 0
            && DataUnitItem.research(dataPort.inventory.getStackInSlot(0)).amount() == 0,
        "Observatory did not preserve the original scan, research and programming costs");
    BlockPos motor =
        TestMultiblocks.at(space, "observatory", pos, Direction.NORTH).position(4, 2, 2);
    space.setBlock(motor, MachinePorts.ADVANCED_MOTOR.get().defaultBlockState(), 2);
    helper.assertTrue(
        observatory.observatoryRange() == 75,
        "Advanced observatory motor did not add its original range bonus");
    BlockPos lens =
        TestMultiblocks.at(space, "observatory", pos, Direction.NORTH).position(0, 1, 2);
    space.setBlock(lens, Blocks.GLASS.defaultBlockState(), 2);
    helper.assertTrue(
        observatory.observatoryRange() == 70,
        "Glass lens did not preserve the frame without the optical bonus");
    space.removeBlock(motor, false);
    helper.assertTrue(
        !observatory.observatoryStructure(),
        "Observatory continued operating after its motor was removed");
    observatory.inventory.setStackInSlot(4, ItemStack.EMPTY);
    observatory.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get()));
    powerPort.energyStorage.receiveEnergy(500, false);
    helper.assertTrue(
        !observatory.programAsteroidChip(), "Incomplete observatory programmed a chip");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void asteroidMissionReturnsRocketWithPersistentCargo(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    RocketStructure ship = new RocketStructure();
    ship.drillingPower = .02f;
    CompoundTag chipData = new CompoundTag();
    ItemStack chip = new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get());
    AsteroidChipItem.program(chip, "adv_rocketry:small", 12345);
    AsteroidChipItem.addData(chip, ResearchType.OPTICAL, 1000);
    AsteroidChipItem.addData(chip, ResearchType.COMPOSITION, 1000);
    var contents = new ItemStackHandler(1);
    contents.setStackInSlot(0, chip);
    chipData.put("guidance_inventory", contents.serializeNBT(level.registryAccess()));
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            chipData));
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    helper.assertTrue(rocket != null, "Rocket entity was not registered");
    BlockPos origin = helper.absolutePos(new BlockPos(5, 3, 5));
    rocket.initialize(ship, origin);
    helper.assertTrue(MiningMission.begin(level, rocket, chip), "Mining mission did not start");
    Mission mission =
        galaxy.miningMissions.values().stream().reduce((first, second) -> second).orElseThrow();
    helper.assertTrue(
        mission.duration == 18000, "Original drill power did not determine mission duration");
    Mission reloaded = Mission.load(mission.save());
    helper.assertTrue(
        reloaded.asteroid != null && reloaded.asteroid.id().equals(mission.asteroidId),
        "Mission did not snapshot its asteroid definition");
    mission = reloaded;
    mission.asteroidId = "removed_after_launch";
    helper.assertTrue(
        MiningMission.finish(level, mission), "Mining mission did not return the rocket");
    helper.assertTrue(
        !AsteroidCatalog.harvest(
                AsteroidCatalog.get(helper.getLevel().registryAccess(), "adv_rocketry:small"),
                12345,
                1000,
                1000)
            .isEmpty(),
        "Original asteroid harvest was empty");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void asteroidDefinitionsComeFromTheDatapackRegistry(GameTestHelper helper) {
    var types = AsteroidCatalog.types(helper.getLevel().registryAccess());
    helper.assertTrue(
        types.stream()
            .map(AsteroidCatalog.Asteroid::id)
            .toList()
            .equals(
                List.of(
                    "adv_rocketry:iridium",
                    "adv_rocketry:light",
                    "adv_rocketry:small",
                    "adv_rocketry:strange")),
        "Bundled asteroid definitions are missing or unsorted: " + types);
    helper.assertTrue(
        types.get(2).displayName().getString().equals("Small Asteroid"),
        "Asteroid name is not translated");
    var json =
        JsonParser.parseString(
            """
            {"distance": 25, "mass": 80, "mass_variability": 0.2, "richness": 0.5,
             "richness_variability": 0.1, "probability": 2.5, "time_multiplier": 1.75,
             "base": "minecraft:red_wool",
             "ores": [{"item": "adv_rocketry:aluminum_ore", "weight": 1.25},
                      {"item": "adv_rocketry:iridium_ore", "weight": 0.5}]}
            """);
    var asteroid =
        AsteroidCatalog.Definition.CODEC
            .parse(JsonOps.INSTANCE, json)
            .getOrThrow()
            .named(ResourceLocation.parse("test:custom"));
    helper.assertTrue(
        asteroid.probability() == 2.5f
            && asteroid.timeMultiplier() == 1.75f
            && asteroid.base().toString().equals("minecraft:red_wool")
            && asteroid.ores().getFirst().weight() == 1.25f
            && asteroid.ores().get(1).item().toString().equals("adv_rocketry:iridium_ore"),
        "Asteroid JSON lost its values or fractional weights");
    helper.assertTrue(
        AsteroidCatalog.load(AsteroidCatalog.save(asteroid)).equals(asteroid),
        "Asteroid definition changed during mission serialization");
    json.getAsJsonObject().getAsJsonArray("ores").get(0).getAsJsonObject().addProperty("weight", 0);
    helper.assertTrue(
        AsteroidCatalog.Definition.CODEC.parse(JsonOps.INSTANCE, json).isError(),
        "A zero ore weight was accepted");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void asteroidSurveyResearchChangesCertaintyAndAllCargoBonuses(
      GameTestHelper helper) {
    AsteroidCatalog.Asteroid asteroid =
        AsteroidCatalog.get(helper.getLevel().registryAccess(), "adv_rocketry:small");
    var uncertain = AsteroidCatalog.survey(asteroid, 12345, 0, 0);
    var certain = AsteroidCatalog.survey(asteroid, 12345, 2000, 2000);
    helper.assertTrue(
        uncertain.size() == certain.size() && uncertain.getFirst().variability() > 0,
        "Unresearched asteroid survey did not expose uncertainty");
    for (int index = 0; index < certain.size(); index++)
      helper.assertTrue(
          certain.get(index).variability() == 0
              && certain.get(index).midpoint() == certain.get(index).count()
              && certain.get(index).count() == uncertain.get(index).count(),
          "Survey research changed the asteroid instead of refining its estimate");
    int unresearchedBase =
        AsteroidCatalog.harvest(asteroid, 12345, 0, 0).stream()
            .filter(stack -> stack.is(Items.COBBLESTONE))
            .mapToInt(ItemStack::getCount)
            .sum();
    int researchedBase =
        AsteroidCatalog.harvest(asteroid, 12345, 1000, 1000).stream()
            .filter(stack -> stack.is(Items.COBBLESTONE))
            .mapToInt(ItemStack::getCount)
            .sum();
    helper.assertTrue(
        researchedBase == (int) ((int) (unresearchedBase * 1.25f) * 1.25f),
        "Research bonuses skipped the asteroid's base material");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void solarArrayRotatesItsStructureWithController(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    for (int x = 0; x <= 21; x++) level.getChunkAt(center.offset(-x, 0, 0));
    level.setBlock(
        center,
        OrbitalRegistry.SOLAR_ARRAY
            .get()
            .defaultBlockState()
            .setValue(HorizontalOrbitalBlock.FACING, Direction.EAST),
        2);
    level.setBlock(
        center.north(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(
        center.south(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(center.west(), OrbitalRegistry.SOLAR_ARRAY_PANEL.get().defaultBlockState(), 2);
    OrbitalBlockEntity array = (OrbitalBlockEntity) level.getBlockEntity(center);
    array.tick();
    helper.assertTrue(
        SolarArrayLogic.panels(array) == 1 && array.powerMadeLastTick > 0,
        "Rotated solar array did not recognize its panel lane");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void stationGravityControllerAdjustsPersistentStation(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    long id = galaxy.newStation(station);
    BlockPos pos = new BlockPos(6144 + (int) id * 256, 120, 6144);
    station.x = pos.getX();
    station.z = pos.getZ();
    galaxy.setDirty();
    space.setBlock(pos, OrbitalRegistry.STATION_GRAVITY_CONTROLLER.get().defaultBlockState(), 2);
    OrbitalBlockEntity controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
    StationMotionLogic.adjustGravity(controller, -10);
    helper.assertTrue(
        galaxy.stations.get(id).targetGravity == 90, "Station gravity target was not saved");
    controller.tick();
    helper.assertTrue(
        galaxy.stations.get(id).gravity != null && galaxy.stations.get(id).gravity < 1,
        "Station gravity did not begin approaching its target");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void solarArrayProducesPowerFromOriginalPanelLayout(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8)).atY(200);
    for (int z = 0; z <= 21; z++) level.getChunkAt(center.offset(0, 0, z));
    level.setBlock(center, OrbitalRegistry.SOLAR_ARRAY.get().defaultBlockState(), 2);
    level.setBlock(
        center.east(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(
        center.west(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(center.south(), OrbitalRegistry.SOLAR_ARRAY_PANEL.get().defaultBlockState(), 2);
    OrbitalBlockEntity array = (OrbitalBlockEntity) level.getBlockEntity(center);
    array.tick();
    PortBlockEntity output = (PortBlockEntity) level.getBlockEntity(center.west());
    helper.assertTrue(
        array.powerMadeLastTick > 0 && output.energyStorage.getEnergyStored() > 0,
        "Original solar array panel layout did not export power: generated="
            + array.powerMadeLastTick
            + ", output="
            + output.energyStorage.getEnergyStored()
            + ", day="
            + level.isDay()
            + ", sky="
            + level.canSeeSky(center.above())
            + ", panels="
            + SolarArrayLogic.panels(array)
            + ", status="
            + array.status);
    level.removeBlock(center.south(), false);
    array.tick();
    helper.assertTrue(array.powerMadeLastTick == 0, "Incomplete solar array continued generating");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void microwaveReceiverDrawsLinkedSatellitePower(GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    Satellite satellite = new Satellite();
    satellite.deployed = true;
    satellite.type = SatelliteType.byName("solar_energy");
    satellite.orbitPlanet = GalaxyData.EARTH_ID;
    satellite.powerGeneration = 40;
    satellite.powerStorage = 10000;
    satellite.energy = 400;
    satellite.lastTick = level.getGameTime();
    long id = galaxy.newSatellite(satellite);
    BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
    level.setBlock(center, OrbitalRegistry.MICROWAVE_RECEIVER.get().defaultBlockState(), 2);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        if (Math.abs(x) == 2 && Math.abs(z) == 2
            || Math.abs(x) <= 1 && Math.abs(z) <= 1 && (x != 0 || z != 0))
          level.setBlock(
              center.offset(x, 0, z), MachinePorts.SOLAR_PANEL.get().defaultBlockState(), 2);
    level.setBlock(
        center.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    OrbitalBlockEntity receiver = (OrbitalBlockEntity) level.getBlockEntity(center);
    ItemStack chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    receiver.inventory.setStackInSlot(0, chip);
    receiver.tick();
    PortBlockEntity output = (PortBlockEntity) level.getBlockEntity(center.below());
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 800,
        "Receiver did not export doubled microwave FE from the linked satellite");
    helper.assertTrue(
        galaxy.satellites.get(id).energy == 0,
        "Microwave transmission did not consume satellite battery energy");
    receiver.tick();
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 800,
        "Receiver duplicated satellite energy in the same tick");
    galaxy.satellites.get(id).energy = 400;
    level.removeBlock(center.offset(2, 0, 2), false);
    receiver.tick();
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 800 && galaxy.satellites.get(id).energy == 400,
        "Incomplete microwave array drew satellite power");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void microwaveStationTracksOrbitalInsolationAndWarp(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    var planet = new Planet(19301, "Microwave flux probe");
    planet.star = 19301;
    galaxy.planets.put(planet.id, planet);
    galaxy.stars.put(19301, new Star(19301, "Flux probe", 100, 1, 0, 0, false));
    BlockPos center = new BlockPos(12500000, 150, 12500000);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = planet.id;
    station.x = center.getX();
    station.z = center.getZ();
    long stationId = galaxy.newStation(station);
    space.setBlock(center, OrbitalRegistry.MICROWAVE_RECEIVER.get().defaultBlockState(), 2);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        if (Math.abs(x) == 2 && Math.abs(z) == 2
            || Math.abs(x) <= 1 && Math.abs(z) <= 1 && (x != 0 || z != 0))
          space.setBlock(
              center.offset(x, 0, z), MachinePorts.SOLAR_PANEL.get().defaultBlockState(), 2);
    space.setBlock(
        center.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    var receiver = (OrbitalBlockEntity) space.getBlockEntity(center);
    var output = (PortBlockEntity) space.getBlockEntity(center.below());
    Satellite satellite = new Satellite();
    satellite.deployed = true;
    satellite.type = SatelliteType.byName("solar_energy");
    satellite.orbitPlanet = planet.id;
    satellite.powerStorage = 10000;
    satellite.energy = 400;
    satellite.lastTick = space.getGameTime();
    long satelliteId = galaxy.newSatellite(satellite);
    ItemStack chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(satelliteId));
    receiver.inventory.setStackInSlot(0, chip);
    planet.orbitalDistance = 50;
    receiver.tick();
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 4186,
        "Close station did not apply fourfold unattenuated stellar flux");
    output.energyStorage.consume(100000, false);
    satellite.energy = 400;
    galaxy.satellites.put(satelliteId, satellite.copy());
    planet.orbitalDistance = 200;
    receiver.tick();
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 262,
        "Distant station did not apply quarter stellar flux");
    output.energyStorage.consume(100000, false);
    satellite.energy = 400;
    galaxy.satellites.put(satelliteId, satellite.copy());
    station.warpEta = (long) (space.getGameTime() + 1000);
    receiver.tick();
    helper.assertTrue(
        output.energyStorage.getEnergyStored() == 0
            && galaxy.satellites.get(satelliteId).energy == 400,
        "Warp drained or generated microwave power");
    space.removeBlock(center, false);
    galaxy.satellites.remove(satelliteId);
    galaxy.stations.remove(stationId);
    galaxy.planets.remove(planet.id);
    galaxy.stars.remove(19301);
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void stationLaunchUsesStationOrbit(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = 1;
    station.x = 4096;
    station.z = 4096;
    galaxy.newStation(station);
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    Satellite satellite = new Satellite();
    satellite.type = SatelliteType.byName("optical");
    long satelliteId = galaxy.newSatellite(satellite);
    ItemStack payload = new ItemStack(OrbitalRegistry.SATELLITE.get());
    payload.set(
        ModComponents.SATELLITE,
        new SatelliteProperties(satelliteId, SatelliteType.OPTICAL, 0, 0, 0));
    BlockPos hatchPos = new BlockPos(4096, 120, 4096);
    space.setBlock(hatchPos, OrbitalRegistry.HATCH.get().defaultBlockState(), 2);
    OrbitalBlockEntity hatch = (OrbitalBlockEntity) space.getBlockEntity(hatchPos);
    hatch.inventory.setStackInSlot(0, payload);
    RocketStructure rocket = new RocketStructure();
    rocket.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            OrbitalRegistry.HATCH.get().defaultBlockState(),
            hatch.saveWithFullMetadata(space.registryAccess())));
    helper.assertTrue(
        SatelliteLogic.deployCargo(space, rocket, hatchPos) == 1,
        "Station launch did not deploy satellite");
    helper.assertTrue(
        galaxy.satellites.get(satelliteId).orbitPlanet == 1,
        "Satellite inherited the space dimension instead of station's planet");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void satelliteBuildLaunchAndPersistence(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos builderPos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(builderPos, OrbitalRegistry.BUILDER.get().defaultBlockState(), 2);
    OrbitalBlockEntity builder = (OrbitalBlockEntity) level.getBlockEntity(builderPos);
    builder.inventory.setStackInSlot(7, new ItemStack(OrbitalRegistry.SATELLITE.get()));
    builder.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("optical_sensor").get()));
    builder.inventory.setStackInSlot(1, new ItemStack(OrbitalRegistry.LARGE_SOLAR_PANEL.get()));
    builder.inventory.setStackInSlot(
        2, new ItemStack(ProcessingRegistry.PART_ITEMS.get("advanced_battery").get()));
    builder.inventory.setStackInSlot(3, new ItemStack(OrbitalRegistry.DATA_UNIT.get()));
    builder.inventory.setStackInSlot(8, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    level.setBlock(
        builderPos.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    PortBlockEntity builderPower = (PortBlockEntity) level.getBlockEntity(builderPos.below());
    builderPower.energyStorage.receiveEnergy(1000, false);
    helper.assertTrue(
        SatelliteBuilderLogic.build(builder),
        "Original component recipe did not assemble satellite");
    helper.assertTrue(
        !SatelliteBuilderLogic.build(builder),
        "Builder accepted a second job while the first was running");
    for (int tick = 0; tick < 50; tick++) builder.tick();
    helper.assertTrue(
        builder.builderProgress == 51
            && builderPower.energyStorage.getEnergyStored() == 500
            && builder.inventory.getStackInSlot(9).isEmpty(),
        "Builder did not draw 10 FE per tick during assembly");
    helper.assertTrue(
        builder.inventory.extractItem(10, 1, false).isEmpty(),
        "Automation extracted an unfinished satellite");
    CompoundTag savedBuilder = builder.saveWithFullMetadata(level.registryAccess());
    builder.builderProgress = 0;
    builder.inventory.setStackInSlot(10, ItemStack.EMPTY);
    builder.loadWithComponents(savedBuilder, level.registryAccess());
    helper.assertTrue(
        builder.builderProgress == 51
            && SatelliteItem.assembled(builder.inventory.getStackInSlot(10)),
        "Builder lost its unfinished job on reload");
    builderPower.energyStorage.consume(500, false);
    builder.tick();
    helper.assertTrue(builder.builderProgress == 51, "Builder progressed without power");
    builderPower.energyStorage.receiveEnergy(500, false);
    for (int tick = 50; tick < 100; tick++) builder.tick();
    ItemStack payload = builder.inventory.getStackInSlot(9);
    helper.assertTrue(
        SatelliteItem.assembled(payload), "Builder did not produce an assembled chassis");
    helper.assertTrue(
        payload.get(ModComponents.SATELLITE).powerStorage() == 40720,
        "Advanced battery lost forty-thousand storage plus the original 720 base buffer");
    helper.assertTrue(
        OrbitalRegistry.storage(new ItemStack(ProcessingRegistry.PART_ITEMS.get("battery").get()))
            == 10000,
        "Basic battery capacity changed");
    long id = payload.get(ModComponents.SATELLITE).id();
    helper.assertTrue(
        id > 0 && SatelliteLink.read(builder.inventory.getStackInSlot(8)) == id,
        "Satellite ID chip was not programmed");
    helper.assertTrue(
        builderPower.energyStorage.getEnergyStored() == 0,
        "Builder did not draw assembly power from its input hatch");
    builder.inventory.setStackInSlot(9, ItemStack.EMPTY);
    builder.inventory.setStackInSlot(11, new ItemStack(OrbitalRegistry.STATION_CHIP.get()));
    helper.assertTrue(
        !SatelliteBuilderLogic.copyChip(builder), "Builder copied an ID to a mismatched chip");
    builder.inventory.setStackInSlot(11, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    builderPower.energyStorage.receiveEnergy(1000, false);
    helper.assertTrue(
        SatelliteBuilderLogic.copyChip(builder),
        "Builder could not copy a programmed satellite chip");
    helper.assertTrue(
        builder.inventory.getStackInSlot(9).isEmpty(), "Chip copy appeared before processing");
    for (int tick = 0; tick < 100; tick++) builder.tick();
    helper.assertTrue(
        SatelliteLink.read(builder.inventory.getStackInSlot(9)) == id,
        "Copied satellite chip lost its ID");
    ItemStack programmedChip = builder.inventory.getStackInSlot(8).copy();
    builder.inventory.setStackInSlot(9, ItemStack.EMPTY);
    builder.inventory.setStackInSlot(8, payload.copy());
    builder.inventory.setStackInSlot(11, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    builderPower.energyStorage.receiveEnergy(1000, false);
    helper.assertTrue(
        SatelliteBuilderLogic.copyChip(builder),
        "Builder could not copy an assembled satellite ID");
    for (int tick = 0; tick < 100; tick++) builder.tick();
    helper.assertTrue(
        SatelliteLink.read(builder.inventory.getStackInSlot(9)) == id,
        "Copied satellite ID did not match its chassis");
    builder.inventory.setStackInSlot(8, programmedChip);
    CompoundTag olderInventory = builder.saveWithFullMetadata(level.registryAccess());
    olderInventory.getCompound("inventory").putInt("Size", 10);
    builder.loadWithComponents(olderInventory, level.registryAccess());
    helper.assertTrue(builder.inventory.getSlots() == 12, "Old builder save lost new slots");
    BlockPos hatchPos = helper.absolutePos(new BlockPos(5, 3, 3));
    level.setBlock(hatchPos, OrbitalRegistry.HATCH.get().defaultBlockState(), 2);
    OrbitalBlockEntity hatch = (OrbitalBlockEntity) level.getBlockEntity(hatchPos);
    hatch.inventory.setStackInSlot(0, payload.copy());
    RocketStructure structure = new RocketStructure();
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            OrbitalRegistry.HATCH.get().defaultBlockState(),
            hatch.saveWithFullMetadata(level.registryAccess())));
    RocketStructure restored = RocketStructure.load(structure.save(true));
    helper.assertTrue(
        SatelliteLogic.deployCargo(level, restored, hatchPos) == 1,
        "Rocket did not deploy the satellite in orbit");
    helper.assertTrue(
        SatelliteLogic.deployCargo(level, restored, hatchPos) == 0,
        "Satellite payload was deployed twice");
    var record = GalaxyData.get(level.getServer()).satellites.get(id);
    helper.assertTrue(record.deployed && record.orbitPlanet == 0, "Satellite orbit was not saved");
    SatelliteLogic.advance(GalaxyData.get(level.getServer()), id, level.getGameTime() + 1000);
    helper.assertTrue(
        GalaxyData.get(level.getServer()).satellites.get(id).data > 0,
        "Powered optical satellite did not collect distance data");
    BlockPos terminalPos = helper.absolutePos(new BlockPos(8, 3, 3));
    level.setBlock(terminalPos, OrbitalRegistry.TERMINAL.get().defaultBlockState(), 2);
    OrbitalBlockEntity terminal = (OrbitalBlockEntity) level.getBlockEntity(terminalPos);
    terminal.inventory.setStackInSlot(0, builder.inventory.getStackInSlot(8).copy());
    terminal.inventory.setStackInSlot(1, new ItemStack(OrbitalRegistry.DATA_UNIT.get()));
    terminal.energy.receiveEnergy(10, false);
    helper.assertTrue(
        SatelliteTerminalLogic.transferData(terminal),
        "Satellite terminal could not download data");
    helper.assertTrue(
        DataUnitItem.research(terminal.inventory.getStackInSlot(1)).amount() > 0,
        "Data unit did not retain downloaded satellite data");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void registeredDensitySatelliteGeneratesAndDownloadsTypedData(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var galaxy = GalaxyData.get(level.getServer());
    Satellite record = new Satellite();
    record.type = SatelliteType.byName("density");
    record.deployed = true;
    record.orbitPlanet = GalaxyData.EARTH_ID;
    record.powerGeneration = 40;
    record.powerStorage = 40000;
    record.maxData = 1000;
    record.lastTick = level.getGameTime();
    long id = galaxy.newSatellite(record);
    SatelliteLogic.advance(galaxy, id, level.getGameTime() + 1000);
    helper.assertTrue(galaxy.satellites.get(id).data > 0, "Density satellite collected no data");
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, OrbitalRegistry.TERMINAL.get().defaultBlockState(), 2);
    var terminal = (OrbitalBlockEntity) level.getBlockEntity(pos);
    var chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    terminal.inventory.setStackInSlot(0, chip);
    terminal.inventory.setStackInSlot(1, new ItemStack(OrbitalRegistry.DATA_UNIT.get()));
    terminal.energy.receiveEnergy(10, false);
    helper.assertTrue(SatelliteTerminalLogic.transferData(terminal), "Density download failed");
    var unit = DataUnitItem.research(terminal.inventory.getStackInSlot(1));
    helper.assertTrue(
        unit.amount() > 0 && unit.type() == ResearchType.ATMOSPHERE_DENSITY,
        "Density data channel changed");
    galaxy.satellites.remove(id);
    helper.succeed();
  }

  private static void buildWarpCore(Level level, BlockPos controller) {
    TestMultiblocks.build(level, "warp_core", controller, Direction.NORTH);
  }

  private static void buildObservatory(Level level, BlockPos controller) {
    TestMultiblocks.build(
        level,
        "observatory",
        controller,
        Direction.NORTH,
        Map.of('n', ProcessingRegistry.part("lens_block")));
  }
}

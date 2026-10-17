package advRocketry.orbit;

import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class GasMissionTests {
  @GameTest(template = "empty")
  public static void pressurizedCargoUsesConfiguredTankCapacity(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, OrbitalRegistry.FLUID_TANK.get().defaultBlockState(), 3);
    var tankBlock = (FluidTankBlockEntity) level.getBlockEntity(pos);
    RocketStructure ship = new RocketStructure();
    ship.intakePower = 10;
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            level.getBlockState(pos),
            tankBlock.saveWithFullMetadata(level.registryAccess())));
    helper.assertTrue(
        GasMission.capacity(ship) == tankBlock.tank.getCapacity()
            && GasMission.capacity(ship) == 64000,
        "Packed pressurized tank used hatch capacity");
    CompoundTag snapshot = new CompoundTag();
    snapshot.put("ship", ship.save(true));
    snapshot.putDouble("x", pos.getX() + .5);
    snapshot.putDouble("z", pos.getZ() + .5);
    Mission mission = new Mission(Mission.Kind.GAS, snapshot);
    mission.gas = ResourceLocation.parse("adv_rocketry:oxygen");
    RocketEntity returned = GasMission.createReturn(level, mission);
    helper.assertTrue(returned != null, "Pressurized cargo mission did not return");
    CompoundTag berthed = returned.saveWithoutId(new CompoundTag());
    berthed.putInt("flight", 0);
    returned.load(berthed);
    FluidTank recovered = new FluidTank(64000);
    helper.assertTrue(
        returned.moveCargoFluid(recovered, true) == 64000
            && recovered.getFluid().is(ProcessingFluids.DEFINITIONS.get("oxygen").source.get()),
        "Pressurized mission cargo could not unload");
    helper.assertTrue(
        returned.moveCargoFluid(new FluidTank(64000), true) == 0,
        "Pressurized cargo duplicated on unload");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void gasGiantMissionFillsPackedFluidHatch(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    GalaxyData galaxy = GalaxyData.get(server);
    Planet giant =
        galaxy.planets.values().stream()
            .filter(planet -> planet.gasGiant)
            .findFirst()
            .orElseThrow();
    var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos testOrigin = helper.absolutePos(new BlockPos(5, 4, 5));
    BlockPos pos =
        new BlockPos(
            1_000_000 + Math.floorMod(testOrigin.getX(), 1_000_000),
            120,
            1_000_000 + Math.floorMod(testOrigin.getZ(), 1_000_000));
    space.setBlock(pos.below(), Blocks.IRON_BLOCK.defaultBlockState(), 2);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = giant.id;
    station.x = pos.getX();
    station.y = pos.getY();
    station.z = pos.getZ();
    long stationId = galaxy.newStation(station);
    RocketStructure ship = new RocketStructure();
    ship.height = 2;
    ship.intakePower = 10;
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            ProcessingRegistry.part("gas_intake").defaultBlockState(),
            new CompoundTag()));
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO.above(),
            MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(),
            new CompoundTag()));
    RocketEntity source = RocketRegistry.ROCKET.get().create(space);
    helper.assertTrue(source != null, "Gas collection rocket could not be created");
    source.initialize(ship, pos);
    helper.assertTrue(GasMission.possible(space, source), "Gas collection rocket was not eligible");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("atm_analyzer").get()));
    helper.assertTrue(
        player
            .getItemInHand(InteractionHand.MAIN_HAND)
            .is(ProcessingRegistry.PART_ITEMS.get("atm_analyzer").get()),
        "Mock player did not hold the analyzer");
    source.interact(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(source.selectedGas() == 1, "Analyzer did not select the next gas");
    CompoundTag programmed = source.saveWithoutId(new CompoundTag());
    helper.assertTrue(programmed.getInt("selected_gas") == 1, "Rocket save omitted selected gas");
    source.load(programmed);
    helper.assertTrue(source.selectedGas() == 1, "Selected gas was not saved on the rocket");
    helper.assertTrue(
        GasMission.begin(space, source),
        "Uncrewed intake rocket could not start gas mission from gas-giant orbit");
    long missionId =
        galaxy.miningMissions.entrySet().stream()
            .filter(
                entry ->
                    entry.getValue().kind == Mission.Kind.GAS
                        && Math.abs(entry.getValue().rocket.getDouble("x") - pos.getX() - .5) < 1)
            .findFirst()
            .orElseThrow()
            .getKey();
    Mission gasMission = galaxy.miningMissions.get(missionId);
    helper.assertTrue(
        giant.gases.get(1).equals(gasMission.gas), "Gas mission ignored the selected gas");
    helper.assertTrue(gasMission.duration == 3200, "Original gas collection duration changed");
    RocketEntity returned = GasMission.createReturn(space, gasMission);
    helper.assertTrue(
        returned != null && returned.selectedGas() == 1,
        "Gas mission return lost the selected gas");
    FluidTank tank = new FluidTank(64000);
    tank.readFromNBT(
        space.registryAccess(), returned.structure().cells.get(1).data().getCompound("fluid"));
    helper.assertTrue(
        tank.getFluidAmount() == 16000 && returned.propellant() == 0,
        "Returned rocket lost gas cargo or retained propellant");
    helper.assertTrue(
        GasMission.finish(space, gasMission), "Gas mission failed to spawn its return");
    galaxy.miningMissions.remove(missionId);
    galaxy.stations.remove(stationId);
    galaxy.setDirty();
    helper.succeed();
  }
}

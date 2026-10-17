package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class StationComparatorTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void stationComparatorsTrackCurrentGravityAndAltitude(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    var space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos =
        new BlockPos(
            6000000 + Math.floorMod(helper.absolutePos(BlockPos.ZERO).getX(), 100000),
            120,
            6000000);
    BlockPos altitude = pos.south(3);
    BlockPos secondGravity = pos.south(6);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = pos.getX();
    station.y = pos.getY();
    station.z = pos.getZ();
    station.gravity = (double) (.1);
    station.targetGravity = 10;
    station.altitude = 4;
    station.targetAltitude = 4;
    long id = galaxy.newStation(station);
    space.setBlock(pos, OrbitalRegistry.STATION_GRAVITY_CONTROLLER.get().defaultBlockState(), 3);
    space.setBlock(
        secondGravity, OrbitalRegistry.STATION_GRAVITY_CONTROLLER.get().defaultBlockState(), 3);
    space.setBlock(
        altitude, OrbitalRegistry.STATION_ALTITUDE_CONTROLLER.get().defaultBlockState(), 3);
    for (BlockPos controller : new BlockPos[] {pos, altitude, secondGravity}) {
      helper.assertTrue(
          space.getBlockState(controller).hasAnalogOutputSignal(),
          "Station controller lacks analog capability");
      helper.assertTrue(
          space.getBlockState(controller).getAnalogOutputSignal(space, controller) == 0,
          "Minimum station setting did not produce zero");
      space.setBlock(controller.west().below(), Blocks.STONE.defaultBlockState(), 3);
      space.setBlock(
          controller.west(),
          Blocks.COMPARATOR
              .defaultBlockState()
              .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
          3);
    }
    station = galaxy.stations.get(id).copy();
    station.gravity = 1d;
    station.altitude = 190;
    galaxy.stations.put(id, station);
    helper.assertTrue(
        space.getBlockState(pos).getAnalogOutputSignal(space, pos) == 15
            && space.getBlockState(altitude).getAnalogOutputSignal(space, altitude) == 15,
        "Maximum station settings did not produce fifteen");
    station = station.copy();
    station.gravity = (double) (0);
    galaxy.stations.put(id, station);
    helper.assertTrue(
        space.getBlockState(pos).getAnalogOutputSignal(space, pos) == 0,
        "Zero gravity produced invalid negative redstone");
    station = station.copy();
    station.gravity = (double) (.4535);
    station.targetGravity = 50;
    station.altitude = 7.99f;
    station.targetAltitude = 9;
    galaxy.stations.put(id, station);
    space.getChunkSource().updateChunkForced(new ChunkPos(pos), true);
    helper.runAfterDelay(
        10,
        () -> {
          // Explicit controller ticks also cover a station outside normal player ticking range.
          ((OrbitalBlockEntity) space.getBlockEntity(pos)).tick();
          ((OrbitalBlockEntity) space.getBlockEntity(altitude)).tick();
          ((OrbitalBlockEntity) space.getBlockEntity(secondGravity)).tick();
          helper.assertTrue(
              space.getBlockState(pos).getAnalogOutputSignal(space, pos) == 6,
              "Gravity comparator used target instead of current gravity");
          helper.assertTrue(
              space.getBlockState(altitude).getAnalogOutputSignal(space, altitude) == 1,
              "Altitude comparator did not follow current altitude");
          helper.runAfterDelay(
              10,
              () -> {
                helper.assertTrue(
                    ((ComparatorBlockEntity) space.getBlockEntity(pos.west())).getOutputSignal()
                            == 6
                        && ((ComparatorBlockEntity) space.getBlockEntity(secondGravity.west()))
                                .getOutputSignal()
                            == 6,
                    "Both station gravity comparators were not notified");
                helper.assertTrue(
                    ((ComparatorBlockEntity) space.getBlockEntity(altitude.west()))
                            .getOutputSignal()
                        == 1,
                    "Station altitude comparator was not notified");
                for (BlockPos controller : new BlockPos[] {pos, altitude, secondGravity}) {
                  space.setBlock(controller, Blocks.AIR.defaultBlockState(), 3);
                  space.setBlock(controller.west(), Blocks.AIR.defaultBlockState(), 3);
                }
                space.getChunkSource().updateChunkForced(new ChunkPos(pos), false);
                galaxy.stations.remove(id);
                galaxy.setDirty();
                helper.succeed();
              });
        });
  }
}

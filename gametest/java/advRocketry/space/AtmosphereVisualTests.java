package advRocketry.space;

import advRocketry.life.AtmosphereSync;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.life.SealedRooms;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class AtmosphereVisualTests {
  @GameTest(template = "empty")
  public static void originalPressureFogAndAltitudeDensityBoundaries(GameTestHelper helper) {
    var vacuum = AtmosphereVisuals.fog(0, false, 100);
    var normal = AtmosphereVisuals.fog(100, false, 100);
    var dense = AtmosphereVisuals.fog(200, false, 100);
    var visor = AtmosphereVisuals.fog(800, true, 100);
    helper.assertTrue(
        Math.abs(vacuum.near() - 150) < .01 && Math.abs(vacuum.far() - 200.2) < .01,
        "Vacuum fog does not use the original extended view distance");
    helper.assertTrue(
        Math.abs(normal.near() - 75) < .01 && dense.near() == -150 && dense.far() == 100,
        "Dense atmosphere did not thicken the foreground fog");
    helper.assertTrue(
        visor.equals(normal) && AtmosphereVisuals.fog(800, false, 100).equals(dense),
        "Atmosphere visor or density cap differs from original fog rules");
    helper.assertTrue(
        AtmosphereVisuals.densityAtHeight(200, 0) == 2
            && AtmosphereVisuals.densityAtHeight(200, 256) == 2
            && AtmosphereVisuals.densityAtHeight(200, 356) == 1
            && AtmosphereVisuals.densityAtHeight(200, 456) == 0
            && AtmosphereVisuals.densityAtHeight(0, 64) == 0,
        "Sky density does not fade across the original 256-to-456 altitude interval");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void localPressureFollowsActiveVentRoomAndReturnsOutside(GameTestHelper helper) {
    var level = helper.getLevel();
    var planet = GalaxyData.get(level.getServer()).planet(level);
    int pressure = planet.atmosphere;
    var player = FakePlayerFactory.getMinecraft(level);
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
    level.setBlock(pos, LifeSupportRegistry.VENT.get().defaultBlockState(), 3);
    var vent = (OxygenBlockEntity) level.getBlockEntity(pos);
    try {
      planet.atmosphere = 175;
      vent.setRoom(Set.of(pos.above()));
      vent.active = true;
      SealedRooms.register(vent);
      player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
      helper.assertTrue(
          AtmosphereSync.pressure(player) == 100, "Active room did not override planet pressure");
      player.setPos(pos.getX() + 2, pos.getY() + 1, pos.getZ());
      helper.assertTrue(
          AtmosphereSync.pressure(player) == 175, "Room pressure leaked outside its sealed volume");
      player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
      vent.active = false;
      helper.assertTrue(
          AtmosphereSync.pressure(player) == 175, "Unpowered vent retained its pressure override");
    } finally {
      planet.atmosphere = pressure;
      SealedRooms.remove(vent);
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void stationVisualSnapshotUsesSavedRadiusWithoutPackedInventories(
      GameTestHelper helper) {
    var galaxy = new GalaxyData();
    Station station = new Station();
    station.deployed = true;
    station.radius = 1024;
    station.orbitPlanet = 1;
    station.x = 0;
    station.z = 0;
    station.structure = new CompoundTag();
    station.fuel = 123;
    station.altitude = 50;
    station.rotationAngle[1] = 30;
    long id = galaxy.newStation(station);
    var snapshot = GalaxySync.stationSnapshot(galaxy).getCompound(Long.toString(id));
    helper.assertTrue(
        !snapshot.contains("structure")
            && !snapshot.contains("fuel")
            && snapshot.getFloat("orbital_altitude") == 50
            && snapshot.getDouble("rotation_angle_y") == 30,
        "Visual packet included packed blocks or lost orbital properties");
    helper.assertTrue(
        StationLogic.at(List.of(Station.load(snapshot)), new BlockPos(500, 120, 0)) != null
            && StationLogic.at(List.of(Station.load(snapshot)), new BlockPos(1024, 120, 0)) == null,
        "Client and server station bounds do not honor the saved radius");
    snapshot.remove("station_radius");
    helper.assertTrue(
        StationLogic.at(List.of(Station.load(snapshot)), new BlockPos(128, 120, 0)) == null,
        "Old saves lost their 128-block fallback radius");
    snapshot.putInt("station_radius", 1024);
    snapshot.putLong("attached_to", 7);
    helper.assertTrue(
        StationLogic.at(List.of(Station.load(snapshot)), BlockPos.ZERO) == null,
        "Attached station fragments were treated as independent orbital bodies");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void twilightLuminosityAndEclipsesFollowOriginalBoundaries(GameTestHelper helper) {
    Star sol = new Star(0, "Sol", 100, 1, 0, 0, false);
    helper.assertTrue(
        Math.abs(AtmosphereVisuals.perceivedLight(sol, 100) - 1) < .0001
            && AtmosphereVisuals.perceivedLight(sol, 200) < 1,
        "Stellar distance does not attenuate perceived daylight");
    helper.assertTrue(
        AtmosphereVisuals.surfaceLight(100, 0, .25, false) == .25
            && AtmosphereVisuals.surfaceLight(100, 0, .25, true) == 1
            && AtmosphereVisuals.surfaceLight(100, .5f, 1, false) == 0,
        "Night-vision visor, noon or night surface illumination is incorrect");
    Planet moon = new Planet(1, "Luna");
    moon.orbitalDistance = 100;
    double eclipse = AtmosphereVisuals.eclipse(moon, Math.PI, 100);
    helper.assertTrue(
        eclipse > 0
            && eclipse < .1
            && AtmosphereVisuals.eclipse(moon, 0, 100) == 1
            && Math.abs(AtmosphereVisuals.eclipse(moon, -Math.PI, 100) - eclipse) < .0001,
        "Eclipse attenuation did not track conjunction and retrograde angles");
    moon.orbitalPhi = 90;
    helper.assertTrue(
        AtmosphereVisuals.eclipse(moon, Math.PI, 100) == 1,
        "Polar orbit eclipsed a sun outside its orbital plane");
    var dawn = AtmosphereVisuals.twilight(.25f, 1, 1);
    helper.assertTrue(
        dawn != null
            && dawn[3] > .99
            && dawn[0] > dawn[1]
            && AtmosphereVisuals.twilight(0, 1, 1) == null
            && AtmosphereVisuals.twilight(.25f, 0, 1) == null,
        "Twilight did not require a horizon sun and an atmosphere");
    helper.succeed();
  }
}

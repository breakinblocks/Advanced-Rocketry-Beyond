package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.Station;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import advRocketry.space.Star;
import advRocketry.space.TerrainType;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class SpaceFlightTests {
  @GameTest(template = "empty", timeoutTicks = 600)
  public static void asteroidRcsMovesPhysicalRocketAndRejectsOrdinaryWorld(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    Planet asteroid = new Planet(1222, "RCS asteroid");
    asteroid.terrain = TerrainType.ASTEROID;
    galaxy.planets.put(asteroid.id, asteroid);
    var level = PlanetRuntime.create(helper.getLevel().getServer(), asteroid);
    RocketStructure structure = new RocketStructure();
    structure.seats = 1;
    structure.height = 6;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("seat").get().defaultBlockState(),
            new CompoundTag()));
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(structure, new BlockPos(0, 260, 0));
    var pilot = helper.makeMockPlayer(GameType.SURVIVAL);
    pilot.startRiding(rocket);
    rocket.spaceControls(pilot, 1, 0, 1, true);
    double startY = rocket.getY(), startZ = rocket.getZ();
    for (int tick = 0; tick < 5; tick++) rocket.tick();
    helper.assertTrue(
        rocket.asteroidRcs()
            && rocket.flight() == 0
            && rocket.getY() > startY
            && rocket.getZ() > startZ
            && rocket.navigation() == null,
        "Asteroid RCS failed to maneuver the physical ship independently of free space flight");
    helper.assertTrue(
        rocket.getBoundingBox().getZsize() > 5.9 && rocket.getBoundingBox().getYsize() < 1.01,
        "Horizontal rocket retained an upright collision box");
    CompoundTag saved = new CompoundTag();
    rocket.addAdditionalSaveData(saved);
    RocketEntity loaded = RocketRegistry.ROCKET.get().create(level);
    loaded.readAdditionalSaveData(saved);
    helper.assertTrue(loaded.asteroidRcs(), "Asteroid RCS mode was lost on reload");
    rocket.spaceControls(pilot, 0, 0, 0, true);
    helper.assertTrue(!rocket.asteroidRcs(), "RCS toggle could not return the ship to landed mode");
    helper.assertTrue(
        rocket.getBoundingBox().getYsize() > 5.9,
        "Leaving RCS did not restore upright collision bounds");
    pilot.stopRiding();
    rocket.discard();
    loaded.discard();
    RocketEntity ordinary = RocketRegistry.ROCKET.get().create(helper.getLevel());
    ordinary.initialize(structure, helper.absolutePos(new BlockPos(2, 3, 2)));
    pilot.startRiding(ordinary);
    ordinary.spaceControls(pilot, 1, 1, 1, true);
    helper.assertTrue(!ordinary.asteroidRcs(), "RCS was enabled outside asteroid terrain");
    pilot.stopRiding();
    ordinary.discard();
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 600)
  public static void crewedSpaceEntryAndReturnPreservePassengers(GameTestHelper helper)
      throws ReflectiveOperationException {
    Field field = AdvancedRocketryConfig.class.getDeclaredField("EXPERIMENTAL_SPACE_FLIGHT");
    field.setAccessible(true);
    ModConfigSpec.BooleanValue setting = (ModConfigSpec.BooleanValue) field.get(null);
    boolean previous = setting.get();
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    RocketStructure structure = new RocketStructure();
    structure.seats = 1;
    structure.height = 2;
    structure.thrust = 1000;
    structure.destination = Integer.MAX_VALUE;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    structure.cells.add(
        new RocketStructure.Cell(
            new BlockPos(0, 1, 0),
            RocketRegistry.PARTS.get("seat").get().defaultBlockState(),
            new CompoundTag()));
    rocket.initialize(structure, helper.absolutePos(new BlockPos(2, 3, 2)));
    helper.getLevel().addFreshEntity(rocket);
    var cookie =
        CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "space-flight-test"), false);
    var pilot =
        new ServerPlayer(
            server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    long visitedStation = 0;
    try {
      NetworkRegistry.configureMockConnection(connection);
      server.getPlayerList().placeNewPlayer(connection, pilot, cookie);
      pilot.setGameMode(GameType.CREATIVE);
      setting.set(true);
      pilot.teleportTo(rocket.getX(), rocket.getY() + 2, rocket.getZ());
      helper.assertTrue(pilot.startRiding(rocket), "Server player could not board");
      helper.assertTrue(rocket.launch(pilot), "Crewed empty-guidance launch was rejected");
      rocket.setPos(rocket.getX(), AdvancedRocketryConfig.orbitHeight(), rocket.getZ());
      rocket.tick();
      RocketEntity orbit = pilot.getVehicle() instanceof RocketEntity vehicle ? vehicle : null;
      helper.assertTrue(
          orbit != null && orbit.flight() == 3 && orbit.navigation() != null,
          "Ascent did not transfer the complete rocket into free space; source flight="
              + rocket.flight()
              + ", removed="
              + rocket.isRemoved()
              + ", y="
              + rocket.getY()
              + ", pilot level="
              + pilot.level().dimension().location()
              + ", orbit="
              + (orbit == null ? "missing" : orbit.flight()));
      helper.assertTrue(
          pilot.level() == space && pilot.getVehicle() == orbit,
          "Space entry lost the seated server player");
      orbit.navigation().yaw = 37;
      orbit.navigation().velocity = new Vec3(1, .01, 2);
      CompoundTag savedNavigation = orbit.navigation().save();
      UUID rocketId = orbit.getUUID();
      server.getPlayerList().remove(pilot);
      channel.finishAndReleaseAll();
      helper.assertTrue(orbit.isRemoved(), "Logout left the single-player root vehicle running");
      pilot =
          new ServerPlayer(
              server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
      connection = new Connection(PacketFlow.SERVERBOUND);
      channel = new EmbeddedChannel(connection);
      NetworkRegistry.configureMockConnection(connection);
      server.getPlayerList().placeNewPlayer(connection, pilot, cookie);
      orbit = pilot.getVehicle() instanceof RocketEntity vehicle ? vehicle : null;
      helper.assertTrue(
          orbit != null
              && orbit.getUUID().equals(rocketId)
              && orbit.flight() == 3
              && pilot.level() == space
              && orbit.navigation().save().equals(savedNavigation),
          "Native reconnect did not restore the saved root vehicle and navigation");
      Station station = new Station();
      station.deployed = true;
      station.orbitPlanet = GalaxyData.EARTH_ID;
      station.x = 4000000;
      station.y = 180;
      station.z = 4000000;
      station.radius = 128;
      visitedStation = galaxy.newStation(station);
      for (int x = 3999999; x <= 4000001; x++)
        for (int z = 3999999; z <= 4000001; z++)
          space.setBlock(new BlockPos(x, 180, z), Blocks.IRON_BLOCK.defaultBlockState(), 2);
      var stations = SpaceNavigation.stations(galaxy, 0);
      int stationIndex = 0;
      while (stations.get(stationIndex).getKey() != visitedStation) stationIndex++;
      orbit.navigation().position =
          SpaceNavigation.stationPosition(
              galaxy.planets.get(GalaxyData.EARTH_ID), Vec3.ZERO, stationIndex, stations.size());
      orbit.navigation().velocity = Vec3.ZERO;
      orbit.tick();
      var stationShip = pilot.getVehicle() instanceof RocketEntity vehicle ? vehicle : null;
      helper.assertTrue(
          stationShip != null
              && stationShip.flight() == 2
              && stationShip.navigation() == null
              && pilot.level() == space
              && Math.abs(stationShip.getX() - 4000000.5) < 1,
          "Station approach failed to preserve the passenger during same-dimension descent: "
              + (stationShip == null
                  ? "no ship"
                  : "flight="
                      + stationShip.flight()
                      + ", x="
                      + stationShip.getX()
                      + ", nav="
                      + (stationShip.navigation() == null
                          ? "none"
                          : stationShip.navigation().save()))
              + ", level="
              + pilot.level().dimension().location()
              + ", station="
              + visitedStation);
      stationShip.setPos(stationShip.getX(), 181, stationShip.getZ());
      stationShip.tick();
      helper.assertTrue(
          stationShip.flight() == 0 && stationShip.launch(pilot),
          "Landed free-flight rocket could not relaunch from its station");
      stationShip.setPos(
          stationShip.getX(), AdvancedRocketryConfig.stationClearanceHeight(), stationShip.getZ());
      stationShip.tick();
      orbit = pilot.getVehicle() instanceof RocketEntity vehicle ? vehicle : null;
      helper.assertTrue(
          orbit != null && orbit.flight() == 3 && orbit.navigation().focus == 0,
          "Station relaunch failed to return to the orbiting planet's navigation context");
      orbit.navigation().position = Vec3.ZERO;
      orbit.navigation().velocity = Vec3.ZERO;
      orbit.tick();
      RocketEntity returning = pilot.getVehicle() instanceof RocketEntity vehicle ? vehicle : null;
      helper.assertTrue(
          returning != null && returning.flight() == 2 && returning.navigation() == null,
          "Planet approach did not start normal descent");
      helper.assertTrue(
          pilot.level() == helper.getLevel() && pilot.getVehicle() == returning,
          "Deorbit lost the seated server player");
      helper.assertTrue(
          returning.structure().cells.size() == 2 && returning.guidanceChip().isEmpty(),
          "Free-flight round trip changed the ship or invented a guidance chip");
    } finally {
      setting.set(previous);
      if (visitedStation != 0) galaxy.stations.remove(visitedStation);
      pilot.stopRiding();
      server.getPlayerList().remove(pilot);
      channel.finishAndReleaseAll();
      for (var level : server.getAllLevels()) {
        var entity = level.getEntity(rocket.getUUID());
        if (entity != null) entity.discard();
      }
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void spaceNavigationTransitionsSteeringAndStations(GameTestHelper helper) {
    GalaxyData galaxy = new GalaxyData();
    Star star = new Star(29, "Flight star", 100, 1, 0, 0, false);
    galaxy.stars.put(29, star);
    Planet planet = new Planet(1220, "Flight planet");
    planet.star = 29;
    galaxy.planets.put(planet.id, planet);
    Planet moon = new Planet(1221, "Flight moon");
    moon.star = 29;
    moon.parent = planet.id;
    moon.gravity = .2f;
    galaxy.planets.put(moon.id, moon);
    SpaceNavigation flight = SpaceNavigation.launch(moon, galaxy);
    helper.assertTrue(
        flight.focus == planet.id && flight.star == 29 && flight.position.x == 1100,
        "Moon launch did not enter the parent system outside its landing radius");
    flight.move(1, 1, 1);
    helper.assertTrue(
        flight.yaw == 5 && flight.velocity.x < 0 && flight.velocity.y > 0 && flight.velocity.z > 0,
        "Original turning, forward acceleration or vertical RCS changed");
    flight.rcs = false;
    double speed = flight.velocity.length();
    flight.move(1, 1, 1);
    helper.assertTrue(
        flight.yaw == 5 && flight.velocity.length() < speed,
        "Disabled RCS accepted thrust or lost damping");
    SpaceNavigation restored = SpaceNavigation.load(flight.save());
    helper.assertTrue(
        restored.save().equals(flight.save()), "Navigation persistence lost motion or RCS state");
    flight.position = new Vec3(17000, 0, 0);
    flight.velocity = new Vec3(1, 0, 0);
    helper.assertTrue(
        flight.navigate(galaxy, 0) == null
            && flight.focus == -1
            && flight.velocity.equals(Vec3.ZERO),
        "Leaving planetary space failed to switch to solar navigation and stop");
    flight.position = SpaceNavigation.bodyPosition(planet, star, null, 0).add(150, 0, 0);
    flight.velocity = new Vec3(-1, 0, 0);
    flight.navigate(galaxy, 0);
    helper.assertTrue(
        flight.focus == planet.id && flight.position.length() == 16000,
        "Approaching a solar body did not enter its planetary navigation area");
    flight.position = Vec3.ZERO;
    helper.assertTrue(
        flight.navigate(galaxy, 0).planet() == planet.id,
        "Close planetary approach did not deorbit");
    planet.gasGiant = true;
    helper.assertTrue(flight.navigate(galaxy, 0) == null, "Gas giant allowed a surface landing");
    planet.gasGiant = false;
    flight.position = SpaceNavigation.bodyPosition(moon, star, planet, 0);
    helper.assertTrue(
        flight.navigate(galaxy, 0).planet() == moon.id,
        "Moon approach did not deorbit to the moon");
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = planet.id;
    galaxy.stations.put(91L, station);
    flight.position = SpaceNavigation.stationPosition(planet, Vec3.ZERO, 0, 1);
    helper.assertTrue(
        flight.navigate(galaxy, 0).station() == 91,
        "Station approach lost its exact destination ID");
    station.warpEta = 100L;
    helper.assertTrue(flight.navigate(galaxy, 0) == null, "Station in warp was still boardable");
    galaxy.stations.clear();
    for (long id = 1; id <= 200; id++) {
      var crowded = new Station();
      crowded.deployed = true;
      crowded.orbitPlanet = planet.id;
      galaxy.stations.put(id, crowded);
    }
    flight.position = SpaceNavigation.stationPosition(planet, Vec3.ZERO, 100, 200);
    helper.assertTrue(
        flight.navigate(galaxy, 0).station() == 101,
        "Overlapping station approach radii selected an earlier station instead of the nearest"
            + " one");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void freeFlightPilotAuthorityAndPersistence(GameTestHelper helper) {
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    RocketStructure structure = new RocketStructure();
    structure.seats = 1;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("seat").get().defaultBlockState(),
            new CompoundTag()));
    rocket.initialize(structure, helper.absolutePos(new BlockPos(2, 3, 2)));
    SpaceNavigation flight =
        SpaceNavigation.launch(
            GalaxyData.get(helper.getLevel().getServer()).planets.get(GalaxyData.EARTH_ID),
            GalaxyData.get(helper.getLevel().getServer()));
    flight.position = new Vec3(5000, 0, 0);
    CompoundTag save = new CompoundTag();
    rocket.addAdditionalSaveData(save);
    save.putInt("flight", 3);
    save.put("space_navigation", flight.save());
    rocket.readAdditionalSaveData(save);
    var pilot = helper.makeMockPlayer(GameType.SURVIVAL);
    var outsider = helper.makeMockPlayer(GameType.SURVIVAL);
    helper.assertTrue(pilot.startRiding(rocket), "Pilot could not board navigation test rocket");
    rocket.spaceControls(outsider, 1, 1, 1, true);
    rocket.tick();
    helper.assertTrue(
        rocket.navigation().velocity.equals(Vec3.ZERO) && rocket.navigation().rcs,
        "Non-pilot controlled the rocket");
    rocket.spaceControls(pilot, 127, 127, 127, false);
    rocket.tick();
    helper.assertTrue(
        rocket.navigation().yaw == 5 && rocket.navigation().velocity.length() < 2.01,
        "Pilot input was not bounded to normal steering");
    rocket.spaceControls(pilot, 0, 0, 0, true);
    rocket.tick();
    helper.assertTrue(!rocket.navigation().rcs, "Pilot RCS toggle was ignored");
    rocket.addAdditionalSaveData(save);
    RocketEntity loaded = RocketRegistry.ROCKET.get().create(helper.getLevel());
    loaded.readAdditionalSaveData(save);
    helper.assertTrue(
        loaded.flight() == 3 && loaded.navigation().save().equals(rocket.navigation().save()),
        "Rocket save/reload lost active free flight");
    pilot.stopRiding();
    rocket.discard();
    loaded.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void freeFlightLaunchRemainsOptInAndRequiresEmptyGuidance(GameTestHelper helper)
      throws ReflectiveOperationException {
    Field field = AdvancedRocketryConfig.class.getDeclaredField("EXPERIMENTAL_SPACE_FLIGHT");
    field.setAccessible(true);
    ModConfigSpec.BooleanValue setting = (ModConfigSpec.BooleanValue) field.get(null);
    boolean previous = setting.get();
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    RocketStructure structure = new RocketStructure();
    structure.seats = 1;
    structure.thrust = 1000;
    structure.destination = Integer.MAX_VALUE;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    rocket.initialize(structure, helper.absolutePos(new BlockPos(3, 3, 3)));
    var pilot = helper.makeMockPlayer(GameType.SURVIVAL);
    pilot.startRiding(rocket);
    try {
      setting.set(false);
      helper.assertTrue(
          !rocket.launch(pilot), "Disabled experimental flight accepted missing destination");
      setting.set(true);
      helper.assertTrue(
          rocket.launch(pilot) && rocket.flight() == 1,
          "Enabled empty-guidance crewed launch failed");
      CompoundTag saved = new CompoundTag();
      rocket.addAdditionalSaveData(saved);
      helper.assertTrue(
          saved.getBoolean("free_launch"), "Ascent did not retain pending free-flight entry");
    } finally {
      setting.set(previous);
      pilot.stopRiding();
      rocket.discard();
    }
    helper.succeed();
  }
}

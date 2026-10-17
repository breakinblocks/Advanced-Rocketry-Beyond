package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class PlanetRespawnTests {
  private static ModConfigSpec.BooleanValue setting(String name)
      throws ReflectiveOperationException {
    var field = AdvancedRocketryConfig.class.getDeclaredField(name);
    field.setAccessible(true);
    return (ModConfigSpec.BooleanValue) field.get(null);
  }

  @GameTest(template = "empty", timeoutTicks = 600)
  public static void planetAndStationBedsRespectAtmosphereAndRespawnSettings(GameTestHelper helper)
      throws ReflectiveOperationException {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var allow = setting("ALLOW_PLANET_RESPAWN");
    var force = setting("FORCE_PLANET_RESPAWN");
    boolean previousAllow = allow.get(), previousForce = force.get();
    long oldDay = server.overworld().getDayTime();
    var cookie =
        CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "planet-bed-test"), false);
    var player =
        new ServerPlayer(
            server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    NetworkRegistry.configureMockConnection(connection);
    server.getPlayerList().placeNewPlayer(connection, player, cookie);
    try {
      for (int id : new int[] {1, -2}) {
        var planet = galaxy.planets.get(id);
        int oldPressure = planet.atmosphere, oldTemperature = planet.temperature;
        boolean oldOxygen = planet.oxygen;
        long oldRotation = planet.rotationPeriod;
        var level = PlanetRuntime.create(server, planet);
        try {
          planet.atmosphere = 100;
          planet.temperature = 286;
          planet.oxygen = true;
          planet.rotationPeriod = 24000;
          server.overworld().setDayTime(18000);
          level.updateSkyBrightness();
          var head = new BlockPos(2000, 180, 2000);
          var foot = head.south();
          for (int x = 1998; x <= 2003; x++)
            for (int z = 1998; z <= 2003; z++) {
              level.setBlock(new BlockPos(x, 179, z), Blocks.IRON_BLOCK.defaultBlockState(), 2);
              for (int y = 180; y <= 183; y++)
                level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
            }
          level.setBlock(
              foot,
              Blocks.RED_BED
                  .defaultBlockState()
                  .setValue(BedBlock.FACING, Direction.NORTH)
                  .setValue(BedBlock.PART, BedPart.FOOT),
              2);
          level.setBlock(
              head,
              Blocks.RED_BED
                  .defaultBlockState()
                  .setValue(BedBlock.FACING, Direction.NORTH)
                  .setValue(BedBlock.PART, BedPart.HEAD),
              2);
          helper.assertTrue(
              level.dimensionType().bedWorks(), "Native bed would explode in dimension " + id);
          player.changeDimension(
              new DimensionTransition(
                  level,
                  new Vec3(2001.5, 180, 2000.5),
                  Vec3.ZERO,
                  0,
                  0,
                  DimensionTransition.DO_NOTHING));
          player.setGameMode(GameType.CREATIVE);
          allow.set(false);
          force.set(false);
          helper.assertTrue(
              player.startSleepInBed(head).right().isPresent(),
              "Breathable bed rejected sleep in " + id);
          player.stopSleepInBed(true, true);
          allow.set(true);
          player.teleportTo(2001.5, 180, 2000.5);
          helper.assertTrue(
              player.startSleepInBed(head).right().isPresent(),
              "Enabled respawn bed rejected sleep in " + id);
          player.stopSleepInBed(true, true);
          helper.assertTrue(
              player.getRespawnDimension().equals(level.dimension())
                  && head.equals(player.getRespawnPosition()),
              "Bed did not store native respawn position in " + id);
          player = server.getPlayerList().respawn(player, false, RemovalReason.KILLED);
          player.connection.player = player;
          helper.assertTrue(
              player.level() == level && player.blockPosition().distSqr(head) < 16,
              "Actual respawn lost planet/station bed in " + id);
          planet.atmosphere = 0;
          player.teleportTo(2001.5, 180, 2000.5);
          helper.assertTrue(
              player.startSleepInBed(head).left().orElse(null)
                  == BedSleepingProblem.NOT_POSSIBLE_HERE,
              "Vacuum bed allowed unforced sleep in " + id);
          force.set(true);
          helper.assertTrue(
              player.startSleepInBed(head).right().isPresent(),
              "Forced vacuum sleep failed in " + id);
          player.stopSleepInBed(true, true);
          force.set(false);
          player = server.getPlayerList().respawn(player, false, RemovalReason.KILLED);
          player.connection.player = player;
          helper.assertTrue(
              player.level() == server.overworld(),
              "Unsafe saved bed did not fall back to Earth in " + id);
          planet.atmosphere = 100;
          player.changeDimension(
              new DimensionTransition(
                  level,
                  new Vec3(2001.5, 180, 2000.5),
                  Vec3.ZERO,
                  0,
                  0,
                  DimensionTransition.DO_NOTHING));
          player.setGameMode(GameType.CREATIVE);
          level.setBlock(head.above(), Blocks.STONE.defaultBlockState(), 2);
          var blockedResult = player.startSleepInBed(head);
          helper.assertTrue(
              blockedResult.left().orElse(null) == BedSleepingProblem.OBSTRUCTED,
              "Blocked bed result in "
                  + id
                  + ": "
                  + blockedResult
                  + ", sleeping="
                  + player.isSleeping()
                  + ", position="
                  + player.position()
                  + ", solid="
                  + level.getBlockState(head.above()));
          level.setBlock(head.above(), Blocks.AIR.defaultBlockState(), 2);
          player.teleportTo(2030, 180, 2000);
          helper.assertTrue(
              player.startSleepInBed(head).left().orElse(null) == BedSleepingProblem.TOO_FAR_AWAY,
              "Distant bed allowed sleep in " + id);
        } finally {
          planet.atmosphere = oldPressure;
          planet.temperature = oldTemperature;
          planet.oxygen = oldOxygen;
          planet.rotationPeriod = oldRotation;
        }
      }
    } finally {
      allow.set(previousAllow);
      force.set(previousForce);
      server.overworld().setDayTime(oldDay);
      server.getPlayerList().remove(player);
      channel.finishAndReleaseAll();
    }
    helper.succeed();
  }
}

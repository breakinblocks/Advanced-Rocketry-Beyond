package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.space.GalaxyData;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class StationChipTests {
  @GameTest(template = "empty")
  public static void locationsKeepSelectionAcrossDepartureReloadAndPlanetChanges(
      GameTestHelper helper) {
    var chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
    chip.set(ModComponents.STATION_LINK, new StationLink(900));
    StationChipLocations.remember(chip, 0, new BlockPos(10, 70, 20));
    helper.assertTrue(
        StationChipLocations.destination(chip, 0).pos().getX() == 10,
        "Existing departure was lost");
    StationChipLocations.add(chip, 0, new BlockPos(200, 80, 300), "Mine");
    StationChipLocations.add(chip, 0, new BlockPos(400, 90, 500), "Home");
    helper.assertTrue(
        StationChipLocations.select(chip, 0, 2), "Could not select saved destination");
    StationChipLocations.remember(chip, 0, new BlockPos(600, 75, 700));
    StationChipLocations.remember(chip, 1, new BlockPos(800, 85, 900));
    var restored =
        ItemStack.parse(
                helper.getLevel().registryAccess(), chip.save(helper.getLevel().registryAccess()))
            .orElseThrow();
    helper.assertTrue(
        StationChipLocations.destination(restored, 0).name().equals("Home")
            && StationChipLocations.destination(restored, 0).pos().getX() == 400,
        "Departure overwrote selected named location");
    helper.assertTrue(
        StationChipLocations.list(restored, 0).getFirst().pos().getX() == 600
            && StationChipLocations.destination(restored, 1).pos().getX() == 800,
        "Per-planet Last entry was overwritten");
    helper.assertTrue(
        !StationChipLocations.select(restored, 0, -1)
            && !StationChipLocations.select(restored, 0, 128),
        "Invalid selection accepted");
    helper.assertTrue(
        StationChipLocations.delete(restored, 0)
            && StationChipLocations.selection(restored, 0) == 0,
        "Deleting selection did not fall back to Last");
    helper.assertTrue(!StationChipLocations.delete(restored, 0), "Last was deletable");
    StationChipLocations.clear(restored, 0);
    helper.assertTrue(
        StationChipLocations.list(restored, 0).size() == 1
            && StationChipLocations.destination(restored, 0).pos().getX() == 600
            && StationLink.read(restored) == 900,
        "Clear erased Last or station identity");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void heldChipMenuRejectsReplacementAndSpaceAdds(GameTestHelper helper) {
    var level = helper.getLevel();
    var cookie =
        CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "chip-menu-test"), false);
    var player =
        new ServerPlayer(
            level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    NetworkRegistry.configureMockConnection(connection);
    level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
    try {
      var chip = new ItemStack(OrbitalRegistry.STATION_CHIP.get());
      player.setItemInHand(InteractionHand.OFF_HAND, chip);
      int planet = StationLogic.orbitPlanet(
              GalaxyData.get(player.server), player.serverLevel(), player.blockPosition());
      var menu = new StationChipMenu(7, InteractionHand.OFF_HAND, planet, true, chip);
      helper.assertTrue(
          menu.stillValid(player) && menu.apply(player, 2, 0, "Depot"),
          "Off-hand editor did not add current location");
      helper.assertTrue(
          player.containerMenu instanceof StationChipMenu
              && StationChipLocations.list(chip, planet).size() == 2,
          "Editor failed to refresh after mutation");
      var orbital = new StationChipMenu(8, InteractionHand.OFF_HAND, planet, false, chip);
      helper.assertTrue(
          !orbital.apply(player, 2, 0, "Space") && !orbital.apply(player, 40, 0, "Invalid"),
          "Menu accepted forbidden command");
      player.setItemInHand(InteractionHand.OFF_HAND, chip.copy());
      helper.assertTrue(
          !menu.stillValid(player) && !menu.apply(player, 0, 0, ""),
          "Replaced chip remained editable");
      var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
      StationChipMenu.writeOpeningData(buffer, InteractionHand.OFF_HAND, planet, false, chip);
      var client = new StationChipMenu(9, player.getInventory(), buffer);
      helper.assertTrue(
          client.locations.size() == 2
              && !client.canAdd
              && client.locations.get(1).name().equals("Depot"),
          "Opening snapshot lost destinations");
      buffer.release();
    } finally {
      player.closeContainer();
      level.getServer().getPlayerList().remove(player);
      channel.finishAndReleaseAll();
    }
    helper.succeed();
  }
}

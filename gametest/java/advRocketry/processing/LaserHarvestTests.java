package advRocketry.processing;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class LaserHarvestTests {
  @GameTest(template = "empty")
  public static void laserHarvestsDiamondTierDropsAndPreservesBedrock(GameTestHelper helper) {
    var level = helper.getLevel();
    var server = level.getServer();
    var cookie =
        CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "laser-harvest"), false);
    var player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    NetworkRegistry.configureMockConnection(connection);
    server.getPlayerList().placeNewPlayer(connection, player, cookie);
    var origin = helper.absolutePos(new BlockPos(5, 4, 5)).atY(220);
    BlockPos target = origin.south(8).above();
    var gun = (LaserGunItem) ProcessingRegistry.PART_ITEMS.get("basic_laser_gun").get();
    var stack = new ItemStack(gun);
    try {
      player.setGameMode(GameType.SURVIVAL);
      player.setPos(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
      player.setYRot(0);
      player.setYHeadRot(0);
      player.setXRot(0);
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      helper.assertTrue(
          stack.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 0,
          "Laser should mine by held beam rather than ordinary block punching");
      var blocks =
          new Object[][] {
            {Blocks.STONE, Items.COBBLESTONE},
            {Blocks.DIAMOND_ORE, Items.DIAMOND},
            {Blocks.OBSIDIAN, Items.OBSIDIAN}
          };
      for (var pair : blocks) {
        var block = (Block) pair[0];
        var drop = (Item) pair[1];
        level.setBlock(target, block.defaultBlockState(), 2);
        helper.assertTrue(
            stack.isCorrectToolForDrops(block.defaultBlockState()),
            "Laser lost diamond harvest tier for " + block);
        gun.use(level, player, InteractionHand.MAIN_HAND);
        gun.onUseTick(level, player, stack, 16);
        helper.assertTrue(
            target.equals(gun.lockedTarget(player)),
            "Laser test did not lock "
                + target
                + "; lock="
                + gun.lockedTarget(player)
                + " ray="
                + player.pick(50, 1, false).getLocation());
        gun.finishUsingItem(stack, level, player);
        helper.assertTrue(level.getBlockState(target).isAir(), "Laser did not harvest " + block);
        var drops =
            level.getEntitiesOfClass(
                ItemEntity.class, new AABB(target).inflate(1), item -> item.getItem().is(drop));
        helper.assertTrue(
            drops.stream().mapToInt(item -> item.getItem().getCount()).sum() == 1,
            "Laser destroyed block without its drop: " + block);
        drops.forEach(ItemEntity::discard);
        player.stopUsingItem();
      }
      level.setBlock(target, Blocks.BEDROCK.defaultBlockState(), 2);
      gun.use(level, player, InteractionHand.MAIN_HAND);
      gun.onUseTick(level, player, stack, 16);
      gun.finishUsingItem(stack, level, player);
      helper.assertTrue(
          level.getBlockState(target).is(Blocks.BEDROCK), "Laser bypassed unbreakable-block rule");
      helper.assertTrue(
          !stack.isEmpty() && !stack.isDamageableItem(),
          "Original unbreakable laser acquired durability");
    } finally {
      level.removeBlock(target, false);
      server.getPlayerList().remove(player);
      channel.finishAndReleaseAll();
    }
    helper.succeed();
  }
}

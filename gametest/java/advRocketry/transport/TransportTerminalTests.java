package advRocketry.transport;

import advRocketry.processing.MachinePorts;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import advRocketry.processing.PortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class TransportTerminalTests {
  @GameTest(template = "empty")
  public static void terminalUsesFreshStockAndReturnsCraftingIngredients(GameTestHelper h) {
    var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(2, 2, 3));
    level.setBlock(pos, TransportRegistry.ALL.get(TransportRegistry.Kind.ITEM).get().defaultBlockState(), 3);
    level.setBlock(pos.west(), MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState(), 3);
    level.setBlock(pos.east(), MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 3);
    var pipe = (TransportBlockEntity) level.getBlockEntity(pos);
    pipe.modes[Direction.WEST.ordinal()] = TransportBlockEntity.EXTRACT; pipe.changed();
    var source = (PortBlockEntity) level.getBlockEntity(pos.west()); var sink = (PortBlockEntity) level.getBlockEntity(pos.east());
    source.inventory.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 8));
    var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "TerminalTest"), ClientInformation.createDefault()); player.setPos(Vec3.atCenterOf(pos)); player.getAbilities().mayBuild = true;
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    NetworkRegistry.configureMockConnection(connection);
    player.connection = new ServerGamePacketListenerImpl(level.getServer(), connection, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
    var menu = new TransportTerminalMenu(2, player.getInventory(), pipe); menu.broadcastChanges();
    h.assertTrue(menu.getSlot(0).getItem().is(Items.OAK_PLANKS), "Stock listing missing");
    source.inventory.setStackInSlot(0, ItemStack.EMPTY);
    menu.clicked(0, 0, ClickType.PICKUP, player);
    h.assertTrue(menu.getCarried().isEmpty(), "Stale listing created stock");
    source.inventory.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 8));
    menu.clicked(0, 1, ClickType.PICKUP, player);
    h.assertTrue(menu.getCarried().getCount() == 1 && source.inventory.getStackInSlot(0).getCount() == 7, "Withdrawal not conserved");
    menu.clicked(37, 0, ClickType.PICKUP, player);
    menu.setCarried(new ItemStack(Items.OAK_PLANKS)); menu.clicked(40, 0, ClickType.PICKUP, player);
    h.assertTrue(menu.getSlot(36).getItem().is(Items.STICK), "Server crafting lookup missing");
    menu.clicked(36, 0, ClickType.PICKUP, player);
    h.assertTrue(menu.getCarried().is(Items.STICK) && menu.getCarried().getCount() == 4, "Craft result incorrect");
    h.assertTrue(menu.getSlot(37).getItem().isEmpty() && menu.getSlot(40).getItem().isEmpty(), "Craft did not consume ingredients");
    menu.clickMenuButton(player, 2);
    h.assertTrue(menu.getCarried().isEmpty() && sink.inventory.getStackInSlot(0).getCount() == 4, "Deposit not conserved");
    menu.setCarried(new ItemStack(Items.DIAMOND)); menu.clicked(37, 0, ClickType.PICKUP, player); menu.removed(player);
    h.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 1, "Closing lost crafting ingredient"); channel.finishAndReleaseAll(); h.succeed();
  }
  @GameTest(template = "empty")
  public static void terminalDoesNotExposeFilteredOrRedstoneBlockedSources(GameTestHelper h) {
    var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(2, 2, 3));
    level.setBlock(pos, TransportRegistry.ALL.get(TransportRegistry.Kind.ITEM).get().defaultBlockState(), 3);
    level.setBlock(pos.west(), MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState(), 3);
    var pipe = (TransportBlockEntity) level.getBlockEntity(pos); pipe.modes[Direction.WEST.ordinal()] = TransportBlockEntity.EXTRACT;
    var source = (PortBlockEntity) level.getBlockEntity(pos.west()); source.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
    pipe.side(Direction.WEST).configured = true; pipe.changed();
    var player = h.makeMockPlayer(GameType.SURVIVAL); player.setPos(Vec3.atCenterOf(pos));
    var menu = new TransportTerminalMenu(2, player.getInventory(), pipe); menu.broadcastChanges();
    h.assertTrue(menu.getSlot(0).getItem().isEmpty(), "Empty allowlist leaked stock");
    pipe.side(Direction.WEST).configured = false; pipe.side(Direction.WEST).extractRedstone = 1; pipe.changed();
    h.assertTrue(TransportNetworks.storage(pipe, false).isEmpty(), "Terminal bypassed source redstone"); h.succeed();
  }
}

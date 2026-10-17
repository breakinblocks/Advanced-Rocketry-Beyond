package advRocketry.rocket;

import advRocketry.processing.MachinePorts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class CargoLinkTests {
  @GameTest(template = "empty")
  public static void assemblerLinksReconnectOnlyItsLandedRocket(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    BlockPos builderPos = pos.north(3);
    level.setBlock(pos, RocketRegistry.ITEM_LOADER.get().defaultBlockState(), 3);
    level.setBlock(
        builderPos, RocketRegistry.PARTS.get("rocket_builder").get().defaultBlockState(), 3);
    var port = (RocketCargoPortBlockEntity) level.getBlockEntity(pos);
    var builder = (RocketBlockEntity) level.getBlockEntity(builderPos);
    var near = rocket(helper, pos.east(2));
    var target = rocket(helper, pos.east(7));
    target.linkBuilder(builderPos);
    helper.assertTrue(port.linkBuilder(builder), "Cargo port rejected assembler");
    port.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND));
    port.tick();
    helper.assertTrue(
        target.moveCargoItems(new ItemStackHandler(4), true) == 1,
        "Assembler link missed its rocket");
    var flight = new CompoundTag();
    target.addAdditionalSaveData(flight);
    flight.putInt("flight", 1);
    target.readAdditionalSaveData(flight);
    port.inventory.setStackInSlot(0, new ItemStack(Items.EMERALD));
    port.tick();
    helper.assertTrue(
        port.inventory.getStackInSlot(0).is(Items.EMERALD), "Departing rocket was still loaded");
    var savedPort = port.saveWithFullMetadata(level.registryAccess());
    level.removeBlockEntity(pos);
    var restored =
        new RocketCargoPortBlockEntity(pos, RocketRegistry.ITEM_LOADER.get().defaultBlockState());
    restored.loadWithComponents(savedPort, level.registryAccess());
    level.setBlockEntity(restored);
    target.discard();
    var replacement = rocket(helper, pos.east(6));
    replacement.linkBuilder(builderPos);
    restored.tick();
    helper.assertTrue(
        replacement.moveCargoItems(new ItemStackHandler(4), true) == 1
            && near.moveCargoItems(new ItemStackHandler(4), true) == 0,
        "Reloaded assembler link missed replacement or chose nearby rocket");
    restored.link(replacement);
    replacement.addAdditionalSaveData(flight);
    flight.putInt("flight", 1);
    replacement.readAdditionalSaveData(flight);
    restored.tick();
    flight.putInt("flight", 0);
    replacement.readAdditionalSaveData(flight);
    restored.inventory.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT));
    restored.tick();
    helper.assertTrue(
        restored.inventory.getStackInSlot(0).is(Items.GOLD_INGOT),
        "Direct link survived liftoff without assembler association");
    replacement.discard();
    near.discard();
    helper.succeed();
  }

  private static RocketEntity rocket(GameTestHelper helper, BlockPos pos) {
    var ship = new RocketStructure();
    var data = new CompoundTag();
    data.put("inventory", new ItemStackHandler(4).serializeNBT(helper.getLevel().registryAccess()));
    ship.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO, MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), data));
    var rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    rocket.initialize(ship, pos);
    helper.getLevel().addFreshEntity(rocket);
    return rocket;
  }

  @GameTest(template = "empty")
  public static void cargoLinksAndRedstoneSurviveReloadWithoutCrossLoading(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.ITEM_LOADER.get().defaultBlockState(), 3);
    var port = (RocketCargoPortBlockEntity) level.getBlockEntity(pos);
    var near = rocket(helper, pos.east(2));
    var chosen = rocket(helper, pos.east(8));
    port.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
    port.tick();
    helper.assertTrue(
        port.inventory.getStackInSlot(0).getCount() == 4,
        "Unlinked cargo port selected a nearby rocket");
    helper.assertTrue(port.link(chosen), "Within-range rocket link failed");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(pos.getX(), pos.getY(), pos.getZ());
    var menu = new RocketCargoPortMenu(0, player.getInventory(), port);
    helper.assertTrue(!menu.clickMenuButton(player, 99), "Invalid redstone command accepted");
    menu.clickMenuButton(player, 0); // OFF -> INVERTED
    menu.clickMenuButton(player, 0); // INVERTED -> ON
    int north = 2 + Direction.NORTH.ordinal();
    menu.clickMenuButton(player, north);
    menu.clickMenuButton(player, north); // input
    int south = 2 + Direction.SOUTH.ordinal();
    menu.clickMenuButton(player, south); // output
    level.setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    port.tick();
    helper.assertTrue(
        port.inventory.getStackInSlot(0).getCount() == 4,
        "Unselected redstone face powered loading");
    level.setBlock(pos.north(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    var saved = port.saveWithFullMetadata(level.registryAccess());
    level.removeBlockEntity(pos);
    var restored =
        new RocketCargoPortBlockEntity(pos, RocketRegistry.ITEM_LOADER.get().defaultBlockState());
    restored.loadWithComponents(saved, level.registryAccess());
    level.setBlockEntity(restored);
    restored.tick();
    var cargo = new ItemStackHandler(4);
    helper.assertTrue(
        restored.inventory.getStackInSlot(0).isEmpty()
            && near.moveCargoItems(new ItemStackHandler(4), true) == 0
            && chosen.moveCargoItems(cargo, true) == 4,
        "Saved link cross-loaded or lost selected rocket");
    helper.assertTrue(
        !level.getBlockState(pos).getValue(RocketCargoPortBlock.POWERED),
        "Empty loader incorrectly reported full rocket");
    for (int slot = 0; slot < 4; slot++)
      restored.inventory.setStackInSlot(slot, new ItemStack(Items.DIAMOND, 64));
    for (int i = 0; i < 4; i++) restored.tick();
    helper.assertTrue(chosen.cargoComplete(false, false), "Loaded rocket did not become full");
    helper.assertTrue(
        level.getSignal(pos, Direction.NORTH) == 15 && level.getSignal(pos, Direction.SOUTH) == 0,
        "Output side routing reversed or ignored");
    restored.redstone.cycle(1); // ON -> OFF
    restored.tick();
    helper.assertTrue(
        level.getSignal(pos, Direction.NORTH) == 0, "OFF output still emitted signal");
    restored.redstone.cycle(1); // OFF -> INVERTED
    restored.tick();
    helper.assertTrue(
        level.getSignal(pos, Direction.NORTH) == 0, "Inverted output emitted on full rocket");
    chosen.moveCargoItems(new ItemStackHandler(4), true);
    restored.tick();
    helper.assertTrue(
        level.getSignal(pos, Direction.NORTH) == 15,
        "Inverted output did not emit on incomplete cargo");
    restored.unlink();
    restored.inventory.setStackInSlot(0, new ItemStack(Items.EMERALD));
    restored.tick();
    helper.assertTrue(
        restored.inventory.getStackInSlot(0).is(Items.EMERALD),
        "Unlink resumed nearest-rocket loading");
    near.discard();
    chosen.discard();
    helper.succeed();
  }
}

package advRocketry.processing;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class FluidHatchTests {
  @GameTest(template = "empty")
  public static void pagedMenuPreservesPortOrderingAndTransfers(GameTestHelper helper) {
    var level = helper.getLevel();
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    List<BlockPos> positions = new ArrayList<>();
    for (int index = 0; index < 8; index++) {
      BlockPos pos = helper.absolutePos(new BlockPos(index + 1, 3, 3));
      level.setBlock(pos, MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState(), 3);
      positions.add(pos);
    }
    var last = (PortBlockEntity) level.getBlockEntity(positions.getLast());
    last.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
    var menu = new MachineMenu(0, player.getInventory(), positions.getFirst(), positions);
    helper.assertTrue(menu.pageCount() == 2 && menu.slots.size() == 68, "Paging changed slot count");
    menu.page(1);
    helper.assertTrue(menu.page() == 1 && menu.getSlot(28).getItem().getCount() == 7, "Paging changed slot identity");
    var moved = menu.quickMoveStack(player, 28);
    helper.assertTrue(moved.is(Items.DIAMOND) && moved.getCount() == 7 && last.inventory.getStackInSlot(0).isEmpty(), "Second-page extraction failed");
    helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 7, "Page transfer lost items");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void menuOpeningCarriesPortsBeforeBlockEntitiesArrive(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos input = helper.absolutePos(new BlockPos(3, 3, 3));
    BlockPos output = input.east(2);
    level.setBlock(input, MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(), 3);
    level.setBlock(output, MachinePorts.BLOCK_FLUID_OUTPUT_BLOCK.get().defaultBlockState(), 3);
    RegistryFriendlyByteBuf buffer =
        new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    try {
      MachineMenu.writeOpeningData(buffer, input, List.of(input, output), level);
      level.removeBlockEntity(input);
      level.removeBlockEntity(output);
      var menu =
          new MachineMenu(0, helper.makeMockPlayer(GameType.SURVIVAL).getInventory(), buffer);
      helper.assertTrue(
          menu.ports().size() == 2 && menu.slots.size() == 40,
          "Opening packet lost unloaded port definitions or container slots");
      helper.assertTrue(
          menu.ports().getFirst().kind() == MachinePorts.Kind.FLUID_INPUT
              && menu.ports().getLast().kind() == MachinePorts.Kind.FLUID_OUTPUT,
          "Opening packet changed port roles");
      for (int index = 0; index < 9; index++) menu.setData(index, 0);
      helper.succeed();
    } finally {
      buffer.release();
    }
  }

  @GameTest(template = "empty")
  public static void hatchContainersPreserveFluidWhenBlockedAndResumeAfterReload(
      GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(), 3);
    PortBlockEntity port = (PortBlockEntity) level.getBlockEntity(pos);
    helper.assertTrue(
        port.myTank.getCapacity() == 16000, "Original hatch capacity was not sixteen buckets");
    helper.assertTrue(
        port.automationInventory().getSlots() == 2, "Fluid hatch lacks two container slots");
    helper.assertTrue(
        port.automationInventory()
            .insertItem(0, new ItemStack(Items.WATER_BUCKET), false)
            .isEmpty(),
        "Container input rejected a water bucket");
    port.tick();
    helper.assertTrue(
        port.myTank.getFluidAmount() == 1000
            && port.inventory.getStackInSlot(0).isEmpty()
            && port.inventory.getStackInSlot(1).is(Items.BUCKET),
        "Bucket was not emptied into hatch");
    helper.assertTrue(
        !port.automationInventory().insertItem(1, new ItemStack(Items.BUCKET), false).isEmpty()
            && port.automationInventory().extractItem(0, 1, false).isEmpty(),
        "Container automation ignored input/output directions");
    port.automationInventory().extractItem(1, 1, false);
    port.inventory.setStackInSlot(0, new ItemStack(Items.BUCKET));
    port.inventory.setStackInSlot(1, new ItemStack(Items.LAVA_BUCKET));
    port.tick();
    helper.assertTrue(
        port.myTank.getFluidAmount() == 1000 && port.inventory.getStackInSlot(0).is(Items.BUCKET),
        "Blocked output consumed fluid or input");
    var saved = port.saveWithFullMetadata(level.registryAccess());
    var state = port.getBlockState();
    level.removeBlockEntity(pos);
    PortBlockEntity restored = new PortBlockEntity(pos, state);
    restored.loadWithComponents(saved, level.registryAccess());
    level.setBlockEntity(restored);
    restored.tick();
    helper.assertTrue(
        restored.myTank.getFluidAmount() == 1000
            && restored.inventory.getStackInSlot(1).is(Items.LAVA_BUCKET),
        "Blocked container state was lost across reload");
    restored.automationInventory().extractItem(1, 1, false);
    restored.tick();
    helper.assertTrue(
        restored.myTank.isEmpty()
            && restored.inventory.getStackInSlot(0).isEmpty()
            && restored.inventory.getStackInSlot(1).is(Items.WATER_BUCKET),
        "Empty container did not fill after output cleared");
    var menu =
        new MachineMenu(
            0, helper.makeMockPlayer(GameType.SURVIVAL).getInventory(), pos, List.of(pos));
    helper.assertTrue(
        menu.slots.size() == 38
            && menu.getSlot(0).mayPlace(new ItemStack(Items.BUCKET))
            && !menu.getSlot(1).mayPlace(new ItemStack(Items.BUCKET)),
        "Hatch menu container slots have incorrect insertion rules");
    BlockPos outputPos = pos.east(3);
    level.setBlock(outputPos, MachinePorts.BLOCK_FLUID_OUTPUT_BLOCK.get().defaultBlockState(), 3);
    PortBlockEntity output = (PortBlockEntity) level.getBlockEntity(outputPos);
    output.myTank.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 2000),
        IFluidHandler.FluidAction.EXECUTE);
    output.inventory.setStackInSlot(0, new ItemStack(Items.BUCKET, 2));
    output.tick();
    output.tick();
    helper.assertTrue(
        output.myTank.getFluidAmount() == 1000
            && output.inventory.getStackInSlot(0).getCount() == 1
            && output
                .inventory
                .getStackInSlot(1)
                .is(ProcessingFluids.DEFINITIONS.get("oxygen").bucket.get()),
        "Custom gas container fill or full output guard failed");
    output.automationInventory().extractItem(1, 1, false);
    output.tick();
    helper.assertTrue(
        output.myTank.isEmpty() && output.inventory.getStackInSlot(0).isEmpty(),
        "Queued gas bucket did not resume");
    helper.succeed();
  }
}

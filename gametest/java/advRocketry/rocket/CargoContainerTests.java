package advRocketry.rocket;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class CargoContainerTests {
  @GameTest(template = "empty")
  public static void fluidCargoContainersPersistAndMenusOpenBeforeChunks(GameTestHelper helper) {
    var level = helper.getLevel();
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    for (var block :
        new RocketCargoPortBlock[] {
          RocketRegistry.FLUID_LOADER.get(), RocketRegistry.FLUID_UNLOADER.get()
        }) {
      BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
      level.setBlock(pos, block.defaultBlockState(), 3);
      var port = (RocketCargoPortBlockEntity) level.getBlockEntity(pos);
      var items = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
      helper.assertTrue(
          items != null && items.getSlots() == 2, "Missing fluid cargo container capability");
      helper.assertTrue(
          port.tank.getCapacity() == 16000, "Fluid cargo hatch lost original capacity");
      helper.assertTrue(
          items.insertItem(0, new ItemStack(Items.WATER_BUCKET), false).isEmpty(),
          "Container input rejected bucket");
      port.tick();
      helper.assertTrue(
          port.tank.getFluidAmount() == 1000 && items.getStackInSlot(1).is(Items.BUCKET),
          "Cargo hatch failed to empty container");
      items.extractItem(1, 1, false);
      items.insertItem(0, new ItemStack(Items.BUCKET), false);
      port.inventory.setStackInSlot(1, new ItemStack(Items.LAVA_BUCKET));
      port.tick();
      helper.assertTrue(
          port.tank.getFluidAmount() == 1000 && items.getStackInSlot(0).is(Items.BUCKET),
          "Blocked output consumed fluid");
      var saved = port.saveWithFullMetadata(level.registryAccess());
      level.removeBlockEntity(pos);
      var restored = new RocketCargoPortBlockEntity(pos, block.defaultBlockState());
      restored.loadWithComponents(saved, level.registryAccess());
      level.setBlockEntity(restored);
      restored.tick();
      helper.assertTrue(
          restored.tank.getFluidAmount() == 1000, "Blocked output changed after reload");
      restored.inventory.setStackInSlot(1, ItemStack.EMPTY);
      restored.tick();
      helper.assertTrue(
          restored.tank.isEmpty() && restored.inventory.getStackInSlot(1).is(Items.WATER_BUCKET),
          "Reloaded cargo container processing failed");
      var menu = new RocketCargoPortMenu(0, player.getInventory(), restored);
      helper.assertTrue(
          menu.fluid()
              && menu.slots.size() == 38
              && !menu.getSlot(1).mayPlace(new ItemStack(Items.BUCKET)),
          "Fluid cargo menu lost its two directed slots");
      var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
      try {
        RocketCargoPortMenu.writeOpeningData(buffer, restored);
        level.removeBlockEntity(pos);
        var opening = new RocketCargoPortMenu(0, player.getInventory(), buffer);
        helper.assertTrue(
            opening.fluid() && opening.slots.size() == 38,
            "Cargo menu depends on loaded block entity");
        opening.setData(0, 1234);
      } finally {
        buffer.release();
      }
      level.removeBlock(pos, false);
    }
    helper.succeed();
  }
}

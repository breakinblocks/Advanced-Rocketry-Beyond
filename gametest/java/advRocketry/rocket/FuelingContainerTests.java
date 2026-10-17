package advRocketry.rocket;

import advRocketry.processing.ProcessingFluids;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class FuelingContainerTests {
  @GameTest(template = "empty")
  public static void fuelingContainersAndLinksPreserveFuelAcrossReload(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.FUELING_STATION.get().defaultBlockState(), 3);
    var station = (FuelingStationBlockEntity) level.getBlockEntity(pos);
    var fuel = ProcessingFluids.DEFINITIONS.get("rocket_fuel");
    var input = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
    var output = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN);
    helper.assertTrue(
        input.getSlots() == 1 && output.getSlots() == 1, "Fueling station lost sided slots");
    helper.assertTrue(
        !input.insertItem(0, new ItemStack(Items.WATER_BUCKET), false).isEmpty()
            && !input.insertItem(0, new ItemStack(Items.BUCKET), false).isEmpty(),
        "Non-fuel container accepted");
    helper.assertTrue(
        input.insertItem(0, new ItemStack(fuel.bucket.get()), false).isEmpty(),
        "Fuel bucket rejected");
    station.inventory.setStackInSlot(1, new ItemStack(Items.LAVA_BUCKET));
    station.tick();
    helper.assertTrue(
        station.tank.isEmpty() && station.inventory.getStackInSlot(0).is(fuel.bucket.get()),
        "Blocked output lost fuel bucket");
    var ship = new RocketStructure();
    ship.capacity = 10;
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, pos.east(3));
    level.addFreshEntity(rocket);
    station.energy.receiveEnergy(1000, false);
    helper.assertTrue(station.link(rocket), "Fueling link failed");
    station.cycleOutputMode(); // OFF
    var saved = station.saveWithFullMetadata(level.registryAccess());
    level.removeBlockEntity(pos);
    var restored =
        new FuelingStationBlockEntity(
            pos, RocketRegistry.FUELING_STATION.get().defaultBlockState());
    restored.loadWithComponents(saved, level.registryAccess());
    level.setBlockEntity(restored);
    restored.tick();
    helper.assertTrue(
        restored.energy.getEnergyStored() == 1000 && restored.tank.isEmpty(),
        "Blocked reload consumed energy");
    restored.automationItems(Direction.DOWN).extractItem(0, 1, false);
    restored.tick();
    helper.assertTrue(
        restored.tank.getFluidAmount() == 990
            && restored.inventory.getStackInSlot(1).is(Items.BUCKET)
            && rocket.propellant() == 10
            && restored.energy.getEnergyStored() == 970,
        "Saved link/container did not fuel exactly ten mB for thirty FE");
    restored.tick();
    helper.assertTrue(
        !level.getBlockState(pos).getValue(FuelingStationBlock.POWERED),
        "Saved OFF mode emitted redstone");
    restored.cycleOutputMode(); // INVERTED
    restored.tick();
    helper.assertTrue(
        !level.getBlockState(pos).getValue(FuelingStationBlock.POWERED),
        "Inverted full-fuel signal wrong");
    restored.cycleOutputMode(); // ON
    restored.tick();
    helper.assertTrue(
        level.getBlockState(pos).getValue(FuelingStationBlock.POWERED), "Full-fuel signal absent");
    restored.unlink();
    restored.tank.drain(5000, IFluidHandler.FluidAction.EXECUTE);
    restored.tank.fill(new FluidStack(fuel.source.get(), 9), IFluidHandler.FluidAction.EXECUTE);
    rocket.discard();
    rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, pos.east(3));
    level.addFreshEntity(rocket);
    restored.link(rocket);
    restored.tick();
    helper.assertTrue(
        restored.tank.getFluidAmount() == 9
            && restored.energy.getEnergyStored() == 970
            && rocket.propellant() == 0,
        "Sub-ten-mB operation ignored original minimum");
    restored.tank.fill(new FluidStack(fuel.source.get(), 1), IFluidHandler.FluidAction.EXECUTE);
    restored.tick();
    restored.tick();
    helper.assertTrue(
        restored.tank.isEmpty()
            && rocket.propellant() == 10
            && level.getBlockState(pos).getValue(FuelingStationBlock.POWERED),
        "Last ten mB lost full-rocket output when station tank emptied");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var menu = new FuelingStationMenu(0, player.getInventory(), restored);
    helper.assertTrue(
        menu.slots.size() == 38 && !menu.getSlot(1).mayPlace(new ItemStack(fuel.bucket.get())),
        "Fueling menu output accepted insertion");
    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    try {
      buffer.writeBlockPos(pos);
      level.removeBlockEntity(pos);
      var opened = new FuelingStationMenu(0, player.getInventory(), buffer);
      helper.assertTrue(
          opened.slots.size() == 38, "Fueling opening depended on client block entity");
      for (int index = 0; index < 3; index++) opened.setData(index, 0);
    } finally {
      buffer.release();
    }
    rocket.discard();
    helper.succeed();
  }
}

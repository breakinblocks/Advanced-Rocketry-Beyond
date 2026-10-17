package advRocketry.rocket;

import advRocketry.orbit.AsteroidChipItem;
import advRocketry.orbit.MiningMission;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.processing.ProcessingFluids;
import advRocketry.space.GalaxyData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class MonitoringTests {
  @GameTest(template = "empty")
  public static void monitorTracksSavedMiningMissionAndReturningRocket(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.MONITORING_STATION.get().defaultBlockState(), 3);
    var monitor = (RocketMonitoringBlockEntity) level.getBlockEntity(pos);
    var ship = new RocketStructure();
    ship.drillingPower = 10;
    ship.capacity = 1000000;
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, pos.east(3));
    rocket.fluids.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 123456),
        IFluidHandler.FluidAction.EXECUTE);
    level.addFreshEntity(rocket);
    monitor.link(rocket);
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var menu = new RocketMonitoringMenu(0, player.getInventory(), monitor);
    helper.assertTrue(
        menu.value(2) == 123456 && menu.value(3) == 1000000,
        "Monitor menu truncated large fuel values");
    rocket.setPos(rocket.getX(), 500, rocket.getZ());
    monitor.tick();
    helper.assertTrue(
        level.getBlockState(pos).getAnalogOutputSignal(level, pos) > 0, "Height comparator absent");
    var chip = new ItemStack(OrbitalRegistry.ASTEROID_CHIP.get());
    AsteroidChipItem.program(chip, "adv_rocketry:small", 12345);
    helper.assertTrue(
        MiningMission.begin(level, rocket, chip), "Original asteroid mission failed to begin");
    var galaxy = GalaxyData.get(level.getServer());
    var entry =
        galaxy.miningMissions.entrySet().stream()
            .filter(candidate -> rocket.getUUID().equals(candidate.getValue().sourceRocket()))
            .findFirst()
            .orElseThrow();
    var mission = entry.getValue();
    mission.duration = 3600;
    mission.startTick = level.getServer().overworld().getGameTime() - 1800;
    rocket.discard();
    monitor.refresh();
    helper.assertTrue(
        monitor.value(7) == 1 && monitor.value(6) == 500 && monitor.value(5) == 90,
        "Monitor lost original three-phase mission progress/time");
    var saved = monitor.saveWithFullMetadata(level.registryAccess());
    level.removeBlockEntity(pos);
    var restored =
        new RocketMonitoringBlockEntity(
            pos, RocketRegistry.MONITORING_STATION.get().defaultBlockState());
    restored.loadWithComponents(saved, level.registryAccess());
    level.setBlockEntity(restored);
    restored.refresh();
    helper.assertTrue(
        restored.value(7) == 1 && restored.value(5) == 90, "Mission link did not survive reload");
    helper.assertTrue(MiningMission.finish(level, mission), "Monitored mission failed to return");
    galaxy.miningMissions.remove(entry.getKey());
    restored.refresh();
    helper.assertTrue(
        restored.value(7) == 0 && restored.value(4) == 2 && restored.value(9) == 1,
        "Monitor did not reconnect to returning rocket");
    var returning = (RocketEntity) level.getEntity(restored.linkedRocket());
    var savedRocket = new CompoundTag();
    returning.addAdditionalSaveData(savedRocket);
    returning.readAdditionalSaveData(savedRocket);
    helper.assertTrue(
        returning.returnedFrom(rocket.getUUID()), "Return identity was not saved with rocket");
    var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
    try {
      buffer.writeBlockPos(pos);
      level.removeBlockEntity(pos);
      var opened = new RocketMonitoringMenu(0, player.getInventory(), buffer);
      for (int index = 0; index < 20; index++) opened.setData(index, 0);
    } finally {
      buffer.release();
    }
    returning.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void monitorHonorsDisabledAndInvertedLaunchInput(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, RocketRegistry.MONITORING_STATION.get().defaultBlockState(), 3);
    var monitor = (RocketMonitoringBlockEntity) level.getBlockEntity(pos);
    var ship = new RocketStructure();
    ship.capacity = 10000;
    ship.thrust = 1000;
    ship.consumption = 1;
    ship.destination = 1;
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(ship, pos.east(3));
    rocket.fluids.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("rocket_fuel").source.get(), 10000),
        IFluidHandler.FluidAction.EXECUTE);
    level.addFreshEntity(rocket);
    monitor.link(rocket);
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(pos.getX(), pos.getY(), pos.getZ());
    var menu = new RocketMonitoringMenu(0, player.getInventory(), monitor);
    helper.assertTrue(!menu.clickMenuButton(player, 99), "Unknown monitor button accepted");
    menu.clickMenuButton(player, 1); // OFF
    level.setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    monitor.tick();
    helper.assertTrue(rocket.flight() == 0, "OFF monitor launched rocket");
    menu.clickMenuButton(player, 1); // INVERTED
    monitor.tick();
    helper.assertTrue(rocket.flight() == 0, "Inverted monitor launched while powered");
    level.setBlock(pos.west(), Blocks.AIR.defaultBlockState(), 3);
    monitor.tick();
    helper.assertTrue(rocket.flight() == 1, "Inverted monitor did not launch when power removed");
    rocket.discard();
    helper.succeed();
  }
}

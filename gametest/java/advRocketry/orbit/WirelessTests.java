package advRocketry.orbit;

import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class WirelessTests {
  @GameTest(template = "empty")
  public static void linkedNodesMoveTypedResearchThroughDataBuses(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos sourcePos = helper.absolutePos(new BlockPos(5, 4, 5));
    BlockPos sinkPos = helper.absolutePos(new BlockPos(10, 4, 5));
    level.setBlock(sourcePos, OrbitalRegistry.WIRELESS_TRANSCEIVER.get().defaultBlockState(), 2);
    level.setBlock(sinkPos, OrbitalRegistry.WIRELESS_TRANSCEIVER.get().defaultBlockState(), 2);
    level.setBlock(sourcePos.south(), MachinePorts.DATA_BUS.get().defaultBlockState(), 2);
    level.setBlock(sinkPos.south(), MachinePorts.DATA_BUS.get().defaultBlockState(), 2);
    var source = (WirelessTransceiverBlockEntity) level.getBlockEntity(sourcePos);
    var sink = (WirelessTransceiverBlockEntity) level.getBlockEntity(sinkPos);
    var sourceBus = (PortBlockEntity) level.getBlockEntity(sourcePos.south());
    var sinkBus = (PortBlockEntity) level.getBlockEntity(sinkPos.south());
    ItemStack filled = new ItemStack(OrbitalRegistry.DATA_UNIT.get());
    DataUnitItem.store(filled, ResearchType.DISTANCE, 50);
    sourceBus.inventory.setStackInSlot(0, filled);
    sinkBus.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.DATA_UNIT.get()));
    source.cycleMode();
    sink.cycleMode();
    sink.cycleMode();
    helper.assertTrue(
        WirelessTransceiverBlockEntity.link(source, sink),
        "Wireless linker rejected two native nodes");
    source.tick();
    sink.tick();
    helper.assertTrue(
        DataUnitItem.research(sourceBus.inventory.getStackInSlot(0)).amount() == 0
            && DataUnitItem.research(sinkBus.inventory.getStackInSlot(0)).amount() == 50,
        "Linked transceivers did not move typed research to the receiving data bus: source="
            + DataUnitItem.research(sourceBus.inventory.getStackInSlot(0)).amount()
            + ", sink="
            + DataUnitItem.research(sinkBus.inventory.getStackInSlot(0)).amount()
            + ", network="
            + source.network());
    BlockPos thirdPos = helper.absolutePos(new BlockPos(5, 4, 10));
    BlockPos fourthPos = helper.absolutePos(new BlockPos(10, 4, 10));
    level.setBlock(thirdPos, OrbitalRegistry.WIRELESS_TRANSCEIVER.get().defaultBlockState(), 2);
    level.setBlock(fourthPos, OrbitalRegistry.WIRELESS_TRANSCEIVER.get().defaultBlockState(), 2);
    var third = (WirelessTransceiverBlockEntity) level.getBlockEntity(thirdPos);
    var fourth = (WirelessTransceiverBlockEntity) level.getBlockEntity(fourthPos);
    WirelessTransceiverBlockEntity.link(third, fourth);
    helper.assertTrue(source.network() != third.network(), "Independent links shared an ID");
    WirelessTransceiverBlockEntity.link(sink, third);
    helper.assertTrue(
        source.network() == fourth.network(),
        "Linking two wireless groups did not merge every member");
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    GalaxyData restored =
        GalaxyData.load(
            galaxy.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
    helper.assertTrue(
        source.network() > 0
            && restored.wirelessNetwork(source.positionKey()) == source.network()
            && restored.wirelessNetwork(sink.positionKey()) == source.network()
            && restored.wirelessNetwork(fourth.positionKey()) == source.network(),
        "Wireless link did not survive native galaxy serialization");
    helper.succeed();
  }
}

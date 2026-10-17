package advRocketry.transport;

import advRocketry.processing.MachinePorts;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.ItemStackHandler;
import advRocketry.processing.PortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class TransportPolicyTests {
  private static TransportBlockEntity pipe(GameTestHelper h, TransportRegistry.Kind kind, int x) {
    var pos = h.absolutePos(new BlockPos(x, 2, 3));
    h.getLevel().setBlock(pos, TransportRegistry.ALL.get(kind).get().defaultBlockState(), 3);
    return (TransportBlockEntity) h.getLevel().getBlockEntity(pos);
  }
  private static PortBlockEntity port(GameTestHelper h, TransportBlockEntity p, Direction side, boolean input) {
    var block = p.kind() == TransportRegistry.Kind.FLUID
        ? (input ? MachinePorts.BLOCK_FLUID_INPUT_BLOCK : MachinePorts.BLOCK_FLUID_OUTPUT_BLOCK)
        : (input ? MachinePorts.BLOCK_ENERGY_INPUT_BLOCK : MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK);
    h.getLevel().setBlock(p.getBlockPos().relative(side), block.get().defaultBlockState(), 3);
    p.modes[side.ordinal()] = input ? TransportBlockEntity.INSERT : TransportBlockEntity.EXTRACT;
    p.changed();
    return (PortBlockEntity) h.getLevel().getBlockEntity(p.getBlockPos().relative(side));
  }
  @GameTest(template = "empty")
  public static void highPriorityPartialOverflowUsesNextReceiver(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ENERGY, 2);
    var source = port(h, p, Direction.WEST, false); var first = port(h, p, Direction.NORTH, true); var second = port(h, p, Direction.SOUTH, true);
    source.injectGeneratedEnergy(10000);
    first.energyStorage.receiveEnergy(first.energyStorage.getMaxEnergyStored() - 100, false);
    var network = TransportNetworks.network(p);
    p.side(Direction.NORTH).priority = 10; p.policyChanged();
    h.assertTrue(TransportNetworks.network(p) == network, "Policy edit rebuilt topology");
    TransportNetworks.tick(p);
    h.assertTrue(first.energyStorage.getEnergyStored() == first.energyStorage.getMaxEnergyStored() && second.energyStorage.getEnergyStored() == 4900, "Partial priority overflow did not conserve 5000 FE"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void explicitFurthestOverridesLegacyRotation(GameTestHelper h) {
    var a = pipe(h, TransportRegistry.Kind.ENERGY, 1); pipe(h, TransportRegistry.Kind.ENERGY, 2); var b = pipe(h, TransportRegistry.Kind.ENERGY, 3);
    var source = port(h, a, Direction.WEST, false); var near = port(h, a, Direction.NORTH, true); var far = port(h, b, Direction.NORTH, true);
    source.injectGeneratedEnergy(5000);
    a.side(Direction.WEST).routing = TransportSideConfig.Routing.FURTHEST;
    TransportNetworks.roundRobin(a); a.policyChanged(); TransportNetworks.tick(a);
    h.assertTrue(near.energyStorage.getEnergyStored() == 0 && far.energyStorage.getEnergyStored() == 5000, "Explicit distance routing ignored"); h.succeed();
  }
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void silentReceiverSpaceRecoversWithinEnergyRetryCap(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ENERGY, 2); var source = port(h, p, Direction.WEST, false);
    var targetPos = p.getBlockPos().east(); var sink = new ChangingEnergySink(); sink.accepting = true; sink.receiveEnergy(100000, false);
    ChangingEnergySink.SINKS.put(targetPos, sink); h.getLevel().setBlock(targetPos, Blocks.EMERALD_BLOCK.defaultBlockState(), 3);
    source.injectGeneratedEnergy(10000); p.side(Direction.WEST).retryCap = 4; p.changed();
    h.runAtTickTime(20, () -> sink.extractEnergy(1000, false));
    h.runAtTickTime(25, () -> {
      h.assertTrue(sink.getEnergyStored() == 100000 && source.energyStorage.getEnergyStored() == 9000, "Silent content change did not recover within cap");
      ChangingEnergySink.SINKS.remove(targetPos); h.succeed();
    });
  }

  @GameTest(template = "empty", timeoutTicks = 160)
  public static void largeSourceScansResumeWithinBound(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ITEM, 2);
    var sourcePos = p.getBlockPos().west();
    class Counted extends ItemStackHandler {
      int tickProbes, maxProbes; long tick = Long.MIN_VALUE;
      Counted() { super(1000); }
      @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (simulate) {
          if (tick != h.getLevel().getGameTime()) { tick = h.getLevel().getGameTime(); tickProbes = 0; }
          tickProbes++; maxProbes = Math.max(maxProbes, tickProbes);
        }
        return super.extractItem(slot, amount, simulate);
      }
    }
    var input = new Counted(); input.setStackInSlot(900, new ItemStack(Items.DIAMOND, 16));
    ChangingEnergySink.ITEMS.put(sourcePos, input);
    h.getLevel().setBlock(sourcePos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
    h.getLevel().setBlock(p.getBlockPos().east(), MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 3);
    var output = (PortBlockEntity) h.getLevel().getBlockEntity(p.getBlockPos().east());
    p.modes[Direction.WEST.ordinal()] = TransportBlockEntity.EXTRACT; p.side(Direction.WEST).retryCap = 80; p.changed();
    h.succeedWhen(() -> {
      h.assertTrue(output.inventory.getStackInSlot(0).getCount() == 16, "Resumable scan did not reach slot 900");
      h.assertTrue(input.maxProbes <= 256 && input.getStackInSlot(900).isEmpty(), "Source probe bound or conservation failed: " + input.maxProbes);
      ChangingEnergySink.ITEMS.remove(sourcePos);
    });
  }

  @GameTest(template = "empty")
  public static void retryCountsCapsAndWake(GameTestHelper h) {
    var retry = new TransportRetry(); int attempts = 0;
    for (int tick = 0; tick < 200; tick++) if (retry.ready(tick)) { attempts++; retry.attempted(tick, 0, 1, 20); }
    h.assertTrue(attempts == 14 && retry.delay() == 20, "Fluid retry count/cap changed: " + attempts);
    retry.wake(); h.assertTrue(retry.ready(200), "Explicit wake failed");
    retry.attempted(200, 1, 1, 20);
    h.assertTrue(retry.delay() == 0 && retry.ready(201), "Partial success did not restore cadence");
    var item = new TransportRetry(); int itemAttempts = 0;
    for (int tick = 0; tick < 400; tick += 20) if (item.ready(tick)) { itemAttempts++; item.attempted(tick, 0, 20, 80); }
    h.assertTrue(itemAttempts == 7, "Item opportunity alignment changed: " + itemAttempts);
    h.succeed();
  }
  @GameTest(template = "empty")
  public static void filtersAndSettingsSurviveSave(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ITEM, 2); var config = p.side(Direction.WEST);
    config.configured = true; config.exact = false; config.priority = 7; config.retryCap = 80;
    config.entries[0] = TransportSideConfig.Entry.stack(new ItemStack(Items.IRON_INGOT));
    config.entries[1] = new TransportSideConfig.Entry(ItemStack.EMPTY, FluidStack.EMPTY, ResourceLocation.parse("minecraft:planks"));
    h.assertTrue(config.accepts(new ItemStack(Items.OAK_PLANKS)) && !config.accepts(new ItemStack(Items.DIAMOND)), "Tag filter mismatch");
    p.channel = 3; var tag = p.saveWithoutMetadata(h.getLevel().registryAccess());
    var loaded = new TransportBlockEntity(p.getBlockPos(), p.getBlockState()); loaded.loadWithComponents(tag, h.getLevel().registryAccess());
    h.assertTrue(loaded.channel == 3 && loaded.side(Direction.WEST).priority == 7 && loaded.side(Direction.WEST).retryCap == 80, "Policy lost on save");
    h.assertTrue(loaded.side(Direction.WEST).accepts(new ItemStack(Items.IRON_INGOT)), "Filter lost on save");
    config.entries[0] = null; config.entries[1] = null;
    h.assertTrue(!config.accepts(new ItemStack(Items.IRON_INGOT)), "Empty allowlist accepted");
    config.deny = true; h.assertTrue(config.accepts(new ItemStack(Items.IRON_INGOT)), "Empty denylist rejected"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void channelsNeverBridgeThroughUncolored(GameTestHelper h) {
    var a = pipe(h, TransportRegistry.Kind.ENERGY, 1); var b = pipe(h, TransportRegistry.Kind.ENERGY, 2); var c = pipe(h, TransportRegistry.Kind.ENERGY, 3);
    a.channel = 2; c.channel = 2; a.changed(); c.changed();
    h.assertTrue(TransportNetworks.network(a).nodes.size() == 1 && TransportNetworks.network(b).nodes.size() == 1, "Uncolored bridge joined colored cables");
    b.channel = 2; b.changed(); h.assertTrue(TransportNetworks.network(a).nodes.size() == 3, "Matching channel did not rejoin"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void priorityCapsAndSensorApplyToActualMovement(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ENERGY, 2);
    var source = port(h, p, Direction.WEST, false); var first = port(h, p, Direction.NORTH, true); var second = port(h, p, Direction.SOUTH, true);
    source.injectGeneratedEnergy(10000); p.side(Direction.SOUTH).priority = 10; p.side(Direction.SOUTH).sensor = true; p.side(Direction.WEST).extractionLimit = 700;
    p.changed(); TransportNetworks.tick(p);
    h.assertTrue(first.energyStorage.getEnergyStored() == 0 && second.energyStorage.getEnergyStored() == 700, "Priority or extraction cap ignored");
    h.assertTrue(p.sensorUntil > h.getLevel().getGameTime(), "Successful receiver did not pulse"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void fluidFiltersAndVoidRunAfterRealReceivers(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.FLUID, 2); var source = port(h, p, Direction.WEST, false); var sink = port(h, p, Direction.EAST, true);
    source.myTank.fill(new FluidStack(Fluids.WATER, 8000), FluidAction.EXECUTE);
    p.side(Direction.NORTH).disposal = true; p.side(Direction.NORTH).priority = 100;
    p.side(Direction.EAST).configured = true; p.side(Direction.EAST).entries[0] = TransportSideConfig.Entry.stack(new ItemStack(Items.WATER_BUCKET));
    p.changed(); TransportNetworks.tick(p);
    h.assertTrue(sink.myTank.getFluidAmount() == 4000 && source.myTank.getFluidAmount() == 4000, "Void consumed before real receiver"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void remoteEditWakesSleepingSource(GameTestHelper h) {
    var a = pipe(h, TransportRegistry.Kind.ENERGY, 1); var b = pipe(h, TransportRegistry.Kind.ENERGY, 2);
    port(h, a, Direction.WEST, false); port(h, b, Direction.EAST, true); TransportNetworks.network(a);
    var retry = a.side(Direction.WEST).retry; retry.attempted(h.getLevel().getGameTime(), 0, 1, 4);
    h.assertTrue(!retry.ready(h.getLevel().getGameTime()), "Fixture must sleep"); b.changed();
    h.assertTrue(retry.ready(h.getLevel().getGameTime()), "Remote edit did not wake source"); h.succeed();
  }
  @GameTest(template = "empty")
  public static void ghostTagsRespectReachAndBuildPermissions(GameTestHelper h) {
    var p = pipe(h, TransportRegistry.Kind.ITEM, 2); var player = h.makeMockPlayer(GameType.SURVIVAL); player.setPos(Vec3.atCenterOf(p.getBlockPos()));
    var menu = new TransportMenu(1, player.getInventory(), p);
    menu.setTag(player, 0, 0, "minecraft:planks");
    h.assertTrue(p.sides[0].entries[0] != null, "Valid tag edit rejected");
    player.getAbilities().mayBuild = false; menu.setTag(player, 0, 0, "minecraft:logs");
    h.assertTrue(p.sides[0].entries[0].tag().equals(ResourceLocation.parse("minecraft:planks")), "Adventure edit accepted");
    h.assertTrue(!menu.clickMenuButton(player, 32), "Adventure player enabled disposal"); h.succeed();
  }
}

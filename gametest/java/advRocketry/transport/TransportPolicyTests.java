package advRocketry.transport;

import advRocketry.processing.MachinePorts;
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

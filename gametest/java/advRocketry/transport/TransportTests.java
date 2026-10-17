package advRocketry.transport;

import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class TransportTests {
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void oversizedDiscoveryIsBoundedAndNeverSplits(GameTestHelper h) {
    var level = h.getLevel();
    BlockPos origin = new BlockPos(9200000, 100, 9200000);
    var state = TransportRegistry.ALL.get(TransportRegistry.Kind.ENERGY).get().defaultBlockState();
    for (int x = 0; x < 17; x++) for (int z = 0; z < 17; z++) {
      level.getChunkAt(origin.offset(x, 0, z));
      for (int y = 0; y < 15; y++) level.setBlock(origin.offset(x, y, z), state, 18);
    }
    try {
      var first = (TransportBlockEntity) level.getBlockEntity(origin);
      var far = (TransportBlockEntity) level.getBlockEntity(origin.offset(16, 14, 16));
      var a = TransportNetworks.network(first);
      var b = TransportNetworks.network(far);
      h.assertTrue(a.oversized && b.oversized, "Oversized component exposed a working subnet");
      h.assertTrue(a.members.size() <= TransportNetworks.MAX_NODES + 1
          && b.members.size() <= TransportNetworks.MAX_NODES + 1, "Discovery exceeded its member bound");
    } finally {
      for (int x = 0; x < 17; x++) for (int z = 0; z < 17; z++)
        for (int y = 0; y < 15; y++) level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 18);
      TransportNetworks.invalidate(level);
    }
    h.succeed();
  }
  @GameTest(template="empty",timeoutTicks=80)
  public static void capabilityInvalidationWakesPassiveEndpoint(GameTestHelper h) {
    var pipe=pipe(h,TransportRegistry.Kind.ENERGY,2);var sourcePos=pipe.getBlockPos().west();var targetPos=pipe.getBlockPos().east();
    h.getLevel().setBlock(sourcePos,Blocks.EMERALD_BLOCK.defaultBlockState(),3);h.getLevel().setBlock(targetPos,Blocks.EMERALD_BLOCK.defaultBlockState(),3);pipe.modes[Direction.WEST.ordinal()]=TransportBlockEntity.EXTRACT;pipe.changed();
    h.assertTrue(!pipe.getBlockState().getValue(TransportBlock.ENDPOINT),"No capabilities means no ticker");
    var source=new ChangingEnergySink();source.accepting=true;source.receiveEnergy(10000,false);var sink=new ChangingEnergySink();sink.accepting=true;
    ChangingEnergySink.SINKS.put(sourcePos,source);ChangingEnergySink.SINKS.put(targetPos,sink);h.getLevel().invalidateCapabilities(sourcePos);h.getLevel().invalidateCapabilities(targetPos);
    h.succeedWhen(()->{h.assertTrue(sink.getEnergyStored()>0 && pipe.getBlockState().getValue(TransportBlock.ENDPOINT),"Capability notification must wake a passive pipe without neighbor changes");ChangingEnergySink.SINKS.remove(sourcePos);ChangingEnergySink.SINKS.remove(targetPos);});
  }
  @GameTest(template="empty")
  public static void rejectedExecutionRetainsEnergyAndRetriesExactlyOnce(GameTestHelper h) {
    var pipe=pipe(h,TransportRegistry.Kind.ENERGY,2);var source=port(h,pipe,Direction.WEST,false);
    var target=pipe.getBlockPos().east();var sink=new ChangingEnergySink();ChangingEnergySink.SINKS.put(target,sink);h.getLevel().setBlock(target,Blocks.EMERALD_BLOCK.defaultBlockState(),3);pipe.changed();source.injectGeneratedEnergy(10000);
    h.getLevel().invalidateCapabilities(target);TransportNetworks.tick(pipe);
    h.assertTrue(source.energyStorage.getEnergyStored()==5000 && pipe.pendingEnergy==5000 && sink.getEnergyStored()==0,"Execute rejection retains the extracted energy: source="+source.energyStorage.getEnergyStored()+" buffer="+pipe.pendingEnergy+" sink="+sink.getEnergyStored());
    var tag=pipe.saveWithoutMetadata(h.getLevel().registryAccess());pipe.pendingEnergy=0;pipe.loadWithComponents(tag,h.getLevel().registryAccess());sink.accepting=true;TransportNetworks.invalidate(h.getLevel());TransportNetworks.tick(pipe);
    h.assertTrue(pipe.pendingEnergy==0 && sink.getEnergyStored()==5000 && source.energyStorage.getEnergyStored()==5000,"Saved remainder retries once and obeys input/output budgets");
    TransportNetworks.invalidate(h.getLevel());TransportNetworks.tick(pipe);h.assertTrue(sink.getEnergyStored()==5000,"Repeated invalidation cannot duplicate delivery");ChangingEnergySink.SINKS.remove(target);h.succeed();
  }
  @GameTest(template="empty")
  public static void chunkBoundaryScanNeverLoadsMissingChunk(GameTestHelper h) {
    var level=h.getLevel();BlockPos pos=new BlockPos(9100000,100,9100000);level.getChunkAt(pos);
    pos=new BlockPos(pos.getX()+15,pos.getY(),pos.getZ()+8);var unloaded=pos.east();
    h.assertTrue(!level.hasChunkAt(unloaded),"Fixture needs an unloaded adjacent chunk");
    for(BlockPos fixture:List.of(pos,pos.west(),pos.north()))level.setBlock(fixture,Blocks.AIR.defaultBlockState(),18);
    level.setBlock(pos,TransportRegistry.ALL.get(TransportRegistry.Kind.ENERGY).get().defaultBlockState(),18);var pipe=(TransportBlockEntity)level.getBlockEntity(pos);
    h.assertTrue(!level.hasChunkAt(unloaded),"Fixture placement must not load neighbors");
    level.setBlock(pos.west(),MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(),18);level.setBlock(pos.north(),MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(),18);
    ((PortBlockEntity)level.getBlockEntity(pos.west())).injectGeneratedEnergy(5000);pipe.modes[Direction.WEST.ordinal()]=TransportBlockEntity.EXTRACT;pipe.changed();
    pipe.refreshConnections();TransportNetworks.network(pipe);TransportNetworks.tick(pipe);pipe.redstone=1;h.assertTrue(!pipe.extracting(),"Unpowered boundary endpoint remains off");
    h.assertTrue(((PortBlockEntity)level.getBlockEntity(pos.north())).energyStorage.getEnergyStored()==5000,"Boundary fixture must actually transfer and activate its texture");
    h.assertTrue(!level.hasChunkAt(unloaded),"Capability checks and topology scans must not load adjacent chunks");level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
    level.setBlock(pos.west(),Blocks.AIR.defaultBlockState(),18);level.setBlock(pos.north(),Blocks.AIR.defaultBlockState(),18);h.succeed();
  }
  private static TransportBlockEntity pipe(GameTestHelper h, TransportRegistry.Kind kind,int x) {
    var pos=h.absolutePos(new BlockPos(x,2,3));h.getLevel().setBlock(pos,TransportRegistry.ALL.get(kind).get().defaultBlockState(),3);return (TransportBlockEntity)h.getLevel().getBlockEntity(pos);
  }
  private static PortBlockEntity port(GameTestHelper h,TransportBlockEntity pipe,Direction side,boolean input) {
    var block=switch(pipe.kind()) {case ENERGY->input?MachinePorts.BLOCK_ENERGY_INPUT_BLOCK:MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK;case FLUID->input?MachinePorts.BLOCK_FLUID_INPUT_BLOCK:MachinePorts.BLOCK_FLUID_OUTPUT_BLOCK;case ITEM->input?MachinePorts.BLOCK_ITEM_INPUT_BLOCK:MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK;};
    var pos=pipe.getBlockPos().relative(side);h.getLevel().setBlock(pos,block.get().defaultBlockState(),3);
    pipe.modes[side.ordinal()]=input?TransportBlockEntity.INSERT:TransportBlockEntity.EXTRACT;pipe.changed();return (PortBlockEntity)h.getLevel().getBlockEntity(pos);
  }
  @GameTest(template="empty")
  public static void energyBudgetPerConnectionAndSharedOutput(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.ENERGY,1);var middle=pipe(h,TransportRegistry.Kind.ENERGY,2);var right=pipe(h,TransportRegistry.Kind.ENERGY,3);
    var a=port(h,left,Direction.NORTH,false);var b=port(h,left,Direction.SOUTH,false);
    var x=port(h,right,Direction.NORTH,true);var y=port(h,right,Direction.SOUTH,true);
    a.injectGeneratedEnergy(30000);b.injectGeneratedEnergy(30000);
    TransportNetworks.tick(left);TransportNetworks.tick(right);TransportNetworks.invalidate(h.getLevel());TransportNetworks.tick(left);
    h.assertTrue(x.energyStorage.getEnergyStored()==5000 && y.energyStorage.getEnergyStored()==5000,"Each output must receive 5k, including after cache rebuild");
    h.assertTrue(a.energyStorage.getEnergyStored()==25000 && b.energyStorage.getEnergyStored()==25000,"Two inputs supply 10k without exceeding either input's 5k cap");
    middle.refreshConnections();h.assertTrue(!middle.getBlockState().getValue(TransportBlock.ENDPOINT),"Intermediate cable is passive");
    h.assertTrue(middle.getBlockState().getTicker(h.getLevel(),TransportRegistry.ENTITY.get())==null,"Intermediate cable must have no ticker");
    h.succeed();
  }
  @GameTest(template="empty")
  public static void oneEnergyOutputCannotReceiveTenThousand(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.ENERGY,1);var right=pipe(h,TransportRegistry.Kind.ENERGY,2);
    var a=port(h,left,Direction.NORTH,false);var b=port(h,left,Direction.SOUTH,false);var output=port(h,right,Direction.EAST,true);
    a.injectGeneratedEnergy(10000);b.injectGeneratedEnergy(10000);TransportNetworks.tick(left);
    h.assertTrue(output.energyStorage.getEnergyStored()==5000,"Single output capped at 5k");
    h.assertTrue(a.energyStorage.getEnergyStored()+b.energyStorage.getEnergyStored()==15000,"No excess extraction");h.succeed();
  }
  @GameTest(template="empty")
  public static void fluidRateLockAndNativeBucketActions(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.FLUID,1);var right=pipe(h,TransportRegistry.Kind.FLUID,2);
    var input=port(h,left,Direction.WEST,false);var output=port(h,right,Direction.EAST,true);
    input.myTank.fill(new FluidStack(Fluids.WATER,12000),FluidAction.EXECUTE);
    var player=h.makeMockPlayer(GameType.SURVIVAL);player.setShiftKeyDown(true);var bucket=new ItemStack(Items.WATER_BUCKET);player.setItemInHand(InteractionHand.MAIN_HAND,bucket);
    var hit=new BlockHitResult(Vec3.atCenterOf(left.getBlockPos()),Direction.UP,left.getBlockPos(),false);
    TransportInteraction.bucket(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,left.getBlockPos(),hit));
    h.assertTrue(left.lock==Fluids.WATER && right.lock==Fluids.WATER && bucket.is(Items.WATER_BUCKET),"Filled bucket locks whole network without consumption");
    TransportNetworks.tick(left);TransportNetworks.tick(right);
    h.assertTrue(output.myTank.getFluidAmount()==4000 && input.myTank.getFluidAmount()==8000,"Fluid network cap is 4000 mB/t");
    left.pendingFluid=new FluidStack(Fluids.WATER,700);right.pendingFluid=new FluidStack(Fluids.WATER,300);
    var empty=new ItemStack(Items.BUCKET);player.setItemInHand(InteractionHand.MAIN_HAND,empty);TransportInteraction.bucket(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,left.getBlockPos(),hit));
    h.assertTrue(left.pendingFluid.isEmpty() && right.pendingFluid.isEmpty(),"Empty bucket clears every pipe buffer");
    h.assertTrue(output.myTank.getFluidAmount()==4000 && input.myTank.getFluidAmount()==8000 && left.lock==Fluids.WATER,"Void preserves connected tanks and lock");h.succeed();
  }
  @GameTest(template="empty")
  public static void wrongFluidAndConflictingNetworksNeverMix(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.FLUID,1);var right=pipe(h,TransportRegistry.Kind.FLUID,2);var input=port(h,left,Direction.WEST,false);var output=port(h,right,Direction.EAST,true);
    input.myTank.fill(new FluidStack(Fluids.LAVA,8000),FluidAction.EXECUTE);TransportNetworks.lockFluid(left,new FluidStack(Fluids.WATER,1000),null);TransportNetworks.tick(left);
    h.assertTrue(output.myTank.isEmpty() && input.myTank.getFluidAmount()==8000,"Wrong fluid cannot be extracted");
    right.lock=Fluids.LAVA;TransportNetworks.invalidate(h.getLevel());h.assertTrue(TransportNetworks.network(left).conflicted(),"Merging incompatible locks stalls visibly");
    right.pendingFluid=new FluidStack(Fluids.LAVA,300);h.assertTrue(!TransportNetworks.lockFluid(left,new FluidStack(Fluids.WATER,1000),null),"Relocking cannot silently destroy an existing buffer");h.succeed();
  }
  @GameTest(template="empty",timeoutTicks=80)
  public static void itemRoundRobinDistributesIndividuals(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.ITEM,1);var right=pipe(h,TransportRegistry.Kind.ITEM,2);var input=port(h,left,Direction.WEST,false);var a=port(h,right,Direction.NORTH,true);var b=port(h,right,Direction.SOUTH,true);
    input.inventory.setStackInSlot(0,new ItemStack(Items.IRON_INGOT,32));TransportNetworks.roundRobin(left);
    h.succeedWhen(()->{h.assertTrue(a.inventory.getStackInSlot(0).getCount()==8 && b.inventory.getStackInSlot(0).getCount()==8,"One batch rotates evenly, not one whole stack per output");h.assertTrue(input.inventory.getStackInSlot(0).getCount()==16,"Exactly 16 source items consumed");});
  }
  @GameTest(template="empty",timeoutTicks=80)
  public static void filtersAndRedstoneGateExtraction(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.ITEM,1);var right=pipe(h,TransportRegistry.Kind.ITEM,2);var input=port(h,left,Direction.WEST,false);var output=port(h,right,Direction.EAST,true);
    input.inventory.setStackInSlot(0,new ItemStack(Items.GOLD_INGOT,32));input.inventory.setStackInSlot(1,new ItemStack(Items.IRON_INGOT,32));right.filter=new ItemStack(Items.IRON_INGOT);left.redstone=1;
    h.runAtTickTime(21,()->{h.assertTrue(output.inventory.getStackInSlot(0).isEmpty(),"Unpowered extraction stays off");left.redstone=0;});
    h.runAtTickTime(41,()->{h.assertTrue(output.inventory.getStackInSlot(0).is(Items.IRON_INGOT) && output.inventory.getStackInSlot(0).getCount()==16,"Filter selects matching source slot");h.assertTrue(input.inventory.getStackInSlot(0).getCount()==32,"Rejected item remains intact");h.succeed();});
  }
  @GameTest(template="empty")
  public static void sideDisconnectAndRejoinRebuildTopology(GameTestHelper h) {
    var left=pipe(h,TransportRegistry.Kind.ENERGY,1);var middle=pipe(h,TransportRegistry.Kind.ENERGY,2);var right=pipe(h,TransportRegistry.Kind.ENERGY,3);
    h.assertTrue(TransportNetworks.network(left).nodes.size()==3,"Joined topology");middle.modes[Direction.EAST.ordinal()]=TransportBlockEntity.DISABLED;middle.changed();
    h.assertTrue(TransportNetworks.network(left).nodes.size()==2 && TransportNetworks.network(right).nodes.size()==1,"Disabled side separates components");middle.modes[Direction.EAST.ordinal()]=TransportBlockEntity.INSERT;middle.changed();
    h.assertTrue(TransportNetworks.network(right).nodes.size()==3,"Reenabled side restores network");h.getLevel().setBlock(middle.getBlockPos(),Blocks.AIR.defaultBlockState(),3);
    h.assertTrue(TransportNetworks.network(left).nodes.size()==1,"Broken bridge is removed from cache");h.succeed();
  }
  @GameTest(template="empty")
  public static void savedBuffersSettingsAndGhostFilterRemainIntact(GameTestHelper h) {
    var pipe=pipe(h,TransportRegistry.Kind.ITEM,2);pipe.filter=new ItemStack(Items.DIAMOND);pipe.pendingItem=new ItemStack(Items.IRON_INGOT,7);pipe.roundRobin=true;pipe.cursor=3;pipe.redstone=2;pipe.modes[0]=1;
    var tag=pipe.saveWithoutMetadata(h.getLevel().registryAccess());var restored=new TransportBlockEntity(pipe.getBlockPos(),pipe.getBlockState());restored.loadWithComponents(tag,h.getLevel().registryAccess());
    h.assertTrue(restored.filter.is(Items.DIAMOND) && restored.pendingItem.getCount()==7 && restored.roundRobin && restored.cursor==3 && restored.redstone==2 && restored.modes[0]==1,"Configuration and retained payload survive NBT");
    var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(pipe.getBlockPos()));var menu=new TransportMenu(0,player.getInventory(),pipe);menu.setCarried(new ItemStack(Items.GOLD_INGOT,12));menu.clicked(0,0,ClickType.PICKUP,player);
    h.assertTrue(pipe.filter.is(Items.GOLD_INGOT) && pipe.filter.getCount()==1 && menu.getCarried().getCount()==12,"Ghost filter copies without consuming or duplicating cursor stack");
    menu.clicked(0,0,ClickType.QUICK_MOVE,player);h.assertTrue(menu.getCarried().getCount()==12,"Shift click cannot extract ghost items");player.setPos(100000,200,100000);h.assertTrue(!menu.clickMenuButton(player,0),"Remote menu action rejected");h.succeed();
  }
  @GameTest(template="empty")
  public static void savedFluidBufferAndLockDrainWithoutNewExtraction(GameTestHelper h) {
    var pipe=pipe(h,TransportRegistry.Kind.FLUID,2);var output=port(h,pipe,Direction.EAST,true);pipe.pendingFluid=new FluidStack(Fluids.WATER,1234);pipe.lock=Fluids.WATER;
    var tag=pipe.saveWithoutMetadata(h.getLevel().registryAccess());pipe.pendingFluid=FluidStack.EMPTY;pipe.lock=Fluids.EMPTY;pipe.loadWithComponents(tag,h.getLevel().registryAccess());TransportNetworks.invalidate(h.getLevel());TransportNetworks.tick(pipe);
    h.assertTrue(pipe.lock==Fluids.WATER && pipe.pendingFluid.isEmpty() && output.myTank.getFluidAmount()==1234,"Saved endpoint buffer drains exactly once");TransportNetworks.tick(pipe);h.assertTrue(output.myTank.getFluidAmount()==1234,"No retry duplication");h.succeed();
  }
  @GameTest(template="empty",timeoutTicks=80)
  public static void activeAnimationClearsAfterTransferStops(GameTestHelper h) {
    var pipe=pipe(h,TransportRegistry.Kind.ENERGY,2);var source=port(h,pipe,Direction.WEST,false);port(h,pipe,Direction.EAST,true);source.injectGeneratedEnergy(5000);TransportNetworks.tick(pipe);
    h.assertTrue(pipe.getBlockState().getValue(TransportBlock.ACTIVE),"Successful transfer activates texture");h.runAtTickTime(12,()->{h.assertTrue(!pipe.getBlockState().getValue(TransportBlock.ACTIVE),"Stopped transfer restores idle texture");h.succeed();});
  }
}

package advRocketry.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class GasFlowTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void stationaryEntitiesReceiveLavaContactButFireImmuneMobsDoNot(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(5, 6, 5));
    var lava = ProcessingFluids.DEFINITIONS.get("enriched_lava");
    level.setBlock(pos, lava.block.get().defaultBlockState(), 3);
    level.setBlock(pos.east(4), lava.block.get().defaultBlockState(), 3);
    var cow = EntityType.COW.create(level);
    cow.setNoAi(true);
    cow.setNoGravity(true);
    cow.moveTo(pos.getX() + .5, pos.getY() + .1, pos.getZ() + .5, 0, 0);
    float health = cow.getHealth();
    level.addFreshEntity(cow);
    var magma = EntityType.MAGMA_CUBE.create(level);
    magma.setSize(2, true);
    magma.setNoAi(true);
    magma.setNoGravity(true);
    magma.moveTo(pos.getX() + 4.5, pos.getY() + .1, pos.getZ() + .5, 0, 0);
    float immuneHealth = magma.getHealth();
    level.addFreshEntity(magma);
    helper.runAfterDelay(
        20,
        () -> {
          helper.assertTrue(
              cow.getHealth() < health && cow.isOnFire(),
              "Stationary cow escaped enriched-lava damage and ignition");
          helper.assertTrue(
              magma.getHealth() == immuneHealth, "Enriched lava ignored native fire immunity");
          helper.assertTrue(
              ProcessingFluids.isInLava(magma),
              "Atmosphere exemption did not recognize enriched lava");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 200)
  public static void gasRisesAndDrainsAfterSourceRemoval(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(5, 6, 5));
    var oxygen = ProcessingFluids.DEFINITIONS.get("oxygen");
    helper.assertTrue(
        !oxygen.type.get().canPushEntity(EntityType.COW.create(level)),
        "Original rising gas should not accelerate immersed entities");
    for (int y = 0; y < 3; y++)
      for (Direction side : Direction.Plane.HORIZONTAL)
        level.setBlock(pos.above(y).relative(side), Blocks.STONE.defaultBlockState(), 3);
    level.setBlock(pos.above(3), Blocks.STONE.defaultBlockState(), 3);
    level.setBlock(pos, oxygen.block.get().defaultBlockState(), 3);
    helper.runAfterDelay(
        35,
        () -> {
          helper.assertTrue(
              oxygen.source.get().isSame(level.getFluidState(pos.above(2)).getType()),
              "Negative-density gas did not rise");
          helper.assertTrue(level.getFluidState(pos.below()).isEmpty(), "Gas flowed downward");
          helper.assertTrue(
              !level.getFluidState(pos.above()).isSource(),
              "Rising gas created a renewable source");
          helper.assertTrue(
              level.getFluidState(pos.above()).getFlow(level, pos.above()).y > 0,
              "Gas pushes entities down");
          level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        });
    helper.runAfterDelay(
        140,
        () -> {
          for (int y = 0; y < 3; y++)
            helper.assertTrue(
                level.getFluidState(pos.above(y)).isEmpty(), "Orphaned gas did not decay");
          helper.succeed();
        });
  }

  @GameTest(template = "empty")
  public static void gasSelectsNearestCeilingEscape(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(7, 6, 7));
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 3; z++)
        level.setBlock(pos.offset(x, 1, z), Blocks.STONE.defaultBlockState(), 3);
    level.removeBlock(pos.east().above(), false);
    var gas = ProcessingFluids.DEFINITIONS.get("hydrogen");
    level.setBlock(pos, gas.block.get().defaultBlockState(), 3);
    gas.source.get().tick(level, pos, level.getFluidState(pos));
    helper.assertTrue(
        gas.source.get().isSame(level.getFluidState(pos.east()).getType()),
        "Gas ignored its nearest ceiling opening");
    helper.assertTrue(
        level.getFluidState(pos.west()).isEmpty()
            && level.getFluidState(pos.north()).isEmpty()
            && level.getFluidState(pos.south()).isEmpty(),
        "Gas ignored ceiling path cost");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void liquidFuelsStillFlowDown(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(new BlockPos(5, 6, 5));
    for (String name : new String[] {"rocket_fuel", "enriched_lava"}) {
      var fluid = ProcessingFluids.DEFINITIONS.get(name);
      helper.assertTrue(
          fluid.source.get().getTickDelay(level) == (name.equals("rocket_fuel") ? 7 : 30),
          "Liquid flow ignored original viscosity-based update timing");
      level.setBlock(pos, fluid.block.get().defaultBlockState(), 3);
      fluid.source.get().tick(level, pos, level.getFluidState(pos));
      helper.assertTrue(
          fluid.source.get().isSame(level.getFluidState(pos.below()).getType())
              && level.getFluidState(pos.above()).isEmpty(),
          "Liquid changed its downward flow: " + name);
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
      level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
    }
    helper.succeed();
  }
}

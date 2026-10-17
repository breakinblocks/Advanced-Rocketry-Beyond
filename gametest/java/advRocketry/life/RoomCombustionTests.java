package advRocketry.life;

import advRocketry.ModTags;
import advRocketry.TestTags;
import advRocketry.processing.ProcessingFluids;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class RoomCombustionTests {
  @SuppressWarnings("unchecked")
  @GameTest(template = "empty")
  public static void lossOfRoomAirExtinguishesExistingTorches(GameTestHelper helper)
      throws Exception {
    var server = helper.getLevel().getServer();
    var level = PlanetRuntime.create(server, GalaxyData.get(server).planets.get(1));
    var start = helper.absolutePos(new BlockPos(2, 2, 2)).atY(150);
    for (var pos : BlockPos.betweenClosed(start, start.offset(4, 4, 4))) {
      level.getChunkAt(pos);
      boolean wall =
          pos.getX() == start.getX()
              || pos.getX() == start.getX() + 4
              || pos.getY() == start.getY()
              || pos.getY() == start.getY() + 4
              || pos.getZ() == start.getZ()
              || pos.getZ() == start.getZ() + 4;
      level.setBlock(pos, (wall ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 2);
    }
    BlockPos firstPos = start.offset(0, 2, 2), secondPos = start.offset(4, 2, 2);
    level.setBlock(firstPos, LifeSupportRegistry.VENT.get().defaultBlockState(), 2);
    level.setBlock(secondPos, LifeSupportRegistry.VENT.get().defaultBlockState(), 2);
    var first = (OxygenBlockEntity) level.getBlockEntity(firstPos);
    var second = (OxygenBlockEntity) level.getBlockEntity(secondPos);
    for (var vent : new OxygenBlockEntity[] {first, second}) {
      vent.setRoom(SealedRooms.scan(level, start.offset(2, 2, 2)));
      vent.energy.receiveEnergy(1000, false);
      vent.tank.fill(
          new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 1000),
          IFluidHandler.FluidAction.EXECUTE);
      vent.tick();
      helper.assertTrue(vent.active, "Test vent did not pressurize room");
    }
    BlockPos floor = start.offset(2, 1, 2),
        wall = start.offset(1, 2, 2),
        thermite = start.offset(3, 1, 2),
        configured = start.offset(2, 1, 3);
    level.setBlock(floor, Blocks.TORCH.defaultBlockState(), 2);
    level.setBlock(
        wall,
        Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST),
        2);
    level.setBlock(thermite, LightingRegistry.THERMITE_TORCH.get().defaultBlockState(), 2);
    level.setBlock(configured, Blocks.SOUL_TORCH.defaultBlockState(), 2);
    try (var tag =
        TestTags.add(
            BuiltInRegistries.BLOCK, ModTags.TORCHES, Blocks.SOUL_TORCH.builtInRegistryHolder())) {
      first.energy.setEnergy(0);
      first.tick();
      helper.assertTrue(
          !first.active && level.getBlockState(floor).is(Blocks.TORCH),
          "Remaining active vent did not protect flame");
      level.removeBlock(secondPos, false);
      helper.assertTrue(
          level.getBlockState(floor).is(LightingRegistry.UNLIT_TORCH.get()),
          "Removing last vent left floor torch burning");
      helper.assertTrue(
          level.getBlockState(wall).is(LightingRegistry.UNLIT_WALL_TORCH.get())
              && level.getBlockState(wall).getValue(WallTorchBlock.FACING) == Direction.EAST,
          "Wall torch lost extinction or facing");
      helper.assertTrue(
          level.getBlockState(thermite).is(LightingRegistry.THERMITE_TORCH.get()),
          "Vacuum-safe torch extinguished");
      helper.assertTrue(
          level.getBlockState(configured).isAir(),
          "Configured torch remained placed after pressure loss");
    } finally {
      level.removeBlock(firstPos, false);
      level.removeBlock(secondPos, false);
    }
    helper.succeedWhen(
        () -> {
          int drops =
              level
                  .getEntitiesOfClass(
                      ItemEntity.class,
                      new AABB(configured).inflate(.5),
                      item -> item.getItem().is(Blocks.SOUL_TORCH.asItem()))
                  .stream()
                  .mapToInt(item -> item.getItem().getCount())
                  .sum();
          helper.assertTrue(
              drops == 1,
              "Expected one configured torch drop after entity-chunk activation, got " + drops);
        });
  }
}

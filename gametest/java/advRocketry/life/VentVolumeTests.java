package advRocketry.life;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class VentVolumeTests {
  @GameTest(template = "empty")
  public static void originalVolumeAndRadiusAreIndependent(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos base = helper.absolutePos(new BlockPos(2, 2, 2));
    for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(10, 2, 2)))
      level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 3);
    for (int x = 1; x < 10; x++)
      level.setBlock(base.offset(x, 1, 1), Blocks.AIR.defaultBlockState(), 3);
    BlockPos start = base.offset(1, 1, 1);
    helper.assertTrue(
        SealedRooms.scan(level, start, start, 3, true).size() == 9,
        "Volume mode rejected a sealed narrow room extending beyond radius");
    helper.assertTrue(
        SealedRooms.scan(level, start, start, 3, false).isEmpty(),
        "Radius mode accepted air outside its sphere");
    helper.assertTrue(
        SealedRooms.scan(level, start, start, 1, true).isEmpty(),
        "Volume cap ignored sphere volume");
    helper.assertTrue(
        SealedRooms.scan(level, start, start, 2, true).size() == 9,
        "Volume mode used cube volume instead of sphere volume");
    level.setBlock(base.offset(10, 1, 1), Blocks.AIR.defaultBlockState(), 3);
    helper.assertTrue(
        SealedRooms.scan(level, start, start, 3, true).isEmpty(),
        "Open tunnel remained breathable in volume mode");
    helper.succeed();
  }
}

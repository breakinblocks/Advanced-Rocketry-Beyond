package advRocketry.space;

import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class AlienTreeTests {
  @GameTest(template = "empty", timeoutTicks = 400)
  public static void alienTreeBranchesReachEveryQuadrantAndCanopySurvivesTranslation(
      GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos first = helper.absolutePos(new BlockPos(0, 0, 0)).atY(230);
    BlockPos second = first.east(48);
    for (BlockPos base : new BlockPos[] {first, second}) {
      for (int x = -1; x <= 2; x++)
        for (int z = -1; z <= 2; z++)
          level.setBlock(base.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 2);
      helper.assertTrue(
          AlienTree.grow(level, base, RandomSource.create(42)),
          "Alien tree refused empty sky above soil");
      for (int dx : new int[] {-1, 1})
        for (int dz : new int[] {-1, 1}) {
          boolean branch = false;
          for (int x = 3; x <= 6; x++)
            for (int z = 3; z <= 6; z++)
              for (int y = 0; y < 32; y++)
                branch |=
                    level
                        .getBlockState(base.offset(dx * x, y, dz * z))
                        .is(ProcessingRegistry.part("alien_log"));
          helper.assertTrue(
              branch, "Alien trunk blocked its own branch in quadrant " + dx + "," + dz);
        }
    }
    int foliage = 0;
    for (int x = -14; x <= 14; x++)
      for (int z = -14; z <= 14; z++)
        for (int y = 0; y < 35; y++) {
          var a = level.getBlockState(first.offset(x, y, z));
          var b = level.getBlockState(second.offset(x, y, z));
          helper.assertTrue(
              a.equals(b),
              "Tree shape depends on absolute coordinates at " + x + "," + y + "," + z);
          if (a.is(ProcessingRegistry.part("alien_leaves"))) foliage++;
        }
    helper.assertTrue(foliage > 1500, "Original radial leaf pods or crown are missing");
    helper.succeed();
  }
}

package advRocketry.space;

import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.SolarArrayLogic;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class SolarPanelTests {
  @GameTest(template = "empty")
  public static void thinPanelsHoldWaterAndGenerateUnderWater(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2)).atY(240);
    for (var block :
        new Block[] {MachinePorts.SOLAR_PANEL.get(), OrbitalRegistry.SOLAR_ARRAY_PANEL.get()}) {
      var state = block.defaultBlockState();
      level.setBlock(pos, state, 2);
      helper.assertTrue(
          state.getShape(level, pos).max(Direction.Axis.Y) == .125, "Panel must be 1/8 block tall");
      helper.assertTrue(
          ((SimpleWaterloggedBlock) block)
              .placeLiquid(level, pos, state, Fluids.WATER.getSource(false)),
          "Panel must accept water");
      helper.assertTrue(
          level.getFluidState(pos).getType() == Fluids.WATER, "Waterlogging lost source water");
      ((SimpleWaterloggedBlock) block).pickupBlock(null, level, pos, level.getBlockState(pos));
      helper.assertTrue(
          !level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED),
          "Bucket did not remove water");
    }
    level.setBlock(
        pos,
        MachinePorts.SOLAR_PANEL
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, true),
        2);
    for (int y = 1; y <= 3; y++) level.setBlock(pos.above(y), Blocks.WATER.defaultBlockState(), 2);
    helper.assertTrue(
        SolarPower.canSeeSkyThroughWater(level, pos.above()),
        "Water above sea level obstructed sunlight");
    PortBlockEntity panel = (PortBlockEntity) level.getBlockEntity(pos);
    panel.tick();
    helper.assertTrue(
        panel.energyStorage.getEnergyStored() > 0, "Submerged panel did not generate energy");
    level.setBlock(pos.above(4), Blocks.STONE.defaultBlockState(), 2);
    int energy = panel.energyStorage.getEnergyStored();
    panel.tick();
    helper.assertTrue(
        !SolarPower.canSeeSkyThroughWater(level, pos.above())
            && panel.energyStorage.getEnergyStored() == energy,
        "Solid roof must block generation");
    for (int y = 0; y <= 4; y++) level.removeBlock(pos.above(y), false);
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void arrayCountsOnlyExposedPanels(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(6, 2, 6)).atY(245);
    for (int z = 0; z <= 21; z++) level.getChunkAt(pos.south(z));
    level.setBlock(pos, OrbitalRegistry.SOLAR_ARRAY.get().defaultBlockState(), 2);
    level.setBlock(pos.east(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(pos.west(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(
        pos.south(),
        OrbitalRegistry.SOLAR_ARRAY_PANEL
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, true),
        2);
    level.setBlock(pos.south().above(), Blocks.WATER.defaultBlockState(), 2);
    level.setBlock(pos.south(2), OrbitalRegistry.SOLAR_ARRAY_PANEL.get().defaultBlockState(), 2);
    level.setBlock(pos.south(2).above(2), Blocks.STONE.defaultBlockState(), 2);
    level.setBlock(pos.south(3), Blocks.WATER.defaultBlockState(), 2);
    var array = (OrbitalBlockEntity) level.getBlockEntity(pos);
    SolarArrayLogic.tick(array);
    helper.assertTrue(
        array.powerMadeLastTick == SolarPower.perPanel(level, pos),
        "Shaded panel counted, or submerged panel excluded");
    for (int z = 0; z <= 2; z++)
      for (int y = 0; y <= 2; y++) level.removeBlock(pos.south(z).above(y), false);
    level.removeBlock(pos.south(3), false);
    level.removeBlock(pos.east(), false);
    level.removeBlock(pos.west(), false);
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void solarUsesPlanetClock(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var planet = galaxy.planets.get(1);
    long rotation = planet.rotationPeriod, offset = planet.dayTimeOffset;
    long time = server.overworld().getDayTime();
    try {
      planet.rotationPeriod = 48000;
      planet.dayTimeOffset = 0;
      var level = PlanetRuntime.create(server, planet);
      server.overworld().setDayTime(12000);
      helper.assertTrue(
          SolarPower.daylight(level, BlockPos.ZERO), "48,000-tick planet should be at noon");
      server.overworld().setDayTime(36000);
      helper.assertTrue(
          !SolarPower.daylight(level, BlockPos.ZERO), "48,000-tick planet should be at midnight");
      planet.rotationPeriod = 12000;
      server.overworld().setDayTime(3000);
      helper.assertTrue(
          SolarPower.daylight(level, BlockPos.ZERO), "12,000-tick planet should be at noon");
      server.overworld().setDayTime(9000);
      helper.assertTrue(
          !SolarPower.daylight(level, BlockPos.ZERO), "12,000-tick planet should be at midnight");
    } finally {
      planet.rotationPeriod = rotation;
      planet.dayTimeOffset = offset;
      server.overworld().setDayTime(time);
    }
    helper.succeed();
  }
}

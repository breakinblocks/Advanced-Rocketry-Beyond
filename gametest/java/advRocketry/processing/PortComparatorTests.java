package advRocketry.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class PortComparatorTests {
  @GameTest(template = "empty", timeoutTicks = 60)
  public static void nativeComparatorsTrackOriginalHatchAndEnergyFullness(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos itemPos = helper.absolutePos(new BlockPos(3, 3, 3));
    BlockPos fluidPos = itemPos.south(3);
    BlockPos energyPos = itemPos.south(6);
    level.setBlock(itemPos, MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 3);
    level.setBlock(fluidPos, MachinePorts.BLOCK_FLUID_INPUT_BLOCK.get().defaultBlockState(), 3);
    level.setBlock(energyPos, MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 3);
    var item = (PortBlockEntity) level.getBlockEntity(itemPos);
    var fluid = (PortBlockEntity) level.getBlockEntity(fluidPos);
    var energy = (PortBlockEntity) level.getBlockEntity(energyPos);
    helper.assertTrue(
        energy.energyStorage.getMaxEnergyStored() == 100000
            && energy.energyStorage.canReceive()
            && !energy.energyStorage.canExtract(),
        "Input lost the original tier-one capacity or advertised direction");
    for (BlockPos pos : new BlockPos[] {itemPos, fluidPos, energyPos}) {
      helper.assertTrue(
          level.getBlockState(pos).hasAnalogOutputSignal(), "Port lacks comparator capability");
      level.setBlock(pos.west().below(), Blocks.STONE.defaultBlockState(), 3);
      level.setBlock(
          pos.west(),
          Blocks.COMPARATOR
              .defaultBlockState()
              .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
          3);
    }
    item.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
    helper.assertTrue(
        level.getBlockState(itemPos).getAnalogOutputSignal(level, itemPos) == 1,
        "Single item should produce signal one");
    item.inventory.setStackInSlot(0, new ItemStack(Items.IRON_PICKAXE));
    helper.assertTrue(
        level.getBlockState(itemPos).getAnalogOutputSignal(level, itemPos) == 4,
        "Unstackable item fullness ignored its stack limit");
    for (int slot = 0; slot < 4; slot++)
      item.inventory.setStackInSlot(slot, new ItemStack(Items.IRON_INGOT, 32));
    fluid.myTank.fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE);
    energy.energyStorage.receiveEnergy(50000, false);
    helper.runAfterDelay(
        5,
        () -> {
          helper.assertTrue(
              ((ComparatorBlockEntity) level.getBlockEntity(itemPos.west())).getOutputSignal() == 8,
              "Real comparator did not read half-full item hatch");
          helper.assertTrue(
              ((ComparatorBlockEntity) level.getBlockEntity(fluidPos.west())).getOutputSignal()
                  == 7,
              "Real comparator did not read half-full fluid hatch");
          helper.assertTrue(
              ((ComparatorBlockEntity) level.getBlockEntity(energyPos.west())).getOutputSignal()
                  == 7,
              "Real comparator did not read half-full energy input");
          for (int slot = 0; slot < 4; slot++) item.inventory.setStackInSlot(slot, ItemStack.EMPTY);
          fluid.myTank.drain(8000, IFluidHandler.FluidAction.EXECUTE);
          energy.energyStorage.consume(50000, false);
          helper.runAfterDelay(
              5,
              () -> {
                for (BlockPos pos : new BlockPos[] {itemPos, fluidPos, energyPos})
                  helper.assertTrue(
                      ((ComparatorBlockEntity) level.getBlockEntity(pos.west())).getOutputSignal()
                          == 0,
                      "Empty port did not notify its comparator");
                level.setBlock(
                    energyPos, MachinePorts.CREATIVE_ENERGY_INPUT.get().defaultBlockState(), 3);
                helper.assertTrue(
                    level.getBlockState(energyPos).getAnalogOutputSignal(level, energyPos) == 15,
                    "Creative energy overflowed comparator arithmetic");
                level.setBlock(
                    energyPos, MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 3);
                var output = (PortBlockEntity) level.getBlockEntity(energyPos);
                helper.assertTrue(
                    output.energyStorage.getMaxEnergyStored() == 100000
                        && output.energyStorage.canExtract()
                        && !output.energyStorage.canReceive(),
                    "Output lost the original tier-one capacity or advertised direction");
                output.energyStorage.deserializeNBT(level.registryAccess(), IntTag.valueOf(100000));
                helper.assertTrue(
                    level.getBlockState(energyPos).getAnalogOutputSignal(level, energyPos) == 15,
                    "Full energy output did not produce fifteen");
                var solar =
                    new PortBlockEntity(
                        energyPos.above(), MachinePorts.SOLAR_PANEL.get().defaultBlockState());
                helper.assertTrue(
                    solar.energyStorage.getMaxEnergyStored() == 10000
                        && solar.energyStorage.canExtract()
                        && !solar.energyStorage.canReceive(),
                    "Solar panel lost its original ten-thousand FE buffer");
                helper.succeed();
              });
        });
  }
}

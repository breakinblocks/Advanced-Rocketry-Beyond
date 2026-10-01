// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Five-layer casing, lenses and interchangeable service rim from the original observatory. */
public final class ObservatoryStructure {
  private ObservatoryStructure() {}

  public static PlacedMultiblock structure(OrbitalBlockEntity controller) {
    return PlacedMultiblock.of(controller, "observatory");
  }

  public static boolean complete(OrbitalBlockEntity controller) {
    return controller.observatory() && PlacedMultiblock.complete(controller, "observatory");
  }

  public static int range(OrbitalBlockEntity controller) {
    if (!complete(controller)) return 0;
    PlacedMultiblock structure = structure(controller);
    int range = 10;
    for (BlockPos lens : structure.positions('n'))
      if (controller.getLevel().getBlockState(lens).is(ProcessingRegistry.part("lens_block")))
        range += 5;
    BlockState state = controller.getLevel().getBlockState(structure.first('M'));
    range +=
        state.is(MachinePorts.ELITE_MOTOR.get())
            ? 175
            : state.is(MachinePorts.ENHANCED_MOTOR.get())
                ? 100
                : state.is(MachinePorts.ADVANCED_MOTOR.get()) ? 50 : 25;
    return range;
  }

  public static BlockPos skyPosition(OrbitalBlockEntity controller) {
    return structure(controller).first('n').above();
  }
}

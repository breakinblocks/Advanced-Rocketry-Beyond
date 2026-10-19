package advRocketry.processing;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Tinted crystal glass joins without drawing internal faces between like blocks. */
public final class CrystalBlock extends MachinePartBlock {
  public CrystalBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
    return adjacent.is(this) || super.skipRendering(state, adjacent, side);
  }
}

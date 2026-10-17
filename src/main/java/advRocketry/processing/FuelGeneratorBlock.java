package advRocketry.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class FuelGeneratorBlock extends GeneratorBlock implements EntityBlock {
  public FuelGeneratorBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new FuelGeneratorBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && type == MachinePorts.FUEL_GENERATOR_ENTITY.get()
        ? (world, pos, current, entity) -> ((FuelGeneratorBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (player.isSpectator() || !player.mayBuild())
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (FluidUtil.getFluidHandler(stack).isPresent()) {
      if (!level.isClientSide
          && level.getBlockEntity(pos) instanceof FuelGeneratorBlockEntity generator)
        FluidUtil.interactWithFluidHandler(player, hand, generator.tank);
      return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer server
        && level.getBlockEntity(pos) instanceof FuelGeneratorBlockEntity generator)
      server.openMenu(generator, buffer -> buffer.writeBlockPos(pos));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof FuelGeneratorBlockEntity generator
        ? generator.energyStored() * 15 / FuelGeneratorBlockEntity.CAPACITY
        : 0;
  }
}

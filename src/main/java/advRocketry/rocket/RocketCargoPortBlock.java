// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;

/** Original rocket item/fluid loader and unloader hatch roles. */
public final class RocketCargoPortBlock extends RocketInfrastructureBlock {
  public enum Kind {
    ITEM_LOAD,
    ITEM_UNLOAD,
    FLUID_LOAD,
    FLUID_UNLOAD,
    GUIDANCE
  }

  public final Kind kind;

  public RocketCargoPortBlock(Kind kind, Properties properties) {
    super(properties, RocketRegistry.CARGO_PORT_ENTITY);
    this.kind = kind;
  }

  public boolean fluid() {
    return kind == Kind.FLUID_LOAD || kind == Kind.FLUID_UNLOAD;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(POWERED);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new RocketCargoPortBlockEntity(pos, state);
  }

  @Override
  protected IItemHandler contents(BlockEntity entity) {
    return entity instanceof RocketCargoPortBlockEntity cargo ? cargo.inventory : null;
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
    return fluid()
            && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())
        ? ItemInteractionResult.SUCCESS
        : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof RocketCargoPortBlockEntity entity)
      serverPlayer.openMenu(entity, buffer -> RocketCargoPortMenu.writeOpeningData(buffer, entity));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected boolean isSignalSource(BlockState state) {
    return true;
  }

  @Override
  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    return state.getValue(POWERED)
            && level.getBlockEntity(pos) instanceof RocketCargoPortBlockEntity entity
            && (kind == Kind.GUIDANCE || entity.redstone.outputsTo(side))
        ? 15
        : 0;
  }
}

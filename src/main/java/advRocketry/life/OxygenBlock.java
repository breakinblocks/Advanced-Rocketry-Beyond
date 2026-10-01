// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class OxygenBlock extends Block implements EntityBlock {
  public final boolean charger;

  public OxygenBlock(boolean charger, Properties properties) {
    super(properties);
    this.charger = charger;
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
    if (!state.is(replacement.getBlock())
        && level.getBlockEntity(pos) instanceof OxygenBlockEntity entity) entity.decommission();
    super.onRemove(state, level, pos, replacement, moving);
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return charger ? Block.box(0, 0, 0, 16, 8, 16) : super.getShape(state, level, pos, context);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof OxygenBlockEntity entity)
      serverPlayer.openMenu(entity, buffer -> OxygenMenu.writeOpeningData(buffer, entity));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new OxygenBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    if (type != LifeSupportRegistry.OXYGEN_ENTITY.get()) return null;
    return level.isClientSide
        ? (world, pos, blockState, entity) -> ((OxygenBlockEntity) entity).tickClient()
        : (world, pos, blockState, entity) -> ((OxygenBlockEntity) entity).tick();
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
    return FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())
        ? ItemInteractionResult.SUCCESS
        : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }
}

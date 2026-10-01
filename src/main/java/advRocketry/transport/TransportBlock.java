// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TransportBlock extends Block implements EntityBlock {
  public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
  public static final BooleanProperty ENDPOINT = BooleanProperty.create("endpoint");
  public static final BooleanProperty[] SIDES = {
    BooleanProperty.create("down"),
    BooleanProperty.create("up"),
    BooleanProperty.create("north"),
    BooleanProperty.create("south"),
    BooleanProperty.create("west"),
    BooleanProperty.create("east")
  };
  private static final VoxelShape CORE = box(5, 5, 5, 11, 11, 11);
  private static final VoxelShape[] ARMS = {
    box(5, 0, 5, 11, 5, 11),
    box(5, 11, 5, 11, 16, 11),
    box(5, 5, 0, 11, 11, 5),
    box(5, 5, 11, 11, 11, 16),
    box(0, 5, 5, 5, 11, 11),
    box(11, 5, 5, 16, 11, 11)
  };
  public final TransportRegistry.Kind kind;

  public TransportBlock(TransportRegistry.Kind kind, Properties properties) {
    super(properties);
    this.kind = kind;
    BlockState state = stateDefinition.any().setValue(ACTIVE, false).setValue(ENDPOINT, false);
    for (var side : SIDES) state = state.setValue(side, false);
    registerDefaultState(state);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(ACTIVE, ENDPOINT);
    builder.add(SIDES);
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    VoxelShape shape = CORE;
    for (int i = 0; i < 6; i++) if (state.getValue(SIDES[i])) shape = Shapes.or(shape, ARMS[i]);
    return shape;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TransportBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide && state.getValue(ENDPOINT) && type == TransportRegistry.ENTITY.get()
        ? (world, pos, current, be) -> TransportNetworks.tick((TransportBlockEntity) be)
        : null;
  }

  @Override
  protected void onPlace(
      BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
    super.onPlace(state, level, pos, old, moved);
    if (!state.is(old.getBlock()) && !level.isClientSide) level.scheduleTick(pos, this, 1);
  }

  @Override
  protected void neighborChanged(
      BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moved) {
    if (!level.isClientSide) {
      TransportNetworks.invalidate(level, pos);
      level.scheduleTick(pos, this, 1);
    }
  }

  @Override
  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (level.getBlockEntity(pos) instanceof TransportBlockEntity pipe) {
      pipe.refreshConnections();
      if (pipe.activeUntil <= level.getGameTime() && level.getBlockState(pos).getValue(ACTIVE))
        level.setBlock(
            pos,
            level.getBlockState(pos).setValue(ACTIVE, false),
            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
      else if (pipe.activeUntil > level.getGameTime())
        level.scheduleTick(pos, this, (int) (pipe.activeUntil - level.getGameTime()));
    }
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer server
        && level.getBlockEntity(pos) instanceof TransportBlockEntity pipe)
      server.openMenu(
          pipe,
          buf -> {
            buf.writeBlockPos(pos);
            buf.writeEnum(kind);
          });
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
    if (!state.is(next.getBlock())) {
      if (!level.isClientSide
          && level.getBlockEntity(pos) instanceof TransportBlockEntity pipe
          && !pipe.pendingItem.isEmpty())
        Containers.dropItemStack(
            level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, pipe.pendingItem);
      TransportNetworks.invalidate(level, pos);
    }
    super.onRemove(state, level, pos, next, moved);
  }
}

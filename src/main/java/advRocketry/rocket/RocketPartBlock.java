// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Native rocket components with the original engine thrust and tank capacities. */
public final class RocketPartBlock extends Block implements EntityBlock {
  public enum Kind {
    ENGINE,
    TANK,
    GUIDANCE,
    BUILDER,
    DEPLOYABLE_BUILDER,
    STATION_BUILDER,
    PAD,
    SEAT,
    DRILL,
    CORE
  }

  public enum Fuel {
    MONOPROPELLANT,
    BIPROPELLANT,
    OXIDIZER,
    NUCLEAR
  }

  public enum TankState implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom"),
    MIDDLE("middle");
    private final String name;

    TankState(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return name;
    }
  }

  public static final EnumProperty<TankState> TANK_STATE =
      EnumProperty.create("tankstates", TankState.class);
  public final Kind kind;
  public final Fuel fuel;
  public final int thrust;
  public final int consumption;

  public RocketPartBlock(Kind kind, Fuel fuel, int thrust, int consumption, Properties properties) {
    super(properties);
    this.kind = kind;
    this.fuel = fuel;
    this.thrust = thrust;
    this.consumption = consumption;
    registerDefaultState(
        stateDefinition
            .any()
            .setValue(BlockStateProperties.FACING, Direction.DOWN)
            .setValue(TANK_STATE, TankState.MIDDLE));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(BlockStateProperties.FACING, TANK_STATE);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction facing =
        kind == Kind.ENGINE ? Direction.DOWN : context.getHorizontalDirection().getOpposite();
    BlockState state = defaultBlockState().setValue(BlockStateProperties.FACING, facing);
    return kind == Kind.TANK
        ? tankState(state, context.getLevel(), context.getClickedPos())
        : state;
  }

  private BlockState tankState(BlockState state, LevelAccessor level, BlockPos pos) {
    boolean above = level.getBlockState(pos.above()).is(this),
        below = level.getBlockState(pos.below()).is(this);
    return state.setValue(
        TANK_STATE,
        above && !below ? TankState.BOTTOM : below && !above ? TankState.TOP : TankState.MIDDLE);
  }

  @Override
  protected BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighbor,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    return kind == Kind.TANK
        ? tankState(state, level, pos)
        : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return kind == Kind.TANK
            || kind == Kind.GUIDANCE
            || kind == Kind.BUILDER
            || kind == Kind.DEPLOYABLE_BUILDER
            || kind == Kind.STATION_BUILDER
        ? new RocketBlockEntity(pos, state)
        : null;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return !level.isClientSide
            && (kind == Kind.BUILDER || kind == Kind.DEPLOYABLE_BUILDER)
            && type == RocketRegistry.BLOCK_ENTITY.get()
        ? (world, pos, blockState, entity) -> ((RocketBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
    if ((kind == Kind.STATION_BUILDER || kind == Kind.GUIDANCE)
        && state.getBlock() != replacement.getBlock()
        && level.getBlockEntity(pos) instanceof RocketBlockEntity entity) {
      var contents = kind == Kind.GUIDANCE ? entity.guidanceInventory : entity.stationInventory;
      for (int slot = 0; slot < contents.getSlots(); slot++)
        popResource(level, pos, contents.getStackInSlot(slot));
    }
    super.onRemove(state, level, pos, replacement, moved);
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
    if (kind == Kind.TANK
        && AdvancedRocketryConfig.canBeFueledByHand()
        && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection()))
      return ItemInteractionResult.SUCCESS;
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (kind == Kind.SEAT) {
      if (!level.isClientSide) {
        var mounts = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        SeatEntity mount =
            mounts.isEmpty() ? RocketRegistry.SEAT_MOUNT.get().create(level) : mounts.getFirst();
        if (mount != null && mount.getPassengers().isEmpty()) {
          if (mount.isRemoved() || mounts.isEmpty()) {
            mount.setPos(pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5);
            level.addFreshEntity(mount);
          }
          player.startRiding(mount);
        }
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    if (kind == Kind.ENGINE && player.isShiftKeyDown()) {
      if (!level.isClientSide) {
        Direction current = state.getValue(BlockStateProperties.FACING);
        Direction next = Direction.values()[(current.ordinal() + 1) % Direction.values().length];
        level.setBlock(pos, state.setValue(BlockStateProperties.FACING, next), 3);
        player.displayClientMessage(
            Texts.translate("message.adv_rocketry.rocket_part_block.engine_facing", next.getName()),
            true);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof RocketBlockEntity entity) {
      serverPlayer.openMenu(entity, buffer -> RocketMenu.writeOpeningData(buffer, entity));
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}

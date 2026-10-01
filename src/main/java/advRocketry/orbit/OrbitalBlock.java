// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OrbitalBlock extends Block implements EntityBlock {
  public enum Kind {
    BUILDER,
    HATCH,
    TERMINAL,
    MICROWAVE_RECEIVER,
    SOLAR_ARRAY,
    BLACK_HOLE_GENERATOR,
    STATION_GRAVITY_CONTROLLER,
    OBSERVATORY,
    SPACE_ELEVATOR,
    AREA_GRAVITY_CONTROLLER,
    ORBITAL_LASER,
    BIOME_SCANNER,
    BEACON,
    DOCKING_PORT,
    RAILGUN,
    ASTROBODY_PROCESSOR,
    ATMOSPHERE_TERRAFORMER,
    STATION_ALTITUDE_CONTROLLER,
    STATION_ORIENTATION_CONTROLLER,
    FORCE_FIELD_PROJECTOR,
    WARP_CORE,
    WARP_CONTROLLER,
    PLANET_SELECTOR,
    HOLOGRAPHIC_SELECTOR
  }

  public final Kind kind;

  public OrbitalBlock(Kind kind, Properties properties) {
    super(properties);
    this.kind = kind;
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return kind == Kind.STATION_GRAVITY_CONTROLLER || kind == Kind.STATION_ALTITUDE_CONTROLLER;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof OrbitalBlockEntity controller
        ? StationMotionLogic.comparatorSignal(controller)
        : 0;
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return kind == Kind.HOLOGRAPHIC_SELECTOR
        ? Block.box(0, 0, 0, 16, 8, 16)
        : super.getShape(state, level, pos, context);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new OrbitalBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    if (type != OrbitalRegistry.BLOCK_ENTITY.get()) return null;
    return level.isClientSide
        ? (world, pos, blockState, entity) -> ((OrbitalBlockEntity) entity).tickClient()
        : (world, pos, blockState, entity) -> ((OrbitalBlockEntity) entity).tick();
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof OrbitalBlockEntity entity)
      serverPlayer.openMenu(entity, buffer -> OrbitalMenu.writeOpeningData(buffer, entity));
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
    if (state.getBlock() != replacement.getBlock()
        && level.getBlockEntity(pos) instanceof OrbitalBlockEntity entity) {
      if (!level.isClientSide) cleanup(entity);
      for (int slot = 0; slot < entity.inventory.getSlots(); slot++)
        popResource(level, pos, entity.inventory.storedStackInSlot(slot));
    }
    super.onRemove(state, level, pos, replacement, moved);
  }

  public static void cleanup(OrbitalBlockEntity entity) {
    if (entity.spaceElevator()) SpaceElevatorLogic.unlink(entity);
    if (entity.beacon()) BeaconLogic.remove(entity);
    if (entity.dockingPort()) DockingPortLogic.remove(entity);
    if (entity.forceFieldProjector()) ForceFieldLogic.remove(entity);
    if (entity.orbitalLaser()) OrbitalLaserLogic.clearLights(entity);
    if (entity.warpCore()) WarpCoreLogic.unregister(entity);
  }
}

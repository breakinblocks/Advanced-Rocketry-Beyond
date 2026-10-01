// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

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

public final class PortBlock extends MachinePartBlock implements EntityBlock {
  private final MachinePorts.Kind kind;

  public PortBlock(MachinePorts.Kind kind, Properties properties) {
    super(properties);
    this.kind = kind;
  }

  public MachinePorts.Kind kind() {
    return kind;
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return kind != MachinePorts.Kind.SOLAR;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    if (!(level.getBlockEntity(pos) instanceof PortBlockEntity port)) return 0;
    if (port.hasFluid())
      return (int) (15L * port.myTank.getFluidAmount() / port.myTank.getCapacity());
    if (port.hasEnergy())
      return (int)
          (15L * port.energyStorage.getEnergyStored() / port.energyStorage.getMaxEnergyStored());
    if (!port.hasItems()) return 0;
    double fullness = 0;
    boolean occupied = false;
    for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
      ItemStack stack = port.inventory.getStackInSlot(slot);
      if (stack.isEmpty()) continue;
      occupied = true;
      fullness +=
          stack.getCount()
              / (double) Math.min(port.inventory.getSlotLimit(slot), stack.getMaxStackSize());
    }
    return (int) Math.floor(14 * fullness / port.inventory.getSlots()) + (occupied ? 1 : 0);
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
    if (kind == MachinePorts.Kind.FLUID_INPUT || kind == MachinePorts.Kind.FLUID_OUTPUT) {
      if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection()))
        return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new PortBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return type == MachinePorts.ENTITY.get()
        ? (world, pos, blockState, entity) -> ((PortBlockEntity) entity).tick()
        : null;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof PortBlockEntity port) {
      serverPlayer.openMenu(
          port, buffer -> MachineMenu.writeOpeningData(buffer, pos, port.menuPorts(), level));
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
    if (state.getBlock() != replacement.getBlock()
        && level.getBlockEntity(pos) instanceof PortBlockEntity port) port.dropContents();
    super.onRemove(state, level, pos, replacement, moved);
  }
}

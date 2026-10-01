package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

final class RocketDisassembly {
  private RocketDisassembly() {}

  static void disassemble(RocketEntity rocket, @Nullable Player player) {
    if (!(rocket.level() instanceof ServerLevel level)
        || rocket.flight() != 0
        || !rocket.getPassengers().isEmpty()) return;
    BlockPos origin = rocket.origin();
    for (var cell : rocket.structure.cells) {
      BlockPos position = origin.offset(cell.position());
      if (!level.isInWorldBounds(position)
          || !level.getWorldBorder().isWithinBounds(position)
          || !level.getBlockState(position).canBeReplaced()) {
        refuseDisassembly(
            player,
            Component.translatable(
                "message.adv_rocketry.rocket.the_rocket_needs_clear_space_before"));
        return;
      }
    }
    int primary = Math.max(0, (int) Math.floor(rocket.propellant - rocket.primaryFuelCarry + 1e-9));
    int secondary =
        Math.max(0, (int) Math.floor(rocket.oxidizer - rocket.oxidizerFuelCarry + 1e-9));
    int primaryTanks = 0, secondaryTanks = 0;
    for (var cell : rocket.structure.cells)
      if (cell.state().getBlock() instanceof RocketPartBlock part
          && part.kind == RocketPartBlock.Kind.TANK) {
        if (part.fuel == RocketPartBlock.Fuel.OXIDIZER) secondaryTanks++;
        else primaryTanks++;
      }
    int perTank = RocketBlockEntity.tankCapacity();
    if ((long) primaryTanks * perTank < primary || (long) secondaryTanks * perTank < secondary) {
      refuseDisassembly(
          player,
          Component.translatable("message.adv_rocketry.rocket.the_rocket_tanks_cannot_hold_its"));
      return;
    }
    Entity placer = player == null ? rocket : player;
    for (var cell : rocket.structure.cells) {
      BlockPos position = origin.offset(cell.position());
      if (player != null && !level.mayInteract(player, position)
          || NeoForge.EVENT_BUS
              .post(
                  new BlockEvent.EntityPlaceEvent(
                      BlockSnapshot.create(level.dimension(), level, position),
                      level.getBlockState(position.below()),
                      placer))
              .isCanceled()) {
        refuseDisassembly(
            player,
            Component.translatable(
                "message.adv_rocketry.rocket.the_rocket_cannot_be_disassembled_here"));
        return;
      }
    }
    for (var cell : rocket.structure.cells) {
      BlockPos position = origin.offset(cell.position());
      level.setBlock(position, cell.state(), 2);
      var entity = level.getBlockEntity(position);
      if (entity != null) {
        entity.loadWithComponents(cell.data(), rocket.registryAccess());
        if (entity instanceof RocketBlockEntity component
            && cell.state().getBlock() instanceof RocketPartBlock part
            && part.kind == RocketPartBlock.Kind.TANK) {
          boolean oxidizerTank = part.fuel == RocketPartBlock.Fuel.OXIDIZER;
          int amount = Math.min(perTank, oxidizerTank ? secondary : primary);
          ResourceLocation fuel = oxidizerTank ? rocket.oxidizerFluid : rocket.primaryFluid;
          component.tank.setFluid(new FluidStack(BuiltInRegistries.FLUID.get(fuel), amount));
          if (oxidizerTank) secondary -= amount;
          else primary -= amount;
        }
        entity.setChanged();
      }
    }
    for (ItemStack cargo : rocket.missionCargo) {
      ItemStack remaining = cargo.copy();
      for (var cell : rocket.structure.cells) {
        BlockPos position = origin.offset(cell.position());
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, position, null);
        if (handler == null) continue;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++)
          remaining = handler.insertItem(slot, remaining, false);
        if (remaining.isEmpty()) break;
      }
      if (!remaining.isEmpty()) Block.popResource(level, origin, remaining);
    }
    rocket.discard();
  }

  static void refuseDisassembly(@Nullable Player player, Component message) {
    if (player != null) player.displayClientMessage(message, true);
  }
}

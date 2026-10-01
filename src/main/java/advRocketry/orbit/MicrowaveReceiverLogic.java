package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.space.GalaxyData;
import advRocketry.space.SolarPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class MicrowaveReceiverLogic {
  private MicrowaveReceiverLogic() {}

  public static boolean structure(OrbitalBlockEntity entity) {
    PlacedMultiblock structure = PlacedMultiblock.of(entity, "microwave_receiver");
    if (structure == null || !structure.complete()) return false;
    for (BlockPos pos : structure.positions('p'))
      if (!entity.getLevel().canSeeSky(pos.above())) return false;
    return entity.getLevel().canSeeSky(entity.getBlockPos().above());
  }

  public static void burnBeam(ServerLevel level, BlockPos center, int power) {
    AABB beam =
        new AABB(
            center.getX() - 2,
            center.getY() + 1,
            center.getZ() - 2,
            center.getX() + 3,
            level.getMaxBuildHeight(),
            center.getZ() + 3);
    int fireTicks = Math.max(0, power * 2);
    for (Entity entity : level.getEntities((Entity) null, beam))
      entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), fireTicks));
  }

  public static void tick(OrbitalBlockEntity entity) {
    if (!entity.microwaveReceiver() || !(entity.getLevel() instanceof ServerLevel serverLevel))
      return;
    int received = 0;
    if (structure(entity)) {
      GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
      int orbit = StationLogic.orbitPlanet(galaxy, serverLevel, entity.getBlockPos());
      long id = SatelliteLink.read(entity.inventory.getStackInSlot(0));
      double conversion =
          2
              * SolarPower.insolationMultiplier(serverLevel, entity.getBlockPos())
              * AdvancedRocketryConfig.microwaveReceiverMultiplier();
      if (conversion > 0 && Double.isFinite(conversion)) {
        int capacity =
            (int)
                Math.max(
                    0,
                    Math.min(
                        100000 / conversion,
                        (entity.energy.getMaxEnergyStored() - entity.energy.getEnergyStored())
                            / conversion));
        int drawn =
            SatelliteLogic.drawMicrowaveEnergy(
                galaxy, id, orbit, capacity, serverLevel.getGameTime());
        received = (int) Math.min(100000, Math.round(drawn * conversion));
      }
      if (received > 0) entity.addGeneratedEnergy(received);
    }
    if (entity.powerMadeLastTick != received) {
      entity.powerMadeLastTick = received;
      entity.setChanged();
      entity
          .getLevel()
          .sendBlockUpdated(
              entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 2);
    }
    if (received > 0 && entity.getLevel().getGameTime() % 100 == 0)
      burnBeam(serverLevel, entity.getBlockPos(), received);
    for (Direction side : Direction.values()) {
      var destination =
          serverLevel.getCapability(
              Capabilities.EnergyStorage.BLOCK,
              entity.getBlockPos().relative(side),
              side.getOpposite());
      if (destination != null && destination.canReceive())
        entity.energy.extractEnergy(
            destination.receiveEnergy(entity.energy.getEnergyStored(), false), false);
    }
  }
}

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.SolarPower;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class SolarArrayLogic {
  private SolarArrayLogic() {}

  public static void tick(OrbitalBlockEntity entity) {
    if (!(entity.getLevel() instanceof ServerLevel serverLevel)) return;
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    Planet planet =
        galaxy.planets.get(StationLogic.orbitPlanet(galaxy, serverLevel, entity.getBlockPos()));
    boolean stationOrbit = StationLogic.inSpace(galaxy, serverLevel);
    if (planet == null
        || !(stationOrbit
            ? serverLevel.canSeeSky(entity.getBlockPos().below())
            : serverLevel.canSeeSky(entity.getBlockPos().above()))) {
      entity.powerMadeLastTick = 0;
      return;
    }
    int panels = panels(entity);
    if (panels < 0 || !serverLevel.isDay() && !stationOrbit) {
      entity.powerMadeLastTick = 0;
      return;
    }
    int generated =
        Math.min(100000, panels * SolarPower.perPanel(serverLevel, entity.getBlockPos()));
    entity.powerMadeLastTick = entity.addGeneratedEnergy(generated);
    List<PortBlockEntity> outputs =
        PlacedMultiblock.of(entity, "solar_array").entities('e', PortBlockEntity.class);
    for (int i = 0; i < outputs.size(); i++) {
      int share = Math.max(1, entity.energy.getEnergyStored() / (outputs.size() - i));
      entity.energy.extractEnergy(outputs.get(i).injectGeneratedEnergy(share), false);
    }
  }

  public static int panels(OrbitalBlockEntity entity) {
    PlacedMultiblock structure = PlacedMultiblock.of(entity, "solar_array");
    if (structure == null) return -1;
    for (BlockPos pos : structure.positions('e'))
      if (!entity.getLevel().getBlockState(pos).is(MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get())) {
        entity.status =
            Component.translatable("status.adv_rocketry.orbital.needs_two_energy_output_ports");
        return -1;
      }
    int panels = 0;
    for (BlockPos pos : structure.positions('p')) {
      if (!entity.getLevel().hasChunkAt(pos)) {
        entity.status =
            Component.translatable("status.adv_rocketry.orbital.array_chunk_is_not_loaded");
        return -1;
      }
      if (entity.getLevel().getBlockState(pos).is(OrbitalRegistry.SOLAR_ARRAY_PANEL.get())) {
        panels++;
      } else if (!entity.getLevel().isEmptyBlock(pos)) {
        entity.status =
            Texts.translate(
                "status.adv_rocketry.orbital.array_lane_is_obstructed_at", pos.toShortString());
        return -1;
      }
    }
    if (panels == 0)
      entity.status =
          Component.translatable("status.adv_rocketry.orbital.array_needs_solar_panels");
    return panels == 0 ? -1 : panels;
  }
}

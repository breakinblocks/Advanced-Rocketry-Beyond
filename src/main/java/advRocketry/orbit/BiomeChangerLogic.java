// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/** Persistent ten-cell-per-tick biome changing job powered by an orbital satellite. */
public final class BiomeChangerLogic {
  private static final int ENERGY_PER_CELL = 1920;
  private static final int CELLS = 64;

  private BiomeChangerLogic() {}

  public static Component start(ServerLevel level, long id, BlockPos center, String biomeName) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Satellite record = galaxy.satellites.get(id);
    int orbit = StationLogic.orbitPlanet(galaxy, level, center);
    if (record == null || !record.orbits(SatelliteType.BIOME_CHANGER, orbit))
      return Component.translatable("message.adv_rocketry.biome_changer.no_biome_changer_in_orbit");
    if (record.powerStorage < ENERGY_PER_CELL)
      return Component.translatable("message.adv_rocketry.biome_changer.needs_satellite_storage");
    ResourceLocation biome = ResourceLocation.tryParse(biomeName);
    if (biome == null
        || !level.registryAccess().registryOrThrow(Registries.BIOME).containsKey(biome))
      return Component.translatable("message.adv_rocketry.biome_changer.sneak_use_to_select");
    if (record.biomeJob != null)
      return Component.translatable("message.adv_rocketry.biome_changer.already_working");
    record.biomeJob =
        new Satellite.BiomeJob(
            level.dimension().location(),
            biome,
            Math.floorDiv(center.getX() - 16, 4) * 4,
            Math.floorDiv(center.getZ() - 16, 4) * 4);
    galaxy.setDirty();
    return Component.translatable("message.adv_rocketry.biome_changer.queued");
  }

  public static void run(MinecraftServer server) {
    GalaxyData galaxy = GalaxyData.get(server);
    for (Map.Entry<Long, Satellite> entry : new ArrayList<>(galaxy.satellites.entrySet())) {
      Satellite record = entry.getValue();
      Satellite.BiomeJob job = record.biomeJob;
      if (job == null) continue;
      SatelliteLogic.advance(galaxy, entry.getKey(), server.overworld().getGameTime());
      ResourceLocation dimension = job.dimension;
      ResourceLocation biomeId = job.biome;
      ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
      if (level == null) {
        Planet planet = galaxy.byDimension(dimension);
        if (planet != null && planet.landable()) level = PlanetRuntime.create(server, planet);
      }
      if (level == null) continue;
      Holder<Biome> biome =
          level
              .registryAccess()
              .registryOrThrow(Registries.BIOME)
              .getHolder(ResourceKey.create(Registries.BIOME, biomeId))
              .orElse(null);
      if (biome == null) continue;
      for (int changed = 0; changed < 10 && job.index < CELLS; changed++) {
        if (record.energy < ENERGY_PER_CELL) break;
        BlockPos cell = job.cell(level.getMinBuildHeight());
        level.getChunkAt(cell);
        if (FillBiomeCommand.fill(
                level,
                cell,
                new BlockPos(cell.getX() + 3, level.getMaxBuildHeight() - 1, cell.getZ() + 3),
                biome)
            .right()
            .isPresent()) break;
        record.energy -= ENERGY_PER_CELL;
        job.index++;
      }
      if (job.index >= CELLS) record.biomeJob = null;
      galaxy.setDirty();
    }
  }
}

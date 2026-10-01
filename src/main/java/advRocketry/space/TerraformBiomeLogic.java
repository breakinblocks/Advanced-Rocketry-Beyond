// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Gradually replaces existing planet biomes after an original atmosphere-class transition. */
public final class TerraformBiomeLogic {
  private static final Map<ServerLevel, ArrayDeque<ChunkWork>> PENDING = new WeakHashMap<>();
  private static final Map<ServerLevel, Set<ChunkPos>> QUEUED = new WeakHashMap<>();
  private static final Map<ServerLevel, Set<ChunkPos>> LOADED = new WeakHashMap<>();

  private TerraformBiomeLogic() {}

  private record ChunkWork(ChunkPos pos, int index) {}

  public static boolean eligible(Planet planet, ServerLevel level) {
    return planet != null
        && planet.id != GalaxyData.SPACE_ID
        && planet.id != -1
        && planet.dimension.equals(level.dimension().location())
        && (planet.id > 0 || AdvancedRocketryConfig.allowTerraformNonAR());
  }

  public static void chunkLoaded(ChunkEvent.Load event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    ChunkPos pos = event.getChunk().getPos();
    level
        .getServer()
        .execute(
            () -> {
              Planet planet =
                  GalaxyData.get(level.getServer()).byDimension(level.dimension().location());
              if (planet == null && !AdvancedRocketryConfig.allowTerraformNonAR()
                  || planet != null && !eligible(planet, level)) return;
              LOADED.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos);
              if (planet != null && !planet.terraformedBiomes.isEmpty()) enqueue(level, pos);
            });
  }

  public static void chunkUnloaded(ChunkEvent.Unload event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    ChunkPos pos = event.getChunk().getPos();
    level
        .getServer()
        .execute(
            () -> {
              Set<ChunkPos> loaded = LOADED.get(level);
              if (loaded != null) loaded.remove(pos);
            });
  }

  public static void queueLoaded(ServerLevel level) {
    Set<ChunkPos> loaded = LOADED.get(level);
    if (loaded != null) for (ChunkPos pos : loaded) enqueue(level, pos);
  }

  private static void enqueue(ServerLevel level, ChunkPos pos) {
    Set<ChunkPos> queued = QUEUED.computeIfAbsent(level, ignored -> new HashSet<>());
    if (queued.add(pos))
      PENDING.computeIfAbsent(level, ignored -> new ArrayDeque<>()).addLast(new ChunkWork(pos, 0));
  }

  public static int atmosphereClass(int density) {
    if (density > 800) return 4;
    if (density > 200) return 3;
    if (density > 75) return 2;
    if (density > 25) return 1;
    return 0;
  }

  public static void changeAtmosphere(Planet planet, int density, long seed) {
    int before = atmosphereClass(planet.atmosphere);
    if (planet.originalAtmosphere == null) planet.originalAtmosphere = planet.atmosphere;
    planet.atmosphere = density;
    if (before == atmosphereClass(density)) return;
    Planet candidate = new Planet(planet.id, planet.name);
    candidate.atmosphere = density;
    candidate.temperature = planet.temperature;
    try {
      GalaxyData.selectBiomes(candidate, new Random(PlanetSeeds.biomes(seed, planet.id) ^ density));
    } catch (IllegalStateException | ResourceLocationException exception) {
      return;
    }
    planet.terraformedBiomes.clear();
    planet.terraformedBiomes.addAll(candidate.biomes);
  }

  public static void tick(MinecraftServer server) {
    if (AdvancedRocketryConfig.enableTerraforming()) run(server);
  }

  static void run(MinecraftServer server) {
    GalaxyData galaxy = GalaxyData.get(server);
    for (ServerLevel level : server.getAllLevels()) {
      Planet planet = galaxy.find(level);
      if (!eligible(planet, level)
          || planet.terraformedBiomes.isEmpty()
          || level.players().isEmpty() && !PENDING.containsKey(level)) continue;
      var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
      List<Holder<Biome>> targetBiomes = new ArrayList<>();
      for (ResourceLocation id : planet.terraformedBiomes)
        registry.getHolder(ResourceKey.create(Registries.BIOME, id)).ifPresent(targetBiomes::add);
      if (targetBiomes.isEmpty()) continue;
      long seed = PlanetSeeds.planet(server.getWorldData().worldGenOptions().seed(), planet.id);
      PlanetBiomeSource source =
          PlanetBiomeSource.forPlanet(targetBiomes, List.of(), seed, planet, registry);
      ArrayDeque<ChunkWork> pending = PENDING.get(level);
      int rate = AdvancedRocketryConfig.biomeUpdateSpeed();
      int processed = 0;
      for (; pending != null && processed < rate && !pending.isEmpty(); processed++) {
        ChunkWork work = pending.removeFirst();
        if (level.getChunkSource().getChunkNow(work.pos.x, work.pos.z) == null) {
          QUEUED.get(level).remove(work.pos);
          continue;
        }
        int x = work.pos.getMinBlockX() + work.index % 4 * 4;
        int z = work.pos.getMinBlockZ() + work.index / 4 * 4;
        changeCell(level, source, x, z);
        if (work.index < 15) pending.addLast(new ChunkWork(work.pos, work.index + 1));
        else QUEUED.get(level).remove(work.pos);
      }
      if (pending != null && pending.isEmpty()) {
        PENDING.remove(level);
        QUEUED.remove(level);
      }
      if (level.players().isEmpty()) continue;
      for (int attempt = processed; attempt < rate; attempt++) {
        ServerPlayer player = level.players().get(attempt % level.players().size());
        int chunkX = (player.getBlockX() >> 4) + level.random.nextInt(17) - 8;
        int chunkZ = (player.getBlockZ() >> 4) + level.random.nextInt(17) - 8;
        if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) continue;
        int x = chunkX * 16 + level.random.nextInt(4) * 4;
        int z = chunkZ * 16 + level.random.nextInt(4) * 4;
        changeCell(level, source, x, z);
      }
    }
  }

  static void changeCell(ServerLevel level, PlanetBiomeSource source, int x, int z) {
    Holder<Biome> target =
        source.getNoiseBiome(x >> 2, 0, z >> 2, level.getChunkSource().randomState().sampler());
    BlockPos pos = new BlockPos(x, level.getMinBuildHeight(), z);
    var chunk = level.getChunkAt(pos);
    boolean unchanged = true;
    for (int y = level.getMinBuildHeight() >> 2; y < level.getMaxBuildHeight() >> 2; y++) {
      if (!chunk.getNoiseBiome(x >> 2, y, z >> 2).is(target)) {
        unchanged = false;
        break;
      }
    }
    if (unchanged) return;
    FillBiomeCommand.fill(
        level, pos, new BlockPos(x + 3, level.getMaxBuildHeight() - 1, z + 3), target);
  }
}

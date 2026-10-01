// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.serialization.MapCodec;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Owns native server levels; no external dimension library or copied fork infrastructure. */
public final class PlanetRuntime {
  private static final Set<MinecraftServer> PENDING_RESETS =
      Collections.newSetFromMap(new WeakHashMap<>());
  private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS =
      DeferredRegister.create(Registries.CHUNK_GENERATOR, Main.MODID);
  private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOMES =
      DeferredRegister.create(Registries.BIOME_SOURCE, Main.MODID);

  static {
    GENERATORS.register("planet", () -> PlanetTerrain.CODEC);
    BIOMES.register("planet", () -> PlanetBiomeSource.CODEC);
  }

  private PlanetRuntime() {}

  public static void register(IEventBus bus) {
    GENERATORS.register(bus);
    BIOMES.register(bus);
    NeoForge.EVENT_BUS.addListener(PlanetRuntime::starting);
    NeoForge.EVENT_BUS.addListener(PlanetRuntime::started);
  }

  private static void starting(ServerStartingEvent event) {
    MinecraftServer server = event.getServer();
    GalaxyData galaxy = GalaxyData.get(server);
    long seed = server.getWorldData().worldGenOptions().seed();
    var definitions = GalaxyDefinitions.Definitions.of(server.registryAccess());
    if (AdvancedRocketryConfig.resetPlanetsFromDefinitions()) {
      GalaxyDefinitions.reset(galaxy, definitions, seed);
      if (AdvancedRocketryConfig.resetPlanetsOnlyOnce()) PENDING_RESETS.add(server);
    } else GalaxyDefinitions.addMissing(galaxy, definitions, seed);
    // Reopen saved surfaces before player data resolves its login and respawn dimensions.
    // Unvisited planets remain lazy.
    for (Planet planet : galaxy.planets.values())
      if (planet.landable()
          && (GalaxyData.LUNA.equals(planet.location)
              || planet.id == GalaxyData.SPACE_ID
              || Files.isDirectory(server.storageSource.getDimensionPath(planet.key())))
          && server.getLevel(planet.key()) == null) create(server, planet);
  }

  private static void started(ServerStartedEvent event) {
    if (!PENDING_RESETS.remove(event.getServer())) return;
    // Persist the replacement before consuming the one-shot option.
    event.getServer().overworld().getDataStorage().save();
    AdvancedRocketryConfig.consumePlanetReset();
  }

  public static ServerLevel create(MinecraftServer server, Planet planet) {
    ServerLevel existing = server.getLevel(planet.key());
    if (existing != null) return existing;
    if (!planet.landable())
      throw new IllegalArgumentException("Cannot create a surface on a gas giant");
    var registries = server.registryAccess();
    var type =
        registries
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(
                ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                        Main.MODID, planet.terrain == TerrainType.SPACE ? "space" : "planet")));
    var biomeRegistry = registries.registryOrThrow(Registries.BIOME);
    List<Holder<Biome>> biomes = new ArrayList<>();
    for (ResourceLocation id : planet.terraformedBiomes)
      biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, id)).ifPresent(biomes::add);
    boolean terraformed = !biomes.isEmpty();
    if (!terraformed)
      for (ResourceLocation id : planet.biomes)
        biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, id)).ifPresent(biomes::add);
    if (biomes.isEmpty()) {
      List<ResourceLocation> defaults =
          switch (planet.terrain) {
            case MOON -> List.of(ResourceLocation.fromNamespaceAndPath(Main.MODID, "moon"));
            case SPACE -> List.of(ResourceLocation.fromNamespaceAndPath(Main.MODID, "space"));
            case VOLCANIC -> List.of(ResourceLocation.fromNamespaceAndPath(Main.MODID, "volcanic"));
            default ->
                planet.breathable()
                    ? List.of("plains", "forest", "taiga", "desert").stream()
                        .map(ResourceLocation::withDefaultNamespace)
                        .toList()
                    : List.of(ResourceLocation.withDefaultNamespace("stony_shore"));
          };
      for (ResourceLocation id : defaults)
        biomes.add(biomeRegistry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, id)));
    }
    var noiseKey =
        planet.terrain == TerrainType.ASTEROID
            ? NoiseGeneratorSettings.FLOATING_ISLANDS
            : planet.terrain == TerrainType.CAVE
                ? NoiseGeneratorSettings.CAVES
                : NoiseGeneratorSettings.OVERWORLD;
    var base =
        registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(noiseKey).value();
    BlockState filler = planet.fillerState();
    BlockState ocean =
        planet.atmosphere == 0
            ? Blocks.AIR.defaultBlockState()
            : BuiltInRegistries.BLOCK.get(planet.ocean).defaultBlockState();
    NoiseGeneratorSettings settings =
        new NoiseGeneratorSettings(
            base.noiseSettings(),
            filler,
            ocean,
            base.noiseRouter(),
            base.surfaceRule(),
            base.spawnTarget(),
            planet.seaLevel,
            !planet.breathable(),
            planet.atmosphere > 0,
            PlanetOres.select(planet).isEmpty() && base.oreVeinsEnabled(),
            base.useLegacyRandomSource());
    long seed = PlanetSeeds.planet(server.getWorldData().worldGenOptions().seed(), planet.id);
    List<Integer> biomeWeights =
        planet.biomeWeights.isEmpty() || terraformed
            ? List.of()
            : biomes.stream()
                .map(
                    biome ->
                        biome
                            .unwrapKey()
                            .map(key -> planet.biomeWeights.getOrDefault(key.location(), 30))
                            .orElse(30))
                .toList();
    PlanetTerrain terrain =
        new PlanetTerrain(
            PlanetBiomeSource.forPlanet(biomes, biomeWeights, seed, planet, biomeRegistry),
            Holder.direct(settings),
            planet,
            seed);
    ServerLevel level =
        new ServerLevel(
            server,
            Util.backgroundExecutor(),
            server.storageSource,
            new PlanetLevelData(server.getWorldData(), planet),
            planet.key(),
            new LevelStem(type, terrain),
            LoggerChunkProgressListener.createCompleted(),
            false,
            seed,
            List.of(),
            false,
            server.overworld().getRandomSequences()) {
          @Override
          public long getSeed() {
            return seed;
          }
        };
    WorldBorder border = server.overworld().getWorldBorder();
    level.getWorldBorder().applySettings(border.createSettings());
    border.addListener(
        new BorderChangeListener.DelegateBorderChangeListener(level.getWorldBorder()));
    server.forgeGetWorldMap().put(planet.key(), level);
    server.markWorldsDirty();
    NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
    return level;
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Persistent unmanned asteroid mission and original research-weighted harvest rule. */
public final class MiningMission {
  private MiningMission() {}

  public static boolean begin(ServerLevel level, RocketEntity rocket, ItemStack chip) {
    AsteroidTarget target = AsteroidChipItem.target(chip);
    AsteroidCatalog.Asteroid asteroid = AsteroidCatalog.get(level.registryAccess(), target.type());
    if (asteroid == null || rocket.structure().drillingPower <= 0) return false;
    schedule(
        level,
        rocket,
        Mission.Kind.ASTEROID,
        (long)
            (360
                / rocket.structure().drillingPower
                * asteroid.timeMultiplier()
                * AdvancedRocketryConfig.miningMissionTimeMultiplier()),
        mission -> {
          mission.asteroidId = asteroid.id();
          mission.asteroid = asteroid;
          mission.asteroidSeed = target.seed();
          for (ResearchType type : ResearchType.ASTEROID_TYPES)
            mission.research.put(type, AsteroidChipItem.data(chip, type));
        });
    return true;
  }

  static void schedule(
      ServerLevel level,
      RocketEntity rocket,
      Mission.Kind kind,
      long duration,
      Consumer<Mission> details) {
    Mission mission = new Mission(kind, rocket.miningSnapshot());
    details.accept(mission);
    mission.dimension = level.dimension();
    mission.startTick = level.getServer().overworld().getGameTime();
    mission.duration = Math.max(1, duration);
    GalaxyData.get(level.getServer()).newMiningMission(mission);
  }

  static boolean deliver(ServerLevel level, RocketEntity rocket) {
    return rocket != null && level.isLoaded(rocket.blockPosition()) && level.addFreshEntity(rocket);
  }

  public static void tick(MinecraftServer server) {
    long now = server.overworld().getGameTime();
    GalaxyData galaxy = GalaxyData.get(server);
    for (Map.Entry<Long, Mission> entry : new ArrayList<>(galaxy.miningMissions.entrySet())) {
      Mission mission = entry.getValue();
      if (!mission.due(now) || mission.dimension == null) continue;
      ResourceKey<Level> key = mission.dimension;
      ServerLevel destination = server.getLevel(key);
      if (destination == null) {
        Planet planet = galaxy.byDimension(key.location());
        if (planet != null && planet.landable()) destination = PlanetRuntime.create(server, planet);
      }
      if (destination == null
          || !(mission.kind == Mission.Kind.GAS
              ? GasMission.finish(destination, mission)
              : finish(destination, mission))) continue;
      galaxy.miningMissions.remove(entry.getKey());
      galaxy.setDirty();
    }
  }

  public static boolean finish(ServerLevel level, Mission mission) {
    AsteroidCatalog.Asteroid asteroid =
        mission.asteroid != null
            ? mission.asteroid
            : AsteroidCatalog.get(level.registryAccess(), mission.asteroidId);
    if (asteroid == null) return false;
    long seed = mission.asteroidSeed;
    List<ItemStack> harvest =
        new Random(seed ^ 0x5deece66dL).nextFloat()
                < mission.research(ResearchType.OPTICAL) / (float) ResearchType.UNIT_CAPACITY
            ? AsteroidCatalog.harvest(
                asteroid,
                seed,
                mission.research(ResearchType.COMPOSITION),
                mission.research(ResearchType.MASS))
            : List.of();
    RocketStructure ship = RocketStructure.load(mission.rocket.getCompound("ship"));
    for (RocketStructure.Cell cell : ship.cells) {
      if (!(cell.state().getBlock() instanceof RocketPartBlock part)
          || part.kind != RocketPartBlock.Kind.GUIDANCE) continue;
      ItemStackHandler contents = new ItemStackHandler(1);
      contents.deserializeNBT(
          level.registryAccess(), cell.data().getCompound("guidance_inventory"));
      ItemStack chip = contents.getStackInSlot(0);
      if (!AsteroidChipItem.programmed(chip)) continue;
      AsteroidChipItem.erase(chip);
      contents.setStackInSlot(0, chip);
      cell.data().put("guidance_inventory", contents.serializeNBT(level.registryAccess()));
      break;
    }
    CompoundTag returning = mission.rocket.copy();
    returning.put("ship", ship.save(true));
    return deliver(level, RocketEntity.returnFromMission(level, returning, harvest));
  }
}

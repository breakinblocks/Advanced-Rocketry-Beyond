// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry;

import advRocketry.command.RocketryCommands;
import advRocketry.datagen.ModDataGenerators;
import advRocketry.life.AtmosphereSync;
import advRocketry.life.JetpackControl;
import advRocketry.life.JetpackToggle;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.LightingRegistry;
import advRocketry.life.PlanetEnvironment;
import advRocketry.life.SealedRooms;
import advRocketry.orbit.BeaconLogic;
import advRocketry.orbit.BiomeChangerLogic;
import advRocketry.orbit.DockingPortConfig;
import advRocketry.orbit.ElevatorCapsule;
import advRocketry.orbit.MiningMission;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.StationChipAction;
import advRocketry.orbit.WarpLogic;
import advRocketry.processing.LaserBeamSync;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.processing.ProjectorSelection;
import advRocketry.rocket.HovercraftControl;
import advRocketry.rocket.HovercraftRegistry;
import advRocketry.rocket.RocketControl;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.SpaceFlightControl;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxySync;
import advRocketry.space.OreWorldgen;
import advRocketry.space.PlanetBiomeCatalog;
import advRocketry.space.PlanetOres;
import advRocketry.space.PlanetRespawn;
import advRocketry.space.PlanetRuntime;
import advRocketry.space.PlanetSpawns;
import advRocketry.space.TerraformBiomeLogic;
import advRocketry.transport.TransportInteraction;
import advRocketry.transport.TransportNetworks;
import advRocketry.transport.TransportRegistry;
import advRocketry.util.ServerSchedule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/** Native NeoForge entry point for the port of the original MIT source baseline. */
@Mod(Main.MODID)
public final class Main {
  public static final String MODID = "adv_rocketry";

  public Main(IEventBus bus, ModContainer container) {
    container.registerConfig(ModConfig.Type.SERVER, AdvancedRocketryConfig.SPEC);
    ModComponents.register(bus);
    DataRegistries.register(bus);
    DataMaps.register(bus);
    ModDataGenerators.register(bus);
    MachinePorts.register(bus);
    TransportRegistry.register(bus);
    NeoForge.EVENT_BUS.addListener(TransportNetworks::unload);
    NeoForge.EVENT_BUS.addListener(RocketryCommands::register);
    NeoForge.EVENT_BUS.addListener(TransportNetworks::levelTick);
    NeoForge.EVENT_BUS.addListener(TransportInteraction::bucket);
    ProcessingFluids.register(bus);
    NeoForge.EVENT_BUS.addListener(ProcessingFluids::lavaContact);
    ProcessingRegistry.register(bus);
    PlanetRuntime.register(bus);
    OreWorldgen.register(bus);
    RocketRegistry.register(bus);
    HovercraftRegistry.register(bus);
    OrbitalRegistry.register(bus);
    LifeSupportRegistry.register(bus);
    AdvancementLogic.register(bus);
    bus.addListener(AtmosphereSync::register);
    bus.addListener(LaserBeamSync::register);
    LightingRegistry.register(bus);
    NeoForge.EVENT_BUS.addListener(PlanetEnvironment::tick);
    NeoForge.EVENT_BUS.addListener(PlanetEnvironment::tickLiving);
    NeoForge.EVENT_BUS.addListener(PlanetEnvironment::preventNarcosisJump);
    NeoForge.EVENT_BUS.addListener(AdvancementLogic::seatOnTnt);
    NeoForge.EVENT_BUS.addListener(PlanetRespawn::sleep);
    NeoForge.EVENT_BUS.addListener(PlanetRespawn::setSpawn);
    NeoForge.EVENT_BUS.addListener(PlanetRespawn::respawn);
    NeoForge.EVENT_BUS.addListener(SealedRooms::unload);
    NeoForge.EVENT_BUS.addListener(SealedRooms::neighborChanged);
    NeoForge.EVENT_BUS.addListener(SealedRooms::gameEvent);
    NeoForge.EVENT_BUS.addListener(SealedRooms::chunkLoaded);
    NeoForge.EVENT_BUS.addListener(PlanetBiomeCatalog::starting);
    NeoForge.EVENT_BUS.addListener(PlanetBiomeCatalog::stopped);
    NeoForge.EVENT_BUS.addListener(PlanetOres::starting);
    NeoForge.EVENT_BUS.addListener(PlanetSpawns::potential);
    NeoForge.EVENT_BUS.addListener(PlanetSpawns::finalizeSpawn);
    NeoForge.EVENT_BUS.addListener(PlanetSpawns::joined);
    NeoForge.EVENT_BUS.addListener(PlanetEnvironment::checkSpawn);
    NeoForge.EVENT_BUS.addListener(TerraformBiomeLogic::chunkLoaded);
    NeoForge.EVENT_BUS.addListener(TerraformBiomeLogic::chunkUnloaded);
    NeoForge.EVENT_BUS.addListener(ServerSchedule::tick);
    ServerSchedule.every(1, RocketEntity::tickUnscheduled);
    ServerSchedule.every(1, ElevatorCapsule::tickUnscheduled);
    ServerSchedule.every(1, WarpLogic::tick);
    ServerSchedule.every(1, BiomeChangerLogic::run);
    ServerSchedule.every(1, TerraformBiomeLogic::tick);
    ServerSchedule.every(20, MiningMission::tick);
    ServerSchedule.every(20, GalaxySync::syncStations);
    ServerSchedule.every(40, BeaconLogic::sendFinders);
    NeoForge.EVENT_BUS.addListener(ElevatorCapsule::serverStopped);
    bus.addListener(RocketControl::register);
    bus.addListener(SpaceFlightControl::register);
    bus.addListener(ProjectorSelection::register);
    bus.addListener(HovercraftControl::register);
    bus.addListener(GalaxySync::register);
    bus.addListener(DockingPortConfig::register);
    bus.addListener(StationChipAction::register);
    bus.addListener(JetpackControl::register);
    bus.addListener(JetpackToggle::register);
    NeoForge.EVENT_BUS.addListener(JetpackControl::loggedOut);
    NeoForge.EVENT_BUS.addListener(GalaxySync::loggedIn);
    NeoForge.EVENT_BUS.addListener(GalaxySync::changedDimension);
    bus.addListener(MachinePorts::registerCapabilities);
  }
}

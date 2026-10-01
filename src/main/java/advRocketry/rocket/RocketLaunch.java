package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.AsteroidCatalog;
import advRocketry.orbit.AsteroidChipItem;
import advRocketry.orbit.GasMission;
import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.LinkerItem;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.Milestone;
import advRocketry.space.Planet;
import advRocketry.util.Texts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

final class RocketLaunch {
  private RocketLaunch() {}

  static boolean begin(RocketEntity rocket, @Nullable Player pilot) {
    if (!(rocket.level() instanceof ServerLevel level) || rocket.flight() != 0) return false;
    rocket.overridePlanet = Integer.MIN_VALUE;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    ItemStack guidance = rocket.guidanceChip();
    LandingPadLogic.Target guidanceTarget =
        GuidanceComputer.route(level, guidance, rocket.blockPosition());
    if (guidanceTarget == null
        && (guidance.is(OrbitalRegistry.SATELLITE_CHIP.get())
            || guidance.getItem() instanceof LinkerItem)) {
      if (pilot != null)
        pilot.displayClientMessage(
            Component.translatable("message.adv_rocketry.rocket.guidance_target_is_not_available"),
            true);
      return false;
    }
    if (guidanceTarget != null) {
      rocket.structure.destination = guidanceTarget.planet();
      rocket.structure.stationId = guidanceTarget.stationId();
    }
    LandingPadLogic.Target padTarget =
        guidance.getItem() instanceof LinkerItem
                || guidanceTarget != null
                    && GuidanceComputer.hasReturnLocation(guidance, guidanceTarget.planet())
            ? guidanceTarget
            : LandingPadLogic.override(level, rocket.blockPosition(), galaxy);
    if (padTarget != null) {
      rocket.structure.destination = padTarget.planet();
      rocket.structure.stationId = padTarget.stationId();
      rocket.sync(RocketEntity.SHIP, rocket.structure.save(false));
    }
    Planet current = galaxy.planet(level),
        target = galaxy.planets.get(rocket.structure.destination);
    ItemStack asteroidChip = rocket.miningChip();
    boolean mining =
        asteroidChip != null && rocket.structure.drillingPower > 0 && rocket.structure.seats == 0;
    boolean free =
        AdvancedRocketryConfig.experimentalSpaceFlight()
            && pilot != null
            && rocket.structure.seats > 0
            && rocket.emptyGuidance()
            && padTarget == null;
    Station launchStation =
        current.id == GalaxyData.SPACE_ID
            ? StationLogic.at(galaxy, rocket.level(), rocket.blockPosition())
            : null;
    Planet effectiveCurrent =
        launchStation == null ? current : galaxy.planets.get(launchStation.orbitPlanet);
    boolean gasMission =
        launchStation != null
            && effectiveCurrent != null
            && effectiveCurrent.gasGiant
            && rocket.structure.intakePower > 0
            && rocket.structure.seats == 0;
    Planet destinationBody =
        target != null && target.id == GalaxyData.SPACE_ID ? effectiveCurrent : target;
    Component error = null;
    if (rocket.deployable && !gasMission)
      error =
          Component.translatable(
              "message.adv_rocketry.rocket.deployable_rockets_require_a_gas_giant");
    else if (gasMission && !GasMission.possible(level, rocket))
      error =
          Component.translatable("message.adv_rocketry.rocket.gas_mission_needs_an_intake_fluid");
    else if (mining
        && !gasMission
        && AsteroidCatalog.get(
                rocket.level().registryAccess(), AsteroidChipItem.target(asteroidChip).type())
            == null)
      error =
          Component.translatable("message.adv_rocketry.rocket.the_asteroid_chip_names_an_unknown");
    else if (!free && !mining && !gasMission && (target == null || !target.landable()))
      error = Component.translatable("message.adv_rocketry.rocket.choose_a_landable_planet_in_the");
    else if (!free
        && !mining
        && !gasMission
        && target.id == GalaxyData.SPACE_ID
        && StationLogic.destination(galaxy, current.id, rocket.structure.stationId) == null)
      error =
          Component.translatable("message.adv_rocketry.rocket.no_space_station_orbits_this_planet");
    else if (current.id == GalaxyData.SPACE_ID && launchStation == null)
      error = Component.translatable("message.adv_rocketry.rocket.rocket_must_launch_from_a_space");
    else if (!free
        && !mining
        && !gasMission
        && (effectiveCurrent == null || destinationBody.star != effectiveCurrent.star))
      error =
          Component.translatable(
              "message.adv_rocketry.rocket.interstellar_travel_requires_a_warp_capable");
    else if (!free
        && !mining
        && !gasMission
        && current.id != GalaxyData.SPACE_ID
        && target.id != GalaxyData.SPACE_ID
        && target.id != current.id
        && target.parent != current.id
        && current.parent != target.id
        && !(target.parent >= 0 && target.parent == current.parent))
      error =
          Component.translatable("message.adv_rocketry.rocket.this_flight_requires_a_trans_body");
    int needed =
        gasMission
            ? rocket.structure.capacity
            : rocket.structure.requiredFuel(
                (int) rocket.getY(),
                current.gravity,
                launchStation == null
                    ? AdvancedRocketryConfig.orbitHeight()
                    : AdvancedRocketryConfig.stationClearanceHeight());
    if (error == null && AdvancedRocketryConfig.rocketsRequireFuel() && !rocket.enoughFuel(needed))
      error =
          Texts.translate(
              "message.adv_rocketry.rocket.insufficient_propellant_for_orbit_requires_mb",
              RocketFuelRegistry.cost(needed, rocket.structure.fuel, rocket.primaryFluid));
    if (error != null) {
      if (pilot != null) pilot.displayClientMessage(error, true);
      return false;
    }
    if (guidance.is(OrbitalRegistry.STATION_CHIP.get())
        && rocket.structure.destination == GalaxyData.SPACE_ID) {
      int selectedDestination = rocket.structure.destination;
      long selectedStation = rocket.structure.stationId;
      GuidanceComputer.rememberDeparture(level, guidance, rocket.blockPosition());
      rocket.setGuidanceChip(guidance);
      rocket.structure.destination = selectedDestination;
      rocket.structure.stationId = selectedStation;
    }
    rocket.verticalSpeed = 0;
    rocket.freeLaunch = free;
    rocket.sync(RocketEntity.ASTEROID_RCS, false);
    rocket.asteroidVelocity = Vec3.ZERO;
    if (padTarget != null) {
      rocket.overridePlanet = padTarget.planet();
      rocket.overrideX = padTarget.x();
      rocket.overrideZ = padTarget.z();
    }
    rocket.transferHeight =
        (launchStation == null
                ? AdvancedRocketryConfig.orbitHeight()
                : AdvancedRocketryConfig.stationClearanceHeight())
            + (gasMission || free
                ? 0
                : RocketTrajectory.injectionBurn(effectiveCurrent, destinationBody, mining));
    rocket.launchX = rocket.getX();
    rocket.launchY = rocket.getY();
    rocket.launchZ = rocket.getZ();
    rocket.horizontalSpeed = 0;
    rocket.sync(RocketEntity.COASTING, rocket.deployable);
    rocket.sync(RocketEntity.FLIGHT, 1);
    rocket.transit.update();
    if (AdvancedRocketryConfig.launchBlockDestruction() && !rocket.deployable)
      RocketScorch.scorch(level, rocket.blockPosition().below(), rocket.structure.thrust);
    for (Entity passenger : rocket.getPassengers())
      if (passenger instanceof ServerPlayer player)
        AdvancementLogic.grant(player, Milestone.ROCKET_LAUNCH);
    return true;
  }
}

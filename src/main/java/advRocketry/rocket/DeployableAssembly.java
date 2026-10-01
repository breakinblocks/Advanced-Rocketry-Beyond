// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.GasMission;
import advRocketry.orbit.Station;
import advRocketry.orbit.StationLogic;
import advRocketry.processing.MachinePorts;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.util.AssemblyFailure;
import advRocketry.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Original 17-block horizontal deployment frame with an uncrewed gas-rocket scan. */
public final class DeployableAssembly {
  private static final int MAX_SIZE = 17;

  private DeployableAssembly() {}

  public static void scan(RocketBlockEntity builder) {
    run(builder, false);
  }

  static void assemble(RocketBlockEntity builder) {
    run(builder, true);
  }

  private static void run(RocketBlockEntity builder, boolean assemble) {
    if (!(builder.getLevel() instanceof ServerLevel level)) return;
    builder.ready = false;
    try {
      scanFrame(builder, level, assemble);
    } catch (IllegalArgumentException exception) {
      builder.status = AssemblyFailure.describe(exception);
    }
    RocketAssembly.publish(builder, level);
  }

  private static void scanFrame(RocketBlockEntity builder, ServerLevel level, boolean assemble) {
    BlockPos frame = builder.getBlockPos();
    Direction forward = builder.getBlockState().getValue(BlockStateProperties.FACING);
    if (!forward.getAxis().isHorizontal())
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployable_assembler_must_face_horizontally"));
    Direction right = forward.getClockWise();
    int height = 0;
    while (height < MAX_SIZE && tower(level, frame.above(height + 1))) height++;
    if (height < 3)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_tower_at"));
    int length = 0;
    while (length < MAX_SIZE && tower(level, frame.above(height).relative(forward, length)))
      length++;
    if (length < 3)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_top_rail"));
    int left = 0, rightLength = 0;
    while (left < MAX_SIZE / 2 && tower(level, frame.relative(right, -left - 1))) left++;
    while (rightLength < MAX_SIZE / 2 && tower(level, frame.relative(right, rightLength + 1)))
      rightLength++;
    if (left + rightLength + 1 < 3)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_base_rail"));
    int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    for (int along = 1; along < length; along++)
      for (int across = -left + 1; across < rightLength; across++) {
        BlockPos column = frame.relative(forward, along).relative(right, across);
        minX = Math.min(minX, column.getX());
        minZ = Math.min(minZ, column.getZ());
        maxX = Math.max(maxX, column.getX());
        maxZ = Math.max(maxZ, column.getZ());
      }
    BlockPos origin = new BlockPos(minX, frame.getY(), minZ);
    RocketStructure ship = new RocketStructure();
    ship.width = maxX - minX + 1;
    ship.depth = maxZ - minZ + 1;
    ship.height = height;
    RocketAssembly.capture(
        builder,
        level,
        ship,
        origin,
        forward,
        Component.translatable("message.adv_rocketry.rocket_assembly.noun_deployable"),
        Component.translatable("message.adv_rocketry.deployable_assembly.engines_face_outward"));
    if (ship.seats != 0)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployable_gas_rockets_cannot_carry_passengers"));
    if (ship.intakePower <= 0 || GasMission.capacity(ship) <= 0)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.gas_rocket_needs_an_intake_and"));
    if (AdvancedRocketryConfig.rocketsRequireFuel()
            && ship.capacity * RocketFuelRegistry.maximumMultiplier(ship.fuel) < ship.consumption
        || ship.thrust <= ship.cells.size())
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployable_rocket_needs_working_engines_and"));
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, frame);
    Planet orbit = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    if (galaxy.planet(level).id != GalaxyData.SPACE_ID || orbit == null || !orbit.gasGiant)
      throw new AssemblyFailure(
          Component.translatable(
              "message.adv_rocketry.deployable_assembly.deployable_rockets_require_a_gas_giant"));
    ship.storedFuel(level.registryAccess());
    builder.ready = true;
    builder.status =
        Texts.translate(
            "status.adv_rocketry.deployable_assembly.ready_mb_propellant_and_mb_gas",
            ship.capacity,
            GasMission.capacity(ship));
    if (assemble)
      RocketAssembly.commit(
          builder,
          level,
          ship,
          origin,
          rocket -> rocket.initializeDeployable(ship, origin, forward),
          Component.translatable("message.adv_rocketry.rocket_assembly.name_deployable"));
  }

  private static boolean tower(ServerLevel level, BlockPos position) {
    return level.getBlockState(position).is(MachinePorts.STRUCTURE_TOWER.get());
  }
}

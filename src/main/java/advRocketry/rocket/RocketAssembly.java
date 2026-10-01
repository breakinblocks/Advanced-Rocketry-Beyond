// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.util.AssemblyFailure;
import advRocketry.util.Texts;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Original 16 by 16 launch-pad and 64-block tower scan, with native ship storage. */
public final class RocketAssembly {
  private RocketAssembly() {}

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
      BlockPos pad = null;
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        BlockPos candidate = builder.getBlockPos().relative(direction).below();
        if (level.getBlockState(candidate).is(RocketRegistry.PARTS.get("launch_pad").get())) {
          pad = candidate;
          break;
        }
      }
      if (pad == null)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_assembly.place_the_builder_beside_a_launch"));
      Set<BlockPos> pads = new HashSet<>();
      ArrayDeque<BlockPos> pending = new ArrayDeque<>();
      pending.add(pad);
      while (!pending.isEmpty()) {
        BlockPos position = pending.removeFirst();
        if (pads.contains(position)
            || !level.getBlockState(position).is(RocketRegistry.PARTS.get("launch_pad").get()))
          continue;
        pads.add(position);
        if (pads.size() > 256)
          throw new AssemblyFailure(
              Component.translatable(
                  "message.adv_rocketry.rocket_assembly.launch_pad_exceeds_16_by_16"));
        for (Direction direction : Direction.Plane.HORIZONTAL)
          pending.add(position.relative(direction));
      }
      int minX = pads.stream().mapToInt(BlockPos::getX).min().orElseThrow(),
          maxX = pads.stream().mapToInt(BlockPos::getX).max().orElseThrow();
      int minZ = pads.stream().mapToInt(BlockPos::getZ).min().orElseThrow(),
          maxZ = pads.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
      int width = maxX - minX + 1, depth = maxZ - minZ + 1;
      if (width > 16 || depth > 16 || pads.size() != width * depth)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_assembly.launch_pad_must_be_a_solid"));
      int height = 0;
      for (int x = minX - 1; x <= maxX + 1; x++)
        for (int z = minZ - 1; z <= maxZ + 1; z++) {
          if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) continue;
          int tower = 0;
          while (tower < 64
              && level
                  .getBlockState(new BlockPos(x, pad.getY() + 1 + tower, z))
                  .is(MachinePorts.STRUCTURE_TOWER.get())) tower++;
          height = Math.max(height, tower);
        }
      if (height < 4)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_assembly.a_structure_tower_at_least_four"));
      BlockPos origin = new BlockPos(minX, pad.getY() + 1, minZ);
      RocketStructure structure = new RocketStructure();
      structure.width = width;
      structure.depth = depth;
      structure.height = height;
      boolean guidance =
          capture(
              builder,
              level,
              structure,
              origin,
              Direction.DOWN,
              Component.translatable("message.adv_rocketry.rocket_assembly.noun_rocket"),
              Component.translatable("message.adv_rocketry.rocket_assembly.engines_face_down"));
      if (!guidance)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_assembly.a_guidance_computer_is_required"));
      var launchPlanet = GalaxyData.get(level.getServer()).planet(level);
      float gravity = launchPlanet.gravity;
      if (structure.acceleration(gravity) <= 0)
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_assembly.thrust_must_exceed_rocket_mass_under"));
      int needed =
          structure.requiredFuel(
              origin.getY(),
              gravity,
              launchPlanet.id == GalaxyData.SPACE_ID
                  ? AdvancedRocketryConfig.stationClearanceHeight()
                  : AdvancedRocketryConfig.orbitHeight());
      if (AdvancedRocketryConfig.rocketsRequireFuel()
          && (structure.capacity * RocketFuelRegistry.maximumMultiplier(structure.fuel) < needed
              || structure.fuel == RocketPartBlock.Fuel.BIPROPELLANT
                  && structure.oxidizerCapacity
                          * RocketFuelRegistry.maximumMultiplier(RocketPartBlock.Fuel.OXIDIZER)
                      < needed))
        throw new AssemblyFailure(
            Texts.translate(
                "message.adv_rocketry.rocket_assembly.insufficient_tank_capacity_for_orbit_requires",
                needed));
      int minimumFuel =
          (int)
              Math.ceil(
                  needed / Math.max(.001, RocketFuelRegistry.maximumMultiplier(structure.fuel)));
      structure.storedFuel(level.registryAccess());
      builder.status =
          Texts.translate(
              "status.adv_rocketry.rocket_assembly.ready_at_least_mb_required_for", minimumFuel);
      builder.ready = true;
      if (assemble)
        commit(
            builder,
            level,
            structure,
            origin,
            rocket -> rocket.initialize(structure, origin),
            Component.translatable("message.adv_rocketry.rocket_assembly.name_rocket"));
    } catch (IllegalArgumentException exception) {
      builder.status = AssemblyFailure.describe(exception);
    }
    publish(builder, level);
  }

  static boolean capture(
      RocketBlockEntity builder,
      ServerLevel level,
      RocketStructure structure,
      BlockPos origin,
      Direction engineFacing,
      Component noun,
      Component engineError) {
    Set<RocketPartBlock.Fuel> engines = new HashSet<>();
    int nuclearCore = 0;
    boolean guidance = false;
    for (int y = 0; y < structure.height; y++)
      for (int z = 0; z < structure.depth; z++)
        for (int x = 0; x < structure.width; x++) {
          BlockPos position = origin.offset(x, y, z);
          if (!level.hasChunkAt(position))
            throw new AssemblyFailure(
                Texts.translate(
                    "message.adv_rocketry.rocket_assembly.the_extends_into_unloaded_chunks", noun));
          var state = level.getBlockState(position);
          if (state.isAir() || state.canBeReplaced()) continue;
          if (state.is(ModTags.ROCKET_BLACKLIST))
            throw new AssemblyFailure(
                Texts.translate(
                    "message.adv_rocketry.rocket_assembly.the_contains_a_blacklisted_block", noun));
          if (state.getDestroySpeed(level, position) < 0)
            throw new AssemblyFailure(
                Texts.translate(
                    "message.adv_rocketry.rocket_assembly.the_contains_an_unmovable_block", noun));
          var entity = level.getBlockEntity(position);
          CompoundTag data =
              entity == null
                  ? new CompoundTag()
                  : entity.saveWithFullMetadata(level.registryAccess());
          structure.cells.add(new RocketStructure.Cell(new BlockPos(x, y, z), state, data));
          if (state.is(ProcessingRegistry.part("nuclear_core")))
            nuclearCore += (int) Math.round(1000 * AdvancedRocketryConfig.nuclearCoreThrustRatio());
          if (state.is(ProcessingRegistry.part("gas_intake"))) structure.intakePower += 10;
          if (!(state.getBlock() instanceof RocketPartBlock part)) continue;
          switch (part.kind) {
            case ENGINE -> {
              if (state.getValue(BlockStateProperties.FACING) != engineFacing)
                throw new AssemblyFailure(engineError);
              engines.add(part.fuel);
              structure.thrust += part.thrust;
              structure.consumption += part.consumption;
            }
            case GUIDANCE -> {
              guidance = true;
              RocketBlockEntity computer = (RocketBlockEntity) entity;
              structure.destination = computer.destination;
              structure.stationId = computer.stationId;
              var route =
                  GuidanceComputer.route(
                      level, computer.guidanceInventory.getStackInSlot(0), origin);
              if (route != null) {
                structure.destination = route.planet();
                structure.stationId = route.stationId();
              }
            }
            case SEAT -> structure.seats++;
            case DRILL ->
                structure.drillingPower +=
                    level.isEmptyBlock(position.above()) && level.isEmptyBlock(position.above(2))
                        ? .02f
                        : .01f;
            default -> {}
          }
        }
    aggregate(builder, structure, engines, nuclearCore, noun);
    return guidance;
  }

  private static void aggregate(
      RocketBlockEntity builder,
      RocketStructure structure,
      Set<RocketPartBlock.Fuel> engines,
      int nuclearCore,
      Component noun) {
    builder.mass = structure.cells.size();
    builder.thrust = structure.thrust;
    if (engines.size() != 1)
      throw new AssemblyFailure(
          Texts.translate(
              "message.adv_rocketry.rocket_assembly.use_one_engine_and_fuel_type", noun));
    structure.fuel = engines.iterator().next();
    if (structure.fuel == RocketPartBlock.Fuel.NUCLEAR)
      structure.thrust = Math.min(structure.thrust, nuclearCore);
    int tanks = 0, oxidizerTanks = 0;
    for (var cell : structure.cells)
      if (cell.state().getBlock() instanceof RocketPartBlock part
          && part.kind == RocketPartBlock.Kind.TANK) {
        if (part.fuel == RocketPartBlock.Fuel.OXIDIZER) oxidizerTanks++;
        else if (part.fuel == structure.fuel) tanks++;
        else
          throw new AssemblyFailure(
              Component.translatable(
                  "message.adv_rocketry.rocket_assembly.do_not_mix_propellant_tank_types"));
      }
    structure.thrust =
        (int) Math.round(structure.thrust * AdvancedRocketryConfig.rocketThrustMultiplier());
    structure.capacity = tanks * RocketBlockEntity.tankCapacity();
    structure.oxidizerCapacity = oxidizerTanks * RocketBlockEntity.tankCapacity();
    builder.thrust = structure.thrust;
    builder.fuel = structure.capacity;
    builder.scannedVolume = structure.width * structure.depth * structure.height;
  }

  static RocketEntity commit(
      RocketBlockEntity builder,
      ServerLevel level,
      RocketStructure structure,
      BlockPos origin,
      Consumer<RocketEntity> setup,
      Component name) {
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    if (rocket == null) throw new IllegalStateException("Rocket entity registration failed");
    setup.accept(rocket);
    rocket.linkBuilder(builder.getBlockPos());
    if (!level.addFreshEntity(rocket))
      throw new AssemblyFailure(
          Texts.translate("message.adv_rocketry.rocket_assembly.assembly_was_cancelled", name));
    builder.lastAssembledRocket = rocket;
    for (var cell : structure.cells) level.removeBlockEntity(origin.offset(cell.position()));
    for (var cell : structure.cells)
      level.setBlock(origin.offset(cell.position()), Blocks.AIR.defaultBlockState(), 2);
    builder.status = Texts.translate("status.adv_rocketry.rocket_assembly.assembled", name);
    return rocket;
  }

  static void publish(RocketBlockEntity builder, ServerLevel level) {
    builder.setChanged();
    level.sendBlockUpdated(
        builder.getBlockPos(), builder.getBlockState(), builder.getBlockState(), 3);
    for (var player : level.players())
      if (player.containerMenu instanceof RocketMenu menu && menu.entity == builder)
        player.displayClientMessage(builder.status, false);
  }

  public static void launch(RocketBlockEntity builder) {
    if (!(builder.getLevel() instanceof ServerLevel level)) return;
    BlockPos pos = builder.getBlockPos();
    RocketEntity rocket = builder.lastAssembledRocket;
    if (rocket == null || rocket.isRemoved() || !rocket.launchFrom(builder))
      rocket =
          RocketInfrastructureLink.builderRockets(level, pos, pos.getCenter()).stream()
              .filter(candidate -> candidate.launchFrom(builder))
              .findFirst()
              .orElse(null);
    builder.status =
        rocket == null
            ? Component.translatable(
                "status.adv_rocketry.rocket_assembly.no_ready_rocket_linked_to_this")
            : Component.translatable("status.adv_rocketry.rocket_assembly.rocket_launched");
    builder.setChanged();
    level.sendBlockUpdated(pos, builder.getBlockState(), builder.getBlockState(), 3);
  }
}

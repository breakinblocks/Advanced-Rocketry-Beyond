// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModTags;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import advRocketry.util.IdOrTag;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original orbital laser footprint and default 10,000-FE void-drill ore table. */
@EventBusSubscriber(modid = Main.MODID)
public final class OrbitalLaserLogic {
  private record Palette(List<IdOrTag> planetOres, List<ItemStack> ores) {}

  private static final Map<Integer, Palette> PALETTES = new ConcurrentHashMap<>();

  private OrbitalLaserLogic() {}

  public static boolean complete(OrbitalBlockEntity controller) {
    return AdvancedRocketryConfig.enableLaserDrill()
        && controller.orbitalLaser()
        && PlacedMultiblock.complete(controller, "orbital_laser");
  }

  private static List<PortBlockEntity> outputs(OrbitalBlockEntity controller) {
    return PlacedMultiblock.of(controller, "orbital_laser").entities('O', PortBlockEntity.class);
  }

  public static void control(OrbitalBlockEntity controller, int button) {
    if (!controller.orbitalLaser()) return;
    if (button == 13) {
      if (controller.laserRunning) {
        controller.laserRunning = false;
        clearLights(controller);
        controller.status =
            Component.translatable("status.adv_rocketry.orbital_laser.orbital_laser_stopped");
      } else start(controller);
    } else if (button == 19) {
      controller.laserJammed = false;
      controller.status =
          Component.translatable("status.adv_rocketry.orbital_laser.output_jam_cleared");
    } else if (button == 18 && !controller.laserRunning) {
      controller.laserTerrain = !controller.laserTerrain;
      controller.laserDepth = 0;
      controller.status =
          controller.laserTerrain
              ? Component.translatable("status.adv_rocketry.orbital_laser.terrain_mining_selected")
              : Component.translatable("status.adv_rocketry.orbital_laser.void_drilling_selected");
    } else if (!controller.laserRunning) {
      switch (button) {
        case 14 -> controller.laserX -= 16;
        case 15 -> controller.laserX += 16;
        case 16 -> controller.laserZ -= 16;
        case 17 -> controller.laserZ += 16;
        default -> {
          return;
        }
      }
      controller.laserDepth = 0;
      ServerLevel target = targetLevel(controller);
      if (target != null) clampTarget(controller, target);
      else if (controller.getLevel() != null) clampTarget(controller, controller.getLevel());
      controller.status =
          Texts.translate(
              "status.adv_rocketry.orbital_laser.target", controller.laserX, controller.laserZ);
    }
    controller.setChanged();
    if (controller.getLevel() instanceof ServerLevel level)
      level.sendBlockUpdated(
          controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(), 3);
  }

  private static ServerLevel targetLevel(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return null;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, controller.getBlockPos());
    Planet planet = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    return planet == null || !planet.landable()
        ? null
        : PlanetRuntime.create(level.getServer(), planet);
  }

  private static void clampTarget(OrbitalBlockEntity controller, Level target) {
    WorldBorder border = target.getWorldBorder();
    controller.laserX =
        Mth.clamp(
            controller.laserX,
            (int) Math.ceil(border.getMinX()) + 1,
            (int) Math.floor(border.getMaxX()) - 2);
    controller.laserZ =
        Mth.clamp(
            controller.laserZ,
            (int) Math.ceil(border.getMinZ()) + 1,
            (int) Math.floor(border.getMaxZ()) - 2);
  }

  private static void start(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level) || !complete(controller)) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.build_the_orbital_laser_multiblock");
      return;
    }
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    if (!StationLogic.inSpace(galaxy, level)) {
      controller.status =
          Component.translatable("status.adv_rocketry.orbital_laser.orbital_laser_must_be_on_a");
      return;
    }
    var station = StationLogic.at(galaxy, level, controller.getBlockPos());
    Planet planet = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    if (planet == null || !planet.landable()) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.station_needs_a_landable_planet_below");
      return;
    }
    if (!planet.laserDrillable) {
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.orbital_laser_is_disabled_above_this");
      return;
    }
    ServerLevel target = PlanetRuntime.create(level.getServer(), planet);
    int operationEnergy = AdvancedRocketryConfig.laserDrillEnergy();
    if (controller.energy.consume(operationEnergy, true) < operationEnergy) {
      controller.status =
          Texts.translate(
              "status.adv_rocketry.orbital_laser.orbital_laser_needs_fe", operationEnergy);
      return;
    }
    controller.laserRunning = true;
    controller.laserJammed = false;
    controller.laserTargetPlanet = planet.id;
    clampTarget(controller, target);
    if (controller.laserTerrain && controller.laserDepth == 0) {
      int top = target.getMinBuildHeight();
      for (int x = -1; x <= 1; x++)
        for (int z = -1; z <= 1; z++)
          top =
              Math.max(
                  top,
                  target.getHeight(
                      Heightmap.Types.MOTION_BLOCKING,
                      controller.laserX + x,
                      controller.laserZ + z));
      controller.laserDepth = top - 1;
    }
    controller.status =
        Texts.translate(
            "status.adv_rocketry.orbital_laser.orbital_laser_running_above", planet.name);
  }

  public static void tick(OrbitalBlockEntity controller) {
    if (!(controller.getLevel() instanceof ServerLevel level)) return;
    if (!complete(controller)) {
      if (controller.laserRunning) {
        controller.laserRunning = false;
        clearLights(controller);
        controller.status =
            Component.translatable(
                "status.adv_rocketry.orbital_laser.orbital_laser_structure_incomplete");
        controller.setChanged();
      }
      return;
    }
    for (PortBlockEntity port :
        PlacedMultiblock.of(controller, "orbital_laser").entities('P', PortBlockEntity.class))
      controller.pullFrom(port, 1000);
    if (!controller.laserRunning || controller.laserJammed) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    var station = StationLogic.at(galaxy, level, controller.getBlockPos());
    Planet target = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    if (target == null
        || !target.laserDrillable
        || controller.laserTargetPlanet != Integer.MIN_VALUE
            && target.id != controller.laserTargetPlanet) {
      controller.laserRunning = false;
      clearLights(controller);
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.orbital_laser_has_no_eligible_planet");
      controller.setChanged();
      return;
    }
    if (controller.laserTerrain)
      refreshNode(controller, PlanetRuntime.create(level.getServer(), target));
    controller.laserTicks++;
    int stored = controller.energy.getEnergyStored();
    int operationEnergy = AdvancedRocketryConfig.laserDrillEnergy();
    if (stored < operationEnergy
        || controller.laserTicks <= 3 * controller.energy.getMaxEnergyStored() / stored) return;
    boolean produced;
    if (controller.laserTerrain) produced = mineTerrain(controller, level);
    else {
      ItemStack yield = output(level, target);
      produced = canInsertOutput(controller, List.of(yield)) && insertOutput(controller, yield);
    }
    if (!produced) {
      controller.laserJammed = true;
      controller.laserRunning = false;
      clearLights(controller);
      controller.status =
          Component.translatable("status.adv_rocketry.orbital_laser.orbital_laser_output_jammed");
      controller.setChanged();
      return;
    }
    controller.energy.consume(operationEnergy, false);
    controller.laserTicks = 0;
    controller.setChanged();
  }

  public static void clearLights(OrbitalBlockEntity controller) {
    if (!controller.laserTerrain || !(controller.getLevel() instanceof ServerLevel stationLevel))
      return;
    GalaxyData galaxy = GalaxyData.get(stationLevel.getServer());
    var station = StationLogic.at(galaxy, stationLevel, controller.getBlockPos());
    Planet planet =
        controller.laserTargetPlanet == Integer.MIN_VALUE
            ? station == null ? null : galaxy.planets.get(station.orbitPlanet)
            : galaxy.planets.get(controller.laserTargetPlanet);
    controller.laserTargetPlanet = Integer.MIN_VALUE;
    if (planet == null || !planet.landable()) return;
    ServerLevel target = PlanetRuntime.create(stationLevel.getServer(), planet);
    if (controller.laserNode != null) controller.laserNode.discard();
    else if (controller.laserNodeId != null
        && target.getEntity(controller.laserNodeId) instanceof LaserNodeEntity node) node.discard();
    controller.laserNode = null;
    controller.laserNodeId = null;
    clearLights(target, controller.laserX, controller.laserZ);
  }

  private static void refreshNode(OrbitalBlockEntity controller, ServerLevel target) {
    LaserNodeEntity node =
        controller.laserNode != null && !controller.laserNode.isRemoved()
            ? controller.laserNode
            : controller.laserNodeId == null
                ? null
                : target.getEntity(controller.laserNodeId) instanceof LaserNodeEntity existing
                    ? existing
                    : null;
    if (node == null) {
      node = OrbitalRegistry.LASER_NODE.get().create(target);
      if (node == null) return;
      controller.laserNodeId = node.getUUID();
      controller.laserNode = node;
      node.refresh(controller.laserX + .5, controller.laserDepth + .5, controller.laserZ + .5);
      target.addFreshEntity(node);
      controller.setChanged();
    } else {
      controller.laserNode = node;
      node.refresh(controller.laserX + .5, controller.laserDepth + .5, controller.laserZ + .5);
    }
  }

  static void clearLights(ServerLevel level, int x, int z) {
    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
      for (int dx = -1; dx <= 1; dx++)
        for (int dz = -1; dz <= 1; dz++) {
          BlockPos pos = new BlockPos(x + dx, y, z + dz);
          if (level.getBlockState(pos).is(OrbitalRegistry.LASER_LIGHT.get()))
            level.removeBlock(pos, false);
        }
  }

  private static boolean mineTerrain(OrbitalBlockEntity controller, ServerLevel stationLevel) {
    GalaxyData galaxy = GalaxyData.get(stationLevel.getServer());
    var station = StationLogic.at(galaxy, stationLevel, controller.getBlockPos());
    Planet planet = station == null ? null : galaxy.planets.get(station.orbitPlanet);
    if (planet == null || !planet.landable()) {
      controller.laserRunning = false;
      clearLights(controller);
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.no_landable_planet_below_the_laser");
      return true;
    }
    if (!planet.laserDrillable) {
      controller.laserRunning = false;
      clearLights(controller);
      controller.status =
          Component.translatable(
              "status.adv_rocketry.orbital_laser.orbital_laser_is_disabled_above_this");
      return true;
    }
    ServerLevel target = PlanetRuntime.create(stationLevel.getServer(), planet);
    if (controller.laserDepth <= target.getMinBuildHeight()) {
      controller.laserRunning = false;
      clearLights(controller);
      controller.laserDepth = 0;
      controller.status =
          Component.translatable("status.adv_rocketry.orbital_laser.terrain_shaft_complete");
      return true;
    }
    int y = controller.laserDepth;
    ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        BlockPos pos = new BlockPos(controller.laserX + x, y, controller.laserZ + z);
        BlockState state = target.getBlockState(pos);
        if (state.isAir()) {
          target.setBlock(pos, OrbitalRegistry.LASER_LIGHT.get().defaultBlockState(), 3);
          continue;
        }
        if (state.getDestroySpeed(target, pos) < 0 || state.hasBlockEntity()) continue;
        BlockEvent.BreakEvent event =
            new BlockEvent.BreakEvent(target, pos, state, FakePlayerFactory.getMinecraft(target));
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) continue;
        List<ItemStack> drops =
            state.getDrops(
                new LootParams.Builder(target)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, tool));
        if (!canInsertOutput(controller, drops)) return false;
        if (!target.destroyBlock(pos, false)) continue;
        target.setBlock(pos, OrbitalRegistry.LASER_LIGHT.get().defaultBlockState(), 3);
        for (ItemStack drop : drops) insertOutput(controller, drop);
      }
    controller.laserDepth--;
    return true;
  }

  private static boolean canInsertOutput(OrbitalBlockEntity controller, List<ItemStack> drops) {
    ItemStackHandler simulated = new ItemStackHandler(8);
    int offset = 0;
    List<PortBlockEntity> outputs = outputs(controller);
    if (outputs.size() < 2) return false;
    for (PortBlockEntity port : outputs) {
      for (int slot = 0; slot < port.inventory.getSlots(); slot++)
        simulated.setStackInSlot(offset++, port.inventory.getStackInSlot(slot).copy());
    }
    for (ItemStack drop : drops) {
      ItemStack remaining = drop.copy();
      for (int slot = 0; slot < offset && !remaining.isEmpty(); slot++)
        remaining = simulated.insertItem(slot, remaining, false);
      if (!remaining.isEmpty()) return false;
    }
    return true;
  }

  private static ItemStack output(ServerLevel level, Planet planet) {
    if (level.random.nextInt(10) != 0) return new ItemStack(Blocks.COBBLESTONE, 5);
    List<ItemStack> ores = orePalette(planet);
    return ores.isEmpty()
        ? new ItemStack(Blocks.COBBLESTONE, 5)
        : ores.get(level.random.nextInt(ores.size())).copy();
  }

  @SubscribeEvent
  public static void tagsUpdated(TagsUpdatedEvent event) {
    PALETTES.clear();
  }

  static List<ItemStack> orePalette(Planet planet) {
    Palette cached = PALETTES.get(planet.id);
    if (cached != null && cached.planetOres().equals(planet.laserOres)) return cached.ores();
    List<ItemStack> ores = new ArrayList<>();
    Set<ResourceLocation> selected = new LinkedHashSet<>();
    for (var item : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.LASER_DRILL_ORES))
      addOre(ores, selected, new ItemStack(item));
    for (IdOrTag entry : planet.laserOres) addOre(ores, selected, entry.stack());
    PALETTES.put(planet.id, new Palette(List.copyOf(planet.laserOres), List.copyOf(ores)));
    return List.copyOf(ores);
  }

  private static void addOre(
      List<ItemStack> ores, Set<ResourceLocation> selected, ItemStack stack) {
    if (!stack.isEmpty() && selected.add(BuiltInRegistries.ITEM.getKey(stack.getItem())))
      ores.add(stack);
  }

  private static boolean insertOutput(OrbitalBlockEntity controller, ItemStack yield) {
    ItemStack remaining = yield.copy();
    for (PortBlockEntity port : outputs(controller)) {
      for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
        remaining = port.inventory.insertItem(slot, remaining, false);
        if (remaining.isEmpty()) return true;
      }
    }
    return false;
  }
}

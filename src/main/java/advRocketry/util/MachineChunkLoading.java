package advRocketry.util;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.orbit.ForceFieldProjectorBlock;
import advRocketry.orbit.FormedState;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.StationLogic;
import advRocketry.orbit.WirelessTransceiverBlockEntity;
import advRocketry.space.GalaxyData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Persistent, independently owned tickets for active orbital infrastructure and its service ports.
 */
public final class MachineChunkLoading {
  private record Lease(String system, Set<ChunkPos> chunks) {}

  private static final Map<ServerLevel, Map<BlockPos, Lease>> ACTIVE = new HashMap<>();
  private static final Map<String, TicketController> CONTROLLERS = new HashMap<>();

  static {
    for (String system : new String[] {"orbitalMachines", "elevators", "wireless"}) {
      CONTROLLERS.put(
          system,
          new TicketController(
              ResourceLocation.fromNamespaceAndPath(
                  Main.MODID, "machines_" + system.toLowerCase(Locale.ROOT)),
              (level, helper) -> restore(level, helper, system)));
    }
  }

  private MachineChunkLoading() {}

  public static void register(RegisterTicketControllersEvent event) {
    CONTROLLERS.values().forEach(event::register);
  }

  private static void restore(ServerLevel level, TicketHelper helper, String system) {
    var owners = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
    helper
        .getBlockTickets()
        .forEach(
            (owner, tickets) -> {
              if (!AdvancedRocketryConfig.chunkLoading(system)) {
                helper.removeAllTickets(owner);
                return;
              }
              Set<ChunkPos> chunks = new HashSet<>();
              tickets.ticking().forEach((long chunk) -> chunks.add(new ChunkPos(chunk)));
              // This controller never creates non-ticking or entity tickets.
              tickets
                  .nonTicking()
                  .forEach((long chunk) -> helper.removeTicket(owner, chunk, false));
              if (!chunks.isEmpty()) owners.put(owner, new Lease(system, chunks));
            });
    helper.getEntityTickets().keySet().forEach(helper::removeAllTickets);
  }

  public static void stopped(ServerStoppedEvent event) {
    ACTIVE.keySet().removeIf(level -> level.getServer() == event.getServer());
  }

  public static void tick(MinecraftServer server) {
    for (var entry : new ArrayList<>(ACTIVE.entrySet())) {
      ServerLevel level = entry.getKey();
      if (level.getServer() != server) continue;
      for (BlockPos pos : new ArrayList<>(entry.getValue().keySet())) {
        if (!level.hasChunkAt(pos)) continue;
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) remove(level, pos);
        else update(entity);
      }
    }
  }

  public static void refresh(BlockEntity entity) {
    if (entity.getLevel() instanceof ServerLevel level
        && Math.floorMod(level.getGameTime() + entity.getBlockPos().hashCode(), 20) == 0)
      update(entity);
  }

  public static void update(BlockEntity entity) {
    if (!(entity.getLevel() instanceof ServerLevel level)) return;
    BlockPos owner = entity.getBlockPos().immutable();
    Set<ChunkPos> wanted = new HashSet<>();
    if (!entity.isRemoved() && eligible(entity)) {
      // Adjacent connections can straddle a chunk border.
      add(wanted, owner);
      if (entity instanceof OrbitalBlockEntity orbital) {
        if (orbital.forceFieldProjector()) {
          var facing = orbital.getBlockState().getValue(ForceFieldProjectorBlock.FACING);
          for (int distance = 1; distance <= 32; distance++)
            add(wanted, owner.relative(facing, distance));
        }
        String id =
            switch (orbital.kind()) {
              case BLACK_HOLE_GENERATOR -> "black_hole_generator";
              case SOLAR_ARRAY -> "solar_array";
              case MICROWAVE_RECEIVER -> "microwave_receiver";
              case SPACE_ELEVATOR -> "space_elevator";
              case ORBITAL_LASER -> "orbital_laser";
              case RAILGUN -> "railgun";
              case ATMOSPHERE_TERRAFORMER -> "atmosphere_terraformer";
              case ASTROBODY_PROCESSOR -> "astrobody_data_processor";
              case WARP_CORE -> "warp_core";
              case BUILDER -> "satellite_builder";
              default -> null;
            };
        if (id != null) {
          var structure = PlacedMultiblock.of(orbital, id);
          if (structure != null) for (var cell : structure.cells()) add(wanted, cell.pos());
        }
      }
    }
    String system =
        entity instanceof WirelessTransceiverBlockEntity
            ? "wireless"
            : entity instanceof OrbitalBlockEntity b && b.spaceElevator()
                ? "elevators"
                : "orbitalMachines";
    var owners = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
    Lease previous = owners.get(owner);
    if (previous != null && !previous.system().equals(system)) {
      remove(level, owner);
      previous = null;
    }
    Set<ChunkPos> old = previous == null ? Set.of() : previous.chunks();
    TicketController tickets = CONTROLLERS.get(system);
    for (ChunkPos chunk : old)
      if (!wanted.contains(chunk)) tickets.forceChunk(level, owner, chunk.x, chunk.z, false, true);
    for (ChunkPos chunk : wanted)
      if (!old.contains(chunk)) tickets.forceChunk(level, owner, chunk.x, chunk.z, true, true);
    if (wanted.isEmpty()) owners.remove(owner);
    else owners.put(owner, new Lease(system, wanted));
  }

  private static void add(Set<ChunkPos> chunks, BlockPos pos) {
    for (int x : new int[] {-1, 1})
      for (int z : new int[] {-1, 1}) chunks.add(new ChunkPos(pos.offset(x, 0, z)));
  }

  private static boolean eligible(BlockEntity entity) {
    if (entity instanceof WirelessTransceiverBlockEntity wireless)
      return AdvancedRocketryConfig.chunkLoading("wireless")
          && wireless.mode() != WirelessTransceiverBlockEntity.Mode.OFF
          && wireless.network() != 0;
    if (!(entity instanceof OrbitalBlockEntity b)) return false;
    if (b.spaceElevator())
      return AdvancedRocketryConfig.chunkLoading("elevators")
          && !b.elevatorDimension.isEmpty()
          && FormedState.check(b);
    if (!AdvancedRocketryConfig.chunkLoading("orbitalMachines")) return false;
    return switch (b.kind()) {
      case SOLAR_ARRAY, BLACK_HOLE_GENERATOR, WARP_CORE, ASTROBODY_PROCESSOR ->
          FormedState.check(b);
      case MICROWAVE_RECEIVER -> !b.inventory.getStackInSlot(0).isEmpty() && FormedState.check(b);
      case RAILGUN -> !b.inventory.getStackInSlot(0).isEmpty() && FormedState.check(b);
      case ORBITAL_LASER -> b.laser.running && !b.laser.jammed;
      case ATMOSPHERE_TERRAFORMER -> b.terraformingEnabled && FormedState.check(b);
      case BUILDER -> b.builderProgress > 0;
      case WARP_CONTROLLER -> b.discoveryTicks > 0;
      case STATION_ALTITUDE_CONTROLLER,
              STATION_ORIENTATION_CONTROLLER,
              STATION_GRAVITY_CONTROLLER ->
          StationLogic.at(
                  GalaxyData.get(((ServerLevel) b.getLevel()).getServer()),
                  b.getLevel(),
                  b.getBlockPos())
              != null;
      case FORCE_FIELD_PROJECTOR ->
          b.fieldLength > 0 || b.getLevel().hasNeighborSignal(b.getBlockPos());
      default -> false;
    };
  }

  public static void remove(ServerLevel level, BlockPos owner) {
    var owners = ACTIVE.get(level);
    if (owners == null) return;
    var lease = owners.remove(owner);
    if (lease != null)
      for (ChunkPos chunk : lease.chunks())
        CONTROLLERS.get(lease.system()).forceChunk(level, owner, chunk.x, chunk.z, false, true);
  }
}

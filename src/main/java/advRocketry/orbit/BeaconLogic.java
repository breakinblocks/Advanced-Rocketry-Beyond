// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.life.SpaceSuitItem;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.PacketDistributor;

/** Original beacon column and persistent per-planet navigation marks. */
public final class BeaconLogic {
  private BeaconLogic() {}

  public static boolean complete(OrbitalBlockEntity beacon) {
    return beacon.beacon() && PlacedMultiblock.complete(beacon, "beacon");
  }

  public static void tick(OrbitalBlockEntity beacon) {
    if (!(beacon.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    ResourceLocation dimension = level.dimension().location();
    var positions = galaxy.beacons.computeIfAbsent(dimension, key -> new LinkedHashSet<>());
    if (beacon.beaconEnabled && complete(beacon) && galaxy.planet(level).id >= 0) {
      if (positions.add(beacon.getBlockPos().asLong())) galaxy.setDirty();
    } else if (positions.remove(beacon.getBlockPos().asLong())) galaxy.setDirty();
  }

  public static void remove(OrbitalBlockEntity beacon) {
    if (!(beacon.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    var positions = galaxy.beacons.get(level.dimension().location());
    if (positions != null && positions.remove(beacon.getBlockPos().asLong())) galaxy.setDirty();
  }

  public static BlockPos nearest(ServerLevel level, BlockPos origin) {
    return active(level).stream().min(Comparator.comparingDouble(origin::distSqr)).orElse(null);
  }

  public static List<BlockPos> active(ServerLevel level) {
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Planet planet = galaxy.planet(level);
    if (planet.id < 0) return List.of();
    var positions = galaxy.beacons.get(level.dimension().location());
    if (positions == null) return List.of();
    return positions.stream()
        .map(BlockPos::of)
        .filter(
            pos ->
                !level.hasChunkAt(pos)
                    || level.getBlockEntity(pos) instanceof OrbitalBlockEntity beacon
                        && beacon.beaconEnabled
                        && complete(beacon))
        .toList();
  }

  public static void sendFinders(MinecraftServer server) {
    for (var player : server.getPlayerList().getPlayers()) {
      var helmet = player.getItemBySlot(EquipmentSlot.HEAD);
      if (helmet.getItem() instanceof SpaceSuitItem suit
          && suit.countModule(helmet, "beacon_finder") > 0
          && player.level() instanceof ServerLevel level)
        PacketDistributor.sendToPlayer(
            player, new BeaconFinderItem.Payload(level.dimension().location(), active(level)));
    }
  }
}

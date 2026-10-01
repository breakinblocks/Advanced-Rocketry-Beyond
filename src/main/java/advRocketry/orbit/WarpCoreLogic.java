// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** The original three-layer warp-core frame and its station fuel input. */
public final class WarpCoreLogic {
  public static final int MAX_FUEL = 1000;
  private static final TagKey<Item> DILITHIUM =
      ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/dilithium"));

  private WarpCoreLogic() {}

  public static boolean complete(OrbitalBlockEntity core) {
    return core.warpCore() && PlacedMultiblock.complete(core, "warp_core");
  }

  public static PortBlockEntity input(OrbitalBlockEntity core) {
    if (!complete(core)) return null;
    return PlacedMultiblock.of(core, "warp_core").entity('I', PortBlockEntity.class);
  }

  public static int stationFuel(OrbitalBlockEntity core) {
    if (!(core.getLevel() instanceof ServerLevel level)) return 0;
    Station station = StationLogic.at(level, core.getBlockPos());
    return station == null ? 0 : station.fuel;
  }

  public static void refuel(OrbitalBlockEntity core) {
    if (!(core.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, core.getBlockPos());
    if (station == null) return;
    boolean formed = complete(core);
    register(galaxy, station, core.getBlockPos(), formed);
    if (!formed) return;
    PortBlockEntity input = input(core);
    if (input == null) return;
    int points = AdvancedRocketryConfig.fuelPointsPerDilithium();
    if (points <= 0) return;
    int fuel = station.fuel;
    for (int slot = 0; slot < input.inventory.getSlots() && fuel <= MAX_FUEL - points; slot++) {
      while (fuel <= MAX_FUEL - points && input.inventory.getStackInSlot(slot).is(DILITHIUM)) {
        input.inventory.extractItem(slot, 1, false);
        fuel += points;
      }
    }
    int refueled = fuel;
    if (refueled != station.fuel)
      StationLogic.update(galaxy, station, record -> record.fuel = refueled);
  }

  private static void register(
      GalaxyData galaxy, Station station, BlockPos position, boolean formed) {
    long[] positions = station.warpCores;
    long packed = position.asLong();
    int index = -1;
    for (int i = 0; i < positions.length; i++)
      if (positions[i] == packed) {
        index = i;
        break;
      }
    if (formed && index < 0) {
      long[] updated = Arrays.copyOf(positions, positions.length + 1);
      updated[positions.length] = packed;
      StationLogic.update(galaxy, station, record -> record.warpCores = updated);
    } else if (!formed && index >= 0) {
      long[] updated = new long[positions.length - 1];
      System.arraycopy(positions, 0, updated, 0, index);
      System.arraycopy(positions, index + 1, updated, index, positions.length - index - 1);
      StationLogic.update(galaxy, station, record -> record.warpCores = updated);
    }
  }

  public static void unregister(OrbitalBlockEntity core) {
    if (!(core.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Station station = StationLogic.at(galaxy, level, core.getBlockPos());
    if (station != null) register(galaxy, station, core.getBlockPos(), false);
  }
}

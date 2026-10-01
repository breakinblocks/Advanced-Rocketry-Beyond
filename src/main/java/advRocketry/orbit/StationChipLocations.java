// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** Per-planet destinations and the protected last departure from the original station chip. */
public final class StationChipLocations {
  public static final int LIMIT = 128;

  public record Location(String name, BlockPos pos) {
    public static final Codec<Location> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("name").forGetter(Location::name),
                        BlockPos.CODEC.fieldOf("position").forGetter(Location::pos))
                    .apply(instance, Location::new));
    public static final StreamCodec<ByteBuf, Location> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Location::name,
            BlockPos.STREAM_CODEC,
            Location::pos,
            Location::new);

    public Location {
      pos = pos.immutable();
    }
  }

  private StationChipLocations() {}

  private static StationDestinations destinations(ItemStack chip) {
    return chip.getOrDefault(ModComponents.STATION_DESTINATIONS, StationDestinations.EMPTY);
  }

  public static List<Location> list(ItemStack chip, int planet) {
    var entries = destinations(chip).route(planet).locations();
    List<Location> result = new ArrayList<>();
    for (int i = 0; i < Math.min(entries.size(), LIMIT); i++) {
      var entry = entries.get(i);
      result.add(i == 0 ? new Location("Last", entry.pos()) : entry);
    }
    return result;
  }

  public static int selection(ItemStack chip, int planet) {
    int selected = destinations(chip).route(planet).selected();
    return selected >= 0 && selected < list(chip, planet).size() ? selected : 0;
  }

  public static Location destination(ItemStack chip, int planet) {
    var locations = list(chip, planet);
    return locations.isEmpty() ? null : locations.get(selection(chip, planet));
  }

  private static void save(ItemStack chip, int planet, List<Location> locations, int selected) {
    chip.set(
        ModComponents.STATION_DESTINATIONS,
        destinations(chip)
            .with(
                planet,
                new StationDestinations.Route(
                    locations, selected >= 0 && selected < locations.size() ? selected : 0)));
  }

  public static void remember(ItemStack chip, int planet, BlockPos pos) {
    var locations = list(chip, planet);
    var last = new Location("Last", pos.immutable());
    if (locations.isEmpty()) locations.add(last);
    else locations.set(0, last);
    save(chip, planet, locations, selection(chip, planet));
  }

  public static boolean select(ItemStack chip, int planet, int index) {
    var locations = list(chip, planet);
    if (index < 0 || index >= locations.size()) return false;
    save(chip, planet, locations, index);
    return true;
  }

  public static boolean add(ItemStack chip, int planet, BlockPos pos, String name) {
    var locations = list(chip, planet);
    if (locations.size() >= LIMIT) return false;
    // A chip used before its first flight still needs a protected Last entry.
    if (locations.isEmpty()) locations.add(new Location("Last", pos.immutable()));
    String label = name.strip().replaceAll("[\\p{Cntrl}]", "");
    if (label.length() > 32) label = label.substring(0, 32);
    locations.add(new Location(label, pos.immutable()));
    save(chip, planet, locations, selection(chip, planet));
    return true;
  }

  public static boolean delete(ItemStack chip, int planet) {
    var locations = list(chip, planet);
    int selected = selection(chip, planet);
    if (selected == 0 || locations.isEmpty()) return false;
    locations.remove(selected);
    save(chip, planet, locations, 0);
    return true;
  }

  public static boolean clear(ItemStack chip, int planet) {
    var locations = list(chip, planet);
    if (locations.size() <= 1) return false;
    save(chip, planet, locations.subList(0, 1), 0);
    return true;
  }
}

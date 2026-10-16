package advRocketry.orbit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StationDestinations(Map<Integer, Route> planets) {
  public static final int MAX_PLANETS = 256;
  public static final StationDestinations EMPTY = new StationDestinations(Map.of());
  private static final Codec<Integer> PLANET_KEY =
      Codec.STRING.comapFlatMap(StationDestinations::planetKey, String::valueOf);
  public static final Codec<StationDestinations> CODEC =
      Codec.unboundedMap(PLANET_KEY, Route.CODEC)
          .validate(map -> map.size() <= MAX_PLANETS ? DataResult.success(map)
              : DataResult.error(() -> "Too many station destination planets"))
          .xmap(StationDestinations::new, StationDestinations::planets);
  public static final StreamCodec<ByteBuf, StationDestinations> STREAM_CODEC =
      ByteBufCodecs.<ByteBuf, Integer, Route, Map<Integer, Route>>map(
              HashMap::new, ByteBufCodecs.INT, Route.STREAM_CODEC, MAX_PLANETS)
          .map(StationDestinations::new, StationDestinations::planets);

  public StationDestinations {
    planets = Map.copyOf(planets);
  }

  private static DataResult<Integer> planetKey(String key) {
    try {
      return DataResult.success(Integer.parseInt(key));
    } catch (NumberFormatException exception) {
      return DataResult.error(() -> "Invalid planet id " + key);
    }
  }

  public Route route(int planet) {
    return planets.getOrDefault(planet, Route.EMPTY);
  }

  public StationDestinations with(int planet, Route route) {
    if (!canStore(planet)) return this;
    Map<Integer, Route> updated = new HashMap<>(planets);
    updated.put(planet, route);
    return new StationDestinations(updated);
  }

  public boolean canStore(int planet) {
    return planets.containsKey(planet) || planets.size() < MAX_PLANETS;
  }

  public record Route(List<StationChipLocations.Location> locations, int selected) {
    public static final Route EMPTY = new Route(List.of(), 0);
    public static final Codec<Route> CODEC =
        RecordCodecBuilder.<Route>create(
            instance ->
                instance
                    .group(
                        StationChipLocations.Location.CODEC
                            .listOf(0, StationChipLocations.LIMIT)
                            .fieldOf("locations")
                            .forGetter(Route::locations),
                        Codec.intRange(0, StationChipLocations.LIMIT - 1).fieldOf("selected").forGetter(Route::selected))
                    .apply(instance, Route::new));
    public static final StreamCodec<ByteBuf, Route> STREAM_CODEC =
        StreamCodec.composite(
            StationChipLocations.Location.STREAM_CODEC.apply(ByteBufCodecs.list(StationChipLocations.LIMIT)),
            Route::locations,
            ByteBufCodecs.VAR_INT,
            Route::selected,
            Route::new);

    public Route {
      locations = List.copyOf(locations);
      selected = selected >= 0 && selected < Math.max(1, locations.size()) ? selected : 0;
    }
  }
}

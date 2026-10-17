package advRocketry.orbit;

import java.util.ArrayList;
import java.util.List;

/** State owned by one orbital hologram controller. */
public final class HologramState {
  public static final int MAX_SIZE = 400;
  public static final float MAX_RADIUS = 8;

  public static int clampSize(int size) {
    return Math.clamp(size, 0, MAX_SIZE);
  }

  public void resize(boolean increase) {
    int current = clampSize(size);
    int step = (increase ? current >= 100 : current > 100) ? 50 : 10;
    size = clampSize(current + (increase ? step : -step));
  }

  public int target = -1;
  public int selectedStar = -1;
  public final List<HologramBodyEntity> bodies = new ArrayList<>();
  public int center = -1;
  public int star;
  public int size = 20;
  public int redstone;
}

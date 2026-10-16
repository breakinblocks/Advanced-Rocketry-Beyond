package advRocketry.orbit;

import java.util.ArrayList;
import java.util.List;

/** State owned by one orbital hologram controller. */
public final class HologramState {
  public int target = -1;
  public int selectedStar = -1;
  public final List<HologramBodyEntity> bodies = new ArrayList<>();
  public int center = -1;
  public int star;
  public int size = 20;
  public int redstone;
}

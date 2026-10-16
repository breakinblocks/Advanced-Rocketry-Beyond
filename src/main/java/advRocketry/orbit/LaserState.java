package advRocketry.orbit;

import java.util.UUID;

/** State owned by one orbital laser controller. */
public final class LaserState {
  public int x;
  public int z;
  public int mode;
  public int ticks;
  public int depth;
  public UUID nodeId;
  public LaserNodeEntity node;
  public int targetPlanet = Integer.MIN_VALUE;
  public boolean terrain;
  public boolean running;
  public boolean jammed;
}

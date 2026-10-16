package advRocketry.orbit;


/** State owned by one orbital observation controller. */
public final class ObservationState {
  public int ticks;
  public int openProgress;
  public int candidate = -1;
  public int[] options = new int[0];
  public int optionIndex = -1;
  public int catalogHash;
  public long seed;
}

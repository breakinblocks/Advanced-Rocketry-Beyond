package advRocketry.transport;

/** Bounded no-progress scheduling. Owned by an endpoint, never by the whole network. */
final class TransportRetry {
  private int delay;
  private long next;

  boolean ready(long now) {
    return now >= next;
  }

  int delay() {
    return delay;
  }

  void wake() {
    delay = 0;
    next = 0;
  }

  void attempted(long now, int moved, int base, int cap) {
    if (moved > 0 || cap == 0) {
      wake();
      return;
    }
    delay = Math.min(cap, Math.max(base, delay * 2));
    next = now + delay;
  }
}

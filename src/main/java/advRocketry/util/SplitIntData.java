package advRocketry.util;

import java.util.function.IntUnaryOperator;
import net.minecraft.world.inventory.ContainerData;

public final class SplitIntData implements ContainerData {
  private final int values;
  private final IntUnaryOperator source;
  private final int[] received;

  private SplitIntData(int values, IntUnaryOperator source) {
    this.values = values;
    this.source = source;
    this.received = source == null ? new int[values * 2] : null;
  }

  public static SplitIntData server(int values, IntUnaryOperator source) {
    return new SplitIntData(values, source);
  }

  public static SplitIntData client(int values) {
    return new SplitIntData(values, null);
  }

  public static int value(ContainerData data, int index) {
    return (data.get(index * 2) & 65535) | (data.get(index * 2 + 1) & 65535) << 16;
  }

  public int value(int index) {
    return value(this, index);
  }

  @Override
  public int get(int index) {
    if (received != null) return received[index];
    int value = source.applyAsInt(index / 2);
    return index % 2 == 0 ? value & 65535 : value >>> 16;
  }

  @Override
  public void set(int index, int value) {
    if (received != null) received[index] = value;
  }

  @Override
  public int getCount() {
    return values * 2;
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/** Original infrastructure input/output modes and per-face redstone routing. */
public final class InfrastructureRedstone {
  public enum Mode {
    ON,
    OFF,
    INVERTED;

    public boolean active(boolean condition) {
      return this == INVERTED ? !condition : this == ON && condition;
    }
  }

  private Mode input = Mode.OFF;
  private Mode output = Mode.ON;
  private final int[] sides = new int[6];

  public int value(int index) {
    return index == 0 ? input.ordinal() : index == 1 ? output.ordinal() : sides[index - 2];
  }

  public void cycle(int index) {
    if (index == 0) input = Mode.values()[(input.ordinal() + 1) % 3];
    else if (index == 1) output = Mode.values()[(output.ordinal() + 1) % 3];
    else if (index >= 2 && index < 8) sides[index - 2] = (sides[index - 2] + 1) % 3;
  }

  public boolean permits(Level level, BlockPos pos) {
    if (input == Mode.OFF) return true;
    boolean powered = false;
    for (Direction side : Direction.values())
      if (sides[side.ordinal()] == 2 && level.getSignal(pos.relative(side), side) > 0)
        powered = true;
    return input.active(powered);
  }

  public boolean output(boolean condition) {
    return output.active(condition);
  }

  public boolean outputsTo(Direction querySide) {
    return sides[querySide.getOpposite().ordinal()] == 1;
  }

  public void save(CompoundTag tag) {
    tag.putInt("redstone_input", input.ordinal());
    tag.putInt("redstone_output", output.ordinal());
    tag.putIntArray("redstone_sides", sides);
  }

  public void load(CompoundTag tag) {
    if (tag.contains("redstone_input")) input = mode(tag.getInt("redstone_input"));
    if (tag.contains("redstone_output")) output = mode(tag.getInt("redstone_output"));
    int[] saved = tag.getIntArray("redstone_sides");
    for (int i = 0; i < sides.length; i++)
      sides[i] = i < saved.length && saved[i] >= 0 && saved[i] < 3 ? saved[i] : 0;
  }

  private static Mode mode(int value) {
    return value >= 0 && value < Mode.values().length ? Mode.values()[value] : Mode.OFF;
  }
}

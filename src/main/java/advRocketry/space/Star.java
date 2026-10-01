// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Stellar properties from the original StellarBody and DimensionManager defaults. */
public record Star(
    int id,
    String name,
    int temperature,
    float size,
    int x,
    int z,
    boolean blackHole,
    List<Companion> companions) {
  public Star {
    companions = List.copyOf(companions);
    validate(temperature, size);
  }

  public Star(int id, String name, int temperature, float size, int x, int z, boolean blackHole) {
    this(id, name, temperature, size, x, z, blackHole, List.of());
  }

  public record Companion(
      String name, int temperature, float size, boolean blackHole, float separation) {
    public Companion {
      validate(temperature, size);
      if (!Float.isFinite(separation) || separation < 0)
        throw new IllegalArgumentException("Star separation must be finite and nonnegative");
    }
  }

  private static void validate(int temperature, float size) {
    if (temperature < 0 || !Float.isFinite(size) || size <= 0)
      throw new IllegalArgumentException(
          "Star temperature must be nonnegative and size finite and positive");
  }

  /** Original StellarBody temperature-to-color curve, with all channels normalized. */
  public static float[] color(int temperature) {
    double heat = Math.max(1, temperature * .477 + 10);
    double red = heat < 66 ? 255 : 329.69 * Math.pow(heat - 60, -.1332);
    double green = heat < 66 ? 99.47 * Math.log(heat) - 161.1 : 288 * Math.pow(heat - 60, -.07551);
    double blue = heat > 67 ? 255 : heat <= 19 ? 0 : 138.51 * Math.log(heat - 10) - 305.04;
    return new float[] {
      (float) Math.clamp(red / 255, 0, 1),
      (float) Math.clamp(green / 255, 0, 1),
      (float) Math.clamp(blue / 255, 0, 1)
    };
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("id", id);
    tag.putString("name", name);
    tag.putInt("temperature", temperature);
    tag.putFloat("size", size);
    tag.putInt("x", x);
    tag.putInt("z", z);
    tag.putBoolean("black_hole", blackHole);
    ListTag children = new ListTag();
    for (Companion companion : companions) {
      CompoundTag child = new CompoundTag();
      child.putString("name", companion.name());
      child.putInt("temperature", companion.temperature());
      child.putFloat("size", companion.size());
      child.putBoolean("black_hole", companion.blackHole());
      child.putFloat("separation", companion.separation());
      children.add(child);
    }
    tag.put("companions", children);
    return tag;
  }

  public static Star load(CompoundTag tag) {
    List<Companion> companions = new ArrayList<>();
    for (Tag entry : tag.getList("companions", Tag.TAG_COMPOUND)) {
      CompoundTag child = (CompoundTag) entry;
      companions.add(
          new Companion(
              child.getString("name"),
              child.getInt("temperature"),
              child.getFloat("size"),
              child.getBoolean("black_hole"),
              child.getFloat("separation")));
    }
    return new Star(
        tag.getInt("id"),
        tag.getString("name"),
        tag.getInt("temperature"),
        tag.getFloat("size"),
        tag.getInt("x"),
        tag.getInt("z"),
        tag.getBoolean("black_hole"),
        companions);
  }
}

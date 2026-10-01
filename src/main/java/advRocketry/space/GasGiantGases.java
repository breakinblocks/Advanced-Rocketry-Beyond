// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.DataMaps;
import advRocketry.ModTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Original gas-giant fluid choices, gravity bounds and independent spawn chances. */
public final class GasGiantGases {
  private GasGiantGases() {}

  public static List<ResourceLocation> select(float gravity, Random random) {
    List<ResourceLocation> selected = new ArrayList<>();
    var gases =
        BuiltInRegistries.FLUID.getDataMap(DataMaps.GAS_GIANT_GAS).entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
            .toList();
    for (var entry : gases) {
      DataMaps.GasGiantGas gas = entry.getValue();
      if (gravity >= gas.minGravity()
          && gravity <= gas.maxGravity()
          && gas.chance() > 0
          && random.nextDouble() < gas.chance()) selected.add(entry.getKey().location());
    }
    return selected;
  }

  public static List<ResourceLocation> harvestable(Planet giant) {
    Set<ResourceLocation> available = new LinkedHashSet<>(giant.gases);
    for (var fluid : BuiltInRegistries.FLUID.getTagOrEmpty(ModTags.HARVESTABLE_GASES))
      fluid.unwrapKey().ifPresent(key -> available.add(key.location()));
    return List.copyOf(available);
  }
}

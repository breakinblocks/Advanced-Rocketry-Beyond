// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

/** Original ore dictionary names resolved through the native common material tags. */
public final class LegacyOreEntries {
  private static final String[][] PREFIXES = {
    {"ingot", "ingots"},
    {"nugget", "nuggets"},
    {"plate", "plates"},
    {"sheet", "sheets"},
    {"stick", "rods"},
    {"rod", "rods"},
    {"dust", "dusts"},
    {"gear", "gears"},
    {"fan", "fans"},
    {"boule", "boules"},
    {"block", "storage_blocks"},
    {"ore", "ores"},
    {"coil", "coils"},
    {"gem", "gems"},
    {"dye", "dyes"}
  };

  private LegacyOreEntries() {}

  public static ResourceLocation oreDictionary(String name) {
    String special =
        switch (name) {
          case "itemBattery" -> "c:batteries";
          case "paneGlass", "paneGlassColorless" -> "c:glass_panes";
          case "blockGlass" -> "c:glass_blocks";
          case "slabWood" -> "minecraft:wooden_slabs";
          case "logWood" -> "minecraft:logs";
          case "plankWood" -> "minecraft:planks";
          case "blockMotor" -> "adv_rocketry:motors";
          case "lensPrecisionLaserEtcher" -> "c:lenses/precision_laser_etcher";
          case "oreRutile" -> "c:ores/titanium";
          default -> null;
        };
    if (special != null) return ResourceLocation.parse(special);
    for (String[] prefix : PREFIXES)
      if (name.startsWith(prefix[0]) && name.length() > prefix[0].length()) {
        String material =
            name.substring(prefix[0].length())
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
        return ResourceLocation.fromNamespaceAndPath("c", prefix[1] + "/" + material);
      }
    return null;
  }
}

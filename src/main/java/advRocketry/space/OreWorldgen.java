// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native ore features use the original per-ore count and clump settings at generation time. */
public final class OreWorldgen {
  private static final DeferredRegister<Feature<?>> FEATURES =
      DeferredRegister.create(Registries.FEATURE, Main.MODID);
  private static final DeferredRegister<PlacementModifierType<?>> MODIFIERS =
      DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Main.MODID);
  static final Supplier<PlacementModifierType<ConfigurableCount>> ORE_COUNT =
      MODIFIERS.register("ore_count", () -> () -> ConfigurableCount.CODEC);
  public static final List<String> NAMES =
      List.of("tin", "aluminum", "titanium", "dilithium", "iridium");
  private static final Map<String, Supplier<ConfigurableOre>> ORES = new LinkedHashMap<>();

  static {
    for (String name : NAMES)
      ORES.put(name, FEATURES.register(name + "_ore", () -> new ConfigurableOre(name)));
  }

  private OreWorldgen() {}

  public static ConfigurableOre feature(String name) {
    return ORES.get(name).get();
  }

  public static ConfigurableCount count(String name) {
    return new ConfigurableCount(name);
  }

  public static void register(IEventBus bus) {
    FEATURES.register(bus);
    MODIFIERS.register(bus);
  }

  public static final class ConfigurableOre extends OreFeature {
    private final String name;

    ConfigurableOre(String name) {
      super(OreConfiguration.CODEC);
      this.name = name;
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
      if (!AdvancedRocketryConfig.oreEnabled(name)) return false;
      OreConfiguration original = context.config();
      OreConfiguration current =
          new OreConfiguration(
              original.targetStates,
              AdvancedRocketryConfig.oreClumpSize(name),
              original.discardChanceOnAirExposure);
      return Feature.ORE.place(
          new FeaturePlaceContext<>(
              context.topFeature(),
              context.level(),
              context.chunkGenerator(),
              context.random(),
              context.origin(),
              current));
    }
  }

  public static final class ConfigurableCount extends PlacementModifier {
    static final MapCodec<ConfigurableCount> CODEC =
        Codec.STRING.fieldOf("ore").xmap(ConfigurableCount::new, value -> value.ore);
    private final String ore;

    ConfigurableCount(String ore) {
      this.ore = ore;
    }

    @Override
    public Stream<BlockPos> getPositions(
        PlacementContext context, RandomSource random, BlockPos origin) {
      return IntStream.range(0, AdvancedRocketryConfig.orePerChunk(ore, false))
          .mapToObj(index -> origin);
    }

    @Override
    public PlacementModifierType<?> type() {
      return ORE_COUNT.get();
    }
  }
}

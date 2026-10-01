package advRocketry.multiblock;

import advRocketry.DataRegistries;
import advRocketry.Main;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class Multiblocks {
  private Multiblocks() {}

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
  }

  public static Multiblock get(RegistryAccess registries, ResourceLocation id) {
    return registries.registryOrThrow(DataRegistries.MULTIBLOCK).get(id);
  }

  public static Multiblock get(Level level, String path) {
    return get(level.registryAccess(), id(path));
  }

  public static Multiblock require(Level level, String path) {
    Multiblock multiblock = get(level, path);
    if (multiblock == null) throw new IllegalStateException("Missing multiblock " + id(path));
    return multiblock;
  }

  public static List<ResourceLocation> ids(RegistryAccess registries) {
    return registries.registryOrThrow(DataRegistries.MULTIBLOCK).entrySet().stream()
        .sorted(
            Comparator.comparingInt(
                    (Map.Entry<ResourceKey<Multiblock>, Multiblock> entry) ->
                        entry.getValue().order())
                .thenComparing(entry -> entry.getKey().location()))
        .map(entry -> entry.getKey().location())
        .toList();
  }

  public static Component title(ResourceLocation id) {
    return BuiltInRegistries.BLOCK
        .getOptional(id)
        .map(Block::getName)
        .orElseGet(() -> Component.literal(id.toString()));
  }

  public static ResourceLocation forController(RegistryAccess registries, Block block) {
    for (var entry : registries.registryOrThrow(DataRegistries.MULTIBLOCK).entrySet())
      if (entry.getValue().controller() == block) return entry.getKey().location();
    return null;
  }
}

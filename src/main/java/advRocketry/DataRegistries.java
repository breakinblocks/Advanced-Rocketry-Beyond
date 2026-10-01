package advRocketry;

import advRocketry.multiblock.Multiblock;
import advRocketry.orbit.AsteroidCatalog;
import advRocketry.space.PlanetDefinition;
import advRocketry.space.PlanetOres;
import advRocketry.space.StarDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class DataRegistries {
  public static final ResourceKey<Registry<AsteroidCatalog.Definition>> ASTEROID = key("asteroid");
  public static final ResourceKey<Registry<StarDefinition>> STAR = key("star");
  public static final ResourceKey<Registry<PlanetDefinition>> PLANET = key("planet");
  public static final ResourceKey<Registry<PlanetOres.Profile>> ORE_PROFILE = key("ore_profile");
  public static final ResourceKey<Registry<Multiblock>> MULTIBLOCK = key("multiblock");

  private DataRegistries() {}

  private static <T> ResourceKey<Registry<T>> key(String name) {
    return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Main.MODID, name));
  }

  public static void register(IEventBus bus) {
    bus.addListener(DataRegistries::registries);
  }

  private static void registries(DataPackRegistryEvent.NewRegistry event) {
    event.dataPackRegistry(
        ASTEROID, AsteroidCatalog.Definition.CODEC, AsteroidCatalog.Definition.CODEC);
    event.dataPackRegistry(STAR, StarDefinition.CODEC);
    event.dataPackRegistry(PLANET, PlanetDefinition.CODEC);
    event.dataPackRegistry(ORE_PROFILE, PlanetOres.Profile.CODEC);
    event.dataPackRegistry(MULTIBLOCK, Multiblock.CODEC, Multiblock.CODEC);
  }
}

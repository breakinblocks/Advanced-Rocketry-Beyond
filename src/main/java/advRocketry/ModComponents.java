package advRocketry;

import advRocketry.life.JetpackSettings;
import advRocketry.orbit.AsteroidResearch;
import advRocketry.orbit.AsteroidTarget;
import advRocketry.orbit.BiomeSelection;
import advRocketry.orbit.LinkTarget;
import advRocketry.orbit.ProgrammedPlanet;
import advRocketry.orbit.SatelliteLink;
import advRocketry.orbit.SatelliteProperties;
import advRocketry.orbit.StationDestinations;
import advRocketry.orbit.StationLink;
import advRocketry.orbit.StoredResearch;
import advRocketry.processing.ProjectorChoice;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {
  private static final DeferredRegister.DataComponents COMPONENTS =
      DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Main.MODID);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>>
      TANK_CONTENT =
          register("tank_content", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SEALED_AIR =
      register("sealed_air", Codec.INT, ByteBufCodecs.VAR_INT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<SatelliteProperties>>
      SATELLITE =
          register("satellite", SatelliteProperties.CODEC, SatelliteProperties.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<SatelliteLink>>
      SATELLITE_LINK = register("satellite_link", SatelliteLink.CODEC, SatelliteLink.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<StationLink>>
      STATION_LINK = register("station_link", StationLink.CODEC, StationLink.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<StationDestinations>>
      STATION_DESTINATIONS =
          register(
              "station_destinations", StationDestinations.CODEC, StationDestinations.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProgrammedPlanet>>
      PLANET = register("planet", ProgrammedPlanet.CODEC, ProgrammedPlanet.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<AsteroidTarget>>
      ASTEROID = register("asteroid", AsteroidTarget.CODEC, AsteroidTarget.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<AsteroidResearch>>
      ASTEROID_RESEARCH =
          register("asteroid_research", AsteroidResearch.CODEC, AsteroidResearch.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredResearch>>
      STORED_RESEARCH =
          register("stored_research", StoredResearch.CODEC, StoredResearch.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkTarget>> LINK =
      register("link", LinkTarget.CODEC, LinkTarget.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<BiomeSelection>>
      BIOME_SELECTION =
          register("biome_selection", BiomeSelection.CODEC, BiomeSelection.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProjectorChoice>>
      PROJECTOR_CHOICE =
          register("projector_choice", ProjectorChoice.CODEC, ProjectorChoice.STREAM_CODEC);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<JetpackSettings>>
      JETPACK = register("jetpack", JetpackSettings.CODEC, JetpackSettings.STREAM_CODEC);

  private ModComponents() {}

  private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
      String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
    return COMPONENTS.registerComponentType(
        name, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
  }

  public static boolean blank(ItemStack stack) {
    return COMPONENTS.getEntries().stream().noneMatch(holder -> stack.has(holder.get()));
  }

  public static void register(IEventBus bus) {
    COMPONENTS.register(bus);
  }
}

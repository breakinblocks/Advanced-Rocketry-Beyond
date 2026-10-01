package advRocketry;

import advRocketry.client.model.ConnectedModelLoader;
import advRocketry.compat.guideme.RocketryGuide;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.SuitMenu;
import advRocketry.life.SuitWorkstationMenu;
import advRocketry.life.client.JetpackClient;
import advRocketry.life.client.JetpackLayer;
import advRocketry.life.client.OxygenScreen;
import advRocketry.life.client.SpaceHelmetLayer;
import advRocketry.life.client.SuitHud;
import advRocketry.life.client.SuitScreen;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.client.ElevatorCapsuleRenderer;
import advRocketry.orbit.client.HologramBodyRenderer;
import advRocketry.orbit.client.LaserNodeRenderer;
import advRocketry.orbit.client.LaserNodeSounds;
import advRocketry.orbit.client.OrbitalRenderer;
import advRocketry.orbit.client.OrbitalScreen;
import advRocketry.orbit.client.OreScanScreen;
import advRocketry.orbit.client.RailgunCargoRenderer;
import advRocketry.orbit.client.StationChipScreen;
import advRocketry.processing.HoloProjectorItem;
import advRocketry.processing.LaserBeamSync;
import advRocketry.processing.client.LaserBeamRenderer;
import advRocketry.processing.client.ProcessingClient;
import advRocketry.processing.client.ProjectorScreen;
import advRocketry.processing.client.StructurePreview;
import advRocketry.rocket.client.HovercraftClient;
import advRocketry.rocket.client.HovercraftRenderer;
import advRocketry.rocket.client.RocketClient;
import advRocketry.space.client.PlanetEffects;
import advRocketry.transport.TransportRegistry;
import advRocketry.transport.client.TransportScreen;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Main.MODID, dist = Dist.CLIENT)
public final class MainClient {
  public MainClient(IEventBus bus, ModContainer container) {
    container.registerConfig(ModConfig.Type.CLIENT, AdvancedRocketryConfig.CLIENT_SPEC);
    if (ModList.get().isLoaded("guideme")) RocketryGuide.register(bus);
    LaserBeamSync.client = LaserBeamRenderer::receive;
    HoloProjectorItem.openScreen = ProjectorScreen::open;
    HoloProjectorItem.preview = StructurePreview::select;
    bus.addListener(MainClient::screens);
    bus.addListener(MainClient::renderers);
    bus.addListener(MainClient::reloadListeners);
    bus.addListener(JetpackLayer::register);
    bus.addListener(ConnectedModelLoader::register);
    bus.<EntityRenderersEvent.RegisterLayerDefinitions>addListener(SpaceHelmetLayer::definitions);
    bus.<EntityRenderersEvent.AddLayers>addListener(SpaceHelmetLayer::register);
    bus.addListener(RocketClient::layers);
    bus.addListener(SuitHud::register);
    bus.addListener(RocketClient::renderers);
    bus.addListener(RocketClient::keys);
    bus.addListener(HovercraftClient::renderers);
    bus.addListener(HovercraftClient::keys);
    bus.addListener(JetpackClient::registerKeys);
    bus.addListener(PlanetEffects::register);
    bus.addListener(ProcessingClient::registerRenderers);
    bus.addListener(ProcessingClient::registerItemColors);
    bus.addListener(ProcessingClient::registerBlockColors);
    bus.addListener(ProcessingClient::registerReloadListener);
    bus.addListener(ProcessingClient::registerFluidExtensions);
    NeoForge.EVENT_BUS.addListener(LaserBeamRenderer::render);
    NeoForge.EVENT_BUS.addListener(RocketClient::controls);
    NeoForge.EVENT_BUS.addListener(RocketClient::tick);
    NeoForge.EVENT_BUS.addListener(LaserNodeSounds::tick);
    NeoForge.EVENT_BUS.addListener(HovercraftClient::controls);
    NeoForge.EVENT_BUS.addListener(JetpackClient::controls);
    NeoForge.EVENT_BUS.addListener(JetpackClient::tick);
    NeoForge.EVENT_BUS.addListener(PlanetEffects::fog);
    NeoForge.EVENT_BUS.addListener(PlanetEffects::disconnected);
    NeoForge.EVENT_BUS.addListener(StructurePreview::render);
    NeoForge.EVENT_BUS.addListener(StructurePreview::scroll);
    NeoForge.EVENT_BUS.addListener(StructurePreview::loggingOut);
  }

  private static void screens(RegisterMenuScreensEvent event) {
    event.register(TransportRegistry.MENU.get(), TransportScreen::new);
    event.register(OrbitalRegistry.MENU.get(), OrbitalScreen::new);
    event.register(OrbitalRegistry.ORE_SCAN_MENU.get(), OreScanScreen::new);
    event.register(OrbitalRegistry.STATION_CHIP_MENU.get(), StationChipScreen::new);
    event.register(LifeSupportRegistry.OXYGEN_MENU.get(), OxygenScreen::new);
    event.register(LifeSupportRegistry.SUIT_MENU.get(), SuitScreen<SuitMenu>::new);
    event.register(
        LifeSupportRegistry.SUIT_WORKSTATION_MENU.get(), SuitScreen<SuitWorkstationMenu>::new);
    RocketClient.screens(event);
    ProcessingClient.registerScreens(event);
  }

  private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(OrbitalRegistry.HOLOGRAM_BODY.get(), HologramBodyRenderer::new);
    event.registerBlockEntityRenderer(OrbitalRegistry.BLOCK_ENTITY.get(), OrbitalRenderer::new);
    event.registerEntityRenderer(
        OrbitalRegistry.ELEVATOR_CAPSULE.get(), ElevatorCapsuleRenderer::new);
    event.registerEntityRenderer(OrbitalRegistry.LASER_NODE.get(), LaserNodeRenderer::new);
    event.registerEntityRenderer(OrbitalRegistry.RAILGUN_CARGO.get(), RailgunCargoRenderer::new);
  }

  private static void reloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(
        (ResourceManagerReloadListener)
            manager -> {
              OrbitalRenderer.clearModels();
              ElevatorCapsuleRenderer.clearModel();
              HovercraftRenderer.clearModel();
              JetpackLayer.clearModel();
            });
  }
}

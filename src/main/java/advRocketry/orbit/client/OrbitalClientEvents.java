package advRocketry.orbit.client;

import advRocketry.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public final class OrbitalClientEvents {
  private OrbitalClientEvents() {}

  @SubscribeEvent
  public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
    LaserNodeSounds.clear();
  }
}

package advRocketry.life.client;

import advRocketry.client.effects.EffectProfiles;
import advRocketry.rocket.RocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Camera-relative pitch ladder and measured altitude/vertical-speed tapes. */
public final class FlightInstruments {
  private static RocketEntity trackedRocket;
  private static double previousAltitude;
  private static long previousTick;
  private static int verticalSpeed;

  public static void tick(ClientTickEvent.Post event) {
    var client = Minecraft.getInstance();
    if (client.level == null
        || client.player == null
        || !(client.player.getVehicle() instanceof RocketEntity rocket)) {
      trackedRocket = null;
      verticalSpeed = 0;
      return;
    }
    if (client.isPaused()) return;
    long now = client.level.getGameTime();
    if (rocket != trackedRocket || now < previousTick) {
      trackedRocket = rocket;
      previousAltitude = rocket.getY();
      previousTick = now;
      verticalSpeed = 0;
    } else if (now - previousTick >= 5) {
      // Rocket flight moves by setPos; vanilla deltaMovement is not its vertical velocity.
      double change = rocket.getY() - previousAltitude;
      verticalSpeed =
          Math.abs(change) > 64 ? 0 : (int) Math.round(change * 20 / (now - previousTick));
      previousAltitude = rocket.getY();
      previousTick = now;
    }
  }

  private FlightInstruments() {}

  public static void render(GuiGraphics graphics) {
    var client = Minecraft.getInstance();
    var profile = EffectProfiles.get("hud");
    if (!profile.enabled()
        || client.player == null
        || !client.options.getCameraType().isFirstPerson()
        || !(client.player.getVehicle() instanceof RocketEntity rocket)) return;
    int x = graphics.guiWidth() / 2, y = graphics.guiHeight() / 2;
    if (graphics.guiWidth() < 320 || graphics.guiHeight() < 200) return;
    int color = profile.argb(.8f);
    graphics.enableScissor(x - 95, y - 55, x + 95, y + 56);
    for (int pitch = -90; pitch <= 90; pitch += 10) {
      int line = y + Math.round((pitch - client.player.getXRot()) * 2);
      int width = pitch == 0 ? 40 : 24;
      graphics.hLine(x - width, x - 8, line, color);
      graphics.hLine(x + 8, x + width, line, color);
      if (line >= y - 50 && line <= y + 50)
        graphics.drawString(client.font, Integer.toString(-pitch), x + 45, line - 4, color, true);
    }
    graphics.disableScissor();
    graphics.hLine(x - 6, x + 6, y, color);
    graphics.vLine(x, y - 3, y + 3, color);
    int altitude = (int) Math.floor(rocket.getY());
    int speed = rocket == trackedRocket ? verticalSpeed : 0;
    graphics.drawString(
        client.font,
        Component.translatable("hud.adv_rocketry.flight_altitude", altitude),
        x - 90,
        y + 65,
        color,
        true);
    graphics.drawString(
        client.font,
        Component.translatable("hud.adv_rocketry.flight_vertical", speed),
        x + 15,
        y + 65,
        color,
        true);
  }
}

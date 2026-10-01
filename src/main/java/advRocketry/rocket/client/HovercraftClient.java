// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.rocket.HovercraftControl;
import advRocketry.rocket.HovercraftEntity;
import advRocketry.rocket.HovercraftRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class HovercraftClient {
  private static final KeyMapping UP =
      new KeyMapping(
          "key.adv_rocketry.hovercraft_up",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_Z,
          "key.categories.adv_rocketry");
  private static final KeyMapping DOWN =
      new KeyMapping(
          "key.adv_rocketry.hovercraft_down",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_X,
          "key.categories.adv_rocketry");

  private HovercraftClient() {}

  public static void keys(RegisterKeyMappingsEvent event) {
    event.register(UP);
    event.register(DOWN);
  }

  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(HovercraftRegistry.ENTITY.get(), HovercraftRenderer::new);
  }

  public static void controls(MovementInputUpdateEvent event) {
    if (!(event.getEntity().getVehicle() instanceof HovercraftEntity)) return;
    var input = event.getInput();
    boolean active = Minecraft.getInstance().screen == null;
    PacketDistributor.sendToServer(
        new HovercraftControl(
            active ? input.forwardImpulse : 0,
            active ? -input.leftImpulse : 0,
            active && (UP.isDown() || input.jumping),
            active && DOWN.isDown()));
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.life.JetpackControl;
import advRocketry.life.JetpackToggle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Sends held jump input only while a jetpack-equipped chest suit is worn. */
public final class JetpackClient {
  private static final KeyMapping TOGGLE =
      new KeyMapping(
          "key.adv_rocketry.toggle_jetpack",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_X,
          "key.categories.adv_rocketry");

  private JetpackClient() {}

  public static void registerKeys(RegisterKeyMappingsEvent event) {
    event.register(TOGGLE);
  }

  public static void tick(ClientTickEvent.Post event) {
    while (TOGGLE.consumeClick()) {
      var player = Minecraft.getInstance().player;
      if (player != null)
        PacketDistributor.sendToServer(new JetpackToggle(player.isShiftKeyDown()));
    }
  }

  public static void controls(MovementInputUpdateEvent event) {
    var chest = event.getEntity().getItemBySlot(EquipmentSlot.CHEST);
    if (event.getInput().jumping && JetpackControl.enabled(chest))
      PacketDistributor.sendToServer(new JetpackControl());
  }
}

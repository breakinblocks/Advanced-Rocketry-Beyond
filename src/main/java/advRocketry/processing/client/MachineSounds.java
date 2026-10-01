// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.Main;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.processing.ProcessingRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Native counterpart of LibVulpes' machine-bound repeating sound. */
@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public final class MachineSounds {
  private static final Map<ProcessingBlockEntity, MachineLoop> SOUNDS = new HashMap<>();

  private MachineSounds() {}

  public static void tick(ProcessingBlockEntity machine) {
    if (!machine.running() || SOUNDS.containsKey(machine)) return;
    var client = Minecraft.getInstance();
    if (client.options.getSoundSourceVolume(SoundSource.BLOCKS) == 0
        || client.options.getSoundSourceVolume(SoundSource.MASTER) == 0) return;
    var sound = ProcessingRegistry.MACHINE_SOUNDS.get(machine.machine());
    if (sound == null) return;
    var loop = new MachineLoop(machine, sound.get());
    SOUNDS.put(machine, loop);
    Minecraft.getInstance().getSoundManager().play(loop);
  }

  public static void clear() {
    var manager = Minecraft.getInstance().getSoundManager();
    SOUNDS.values().forEach(manager::stop);
    SOUNDS.clear();
  }

  @SubscribeEvent
  public static void cleanup(ClientTickEvent.Post event) {
    var client = Minecraft.getInstance();
    SOUNDS
        .entrySet()
        .removeIf(
            entry -> {
              var machine = entry.getKey();
              var loop = entry.getValue();
              if (client.level == null
                  || machine.isRemoved()
                  || machine.getLevel() != client.level
                  || !machine.running()
                  || loop.isStopped()
                  || !client.getSoundManager().isActive(loop)
                      && client.level.getGameTime() - loop.createdAt > 5) {
                client.getSoundManager().stop(loop);
                return true;
              }
              return false;
            });
  }

  private static final class MachineLoop extends AbstractTickableSoundInstance {
    private final ProcessingBlockEntity machine;
    private final long createdAt;

    MachineLoop(ProcessingBlockEntity machine, SoundEvent event) {
      super(event, SoundSource.BLOCKS, RandomSource.create());
      this.machine = machine;
      createdAt = machine.getLevel().getGameTime();
      looping = true;
      delay = 0;
      volume = 1;
      x = machine.getBlockPos().getX() + .5;
      y = machine.getBlockPos().getY() + .5;
      z = machine.getBlockPos().getZ() + .5;
    }

    @Override
    public void tick() {
      if (machine.isRemoved()
          || !machine.running()
          || machine.getLevel() != Minecraft.getInstance().level) stop();
    }
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.orbit.LaserNodeEntity;
import advRocketry.orbit.OrbitalRegistry;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Plays one moving loop per active target node, as in the original orbital drill. */
public final class LaserNodeSounds {
  private static final Map<LaserNodeEntity, LaserSound> ACTIVE = new IdentityHashMap<>();

  private LaserNodeSounds() {}

  public static void tick(ClientTickEvent.Post event) {
    Minecraft minecraft = Minecraft.getInstance();
    ACTIVE
        .entrySet()
        .removeIf(
            entry ->
                entry.getValue().isStopped()
                    || entry.getKey().isRemoved()
                    || entry.getKey().level() != minecraft.level);
    if (minecraft.level == null) return;
    for (var entity : minecraft.level.entitiesForRendering()) {
      if (!(entity instanceof LaserNodeEntity node) || ACTIVE.containsKey(node)) continue;
      LaserSound sound = new LaserSound(node);
      ACTIVE.put(node, sound);
      minecraft.getSoundManager().play(sound);
    }
  }

  public static void clear() {
    ACTIVE.clear();
  }

  private static final class LaserSound extends AbstractTickableSoundInstance {
    private final LaserNodeEntity node;

    LaserSound(LaserNodeEntity node) {
      super(OrbitalRegistry.LASER_SOUND.get(), SoundSource.NEUTRAL, RandomSource.create());
      this.node = node;
      looping = true;
      volume = 1f;
      pitch = .7f;
      tick();
    }

    @Override
    public void tick() {
      if (node.isRemoved() || Minecraft.getInstance().level != node.level()) {
        stop();
        return;
      }
      x = node.getX();
      y = node.getY();
      z = node.getZ();
    }
  }
}

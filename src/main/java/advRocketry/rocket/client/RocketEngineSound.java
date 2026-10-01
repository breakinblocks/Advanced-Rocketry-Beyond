// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Follows an active rocket and stops the original engine recording when thrust ends. */
final class RocketEngineSound extends AbstractTickableSoundInstance {
  private final RocketEntity rocket;

  RocketEngineSound(RocketEntity rocket) {
    super(RocketRegistry.ENGINE_SOUND.get(), SoundSource.NEUTRAL, RandomSource.create());
    this.rocket = rocket;
    looping = true;
    volume = .8f;
    tick();
  }

  static boolean active(RocketEntity rocket) {
    return rocket.enginesActive();
  }

  @Override
  public void tick() {
    if (rocket.isRemoved() || Minecraft.getInstance().level != rocket.level() || !active(rocket)) {
      stop();
      return;
    }
    x = rocket.getX();
    y = rocket.getY();
    z = rocket.getZ();
    pitch = rocket.flight() == 1 ? 1.05f : .9f;
  }
}

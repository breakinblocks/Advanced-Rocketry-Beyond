// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket.client;

import advRocketry.client.effects.EffectProfiles;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Follows an active rocket and stops the original engine recording when thrust ends. */
final class RocketEngineSound extends AbstractTickableSoundInstance {
  private final RocketEntity rocket;
  private float spool;

  RocketEngineSound(RocketEntity rocket) {
    super(RocketRegistry.ENGINE_SOUND.get(), SoundSource.NEUTRAL, RandomSource.create());
    this.rocket = rocket;
    looping = true;
    volume = .02f;
    tick();
  }

  static boolean active(RocketEntity rocket) {
    return rocket.enginesActive();
  }

  @Override
  public void tick() {
    var profile = EffectProfiles.get("audio");
    if (rocket.isRemoved()
        || Minecraft.getInstance().level != rocket.level()
        || !profile.enabled()) {
      stop();
      return;
    }
    x = rocket.getX();
    y = rocket.getY();
    z = rocket.getZ();
    spool = Math.clamp(spool + (active(rocket) ? .05f : -.08f), 0, 1);
    if (spool <= 0) {
      stop();
      return;
    }
    volume = .85f * spool;
    pitch = .65f + .35f * spool;
  }
}

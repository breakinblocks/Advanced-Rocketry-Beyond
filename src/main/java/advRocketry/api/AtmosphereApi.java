// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.api;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import net.minecraft.server.level.ServerLevel;

public final class AtmosphereApi {
    private AtmosphereApi() {}

    public static boolean hasAtmosphere(ServerLevel level) {
        Planet planet = GalaxyData.get(level.getServer()).find(level);
        return planet != null && planet.atmosphere > 0;
    }
}

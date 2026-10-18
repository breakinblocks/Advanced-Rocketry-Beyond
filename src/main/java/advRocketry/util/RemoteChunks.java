package advRocketry.util;

import advRocketry.AdvancedRocketryConfig;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

/** Short leases for remote work; renewed only while requested and never saved across restarts. */
public final class RemoteChunks {
  private static final TicketType<String> TICKET =
      TicketType.create("advanced_rocketry_remote", Comparator.<String>naturalOrder(), 60);

  private RemoteChunks() {}

  public static boolean ready(
      ServerLevel level, BlockPos pos, int radius, String system, String owner) {
    boolean enabled = AdvancedRocketryConfig.chunkLoading(system);
    boolean loaded = true;
    int minX = (pos.getX() - radius) >> 4, maxX = (pos.getX() + radius) >> 4;
    int minZ = (pos.getZ() - radius) >> 4, maxZ = (pos.getZ() + radius) >> 4;
    for (int x = minX; x <= maxX; x++)
      for (int z = minZ; z <= maxZ; z++) {
        ChunkPos chunk = new ChunkPos(x, z);
        if (enabled)
          level.getChunkSource().addRegionTicket(TICKET, chunk, 2, system + ":" + owner, true);
        loaded &= level.hasChunk(x, z);
      }
    return loaded;
  }
}

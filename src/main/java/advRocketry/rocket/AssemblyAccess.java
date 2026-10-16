package advRocketry.rocket;

import advRocketry.util.AssemblyFailure;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class AssemblyAccess {
  private AssemblyAccess() {}

  public static void checkBreak(ServerLevel level, List<BlockPos> positions, ServerPlayer player) {
    for (BlockPos pos : positions)
      if (player == null || !level.hasChunkAt(pos) || !level.mayInteract(player, pos)
          || !player.mayBuild()
          || NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player)).isCanceled())
        throw new AssemblyFailure(Component.translatable(
            "message.adv_rocketry.station_assembly.station_packing_is_not_allowed_at", pos.toShortString()));
  }
}

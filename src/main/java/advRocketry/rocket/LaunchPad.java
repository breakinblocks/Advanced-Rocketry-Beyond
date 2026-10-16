package advRocketry.rocket;

import advRocketry.processing.MachinePorts;
import advRocketry.util.AssemblyFailure;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/** Shared loaded-only launch-pad and tower bounds for rockets and station modules. */
public record LaunchPad(BlockPos origin, int width, int depth, int height) {
  public static LaunchPad scan(ServerLevel level, BlockPos builder) {
    BlockPos pad = null;
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos candidate = builder.relative(direction).below();
      if (pad(level, candidate)) {
        pad = candidate;
        break;
      }
    }
    if (pad == null) throw failure("place_the_builder_beside_a_launch");
    Set<BlockPos> pads = new HashSet<>();
    ArrayDeque<BlockPos> pending = new ArrayDeque<>();
    pending.add(pad);
    while (!pending.isEmpty()) {
      BlockPos pos = pending.removeFirst();
      if (pads.contains(pos) || !pad(level, pos)) continue;
      pads.add(pos);
      if (pads.size() > 256) throw failure("launch_pad_exceeds_16_by_16");
      for (Direction direction : Direction.Plane.HORIZONTAL) pending.add(pos.relative(direction));
    }
    int minX = pads.stream().mapToInt(BlockPos::getX).min().orElseThrow();
    int maxX = pads.stream().mapToInt(BlockPos::getX).max().orElseThrow();
    int minZ = pads.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
    int maxZ = pads.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
    int width = maxX - minX + 1, depth = maxZ - minZ + 1;
    if (width > 16 || depth > 16 || pads.size() != width * depth)
      throw failure("launch_pad_must_be_a_solid");
    int height = 0;
    for (int x = minX - 1; x <= maxX + 1; x++)
      for (int z = minZ - 1; z <= maxZ + 1; z++) {
        if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) continue;
        int tower = 0;
        while (tower < 64) {
          BlockPos pos = new BlockPos(x, pad.getY() + 1 + tower, z);
          if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(MachinePorts.STRUCTURE_TOWER.get())) break;
          tower++;
        }
        height = Math.max(height, tower);
      }
    if (height < 4) throw failure("a_structure_tower_at_least_four");
    return new LaunchPad(new BlockPos(minX, pad.getY() + 1, minZ), width, depth, height);
  }

  private static boolean pad(ServerLevel level, BlockPos pos) {
    return level.hasChunkAt(pos)
        && level.getBlockState(pos).is(RocketRegistry.PARTS.get("launch_pad").get());
  }

  private static AssemblyFailure failure(String reason) {
    return new AssemblyFailure(Component.translatable("message.adv_rocketry.rocket_assembly." + reason));
  }
}

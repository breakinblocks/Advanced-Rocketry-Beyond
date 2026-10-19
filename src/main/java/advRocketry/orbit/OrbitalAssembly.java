package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.MachinePartBlock;
import advRocketry.processing.MultiblockMachine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Reversible visual ownership of supports and ports beneath formed orbital models. */
public final class OrbitalAssembly {
  private static final Map<Level, Map<BlockPos, BlockPos>> OWNERS = new WeakHashMap<>();

  private OrbitalAssembly() {}

  public static void update(OrbitalBlockEntity controller, boolean formed) {
    Level level = controller.getLevel();
    if (level == null || level.isClientSide) return;
    var owners = OWNERS.computeIfAbsent(level, ignored -> new HashMap<>());
    Set<BlockPos> target = new HashSet<>();
    String id =
        controller.observatory()
            ? "observatory"
            : controller.blackHoleGenerator()
                ? "black_hole_generator"
                : controller.biomeScanner()
                    ? "biome_scanner"
                    : controller.warpCore()
                        ? "warp_core"
                        : controller.orbitalLaser()
                            ? "orbital_laser"
                            : controller.spaceElevator() ? "space_elevator" : null;
    if (id == null) return;
    var structure = PlacedMultiblock.of(controller, id);
    if (formed && structure != null) {
      if (!structure.loaded()) return;
      for (var cell : structure.cells()) {
        BlockPos pos = cell.pos();
        if (pos.equals(controller.getBlockPos()) || MultiblockMachine.isPartClaimed(level, pos))
          continue;
        BlockPos owner = owners.get(pos);
        if (owner != null && !owner.equals(controller.getBlockPos()) && isClaimed(level, pos))
          continue;
        if (level.getBlockState(pos).hasProperty(MachinePartBlock.ASSEMBLED)) target.add(pos);
      }
    }
    owners
        .entrySet()
        .removeIf(
            entry -> {
              if (!entry.getValue().equals(controller.getBlockPos())
                  || target.contains(entry.getKey())) return false;
              setHidden(level, entry.getKey(), false);
              return true;
            });
    for (BlockPos pos : target) {
      owners.put(pos, controller.getBlockPos());
      setHidden(level, pos, true);
    }
  }

  private static void setHidden(Level level, BlockPos pos, boolean hidden) {
    if (!level.isLoaded(pos)) return;
    var state = level.getBlockState(pos);
    if (!state.hasProperty(MachinePartBlock.ASSEMBLED)) return;
    if (state.getValue(MachinePartBlock.ASSEMBLED) != hidden)
      level.setBlock(pos, state.setValue(MachinePartBlock.ASSEMBLED, hidden), Block.UPDATE_CLIENTS);
    if (hidden) level.scheduleTick(pos, state.getBlock(), 40);
  }

  public static boolean isClaimed(Level level, BlockPos pos) {
    BlockPos owner = OWNERS.getOrDefault(level, Map.of()).get(pos);
    if (owner == null) return false;
    if (!level.isLoaded(owner)) return true;
    return level.getBlockEntity(owner) instanceof OrbitalBlockEntity controller
        && (controller.blackHoleGenerator()
            || controller.observatory()
            || controller.biomeScanner()
            || controller.warpCore()
            || controller.orbitalLaser()
            || controller.spaceElevator())
        && controller.formed;
  }
}

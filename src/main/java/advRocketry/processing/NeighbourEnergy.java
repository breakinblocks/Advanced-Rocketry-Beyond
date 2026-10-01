package advRocketry.processing;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

final class NeighbourEnergy {
  private final BlockEntity owner;
  private BlockCapabilityCache<IEnergyStorage, Direction>[] caches;

  NeighbourEnergy(BlockEntity owner) {
    this.owner = owner;
  }

  @SuppressWarnings("unchecked")
  void load() {
    if (caches != null || !(owner.getLevel() instanceof ServerLevel level)) return;
    BlockCapabilityCache<IEnergyStorage, Direction>[] created =
        new BlockCapabilityCache[Direction.values().length];
    for (Direction side : Direction.values())
      created[side.ordinal()] =
          BlockCapabilityCache.create(
              Capabilities.EnergyStorage.BLOCK,
              level,
              owner.getBlockPos().relative(side),
              side.getOpposite());
    caches = created;
  }

  void push(IEnergyStorage source) {
    load();
    if (caches == null) return;
    for (BlockCapabilityCache<IEnergyStorage, Direction> cache : caches) {
      int available = source.extractEnergy(Integer.MAX_VALUE, true);
      if (available <= 0) return;
      IEnergyStorage target = cache.getCapability();
      if (target == null || !target.canReceive()) continue;
      source.extractEnergy(target.receiveEnergy(available, false), false);
    }
  }
}

package advRocketry.orbit;

import advRocketry.multiblock.PlacedMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class BiomeScannerLogic {
  private BiomeScannerLogic() {}

  public static int scannedPlanet(OrbitalBlockEntity entity) {
    if (!entity.biomeScanner()
        || !(entity.getLevel() instanceof ServerLevel serverLevel)
        || !structure(entity)
        || !shaftClear(entity)) return -3;
    Station station = StationLogic.at(serverLevel, entity.getBlockPos());
    return station == null ? -3 : station.orbitPlanet;
  }

  public static boolean structure(OrbitalBlockEntity entity) {
    return entity.biomeScanner() && PlacedMultiblock.complete(entity, "biome_scanner");
  }

  private static boolean shaftClear(OrbitalBlockEntity entity) {
    for (int y = entity.getBlockPos().getY() - 4; y > entity.getLevel().getMinBuildHeight(); y--)
      if (!entity
          .getLevel()
          .isEmptyBlock(new BlockPos(entity.getBlockPos().getX(), y, entity.getBlockPos().getZ())))
        return false;
    return true;
  }
}

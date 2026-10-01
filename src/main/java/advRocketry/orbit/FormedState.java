package advRocketry.orbit;

import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public final class FormedState {
  private static final int INTERVAL = 20;

  private FormedState() {}

  public static boolean checked(OrbitalBlockEntity entity) {
    return rule(entity) != null;
  }

  public static boolean check(OrbitalBlockEntity entity) {
    Predicate<OrbitalBlockEntity> rule = rule(entity);
    return rule != null && rule.test(entity);
  }

  private static Predicate<OrbitalBlockEntity> rule(OrbitalBlockEntity entity) {
    return switch (((OrbitalBlock) entity.getBlockState().getBlock()).kind) {
      case BLACK_HOLE_GENERATOR -> BlackHoleGeneratorLogic::complete;
      case OBSERVATORY -> OrbitalBlockEntity::observatoryStructure;
      case BIOME_SCANNER -> BiomeScannerLogic::structure;
      case BEACON -> BeaconLogic::complete;
      case RAILGUN -> RailgunLogic::complete;
      case ASTROBODY_PROCESSOR -> AstrobodyProcessorLogic::complete;
      case ATMOSPHERE_TERRAFORMER -> TerraformerLogic::complete;
      case SPACE_ELEVATOR -> SpaceElevatorLogic::complete;
      case AREA_GRAVITY_CONTROLLER -> AreaGravityLogic::complete;
      case ORBITAL_LASER -> OrbitalLaserLogic::complete;
      case MICROWAVE_RECEIVER -> MicrowaveReceiverLogic::structure;
      case SOLAR_ARRAY -> FormedState::solarArray;
      case WARP_CORE -> WarpCoreLogic::complete;
      default -> null;
    };
  }

  private static boolean solarArray(OrbitalBlockEntity entity) {
    Component status = entity.status;
    boolean formed = SolarArrayLogic.panels(entity) >= 0;
    entity.status = status;
    return formed;
  }

  public static void tick(OrbitalBlockEntity entity) {
    if (!(entity.getLevel() instanceof ServerLevel level)
        || Math.floorMod(level.getGameTime() + entity.getBlockPos().hashCode(), INTERVAL) != 0)
      return;
    boolean formed = check(entity);
    if (formed == entity.formed) return;
    entity.formed = formed;
    entity.setChanged();
    level.sendBlockUpdated(
        entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
  }
}

package advRocketry.orbit;

/** Stable menu action identifiers shared by client widgets and server controls. */
public enum OrbitalAction {
  UNKNOWN(-1),
  BUILD(0),
  TRANSFER_DATA(1),
  NEXT_WARP_TARGET(2),
  WARP(3),
  GRAVITY_DOWN(4),
  GRAVITY_UP(5),
  OBSERVE(6),
  PROGRAM_ASTEROID(7),
  TRAVEL(8),
  AREA_GRAVITY_DOWN(9),
  AREA_GRAVITY_UP(10),
  AREA_RADIUS(11),
  AREA_DIRECTION(12),
  LASER_TOGGLE(13),
  LASER_X_DOWN(14),
  LASER_X_UP(15),
  LASER_Z_DOWN(16),
  LASER_Z_UP(17),
  LASER_TERRAIN(18),
  LASER_CLEAR_JAM(19),
  BEACON_TOGGLE(20),
  RAILGUN_MINIMUM_DOWN(21),
  RAILGUN_MINIMUM_UP(22),
  RAILGUN_REDSTONE(23),
  RESEARCH_COMPOSITION(24),
  RESEARCH_OPTICAL(25),
  RESEARCH_MASS(26),
  DISCOVER(27),
  IMPORT_PLANET(28),
  TERRAFORM_INCREASE(29),
  TERRAFORM_DECREASE(30),
  TERRAFORM_TOGGLE(31),
  ALTITUDE_DOWN(32),
  ALTITUDE_UP(33),
  ROTATION_X_DOWN(34),
  ROTATION_X_UP(35),
  ROTATION_Y_DOWN(36),
  ROTATION_Y_UP(37),
  ROTATION_Z_DOWN(38),
  ROTATION_Z_UP(39),
  ROTATION_RESET(40),
  SELECTOR_NEXT(41),
  SELECTOR_TARGET(42),
  SELECTOR_CENTER(43),
  SELECTOR_BACK(44),
  SELECTOR_STAR(45),
  SELECTOR_SIZE_UP(46),
  SELECTOR_SIZE_DOWN(47),
  SELECTOR_REDSTONE(48),
  COPY_CHIP(49),
  NEXT_ASTEROID(50);

  private final int id;
  private static final OrbitalAction[] BY_ID = new OrbitalAction[51];

  static {
    for (OrbitalAction action : values()) {
      if (action == UNKNOWN) continue;
      if (BY_ID[action.id] != null) throw new IllegalStateException("Duplicate orbital action id");
      BY_ID[action.id] = action;
    }
  }

  OrbitalAction(int id) {
    this.id = id;
  }

  public int id() {
    return id;
  }

  public static OrbitalAction fromId(int id) {
    return id >= 0 && id < BY_ID.length && BY_ID[id] != null ? BY_ID[id] : UNKNOWN;
  }
}

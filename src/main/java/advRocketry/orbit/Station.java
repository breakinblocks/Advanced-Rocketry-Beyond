package advRocketry.orbit;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public final class Station {
  public static final int DEFAULT_RADIUS = 128;
  public static final float DEFAULT_ALTITUDE = 4;
  private static final String[] AXES = {"x", "y", "z"};

  public long id;
  public boolean deployed;
  public Long attachedTo;
  public long deployedTime;
  public int orbitPlanet = -1;
  public int x;
  public int y;
  public int z;
  public int radius = DEFAULT_RADIUS;
  public float altitude = DEFAULT_ALTITUDE;
  public int targetAltitude = (int) DEFAULT_ALTITUDE;
  public Double gravity;
  public int targetGravity = 100;
  public boolean anchored;
  public final double[] rotationVelocity = new double[3];
  public final double[] rotationAngle = new double[3];
  public final int[] targetRotation = new int[3];
  public long rotationTime;
  public int fuel;
  public long[] warpCores = new long[0];
  public Integer warpTarget;
  public Long warpEta;
  public int warpSource;
  public int warpDestination;
  public int lastWarpCost;
  public long lastWarpTime;
  public int[] knownPlanets = new int[0];
  public final Map<Long, String> landingPads = new LinkedHashMap<>();
  public final Map<Long, String> dockingPorts = new LinkedHashMap<>();
  public CompoundTag structure;

  public boolean inWarp() {
    return warpEta != null;
  }

  public boolean free() {
    return deployed && attachedTo == null;
  }

  public boolean orbits(int planet) {
    return free() && orbitPlanet == planet;
  }

  public Station copy() {
    return load(save());
  }

  public CompoundTag save() {
    CompoundTag tag = snapshot();
    tag.putLong("deployed_time", deployedTime);
    tag.putInt("target_altitude", targetAltitude);
    tag.putInt("target_gravity", targetGravity);
    if (gravity != null) tag.putDouble("gravity", gravity);
    for (int axis = 0; axis < 3; axis++)
      tag.putInt("target_rotation_" + AXES[axis], targetRotation[axis]);
    tag.putInt("fuel", fuel);
    tag.putLongArray("warp_cores", warpCores);
    if (warpTarget != null) tag.putInt("warp_target", warpTarget);
    tag.putInt("last_warp_cost", lastWarpCost);
    tag.putLong("last_warp_time", lastWarpTime);
    tag.putIntArray("known_planets", knownPlanets);
    tag.put("landing_pads", positions(landingPads));
    tag.put("docking_ports", positions(dockingPorts));
    if (structure != null) tag.put("structure", structure.copy());
    return tag;
  }

  public CompoundTag snapshot() {
    CompoundTag tag = new CompoundTag();
    tag.putLong("station_id", id);
    tag.putBoolean("deployed", deployed);
    if (attachedTo != null) tag.putLong("attached_to", attachedTo);
    tag.putInt("station_x", x);
    tag.putInt("station_y", y);
    tag.putInt("station_z", z);
    tag.putInt("station_radius", radius);
    tag.putInt("orbit_planet", orbitPlanet);
    tag.putFloat("orbital_altitude", altitude);
    tag.putBoolean("anchored", anchored);
    if (warpEta != null) {
      tag.putLong("warp_eta", warpEta);
      tag.putInt("warp_source", warpSource);
      tag.putInt("warp_destination", warpDestination);
    }
    tag.putLong("rotation_time", rotationTime);
    for (int axis = 0; axis < 3; axis++) {
      tag.putDouble("rotation_angle_" + AXES[axis], rotationAngle[axis]);
      tag.putDouble("rotation_velocity_" + AXES[axis], rotationVelocity[axis]);
    }
    return tag;
  }

  public static Station load(CompoundTag tag) {
    Station station = new Station();
    station.id = tag.getLong("station_id");
    station.deployed = tag.getBoolean("deployed");
    if (tag.contains("attached_to")) station.attachedTo = tag.getLong("attached_to");
    station.deployedTime = tag.getLong("deployed_time");
    if (tag.contains("orbit_planet")) station.orbitPlanet = tag.getInt("orbit_planet");
    station.x = tag.getInt("station_x");
    station.y = tag.getInt("station_y");
    station.z = tag.getInt("station_z");
    if (tag.contains("station_radius")) station.radius = tag.getInt("station_radius");
    if (tag.contains("orbital_altitude")) station.altitude = tag.getFloat("orbital_altitude");
    if (tag.contains("target_altitude")) station.targetAltitude = tag.getInt("target_altitude");
    if (tag.contains("gravity")) station.gravity = tag.getDouble("gravity");
    if (tag.contains("target_gravity")) station.targetGravity = tag.getInt("target_gravity");
    station.anchored = tag.getBoolean("anchored");
    for (int axis = 0; axis < 3; axis++) {
      station.rotationVelocity[axis] = tag.getDouble("rotation_velocity_" + AXES[axis]);
      station.rotationAngle[axis] = tag.getDouble("rotation_angle_" + AXES[axis]);
      station.targetRotation[axis] = tag.getInt("target_rotation_" + AXES[axis]);
    }
    station.rotationTime = tag.getLong("rotation_time");
    station.fuel = tag.getInt("fuel");
    station.warpCores = tag.getLongArray("warp_cores");
    if (tag.contains("warp_target")) station.warpTarget = tag.getInt("warp_target");
    if (tag.contains("warp_eta")) {
      station.warpEta = tag.getLong("warp_eta");
      station.warpSource = tag.getInt("warp_source");
      station.warpDestination = tag.getInt("warp_destination");
    }
    station.lastWarpCost = tag.getInt("last_warp_cost");
    station.lastWarpTime = tag.getLong("last_warp_time");
    station.knownPlanets = tag.getIntArray("known_planets");
    read(tag.getCompound("landing_pads"), station.landingPads);
    read(tag.getCompound("docking_ports"), station.dockingPorts);
    if (tag.contains("structure")) station.structure = tag.getCompound("structure").copy();
    return station;
  }

  public boolean knows(int planet) {
    return Arrays.stream(knownPlanets).anyMatch(known -> known == planet);
  }

  private static CompoundTag positions(Map<Long, String> entries) {
    CompoundTag tag = new CompoundTag();
    entries.forEach((pos, value) -> tag.putString(Long.toString(pos), value));
    return tag;
  }

  private static void read(CompoundTag tag, Map<Long, String> entries) {
    for (String key : tag.getAllKeys()) entries.put(Long.parseLong(key), tag.getString(key));
  }
}

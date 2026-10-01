package advRocketry.processing;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.StringRepresentable;

public enum MachineType implements StringRepresentable {
  LATHE("lathe", "lathe"),
  ROLLING_MACHINE("rolling_machine", "rollingmachine"),
  CRYSTALLIZER("crystallizer", "crystallizer"),
  ELECTROLYZER("electrolyzer", "electrolyser"),
  CHEMICAL_REACTOR("chemical_reactor", "rollingmachine"),
  CUTTING_MACHINE("cutting_machine", "cuttingmachine"),
  PRECISION_ASSEMBLER("precision_assembler", "precass"),
  PRECISION_LASER_ETCHER("precision_laser_etcher", "lathe"),
  CENTRIFUGE("centrifuge", "electrolyser"),
  ELECTRIC_ARC_FURNACE("electric_arc_furnace", "electricarcfurnace"),
  PLATE_PRESS("plate_press", null);

  public static final Codec<MachineType> CODEC = StringRepresentable.fromEnum(MachineType::values);
  public static final List<MachineType> MULTIBLOCKS =
      Arrays.stream(values()).filter(MachineType::multiblock).toList();

  private final String id;
  private final String sound;

  MachineType(String id, String sound) {
    this.id = id;
    this.sound = sound;
  }

  public String id() {
    return id;
  }

  public String sound() {
    return sound;
  }

  public boolean multiblock() {
    return this != PLATE_PRESS;
  }

  public static MachineType byId(String id) {
    for (MachineType type : values()) if (type.id.equals(id)) return type;
    return null;
  }

  @Override
  public String getSerializedName() {
    return id;
  }
}

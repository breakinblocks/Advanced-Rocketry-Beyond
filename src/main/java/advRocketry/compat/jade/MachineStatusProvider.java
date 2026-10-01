package advRocketry.compat.jade;

import advRocketry.Main;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.util.ComponentText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum MachineStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  private static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "machine_status");

  @Override
  public ResourceLocation getUid() {
    return ID;
  }

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    var registries = accessor.getLevel().registryAccess();
    switch (accessor.getBlockEntity()) {
      case OrbitalBlockEntity orbital ->
          data.putString("status", Component.Serializer.toJson(orbital.status, registries));
      case RocketBlockEntity rocket -> {
        data.putString("status", Component.Serializer.toJson(rocket.status, registries));
        if (rocket.buildTotal > 0 && rocket.buildRemaining > 0)
          data.putInt(
              "progress", 100 * (rocket.buildTotal - rocket.buildRemaining) / rocket.buildTotal);
      }
      case ProcessingBlockEntity machine -> {
        data.putBoolean("formed", accessor.getBlockState().getValue(ProcessingBlock.FORMED));
        data.putBoolean("running", machine.running());
        if (machine.durationTicks() > 0)
          data.putInt("progress", 100 * machine.progressTicks() / machine.durationTicks());
      }
      case OxygenBlockEntity vent -> {
        data.putBoolean("running", vent.active);
        data.putInt("room", vent.room().size());
      }
      default -> {}
    }
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    var registries = accessor.getLevel().registryAccess();
    if (data.contains("formed") && !data.getBoolean("formed"))
      tooltip.add(Component.translatable("jade.adv_rocketry.unformed"));
    if (data.contains("status") && !data.getString("status").isEmpty())
      tooltip.add(ComponentText.read(data.getString("status"), registries));
    if (data.contains("running"))
      tooltip.add(
          Component.translatable(
              data.getBoolean("running") ? "jade.adv_rocketry.running" : "jade.adv_rocketry.idle"));
    if (data.contains("progress"))
      tooltip.add(Component.translatable("jade.adv_rocketry.progress", data.getInt("progress")));
    if (data.contains("room"))
      tooltip.add(Component.translatable("jade.adv_rocketry.room", data.getInt("room")));
  }
}

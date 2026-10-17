package advRocketry.compat.jade;

import advRocketry.Main;
import advRocketry.transport.TransportBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Tooltip snapshots never discover topology or inspect inventories. */
enum TransportStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;
  @Override public ResourceLocation getUid() { return ResourceLocation.fromNamespaceAndPath(Main.MODID, "transport_status"); }
  @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    if (!(accessor.getBlockEntity() instanceof TransportBlockEntity pipe)) return;
    data.putInt("nodes", pipe.networkSize); data.putInt("channel", pipe.channel);
    data.putInt("moved", accessor.getLevel().getGameTime() - pipe.lastTransferTick <= 20 ? pipe.lastTransferAmount : 0);
    data.putInt("blocked", pipe.blockedReason);
    var side = accessor.getSide(); var config = pipe.side(side);
    data.putInt("mode", pipe.modes[side.ordinal()]); data.putInt("priority", config.priority);
    data.putBoolean("filter", config.configured); data.putBoolean("void", config.disposal);
  }
  @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    var data = accessor.getServerData();
    tooltip.add(Component.literal("Network: " + data.getInt("nodes") + " pipes / Channel: " + data.getInt("channel")));
    tooltip.add(Component.literal("Priority: " + data.getInt("priority") + " / Last transfer: " + data.getInt("moved")));
    String[] reasons = {"Ready", "Retry delay", "No eligible destination", "No movement"};
    tooltip.add(Component.literal(reasons[Math.clamp(data.getInt("blocked"), 0, 3)]));
    if (data.getBoolean("filter")) tooltip.add(Component.literal("Side filter enabled"));
    if (data.getBoolean("void")) tooltip.add(Component.literal("Overflow disposal enabled"));
  }
}

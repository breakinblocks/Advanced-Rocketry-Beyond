package advRocketry.transport;

import advRocketry.Main;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;

@EventBusSubscriber(modid=Main.MODID,bus=EventBusSubscriber.Bus.MOD)
public final class ChangingEnergySink extends EnergyStorage {
  public static final Map<BlockPos,ChangingEnergySink> SINKS=new HashMap<>();
  public boolean accepting;
  public ChangingEnergySink() {super(100000);}
  @Override public int receiveEnergy(int amount,boolean simulate) {return simulate || accepting?super.receiveEnergy(amount,simulate):0;}
  @SubscribeEvent public static void register(RegisterCapabilitiesEvent event) {
    event.registerBlock(Capabilities.EnergyStorage.BLOCK,(level,pos,state,entity,side)->SINKS.get(pos),Blocks.EMERALD_BLOCK);
  }
}

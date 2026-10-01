package advRocketry.compat.jade;

import advRocketry.life.OxygenBlock;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.orbit.OrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketPartBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class RocketryJadePlugin implements IWailaPlugin {
  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(
        MachineStatusProvider.INSTANCE, OrbitalBlockEntity.class);
    registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, RocketBlockEntity.class);
    registration.registerBlockDataProvider(
        MachineStatusProvider.INSTANCE, ProcessingBlockEntity.class);
    registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, OxygenBlockEntity.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(MachineStatusProvider.INSTANCE, OrbitalBlock.class);
    registration.registerBlockComponent(MachineStatusProvider.INSTANCE, RocketPartBlock.class);
    registration.registerBlockComponent(MachineStatusProvider.INSTANCE, ProcessingBlock.class);
    registration.registerBlockComponent(MachineStatusProvider.INSTANCE, OxygenBlock.class);
  }
}

package advRocketry.compat.jade;

import advRocketry.life.OxygenBlock;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.multiblock.FormedMachineParts;
import advRocketry.orbit.OrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.processing.PortBlock;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketPartBlock;
import advRocketry.transport.TransportBlock;
import advRocketry.transport.TransportBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class RocketryJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TransportStatusProvider.INSTANCE, TransportBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, OrbitalBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, RocketBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, ProcessingBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatusProvider.INSTANCE, OxygenBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (!(accessor instanceof BlockAccessor block)) return accessor;
            var controller = FormedMachineParts.controllerAt(block.getLevel(), block.getPosition());
            if (controller == null) return accessor;
            return registration
                    .blockAccessor()
                    .from(block)
                    .hit(block.getHitResult().withPosition(controller.getBlockPos()))
                    .blockState(controller.getBlockState())
                    .blockEntity(controller)
                    .requireVerification()
                    .build();
        });
        registration.addTooltipCollectedCallback((box, accessor) -> {
            if (!(accessor instanceof BlockAccessor block)
                    || !(Minecraft.getInstance().hitResult instanceof BlockHitResult actual)
                    || actual.getBlockPos().equals(block.getPosition())) return;
            var level = block.getLevel();
            var target = level.getBlockState(actual.getBlockPos()).getBlock();
            if (target instanceof PortBlock
                    && FormedMachineParts.controllerAt(level, actual.getBlockPos()) == block.getBlockEntity())
                box.getTooltip().add(target.getName().withStyle(ChatFormatting.GRAY));
        });
        registration.registerBlockComponent(TransportStatusProvider.INSTANCE, TransportBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE, OrbitalBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE, RocketPartBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE, ProcessingBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE, OxygenBlock.class);
    }
}

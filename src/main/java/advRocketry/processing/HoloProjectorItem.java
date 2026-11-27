// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.ModComponents;
import advRocketry.multiblock.Multiblocks;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;

/** Native blueprint selection and preview; inspired by the original MIT hologram projector. */
public final class HoloProjectorItem extends Item {
    public static Consumer<InteractionHand> openScreen = hand -> {};
    public static Preview preview = (id, anchor, facing) -> {};

    @FunctionalInterface
    public interface Preview {
        void select(ResourceLocation id, BlockPos anchor, Direction facing);
    }

    public HoloProjectorItem(Properties properties) {
        super(properties);
    }

    public static ResourceLocation machine(ItemStack stack) {
        return stack.getOrDefault(ModComponents.PROJECTOR_CHOICE, ProjectorChoice.DEFAULT)
                .machine();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) openScreen.accept(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        if (context.getPlayer().isShiftKeyDown())
            return use(context.getLevel(), context.getPlayer(), context.getHand())
                    .getResult();
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        BlockPos clickedPos = context.getClickedPos();
        BlockState clicked = context.getLevel().getBlockState(clickedPos);
        var registries = context.getLevel().registryAccess();
        ResourceLocation controller = Multiblocks.forController(registries, clicked.getBlock());
        ResourceLocation selected = controller != null ? controller : machine(context.getItemInHand());
        var blueprint = Multiblocks.get(registries, selected);
        if (blueprint == null) return InteractionResult.PASS;
        if (controller != null)
            context.getItemInHand().set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(selected));
        BlockPos anchor = controller != null ? clickedPos : blueprint.controllerAtBase(clickedPos.above());
        Direction facing = controller != null && clicked.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? clicked.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : context.getHorizontalDirection().getOpposite();
        if (context.getPlayer() instanceof ServerPlayer player)
            PacketDistributor.sendToPlayer(player, new ProjectorSelection.Placement(selected, anchor, facing));
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Multiblocks.title(machine(stack)));
        tooltip.add(Component.translatable("message.adv_rocketry.holo_projector.sneak_use_machines_and_materials_use"));
    }
}

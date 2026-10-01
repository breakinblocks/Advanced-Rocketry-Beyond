// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketInfrastructure;
import advRocketry.rocket.RocketMonitoringBlockEntity;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/** Carries a selected tile position in native item data, as the original LibVulpes linker did. */
public final class LinkerItem extends Item {
  public LinkerItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static LinkTarget target(ItemStack stack) {
    return stack.getOrDefault(ModComponents.LINK, LinkTarget.EMPTY);
  }

  public static void setTarget(ItemStack stack, LinkTarget target) {
    if (target.equals(LinkTarget.EMPTY)) stack.remove(ModComponents.LINK);
    else stack.set(ModComponents.LINK, target);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof LandingPadBlockEntity)
      return InteractionResult.PASS;
    return useOn(context);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (context.getLevel().getBlockEntity(context.getClickedPos()) == null)
      return InteractionResult.PASS;
    if (!context.getLevel().isClientSide) {
      ItemStack stack = context.getItemInHand();
      LinkTarget data = target(stack);
      GlobalPos clicked = GlobalPos.of(context.getLevel().dimension(), context.getClickedPos());
      if (context.getLevel().getBlockEntity(context.getClickedPos())
          instanceof RocketInfrastructure port) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
          port.unlink();
          data = LinkTarget.EMPTY;
        } else if (data.rocket().isPresent()
            && ((ServerLevel) context.getLevel()).getEntity(data.rocket().get())
                instanceof RocketEntity rocket) {
          if (!port.link(rocket)) return InteractionResult.FAIL;
          data = LinkTarget.EMPTY;
        } else {
          port.unlink();
          data = LinkTarget.EMPTY.withPosition(clicked);
        }
        setTarget(stack, data);
        if (context.getPlayer() != null)
          context
              .getPlayer()
              .displayClientMessage(
                  Component.translatable(
                      "message.adv_rocketry.linker.infrastructure_link_updated_select_a_rocket"),
                  true);
        return InteractionResult.SUCCESS;
      }
      if (context.getLevel().getBlockEntity(context.getClickedPos())
              instanceof RocketBlockEntity builder
          && data.position().isPresent()
          && data.position().get().dimension().equals(context.getLevel().dimension())
          && context.getLevel().getBlockEntity(data.position().get().pos())
              instanceof RocketInfrastructure port) {
        if (!port.linkBuilder(builder)) return InteractionResult.FAIL;
        setTarget(stack, data.withoutPosition());
        if (context.getPlayer() != null)
          context
              .getPlayer()
              .displayClientMessage(
                  Component.translatable(
                      "message.adv_rocketry.linker.infrastructure_linked_to_assembler"),
                  true);
        return InteractionResult.SUCCESS;
      }
      if (context.getLevel().getBlockEntity(context.getClickedPos())
              instanceof WirelessTransceiverBlockEntity second
          && data.position().isPresent()) {
        ServerLevel source =
            context.getLevel().getServer().getLevel(data.position().get().dimension());
        if (source != null
            && source.getBlockEntity(data.position().get().pos())
                instanceof WirelessTransceiverBlockEntity first
            && WirelessTransceiverBlockEntity.link(first, second)) {
          setTarget(stack, data.withoutPosition());
          if (context.getPlayer() != null)
            context
                .getPlayer()
                .displayClientMessage(
                    Component.translatable(
                        "message.adv_rocketry.linker.wireless_data_network_linked"),
                    true);
          return InteractionResult.SUCCESS;
        }
      }
      if (context.getLevel().getBlockEntity(context.getClickedPos())
              instanceof RocketMonitoringBlockEntity monitor
          && data.rocket().isPresent()) {
        monitor.link(data.rocket().get());
        setTarget(stack, data.withoutRocket());
        if (context.getPlayer() != null)
          context
              .getPlayer()
              .displayClientMessage(
                  Component.translatable("message.adv_rocketry.linker.rocket_monitor_linked"),
                  true);
        return InteractionResult.SUCCESS;
      }
      if (context.getLevel().getBlockEntity(context.getClickedPos())
              instanceof OrbitalBlockEntity elevator
          && elevator.spaceElevator()) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
          SpaceElevatorLogic.unlink(elevator);
          setTarget(stack, data.withoutPosition());
          context
              .getPlayer()
              .displayClientMessage(
                  Component.translatable("message.adv_rocketry.linker.space_elevator_unlinked"),
                  true);
          return InteractionResult.SUCCESS;
        }
        if (data.position().isPresent()) {
          ServerLevel source =
              context.getLevel().getServer().getLevel(data.position().get().dimension());
          if (source != null
              && source.getBlockEntity(data.position().get().pos())
                  instanceof OrbitalBlockEntity first
              && SpaceElevatorLogic.link(first, elevator)) {
            setTarget(stack, data.withoutPosition());
            if (context.getPlayer() != null)
              context
                  .getPlayer()
                  .displayClientMessage(
                      Component.translatable("message.adv_rocketry.linker.space_elevators_linked"),
                      true);
            return InteractionResult.SUCCESS;
          }
          if (context.getPlayer() != null)
            context
                .getPlayer()
                .displayClientMessage(
                    Component.translatable(
                        "message.adv_rocketry.linker.complete_both_frames_station_must_be"),
                    true);
          return InteractionResult.FAIL;
        }
      }
      if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
        data = data.withoutPosition();
        context
            .getPlayer()
            .displayClientMessage(
                Component.translatable("message.adv_rocketry.linker.link_position_cleared"), true);
      } else {
        data = data.withPosition(clicked);
        if (context.getPlayer() != null)
          context
              .getPlayer()
              .displayClientMessage(
                  Texts.translate(
                      "message.adv_rocketry.linker.selected",
                      context.getClickedPos().toShortString()),
                  true);
      }
      setTarget(stack, data);
    }
    return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    target(stack)
        .position()
        .ifPresent(
            position ->
                tooltip.add(
                    Texts.translate(
                        "message.adv_rocketry.linker.selected_2", position.pos().toShortString())));
  }
}

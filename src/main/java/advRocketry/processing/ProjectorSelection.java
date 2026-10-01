// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.Main;
import advRocketry.ModComponents;
import advRocketry.multiblock.Multiblocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** A projector selection changes only the held projector, after server validation. */
public record ProjectorSelection(ResourceLocation machine, InteractionHand hand)
    implements CustomPacketPayload {
  public static final Type<ProjectorSelection> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "projector_selection"));
  public static final StreamCodec<RegistryFriendlyByteBuf, ProjectorSelection> CODEC =
      StreamCodec.of(
          (buffer, value) -> {
            buffer.writeResourceLocation(value.machine());
            buffer.writeEnum(value.hand());
          },
          buffer ->
              new ProjectorSelection(
                  buffer.readResourceLocation(), buffer.readEnum(InteractionHand.class)));

  @Override
  public Type<ProjectorSelection> type() {
    return TYPE;
  }

  public boolean apply(Player player) {
    var stack = player.getItemInHand(hand);
    if (!(stack.getItem() instanceof HoloProjectorItem)
        || Multiblocks.get(player.level().registryAccess(), machine) == null) return false;
    stack.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(machine));
    return true;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(TYPE, CODEC, (payload, context) -> payload.apply(context.player()));
  }
}

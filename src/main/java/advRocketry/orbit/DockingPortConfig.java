// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server-validated docking port labels edited through its machine screen. */
public record DockingPortConfig(BlockPos pos, String id, String target)
    implements CustomPacketPayload {
  public static final Type<DockingPortConfig> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "docking_port_config"));
  public static final StreamCodec<RegistryFriendlyByteBuf, DockingPortConfig> CODEC =
      StreamCodec.of(
          (buffer, config) -> {
            buffer.writeBlockPos(config.pos());
            buffer.writeUtf(config.id(), 32);
            buffer.writeUtf(config.target(), 32);
          },
          buffer ->
              new DockingPortConfig(buffer.readBlockPos(), buffer.readUtf(32), buffer.readUtf(32)));

  @Override
  public Type<DockingPortConfig> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              var player = context.player();
              if (player.containerMenu instanceof OrbitalMenu menu
                  && menu.entity.dockingPort()
                  && menu.entity.getBlockPos().equals(payload.pos())
                  && menu.stillValid(player))
                DockingPortLogic.configure(menu.entity, payload.id(), payload.target());
            });
  }
}

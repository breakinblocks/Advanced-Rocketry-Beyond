package advRocketry.transport;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record TransportTagEdit(int menu, int side, int slot, String tag)
    implements CustomPacketPayload {
  public static final Type<TransportTagEdit> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "transport_tag"));
  public static final StreamCodec<RegistryFriendlyByteBuf, TransportTagEdit> CODEC =
      StreamCodec.of(
          (b, p) -> {
            b.writeInt(p.menu);
            b.writeInt(p.side);
            b.writeInt(p.slot);
            b.writeUtf(p.tag, 256);
          },
          b -> new TransportTagEdit(b.readInt(), b.readInt(), b.readInt(), b.readUtf(256)));

  @Override
  public Type<TransportTagEdit> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (p, context) -> {
              var player = context.player();
              if (player.containerMenu instanceof TransportMenu menu && menu.containerId == p.menu)
                menu.setTag(player, p.side, p.slot, p.tag);
          else if (player.containerMenu instanceof TransportTerminalMenu terminal && terminal.containerId == p.menu) terminal.search(player, p.tag);
            });
  }
}

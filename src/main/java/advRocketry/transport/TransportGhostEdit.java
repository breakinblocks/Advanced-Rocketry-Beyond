package advRocketry.transport;

import advRocketry.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Ghost data cannot enter player inventories or become real fluid. */
public record TransportGhostEdit(int menu, int side, int slot, ItemStack item, FluidStack fluid)
    implements CustomPacketPayload {
  public static final Type<TransportGhostEdit> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "transport_ghost"));
  public static final StreamCodec<RegistryFriendlyByteBuf, TransportGhostEdit> CODEC =
      StreamCodec.of(
          (b, p) -> {
            b.writeInt(p.menu);
            b.writeInt(p.side);
            b.writeInt(p.slot);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(b, p.item);
            FluidStack.OPTIONAL_STREAM_CODEC.encode(b, p.fluid);
          },
          b ->
              new TransportGhostEdit(
                  b.readInt(),
                  b.readInt(),
                  b.readInt(),
                  ItemStack.OPTIONAL_STREAM_CODEC.decode(b),
                  FluidStack.OPTIONAL_STREAM_CODEC.decode(b)));

  @Override
  public Type<TransportGhostEdit> type() {
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
                menu.setGhost(player, p.side, p.slot, p.item, p.fluid);
            });
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.Main;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Finds the closest active planet beacon without a library HUD component. */
public final class BeaconFinderItem extends Item {
  private static Payload client;

  public record Payload(ResourceLocation dimension, List<BlockPos> positions)
      implements CustomPacketPayload {
    public Payload {
      positions = List.copyOf(positions);
    }

    public static final Type<Payload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "beacons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
              buffer.writeResourceLocation(payload.dimension());
              buffer.writeCollection(
                  payload.positions(), (output, position) -> output.writeBlockPos(position));
            },
            buffer ->
                new Payload(
                    buffer.readResourceLocation(), buffer.readList(input -> input.readBlockPos())));

    @Override
    public Type<Payload> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToClient(Payload.TYPE, Payload.CODEC, (payload, context) -> client = payload);
  }

  public static List<BlockPos> clientBeacons(ResourceLocation dimension) {
    return client != null && client.dimension().equals(dimension) ? client.positions() : List.of();
  }

  public static void clearClient() {
    client = null;
  }

  public static double angle(double playerX, double playerZ, float yaw, BlockPos beacon) {
    return Mth.wrapDegrees(
        Math.atan2(playerZ - beacon.getZ(), playerX - beacon.getX()) * 180 / Math.PI + 90 - yaw);
  }

  public BeaconFinderItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack finder = player.getItemInHand(hand);
    if (!(level instanceof ServerLevel serverLevel))
      return InteractionResultHolder.sidedSuccess(finder, level.isClientSide);
    player.displayClientMessage(status(serverLevel, player.blockPosition()), true);
    return InteractionResultHolder.success(finder);
  }

  public static Component status(ServerLevel level, BlockPos origin) {
    BlockPos target = BeaconLogic.nearest(level, origin);
    if (target == null)
      return Component.translatable("message.adv_rocketry.beacon_finder.no_active_beacon");
    int dx = target.getX() - origin.getX();
    int dz = target.getZ() - origin.getZ();
    int distance = (int) Math.round(Math.hypot(dx, dz));
    String direction =
        Math.abs(dx) > Math.abs(dz) ? dx > 0 ? "east" : "west" : dz > 0 ? "south" : "north";
    return Texts.translate(
        "message.adv_rocketry.beacon_finder.beacon_distance",
        distance,
        Component.translatable("message.adv_rocketry.beacon_finder." + direction));
  }
}

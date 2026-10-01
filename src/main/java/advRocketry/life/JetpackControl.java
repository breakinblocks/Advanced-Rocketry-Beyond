// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModComponents;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The original hydrogen jetpack's held-jump thrust, validated against equipped suit modules. */
public record JetpackControl() implements CustomPacketPayload {
  public static final Type<JetpackControl> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "jetpack_thrust"));
  public static final StreamCodec<RegistryFriendlyByteBuf, JetpackControl> CODEC =
      StreamCodec.unit(new JetpackControl());
  private static final Map<UUID, Long> LAST_INPUT = new ConcurrentHashMap<>();

  @Override
  public Type<JetpackControl> type() {
    return TYPE;
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToServer(
            TYPE,
            CODEC,
            (payload, context) -> {
              if (context.player() instanceof ServerPlayer player)
                LAST_INPUT.put(player.getUUID(), player.level().getGameTime());
            });
  }

  public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    LAST_INPUT.remove(event.getEntity().getUUID());
  }

  public static void tick(ServerPlayer player) {
    long last = LAST_INPUT.getOrDefault(player.getUUID(), Long.MIN_VALUE);
    if (last != Long.MIN_VALUE && player.level().getGameTime() - last <= 2) applyThrust(player);
    else applyHover(player);
  }

  static boolean applyThrust(Player player) {
    if (player.isCreative()
        || player.isSpectator()
        || player.isPassenger()
        || player.isFallFlying()) return false;
    if (!enabled(player.getItemBySlot(EquipmentSlot.CHEST)) || !useFuel(player)) return false;
    Vec3 velocity = player.getDeltaMovement();
    var helmet = player.getItemBySlot(EquipmentSlot.HEAD);
    int upgrades =
        helmet.getItem() instanceof SpaceSuitItem suit
            ? suit.countModule(helmet, "flight_speed_upgrade")
            : 0;
    double speed = 1 + Math.min(4, upgrades) * .02;
    player.setDeltaMovement(
        velocity.x * speed,
        Math.min(0.6, velocity.y + .1 * AdvancedRocketryConfig.jetpackThrust()),
        velocity.z * speed);
    player.fallDistance = 0;
    player.hurtMarked = true;
    return true;
  }

  static boolean applyHover(Player player) {
    if (player.onGround()
        || player.isCreative()
        || player.isSpectator()
        || player.isPassenger()
        || player.isFallFlying()
        || player.getDeltaMovement().y >= -0.1) return false;
    var helmet = player.getItemBySlot(EquipmentSlot.HEAD);
    if (!(helmet.getItem() instanceof SpaceSuitItem suit)
        || suit.countModule(helmet, "hover_upgrade") == 0
        || !hover(player.getItemBySlot(EquipmentSlot.CHEST))
        || !useFuel(player)) return false;
    Vec3 velocity = player.getDeltaMovement();
    player.setDeltaMovement(velocity.x, -0.1, velocity.z);
    player.fallDistance = 0;
    player.hurtMarked = true;
    return true;
  }

  private static boolean useFuel(Player player) {
    var chest = player.getItemBySlot(EquipmentSlot.CHEST);
    return chest.getItem() instanceof SpaceSuitItem suit
        && suit.countModule(chest, "jetpack") > 0
        && suit.transferOxygen(chest, new FluidStack(ProcessingFluids.hydrogen(), 1), false) == 1;
  }

  static boolean jetpack(ItemStack module) {
    return module.is(ProcessingRegistry.PART_ITEMS.get("jetpack").get());
  }

  private static JetpackSettings settings(ItemStack chest) {
    return SpaceSuitItem.findModule(chest, JetpackControl::jetpack)
        .getOrDefault(ModComponents.JETPACK, JetpackSettings.OFF);
  }

  public static boolean enabled(ItemStack chest) {
    return settings(chest).enabled();
  }

  private static boolean hover(ItemStack chest) {
    JetpackSettings settings = settings(chest);
    return settings.enabled() && settings.hover();
  }
}

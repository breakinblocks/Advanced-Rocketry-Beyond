// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModTags;
import advRocketry.orbit.ElevatorCapsule;
import advRocketry.orbit.StationLogic;
import advRocketry.processing.ProcessingFluids;
import advRocketry.rocket.RocketEntity;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.Milestone;
import advRocketry.space.Planet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Original suit checks and vacuum cadence with a native, reversible gravity modifier. */
public final class PlanetEnvironment {
  private static final ResourceLocation GRAVITY =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "planet_gravity");
  private static final ResourceLocation BIONIC_LEGS =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "bionic_legs");
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };

  private PlanetEnvironment() {}

  static boolean atmosphereBypass(EntityType<?> type) {
    return type.is(ModTags.ATMOSPHERE_IMMUNE);
  }

  public static void checkSpawn(MobSpawnEvent.PositionCheck event) {
    if (!AdvancedRocketryConfig.enableOxygen()
        || event.getSpawnType() != MobSpawnType.NATURAL
            && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
    var level = event.getLevel().getLevel();
    var mob = event.getEntity();
    var planet = GalaxyData.get(level.getServer()).planet(level);
    if (!planet.breathable()
        && !atmosphereBypass(mob.getType())
        && !SealedRooms.breathable(level, mob.blockPosition()))
      event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
  }

  private static boolean isExempt(LivingEntity living) {
    return living instanceof Player player && (player.isCreative() || player.isSpectator())
        || ProcessingFluids.isInLava(living)
        || living.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())
        || atmosphereBypass(living.getType())
        || living.getVehicle() instanceof RocketEntity
        || living.getVehicle() instanceof ElevatorCapsule;
  }

  private static boolean sealedPiece(LivingEntity living, Level level, EquipmentSlot slot) {
    var armor = living.getItemBySlot(slot);
    return armor.getItem() instanceof SpaceSuitItem || SpaceBreathing.sealed(level, armor);
  }

  private static boolean suitProtected(LivingEntity living, Level level, boolean fullSuit) {
    for (EquipmentSlot slot : ARMOR)
      if ((fullSuit || slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST)
          && !sealedPiece(living, level, slot)) return false;
    return true;
  }

  private static int cadence(Planet planet) {
    return planet.atmosphere <= 25 || !planet.oxygen ? 10 : 20;
  }

  public static void tickLiving(EntityTickEvent.Post event) {
    if (!(event.getEntity() instanceof LivingEntity living)
        || living instanceof ServerPlayer
        || !(living.level() instanceof ServerLevel level)
        || level.getGameTime() % 10 != 0
        || !AdvancedRocketryConfig.enableOxygen()
        || isExempt(living)) return;
    var planet = GalaxyData.get(level.getServer()).planet(level);
    if (level.getGameTime() % cadence(planet) == 0) applyAtmosphere(living, level, planet);
  }

  private static void applyAtmosphere(LivingEntity living, ServerLevel level, Planet planet) {
    if (planet.breathable() || SealedRooms.breathable(level, living.blockPosition())) return;
    boolean vacuum = planet.atmosphere <= 25;
    boolean fullSuit = vacuum || planet.atmosphere > 200 || planet.temperature > 450;
    boolean protectedSuit = suitProtected(living, level, fullSuit);
    var chest = living.getItemBySlot(EquipmentSlot.CHEST);
    if (protectedSuit && planet.oxygen && planet.atmosphere > 75) return;
    if (protectedSuit
        && chest.getItem() instanceof SpaceSuitItem suit
        && suit.transferOxygen(chest, new FluidStack(ProcessingFluids.oxygen(), 1), false) == 1)
      return;
    if (protectedSuit && SpaceBreathing.sealed(level, chest) && SpaceBreathing.consume(chest))
      return;
    boolean hot = !vacuum && planet.temperature > 450;
    boolean superheated = hot && planet.temperature >= 900;
    boolean superHighPressure = planet.atmosphere > 800;
    boolean lowOxygen = planet.oxygen && planet.atmosphere <= 75 && !vacuum;
    int damage =
        vacuum
            ? AdvancedRocketryConfig.vacuumDamage()
            : !planet.oxygen || lowOxygen ? 1 : superheated ? 4 : hot || superHighPressure ? 1 : 0;
    if (damage > 0) {
      if (living instanceof ServerPlayer player) AtmosphereSync.warn(player);
      living.hurt(living.damageSources().drown(), damage);
    }
    if (hot && !planet.oxygen && level.getGameTime() % 20 == 0)
      living.hurt(living.damageSources().onFire(), superheated ? 4 : 1);
    if (hot && planet.oxygen) living.setRemainingFireTicks(20);
    int slowdown = vacuum || !planet.oxygen ? 4 : hot || superHighPressure ? 3 : 2;
    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, slowdown));
    if (!hot || !planet.oxygen)
      living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, slowdown));
    if ((vacuum || !planet.oxygen) && AdvancedRocketryConfig.atmosphericNausea())
      living.addEffect(
          new MobEffectInstance(
              MobEffects.CONFUSION, 400, !planet.oxygen && planet.atmosphere > 200 ? 2 : 1));
  }

  public static void preventNarcosisJump(LivingEvent.LivingJumpEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)
        || !AdvancedRocketryConfig.enableOxygen()
        || isExempt(player)) return;
    var level = player.serverLevel();
    var planet = GalaxyData.get(player.server).planet(level);
    if (planet.oxygen
        || planet.atmosphere <= 800
        || SealedRooms.breathable(level, player.blockPosition())) return;
    var chest = player.getItemBySlot(EquipmentSlot.CHEST);
    boolean hasAir =
        chest.getItem() instanceof SpaceSuitItem suit && suit.oxygen(chest) > 0
            || SpaceBreathing.sealed(level, chest) && SpaceBreathing.air(chest) > 0;
    if (suitProtected(player, level, true) && hasAir) return;
    var motion = player.getDeltaMovement();
    player.setDeltaMovement(motion.x, 0, motion.z);
    player.hurtMarked = true;
  }

  public static void tick(PlayerTickEvent.Post event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;
    AtmosphereSync.tick(player);
    JetpackControl.tick(player);
    var galaxy = GalaxyData.get(player.server);
    var planet = galaxy.planet(player.level());
    if (GalaxyData.LUNA.equals(planet.location)
        && player.level().getGameTime() % 20 == 0
        && player.blockPosition().distSqr(new BlockPos(2347, 80, 67)) < 512)
      AdvancementLogic.grant(player, Milestone.APOLLO_SITE);
    if (!planet.breathable()
        && player.level().getGameTime() % 20 == 0
        && SealedRooms.breathable(player.level(), player.blockPosition()))
      AdvancementLogic.grant(player, Milestone.SEALED_ROOM);
    var gravity = player.getAttribute(Attributes.GRAVITY);
    if (gravity != null) {
      double localGravity = planet.gravity;
      if (planet.id == GalaxyData.SPACE_ID) {
        var station = StationLogic.at(galaxy, player.level(), player.blockPosition());
        if (station != null && station.gravity != null) localGravity = station.gravity;
      }
      double amount = localGravity - 1;
      var modifier = gravity.getModifier(GRAVITY);
      if (modifier != null && modifier.amount() != amount) gravity.removeModifier(GRAVITY);
      if (amount != 0 && gravity.getModifier(GRAVITY) == null)
        gravity.addTransientModifier(
            new AttributeModifier(
                GRAVITY, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    var legArmor = player.getItemBySlot(EquipmentSlot.LEGS);
    int legUpgrades =
        legArmor.getItem() instanceof SpaceSuitItem suit
            ? suit.countModule(legArmor, "legs_upgrade")
            : 0;
    var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
    if (movement != null) {
      var old = movement.getModifier(BIONIC_LEGS);
      double boost = player.isSprinting() ? Math.min(legUpgrades, 4) : 0;
      if (old != null && old.amount() != boost) movement.removeModifier(BIONIC_LEGS);
      if (boost > 0 && movement.getModifier(BIONIC_LEGS) == null)
        movement.addTransientModifier(
            new AttributeModifier(
                BIONIC_LEGS, boost, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    var bootArmor = player.getItemBySlot(EquipmentSlot.FEET);
    if (bootArmor.getItem() instanceof SpaceSuitItem boots
        && boots.countModule(bootArmor, "padded_boots_upgrade") > 0
        && (!AdvancedRocketryConfig.lowGravityBoots() || planet.gravity < 1))
      player.fallDistance = 0;
    if (AdvancedRocketryConfig.enableOxygen()
        && player.level().getGameTime() % cadence(planet) == 0
        && !isExempt(player)) applyAtmosphere(player, player.serverLevel(), planet);
  }
}

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class AtmosphereGameplayTests {
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };

  private static ItemStack suit(ServerPlayer player) {
    var helmet = new ItemStack(LifeSupportRegistry.HELMET.get());
    var chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    var tank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    ((PressureTankItem) tank.getItem())
        .handler(tank)
        .fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 100),
            IFluidHandler.FluidAction.EXECUTE);
    LifeSupportRegistry.CHEST.get().inventory(chest).setStackInSlot(0, tank);
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    player.setItemSlot(EquipmentSlot.CHEST, chest);
    player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(LifeSupportRegistry.LEGS.get()));
    player.setItemSlot(EquipmentSlot.FEET, new ItemStack(LifeSupportRegistry.BOOTS.get()));
    return chest;
  }

  @GameTest(template = "empty", timeoutTicks = 200)
  public static void allPressureTemperatureAndOxygenClassesProtectAndConsumeCorrectly(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var server = level.getServer();
    var planet = GalaxyData.get(server).planet(level);
    int oldPressure = planet.atmosphere, oldTemperature = planet.temperature;
    boolean oldOxygen = planet.oxygen;
    long oldTime = level.getGameTime();
    var cookie =
        CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "atmosphere-test"), false);
    var player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
    var connection = new Connection(PacketFlow.SERVERBOUND);
    var channel = new EmbeddedChannel(connection);
    NetworkRegistry.configureMockConnection(connection);
    server.getPlayerList().placeNewPlayer(connection, player, cookie);
    try {
      player.setGameMode(GameType.CREATIVE);
      for (int tick = 0; tick < 61; tick++) player.tick();
      player.setGameMode(GameType.SURVIVAL);
      var pos = helper.absolutePos(new BlockPos(3, 3, 3)).atY(240);
      player.setPos(pos.getX(), pos.getY(), pos.getZ());
      ((ServerLevelData) server.overworld().getLevelData())
          .setGameTime(oldTime + Math.floorMod(-oldTime, 20));
      // Pressure, Kelvin, oxygen flag, expected unprotected damage, oxygen consumption.
      int[][] cases = {
        {0, 100, 1, AdvancedRocketryConfig.vacuumDamage(), 1},
        {25, 100, 1, AdvancedRocketryConfig.vacuumDamage(), 1},
        {26, 100, 1, 1, 1},
        {75, 100, 1, 1, 1},
        {76, 100, 1, 0, 0},
        {200, 100, 1, 0, 0},
        {201, 100, 1, 0, 0},
        {800, 100, 1, 0, 0},
        {801, 100, 1, 1, 0},
        {100, 450, 1, 0, 0},
        {100, 451, 1, 1, 0},
        {100, 900, 1, 4, 0},
        {100, 100, 0, 1, 1},
        {201, 100, 0, 1, 1},
        {801, 100, 0, 1, 1},
        {100, 451, 0, 1, 1},
        {100, 900, 0, 4, 1}
      };
      for (int[] scenario : cases) {
        planet.atmosphere = scenario[0];
        planet.temperature = scenario[1];
        planet.oxygen = scenario[2] != 0;
        String label =
            "pressure="
                + scenario[0]
                + ", temperature="
                + scenario[1]
                + ", oxygen="
                + planet.oxygen;
        for (var slot : ARMOR) player.setItemSlot(slot, ItemStack.EMPTY);
        player.removeAllEffects();
        player.setHealth(20);
        player.invulnerableTime = 0;
        player.clearFire();
        PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(
            Math.abs(player.getHealth() - (20 - scenario[3])) < .01,
            "Unprotected damage: " + label + " health=" + player.getHealth());
        if (scenario[0] > 200 && scenario[1] <= 450)
          helper.assertTrue(
              player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
              "Pressure did not impair movement: " + label);
        var chest = suit(player);
        player.removeAllEffects();
        player.setHealth(20);
        player.invulnerableTime = 0;
        player.clearFire();
        PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(
            player.getHealth() == 20 && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
            "Full suit did not protect: " + label);
        helper.assertTrue(
            LifeSupportRegistry.CHEST.get().oxygen(chest) == 100 - scenario[4],
            "Incorrect suit consumption: " + label);
      }
      planet.atmosphere = 0;
      planet.temperature = 100;
      planet.oxygen = false;
      var chest = suit(player);
      player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
      player.setHealth(20);
      player.invulnerableTime = 0;
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(
          player.getHealth() < 20 && LifeSupportRegistry.CHEST.get().oxygen(chest) == 100,
          "Incomplete suit consumed oxygen or protected from vacuum");
      level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
      level.setBlock(pos.above(), Blocks.WATER.defaultBlockState(), 3);
      player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
      player.baseTick();
      helper.assertTrue(
          player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()),
          "Player did not enter test water");
      player.setHealth(20);
      player.invulnerableTime = 0;
      player.removeAllEffects();
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(
          player.getHealth() == 20 && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
          "Submerged player still received atmospheric damage");
      level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
      player.baseTick();
      player.setHealth(20);
      player.invulnerableTime = 0;
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(
          player.getHealth() < 20, "Shallow water incorrectly protected exposed player eyes");
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    } finally {
      planet.atmosphere = oldPressure;
      planet.temperature = oldTemperature;
      planet.oxygen = oldOxygen;
      ((ServerLevelData) server.overworld().getLevelData()).setGameTime(oldTime);
      server.getPlayerList().remove(player);
      channel.finishAndReleaseAll();
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void vacuumDamagesOrdinaryMobsButPreservesArmorStandBypass(GameTestHelper helper) {
    var level = helper.getLevel();
    var planet = GalaxyData.get(level.getServer()).planet(level);
    int oldPressure = planet.atmosphere;
    long oldTime = level.getGameTime();
    try {
      planet.atmosphere = 0;
      ((ServerLevelData) level.getServer().overworld().getLevelData())
          .setGameTime(oldTime + Math.floorMod(-oldTime, 20));
      var cow = EntityType.COW.create(level);
      var stand = EntityType.ARMOR_STAND.create(level);
      var pos = helper.absolutePos(new BlockPos(5, 5, 5)).atY(240);
      cow.setPos(pos.getX(), pos.getY(), pos.getZ());
      stand.setPos(pos.getX(), pos.getY(), pos.getZ());
      float cowHealth = cow.getHealth(), standHealth = stand.getHealth();
      PlanetEnvironment.tickLiving(new EntityTickEvent.Post(cow));
      PlanetEnvironment.tickLiving(new EntityTickEvent.Post(stand));
      helper.assertTrue(
          cow.getHealth() < cowHealth && stand.getHealth() == standHealth,
          "Mob damage or armor-stand atmosphere bypass failed");
      level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
      level.setBlock(pos.above(), Blocks.WATER.defaultBlockState(), 3);
      cow.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
      cow.baseTick();
      helper.assertTrue(
          cow.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()), "Cow did not enter test water");
      cow.setHealth(cowHealth);
      cow.invulnerableTime = 0;
      PlanetEnvironment.tickLiving(new EntityTickEvent.Post(cow));
      helper.assertTrue(cow.getHealth() == cowHealth, "Submerged mob received atmosphere damage");
      level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
      cow.baseTick();
      cow.invulnerableTime = 0;
      PlanetEnvironment.tickLiving(new EntityTickEvent.Post(cow));
      helper.assertTrue(
          cow.getHealth() < cowHealth, "Shallow water incorrectly protected exposed mob eyes");
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    } finally {
      planet.atmosphere = oldPressure;
      ((ServerLevelData) level.getServer().overworld().getLevelData()).setGameTime(oldTime);
    }
    helper.succeed();
  }
}

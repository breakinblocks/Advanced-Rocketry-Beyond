package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.TestMultiblocks;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.life.PlanetEnvironment;
import advRocketry.processing.ProcessingFluids;
import advRocketry.rocket.RocketEntity;
import advRocketry.rocket.RocketRegistry;
import advRocketry.rocket.RocketStructure;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class AdvancementTests {
  private record Visitor(ServerPlayer player, EmbeddedChannel channel) implements AutoCloseable {
    static Visitor create(ServerLevel level) {
      var cookie =
          CommonListenerCookie.createInitial(
              new GameProfile(UUID.randomUUID(), "advancement-test"), false);
      var player =
          new ServerPlayer(
              level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
      var connection = new Connection(PacketFlow.SERVERBOUND);
      var channel = new EmbeddedChannel(connection);
      NetworkRegistry.configureMockConnection(connection);
      level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
      player.setGameMode(GameType.CREATIVE);
      return new Visitor(player, channel);
    }

    public void close() {
      player.stopRiding();
      player.server.getPlayerList().remove(player);
      channel.finishAndReleaseAll();
    }
  }

  private static AdvancementHolder advancement(ServerPlayer player, String id) {
    return player
        .server
        .getAdvancements()
        .get(ResourceLocation.fromNamespaceAndPath(Main.MODID, "normal/" + id));
  }

  private static boolean awarded(ServerPlayer player, String id) {
    return player.getAdvancements().getOrStartProgress(advancement(player, id)).isDone();
  }

  private static void revoke(ServerPlayer player, String id) {
    var holder = advancement(player, id);
    for (String criterion : holder.value().criteria().keySet())
      player.getAdvancements().revoke(holder, criterion);
  }

  private static void inventory(ServerPlayer player, int slot, String id) {
    player
        .getInventory()
        .setItem(slot, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
    player.containerMenu.broadcastChanges();
  }

  @GameTest(template = "empty", timeoutTicks = 600)
  public static void inventoryAndSeatEventAwardOnlyMatchingMilestones(GameTestHelper helper) {
    try (var visitor = Visitor.create(helper.getLevel())) {
      var player = visitor.player();
      String[][] milestones = {
        {"root", "minecraft:crafting_table"},
        {"blockpresser", "adv_rocketry:plate_press"},
        {"crystalline", "adv_rocketry:crystallizer"},
        {"dilithium", "adv_rocketry:dilithium_ore"},
        {"electrifying", "adv_rocketry:electrolyzer"},
        {"feeltheheat", "adv_rocketry:electric_arc_furnace"},
        {"holographic", "adv_rocketry:item_holoprojector"},
        {"rollin", "adv_rocketry:rolling_machine"},
        {"spindoctor", "adv_rocketry:lathe"},
        {"warp", "adv_rocketry:warp_core"},
        {"centrifuge", "adv_rocketry:centrifuge"},
        {"cutting_machine", "adv_rocketry:cutting_machine"},
        {"precision_assembler", "adv_rocketry:precision_assembler"},
        {"circuits", "adv_rocketry:basic_circuit"},
        {"laser_etcher", "adv_rocketry:precision_laser_etcher"},
        {"chemical_reactor", "adv_rocketry:chemical_reactor"},
        {"rocket_fuel", "adv_rocketry:rocket_fuel_bucket"},
        {"power", "adv_rocketry:coal_generator"},
        {"rocket_builder", "adv_rocketry:rocket_builder"},
        {"bipropellant", "adv_rocketry:bipropellant_rocket_motor"},
        {"nuclear", "adv_rocketry:nuclear_rocket_motor"},
        {"station_builder", "adv_rocketry:station_builder"},
        {"orbital_laser", "adv_rocketry:orbital_laser"},
        {"railgun", "adv_rocketry:railgun"},
        {"black_hole_generator", "adv_rocketry:black_hole_generator"},
        {"area_gravity", "adv_rocketry:area_gravity_controller"},
        {"jetpack", "adv_rocketry:jetpack"},
        {"observatory", "adv_rocketry:observatory"},
        {"data_processor", "adv_rocketry:astrobody_data_processor"},
        {"satellite_builder", "adv_rocketry:satellite_builder"},
        {"biome_changer", "adv_rocketry:biome_changer_remote"},
        {"terraformer", "adv_rocketry:atmosphere_terraformer"}
      };
      inventory(player, 0, "minecraft:dirt");
      for (String[] milestone : milestones) {
        helper.assertTrue(
            !awarded(player, milestone[0]), "Unrelated inventory awarded " + milestone[0]);
        inventory(player, 0, milestone[1]);
        helper.assertTrue(
            awarded(player, milestone[0]),
            "Native inventory changes did not award " + milestone[0]);
      }
      inventory(player, 0, "adv_rocketry:space_helmet");
      inventory(player, 1, "adv_rocketry:space_chestplate");
      inventory(player, 2, "adv_rocketry:space_leggings");
      helper.assertTrue(!awarded(player, "suitedup"), "Incomplete suit awarded the milestone");
      inventory(player, 3, "adv_rocketry:space_boots");
      helper.assertTrue(awarded(player, "suitedup"), "Complete suit was not recognized");
      BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
      var level = helper.getLevel();
      level.setBlock(pos, Blocks.TNT.defaultBlockState(), 3);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
      var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
      AdvancementLogic.seatOnTnt(
          new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
      helper.assertTrue(!awarded(player, "beer"), "TNT alone awarded the easter egg");
      player.setItemInHand(
          InteractionHand.MAIN_HAND, new ItemStack(RocketRegistry.PARTS.get("seat").get()));
      level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
      AdvancementLogic.seatOnTnt(
          new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
      helper.assertTrue(!awarded(player, "beer"), "Seat on ordinary ground awarded the easter egg");
      level.setBlock(pos, Blocks.TNT.defaultBlockState(), 3);
      AdvancementLogic.seatOnTnt(
          new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
      helper.assertTrue(awarded(player, "beer"), "Seat on TNT failed to award the easter egg");
    }
    helper.succeed();
  }

  private static void move(ServerPlayer player, ServerLevel level, Vec3 position) {
    player.stopRiding();
    player.changeDimension(
        new DimensionTransition(level, position, Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING));
  }

  private static RocketEntity moonFlight(
      ServerLevel level, ServerPlayer passenger, BlockPos origin) {
    var structure = new RocketStructure();
    structure.seats = 1;
    structure.thrust = 1000;
    structure.destination = 1;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("seat").get().defaultBlockState(),
            new CompoundTag()));
    var rocket = RocketRegistry.ROCKET.get().create(level);
    rocket.initialize(structure, origin.atY(AdvancedRocketryConfig.orbitHeight() + 1));
    var saved = rocket.saveWithoutId(new CompoundTag());
    saved.putInt("flight", 1);
    rocket.load(saved);
    level.addFreshEntity(rocket);
    if (passenger != null) {
      move(passenger, level, rocket.position());
      if (!passenger.startRiding(rocket))
        throw new AssertionError("Could not board the moon test ship");
    }
    rocket.tick();
    if (!rocket.isRemoved()) throw new AssertionError("Moon fixture did not transfer its ship");
    return passenger == null ? rocket : (RocketEntity) passenger.getVehicle();
  }

  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void travelAwardsPreserveFirstArrivalAndStationMembership(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    boolean priorMoon = galaxy.reachedMoon, priorWarp = galaxy.reachedWarp;
    long stationId = -1;
    UUID[] uncrewedId = new UUID[1];
    try (var visitor = Visitor.create(helper.getLevel())) {
      var player = visitor.player();
      galaxy.reachedMoon = false;
      var unmanned = moonFlight(helper.getLevel(), null, helper.absolutePos(new BlockPos(2, 2, 2)));
      helper.assertTrue(
          unmanned != null && !galaxy.reachedMoon,
          "Uncrewed travel consumed the first landing milestone");
      uncrewedId[0] = unmanned.getUUID();
      var first = moonFlight(helper.getLevel(), player, helper.absolutePos(new BlockPos(2, 2, 2)));
      helper.assertTrue(
          first != null
              && awarded(player, "moonlanding")
              && awarded(player, "onesmallstep")
              && galaxy.reachedMoon,
          "Crewed moon arrival did not award both first-flight milestones");
      player.stopRiding();
      first.discard();
      revoke(player, "onesmallstep");
      revoke(player, "moonlanding");
      var second = moonFlight(helper.getLevel(), player, helper.absolutePos(new BlockPos(2, 2, 2)));
      helper.assertTrue(
          second != null && awarded(player, "moonlanding") && !awarded(player, "onesmallstep"),
          "Repeat travel did not distinguish global-first and personal moon awards");
      player.stopRiding();
      second.discard();
      var moon = player.serverLevel();
      long previousTime = moon.getGameTime();
      ((ServerLevelData) server.overworld().getLevelData())
          .setGameTime(previousTime + Math.floorMod(-previousTime, 20));
      player.setPos(2347, 80, 100);
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(
          !awarded(player, "wenttothemoon"), "Apollo award triggered outside the landmark radius");
      player.setPos(2347, 80, 67);
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(
          awarded(player, "wenttothemoon"), "Apollo landmark did not award discovery");
      ((ServerLevelData) server.overworld().getLevelData()).setGameTime(previousTime);
      var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
      Station station = new Station();
      station.deployed = true;
      station.orbitPlanet = GalaxyData.EARTH_ID;
      station.x = 1200000;
      station.z = 1200000;
      station.radius = 64;
      station.fuel = 100;
      stationId = galaxy.newStation(station);
      BlockPos pos = new BlockPos(1200000, 120, 1200000);
      BlockPos corePos = pos.east(4);
      TestMultiblocks.build(space, "warp_core", corePos, Direction.NORTH);
      ((OrbitalBlockEntity) space.getBlockEntity(corePos)).tick();
      space.setBlock(pos, OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
      var controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
      controller.warpDestination = 1;
      move(player, space, Vec3.atCenterOf(pos));
      galaxy.reachedWarp = false;
      helper.assertTrue(
          !WarpLogic.warp(controller)
              && !awarded(player, "givingitallshesgot")
              && !galaxy.reachedWarp,
          "Failed warp granted travel credit");
      controller.energy.receiveEnergy(1000, false);
      helper.assertTrue(WarpLogic.warp(controller), "Crewed warp failed: " + controller.status);
      helper.assertTrue(
          awarded(player, "givingitallshesgot") && awarded(player, "flightofpheonix"),
          "First warp did not award its passengers");
      revoke(player, "givingitallshesgot");
      revoke(player, "flightofpheonix");
      WarpLogic.complete(galaxy, galaxy.stations.get(stationId).warpEta);
      controller.warpDestination = GalaxyData.EARTH_ID;
      controller.energy.receiveEnergy(1000, false);
      helper.assertTrue(
          WarpLogic.warp(controller)
              && awarded(player, "givingitallshesgot")
              && !awarded(player, "flightofpheonix"),
          "Repeat warp incorrectly repeated the first-flight award");
      revoke(player, "givingitallshesgot");
      WarpLogic.complete(galaxy, galaxy.stations.get(stationId).warpEta);
      player.setPos(pos.getX() + 100, 120, pos.getZ());
      controller.warpDestination = 1;
      controller.energy.receiveEnergy(1000, false);
      helper.assertTrue(
          WarpLogic.warp(controller) && !awarded(player, "givingitallshesgot"),
          "Warp awarded a player outside the station");
    } finally {
      galaxy.reachedMoon = priorMoon;
      galaxy.reachedWarp = priorWarp;
      if (stationId >= 0) galaxy.stations.remove(stationId);
      galaxy.setDirty();
    }
    helper.succeedWhen(
        () -> {
          var moon = PlanetRuntime.create(server, galaxy.planets.get(1));
          var arrived = moon.getEntity(uncrewedId[0]);
          helper.assertTrue(
              arrived != null, "Uncrewed arrival is not yet in an active destination chunk");
          arrived.discard();
        });
  }

  private static final Pattern BANNED =
      Pattern.compile(
          "[\u2013\u2014]|\\blanded\\b|\\blands\\b|\\bwir(e|es|ed|ing)\\b|\\bsilent(ly)?\\b|\\bquiet(ly)?\\b|\\blives"
              + " in\\b",
          Pattern.CASE_INSENSITIVE);

  private static JsonObject language() {
    var stream = Main.class.getResourceAsStream("/assets/adv_rocketry/lang/en_us.json");
    if (stream == null) throw new AssertionError("English language file is not on the classpath");
    try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException exception) {
      throw new AssertionError("Could not read the English language file", exception);
    }
  }

  private static List<String> strings(JsonElement element) {
    List<String> values = new ArrayList<>();
    if (element.isJsonPrimitive()) values.add(element.getAsString());
    else if (element.isJsonArray())
      for (JsonElement entry : element.getAsJsonArray())
        if (entry.isJsonPrimitive()) values.add(entry.getAsString());
    return values;
  }

  private static boolean dimensionExists(MinecraftServer server, String id) {
    var location = ResourceLocation.parse(id);
    if (server.getLevel(ResourceKey.create(Registries.DIMENSION, location)) != null) return true;
    for (var planet : GalaxyData.get(server).planets.values())
      if (location.equals(planet.dimension)) return true;
    return false;
  }

  private static void inspect(
      MinecraftServer server, String id, JsonElement element, List<String> failures) {
    if (element.isJsonArray()) {
      for (JsonElement entry : element.getAsJsonArray()) inspect(server, id, entry, failures);
      return;
    }
    if (!element.isJsonObject()) return;
    for (var entry : element.getAsJsonObject().entrySet()) {
      String name = entry.getKey();
      JsonElement value = entry.getValue();
      switch (name) {
        case "items" -> {
          for (String item : strings(value))
            if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)))
              failures.add(id + " criterion item " + item + " is not registered");
        }
        case "blocks" -> {
          for (String block : strings(value))
            if (!BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(block)))
              failures.add(id + " criterion block " + block + " is not registered");
        }
        case "to", "from" -> {
          for (String dimension : strings(value))
            if (!dimensionExists(server, dimension))
              failures.add(id + " criterion dimension " + dimension + " does not exist");
        }
        case "vehicle" -> {
          if (value.isJsonObject() && value.getAsJsonObject().has("type"))
            for (String type : strings(value.getAsJsonObject().get("type")))
              if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(type)))
                failures.add(id + " vehicle entity " + type + " is not registered");
        }
        case "trigger" -> {
          String trigger = value.getAsString();
          if (!BuiltInRegistries.TRIGGER_TYPES.containsKey(ResourceLocation.parse(trigger)))
            failures.add(id + " uses unknown trigger " + trigger);
          if (trigger.equals("minecraft:impossible"))
            failures.add(id + " can only be granted by command");
        }
        default -> {}
      }
      inspect(server, id, value, failures);
    }
  }

  @GameTest(template = "empty", timeoutTicks = 200)
  public static void everyAdvancementResolvesAgainstRegistriesAndLanguage(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    var lang = language();
    var files =
        server
            .getResourceManager()
            .listResources(
                "advancement",
                location ->
                    location.getNamespace().equals(Main.MODID)
                        && location.getPath().endsWith(".json"));
    List<String> failures = new ArrayList<>();
    Set<ResourceLocation> ids = new HashSet<>();
    List<String> roots = new ArrayList<>();
    for (var file : files.entrySet()) {
      String path = file.getKey().getPath();
      var id =
          ResourceLocation.fromNamespaceAndPath(
              Main.MODID, path.substring("advancement/".length(), path.length() - 5));
      ids.add(id);
      JsonObject json;
      try (var reader = file.getValue().openAsReader()) {
        json = JsonParser.parseReader(reader).getAsJsonObject();
      } catch (IOException | RuntimeException exception) {
        failures.add(id + " could not be read: " + exception.getMessage());
        continue;
      }
      var loaded = server.getAdvancements().get(id);
      if (loaded == null) failures.add(id + " did not load");
      var display = json.getAsJsonObject("display");
      if (display == null) {
        failures.add(id + " has no display");
        continue;
      }
      var icon = ResourceLocation.parse(display.getAsJsonObject("icon").get("id").getAsString());
      if (!BuiltInRegistries.ITEM.containsKey(icon)
          || BuiltInRegistries.ITEM.get(icon) == Items.AIR)
        failures.add(id + " icon " + icon + " is not an item");
      for (String field : new String[] {"title", "description"}) {
        String key = display.getAsJsonObject(field).get("translate").getAsString();
        if (!lang.has(key)) failures.add(id + " " + field + " key " + key + " is missing");
        else if (BANNED.matcher(lang.get(key).getAsString()).find())
          failures.add(id + " " + field + " uses a banned word or dash: " + lang.get(key));
      }
      if (display.has("hidden")
          && display.get("hidden").getAsBoolean()
          && !id.getPath().equals("normal/beer")) failures.add(id + " is hidden");
      if (json.has("parent")) {
        var parent = ResourceLocation.parse(json.get("parent").getAsString());
        if (server.getAdvancements().get(parent) == null)
          failures.add(id + " parent " + parent + " does not exist");
        else if (loaded != null && !loaded.value().parent().equals(Optional.of(parent)))
          failures.add(id + " loaded with a different parent");
      } else roots.add(id.toString());
      if (!json.has("criteria") || json.getAsJsonObject("criteria").size() == 0)
        failures.add(id + " has no criteria");
      else inspect(server, id.toString(), json.get("criteria"), failures);
    }
    if (!roots.equals(List.of(Main.MODID + ":normal/root")))
      failures.add("Expected a single root, found " + roots);
    if (ids.size() < 46) failures.add("Only " + ids.size() + " advancement files were found");
    for (var holder : server.getAdvancements().getAllAdvancements())
      if (holder.id().getNamespace().equals(Main.MODID) && !ids.contains(holder.id()))
        failures.add(holder.id() + " loaded without a matching file");
    helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
    helper.succeed();
  }

  @SuppressWarnings("unchecked")
  private static ModConfigSpec.ConfigValue<Object> config(String name)
      throws ReflectiveOperationException {
    Field field = AdvancedRocketryConfig.class.getDeclaredField(name);
    field.setAccessible(true);
    return (ModConfigSpec.ConfigValue<Object>) field.get(null);
  }

  @GameTest(template = "empty", timeoutTicks = 400)
  public static void blockDimensionAndRidingTriggersAwardNewMilestones(GameTestHelper helper) {
    var level = helper.getLevel();
    try (var visitor = Visitor.create(level)) {
      var player = visitor.player();
      BlockPos vent = helper.absolutePos(new BlockPos(1, 2, 1));
      level.setBlock(vent, Blocks.STONE.defaultBlockState(), 3);
      CriteriaTriggers.PLACED_BLOCK.trigger(player, vent, new ItemStack(Items.STONE));
      helper.assertTrue(!awarded(player, "oxygen_vent"), "Stone placement awarded the vent");
      level.setBlock(vent, LifeSupportRegistry.VENT.get().defaultBlockState(), 3);
      CriteriaTriggers.PLACED_BLOCK.trigger(
          player, vent, new ItemStack(LifeSupportRegistry.VENT.get()));
      helper.assertTrue(awarded(player, "oxygen_vent"), "Placing an oxygen vent was not noticed");
      for (String pipe : new String[] {"energy_cable", "fluid_pipe", "item_conduit"}) {
        revoke(player, "pipes");
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 1));
        var block =
            BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Main.MODID, pipe));
        level.setBlock(pos, block.defaultBlockState(), 3);
        CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, new ItemStack(block));
        helper.assertTrue(awarded(player, "pipes"), "Placing " + pipe + " was not noticed");
      }
      var galaxy = GalaxyData.get(level.getServer());
      CriteriaTriggers.CHANGED_DIMENSION.trigger(
          player, Level.OVERWORLD, galaxy.planets.get(1).key());
      helper.assertTrue(
          !awarded(player, "space_station"), "Arriving on the Moon counted as a station visit");
      CriteriaTriggers.CHANGED_DIMENSION.trigger(
          player, Level.OVERWORLD, galaxy.planets.get(GalaxyData.SPACE_ID).key());
      helper.assertTrue(awarded(player, "space_station"), "Arriving in orbit was not noticed");
      var capsule = OrbitalRegistry.ELEVATOR_CAPSULE.get().create(level);
      capsule.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
      helper.assertTrue(player.startRiding(capsule, true), "Could not board the capsule");
      helper.assertTrue(awarded(player, "space_elevator"), "Elevator ride was not noticed");
      player.stopRiding();
      capsule.discard();
    }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 400)
  public static void crewedLaunchAwardsLiftoff(GameTestHelper helper)
      throws ReflectiveOperationException {
    var setting = config("EXPERIMENTAL_SPACE_FLIGHT");
    Object previous = setting.get();
    var rocket = RocketRegistry.ROCKET.get().create(helper.getLevel());
    var structure = new RocketStructure();
    structure.seats = 1;
    structure.thrust = 1000;
    structure.destination = Integer.MAX_VALUE;
    structure.cells.add(
        new RocketStructure.Cell(
            BlockPos.ZERO,
            RocketRegistry.PARTS.get("guidance_computer").get().defaultBlockState(),
            new CompoundTag()));
    rocket.initialize(structure, helper.absolutePos(new BlockPos(3, 3, 3)));
    try (var visitor = Visitor.create(helper.getLevel())) {
      var player = visitor.player();
      helper.assertTrue(player.startRiding(rocket), "Could not board the launch test rocket");
      helper.assertTrue(
          !awarded(player, "liftoff") && !awarded(player, "space_elevator"),
          "Boarding a rocket awarded a flight milestone");
      setting.set(false);
      helper.assertTrue(
          !rocket.launch(player) && !awarded(player, "liftoff"),
          "A refused launch awarded liftoff");
      setting.set(true);
      helper.assertTrue(rocket.launch(player), "Crewed launch failed");
      helper.assertTrue(awarded(player, "liftoff"), "Crewed launch did not award liftoff");
    } finally {
      setting.set(previous);
      rocket.discard();
    }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 600)
  public static void sealedRoomAndDiscoveryAwardOnlyInsideTheirSpaces(GameTestHelper helper)
      throws ReflectiveOperationException {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var chance = config("PLANET_DISCOVERY_CHANCE");
    Object previousChance = chance.get();
    long previousTime = server.overworld().getGameTime();
    long stationId = -1;
    BlockPos ventPos = new BlockPos(96, 150, 96);
    var moon = PlanetRuntime.create(server, galaxy.planets.get(1));
    try (var visitor = Visitor.create(helper.getLevel())) {
      var player = visitor.player();
      helper.assertTrue(!galaxy.planets.get(1).breathable(), "Moon fixture is breathable");
      ((ServerLevelData) server.overworld().getLevelData())
          .setGameTime(previousTime + Math.floorMod(-previousTime, 20));
      move(player, moon, Vec3.atBottomCenterOf(ventPos.above()));
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(!awarded(player, "sealed_room"), "Open vacuum counted as a sealed room");
      moon.setBlock(ventPos, LifeSupportRegistry.VENT.get().defaultBlockState(), 2);
      var vent = (OxygenBlockEntity) moon.getBlockEntity(ventPos);
      vent.setRoom(Set.of(ventPos.above(), ventPos.above(2)));
      vent.energy.receiveEnergy(1000, false);
      vent.tank.fill(
          new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 1000),
          IFluidHandler.FluidAction.EXECUTE);
      vent.tick();
      helper.assertTrue(vent.active, "Moon vent did not pressurize its room");
      PlanetEnvironment.tick(new PlayerTickEvent.Post(player));
      helper.assertTrue(awarded(player, "sealed_room"), "Breathing vent air was not noticed");
      moon.setBlock(ventPos, Blocks.AIR.defaultBlockState(), 2);

      Station station = new Station();
      station.deployed = true;
      station.orbitPlanet = GalaxyData.EARTH_ID;
      station.radius = 64;
      stationId = galaxy.newStation(station);
      int coordinate = 1600000 + (int) stationId * 256;
      station.x = coordinate;
      station.z = coordinate;
      var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
      BlockPos pos = new BlockPos(coordinate, 120, coordinate);
      space.setBlock(pos, OrbitalRegistry.WARP_CONTROLLER.get().defaultBlockState(), 2);
      var controller = (OrbitalBlockEntity) space.getBlockEntity(pos);
      ResearchType[] channels = {
        ResearchType.DISTANCE, ResearchType.MASS, ResearchType.COMPOSITION
      };
      chance.set(1);
      move(player, space, Vec3.atCenterOf(pos.above(2)));
      for (int round = 0; round < 2; round++) {
        controller.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.PLANET_CHIP.get()));
        for (int slot = 1; slot <= 3; slot++) {
          var unit = new ItemStack(OrbitalRegistry.DATA_UNIT.get());
          DataUnitItem.store(unit, channels[slot - 1], 100);
          controller.inventory.setStackInSlot(slot, unit);
        }
        if (round == 0) player.setPos(coordinate + 200, 122, coordinate);
        else player.setPos(coordinate + .5, 122, coordinate + .5);
        helper.assertTrue(PlanetDiscoveryLogic.start(controller), "Discovery did not start");
        for (int tick = 0; tick < 999; tick++) PlanetDiscoveryLogic.tick(controller);
        helper.assertTrue(
            PlanetIdChipItem.programmed(controller.inventory.getStackInSlot(0)),
            "Discovery fixture found no planet: " + controller.status);
        helper.assertTrue(
            awarded(player, "planet_discovery") == (round == 1),
            round == 0
                ? "Discovery awarded a player off the station"
                : "Discovery did not award a player aboard the station");
      }
      space.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    } finally {
      chance.set(previousChance);
      ((ServerLevelData) server.overworld().getLevelData()).setGameTime(previousTime);
      if (stationId >= 0) galaxy.stations.remove(stationId);
      galaxy.setDirty();
    }
    helper.succeed();
  }
}

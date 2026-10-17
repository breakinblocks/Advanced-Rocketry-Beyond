package advRocketry.life;

import advRocketry.ModTags;
import advRocketry.TestTags;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import advRocketry.transport.TransportRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class LifeSupportTests {
  @GameTest(template = "empty")
  public static void atmosphereImmunityUsesEntityTag(GameTestHelper helper) {
    helper.assertTrue(
        PlanetEnvironment.atmosphereBypass(EntityType.ARMOR_STAND)
            && !PlanetEnvironment.atmosphereBypass(EntityType.COW),
        "Armor stands lost their atmosphere immunity");
    try (var tag =
        TestTags.add(
            BuiltInRegistries.ENTITY_TYPE,
            ModTags.ATMOSPHERE_IMMUNE,
            EntityType.COW.builtInRegistryHolder())) {
      helper.assertTrue(
          PlanetEnvironment.atmosphereBypass(EntityType.COW)
              && !PlanetEnvironment.atmosphereBypass(EntityType.PIG),
          "Atmosphere immunity did not follow the entity tag");
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void ordinaryTorchesRequireCombustibleAir(GameTestHelper helper) {
    var earth = helper.getLevel();
    var moon =
        PlanetRuntime.create(earth.getServer(), GalaxyData.get(earth.getServer()).planets.get(1));
    BlockPos moonPos = helper.absolutePos(new BlockPos(5, 4, 5)).atY(120);
    moon.getChunkAt(moonPos);
    moon.setBlock(moonPos.below(), Blocks.STONE.defaultBlockState(), 2);
    moon.setBlock(moonPos, LightingRegistry.UNLIT_TORCH.get().defaultBlockState(), 2);
    LightingRegistry.relight(
        new ItemStack(Blocks.TORCH), moon, moonPos, moon.getBlockState(moonPos));
    helper.assertTrue(
        !LightingRegistry.combustible(moon, moonPos)
            && moon.getBlockState(moonPos).is(LightingRegistry.UNLIT_TORCH.get())
            && moon.getBlockState(moonPos).getLightEmission(moon, moonPos) == 0,
        "Ordinary torch lit in a vacuum");
    BlockPos earthPos = helper.absolutePos(new BlockPos(5, 4, 5));
    earth.setBlock(earthPos.below(), Blocks.STONE.defaultBlockState(), 2);
    earth.setBlock(earthPos, LightingRegistry.UNLIT_TORCH.get().defaultBlockState(), 2);
    LightingRegistry.relight(
        new ItemStack(Blocks.TORCH), earth, earthPos, earth.getBlockState(earthPos));
    helper.assertTrue(
        earth.getBlockState(earthPos).is(Blocks.TORCH),
        "Unlit torch did not relight in breathable air");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalLightsIlluminateWithoutOxygen(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos lamp = helper.absolutePos(new BlockPos(5, 4, 5));
    BlockPos torch = lamp.east();
    level.setBlock(lamp, LightingRegistry.CIRCLE_LIGHT.get().defaultBlockState(), 2);
    level.setBlock(torch, LightingRegistry.THERMITE_TORCH.get().defaultBlockState(), 2);
    helper.assertTrue(
        level.getBlockState(lamp).getLightEmission(level, lamp) == 15
            && level.getBlockState(torch).getLightEmission(level, torch) == 15,
        "Original station lamp and thermite torch did not illuminate");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void atmosphereDetectorTracksSelectedAir(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, LifeSupportRegistry.ATMOSPHERE_DETECTOR.get().defaultBlockState(), 2);
    AtmosphereDetectorBlockEntity detector =
        (AtmosphereDetectorBlockEntity) level.getBlockEntity(pos);
    detector.sample(level);
    helper.assertTrue(
        level.getBlockState(pos).getValue(AtmosphereDetectorBlock.POWERED),
        "Atmosphere detector did not emit redstone for ambient air");
    detector.cycle();
    detector.sample(level);
    helper.assertTrue(
        !level.getBlockState(pos).getValue(AtmosphereDetectorBlock.POWERED),
        "Atmosphere detector stayed powered for unpressurized air");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void suitWorkstationEditsStoredArmorModules(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
    level.setBlock(pos, LifeSupportRegistry.SUIT_WORKSTATION.get().defaultBlockState(), 2);
    SuitWorkstationBlockEntity station = (SuitWorkstationBlockEntity) level.getBlockEntity(pos);
    ItemStack armor = new ItemStack(LifeSupportRegistry.CHEST.get());
    station.suit.setStackInSlot(0, armor);
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    SuitWorkstationMenu menu = new SuitWorkstationMenu(1, player.getInventory(), station);
    ItemStack oxygenTank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    ItemStack jetpack = new ItemStack(ProcessingRegistry.PART_ITEMS.get("jetpack").get());
    helper.assertTrue(
        menu.getSlot(1).mayPlace(oxygenTank), "Workstation rejected a chest oxygen tank");
    helper.assertTrue(
        !menu.getSlot(1).mayPlace(jetpack), "Workstation accepted a jetpack in an oxygen slot");
    menu.getSlot(1).set(oxygenTank);
    menu.getSlot(3).set(jetpack);
    SpaceSuitItem suit = (SpaceSuitItem) armor.getItem();
    helper.assertTrue(
        suit.inventory(armor).getStackInSlot(0).is(oxygenTank.getItem())
            && suit.inventory(armor).getStackInSlot(2).is(jetpack.getItem()),
        "Workstation did not store modules in the armor item");
    menu.getSlot(3).set(ItemStack.EMPTY);
    helper.assertTrue(
        suit.inventory(armor).getStackInSlot(2).isEmpty(),
        "Workstation did not remove the armor module");
    player.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void jetpackUsesHydrogenForThrust(GameTestHelper helper) {
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    SpaceSuitItem suit = (SpaceSuitItem) chest.getItem();
    ItemStack tank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    PressureTankItem pressureTank = (PressureTankItem) tank.getItem();
    pressureTank
        .handler(tank)
        .fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 100),
            IFluidHandler.FluidAction.EXECUTE);
    suit.inventory(chest)
        .setStackInSlot(2, new ItemStack(ProcessingRegistry.PART_ITEMS.get("jetpack").get()));
    suit.inventory(chest).setStackInSlot(3, tank);
    player.setItemSlot(EquipmentSlot.CHEST, chest);
    double before = player.getDeltaMovement().y;
    helper.assertTrue(!JetpackControl.applyThrust(player), "Disabled jetpack fired");
    helper.assertTrue(JetpackToggle.toggle(player, false), "Jetpack enable key failed");
    helper.assertTrue(
        JetpackControl.applyThrust(player), "Jetpack did not fire with hydrogen fuel");
    helper.assertTrue(player.getDeltaMovement().y > before, "Jetpack did not lift the player");
    helper.assertTrue(
        ((PressureTankItem) suit.inventory(chest).getStackInSlot(3).getItem())
                .handler(suit.inventory(chest).getStackInSlot(3))
                .getFluid()
                .getAmount()
            == 99,
        "Jetpack did not consume exactly one mB of hydrogen");
    ItemStack helmet = new ItemStack(LifeSupportRegistry.HELMET.get());
    ((SpaceSuitItem) helmet.getItem())
        .inventory(helmet)
        .setStackInSlot(0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("hover_upgrade").get()));
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    player.setOnGround(false);
    player.setDeltaMovement(0, -0.3, 0);
    helper.assertTrue(!JetpackControl.applyHover(player), "Disabled hover mode activated");
    helper.assertTrue(JetpackToggle.toggle(player, true), "Hover mode key failed");
    helper.assertTrue(JetpackControl.applyHover(player), "Hover module did not arrest a fall");
    helper.assertTrue(
        player.getDeltaMovement().y == -0.1
            && ((PressureTankItem) suit.inventory(chest).getStackInSlot(3).getItem())
                    .handler(suit.inventory(chest).getStackInSlot(3))
                    .getFluid()
                    .getAmount()
                == 98,
        "Hover module did not set descent speed and consume hydrogen");
    player.discard();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalSuitUpgradesUseTheirIntendedSlots(GameTestHelper helper) {
    ItemStack helmet = new ItemStack(LifeSupportRegistry.HELMET.get());
    ItemStack legs = new ItemStack(LifeSupportRegistry.LEGS.get());
    ItemStack boots = new ItemStack(LifeSupportRegistry.BOOTS.get());
    SpaceSuitItem headSuit = (SpaceSuitItem) helmet.getItem();
    SpaceSuitItem legSuit = (SpaceSuitItem) legs.getItem();
    SpaceSuitItem bootSuit = (SpaceSuitItem) boots.getItem();
    ItemStack hover = new ItemStack(ProcessingRegistry.PART_ITEMS.get("hover_upgrade").get());
    ItemStack bionic = new ItemStack(ProcessingRegistry.PART_ITEMS.get("legs_upgrade").get());
    ItemStack landing =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("padded_boots_upgrade").get());
    helper.assertTrue(
        headSuit.inventory(helmet).isItemValid(0, hover), "Hover upgrade was rejected from helmet");
    helper.assertTrue(
        !legSuit.inventory(legs).isItemValid(0, hover), "Hover upgrade entered leggings");
    helper.assertTrue(
        legSuit.inventory(legs).isItemValid(0, bionic), "Bionic leg upgrade was rejected");
    helper.assertTrue(
        bootSuit.inventory(boots).isItemValid(0, landing), "Landing boots upgrade was rejected");
    legSuit.inventory(legs).setStackInSlot(0, bionic);
    helper.assertTrue(
        legSuit.countModule(legs, "legs_upgrade") == 1, "Bionic leg module was not retained");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void suitOxygenSurvivesComponentsAndConsumption(GameTestHelper helper) {
    ItemStack chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    var suit = (SpaceSuitItem) chest.getItem();
    ItemStack tank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    var module = suit.inventory(chest);
    helper.assertTrue(module.isItemValid(0, tank), "Empty oxygen tank was rejected");
    module.setStackInSlot(0, tank);
    FluidStack oxygen =
        new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 1500);
    helper.assertTrue(
        suit.transferOxygen(chest, oxygen, true) == 1500, "Suit could not accept oxygen");
    ItemStack restored =
        ItemStack.parseOptional(
            helper.getLevel().registryAccess(),
            (CompoundTag) chest.save(helper.getLevel().registryAccess()));
    helper.assertTrue(
        suit.oxygen(restored) == 1500, "Oxygen did not survive native item serialization");
    helper.assertTrue(
        suit.transferOxygen(restored, oxygen.copyWithAmount(1), false) == 1
            && suit.oxygen(restored) == 1499,
        "Suit oxygen consumption was lost or duplicated");
    ItemStack hydrogenTank = tank.copy();
    ((PressureTankItem) hydrogenTank.getItem())
        .handler(hydrogenTank)
        .fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 1000),
            IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(
        !suit.inventory(restored).isItemValid(1, hydrogenTank),
        "An oxygen-only suit slot accepted hydrogen");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void gasChargePadAcceptsHydrogenForJetpackTanks(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos chargerPos = helper.absolutePos(new BlockPos(5, 3, 5));
    BlockPos ventPos = chargerPos.east();
    level.setBlock(chargerPos, LifeSupportRegistry.CHARGER.get().defaultBlockState(), 2);
    level.setBlock(ventPos, LifeSupportRegistry.VENT.get().defaultBlockState(), 2);
    var charger = (OxygenBlockEntity) level.getBlockEntity(chargerPos);
    var vent = (OxygenBlockEntity) level.getBlockEntity(ventPos);
    FluidStack hydrogen =
        new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 1000);
    helper.assertTrue(
        charger.tank.fill(hydrogen, IFluidHandler.FluidAction.EXECUTE) == 1000,
        "Original gas charge pad rejected hydrogen");
    helper.assertTrue(
        vent.tank.fill(hydrogen, IFluidHandler.FluidAction.EXECUTE) == 0,
        "Oxygen vent accepted jetpack fuel");
    ItemStack chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    var suit = (SpaceSuitItem) chest.getItem();
    ItemStack tank =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    suit.inventory(chest).setStackInSlot(0, tank.copy());
    suit.inventory(chest).setStackInSlot(2, tank.copy());
    helper.assertTrue(
        suit.transferOxygen(chest, charger.tank.getFluid().copy(), true) == 1000,
        "Hydrogen did not fill a jetpack tank");
    helper.assertTrue(
        suit.inventory(chest).getStackInSlot(0).getItem() instanceof PressureTankItem oxygenTank
            && oxygenTank.handler(suit.inventory(chest).getStackInSlot(0)).getFluid().isEmpty(),
        "Hydrogen filled an oxygen-only slot");
    helper.assertTrue(
        suit.transferOxygen(chest, hydrogen.copyWithAmount(1), false) == 1,
        "Jetpack could not consume charge-pad fuel");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void oxygenRoomRejectsLeaks(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos start = helper.absolutePos(new BlockPos(5, 3, 5));
    for (int x = 0; x < 5; x++)
      for (int y = 0; y < 5; y++)
        for (int z = 0; z < 5; z++)
          level.setBlock(
              start.offset(x, y, z),
              x == 0 || x == 4 || y == 0 || y == 4 || z == 0 || z == 4
                  ? Blocks.GLASS.defaultBlockState()
                  : Blocks.AIR.defaultBlockState(),
              2);
    var sealed = SealedRooms.scan(level, start.offset(2, 2, 2));
    helper.assertTrue(sealed.size() == 27, "Sealed room volume is incorrect: " + sealed.size());
    BlockPos door = start.offset(4, 1, 2);
    level.setBlock(door, LifeSupportRegistry.AIRLOCK.get().defaultBlockState(), 2);
    level.setBlock(
        door.above(),
        LifeSupportRegistry.AIRLOCK
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER),
        2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).size() == 27,
        "Closed airlock did not seal the room");
    level.setBlock(door, level.getBlockState(door).setValue(BlockStateProperties.OPEN, true), 2);
    level.setBlock(
        door.above(),
        level.getBlockState(door.above()).setValue(BlockStateProperties.OPEN, true),
        2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).isEmpty(),
        "Open airlock did not expose the room");
    level.setBlock(door, level.getBlockState(door).setValue(BlockStateProperties.OPEN, false), 2);
    level.setBlock(
        door.above(),
        level.getBlockState(door.above()).setValue(BlockStateProperties.OPEN, false),
        2);
    BlockPos pipeGap = start.offset(0, 2, 2);
    level.setBlock(pipeGap, Blocks.AIR.defaultBlockState(), 2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).isEmpty(), "Open pipe frame did not leak");
    for (BlockPos frame :
        new BlockPos[] {pipeGap.above(), pipeGap.below(), pipeGap.north(), pipeGap.south()})
      level.setBlock(frame, LifeSupportRegistry.PIPE_SEALER.get().defaultBlockState(), 2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).size() == 27,
        "Original four-piece pipe sealer frame did not close the gap");
    for (var kind : TransportRegistry.Kind.values()) {
      level.setBlock(pipeGap, TransportRegistry.ALL.get(kind).get().defaultBlockState(), 2);
      helper.assertTrue(
          SealedRooms.scan(level, start.offset(2, 2, 2)).size() == 27,
          "Pipe sealer frame must keep the room airtight around " + kind);
    }
    level.setBlock(pipeGap.above(), Blocks.GLASS.defaultBlockState(), 2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).isEmpty(),
        "Broken pipe sealer frame stayed airtight");
    level.setBlock(pipeGap, Blocks.GLASS.defaultBlockState(), 2);
    BlockPos ventPos = start.offset(0, 2, 2);
    level.setBlock(ventPos, LifeSupportRegistry.VENT.get().defaultBlockState(), 2);
    var vent = (OxygenBlockEntity) level.getBlockEntity(ventPos);
    vent.setRoom(sealed);
    vent.energy.receiveEnergy(1000, false);
    vent.tank.fill(
        new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 1000),
        IFluidHandler.FluidAction.EXECUTE);
    vent.tick();
    helper.assertTrue(
        vent.active && SealedRooms.breathable(level, start.offset(2, 2, 2)),
        "Powered oxygen vent did not pressurize the room");
    helper.assertTrue(
        vent.getUpdatePacket() != null
            && vent.getUpdateTag(level.registryAccess()).getBoolean("active"),
        "Vent activity was not synchronized for client effects");
    BlockPos firstScrubber = ventPos.west();
    BlockPos secondScrubber = ventPos.north();
    level.setBlock(firstScrubber, LifeSupportRegistry.CARBON_SCRUBBER.get().defaultBlockState(), 2);
    level.setBlock(
        secondScrubber, LifeSupportRegistry.CARBON_SCRUBBER.get().defaultBlockState(), 2);
    var first = (CarbonScrubberBlockEntity) level.getBlockEntity(firstScrubber);
    var second = (CarbonScrubberBlockEntity) level.getBlockEntity(secondScrubber);
    first.cartridge.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("carbon_scrubber_cartridge").get()));
    second.cartridge.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("carbon_scrubber_cartridge").get()));
    helper.assertTrue(
        first.comparatorOutput() == 15 && first.useCharge(),
        "Original scrubber cartridge did not expose charge or accept wear");
    int oxygenBefore = vent.tank.getFluidAmount();
    int energyBefore = vent.energy.getEnergyStored();
    vent.tick();
    helper.assertTrue(
        vent.active
            && vent.tank.getFluidAmount() == oxygenBefore
            && vent.energy.getEnergyStored() == energyBefore - 21,
        "Two scrubbers did not exchange extra power for zero oxygen consumption");
    level.setBlock(start.offset(4, 2, 2), Blocks.AIR.defaultBlockState(), 2);
    helper.assertTrue(
        SealedRooms.scan(level, start.offset(2, 2, 2)).isEmpty(), "A leaking room remained sealed");
    vent.energy.setEnergy(0);
    vent.tick();
    helper.assertTrue(
        !vent.active && !vent.getUpdateTag(level.registryAccess()).getBoolean("active"),
        "An unpowered vent continued supplying oxygen or its client effect");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void analyzerHelmetModuleAndOxygenGaugeSurviveItemSerialization(
      GameTestHelper helper) {
    ItemStack helmet = new ItemStack(LifeSupportRegistry.HELMET.get());
    ItemStack analyzer = new ItemStack(ProcessingRegistry.PART_ITEMS.get("atm_analyzer").get());
    var helmetModules = LifeSupportRegistry.HELMET.get().inventory(helmet);
    helper.assertTrue(
        helmetModules.isItemValid(0, analyzer),
        "Helmet rejected the original atmosphere analyzer component");
    helmetModules.setStackInSlot(0, analyzer);
    ItemStack restored =
        ItemStack.parse(
                helper.getLevel().registryAccess(), helmet.save(helper.getLevel().registryAccess()))
            .orElseThrow();
    helper.assertTrue(
        LifeSupportRegistry.HELMET.get().countModule(restored, "atm_analyzer") == 1,
        "Serialized helmet lost its atmosphere analyzer");
    ItemStack chest = new ItemStack(LifeSupportRegistry.CHEST.get());
    var modules = LifeSupportRegistry.CHEST.get().inventory(chest);
    helper.assertTrue(
        !modules.isItemValid(0, analyzer) && !modules.isItemValid(2, analyzer),
        "Analyzer was accepted outside the original helmet equipment slot");
    ItemStack oxygen =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_steel").get());
    PressureTankItem tank = (PressureTankItem) oxygen.getItem();
    tank.handler(oxygen)
        .fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 500),
            IFluidHandler.FluidAction.EXECUTE);
    ItemStack empty = new ItemStack(tank);
    ItemStack hydrogen = new ItemStack(tank);
    tank.handler(hydrogen)
        .fill(
            new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 500),
            IFluidHandler.FluidAction.EXECUTE);
    modules.setStackInSlot(0, oxygen);
    modules.setStackInSlot(1, empty);
    modules.setStackInSlot(2, hydrogen);
    helper.assertTrue(
        LifeSupportRegistry.CHEST.get().oxygen(chest) == 500
            && LifeSupportRegistry.CHEST.get().oxygenCapacity(chest)
                == tank.handler(oxygen).getTankCapacity(0) * 2,
        "Oxygen gauge counted hydrogen or omitted an empty oxygen tank");
    restored =
        ItemStack.parse(
                helper.getLevel().registryAccess(), chest.save(helper.getLevel().registryAccess()))
            .orElseThrow();
    helper.assertTrue(
        LifeSupportRegistry.CHEST.get().oxygen(restored) == 500
            && LifeSupportRegistry.CHEST.get().oxygenCapacity(restored)
                == LifeSupportRegistry.CHEST.get().oxygenCapacity(chest),
        "Oxygen gauge capacity or fill changed after suit serialization");
    helper.succeed();
  }
}

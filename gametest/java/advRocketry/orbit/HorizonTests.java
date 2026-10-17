package advRocketry.orbit;

import advRocketry.ModComponents;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import advRocketry.space.Star;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class HorizonTests {
  @GameTest(template = "empty")
  public static void horizonAssemblyAndPersistence(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
    level.setBlock(pos, OrbitalRegistry.BUILDER.get().defaultBlockState(), 2);
    level.setBlock(pos.below(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    OrbitalBlockEntity builder = (OrbitalBlockEntity) level.getBlockEntity(pos);
    builder.inventory.setStackInSlot(7, new ItemStack(OrbitalRegistry.SATELLITE.get()));
    builder.inventory.setStackInSlot(0, new ItemStack(OrbitalRegistry.MICROWAVE_TRANSMITTER.get()));
    builder.inventory.setStackInSlot(8, new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get()));
    for (int slot = 1; slot <= 6; slot++)
      builder.inventory.setStackInSlot(slot, new ItemStack(OrbitalRegistry.HORIZON_MODULE.get()));
    helper.assertTrue(
        SatelliteBuilderLogic.build(builder),
        "Horizon satellite must assemble without solar generation");
    SatelliteProperties properties =
        builder.inventory.getStackInSlot(10).get(ModComponents.SATELLITE);
    helper.assertTrue(
        properties.type() == SatelliteType.HORIZON
            && properties.horizonBonus() == 90
            && properties.powerGeneration() == 0,
        "Six Horizon modules must give +90% and no power");
    var encoded = SatelliteProperties.CODEC.encodeStart(JsonOps.INSTANCE, properties).getOrThrow();
    helper.assertTrue(
        SatelliteProperties.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().equals(properties),
        "Component save codec lost Horizon bonus");
    var buffer = Unpooled.buffer();
    try {
      SatelliteProperties.STREAM_CODEC.encode(buffer, properties);
      helper.assertTrue(
          SatelliteProperties.STREAM_CODEC.decode(buffer).equals(properties),
          "Network codec lost Horizon bonus");
    } finally {
      buffer.release();
    }
    encoded.getAsJsonObject().remove("horizon_bonus");
    helper.assertTrue(
        SatelliteProperties.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().horizonBonus() == 0,
        "Old satellite components need a zero bonus default");
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    Satellite record = galaxy.satellites.get(properties.id());
    record.deployed = true;
    record.horizonTarget = GlobalPos.of(level.dimension(), pos);
    record.energy = 100;
    SatelliteLogic.advance(galaxy, properties.id(), record.lastTick + 100);
    helper.assertTrue(record.energy == 0, "Horizon satellite produced or retained usable power");
    Satellite restored = Satellite.load(record.save());
    helper.assertTrue(
        restored.horizonBonus == 90 && record.horizonTarget.equals(restored.horizonTarget),
        "Satellite persistence lost bonus or exclusive target");
    helper.assertTrue(
        OrbitalRegistry.generation(new ItemStack(OrbitalRegistry.BASIC_SOLAR_PANEL.get())) == 32
            && OrbitalRegistry.generation(new ItemStack(OrbitalRegistry.LARGE_SOLAR_PANEL.get()))
                == 160,
        "Solar module data map rates are incorrect");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void horizonLinkRequiresValidOrbitAndCannotBeDuplicated(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    galaxy.stars.put(941, new Star(941, "Horizon test", 100, 1, 0, 0, true));
    Planet planet = new Planet(941, "Horizon orbit");
    planet.star = 941;
    galaxy.planets.put(941, planet);
    ServerLevel space =
        PlanetRuntime.create(
            helper.getLevel().getServer(), galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5)).atY(180);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = 941;
    station.x = pos.getX();
    station.z = pos.getZ();
    galaxy.newStation(station);
    space.setBlock(pos, OrbitalRegistry.BLACK_HOLE_GENERATOR.get().defaultBlockState(), 2);
    space.setBlock(pos.east(), OrbitalRegistry.BLACK_HOLE_GENERATOR.get().defaultBlockState(), 2);
    OrbitalBlockEntity first = (OrbitalBlockEntity) space.getBlockEntity(pos);
    OrbitalBlockEntity second = (OrbitalBlockEntity) space.getBlockEntity(pos.east());
    Satellite satellite = new Satellite(SatelliteType.HORIZON, 0, 720, 0);
    satellite.horizonBonus = 90;
    satellite.orbitPlanet = 941;
    long id = galaxy.newSatellite(satellite);
    ItemStack chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    first.inventory.setStackInSlot(0, chip.copy());
    helper.assertTrue(
        HorizonSatelliteLogic.bonus(first) == 0, "Undeployed satellite applied a bonus");
    satellite.deployed = true;
    helper.assertTrue(
        HorizonSatelliteLogic.bonus(first) == 90,
        "Valid Horizon satellite did not apply its additive bonus");
    helper.assertTrue(
        HorizonSatelliteLogic.amplifiedOutput(500, first.horizonBonus) == 950
            && HorizonSatelliteLogic.amplifiedOutput(500, 15) == 575,
        "Horizon output is not additive");
    second.inventory.setStackInSlot(0, chip.copy());
    helper.assertTrue(
        HorizonSatelliteLogic.bonus(second) == 0, "Copied chip duplicated a satellite bonus");
    first.inventory.setStackInSlot(0, ItemStack.EMPTY);
    helper.assertTrue(
        satellite.horizonTarget == null && HorizonSatelliteLogic.bonus(second) == 90,
        "Removing the chip did not release the exclusive link");
    satellite.orbitPlanet = GalaxyData.EARTH_ID;
    helper.assertTrue(HorizonSatelliteLogic.bonus(second) == 0, "Wrong orbit retained its bonus");
    satellite.orbitPlanet = 941;
    galaxy.stars.put(941, new Star(941, "Normal test star", 100, 1, 0, 0, false));
    helper.assertTrue(
        HorizonSatelliteLogic.bonus(second) == 0, "Normal star applied a Horizon bonus");
    galaxy.stars.put(941, new Star(941, "Horizon test", 100, 1, 0, 0, true));
    satellite.deployed = false;
    helper.assertTrue(
        HorizonSatelliteLogic.bonus(second) == 0, "Recovered satellite retained its bonus");
    satellite.deployed = true;
    OrbitalBlock.cleanup(second);
    helper.assertTrue(
        satellite.horizonTarget == null, "Breaking a generator retained its exclusive link");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void horizonEnablesPlanetaryGenerationOnlyWhileLinked(GameTestHelper helper) {
    GalaxyData galaxy = GalaxyData.get(helper.getLevel().getServer());
    galaxy.stars.put(942, new Star(942, "Planetary Horizon test", 100, 1, 0, 0, true));
    Planet planet = new Planet(942, "Horizon surface");
    planet.star = 942;
    galaxy.planets.put(942, planet);
    ServerLevel level = PlanetRuntime.create(helper.getLevel().getServer(), planet);
    BlockPos pos = new BlockPos(0, 120, 0);
    level.setBlock(pos, OrbitalRegistry.BLACK_HOLE_GENERATOR.get().defaultBlockState(), 2);
    BlockPos back = pos.south();
    for (BlockPos cell :
        new BlockPos[] {
          back.below(), back, pos.above(), back.above(), back.above(2), back.above(3), back.south()
        }) level.setBlock(cell, MachinePorts.BLOCK_ADVANCED_STRUCTURE.get().defaultBlockState(), 2);
    level.setBlock(back.east(), MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState(), 2);
    level.setBlock(
        back.west(), MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get().defaultBlockState(), 2);
    var input = (PortBlockEntity) level.getBlockEntity(back.east());
    input.inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
    var generator = (OrbitalBlockEntity) level.getBlockEntity(pos);
    helper.assertTrue(
        BlackHoleGeneratorLogic.complete(generator), "Planetary test frame is incomplete");
    BlackHoleGeneratorLogic.tick(generator);
    helper.assertTrue(
        generator.powerMadeLastTick == 0 && input.inventory.getStackInSlot(0).getCount() == 2,
        "Planetary generator operated without a Horizon link");
    Satellite satellite = new Satellite(SatelliteType.HORIZON, 0, 720, 0);
    satellite.horizonBonus = 90;
    satellite.deployed = true;
    satellite.orbitPlanet = 942;
    long id = galaxy.newSatellite(satellite);
    ItemStack chip = new ItemStack(OrbitalRegistry.SATELLITE_CHIP.get());
    chip.set(ModComponents.SATELLITE_LINK, new SatelliteLink(id));
    generator.inventory.setStackInSlot(0, chip);
    BlackHoleGeneratorLogic.tick(generator);
    helper.assertTrue(
        generator.powerMadeLastTick == 950 && input.inventory.getStackInSlot(0).getCount() == 1,
        "Linked planetary generator must burn one stone for 950 FE");
    generator.inventory.setStackInSlot(0, ItemStack.EMPTY);
    BlackHoleGeneratorLogic.tick(generator);
    helper.assertTrue(
        generator.powerMadeLastTick == 0 && input.inventory.getStackInSlot(0).getCount() == 1,
        "Removing Horizon chip did not stop planetary generation");
    helper.succeed();
  }
}

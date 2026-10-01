// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.DataMaps;
import advRocketry.Main;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingRegistry;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native registrations for the original satellite chassis, builder, hatch and modules. */
public final class OrbitalRegistry {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  private static final DeferredRegister<EntityType<?>> VEHICLES =
      DeferredRegister.create(Registries.ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, Main.MODID);
  public static final ResourceKey<Level> SPACE_DIMENSION =
      ResourceKey.create(
          Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Main.MODID, "space"));
  public static final Supplier<SoundEvent> LASER_SOUND = sound("laserdrill");
  public static final Supplier<SoundEvent> GRAVITY_SOUND = sound("gravityohhh");
  public static final Supplier<SoundEvent> TERRAFORMER_SOUND = sound("machinelarge");
  public static final Supplier<SoundEvent> SELECTOR_BUTTON_SOUND = sound("buttonblipa");
  public static final Supplier<EntityType<ElevatorCapsule>> ELEVATOR_CAPSULE =
      VEHICLES.register(
          "elevator_capsule",
          () ->
              EntityType.Builder.of(ElevatorCapsule::new, MobCategory.MISC)
                  .sized(1.8f, 2.2f)
                  .clientTrackingRange(12)
                  .updateInterval(1)
                  .build("elevator_capsule"));
  public static final Supplier<EntityType<LaserNodeEntity>> LASER_NODE =
      VEHICLES.register(
          "laser_node",
          () ->
              EntityType.Builder.of(LaserNodeEntity::new, MobCategory.MISC)
                  .sized(2, 2)
                  .noSave()
                  .clientTrackingRange(16)
                  .updateInterval(1)
                  .build("laser_node"));
  public static final Supplier<EntityType<HologramBodyEntity>> HOLOGRAM_BODY =
      VEHICLES.register(
          "hologram_body",
          () ->
              EntityType.Builder.of(HologramBodyEntity::new, MobCategory.MISC)
                  .sized(.3f, .3f)
                  .noSave()
                  .clientTrackingRange(12)
                  .updateInterval(1)
                  .build("hologram_body"));
  public static final Supplier<EntityType<RailgunCargoEntity>> RAILGUN_CARGO =
      VEHICLES.register(
          "railgun_cargo",
          () ->
              EntityType.Builder.of(RailgunCargoEntity::new, MobCategory.MISC)
                  .sized(.25f, .25f)
                  .noSave()
                  .clientTrackingRange(8)
                  .updateInterval(1)
                  .build("railgun_cargo"));

  public static final Supplier<OrbitalBlock> BUILDER =
      block("satellite_builder", OrbitalBlock.Kind.BUILDER);
  public static final Supplier<OrbitalBlock> HATCH =
      block("satellite_hatch", OrbitalBlock.Kind.HATCH);
  public static final Supplier<OrbitalBlock> TERMINAL =
      block("satellite_terminal", OrbitalBlock.Kind.TERMINAL);
  public static final Supplier<OrbitalBlock> MICROWAVE_RECEIVER =
      block("microwave_receiver", OrbitalBlock.Kind.MICROWAVE_RECEIVER);
  public static final Supplier<HorizontalOrbitalBlock> SOLAR_ARRAY =
      horizontal("solar_array", OrbitalBlock.Kind.SOLAR_ARRAY, 3);
  public static final Supplier<HorizontalOrbitalBlock> OBSERVATORY =
      horizontal("observatory", OrbitalBlock.Kind.OBSERVATORY, 3);
  public static final Supplier<HorizontalOrbitalBlock> SPACE_ELEVATOR =
      horizontal("space_elevator", OrbitalBlock.Kind.SPACE_ELEVATOR, 4);
  public static final Supplier<OrbitalBlock> AREA_GRAVITY_CONTROLLER =
      block("area_gravity_controller", OrbitalBlock.Kind.AREA_GRAVITY_CONTROLLER);
  public static final Supplier<HorizontalOrbitalBlock> ORBITAL_LASER =
      horizontal("orbital_laser", OrbitalBlock.Kind.ORBITAL_LASER, 4);
  public static final Supplier<OrbitalBlock> STATION_GRAVITY_CONTROLLER =
      block("station_gravity_controller", OrbitalBlock.Kind.STATION_GRAVITY_CONTROLLER);
  public static final Supplier<HorizontalOrbitalBlock> BLACK_HOLE_GENERATOR =
      horizontal("black_hole_generator", OrbitalBlock.Kind.BLACK_HOLE_GENERATOR, 4);
  public static final Supplier<OrbitalBlock> BIOME_SCANNER =
      block("biome_scanner", OrbitalBlock.Kind.BIOME_SCANNER);
  public static final Supplier<OrbitalBlock> BEACON = block("beacon", OrbitalBlock.Kind.BEACON);
  public static final Supplier<DockingPortBlock> DOCKING_PORT =
      block("docking_port", () -> new DockingPortBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<HorizontalOrbitalBlock> RAILGUN =
      horizontal("railgun", OrbitalBlock.Kind.RAILGUN, 4);
  public static final Supplier<WirelessTransceiverBlock> WIRELESS_TRANSCEIVER =
      block(
          "wireless_transceiver",
          () -> new WirelessTransceiverBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<LandingPadBlock> LANDING_PAD =
      block("landing_pad", () -> new LandingPadBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<HorizontalOrbitalBlock> ASTROBODY_PROCESSOR =
      horizontal("astrobody_data_processor", OrbitalBlock.Kind.ASTROBODY_PROCESSOR, 3);
  public static final Supplier<HorizontalOrbitalBlock> ATMOSPHERE_TERRAFORMER =
      horizontal("atmosphere_terraformer", OrbitalBlock.Kind.ATMOSPHERE_TERRAFORMER, 4);
  public static final Supplier<Block> SOLAR_ARRAY_PANEL =
      block("solar_array_panel", () -> new Block(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<LaserLightBlock> LASER_LIGHT =
      BLOCKS.register(
          "laser_light",
          () ->
              new LaserLightBlock(
                  BlockBehaviour.Properties.of()
                      .noCollission()
                      .replaceable()
                      .lightLevel(state -> 15)
                      .noLootTable()));
  public static final Supplier<HorizontalOrbitalBlock> WARP_CORE =
      horizontal("warp_core", OrbitalBlock.Kind.WARP_CORE, 4);
  public static final Supplier<OrbitalBlock> WARP_CONTROLLER =
      block("warp_controller", OrbitalBlock.Kind.WARP_CONTROLLER);
  public static final Supplier<OrbitalBlock> PLANET_SELECTOR =
      block("planet_selector", OrbitalBlock.Kind.PLANET_SELECTOR);
  public static final Supplier<OrbitalBlock> HOLOGRAPHIC_SELECTOR =
      block(
          "holographic_planet_selector",
          () ->
              new OrbitalBlock(
                  OrbitalBlock.Kind.HOLOGRAPHIC_SELECTOR,
                  BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final Supplier<OrbitalBlock> STATION_ALTITUDE_CONTROLLER =
      block("station_altitude_controller", OrbitalBlock.Kind.STATION_ALTITUDE_CONTROLLER);
  public static final Supplier<OrbitalBlock> STATION_ORIENTATION_CONTROLLER =
      block("station_orientation_controller", OrbitalBlock.Kind.STATION_ORIENTATION_CONTROLLER);
  public static final Supplier<ForceFieldProjectorBlock> FORCE_FIELD_PROJECTOR =
      block(
          "force_field_projector",
          () -> new ForceFieldProjectorBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<Block> FORCE_FIELD =
      BLOCKS.register(
          "force_field",
          () ->
              new Block(
                  BlockBehaviour.Properties.of()
                      .strength(-1, 3600000)
                      .noOcclusion()
                      .noLootTable()));
  public static final Supplier<PumpBlock> PUMP =
      block("fluid_pump", () -> new PumpBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<FluidTankBlock> FLUID_TANK =
      block("liquid_tank", () -> new FluidTankBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<SatelliteItem> SATELLITE =
      ITEMS.register("satellite", () -> new SatelliteItem(new Item.Properties()));
  public static final Supplier<AsteroidChipItem> ASTEROID_CHIP =
      ITEMS.register("asteroid_chip", () -> new AsteroidChipItem(new Item.Properties()));
  public static final Supplier<PlanetIdChipItem> PLANET_CHIP =
      ITEMS.register("planet_id_chip", () -> new PlanetIdChipItem(new Item.Properties()));
  public static final Supplier<OreScannerItem> ORE_SCANNER =
      ITEMS.register("ore_scanner", () -> new OreScannerItem(new Item.Properties()));
  public static final Supplier<StationItem> STATION =
      ITEMS.register("space_station", () -> new StationItem(new Item.Properties()));
  public static final Supplier<StationChipItem> STATION_CHIP =
      ITEMS.register("station_id_chip", () -> new StationChipItem(new Item.Properties()));
  public static final Supplier<Item> COMPOSITION_SENSOR = item("composition_sensor");
  public static final Supplier<Item> MASS_DETECTOR = item("mass_detector");
  public static final Supplier<Item> MICROWAVE_TRANSMITTER = item("microwave_transmitter");
  public static final Supplier<Item> ORE_MAPPER = item("ore_mapper");
  public static final Supplier<Item> BASIC_SOLAR_PANEL = item("basic_satellite_solar_panel");
  public static final Supplier<Item> LARGE_SOLAR_PANEL = item("large_satellite_solar_panel");
  public static final Supplier<Item> DATA_UNIT =
      ProcessingRegistry.PART_ITEMS.get("item_data_storage");
  public static final Supplier<Item> SATELLITE_CHIP =
      ProcessingRegistry.PART_ITEMS.get("satellite_id_chip");
  public static final Supplier<BlockEntityType<OrbitalBlockEntity>> BLOCK_ENTITY =
      ENTITIES.register(
          "orbital_machine",
          () ->
              BlockEntityType.Builder.of(
                      OrbitalBlockEntity::new,
                      BUILDER.get(),
                      HATCH.get(),
                      TERMINAL.get(),
                      MICROWAVE_RECEIVER.get(),
                      SOLAR_ARRAY.get(),
                      BLACK_HOLE_GENERATOR.get(),
                      OBSERVATORY.get(),
                      SPACE_ELEVATOR.get(),
                      AREA_GRAVITY_CONTROLLER.get(),
                      ORBITAL_LASER.get(),
                      BIOME_SCANNER.get(),
                      BEACON.get(),
                      DOCKING_PORT.get(),
                      RAILGUN.get(),
                      ASTROBODY_PROCESSOR.get(),
                      ATMOSPHERE_TERRAFORMER.get(),
                      STATION_GRAVITY_CONTROLLER.get(),
                      WARP_CORE.get(),
                      WARP_CONTROLLER.get(),
                      PLANET_SELECTOR.get(),
                      HOLOGRAPHIC_SELECTOR.get(),
                      STATION_ALTITUDE_CONTROLLER.get(),
                      STATION_ORIENTATION_CONTROLLER.get(),
                      FORCE_FIELD_PROJECTOR.get())
                  .build(null));
  public static final Supplier<BlockEntityType<PumpBlockEntity>> PUMP_ENTITY =
      ENTITIES.register(
          "fluid_pump",
          () -> BlockEntityType.Builder.of(PumpBlockEntity::new, PUMP.get()).build(null));
  public static final Supplier<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK_ENTITY =
      ENTITIES.register(
          "liquid_tank",
          () ->
              BlockEntityType.Builder.of(FluidTankBlockEntity::new, FLUID_TANK.get()).build(null));
  public static final Supplier<BlockEntityType<WirelessTransceiverBlockEntity>> WIRELESS_ENTITY =
      ENTITIES.register(
          "wireless_transceiver",
          () ->
              BlockEntityType.Builder.of(
                      WirelessTransceiverBlockEntity::new, WIRELESS_TRANSCEIVER.get())
                  .build(null));
  public static final Supplier<BlockEntityType<LandingPadBlockEntity>> LANDING_PAD_ENTITY =
      ENTITIES.register(
          "landing_pad",
          () ->
              BlockEntityType.Builder.of(LandingPadBlockEntity::new, LANDING_PAD.get())
                  .build(null));
  public static final Supplier<MenuType<OrbitalMenu>> MENU =
      MENUS.register("orbital_machine", () -> IMenuTypeExtension.create(OrbitalMenu::new));
  public static final Supplier<MenuType<OreScanMenu>> ORE_SCAN_MENU =
      MENUS.register("ore_scan", () -> IMenuTypeExtension.create(OreScanMenu::new));

  public static final Supplier<MenuType<StationChipMenu>> STATION_CHIP_MENU =
      MENUS.register("station_chip", () -> IMenuTypeExtension.create(StationChipMenu::new));

  private OrbitalRegistry() {}

  private static Supplier<SoundEvent> sound(String name) {
    return SOUNDS.register(
        name,
        () ->
            SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Main.MODID, name)));
  }

  private static <T extends Block> Supplier<T> block(String name, Supplier<T> factory) {
    Supplier<T> block = BLOCKS.register(name, factory);
    ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    return block;
  }

  private static Supplier<OrbitalBlock> block(String name, OrbitalBlock.Kind kind) {
    return block(name, () -> new OrbitalBlock(kind, BlockBehaviour.Properties.of().strength(3)));
  }

  private static Supplier<HorizontalOrbitalBlock> horizontal(
      String name, OrbitalBlock.Kind kind, float strength) {
    return block(
        name,
        () -> new HorizontalOrbitalBlock(kind, BlockBehaviour.Properties.of().strength(strength)));
  }

  private static Supplier<Item> item(String name) {
    return ITEMS.register(name, () -> new Item(new Item.Properties()));
  }

  public static SatelliteType type(ItemStack stack) {
    if (stack.is(ProcessingRegistry.PART_ITEMS.get("optical_sensor").get()))
      return SatelliteType.OPTICAL;
    if (stack.is(COMPOSITION_SENSOR.get())) return SatelliteType.COMPOSITION;
    if (stack.is(MASS_DETECTOR.get())) return SatelliteType.MASS;
    if (stack.is(MICROWAVE_TRANSMITTER.get())) return SatelliteType.SOLAR_ENERGY;
    if (stack.is(ORE_MAPPER.get())) return SatelliteType.ORE_SCANNER;
    if (stack.is(ProcessingRegistry.PART_ITEMS.get("satellite_biome_changer").get()))
      return SatelliteType.BIOME_CHANGER;
    return null;
  }

  public static int generation(ItemStack stack) {
    DataMaps.SatelliteModule module = satelliteModule(stack);
    return module == null ? 0 : module.powerGeneration();
  }

  private static DataMaps.SatelliteModule satelliteModule(ItemStack stack) {
    return stack.isEmpty() ? null : stack.getItemHolder().getData(DataMaps.SATELLITE_MODULE);
  }

  public static int storage(ItemStack stack) {
    DataMaps.SatelliteModule module = satelliteModule(stack);
    return module == null ? 0 : module.energyStorage();
  }

  public static int data(ItemStack stack) {
    DataMaps.SatelliteModule module = satelliteModule(stack);
    return module == null ? 0 : module.dataStorage();
  }

  public static boolean module(ItemStack stack) {
    return stack.isEmpty() || satelliteModule(stack) != null;
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    MENUS.register(bus);
    VEHICLES.register(bus);
    SOUNDS.register(bus);
    bus.addListener(OrbitalRegistry::capabilities);
    bus.addListener(OrbitalRegistry::creative);
    bus.addListener(BeaconFinderItem::register);
  }

  private static void capabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK, PUMP_ENTITY.get(), (entity, side) -> entity.energy);
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK, PUMP_ENTITY.get(), (entity, side) -> entity.outputTank());
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        FLUID_TANK_ENTITY.get(),
        (entity, side) -> entity.handler());
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK, BLOCK_ENTITY.get(), (entity, side) -> entity.inventory);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK, LANDING_PAD_ENTITY.get(), (entity, side) -> entity.linker);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        BLOCK_ENTITY.get(),
        (entity, side) ->
            entity.hatch() || entity.stationGravityController() ? null : entity.energy);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() != MachinePorts.TAB.get()) return;
    event.accept(BUILDER.get());
    event.accept(HATCH.get());
    event.accept(TERMINAL.get());
    event.accept(MICROWAVE_RECEIVER.get());
    event.accept(SOLAR_ARRAY.get());
    event.accept(BLACK_HOLE_GENERATOR.get());
    event.accept(OBSERVATORY.get());
    event.accept(SPACE_ELEVATOR.get());
    event.accept(AREA_GRAVITY_CONTROLLER.get());
    event.accept(ORBITAL_LASER.get());
    event.accept(STATION_GRAVITY_CONTROLLER.get());
    event.accept(SOLAR_ARRAY_PANEL.get());
    event.accept(WARP_CORE.get());
    event.accept(WARP_CONTROLLER.get());
    event.accept(PLANET_SELECTOR.get());
    event.accept(HOLOGRAPHIC_SELECTOR.get());
    event.accept(STATION_ALTITUDE_CONTROLLER.get());
    event.accept(STATION_ORIENTATION_CONTROLLER.get());
    event.accept(FORCE_FIELD_PROJECTOR.get());
    event.accept(PUMP.get());
    event.accept(FLUID_TANK.get());
    event.accept(BIOME_SCANNER.get());
    event.accept(BEACON.get());
    event.accept(DOCKING_PORT.get());
    event.accept(RAILGUN.get());
    event.accept(WIRELESS_TRANSCEIVER.get());
    event.accept(LANDING_PAD.get());
    event.accept(ASTROBODY_PROCESSOR.get());
    event.accept(ATMOSPHERE_TERRAFORMER.get());
    for (Supplier<? extends Item> part :
        List.<Supplier<? extends Item>>of(
            SATELLITE,
            ASTEROID_CHIP,
            PLANET_CHIP,
            ORE_SCANNER,
            STATION,
            STATION_CHIP,
            COMPOSITION_SENSOR,
            MASS_DETECTOR,
            MICROWAVE_TRANSMITTER,
            ORE_MAPPER,
            BASIC_SOLAR_PANEL,
            LARGE_SOLAR_PANEL)) event.accept(part.get());
  }
}

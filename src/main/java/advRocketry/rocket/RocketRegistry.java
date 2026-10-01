// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.Main;
import advRocketry.processing.FluidContainers;
import advRocketry.processing.MachinePorts;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RocketRegistry {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  private static final DeferredRegister<EntityType<?>> ROCKETS =
      DeferredRegister.create(Registries.ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, Main.MODID);
  public static final Supplier<SoundEvent> ENGINE_SOUND =
      SOUNDS.register(
          "combustionrocket",
          () ->
              SoundEvent.createVariableRangeEvent(
                  ResourceLocation.fromNamespaceAndPath(Main.MODID, "combustionrocket")));
  public static final Supplier<EntityType<RocketEntity>> ROCKET =
      ROCKETS.register(
          "rocket",
          () ->
              EntityType.Builder.of(RocketEntity::new, MobCategory.MISC)
                  .sized(1, 1)
                  .clientTrackingRange(16)
                  .updateInterval(1)
                  .build("rocket"));
  public static final Supplier<EntityType<SeatEntity>> SEAT_MOUNT =
      ROCKETS.register(
          "seat_mount",
          () ->
              EntityType.Builder.of(SeatEntity::new, MobCategory.MISC)
                  .sized(.1f, .1f)
                  .clientTrackingRange(16)
                  .updateInterval(1)
                  .build("seat_mount"));
  public static final Map<String, Supplier<RocketPartBlock>> PARTS = new LinkedHashMap<>();
  public static final Supplier<Block> LANDING_FLOAT =
      BLOCKS.register("landing_float", () -> new Block(BlockBehaviour.Properties.of().strength(1)));
  public static final Supplier<RocketFireBlock> ROCKET_FIRE =
      BLOCKS.register(
          "rocket_fire",
          () ->
              new RocketFireBlock(
                  BlockBehaviour.Properties.of()
                      .noCollission()
                      .replaceable()
                      .lightLevel(state -> 15)
                      .noLootTable()));
  public static final Supplier<FuelingStationBlock> FUELING_STATION =
      BLOCKS.register(
          "fueling_station",
          () -> new FuelingStationBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<RocketMonitoringBlock> MONITORING_STATION =
      BLOCKS.register(
          "monitoring_station",
          () -> new RocketMonitoringBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<RocketCargoPortBlock> ITEM_LOADER =
      cargoPort("rocket_item_loader", RocketCargoPortBlock.Kind.ITEM_LOAD);
  public static final Supplier<RocketCargoPortBlock> ITEM_UNLOADER =
      cargoPort("rocket_item_unloader", RocketCargoPortBlock.Kind.ITEM_UNLOAD);
  public static final Supplier<RocketCargoPortBlock> FLUID_LOADER =
      cargoPort("rocket_fluid_loader", RocketCargoPortBlock.Kind.FLUID_LOAD);
  public static final Supplier<RocketCargoPortBlock> FLUID_UNLOADER =
      cargoPort("rocket_fluid_unloader", RocketCargoPortBlock.Kind.FLUID_UNLOAD);
  public static final Supplier<RocketCargoPortBlock> GUIDANCE_ACCESS =
      cargoPort("guidance_access_hatch", RocketCargoPortBlock.Kind.GUIDANCE);

  static {
    ITEMS.register(
        "landing_float", () -> new BlockItem(LANDING_FLOAT.get(), new Item.Properties()));
    ITEMS.register(
        "fueling_station", () -> new BlockItem(FUELING_STATION.get(), new Item.Properties()));
    ITEMS.register(
        "monitoring_station", () -> new BlockItem(MONITORING_STATION.get(), new Item.Properties()));
    part("rocket_motor", RocketPartBlock.Kind.ENGINE, RocketPartBlock.Fuel.MONOPROPELLANT, 10, 1);
    part(
        "advanced_rocket_motor",
        RocketPartBlock.Kind.ENGINE,
        RocketPartBlock.Fuel.MONOPROPELLANT,
        50,
        3);
    part(
        "bipropellant_rocket_motor",
        RocketPartBlock.Kind.ENGINE,
        RocketPartBlock.Fuel.BIPROPELLANT,
        10,
        1);
    part(
        "advanced_bipropellant_rocket_motor",
        RocketPartBlock.Kind.ENGINE,
        RocketPartBlock.Fuel.BIPROPELLANT,
        50,
        3);
    part("nuclear_rocket_motor", RocketPartBlock.Kind.ENGINE, RocketPartBlock.Fuel.NUCLEAR, 35, 1);
    part("fuel_tank", RocketPartBlock.Kind.TANK, RocketPartBlock.Fuel.MONOPROPELLANT, 0, 0);
    part(
        "bipropellant_fuel_tank",
        RocketPartBlock.Kind.TANK,
        RocketPartBlock.Fuel.BIPROPELLANT,
        0,
        0);
    part("oxidizer_fuel_tank", RocketPartBlock.Kind.TANK, RocketPartBlock.Fuel.OXIDIZER, 0, 0);
    part("nuclear_fuel_tank", RocketPartBlock.Kind.TANK, RocketPartBlock.Fuel.NUCLEAR, 0, 0);
    part(
        "guidance_computer",
        RocketPartBlock.Kind.GUIDANCE,
        RocketPartBlock.Fuel.MONOPROPELLANT,
        0,
        0);
    part("rocket_builder", RocketPartBlock.Kind.BUILDER, RocketPartBlock.Fuel.MONOPROPELLANT, 0, 0);
    part(
        "deployable_rocket_builder",
        RocketPartBlock.Kind.DEPLOYABLE_BUILDER,
        RocketPartBlock.Fuel.MONOPROPELLANT,
        0,
        0);
    part(
        "station_builder",
        RocketPartBlock.Kind.STATION_BUILDER,
        RocketPartBlock.Fuel.MONOPROPELLANT,
        0,
        0);
    part("launch_pad", RocketPartBlock.Kind.PAD, RocketPartBlock.Fuel.MONOPROPELLANT, 0, 0);
    part("seat", RocketPartBlock.Kind.SEAT, RocketPartBlock.Fuel.MONOPROPELLANT, 0, 0);
    part("mining_drill", RocketPartBlock.Kind.DRILL, RocketPartBlock.Fuel.MONOPROPELLANT, 0, 0);
  }

  public static final Supplier<BlockEntityType<RocketBlockEntity>> BLOCK_ENTITY =
      ENTITIES.register(
          "rocket_component",
          () ->
              BlockEntityType.Builder.of(
                      RocketBlockEntity::new,
                      PARTS.values().stream()
                          .map(Supplier::get)
                          .filter(
                              part ->
                                  part.kind == RocketPartBlock.Kind.TANK
                                      || part.kind == RocketPartBlock.Kind.GUIDANCE
                                      || part.kind == RocketPartBlock.Kind.BUILDER
                                      || part.kind == RocketPartBlock.Kind.DEPLOYABLE_BUILDER
                                      || part.kind == RocketPartBlock.Kind.STATION_BUILDER)
                          .toArray(Block[]::new))
                  .build(null));
  public static final Supplier<BlockEntityType<FuelingStationBlockEntity>> FUELING_STATION_ENTITY =
      ENTITIES.register(
          "fueling_station",
          () ->
              BlockEntityType.Builder.of(FuelingStationBlockEntity::new, FUELING_STATION.get())
                  .build(null));
  public static final Supplier<BlockEntityType<RocketMonitoringBlockEntity>>
      MONITORING_STATION_ENTITY =
          ENTITIES.register(
              "monitoring_station",
              () ->
                  BlockEntityType.Builder.of(
                          RocketMonitoringBlockEntity::new, MONITORING_STATION.get())
                      .build(null));
  public static final Supplier<BlockEntityType<RocketCargoPortBlockEntity>> CARGO_PORT_ENTITY =
      ENTITIES.register(
          "rocket_cargo_port",
          () ->
              BlockEntityType.Builder.of(
                      RocketCargoPortBlockEntity::new,
                      ITEM_LOADER.get(),
                      ITEM_UNLOADER.get(),
                      FLUID_LOADER.get(),
                      FLUID_UNLOADER.get(),
                      GUIDANCE_ACCESS.get())
                  .build(null));
  public static final Supplier<MenuType<RocketMenu>> MENU =
      MENUS.register("rocket_builder", () -> IMenuTypeExtension.create(RocketMenu::new));
  public static final Supplier<MenuType<RocketCargoPortMenu>> CARGO_PORT_MENU =
      MENUS.register(
          "rocket_cargo_port", () -> IMenuTypeExtension.create(RocketCargoPortMenu::new));
  public static final Supplier<MenuType<FuelingStationMenu>> FUELING_MENU =
      MENUS.register("fueling_station", () -> IMenuTypeExtension.create(FuelingStationMenu::new));
  public static final Supplier<MenuType<RocketMonitoringMenu>> MONITORING_MENU =
      MENUS.register(
          "monitoring_station", () -> IMenuTypeExtension.create(RocketMonitoringMenu::new));

  private RocketRegistry() {}

  public static final Supplier<MenuType<RocketConsoleMenu>> CONSOLE_MENU =
      MENUS.register("rocket_console", () -> IMenuTypeExtension.create(RocketConsoleMenu::new));

  private static Supplier<RocketCargoPortBlock> cargoPort(
      String id, RocketCargoPortBlock.Kind kind) {
    Supplier<RocketCargoPortBlock> block =
        BLOCKS.register(
            id, () -> new RocketCargoPortBlock(kind, BlockBehaviour.Properties.of().strength(3)));
    ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    return block;
  }

  private static void part(
      String id,
      RocketPartBlock.Kind kind,
      RocketPartBlock.Fuel fuel,
      int thrust,
      int consumption) {
    Supplier<RocketPartBlock> block =
        BLOCKS.register(
            id,
            () ->
                new RocketPartBlock(
                    kind,
                    fuel,
                    thrust,
                    consumption,
                    BlockBehaviour.Properties.of().strength(3).requiresCorrectToolForDrops()));
    PARTS.put(id, block);
    ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    MENUS.register(bus);
    SOUNDS.register(bus);
    ROCKETS.register(bus);
    bus.addListener(RocketRegistry::capabilities);
    bus.addListener(RocketRegistry::creative);
    NeoForge.EVENT_BUS.addListener(RocketEntity::serverStopped);
  }

  private static void capabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        FUELING_STATION_ENTITY.get(),
        FuelingStationBlockEntity::automationItems);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        CARGO_PORT_ENTITY.get(),
        (entity, side) ->
            entity.kind() == RocketCargoPortBlock.Kind.GUIDANCE
                ? entity.guidance()
                : entity.kind() == RocketCargoPortBlock.Kind.ITEM_LOAD
                        || entity.kind() == RocketCargoPortBlock.Kind.ITEM_UNLOAD
                    ? entity.inventory
                    : FluidContainers.automation(entity.inventory));
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        CARGO_PORT_ENTITY.get(),
        (entity, side) ->
            entity.kind() == RocketCargoPortBlock.Kind.FLUID_LOAD
                    || entity.kind() == RocketCargoPortBlock.Kind.FLUID_UNLOAD
                ? entity.automation()
                : null);
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        FUELING_STATION_ENTITY.get(),
        (entity, side) -> entity.tank);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        FUELING_STATION_ENTITY.get(),
        (entity, side) -> entity.energy);
    event.registerEntity(
        Capabilities.FluidHandler.ENTITY, ROCKET.get(), (entity, context) -> entity.fluids);
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        BLOCK_ENTITY.get(),
        (entity, direction) -> entity.kind() == RocketPartBlock.Kind.TANK ? entity.tank : null);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        BLOCK_ENTITY.get(),
        (entity, direction) ->
            entity.kind() == RocketPartBlock.Kind.BUILDER
                    || entity.kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER
                    || entity.kind() == RocketPartBlock.Kind.STATION_BUILDER
                ? entity.energy
                : null);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        BLOCK_ENTITY.get(),
        (entity, direction) ->
            entity.kind() == RocketPartBlock.Kind.STATION_BUILDER
                ? entity.stationInventory
                : entity.kind() == RocketPartBlock.Kind.GUIDANCE ? entity.guidanceInventory : null);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) {
      event.accept(FUELING_STATION.get());
      event.accept(MONITORING_STATION.get());
      event.accept(ITEM_LOADER.get());
      event.accept(ITEM_UNLOADER.get());
      event.accept(FLUID_LOADER.get());
      event.accept(FLUID_UNLOADER.get());
      event.accept(GUIDANCE_ACCESS.get());
      event.accept(LANDING_FLOAT.get());
      PARTS.values().forEach(part -> event.accept(part.get()));
    }
  }
}

// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.Main;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MachinePorts {
  public enum Kind {
    ITEM_INPUT,
    ITEM_OUTPUT,
    FLUID_INPUT,
    FLUID_OUTPUT,
    ENERGY_INPUT,
    CREATIVE_ENERGY_INPUT,
    ENERGY_OUTPUT,
    SOLAR,
    DATA_BUS
  }

  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  private static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);
  public static final Map<String, Supplier<Block>> ALL = new LinkedHashMap<>();
  public static final Supplier<Block> BLOCK_ITEM_INPUT_BLOCK =
      port("block_item_input_block", Kind.ITEM_INPUT);
  public static final Supplier<Block> BLOCK_ITEM_OUTPUT_BLOCK =
      port("block_item_output_block", Kind.ITEM_OUTPUT);
  public static final Supplier<Block> BLOCK_FLUID_INPUT_BLOCK =
      port("block_fluid_input_block", Kind.FLUID_INPUT);
  public static final Supplier<Block> BLOCK_FLUID_OUTPUT_BLOCK =
      port("block_fluid_output_block", Kind.FLUID_OUTPUT);
  public static final Supplier<Block> BLOCK_ENERGY_INPUT_BLOCK =
      port("block_energy_input_block", Kind.ENERGY_INPUT);
  public static final Supplier<Block> CREATIVE_ENERGY_INPUT =
      port("creative_energy_input", Kind.CREATIVE_ENERGY_INPUT);
  public static final Supplier<Block> BLOCK_ENERGY_OUTPUT_BLOCK =
      port("block_energy_output_block", Kind.ENERGY_OUTPUT);
  public static final Supplier<Block> SOLAR_PANEL = port("solar_panel", Kind.SOLAR);
  public static final Supplier<Block> DATA_BUS = port("data_bus", Kind.DATA_BUS);
  public static final Supplier<Block> BLOCK_STRUCTURE = part("block_structure_block");
  public static final Supplier<Block> BLOCK_ADVANCED_STRUCTURE =
      part("block_advanced_structure_block");
  public static final Supplier<Block> BLOCK_MOTOR = part("block_motor_block");
  public static final Supplier<Block> ADVANCED_MOTOR = motor("advanced_motor", 1f / 1.5f);
  public static final Supplier<Block> ENHANCED_MOTOR = motor("enhanced_motor", .5f);
  public static final Supplier<Block> ELITE_MOTOR = motor("elite_motor", .25f);
  public static final Supplier<Block> BLOCK_COIL_COPPER = part("block_coilcopper");
  public static final Supplier<Block> STRUCTURE_TOWER = part("structure_tower");
  public static final Supplier<Block> VACUUM_LASER = part("vacuum_laser");
  public static final Supplier<CoalGeneratorBlock> COAL_GENERATOR = coalGenerator();
  public static final Supplier<MenuType<CoalGeneratorMenu>> COAL_GENERATOR_MENU =
      MENUS.register("coal_generator", () -> IMenuTypeExtension.create(CoalGeneratorMenu::new));

  public static final Supplier<BlockEntityType<PortBlockEntity>> ENTITY =
      ENTITIES.register(
          "machine_port",
          () ->
              BlockEntityType.Builder.of(
                      PortBlockEntity::new,
                      ALL.values().stream()
                          .map(Supplier::get)
                          .filter(block -> block instanceof PortBlock)
                          .toArray(Block[]::new))
                  .build(null));
  public static final Supplier<BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR_ENTITY =
      ENTITIES.register(
          "coal_generator",
          () ->
              BlockEntityType.Builder.of(CoalGeneratorBlockEntity::new, COAL_GENERATOR.get())
                  .build(null));
  public static final Supplier<CreativeModeTab> TAB =
      TABS.register(
          "main",
          () ->
              CreativeModeTab.builder()
                  .title(
                      Component.translatable(
                          "message.adv_rocketry.machine_ports.advanced_rocketry"))
                  .icon(() -> new ItemStack(BLOCK_MOTOR.get()))
                  .displayItems(
                      (parameters, output) ->
                          ALL.values().forEach(block -> output.accept(block.get())))
                  .build());

  private MachinePorts() {}

  private static <T extends Block> Supplier<T> register(String name, Supplier<T> factory) {
    Supplier<T> block = BLOCKS.register(name, factory);
    ALL.put(name, block::get);
    ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    return block;
  }

  private static Supplier<Block> port(String name, Kind kind) {
    return register(name, () -> new PortBlock(kind, BlockBehaviour.Properties.of().strength(3)));
  }

  private static Supplier<Block> part(String name) {
    return register(
        name,
        () ->
            name.equals("block_motor_block")
                ? new MotorBlock(BlockBehaviour.Properties.of().strength(3), 1f)
                : new MachinePartBlock(BlockBehaviour.Properties.of().strength(3)));
  }

  private static Supplier<Block> motor(String name, float timeMultiplier) {
    return register(
        name, () -> new MotorBlock(BlockBehaviour.Properties.of().strength(3), timeMultiplier));
  }

  private static Supplier<CoalGeneratorBlock> coalGenerator() {
    return register(
        "coal_generator", () -> new CoalGeneratorBlock(BlockBehaviour.Properties.of().strength(3)));
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    MENUS.register(bus);
    TABS.register(bus);
  }

  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ENTITY.get(),
        (port, side) -> port.hasItems() ? port.automationInventory() : null);
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        ENTITY.get(),
        (port, side) -> port.hasFluid() ? port.automationTank() : null);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        ENTITY.get(),
        (port, side) -> port.hasEnergy() ? port.energyStorage : null);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        COAL_GENERATOR_ENTITY.get(),
        (generator, side) -> generator.fuel);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        COAL_GENERATOR_ENTITY.get(),
        (generator, side) -> generator.output);
  }
}

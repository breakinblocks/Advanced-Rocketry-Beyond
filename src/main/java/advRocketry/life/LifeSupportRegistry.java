// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.Main;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingRegistry;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LifeSupportRegistry {
  private static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, Main.MODID);
  public static final Supplier<SoundEvent> OXYGEN_VENT_SOUND =
      SOUNDS.register(
          "airhissloop",
          () ->
              SoundEvent.createVariableRangeEvent(
                  ResourceLocation.fromNamespaceAndPath(Main.MODID, "airhissloop")));
  private static final DeferredRegister<ArmorMaterial> MATERIALS =
      DeferredRegister.create(Registries.ARMOR_MATERIAL, Main.MODID);
  public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SUIT_MATERIAL =
      MATERIALS.register(
          "spacesuit",
          () ->
              new ArmorMaterial(
                  Map.of(
                      ArmorItem.Type.HELMET,
                      1,
                      ArmorItem.Type.CHESTPLATE,
                      3,
                      ArmorItem.Type.LEGGINGS,
                      2,
                      ArmorItem.Type.BOOTS,
                      1),
                  15,
                  SoundEvents.ARMOR_EQUIP_LEATHER,
                  () -> Ingredient.of(ProcessingRegistry.PART_ITEMS.get("steel_plate").get()),
                  List.of(
                      new ArmorMaterial.Layer(
                          ResourceLocation.fromNamespaceAndPath(Main.MODID, "spacesuit"))),
                  0,
                  0));
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  public static final Supplier<OxygenBlock> VENT =
      BLOCKS.register(
          "oxygen_vent", () -> new OxygenBlock(false, BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<OxygenBlock> CHARGER =
      BLOCKS.register(
          "oxygen_charger",
          () -> new OxygenBlock(true, BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<DoorBlock> AIRLOCK =
      BLOCKS.register(
          "airlock_door",
          () ->
              new DoorBlock(
                  BlockSetType.IRON, BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final Supplier<SuitWorkstationBlock> SUIT_WORKSTATION =
      BLOCKS.register(
          "suit_workstation",
          () -> new SuitWorkstationBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<Block> PIPE_SEALER =
      BLOCKS.register("pipe_sealer", () -> new Block(BlockBehaviour.Properties.of().strength(2)));
  public static final Supplier<AtmosphereDetectorBlock> ATMOSPHERE_DETECTOR =
      BLOCKS.register(
          "atmosphere_detector",
          () -> new AtmosphereDetectorBlock(BlockBehaviour.Properties.of().strength(2)));
  public static final Supplier<CarbonScrubberBlock> CARBON_SCRUBBER =
      BLOCKS.register(
          "carbon_scrubber",
          () -> new CarbonScrubberBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final Supplier<BlockEntityType<OxygenBlockEntity>> OXYGEN_ENTITY =
      ENTITIES.register(
          "oxygen_machine",
          () ->
              BlockEntityType.Builder.of(OxygenBlockEntity::new, VENT.get(), CHARGER.get())
                  .build(null));
  public static final Supplier<BlockEntityType<SuitWorkstationBlockEntity>>
      SUIT_WORKSTATION_ENTITY =
          ENTITIES.register(
              "suit_workstation",
              () ->
                  BlockEntityType.Builder.of(
                          SuitWorkstationBlockEntity::new, SUIT_WORKSTATION.get())
                      .build(null));
  public static final Supplier<BlockEntityType<AtmosphereDetectorBlockEntity>> DETECTOR_ENTITY =
      ENTITIES.register(
          "atmosphere_detector",
          () ->
              BlockEntityType.Builder.of(
                      AtmosphereDetectorBlockEntity::new, ATMOSPHERE_DETECTOR.get())
                  .build(null));
  public static final Supplier<BlockEntityType<CarbonScrubberBlockEntity>> SCRUBBER_ENTITY =
      ENTITIES.register(
          "carbon_scrubber",
          () ->
              BlockEntityType.Builder.of(CarbonScrubberBlockEntity::new, CARBON_SCRUBBER.get())
                  .build(null));

  static {
    ITEMS.register("oxygen_vent", () -> new BlockItem(VENT.get(), new Item.Properties()));
    ITEMS.register("oxygen_charger", () -> new BlockItem(CHARGER.get(), new Item.Properties()));
    ITEMS.register("airlock_door", () -> new BlockItem(AIRLOCK.get(), new Item.Properties()));
    ITEMS.register(
        "suit_workstation", () -> new BlockItem(SUIT_WORKSTATION.get(), new Item.Properties()));
    ITEMS.register("pipe_sealer", () -> new BlockItem(PIPE_SEALER.get(), new Item.Properties()));
    ITEMS.register(
        "atmosphere_detector",
        () -> new BlockItem(ATMOSPHERE_DETECTOR.get(), new Item.Properties()));
    ITEMS.register(
        "carbon_scrubber", () -> new BlockItem(CARBON_SCRUBBER.get(), new Item.Properties()));
  }

  public static final Supplier<MenuType<SuitMenu>> SUIT_MENU =
      MENUS.register("suit_modules", () -> IMenuTypeExtension.create(SuitMenu::new));
  public static final Supplier<MenuType<OxygenMenu>> OXYGEN_MENU =
      MENUS.register("oxygen_machine", () -> IMenuTypeExtension.create(OxygenMenu::new));
  public static final Supplier<MenuType<SuitWorkstationMenu>> SUIT_WORKSTATION_MENU =
      MENUS.register("suit_workstation", () -> IMenuTypeExtension.create(SuitWorkstationMenu::new));
  public static final Supplier<SpaceSuitItem> HELMET =
      ITEMS.register(
          "space_helmet", () -> new SpaceSuitItem(ArmorItem.Type.HELMET, 4, new Item.Properties()));
  public static final Supplier<SealDetectorItem> SEAL_DETECTOR =
      ITEMS.register(
          "seal_detector", () -> new SealDetectorItem(new Item.Properties().stacksTo(1)));
  public static final Supplier<SpaceSuitItem> CHEST =
      ITEMS.register(
          "space_chestplate",
          () -> new SpaceSuitItem(ArmorItem.Type.CHESTPLATE, 6, new Item.Properties()));
  public static final Supplier<SpaceSuitItem> LEGS =
      ITEMS.register(
          "space_leggings",
          () -> new SpaceSuitItem(ArmorItem.Type.LEGGINGS, 4, new Item.Properties()));
  public static final Supplier<SpaceSuitItem> BOOTS =
      ITEMS.register(
          "space_boots", () -> new SpaceSuitItem(ArmorItem.Type.BOOTS, 4, new Item.Properties()));

  private LifeSupportRegistry() {}

  public static void register(IEventBus bus) {
    MATERIALS.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    BLOCKS.register(bus);
    ENTITIES.register(bus);
    SOUNDS.register(bus);
    bus.addListener(LifeSupportRegistry::capabilities);
    bus.addListener(LifeSupportRegistry::creative);
  }

  private static void capabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        SUIT_WORKSTATION_ENTITY.get(),
        (entity, direction) -> entity.suit);
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        SCRUBBER_ENTITY.get(),
        (entity, direction) -> entity.cartridge);
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK, OXYGEN_ENTITY.get(), (entity, direction) -> entity.tank);
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        OXYGEN_ENTITY.get(),
        (entity, direction) -> entity.energy);
    ProcessingRegistry.PART_ITEMS.values().stream()
        .map(Supplier::get)
        .filter(item -> item instanceof PressureTankItem)
        .forEach(
            item ->
                event.registerItem(
                    Capabilities.FluidHandler.ITEM,
                    (stack, context) -> ((PressureTankItem) item).handler(stack),
                    item));
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) {
      event.accept(HELMET.get());
      event.accept(SEAL_DETECTOR.get());
      event.accept(CHEST.get());
      event.accept(LEGS.get());
      event.accept(BOOTS.get());
      event.accept(VENT.get());
      event.accept(CHARGER.get());
      event.accept(AIRLOCK.get());
      event.accept(SUIT_WORKSTATION.get());
      event.accept(PIPE_SEALER.get());
      event.accept(ATMOSPHERE_DETECTOR.get());
      event.accept(CARBON_SCRUBBER.get());
    }
  }
}

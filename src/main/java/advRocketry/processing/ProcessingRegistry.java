// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.Main;
import advRocketry.life.AtmosphereAnalyzerItem;
import advRocketry.life.PressureTankItem;
import advRocketry.orbit.BeaconFinderItem;
import advRocketry.orbit.BiomeChangerRemoteItem;
import advRocketry.orbit.DataUnitItem;
import advRocketry.orbit.LinkerItem;
import advRocketry.orbit.SatelliteChipItem;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ProcessingRegistry {
  public static Consumer<ProcessingBlockEntity> clientMachineTick = machine -> {};
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<RecipeType<?>> TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, Main.MODID);
  private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  private static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, Main.MODID);
  public static final Supplier<SoundEvent> LASER_GUN_SOUND =
      SOUNDS.register(
          "basic_laser_gun",
          () ->
              SoundEvent.createVariableRangeEvent(
                  ResourceLocation.fromNamespaceAndPath(Main.MODID, "basic_laser_gun")));
  public static final Supplier<SoundEvent> RAILGUN_SOUND =
      SOUNDS.register(
          "railgunbang",
          () ->
              SoundEvent.createVariableRangeEvent(
                  ResourceLocation.fromNamespaceAndPath(Main.MODID, "railgunbang")));
  public static final Supplier<SoundEvent> ELECTRIC_SHOCK_SOUND =
      SOUNDS.register(
          "electricshocksmall",
          () ->
              SoundEvent.createVariableRangeEvent(
                  ResourceLocation.fromNamespaceAndPath(Main.MODID, "electricshocksmall")));
  public static final Map<MachineType, Supplier<SoundEvent>> MACHINE_SOUNDS =
      new EnumMap<>(MachineType.class);

  static {
    Map<String, Supplier<SoundEvent>> recordings = new HashMap<>();
    for (String id :
        List.of(
            "rollingmachine",
            "lathe",
            "electrolyser",
            "electricarcfurnace",
            "cuttingmachine",
            "crystallizer",
            "precass"))
      recordings.put(
          id,
          SOUNDS.register(
              id,
              () ->
                  SoundEvent.createVariableRangeEvent(
                      ResourceLocation.fromNamespaceAndPath(Main.MODID, id))));
    for (MachineType type : MachineType.values())
      if (type.sound() != null) MACHINE_SOUNDS.put(type, recordings.get(type.sound()));
  }

  public static final Supplier<MenuType<MachineMenu>> MENU =
      MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::new));

  public static final Map<MachineType, Supplier<Block>> MACHINE_BLOCKS =
      new EnumMap<>(MachineType.class);
  public static final Map<String, Supplier<Block>> PART_BLOCKS = new LinkedHashMap<>();
  public static final Map<String, Supplier<Item>> PART_ITEMS = new LinkedHashMap<>();
  public static final Map<String, Integer> COLORS = new HashMap<>();
  public static final Supplier<RecipeType<ProcessingRecipe>> RECIPE_TYPE =
      TYPES.register(
          "processing",
          () ->
              new RecipeType<>() {
                @Override
                public String toString() {
                  return Main.MODID + ":processing";
                }
              });
  public static final Supplier<ProcessingRecipeSerializer> SERIALIZER =
      SERIALIZERS.register("processing", ProcessingRecipeSerializer::new);

  static {
    for (MachineType type : MachineType.MULTIBLOCKS) {
      Supplier<Block> block =
          BLOCKS.register(
              type.id(),
              () -> new ProcessingBlock(type, BlockBehaviour.Properties.of().strength(3f)));
      MACHINE_BLOCKS.put(type, block);
      ITEMS.register(type.id(), () -> new BlockItem(block.get(), new Item.Properties()));
    }
    LegacyContent.register();
    Supplier<Block> press =
        BLOCKS.register(
            "plate_press", () -> new PlatePressBlock(BlockBehaviour.Properties.of().strength(3f)));
    PART_BLOCKS.put("plate_press", press);
    ITEMS.register("plate_press", () -> new BlockItem(press.get(), new Item.Properties()));
  }

  public static final Supplier<BlockEntityType<ProcessingBlockEntity>> BLOCK_ENTITY =
      ENTITIES.register(
          "processing_machine",
          () ->
              BlockEntityType.Builder.of(
                      ProcessingBlockEntity::new,
                      MACHINE_BLOCKS.values().stream().map(Supplier::get).toArray(Block[]::new))
                  .build(null));

  public static void registerPartItem(String name, int color) {
    PART_ITEMS.put(
        name,
        ITEMS.register(
            name,
            () -> {
              if (name.equals("item_holoprojector"))
                return new HoloProjectorItem(new Item.Properties().stacksTo(1));
              if (name.equals("atm_analyzer"))
                return new AtmosphereAnalyzerItem(new Item.Properties());
              if (name.equals("satellite_id_chip"))
                return new SatelliteChipItem(new Item.Properties());
              if (name.equals("linker")) return new LinkerItem(new Item.Properties());
              if (name.equals("biome_changer_remote"))
                return new BiomeChangerRemoteItem(new Item.Properties());
              if (name.equals("beacon_finder")) return new BeaconFinderItem(new Item.Properties());
              if (name.equals("item_data_storage")) return new DataUnitItem(new Item.Properties());
              if (name.equals("jackhammer"))
                return new JackhammerItem(new Item.Properties().durability(1561));
              if (name.equals("basic_laser_gun")) return new LaserGunItem(new Item.Properties());
              if (name.equals("thermite")) return new ThermiteItem(new Item.Properties());
              if (name.equals("carbon_scrubber_cartridge"))
                return new Item(new Item.Properties().durability(32766));
              if (name.startsWith("portable_pressure_tank_"))
                return new PressureTankItem(
                    switch (name) {
                      case "portable_pressure_tank_steel" -> 2000;
                      case "portable_pressure_tank_titanium" -> 4000;
                      case "portable_pressure_tank_iridium" -> 8000;
                      default -> 1000;
                    },
                    new Item.Properties());
              return new Item(new Item.Properties());
            }));
    if (color != -1) COLORS.put(name, color);
  }

  public static void registerPartBlock(String name, int color) {
    Supplier<Block> block =
        BLOCKS.register(
            name,
            () ->
                switch (name) {
                  case "alien_sapling" ->
                      new AlienSaplingBlock(
                          BlockBehaviour.Properties.of()
                              .noCollission()
                              .randomTicks()
                              .instabreak()
                              .sound(SoundType.GRASS));
                  case "electric_mushroom" ->
                      new ElectricMushroomBlock(
                          BlockBehaviour.Properties.of()
                              .noCollission()
                              .randomTicks()
                              .instabreak()
                              .sound(SoundType.GRASS));
                  case "alien_leaves" ->
                      new LeavesBlock(
                          BlockBehaviour.Properties.of()
                              .strength(0.2f)
                              .randomTicks()
                              .lightLevel(state -> 8)
                              .sound(SoundType.GRASS)
                              .ignitedByLava()
                              .noOcclusion()
                              .isValidSpawn((state, level, pos, type) -> false));
                  case "alien_log" ->
                      new RotatedPillarBlock(
                          BlockBehaviour.Properties.of()
                              .strength(3f)
                              .sound(SoundType.WOOD)
                              .ignitedByLava());
                  case "charcoal_log" ->
                      new RotatedPillarBlock(
                          BlockBehaviour.Properties.of().strength(3f).sound(SoundType.WOOD));
                  case "geode" ->
                      new Block(
                          BlockBehaviour.Properties.of()
                              .strength(6f, 2000f)
                              .requiresCorrectToolForDrops());
                  case "alien_planks" ->
                      new Block(
                          BlockBehaviour.Properties.of()
                              .strength(2f)
                              .sound(SoundType.WOOD)
                              .lightLevel(state -> 15)
                              .ignitedByLava());
                  default ->
                      new MachinePartBlock(
                          BlockBehaviour.Properties.of()
                              .strength(3f)
                              .requiresCorrectToolForDrops());
                });
    PART_BLOCKS.put(name, block);
    PART_ITEMS.put(
        name, ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
    if (color != -1) COLORS.put(name, color);
  }

  public static Block part(String name) {
    return PART_BLOCKS.get(name).get();
  }

  public static Item item(String name) {
    return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MODID, name));
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    TYPES.register(bus);
    SERIALIZERS.register(bus);
    MENUS.register(bus);
    SOUNDS.register(bus);
    bus.addListener(ProcessingRegistry::addCreative);
  }

  private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) {
      MACHINE_BLOCKS.values().forEach(block -> event.accept(new ItemStack(block.get())));
      PART_ITEMS.values().forEach(item -> event.accept(item.get()));
      event.accept(part("plate_press"));
    }
  }
}

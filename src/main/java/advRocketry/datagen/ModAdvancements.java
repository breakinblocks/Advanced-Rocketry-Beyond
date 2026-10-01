package advRocketry.datagen;

import advRocketry.Main;
import advRocketry.space.AdvancementLogic;
import advRocketry.space.Milestone;
import advRocketry.space.MilestoneTrigger;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModAdvancements implements AdvancementProvider.AdvancementGenerator {
  @Override
  public void generate(
      HolderLookup.Provider registries,
      Consumer<AdvancementHolder> saver,
      ExistingFileHelper files) {
    advancement(
        saver,
        "normal/area_gravity",
        "normal/warp",
        "adv_rocketry:area_gravity_controller",
        "advancement.whatGoesUp",
        AdvancementType.TASK,
        false,
        0,
        "area_gravity_controller",
        hasAny("adv_rocketry:area_gravity_controller"));
    advancement(
        saver,
        "normal/beer",
        "normal/root",
        "minecraft:tnt",
        "advancement.beerOnTheSun",
        AdvancementType.TASK,
        true,
        0,
        "beer",
        milestone(Milestone.SEAT_ON_TNT));
    advancement(
        saver,
        "normal/biome_changer",
        "normal/satellite_builder",
        "adv_rocketry:biome_changer_remote",
        "advancement.landscaping",
        AdvancementType.TASK,
        false,
        0,
        "biome_changer_remote",
        hasAny("adv_rocketry:biome_changer_remote"));
    advancement(
        saver,
        "normal/bipropellant",
        "normal/rocket_builder",
        "adv_rocketry:bipropellant_rocket_motor",
        "advancement.twiceTheThrust",
        AdvancementType.TASK,
        false,
        0,
        "bipropellant_rocket_motor",
        hasAny("adv_rocketry:bipropellant_rocket_motor"));
    advancement(
        saver,
        "normal/black_hole_generator",
        "normal/space_station",
        "adv_rocketry:black_hole_generator",
        "advancement.eventHorizon",
        AdvancementType.GOAL,
        false,
        0,
        "black_hole_generator",
        hasAny("adv_rocketry:black_hole_generator"));
    advancement(
        saver,
        "normal/blockpresser",
        "normal/root",
        "minecraft:piston",
        "advancement.flattening",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:plate_press"));
    advancement(
        saver,
        "normal/centrifuge",
        "normal/holographic",
        "adv_rocketry:centrifuge",
        "advancement.spinCycle",
        AdvancementType.TASK,
        false,
        0,
        "centrifuge",
        hasAny("adv_rocketry:centrifuge"));
    advancement(
        saver,
        "normal/chemical_reactor",
        "normal/electrifying",
        "adv_rocketry:chemical_reactor",
        "advancement.madScientist",
        AdvancementType.TASK,
        false,
        0,
        "chemical_reactor",
        hasAny("adv_rocketry:chemical_reactor"));
    advancement(
        saver,
        "normal/circuits",
        "normal/precision_assembler",
        "adv_rocketry:basic_circuit",
        "advancement.integrated",
        AdvancementType.TASK,
        false,
        0,
        "basic_circuit",
        hasAny("adv_rocketry:basic_circuit"));
    advancement(
        saver,
        "normal/crystalline",
        "normal/holographic",
        "adv_rocketry:crystallizer",
        "advancement.crystalline",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:crystallizer"));
    advancement(
        saver,
        "normal/cutting_machine",
        "normal/crystalline",
        "adv_rocketry:cutting_machine",
        "advancement.cutItOut",
        AdvancementType.TASK,
        false,
        0,
        "cutting_machine",
        hasAny("adv_rocketry:cutting_machine"));
    advancement(
        saver,
        "normal/data_processor",
        "normal/observatory",
        "adv_rocketry:astrobody_data_processor",
        "advancement.numberCruncher",
        AdvancementType.TASK,
        false,
        0,
        "astrobody_data_processor",
        hasAny("adv_rocketry:astrobody_data_processor"));
    advancement(
        saver,
        "normal/dilithium",
        "normal/root",
        "adv_rocketry:dilithium_crystal",
        "advancement.dilithium",
        AdvancementType.TASK,
        false,
        0,
        "dilithium",
        hasAny(
            "adv_rocketry:dilithium_ore",
            "adv_rocketry:dilithium_dust",
            "adv_rocketry:dilithium_crystal"));
    advancement(
        saver,
        "normal/electrifying",
        "normal/holographic",
        "adv_rocketry:electrolyzer",
        "advancement.electrifying",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:electrolyzer"));
    advancement(
        saver,
        "normal/feeltheheat",
        "normal/holographic",
        "adv_rocketry:electric_arc_furnace",
        "advancement.feelTheHeat",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:electric_arc_furnace"));
    advancement(
        saver,
        "normal/flightofpheonix",
        "normal/warp",
        "adv_rocketry:warp_core",
        "advancement.flightOfThePhoenix",
        AdvancementType.CHALLENGE,
        false,
        100,
        "flightofpheonix",
        milestone(Milestone.FIRST_WARP_FLIGHT));
    advancement(
        saver,
        "normal/givingitallshesgot",
        "normal/warp",
        "adv_rocketry:warp_core",
        "advancement.givingItAllShesGot",
        AdvancementType.TASK,
        false,
        0,
        "givingitallshesgot",
        milestone(Milestone.WARP_FLIGHT));
    advancement(
        saver,
        "normal/holographic",
        "normal/blockpresser",
        "adv_rocketry:item_holoprojector",
        "advancement.holographic",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:item_holoprojector"));
    advancement(
        saver,
        "normal/jetpack",
        "normal/suitedup",
        "adv_rocketry:jetpack",
        "advancement.rocketMan",
        AdvancementType.TASK,
        false,
        0,
        "jetpack",
        hasAny("adv_rocketry:jetpack"));
    advancement(
        saver,
        "normal/laser_etcher",
        "normal/circuits",
        "adv_rocketry:precision_laser_etcher",
        "advancement.etchASketch",
        AdvancementType.TASK,
        false,
        0,
        "precision_laser_etcher",
        hasAny("adv_rocketry:precision_laser_etcher"));
    advancement(
        saver,
        "normal/liftoff",
        "normal/rocket_builder",
        "adv_rocketry:rocket_motor",
        "advancement.liftoff",
        AdvancementType.GOAL,
        false,
        0,
        "launch",
        milestone(Milestone.ROCKET_LAUNCH));
    advancement(
        saver,
        "normal/moonlanding",
        "normal/liftoff",
        "adv_rocketry:moon_turf",
        "advancement.moonLanding",
        AdvancementType.TASK,
        false,
        0,
        "moonlanding",
        milestone(Milestone.MOON_LANDING));
    advancement(
        saver,
        "normal/nuclear",
        "normal/bipropellant",
        "adv_rocketry:nuclear_rocket_motor",
        "advancement.atomicAge",
        AdvancementType.GOAL,
        false,
        0,
        "nuclear_rocket_motor",
        hasAny("adv_rocketry:nuclear_rocket_motor"));
    advancement(
        saver,
        "normal/observatory",
        "normal/root",
        "adv_rocketry:observatory",
        "advancement.stargazer",
        AdvancementType.TASK,
        false,
        0,
        "observatory",
        hasAny("adv_rocketry:observatory"));
    advancement(
        saver,
        "normal/onesmallstep",
        "normal/moonlanding",
        "adv_rocketry:moon_turf",
        "advancement.oneSmallStep",
        AdvancementType.CHALLENGE,
        false,
        100,
        "onesmallstep",
        milestone(Milestone.FIRST_MOON_LANDING));
    advancement(
        saver,
        "normal/orbital_laser",
        "normal/space_station",
        "adv_rocketry:orbital_laser",
        "advancement.laserFocus",
        AdvancementType.TASK,
        false,
        0,
        "orbital_laser",
        hasAny("adv_rocketry:orbital_laser"));
    advancement(
        saver,
        "normal/oxygen_vent",
        "normal/suitedup",
        "adv_rocketry:oxygen_vent",
        "advancement.freshAir",
        AdvancementType.TASK,
        false,
        0,
        "oxygen_vent",
        placed("adv_rocketry:oxygen_vent"));
    advancement(
        saver,
        "normal/pipes",
        "normal/power",
        "adv_rocketry:fluid_pipe",
        "advancement.tubular",
        AdvancementType.TASK,
        false,
        0,
        "pipe",
        placed(
            "adv_rocketry:energy_cable", "adv_rocketry:fluid_pipe", "adv_rocketry:item_conduit"));
    advancement(
        saver,
        "normal/planet_discovery",
        "normal/space_station",
        "adv_rocketry:planet_id_chip",
        "advancement.uncharted",
        AdvancementType.GOAL,
        false,
        0,
        "discovery",
        milestone(Milestone.PLANET_DISCOVERY));
    advancement(
        saver,
        "normal/power",
        "normal/root",
        "adv_rocketry:coal_generator",
        "advancement.powerUp",
        AdvancementType.TASK,
        false,
        0,
        "coal_generator",
        hasAny("adv_rocketry:coal_generator"));
    advancement(
        saver,
        "normal/precision_assembler",
        "normal/cutting_machine",
        "adv_rocketry:precision_assembler",
        "advancement.steadyHands",
        AdvancementType.TASK,
        false,
        0,
        "precision_assembler",
        hasAny("adv_rocketry:precision_assembler"));
    advancement(
        saver,
        "normal/railgun",
        "normal/space_station",
        "adv_rocketry:railgun",
        "advancement.specialDelivery",
        AdvancementType.TASK,
        false,
        0,
        "railgun",
        hasAny("adv_rocketry:railgun"));
    advancement(
        saver,
        "normal/rocket_builder",
        "normal/root",
        "adv_rocketry:rocket_builder",
        "advancement.rocketScience",
        AdvancementType.TASK,
        false,
        0,
        "rocket_builder",
        hasAny("adv_rocketry:rocket_builder"));
    advancement(
        saver,
        "normal/rocket_fuel",
        "normal/chemical_reactor",
        "adv_rocketry:rocket_fuel_bucket",
        "advancement.highOctane",
        AdvancementType.TASK,
        false,
        0,
        "rocket_fuel",
        hasAny("adv_rocketry:rocket_fuel_bucket"));
    advancement(
        saver,
        "normal/rollin",
        "normal/holographic",
        "adv_rocketry:rolling_machine",
        "advancement.rollin",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:rolling_machine"));
    root(
        saver,
        "normal/root",
        "adv_rocketry:moon_turf",
        "advancement.advancedRocketry",
        "minecraft:textures/gui/advancements/backgrounds/stone.png",
        "crafting_table",
        hasAny("minecraft:crafting_table"));
    advancement(
        saver,
        "normal/satellite_builder",
        "normal/observatory",
        "adv_rocketry:satellite_builder",
        "advancement.eyeInTheSky",
        AdvancementType.TASK,
        false,
        0,
        "satellite_builder",
        hasAny("adv_rocketry:satellite_builder"));
    advancement(
        saver,
        "normal/sealed_room",
        "normal/oxygen_vent",
        "adv_rocketry:airlock_door",
        "advancement.airtight",
        AdvancementType.GOAL,
        false,
        0,
        "sealed_room",
        milestone(Milestone.SEALED_ROOM));
    advancement(
        saver,
        "normal/space_elevator",
        "normal/space_station",
        "adv_rocketry:space_elevator",
        "advancement.goingUp",
        AdvancementType.GOAL,
        false,
        0,
        "elevator",
        riding("adv_rocketry:elevator_capsule"));
    advancement(
        saver,
        "normal/space_station",
        "normal/station_builder",
        "adv_rocketry:space_station",
        "advancement.homeAwayFromHome",
        AdvancementType.GOAL,
        false,
        0,
        "space",
        enteredDimension("adv_rocketry:space"));
    advancement(
        saver,
        "normal/spindoctor",
        "normal/holographic",
        "adv_rocketry:lathe",
        "advancement.spinDoctor",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:lathe"));
    advancement(
        saver,
        "normal/station_builder",
        "normal/rocket_builder",
        "adv_rocketry:station_builder",
        "advancement.someAssemblyRequired",
        AdvancementType.TASK,
        false,
        0,
        "station_builder",
        hasAny("adv_rocketry:station_builder"));
    advancement(
        saver,
        "normal/suitedup",
        "normal/root",
        "adv_rocketry:space_helmet",
        "advancement.suitedUp",
        AdvancementType.TASK,
        false,
        0,
        "suited",
        hasAll(
            "adv_rocketry:space_boots",
            "adv_rocketry:space_leggings",
            "adv_rocketry:space_helmet",
            "adv_rocketry:space_chestplate"));
    advancement(
        saver,
        "normal/terraformer",
        "normal/biome_changer",
        "adv_rocketry:atmosphere_terraformer",
        "advancement.climateControl",
        AdvancementType.GOAL,
        false,
        0,
        "atmosphere_terraformer",
        hasAny("adv_rocketry:atmosphere_terraformer"));
    advancement(
        saver,
        "normal/warp",
        "normal/dilithium",
        "adv_rocketry:warp_core",
        "advancement.warp",
        AdvancementType.TASK,
        false,
        0,
        "warp",
        hasAny("adv_rocketry:warp_core"));
    advancement(
        saver,
        "normal/wenttothemoon",
        "normal/moonlanding",
        "adv_rocketry:space_boots",
        "advancement.weReallyWentToTheMoon",
        AdvancementType.GOAL,
        false,
        0,
        "wenttothemoon",
        milestone(Milestone.APOLLO_SITE));
  }

  private static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
  }

  private static ItemLike item(String id) {
    return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
  }

  private static void root(
      Consumer<AdvancementHolder> saver,
      String path,
      String icon,
      String title,
      String background,
      String name,
      Criterion<?> criterion) {
    Advancement.Builder.advancement()
        .display(
            new ItemStack(item(icon)),
            Component.translatable(title),
            Component.translatable(title + ".desc"),
            ResourceLocation.parse(background),
            AdvancementType.TASK,
            false,
            false,
            false)
        .addCriterion(name, criterion)
        .save(saver, id(path).toString());
  }

  private static void advancement(
      Consumer<AdvancementHolder> saver,
      String path,
      String parent,
      String icon,
      String title,
      AdvancementType frame,
      boolean hidden,
      int experience,
      String name,
      Criterion<?> criterion) {
    Advancement.Builder builder =
        Advancement.Builder.advancement()
            .parent(AdvancementSubProvider.createPlaceholder(id(parent).toString()))
            .display(
                new ItemStack(item(icon)),
                Component.translatable(title),
                Component.translatable(title + ".desc"),
                null,
                frame,
                true,
                true,
                hidden)
            .addCriterion(name, criterion);
    if (experience > 0) builder.rewards(AdvancementRewards.Builder.experience(experience));
    builder.save(saver, id(path).toString());
  }

  private static Criterion<InventoryChangeTrigger.TriggerInstance> hasAny(String... items) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(
        ItemPredicate.Builder.item()
            .of(Arrays.stream(items).map(ModAdvancements::item).toArray(ItemLike[]::new))
            .build());
  }

  private static Criterion<InventoryChangeTrigger.TriggerInstance> hasAll(String... items) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(
        Arrays.stream(items).map(ModAdvancements::item).toArray(ItemLike[]::new));
  }

  private static Criterion<MilestoneTrigger.TriggerInstance> milestone(Milestone milestone) {
    return AdvancementLogic.MILESTONE
        .get()
        .createCriterion(new MilestoneTrigger.TriggerInstance(Optional.empty(), milestone));
  }

  private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placed(String... blocks) {
    return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
        LocationCheck.checkLocation(
            LocationPredicate.Builder.location()
                .setBlock(
                    BlockPredicate.Builder.block()
                        .of(
                            Arrays.stream(blocks)
                                .map(
                                    block ->
                                        BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)))
                                .toArray(Block[]::new)))));
  }

  private static Criterion<StartRidingTrigger.TriggerInstance> riding(String entity) {
    return StartRidingTrigger.TriggerInstance.playerStartsRiding(
        EntityPredicate.Builder.entity()
            .vehicle(
                EntityPredicate.Builder.entity()
                    .entityType(
                        EntityTypePredicate.of(
                            BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entity))))));
  }

  private static Criterion<ChangeDimensionTrigger.TriggerInstance> enteredDimension(String level) {
    return ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(
        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(level)));
  }
}

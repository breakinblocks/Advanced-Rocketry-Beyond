package advRocketry.processing;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import advRocketry.TestMultiblocks;
import advRocketry.life.LifeSupportRegistry;
import advRocketry.life.SpaceBreathing;
import advRocketry.multiblock.Multiblocks;
import advRocketry.orbit.OrbitalRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class ProcessingTests {
  @GameTest(template = "empty")
  public static void largeTaggedOutputsPreserveComponentsAndSave(GameTestHelper helper) {
    var patch = DataComponentPatch.builder()
        .set(DataComponents.CUSTOM_NAME, Component.literal("Reviewed output")).build();
    var output = new ProcessingOutput(Optional.empty(),
        Optional.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))),
        100, patch, 1);
    var recipe = new ProcessingRecipe(MachineType.ROLLING_MACHINE, 100, 20,
        List.of(), List.of(), List.of(output));
    var rolled = recipe.roll(RandomSource.create(1));
    var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
    var saved = ProcessingRecipe.Rolled.CODEC.encodeStart(ops, rolled).getOrThrow();
    var restored = ProcessingRecipe.Rolled.CODEC.parse(ops, saved).getOrThrow();
    helper.assertTrue(restored.items().stream().mapToInt(ItemStack::getCount).sum() == 100,
        "Large output lost items during persistence");
    helper.assertTrue(restored.items().stream().allMatch(stack -> stack.getCount() <= 64
        && Component.literal("Reviewed output").equals(stack.get(DataComponents.CUSTOM_NAME))),
        "Output stacks lost their limits or components");
    helper.assertTrue(output.displayStacks().stream().allMatch(stack ->
        stack.has(DataComponents.CUSTOM_NAME)), "Recipe display lost output components");
    helper.succeed();
  }

  private static final BlockPos ANCHOR = new BlockPos(8, 6, 8);

  @GameTest(template = "empty")
  public static void projectorSelectsOriginalUtilityStructures(GameTestHelper helper) {
    helper.assertTrue(
        !OrbitalRegistry.WARP_CORE.get().asItem().equals(Items.AIR),
        "Directional warp core has no registered block item");
    helper.assertTrue(
        helper
            .getLevel()
            .getRecipeManager()
            .byKey(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "legacy/warpcore"))
            .isPresent(),
        "Warp-core crafting recipe did not load");
    ItemStack projector =
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("item_holoprojector").get());
    var ids = Multiblocks.ids(helper.getLevel().registryAccess());
    helper.assertTrue(ids.size() == 24, "Projector is missing an original registered structure");
    projector.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(ids.get(10)));
    helper.assertTrue(
        HoloProjectorItem.machine(projector).equals(Multiblocks.id("black_hole_generator")),
        "Projector cannot select the original black-hole generator frame");
    projector.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(ids.get(11)));
    helper.assertTrue(
        HoloProjectorItem.machine(projector).equals(Multiblocks.id("area_gravity_controller")),
        "Projector cannot select the original area-gravity frame");
    projector.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(ids.get(12)));
    helper.assertTrue(
        HoloProjectorItem.machine(projector).equals(Multiblocks.id("microwave_receiver")),
        "Projector cannot select the original microwave receiver frame");
    projector.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(ids.get(13)));
    helper.assertTrue(
        HoloProjectorItem.machine(projector).equals(Multiblocks.id("beacon")),
        "Projector cannot select the original beacon frame");
    for (String id :
        List.of(
            "warp_core",
            "satellite_builder",
            "solar_array",
            "biome_scanner",
            "astrobody_data_processor",
            "railgun",
            "atmosphere_terraformer",
            "orbital_laser",
            "space_elevator",
            "observatory")) {
      projector.set(ModComponents.PROJECTOR_CHOICE, new ProjectorChoice(Multiblocks.id(id)));
      helper.assertTrue(
          HoloProjectorItem.machine(projector).equals(Multiblocks.id(id)),
          "Projector cannot select the original " + id + " frame");
    }
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalCreativePlugPowersMachineWithoutDraining(GameTestHelper helper) {
    ProcessingBlockEntity machine = build(helper, "lathe", Direction.NORTH);
    BlockPos input =
        machine.menuPorts().stream()
            .filter(
                pos ->
                    helper
                        .getLevel()
                        .getBlockState(pos)
                        .is(MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get()))
            .findFirst()
            .orElseThrow();
    helper
        .getLevel()
        .setBlock(input, MachinePorts.CREATIVE_ENERGY_INPUT.get().defaultBlockState(), 2);
    helper.assertTrue(machine.scanStructure(), "Creative power plug did not fit a machine input");
    PortBlockEntity port = (PortBlockEntity) helper.getLevel().getBlockEntity(input);
    int available = port.energyStorage.getEnergyStored();
    helper.assertTrue(
        machine.getEnergyInputTiles().contains(port)
            && available == Integer.MAX_VALUE >> 4
            && port.energyStorage.extractEnergy(5000, false) == 5000
            && port.energyStorage.getEnergyStored() == available,
        "Creative plug did not supply repeatable power without draining");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void enrichedLavaRetainsLavaContactAndLight(GameTestHelper helper) {
    var enriched = ProcessingFluids.DEFINITIONS.get("enriched_lava");
    helper.assertTrue(
        enriched.source.get().is(FluidTags.LAVA)
            && enriched.flowing.get().is(FluidTags.LAVA)
            && enriched.block.get().defaultBlockState().getLightEmission() == 15,
        "Enriched lava must burn like lava and illuminate its surroundings");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void taggedMaterialsCreateFallbackPlateRecipes(GameTestHelper helper) {
    ItemStack ingot = new ItemStack(ProcessingRegistry.PART_ITEMS.get("tin_ingot").get());
    ProcessingRecipe recipe = MaterialRecipes.candidate(MachineType.ROLLING_MACHINE, ingot);
    helper.assertTrue(
        recipe != null
            && recipe.ticks() == 300
            && recipe.energy() == 20
            && recipe.inputs().size() == 1
            && recipe.inputs().getFirst().test(ingot)
            && recipe.fluidInputs().size() == 1
            && recipe.fluidInputs().getFirst().test(new FluidStack(Fluids.WATER, 100))
            && recipe.outputs().getFirst().stack().is(ProcessingRegistry.item("tin_plate")),
        "Common-tag material fallback did not reproduce original rolling-machine recipe");
    ProcessingBlockEntity machine = build(helper, "rolling_machine", Direction.NORTH);
    CompoundTag saved = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
    var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
    saved.put(
        "dynamic_processing_recipe", ProcessingRecipe.CODEC.encodeStart(ops, recipe).getOrThrow());
    saved.put(
        "processing_outputs",
        ProcessingRecipe.Rolled.CODEC
            .encodeStart(ops, recipe.roll(RandomSource.create(1)))
            .getOrThrow());
    saved.putInt("processing_progress", 7);
    machine.loadWithComponents(saved, helper.getLevel().registryAccess());
    CompoundTag reloaded = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
    helper.assertTrue(
        reloaded.contains("dynamic_processing_recipe")
            && reloaded.contains("processing_outputs")
            && reloaded.getInt("processing_progress") == 7,
        "Cross-mod material job lost its recipe or pending output on reload");
    ProcessingRecipe lathe =
        MaterialRecipes.candidate(
            MachineType.LATHE,
            new ItemStack(ProcessingRegistry.PART_ITEMS.get("steel_ingot").get()));
    helper.assertTrue(
        lathe != null
            && lathe.outputs().getFirst().stack().is(ProcessingRegistry.item("steel_rod"))
            && lathe.outputs().getFirst().count() == 2,
        "Common-tag material fallback did not reproduce original two-rod lathe output");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void titaniumOreUsesStandardTags(GameTestHelper helper) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("adv_rocketry", "titanium_ore");
    Block ore = BuiltInRegistries.BLOCK.get(id);
    ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("c", "ores/titanium");
    helper.assertTrue(
        ore != Blocks.AIR
            && ore.defaultBlockState().is(TagKey.create(Registries.BLOCK, tag))
            && ore.defaultBlockState()
                .is(TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores")))
            && new ItemStack(ore).is(TagKey.create(Registries.ITEM, tag)),
        "Titanium ore is missing its c:ores/titanium tags");
    ProcessingRecipe recipe =
        (ProcessingRecipe)
            helper
                .getLevel()
                .getRecipeManager()
                .byKey(ResourceLocation.parse("adv_rocketry:processing/titanium_ore_to_ingot"))
                .orElseThrow()
                .value();
    helper.assertTrue(
        recipe.machine() == MachineType.ELECTRIC_ARC_FURNACE
            && recipe.inputs().getFirst().test(new ItemStack(ore))
            && recipe.outputs().getFirst().stack().is(ProcessingRegistry.item("titanium_ingot")),
        "Titanium ore does not smelt to titanium ingots in the arc furnace");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void enrichedLavaPoolResolvesInstalledNuggets(GameTestHelper helper) {
    ProcessingRecipe recipe =
        (ProcessingRecipe)
            helper
                .getLevel()
                .getRecipeManager()
                .byKey(ResourceLocation.parse("adv_rocketry:processing/enriched_lava"))
                .orElseThrow()
                .value();
    var pool = recipe.weightedPool();
    helper.assertTrue(
        recipe.outputsResolvable()
            && pool.stream().noneMatch(entry -> tagged(entry.output(), "c:nuggets/lead"))
            && pool.stream()
                .anyMatch(
                    entry ->
                        tagged(entry.output(), "c:nuggets/copper")
                            && entry
                                .output()
                                .stack()
                                .is(ProcessingRegistry.PART_ITEMS.get("copper_nugget").get())
                            && entry.weight() == 100),
        "Enriched-lava pool did not resolve installed nugget tags or drop missing ones");
    var roll = recipe.roll(RandomSource.create(1));
    List<ItemStack> rolled = roll.items();
    helper.assertTrue(
        rolled.size() == 4
            && roll.fluids().size() == 1
            && rolled.stream()
                .allMatch(
                    output ->
                        pool.stream()
                            .anyMatch(
                                entry ->
                                    ItemStack.isSameItemSameComponents(
                                        entry.output().stack(), output))),
        "Centrifuge did not roll four outputs from the resolved pool");
    ProcessingRecipe missing =
        new ProcessingRecipe(
            MachineType.CENTRIFUGE,
            200,
            10,
            List.of(),
            List.of(),
            List.of(
                SizedFluidIngredient.of(
                    BuiltInRegistries.FLUID.get(
                        ResourceLocation.parse("adv_rocketry:enriched_lava")),
                    1000)),
            List.of(),
            List.of(),
            List.of(
                new ProcessingRecipe.WeightedOutput(
                    ProcessingOutput.of(
                        TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/missing")),
                        1),
                    1)),
            4);
    helper.assertTrue(
        !missing.outputsResolvable(), "A recipe whose whole pool is missing could still run");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 240)
  public static void chemicalReactorSealsOrdinaryArmor(GameTestHelper helper) {
    ProcessingBlockEntity machine = build(helper, "chemical_reactor", Direction.NORTH);
    PortBlockEntity input = machine.getItemInTiles().getFirst();
    PortBlockEntity output = machine.getItemOutTiles().getFirst();
    PortBlockEntity power = machine.getEnergyInputTiles().getFirst();
    input.inventory.setStackInSlot(0, new ItemStack(Items.IRON_CHESTPLATE));
    input.inventory.setStackInSlot(1, new ItemStack(LifeSupportRegistry.PIPE_SEALER.get()));
    input.inventory.setStackInSlot(
        2, new ItemStack(ProcessingRegistry.PART_ITEMS.get("titanium_aluminide_sheet").get(), 4));
    input.inventory.setStackInSlot(
        3,
        new ItemStack(ProcessingRegistry.PART_ITEMS.get("portable_pressure_tank_iridium").get()));
    power.energyStorage.receiveEnergy(2000, false);
    for (int tick = 0; tick < 100; tick++) machine.tick();
    ItemStack sealed = output.inventory.getStackInSlot(0);
    helper.assertTrue(
        sealed.is(Items.IRON_CHESTPLATE)
            && SpaceBreathing.sealed(helper.getLevel(), sealed)
            && input.inventory.getStackInSlot(0).isEmpty(),
        "Chemical reactor did not seal and output the original chest armor");
    int capacity = AdvancedRocketryConfig.sealedArmorAirCapacity();
    helper.assertTrue(
        SpaceBreathing.fill(sealed, capacity + 1) == capacity
            && SpaceBreathing.air(sealed) == capacity
            && SpaceBreathing.consume(sealed)
            && SpaceBreathing.air(sealed) == capacity - 1,
        "Sealed armor did not hold its configured oxygen buffer");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void originalLaserGunLocksAimedBlock(GameTestHelper helper) {
    var level = helper.getLevel();
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    BlockPos origin = helper.absolutePos(new BlockPos(4, 4, 4));
    player.setPos(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
    player.setYRot(0);
    player.setXRot(0);
    BlockPos target = origin.south(4).above();
    level.setBlock(target, Blocks.STONE.defaultBlockState(), 2);
    var gun = (LaserGunItem) ProcessingRegistry.PART_ITEMS.get("basic_laser_gun").get();
    ItemStack stack = new ItemStack(gun);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    helper.assertTrue(
        player.pick(50, 1, false) instanceof BlockHitResult hit && hit.getBlockPos().equals(target),
        "Laser ray missed the test block");
    gun.use(level, player, InteractionHand.MAIN_HAND);
    gun.onUseTick(level, player, stack, 16);
    helper.assertTrue(
        target.equals(gun.lockedTarget(player)), "Laser did not lock the aimed block");
    helper.assertTrue(
        player.pick(50, 1, false) instanceof BlockHitResult hit && hit.getBlockPos().equals(target),
        "Laser lost its target before harvest");
    gun.finishUsingItem(stack, level, player);
    helper.assertTrue(gun.lockedTarget(player) == null, "Laser retained a finished target lock");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void coalGeneratorBurnsFuelAndExportsPower(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos pos = helper.absolutePos(new BlockPos(3, 4, 3));
    level.setBlock(pos, MachinePorts.COAL_GENERATOR.get().defaultBlockState(), 2);
    level.setBlock(pos.east(), MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState(), 2);
    CoalGeneratorBlockEntity generator = (CoalGeneratorBlockEntity) level.getBlockEntity(pos);
    PortBlockEntity input = (PortBlockEntity) level.getBlockEntity(pos.east());
    helper.assertTrue(
        generator.accepts(new ItemStack(Items.COAL)), "Generator rejected vanilla furnace fuel");
    ItemStack thermite = new ItemStack(ProcessingRegistry.PART_ITEMS.get("thermite").get());
    helper.assertTrue(
        generator.accepts(thermite) && thermite.getBurnTime(RecipeType.SMELTING) == 6000,
        "Original 6,000-tick thermite fuel was not accepted");
    generator.fuel.insertItem(0, new ItemStack(Items.COAL), false);
    generator.tick();
    helper.assertTrue(
        generator.burnTime() == 1599 && generator.fuel.getStackInSlot(0).isEmpty(),
        "Generator did not consume one coal and retain its burn time");
    helper.assertTrue(
        input.energyStorage.getEnergyStored() == 80,
        "Generator did not export 80 FE per tick");
    helper.assertTrue(
        level
            .getRecipeManager()
            .byKey(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "legacy/coalgenerator"))
            .isPresent(),
        "Original coal generator recipe was not ported");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void recoveredOriginalCraftingRecipesLoad(GameTestHelper helper) {
    var recipes = helper.getLevel().getRecipeManager();
    long craftingCount =
        recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
            .filter(holder -> holder.id().getNamespace().equals("adv_rocketry"))
            .count();
    helper.assertTrue(
        craftingCount >= 140, "Fewer than 140 original crafting recipes loaded: " + craftingCount);
    for (var holder : recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
      if (!holder.id().getNamespace().equals("adv_rocketry")) continue;
      var recipe = holder.value();
      var stacks = new ArrayList<ItemStack>();
      for (var ingredient : recipe.getIngredients()) {
        if (ingredient.isEmpty()) stacks.add(ItemStack.EMPTY);
        else {
          helper.assertTrue(
              ingredient.getItems().length > 0,
              "Unavailable crafting ingredient in " + holder.id());
          stacks.add(ingredient.getItems()[0].copy());
        }
      }
      int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
      int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : 3;
      while (stacks.size() < width * height) stacks.add(ItemStack.EMPTY);
      var input = CraftingInput.of(width, height, stacks);
      helper.assertTrue(
          recipe.matches(input, helper.getLevel()),
          "Converted crafting grid does not match " + holder.id());
      var output = recipe.assemble(input, helper.getLevel().registryAccess());
      var expected = recipe.getResultItem(helper.getLevel().registryAccess());
      helper.assertTrue(
          !output.isEmpty()
              && ItemStack.isSameItemSameComponents(output, expected)
              && output.getCount() == expected.getCount(),
          "Crafting assembled the wrong item, components or count for " + holder.id());
    }
    for (String id :
        new String[] {
          "jetpack",
          "stationmarker",
          "quartz_crucible",
          "sealdetector",
          "jackhammer",
          "thermitetorch",
          "circlelight",
          "oxygenscrubber",
          "wirelesstransciever",
          "landingpad",
          "intake"
        })
      helper.assertTrue(
          recipes
              .byKey(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "legacy/" + id))
              .isPresent(),
          "Original crafting recipe did not load: " + id);
    helper.assertTrue(
        BuiltInRegistries.ITEM
            .getTag(
                TagKey.create(
                    Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "pressure_tanks")))
            .filter(tag -> tag.size() == 4)
            .isPresent(),
        "Wildcard pressure tank ingredient did not resolve all four tiers");
    var advancedBattery =
        recipes.byKey(ResourceLocation.fromNamespaceAndPath("adv_rocketry", "legacy/advbattery"));
    helper.assertTrue(
        advancedBattery.isPresent()
            && advancedBattery
                .get()
                .value()
                .getResultItem(helper.getLevel().registryAccess())
                .is(ProcessingRegistry.PART_ITEMS.get("advanced_battery").get()),
        "The original advanced battery metadata variant collapsed into the basic battery");
    helper.assertTrue(
        BuiltInRegistries.ITEM
            .getTag(
                TagKey.create(
                    Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/carbon")))
            .filter(
                tag ->
                    tag.stream()
                        .anyMatch(
                            item ->
                                item.value()
                                    == ProcessingRegistry.PART_ITEMS.get("carbon_brick").get()))
            .isPresent(),
        "The oxygen scrubber's original carbon ingredient has no matching item");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void jackhammerMinesAndRepairsLikeOriginal(GameTestHelper helper) {
    ItemStack stack = new ItemStack(ProcessingRegistry.PART_ITEMS.get("jackhammer").get());
    JackhammerItem jackhammer = (JackhammerItem) stack.getItem();
    helper.assertTrue(
        jackhammer.getDestroySpeed(stack, Blocks.STONE.defaultBlockState()) == 50,
        "Jackhammer lost its original mining speed");
    helper.assertTrue(
        jackhammer.isValidRepairItem(
            stack, new ItemStack(ProcessingRegistry.PART_ITEMS.get("titanium_rod").get())),
        "Jackhammer rejected titanium rod repair");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void processingRecipesResolve(GameTestHelper helper) {
    var recipes =
        helper.getLevel().getRecipeManager().getAllRecipesFor(ProcessingRegistry.RECIPE_TYPE.get());
    helper.assertTrue(
        recipes.size() == 90, "Expected 90 processing recipes, loaded " + recipes.size());
    for (var holder : recipes) {
      var recipe = holder.value();
      for (var ingredient : recipe.requiredItems())
        helper.assertTrue(
            ingredient.getItems().length > 0
                && Arrays.stream(ingredient.getItems()).noneMatch(ItemStack::isEmpty),
            "Unresolved item input in " + holder.id());
      for (var ingredient : recipe.fluidInputs())
        helper.assertTrue(
            ingredient.getFluids().length > 0, "Unresolved fluid input in " + holder.id());
      for (var result : recipe.outputs())
        helper.assertTrue(result.resolvable(), "Unregistered output in " + holder.id());
      for (var result : recipe.fluidOutputs())
        helper.assertTrue(!result.isEmpty(), "Unregistered fluid output in " + holder.id());
      for (var result : recipe.weightedOutputs())
        helper.assertTrue(
            result.output().tag().isPresent() || result.output().resolvable(),
            "Unregistered weighted output in " + holder.id());
    }
    helper.succeed();
  }

  private static ProcessingBlockEntity build(GameTestHelper helper, String id, Direction facing) {
    var level = helper.getLevel();
    BlockPos anchor = helper.absolutePos(ANCHOR);
    level.setBlock(
        anchor,
        ProcessingRegistry.MACHINE_BLOCKS
            .get(MachineType.byId(id))
            .get()
            .defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing),
        2);
    var machine = (ProcessingBlockEntity) level.getBlockEntity(anchor);
    TestMultiblocks.build(level, id, anchor, facing);
    helper.assertTrue(machine.scanStructure(), id + " did not form facing " + facing);
    return machine;
  }

  @GameTest(template = "empty")
  public static void originalMotorTiersSpeedUpFormedMachines(GameTestHelper helper) {
    ProcessingBlockEntity machine = build(helper, "lathe", Direction.NORTH);
    var recipe =
        helper
            .getLevel()
            .getRecipeManager()
            .getAllRecipesFor(ProcessingRegistry.RECIPE_TYPE.get())
            .stream()
            .map(holder -> holder.value())
            .filter(value -> value.machine() == MachineType.LATHE && value.ticks() == 300)
            .findFirst()
            .orElseThrow();
    BlockPos motorPos = machine.structure().first('M');
    helper.assertTrue(motorPos != null, "Lathe has no motor position");
    helper.assertTrue(
        machine.modifiedDuration(recipe) == 300, "Basic motor changed the original recipe time");
    for (var tier :
        List.of(
            MachinePorts.ADVANCED_MOTOR, MachinePorts.ENHANCED_MOTOR, MachinePorts.ELITE_MOTOR)) {
      helper.getLevel().setBlock(motorPos, tier.get().defaultBlockState(), 2);
      helper.assertTrue(machine.scanStructure(), "Upgraded motor broke the lathe structure");
      int expected =
          tier == MachinePorts.ADVANCED_MOTOR
              ? 200
              : tier == MachinePorts.ENHANCED_MOTOR ? 150 : 75;
      helper.assertTrue(
          machine.modifiedDuration(recipe) == expected,
          "Motor tier did not apply its original speed multiplier");
    }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 40)
  public static void structuresRotateAndDisassemble(GameTestHelper helper) {
    for (String id : MachineType.MULTIBLOCKS.stream().map(MachineType::id).toList())
      for (Direction facing : Direction.Plane.HORIZONTAL) {
        var machine = build(helper, id, facing);
        for (var cell : machine.structure().cells())
          helper.getLevel().removeBlock(cell.pos(), false);
        helper.assertTrue(
            helper.getLevel().getBlockEntity(helper.absolutePos(ANCHOR)) == null,
            "Removed controller was restored");
      }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 400)
  public static void latheConsumesInputAndEnergy(GameTestHelper helper) {
    var machine = build(helper, "lathe", Direction.NORTH);
    var input = machine.getItemInTiles().getFirst();
    var output = machine.getItemOutTiles().getFirst();
    var power = machine.getEnergyInputTiles().getFirst();
    input.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
    power.energyStorage.receiveEnergy(10000, false);
    helper.succeedWhen(
        () -> {
          helper.assertTrue(output.inventory.getStackInSlot(0).getCount() == 2, "No rods yet");
          helper.assertTrue(
              output
                  .inventory
                  .getStackInSlot(0)
                  .is(ProcessingRegistry.PART_ITEMS.get("iron_rod").get()),
              "Wrong result");
          helper.assertTrue(input.inventory.getStackInSlot(0).isEmpty(), "Input not consumed");
          helper.assertTrue(
              power.energyStorage.getEnergyStored() == 4000, "Wrong energy consumption");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 220)
  public static void cartridgeRecyclingRequiresExhaustedFilter(GameTestHelper helper) {
    var machine = build(helper, "chemical_reactor", Direction.NORTH);
    var input = machine.getItemInTiles().getFirst();
    var output = machine.getItemOutTiles().getFirst();
    var power = machine.getEnergyInputTiles().getFirst();
    ProcessingRecipe recycle =
        (ProcessingRecipe)
            helper
                .getLevel()
                .getRecipeManager()
                .byKey(ResourceLocation.parse("adv_rocketry:processing/carboncartridgerefresh"))
                .orElseThrow()
                .value();
    var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
    var synced =
        ProcessingRecipe.CODEC
            .parse(ops, ProcessingRecipe.CODEC.encodeStart(ops, recycle).getOrThrow())
            .getOrThrow();
    var exhausted = new ItemStack(ProcessingRegistry.item("carbon_scrubber_cartridge"));
    exhausted.setDamageValue(32766);
    helper.assertTrue(
        synced.inputs().getFirst().test(exhausted), "Damage lost in recipe synchronization");
    var fresh = exhausted.copy();
    fresh.setDamageValue(0);
    input.inventory.setStackInSlot(0, fresh);
    power.energyStorage.receiveEnergy(10000, false);
    helper.runAfterDelay(
        20,
        () -> {
          helper.assertTrue(
              machine.progressTicks() == 0 && power.energyStorage.getEnergyStored() == 10000,
              "Fresh filter recycled");
          var partial = fresh.copy();
          partial.setDamageValue(32765);
          input.inventory.setStackInSlot(0, partial);
        });
    helper.runAfterDelay(
        40,
        () -> {
          helper.assertTrue(
              machine.progressTicks() == 0 && power.energyStorage.getEnergyStored() == 10000,
              "Partly used filter recycled");
          input.inventory.setStackInSlot(0, exhausted.copy());
        });
    helper.runAfterDelay(
        110,
        () -> {
          helper.assertTrue(
              input.inventory.getStackInSlot(0).isEmpty(), "Exhausted filter not consumed");
          helper.assertTrue(
              output.inventory.getStackInSlot(0).getDamageValue() == 0
                  && output.inventory.getStackInSlot(0).is(fresh.getItem()),
              "Filter not refreshed");
          helper.assertTrue(
              output.inventory.getStackInSlot(1).is(Items.CHARCOAL)
                  && output.inventory.getStackInSlot(1).getCount() == 1,
              "Wrong recycling byproduct");
          input.inventory.setStackInSlot(0, output.inventory.extractItem(0, 1, false));
        });
    helper.runAfterDelay(
        180,
        () -> {
          helper.assertTrue(
              !input.inventory.getStackInSlot(0).isEmpty()
                  && output.inventory.getStackInSlot(1).getCount() == 1,
              "Recycled filter generates unlimited charcoal");
          helper.assertTrue(
              power.energyStorage.getEnergyStored() == 9200, "Recycling energy incorrect");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 60)
  public static void blockedOutputsPreserveInputsAndEnergy(GameTestHelper helper) {
    var machine = build(helper, "lathe", Direction.WEST);
    var input = machine.getItemInTiles().getFirst();
    var output = machine.getItemOutTiles().getFirst();
    var power = machine.getEnergyInputTiles().getFirst();
    input.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
    for (int slot = 0; slot < 4; slot++)
      output.inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
    power.energyStorage.receiveEnergy(10000, false);
    helper.runAfterDelay(
        40,
        () -> {
          helper.assertTrue(machine.progressTicks() == 0, "Blocked machine progressed");
          helper.assertTrue(
              input.inventory.getStackInSlot(0).getCount() == 1, "Blocked machine consumed input");
          helper.assertTrue(
              power.energyStorage.getEnergyStored() == 10000, "Blocked machine consumed energy");
          helper.succeed();
        });
  }

  @GameTest(template = "empty")
  public static void outputReservationsAreIndependent(GameTestHelper helper) {
    var slots = new ItemStackHandler(1);
    helper.assertTrue(
        !OutputSimulation.canInsertAllItems(
            List.of(slots),
            List.of(new ItemStack(Items.IRON_INGOT, 40), new ItemStack(Items.IRON_INGOT, 40))),
        "Overbooked output slot");
    helper.assertTrue(slots.getStackInSlot(0).isEmpty(), "Simulation changed inventory");
    helper.assertTrue(OutputSimulation.canInsertAllItems(
        List.of(new ItemStackHandler(4), new ItemStackHandler(4), new ItemStackHandler(4)),
        List.of(new ItemStack(Items.IRON_INGOT, 700))), "Simulation failed with more than eight slots");
    var rejecting = new ItemStackHandler(1) {
      @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    };
    helper.assertTrue(!OutputSimulation.canInsertAllItems(List.of(rejecting),
        List.of(new ItemStack(Items.IRON_INGOT))), "Simulation ignored slot restrictions");
    var tank = new FluidTank(1000);
    var hydrogen = new FluidStack(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get(), 500);
    var oxygen = new FluidStack(ProcessingFluids.DEFINITIONS.get("oxygen").source.get(), 500);
    helper.assertTrue(
        !OutputSimulation.canInsertAllFluids(List.of(tank), List.of(hydrogen, oxygen)),
        "Mixed distinct fluids in one tank");
    helper.assertTrue(
        OutputSimulation.canInsertAllFluids(
            List.of(tank, new FluidTank(1000)), List.of(hydrogen, oxygen)),
        "Failed to reserve separate tanks");
    helper.assertTrue(tank.isEmpty(), "Simulation changed tank");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 180)
  public static void electrolyzerKeepsOutputsSeparate(GameTestHelper helper) {
    var machine = build(helper, "electrolyzer", Direction.SOUTH);
    machine
        .getFluidInTiles()
        .getFirst()
        .myTank
        .fill(new FluidStack(Fluids.WATER, 10), IFluidHandler.FluidAction.EXECUTE);
    for (var port : machine.getEnergyInputTiles()) port.energyStorage.receiveEnergy(10000, false);
    helper.succeedWhen(
        () -> {
          var outputs = machine.getFluidOutTiles();
          helper.assertTrue(outputs.size() == 2, "Wrong fluid port count");
          helper.assertTrue(
              outputs.stream().allMatch(port -> port.myTank.getFluidAmount() == 100),
              "Outputs incomplete");
          helper.assertTrue(
              outputs.stream()
                  .anyMatch(
                      port ->
                          port.myTank
                              .getFluid()
                              .is(ProcessingFluids.DEFINITIONS.get("oxygen").source.get())),
              "Oxygen missing");
          helper.assertTrue(
              outputs.stream()
                  .anyMatch(
                      port ->
                          port.myTank
                              .getFluid()
                              .is(ProcessingFluids.DEFINITIONS.get("hydrogen").source.get())),
              "Hydrogen missing");
          helper.assertTrue(
              machine.getFluidInTiles().getFirst().myTank.isEmpty(), "Water not consumed");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 40)
  public static void platePressRequiresObsidianAndDoesNotMoveBlocks(GameTestHelper helper) {
    var level = helper.getLevel();
    var pos = helper.absolutePos(ANCHOR);
    var press = (PlatePressBlock) ProcessingRegistry.part("plate_press");
    level.setBlock(pos.below(), Blocks.IRON_BLOCK.defaultBlockState(), 2);
    level.setBlock(pos, press.defaultBlockState(), 2);
    level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    helper.assertTrue(
        level.getBlockState(pos.below()).is(Blocks.IRON_BLOCK), "Pressed without obsidian");
    level.setBlock(pos.below(2), Blocks.OBSIDIAN.defaultBlockState(), 3);
    press.neighborChanged(
        level.getBlockState(pos), level, pos, Blocks.OBSIDIAN, pos.below(2), false);
    helper.assertTrue(level.getBlockState(pos.below()).isAir(), "Input block not consumed");
    var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
    int count =
        drops.stream()
            .filter(
                drop -> drop.getItem().is(ProcessingRegistry.PART_ITEMS.get("iron_plate").get()))
            .mapToInt(drop -> drop.getItem().getCount())
            .sum();
    helper.assertTrue(count == 4, "Expected four plates");
    helper.assertTrue(level.getBlockState(pos).is(press), "Press changed into vanilla piston");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 1400)
  public static void etcherPersistsProgressAndRetainsLens(GameTestHelper helper) {
    var machine = build(helper, "precision_laser_etcher", Direction.EAST);
    var input = machine.getItemInTiles().getFirst();
    var output = machine.getItemOutTiles().getFirst();
    input.inventory.setStackInSlot(
        0, new ItemStack(ProcessingRegistry.PART_ITEMS.get("basic_lens").get()));
    input.inventory.setStackInSlot(
        1, new ItemStack(ProcessingRegistry.PART_ITEMS.get("gold_plate").get()));
    input.inventory.setStackInSlot(2, new ItemStack(Items.REDSTONE));
    input.inventory.setStackInSlot(
        3, new ItemStack(ProcessingRegistry.PART_ITEMS.get("silicon_wafer").get(), 4));
    var powerPorts = machine.getEnergyInputTiles();
    for (var power : powerPorts) power.energyStorage.receiveEnergy(100000, false);
    for (int tick = 100; tick < 1300; tick += 100)
      helper.runAfterDelay(
          tick,
          () -> {
            for (var power : powerPorts) power.energyStorage.receiveEnergy(100000, false);
          });
    helper.runAfterDelay(
        60,
        () -> {
          var tag = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
          helper.assertTrue(machine.progressTicks() > 0, "Recipe did not start");
          var state = helper.getLevel().getBlockState(machine.getBlockPos());
          helper.getLevel().removeBlock(machine.getBlockPos(), false);
          helper.getLevel().setBlock(machine.getBlockPos(), state, 2);
          var restored =
              (ProcessingBlockEntity) helper.getLevel().getBlockEntity(machine.getBlockPos());
          restored.loadWithComponents(tag, helper.getLevel().registryAccess());
          restored.scanStructure();
          helper.assertTrue(
              restored.progressTicks() == machine.progressTicks(), "Progress was lost on reload");
        });
    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              output.inventory.getStackInSlot(0).getCount() == 2, "Etching incomplete");
          helper.assertTrue(
              output
                  .inventory
                  .getStackInSlot(0)
                  .is(ProcessingRegistry.PART_ITEMS.get("basic_circuit_plate").get()),
              "Wrong circuit result");
          helper.assertTrue(input.inventory.getStackInSlot(0).getCount() == 1, "Lens was consumed");
          helper.assertTrue(
              input.inventory.getStackInSlot(1).isEmpty()
                  && input.inventory.getStackInSlot(2).isEmpty()
                  && input.inventory.getStackInSlot(3).isEmpty(),
              "Consumables not consumed");
        });
  }

  private static boolean tagged(ProcessingOutput output, String tag) {
    return output.tag().map(key -> key.location().toString().equals(tag)).orElse(false);
  }
}

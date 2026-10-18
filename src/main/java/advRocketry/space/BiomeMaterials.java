package advRocketry.space;

import advRocketry.Main;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BiomeMaterials {
  private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Main.MODID);
  private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Main.MODID);
  public static final Map<String, Supplier<Block>> ALL = new LinkedHashMap<>();
  public static final Map<String, String> PLANTS = new LinkedHashMap<>();
  public static final String[] STONES = {
    "weathered_basalt",
    "cooling_crust",
    "mineral_crust",
    "strike_glass",
    "lunar_breccia",
    "dark_regolith",
    "banded_stone",
    "wet_slate",
    "glacial_stone",
    "porous_stone"
  };
  public static final String[] SOILS = {
    "rooted_loam", "alluvial_silt", "volcanic_ash", "luminous_moss"
  };

  static {
    for (String name : STONES)
      add(
          name,
          () ->
              new Block(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                      .strength(1.8f, 6)
                      .lightLevel(s -> name.equals("cooling_crust") ? 2 : 0)));
    for (String name : SOILS)
      add(
          name,
          () ->
              new Block(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                      .sound(name.equals("volcanic_ash") ? SoundType.SAND : SoundType.ROOTED_DIRT)
                      .lightLevel(s -> name.equals("luminous_moss") ? 3 : 0)));
    add(
        "rootwood_log",
        () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    add(
        "stripped_rootwood_log",
        () ->
            new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    add(
        "rootwood_planks",
        () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    add(
        "rootwood_leaves",
        () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));
    add(
        "rootwood_sapling",
        () ->
            new SaplingBlock(
                new TreeGrower(
                    "rootwood",
                    Optional.empty(),
                    Optional.of(
                        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("rootwood_tree"))),
                    Optional.empty()),
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
    plant("fan_fern", "floor", 0);
    plant("spiral_seedling", "floor", 0);
    plant("forest_groundcover", "floor", 0);
    plant("fallen_foliage", "floor", 0);
    plant("hanging_roots", "ceiling", 0);
    plant("luminous_seed_cluster", "ceiling", 5);
    plant("shelf_fungus", "wall", 2);
    plant("cave_blossom", "floor", 4);
    plant("jointed_reeds", "floor", 0);
    plant("marsh_sedge", "floor", 0);
    plant("floating_leaves", "floating", 0);
    plant("aquatic_frond", "aquatic", 0);
    plant("vent_growth", "heat", 3);
    plant("ember_curl", "heat", 1);
    plant("storm_blades", "heat", 0);
    plant("spark_mushroom", "heat", 5);
    plant("mineral_bloom", "mineral", 2);
    plant("salt_shrub", "shore", 0);
  }

  private BiomeMaterials() {}

  private static void add(String name, Supplier<Block> factory) {
    var entry = BLOCKS.register(name, factory);
    ALL.put(name, entry);
    ITEMS.register(name, () -> new BlockItem(entry.get(), new Item.Properties()));
  }

  private static void plant(String name, String habitat, int light) {
    PLANTS.put(name, habitat);
    add(
        name,
        () ->
            new HabitatPlantBlock(
                habitat,
                BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> light)));
  }

  public static ResourceLocation id(String name) {
    return ResourceLocation.fromNamespaceAndPath(Main.MODID, name);
  }

  public static Block block(String name) {
    return ALL.get(name).get();
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    bus.addListener(BiomeMaterials::setup);
    NeoForge.EVENT_BUS.addListener(BiomeMaterials::strip);
  }

  private static void setup(FMLCommonSetupEvent event) {
    event.enqueueWork(
        () -> {
          FireBlock fire = (FireBlock) Blocks.FIRE;
          fire.setFlammable(block("rootwood_log"), 5, 5);
          fire.setFlammable(block("stripped_rootwood_log"), 5, 5);
          fire.setFlammable(block("rootwood_planks"), 5, 20);
          fire.setFlammable(block("rootwood_leaves"), 30, 60);
        });
  }

  private static void strip(BlockEvent.BlockToolModificationEvent event) {
    if (event.getItemAbility() == ItemAbilities.AXE_STRIP
        && event.getState().is(block("rootwood_log")))
      event.setFinalState(
          block("stripped_rootwood_log")
              .defaultBlockState()
              .setValue(
                  RotatedPillarBlock.AXIS, event.getState().getValue(RotatedPillarBlock.AXIS)));
  }
}

package advRocketry.datagen;

import advRocketry.Main;
import advRocketry.ModComponents;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class ModBlockLoot extends BlockLootSubProvider {
  public ModBlockLoot(HolderLookup.Provider registries) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
  }

  private static Block block(String path) {
    return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Main.MODID, path));
  }

  @Override
  protected void generate() {
    for (Block block : getKnownBlocks()) {
      String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
      switch (path) {
        case "alien_leaves" ->
            add(
                block,
                LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(
                                LootItem.lootTableItem(block("alien_sapling"))
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                            .when(LootItemRandomChanceCondition.randomChance(0.01f))));
        case "liquid_tank" ->
            add(
                block,
                LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(
                                LootItem.lootTableItem(block)
                                    .apply(
                                        CopyComponentsFunction.copyComponents(
                                                CopyComponentsFunction.Source.BLOCK_ENTITY)
                                            .include(ModComponents.TANK_CONTENT.get())))));
        case "airlock_door" -> add(block, createDoorTable(block));
        case "thermite_wall_torch" -> dropOther(block, block("thermite_torch"));
        case "unlit_torch", "unlit_wall_torch" -> dropOther(block, Items.TORCH);
        default -> dropSelf(block);
      }
    }
  }

  @Override
  protected List<Block> getKnownBlocks() {
    return BuiltInRegistries.BLOCK.stream()
        .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Main.MODID))
        .filter(block -> block.getLootTable() != BuiltInLootTables.EMPTY)
        .toList();
  }
}

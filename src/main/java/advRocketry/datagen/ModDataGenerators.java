package advRocketry.datagen;

import advRocketry.Main;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
  private ModDataGenerators() {}

  public static void register(IEventBus bus) {
    bus.addListener(ModDataGenerators::gather);
  }

  private static void gather(GatherDataEvent event) {
    var generator = event.getGenerator();
    var output = generator.getPackOutput();
    var server = event.includeServer();
    var files = event.getExistingFileHelper();
    var lookup =
        generator
            .addProvider(
                server,
                new DatapackBuiltinEntriesProvider(
                    output,
                    event.getLookupProvider(),
                    ModRegistryEntries.builder(),
                    Set.of(Main.MODID)))
            .getRegistryProvider();
    generator.addProvider(
        server,
        new LootTableProvider(
            output,
            Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(
                    ModBlockLoot::new, LootContextParamSets.BLOCK)),
            lookup));
    generator.addProvider(
        server,
        new ModTagProvider<>(output, Registries.BLOCK, lookup, files, ModTagContent::blocks));
    generator.addProvider(
        server, new ModTagProvider<>(output, Registries.ITEM, lookup, files, ModTagContent::items));
    generator.addProvider(
        server,
        new ModTagProvider<>(output, Registries.FLUID, lookup, files, ModTagContent::fluids));
    generator.addProvider(
        server,
        new ModTagProvider<>(
            output, Registries.ENTITY_TYPE, lookup, files, ModTagContent::entities));
    generator.addProvider(
        server,
        new ModTagProvider<>(output, Registries.BIOME, lookup, files, ModTagContent::biomes));
    generator.addProvider(server, new ModDataMaps(output, lookup));
    generator.addProvider(server, new ModRecipeProvider(output, lookup));
    generator.addProvider(event.includeClient(), new ModLanguage(output));
    generator.addProvider(event.includeClient(), new ModBlockStates(output, files));
    generator.addProvider(event.includeClient(), new ModItemModels(output, files));
    generator.addProvider(event.includeClient(), new ModSounds(output, files));
    generator.addProvider(
        server, new AdvancementProvider(output, lookup, files, List.of(new ModAdvancements())));
  }
}

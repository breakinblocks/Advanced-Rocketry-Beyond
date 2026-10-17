package advRocketry.space;

import advRocketry.AdvancedRocketryConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class TerraformRateTests {
  @GameTest(template = "empty")
  public static void terraformUpdatesUpperBiomesWhenBottomAlreadyMatches(GameTestHelper helper) {
    var level = helper.getLevel();
    var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
    var desert =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:desert")));
    var moon =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.parse("adv_rocketry:moon")));
    var anchor = helper.absolutePos(new BlockPos(4, 4, 4));
    int x = anchor.getX() & ~3, z = anchor.getZ() & ~3;
    FillBiomeCommand.fill(
        level,
        new BlockPos(x, level.getMinBuildHeight(), z),
        new BlockPos(x + 3, level.getMaxBuildHeight() - 1, z + 3),
        desert);
    FillBiomeCommand.fill(level, new BlockPos(x, 80, z), new BlockPos(x + 3, 83, z + 3), moon);
    helper.assertTrue(
        level.getChunkAt(anchor).getNoiseBiome(x >> 2, 20, z >> 2).is(moon),
        "Vertical test biome was not installed");
    TerraformBiomeLogic.changeCell(level, new PlanetBiomeSource(List.of(desert), 9), x, z);
    for (int y = level.getMinBuildHeight() >> 2; y < level.getMaxBuildHeight() >> 2; y++)
      helper.assertTrue(
          level.getChunkAt(anchor).getNoiseBiome(x >> 2, y, z >> 2).is(desert),
          "Terraform skipped a differing upper biome because the bottom matched");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 400)
  public static void queuedLoadedChunkRespectsConfiguredRate(GameTestHelper helper)
      throws Exception {
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    var planet = new Planet(2977, "Terraform queue budget");
    planet.atmosphere = 0;
    planet.biomes.add(ResourceLocation.parse("adv_rocketry:moon"));
    galaxy.planets.put(planet.id, planet);
    ServerLevel level = PlanetRuntime.create(server, planet);
    var pos = new ChunkPos(0, 0);
    var chunk = level.getChunk(pos.x, pos.z);
    var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
    var moon =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.parse("adv_rocketry:moon")));
    var desert = ResourceLocation.parse("minecraft:desert");
    var source = new PlanetBiomeSource(List.of(moon), 9);
    for (int x = 0; x < 16; x += 4)
      for (int z = 0; z < 16; z += 4) TerraformBiomeLogic.changeCell(level, source, x, z);
    var field = AdvancedRocketryConfig.class.getDeclaredField("BIOME_UPDATE_SPEED");
    field.setAccessible(true);
    var setting = (ModConfigSpec.IntValue) field.get(null);
    int previous = setting.get();
    var enqueue =
        TerraformBiomeLogic.class.getDeclaredMethod("enqueue", ServerLevel.class, ChunkPos.class);
    enqueue.setAccessible(true);
    try {
      planet.terraformedBiomes.add(desert);
      enqueue.invoke(null, level, pos);
      setting.set(1);
      TerraformBiomeLogic.run(server);
      helper.assertTrue(changed(level, pos) == 1, "Queued terraform ignored one-column budget");
      setting.set(3);
      TerraformBiomeLogic.run(server);
      helper.assertTrue(
          changed(level, pos) == 4, "Queued terraform ignored changed three-column budget");
      for (int tick = 0; tick < 4; tick++) TerraformBiomeLogic.run(server);
      helper.assertTrue(
          changed(level, pos) == 16,
          "Queued terraform did not cover the entire player-free loaded chunk");
      helper.assertTrue(chunk.isUnsaved(), "Converted chunk was not marked for saving");
    } finally {
      setting.set(previous);
      planet.terraformedBiomes.clear();
      galaxy.setDirty();
    }
    helper.succeed();
  }

  private static int changed(ServerLevel level, ChunkPos pos) {
    int count = 0;
    for (int x = 0; x < 4; x++)
      for (int z = 0; z < 4; z++)
        if (level
            .getChunk(pos.x, pos.z)
            .getNoiseBiome(x, level.getMinBuildHeight() >> 2, z)
            .is(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:desert"))))
          count++;
    return count;
  }
}

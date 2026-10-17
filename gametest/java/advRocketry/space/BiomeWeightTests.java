package advRocketry.space;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class BiomeWeightTests {
  @GameTest(template = "empty")
  public static void weightedRegionsPersistAndOldSourcesStillDecode(GameTestHelper helper) {
    var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
    var plains =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    var desert =
        registry.getHolderOrThrow(
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("desert")));
    var weighted = new PlanetBiomeSource(List.of(plains, desert), List.of(9, 1), 573L);
    var ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
    CompoundTag encoded =
        (CompoundTag) PlanetBiomeSource.CODEC.codec().encodeStart(ops, weighted).getOrThrow();
    var reloaded = PlanetBiomeSource.CODEC.codec().parse(ops, encoded).getOrThrow();
    int plainsCount = 0;
    for (int x = -50; x < 50; x++)
      for (int z = -50; z < 50; z++) {
        var biome = weighted.getNoiseBiome(x * 32, 0, z * 32, null);
        if (biome.is(plains)) plainsCount++;
        helper.assertTrue(
            biome.is(reloaded.getNoiseBiome(x * 32, 0, z * 32, null)),
            "Biome weights or seed changed on codec reload");
      }
    helper.assertTrue(
        plainsCount > 8700 && plainsCount < 9300,
        "A 9:1 biome distribution was not honored: " + plainsCount + "/10000");
    encoded.remove("weights");
    var legacy = PlanetBiomeSource.CODEC.codec().parse(ops, encoded).getOrThrow();
    var equal = new PlanetBiomeSource(List.of(plains, desert), 573L);
    for (int x = -32; x < 32; x++)
      helper.assertTrue(
          legacy.getNoiseBiome(x * 32, 0, 0, null).is(equal.getNoiseBiome(x * 32, 0, 0, null)),
          "An older unweighted saved generator changed biome distribution");
    helper.succeed();
  }
}

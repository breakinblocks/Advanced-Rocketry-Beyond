package advRocketry.space;

import advRocketry.life.PlanetEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class SpawnTests {
  @GameTest(template = "empty", timeoutTicks = 1200)
  public static void planetWildlifeGroupsDataAndAtmosphere(GameTestHelper helper) {
    Planet planet = new Planet(995, "Wildlife Test");
    planet.spawns.add(
        OreTests.parse(
            PlanetSpawns.Entry.CODEC,
            "{\"entity\": \"minecraft:sheep\", \"weight\": 17, \"min\": 2, \"max\": 5,"
                + " \"data\": {\"Color\": 14, \"NoAI\": true, \"Pos\": [999.0, 999.0, 999.0]}}"));
    planet = Planet.load(planet.save());
    var rule = planet.spawns.getFirst();
    helper.assertTrue(
        rule.weight() == 17
            && rule.min() == 2
            && rule.max() == 5
            && rule.data().getByte("Color") == 14,
        "Spawn JSON or save/reload lost weight, independent group bounds or entity NBT");
    var server = helper.getLevel().getServer();
    var galaxy = GalaxyData.get(server);
    galaxy.planets.put(planet.id, planet);
    var level = PlanetRuntime.create(server, planet);
    var pos = new BlockPos(8, 100, 8);
    var potential =
        new LevelEvent.PotentialSpawns(
            level,
            MobCategory.CREATURE,
            pos,
            WeightedRandomList.<MobSpawnSettings.SpawnerData>create());
    PlanetSpawns.potential(potential);
    helper.assertTrue(
        potential.getSpawnerDataList().size() == 1
            && potential.getSpawnerDataList().getFirst().type == EntityType.SHEEP
            && potential.getSpawnerDataList().getFirst().minCount == 2
            && potential.getSpawnerDataList().getFirst().maxCount == 5,
        "Planet wildlife was not added to its native creature spawn category");
    var aquatic =
        new LevelEvent.PotentialSpawns(
            level,
            MobCategory.WATER_CREATURE,
            pos,
            WeightedRandomList.<MobSpawnSettings.SpawnerData>create());
    PlanetSpawns.potential(aquatic);
    helper.assertTrue(
        aquatic.getSpawnerDataList().isEmpty(), "Land wildlife leaked into water spawn tables");
    var sheep = EntityType.SHEEP.create(level);
    sheep.moveTo(8.5, 100, 8.5);
    var uuid = sheep.getUUID();
    var position = sheep.position();
    PlanetSpawns.finalizeSpawn(
        new FinalizeSpawnEvent(
            sheep,
            level,
            8.5,
            100,
            8.5,
            level.getCurrentDifficultyAt(pos),
            MobSpawnType.NATURAL,
            null,
            null));
    level.addFreshEntity(sheep);
    helper.assertTrue(
        sheep.getColor() == DyeColor.RED
            && sheep.isNoAi()
            && sheep.getUUID().equals(uuid)
            && sheep.position().equals(position),
        "Spawn NBT was not applied after initialization or changed entity identity/location");
    planet.atmosphere = 0;
    var natural = new MobSpawnEvent.PositionCheck(sheep, level, MobSpawnType.NATURAL, null);
    PlanetEnvironment.checkSpawn(natural);
    helper.assertTrue(
        natural.getResult() == MobSpawnEvent.PositionCheck.Result.FAIL,
        "Unprotected wildlife was allowed to spawn naturally in vacuum");
    var egg = new MobSpawnEvent.PositionCheck(sheep, level, MobSpawnType.SPAWN_EGG, null);
    PlanetEnvironment.checkSpawn(egg);
    helper.assertTrue(
        egg.getResult() == MobSpawnEvent.PositionCheck.Result.DEFAULT,
        "Natural spawn restrictions also blocked explicit spawn eggs");
    sheep.discard();
    helper.succeed();
  }
}

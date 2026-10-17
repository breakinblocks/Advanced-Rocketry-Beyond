package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModTags;
import advRocketry.space.Planet;
import advRocketry.util.IdOrTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class LaserConfigurationTests {
  @SuppressWarnings("unchecked")
  private static <T> ModConfigSpec.ConfigValue<T> setting(String name) throws Exception {
    var field = AdvancedRocketryConfig.class.getDeclaredField(name);
    field.setAccessible(true);
    return (ModConfigSpec.ConfigValue<T>) field.get(null);
  }

  @GameTest(template = "empty")
  public static void drillOresComeFromTagsAndPlanetDefinitions(GameTestHelper helper)
      throws Exception {
    var planet = new Planet(2700, "Configured drill");
    var palette = OrbitalLaserLogic.orePalette(planet);
    int tagged = 0;
    for (var ignored : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.LASER_DRILL_ORES)) tagged++;
    helper.assertTrue(
        tagged == 6
            && palette.size() == tagged
            && palette.stream().allMatch(stack -> stack.is(ModTags.LASER_DRILL_ORES)),
        "Void drill palette does not match the laser_drill_ores tag");
    planet.laserOres.add(new IdOrTag("minecraft:coal", 3));
    planet.laserOres.add(new IdOrTag("#c:gems/diamond", 2));
    palette = OrbitalLaserLogic.orePalette(planet);
    helper.assertTrue(
        palette.stream().anyMatch(stack -> stack.is(Items.COAL) && stack.getCount() == 3)
            && palette.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 2),
        "Planet item or tag addition lost its stack size");
    helper.assertTrue(
        planet.laserDrillable && !Planet.load(withDrillDisabled(planet).save()).laserDrillable,
        "Per-planet drill exclusion did not survive save/reload");
    ModConfigSpec.ConfigValue<Boolean> terrain = setting("LASER_DRILL_PLANET");
    boolean oldTerrain = terrain.get();
    try {
      terrain.set(true);
      var controller =
          new OrbitalBlockEntity(
              BlockPos.ZERO, OrbitalRegistry.ORBITAL_LASER.get().defaultBlockState());
      helper.assertTrue(controller.laserTerrain, "New controller ignored terrain default");
      var saved = controller.saveWithoutMetadata(helper.getLevel().registryAccess());
      terrain.set(false);
      controller =
          new OrbitalBlockEntity(
              BlockPos.ZERO, OrbitalRegistry.ORBITAL_LASER.get().defaultBlockState());
      controller.loadWithComponents(saved, helper.getLevel().registryAccess());
      helper.assertTrue(controller.laserTerrain, "Changed default overwrote saved drill mode");
    } finally {
      terrain.set(oldTerrain);
    }
    helper.succeed();
  }

  private static Planet withDrillDisabled(Planet planet) {
    Planet copy = Planet.load(planet.save());
    copy.laserDrillable = false;
    return copy;
  }
}

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.space.Star;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class HologramTests {
  @GameTest(template = "empty")
  public static void worldBodiesSelectFocusNavigateAndRejectInvalidClicks(GameTestHelper helper) {
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
    var level = helper.getLevel();
    level.setBlock(pos, OrbitalRegistry.HOLOGRAPHIC_SELECTOR.get().defaultBlockState(), 3);
    var selector = (OrbitalBlockEntity) level.getBlockEntity(pos);
    var galaxy = GalaxyData.get(level.getServer());
    var player = helper.makeMockPlayer(GameType.CREATIVE);
    selector.tick();
    var earthBody =
        HolographicSelectorLogic.bodies(galaxy, selector).stream()
            .filter(
                body ->
                    body.kind() == HolographicSelectorLogic.Kind.PLANET
                        && body.id() == GalaxyData.EARTH_ID)
            .findFirst()
            .orElseThrow();
    var earth =
        selector.hologram.bodies.stream()
            .filter(entity -> entity.matches(earthBody))
            .findFirst()
            .orElseThrow();
    player.setPos(earth.position().add(0, 0, 1));
    helper.assertTrue(
        earth.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.CONSUME,
        "Native entity interaction failed");
    helper.assertTrue(
        selector.hologram.target == 0 && selector.hologram.center == -1,
        "First click must select without changing the system");
    helper.assertTrue(
        earth.selected() && earth.getBoundingBox().contains(earth.position()),
        "Selection or interaction bounds did not match the rendered body");
    earth.interact(player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        selector.hologram.center == 0 && selector.hologram.target == -1,
        "Second click did not focus Earth and its moons");
    helper.assertTrue(
        HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.PLANET, 1),
        "Luna was not selectable after focusing Earth");
    HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.BACK, 0);
    helper.assertTrue(
        selector.hologram.center == -1 && selector.hologram.star == 0, "Back did not restore Sol");
    HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.BACK, 0);
    helper.assertTrue(
        selector.hologram.star == -1 && earth.isRemoved(), "Back did not remove the old planet bodies");
    helper.assertTrue(
        !HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.PLANET, 0),
        "A stale planet click changed the galaxy view");
    HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.STAR, 0);
    helper.assertTrue(
        selector.hologram.star == -1 && selector.hologram.selectedStar == 0,
        "First star click did not select the star");
    HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.STAR, 0);
    helper.assertTrue(selector.hologram.star == 0, "Second star click did not open Sol");
    var body =
        selector.hologram.bodies.stream()
            .filter(entity -> entity.matches(earthBody))
            .findFirst()
            .orElseThrow();
    player.setPos(body.position().add(100, 0, 0));
    helper.assertTrue(
        body.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.PASS,
        "Out-of-reach interaction was accepted");
    helper.assertTrue(!body.save(new CompoundTag()), "UI entity would be saved into world chunks");
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    for (var entity : selector.hologram.bodies) entity.tick();
    helper.assertTrue(
        selector.hologram.bodies.stream().allMatch(HologramBodyEntity::isRemoved),
        "Breaking the controller left orphaned UI entities");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void redstoneRebuildsWithoutDuplicatesAndRejectsDisabledInteraction(
      GameTestHelper helper) {
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
    var level = helper.getLevel();
    level.setBlock(pos, OrbitalRegistry.HOLOGRAPHIC_SELECTOR.get().defaultBlockState(), 3);
    var selector = (OrbitalBlockEntity) level.getBlockEntity(pos);
    selector.tick();
    int count = selector.hologram.bodies.size();
    var body = selector.hologram.bodies.getFirst();
    selector.tick();
    helper.assertTrue(
        count > 1 && selector.hologram.bodies.size() == count && selector.hologram.bodies.contains(body),
        "Normal ticks recreated the hologram or duplicated its entities");
    selector.hologram.redstone = 1;
    helper.assertTrue(
        !HolographicSelectorLogic.select(selector, HolographicSelectorLogic.Kind.PLANET, 0),
        "Disabled hologram accepted interaction");
    selector.tick();
    helper.assertTrue(
        body.isRemoved() && selector.hologram.bodies.isEmpty(), "Redstone off left clickable bodies");
    level.setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    selector.tick();
    helper.assertTrue(selector.hologram.bodies.size() == count, "Power did not restore hologram");
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    for (var entity : selector.hologram.bodies) entity.tick();
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void companionStarsKeepTheirSizesColorsAndSystemScope(GameTestHelper helper) {
    var galaxy = GalaxyData.get(helper.getLevel().getServer());
    int starId = 1987;
    var original =
        galaxy.stars.put(
            starId,
            new Star(
                starId,
                "Hologram primary",
                100,
                1,
                0,
                0,
                false,
                List.of(
                    new Star.Companion("Companion", 20, .5f, false, 30),
                    new Star.Companion("Black hole", 200, 1.5f, true, 40))));
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
    helper
        .getLevel()
        .setBlock(pos, OrbitalRegistry.HOLOGRAPHIC_SELECTOR.get().defaultBlockState(), 3);
    var selector = (OrbitalBlockEntity) helper.getLevel().getBlockEntity(pos);
    selector.hologram.star = starId;
    try {
      selector.tick();
      var companions =
          HolographicSelectorLogic.bodies(galaxy, selector).stream()
              .filter(body -> body.kind() == HolographicSelectorLogic.Kind.COMPANION)
              .toList();
      helper.assertTrue(
          companions.size() == 2 && selector.hologram.bodies.size() == 4,
          "System hologram omitted companion stars or its back control");
      helper.assertTrue(
          companions.get(0).offset().x > 0 && companions.get(1).offset().x < 0,
          "Companion separation was not distributed around the primary");
      helper.assertTrue(
          companions.get(1).radius() > companions.get(0).radius()
              && companions.get(0).color() != companions.get(1).color(),
          "Companion size and black-hole appearance were collapsed");
      selector.hologram.star = -1;
      selector.tick();
      helper.assertTrue(
          HolographicSelectorLogic.bodies(galaxy, selector).stream()
              .noneMatch(body -> body.kind() == HolographicSelectorLogic.Kind.COMPANION),
          "Companions leaked into galaxy navigation");
    } finally {
      if (original == null) galaxy.stars.remove(starId);
      else galaxy.stars.put(starId, original);
      helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
      for (var entity : selector.hologram.bodies) entity.discard();
    }
    helper.succeed();
  }
}

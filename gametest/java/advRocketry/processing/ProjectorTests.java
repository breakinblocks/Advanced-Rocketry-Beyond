package advRocketry.processing;

import advRocketry.multiblock.Multiblocks;
import advRocketry.orbit.OrbitalRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class ProjectorTests {
  @GameTest(template = "empty")
  public static void materialCountsAndValidatedSelection(GameTestHelper helper) {
    var registries = helper.getLevel().registryAccess();
    for (ResourceLocation id : Multiblocks.ids(registries)) {
      var cells = ProjectorBlueprint.cells(registries, id, BlockPos.ZERO, Direction.NORTH);
      helper.assertTrue(!cells.isEmpty(), "No projector blueprint for " + id);
      helper.assertTrue(
          cells.stream().map(ProjectorBlueprint.Cell::position).distinct().count() == cells.size(),
          "Duplicate cells would double-count materials for " + id);
      helper.assertTrue(
          !ProjectorBlueprint.materials(registries, id).isEmpty(), "No material list for " + id);
    }
    var warp = ProjectorBlueprint.materials(registries, Multiblocks.id("warp_core"));
    helper.assertTrue(
        count(warp, ProcessingRegistry.part("titanium_block")) == 15
            && count(warp, Blocks.GOLD_BLOCK) == 2
            && count(warp, MachinePorts.BLOCK_STRUCTURE.get()) == 4
            && count(warp, OrbitalRegistry.WARP_CORE.get()) == 1,
        "Warp-core material counts differ from the original frame");
    var array = ProjectorBlueprint.materials(registries, Multiblocks.id("solar_array"));
    helper.assertTrue(
        array.stream()
            .anyMatch(
                material ->
                    material.optional()
                        && material.count() == 63
                        && material
                            .alternatives()
                            .contains(OrbitalRegistry.SOLAR_ARRAY_PANEL.get())),
        "Expandable solar-array panel area was listed as mandatory");
    var player = helper.makeMockPlayer(GameType.SURVIVAL);
    var projector = new ItemStack(ProcessingRegistry.PART_ITEMS.get("item_holoprojector").get());
    player.setItemInHand(InteractionHand.OFF_HAND, projector);
    ResourceLocation selected = Multiblocks.id("observatory");
    helper.assertTrue(
        new ProjectorSelection(selected, InteractionHand.OFF_HAND).apply(player)
            && HoloProjectorItem.machine(projector).equals(selected),
        "Off-hand selection was not saved");
    helper.assertTrue(
        !new ProjectorSelection(Multiblocks.id("missing"), InteractionHand.OFF_HAND).apply(player),
        "Unknown projector selection was accepted");
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    helper.assertTrue(
        !new ProjectorSelection(selected, InteractionHand.MAIN_HAND).apply(player),
        "Projector packet modified an unrelated held item");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void controllerPreviewRunsBeforeMachineMenu(GameTestHelper helper) {
    BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
    helper.getLevel().setBlock(pos, OrbitalRegistry.WARP_CORE.get().defaultBlockState(), 2);
    var player = helper.makeMockPlayer(GameType.CREATIVE);
    var projector = new ItemStack(ProcessingRegistry.PART_ITEMS.get("item_holoprojector").get());
    player.setItemInHand(InteractionHand.MAIN_HAND, projector);
    var context =
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    helper.assertTrue(
        projector.getItem().onItemUseFirst(projector, context).consumesAction(),
        "Machine menu would intercept projector before preview selection");
    helper.assertTrue(
        HoloProjectorItem.machine(projector).equals(Multiblocks.id("warp_core")),
        "Controller preview did not select the clicked machine");
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 2400)
  public static void showcaseBlueprintsFormEveryCheckedStructure(GameTestHelper helper) {
    var level = helper.getLevel();
    BlockPos origin = helper.absolutePos(new BlockPos(8, 3, 8)).atY(260);
    List<String> unchecked = new ArrayList<>();
    List<CompletableFuture<?>> light = new ArrayList<>();
    var sequence = helper.startSequence();
    for (ResourceLocation id : Multiblocks.ids(level.registryAccess())) {
      var cells = ProjectorBlueprint.cells(level.registryAccess(), id, origin, Direction.NORTH);
      sequence
          .thenExecute(
              () -> {
                BlueprintPlacer.place(level, cells, Direction.NORTH);
                light.add(settled(level, cells));
              })
          .thenWaitUntil(() -> helper.assertTrue(light.getLast().isDone(), "Light pending"))
          .thenExecute(
              () -> {
                var controller = level.getBlockEntity(origin);
                if (BlueprintPlacer.checked(controller))
                  helper.assertTrue(
                      BlueprintPlacer.formed(controller),
                      id + " built from its projector blueprint did not form");
                else unchecked.add(id.getPath());
                for (var cell : cells)
                  level.setBlock(cell.position(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                light.add(settled(level, cells));
              })
          .thenWaitUntil(() -> helper.assertTrue(light.getLast().isDone(), "Light pending"));
    }
    sequence
        .thenExecute(
            () ->
                helper.assertTrue(
                    unchecked.equals(List.of("satellite_builder")),
                    "Unexpected structures without a formation check: " + unchecked))
        .thenSucceed();
  }

  private static CompletableFuture<?> settled(
      ServerLevel level, List<ProjectorBlueprint.Cell> cells) {
    return CompletableFuture.allOf(
        cells.stream()
            .map(cell -> new ChunkPos(cell.position()))
            .distinct()
            .map(
                chunk ->
                    level.getChunkSource().getLightEngine().waitForPendingTasks(chunk.x, chunk.z))
            .toArray(CompletableFuture[]::new));
  }

  private static int count(List<ProjectorBlueprint.Material> materials, Block block) {
    return materials.stream()
        .filter(material -> material.alternatives().contains(block))
        .mapToInt(ProjectorBlueprint.Material::count)
        .sum();
  }
}

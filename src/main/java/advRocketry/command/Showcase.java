package advRocketry.command;

import advRocketry.Main;
import advRocketry.multiblock.Multiblocks;
import advRocketry.processing.BlueprintPlacer;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProjectorBlueprint;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Showcase {
  private static final int BLOCK_COLUMNS = 16;
  private static final int ROW_WIDTH = 64;
  private static final int ITEM_COLUMNS = 24;
  private static final int GAP = 3;

  private static final TagKey<Block> CONNECTED =
      TagKey.create(
          Registries.BLOCK,
          ResourceLocation.fromNamespaceAndPath(Main.MODID, "connected_textures"));

  private record Plan(ResourceLocation id, BlockPos origin, List<ProjectorBlueprint.Cell> cells) {}

  private Showcase() {}

  public static int build(CommandSourceStack source) {
    ServerLevel level = source.getLevel();
    BlockPos start = BlockPos.containing(source.getPosition()).south(3);
    List<Block> blocks =
        BuiltInRegistries.BLOCK.entrySet().stream()
            .filter(entry -> entry.getKey().location().getNamespace().equals(Main.MODID))
            .sorted(Comparator.comparing(entry -> entry.getKey().location().getPath()))
            .map(Map.Entry::getValue)
            .filter(block -> block.asItem() != Items.AIR)
            .toList();
    List<Item> items =
        BuiltInRegistries.ITEM.entrySet().stream()
            .filter(entry -> entry.getKey().location().getNamespace().equals(Main.MODID))
            .sorted(Comparator.comparing(entry -> entry.getKey().location().getPath()))
            .map(Map.Entry::getValue)
            .toList();
    int blockDepth = (blocks.size() + BLOCK_COLUMNS - 1) / BLOCK_COLUMNS * 2;
    List<Block> connected =
        BuiltInRegistries.BLOCK
            .getTag(CONNECTED)
            .map(tag -> tag.stream().map(holder -> holder.value()).toList())
            .orElse(List.of());
    int swatchZ = blockDepth + GAP;
    List<Plan> plans = new ArrayList<>();
    int cursorX = 0, cursorZ = swatchZ + (connected.isEmpty() ? 0 : 1 + GAP), rowDepth = 0;
    int tallest = 4;
    var registries = level.registryAccess();
    for (ResourceLocation id : Multiblocks.ids(registries)) {
      var probe = ProjectorBlueprint.cells(registries, id, BlockPos.ZERO, Direction.NORTH);
      int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
      for (var cell : probe) {
        BlockPos p = cell.position();
        minX = Math.min(minX, p.getX());
        minY = Math.min(minY, p.getY());
        minZ = Math.min(minZ, p.getZ());
        maxX = Math.max(maxX, p.getX());
        maxY = Math.max(maxY, p.getY());
        maxZ = Math.max(maxZ, p.getZ());
      }
      int width = maxX - minX + 1, depth = maxZ - minZ + 1;
      if (cursorX > 0 && cursorX + width > ROW_WIDTH) {
        cursorX = 0;
        cursorZ += rowDepth + GAP;
        rowDepth = 0;
      }
      BlockPos origin = start.offset(cursorX - minX, -minY, cursorZ - minZ);
      plans.add(
          new Plan(id, origin, ProjectorBlueprint.cells(registries, id, origin, Direction.NORTH)));
      cursorX += width + GAP;
      rowDepth = Math.max(rowDepth, depth);
      tallest = Math.max(tallest, maxY - minY + 1);
    }
    int wallZ = cursorZ + rowDepth + GAP;
    int itemRows = (items.size() + ITEM_COLUMNS - 1) / ITEM_COLUMNS;
    int width = Math.max(ROW_WIDTH, Math.max(BLOCK_COLUMNS * 2, ITEM_COLUMNS));
    int height = Math.max(tallest, itemRows) + 3;
    clear(level, start, width, wallZ, height);
    for (int i = 0; i < blocks.size(); i++) {
      BlockPos pos = start.offset(i % BLOCK_COLUMNS * 2, 0, i / BLOCK_COLUMNS * 2);
      placeBlock(level, pos, blocks.get(i));
      label(source, pos.getCenter().add(0, 1.2, 0), blocks.get(i).getName().getString(), 0.5f);
    }
    for (int i = 0; i < connected.size(); i++) {
      BlockPos corner = start.offset(i % (ROW_WIDTH / 4) * 4, 0, swatchZ + i / (ROW_WIDTH / 4) * 2);
      BlockState state =
          BlueprintPlacer.facing(connected.get(i).defaultBlockState(), Direction.NORTH);
      for (int x = 0; x < 3; x++)
        for (int y = 0; y < 3; y++)
          if (x != 1 || y != 1 || i % 2 == 0)
            level.setBlock(corner.offset(x, y, 0), state, Block.UPDATE_ALL);
      label(
          source,
          corner.offset(1, 3, 0).getCenter().add(0, 0.3, 0),
          connected.get(i).getName().getString(),
          0.5f);
    }
    List<String> unformed = new ArrayList<>();
    for (Plan plan : plans) {
      BlueprintPlacer.place(level, plan.cells(), Direction.NORTH);
      var controller = level.getBlockEntity(plan.origin());
      if (BlueprintPlacer.checked(controller) && !BlueprintPlacer.formed(controller))
        unformed.add(plan.id().toString());
      double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minZ = Double.MAX_VALUE;
      double maxZ = -Double.MAX_VALUE, top = -Double.MAX_VALUE;
      for (var cell : plan.cells()) {
        minX = Math.min(minX, cell.position().getX());
        maxX = Math.max(maxX, cell.position().getX() + 1);
        minZ = Math.min(minZ, cell.position().getZ());
        maxZ = Math.max(maxZ, cell.position().getZ() + 1);
        top = Math.max(top, cell.position().getY() + 1);
      }
      label(
          source,
          new Vec3((minX + maxX) / 2, top + 1.5, (minZ + maxZ) / 2),
          Multiblocks.title(plan.id()).getString(),
          1.6f);
    }
    BlockPos wall = start.offset(0, 0, wallZ);
    for (int i = 0; i < items.size(); i++) {
      BlockPos back = wall.offset(i % ITEM_COLUMNS, itemRows - 1 - i / ITEM_COLUMNS, 0);
      level.setBlock(
          back, MachinePorts.BLOCK_STRUCTURE.get().defaultBlockState(), Block.UPDATE_ALL);
      ItemFrame frame = new ItemFrame(level, back.north(), Direction.NORTH);
      frame.setItem(new ItemStack(items.get(i)), false);
      frame.setInvulnerable(true);
      level.addFreshEntity(frame);
    }
    int multiblocks = plans.size();
    source.sendSuccess(
        () ->
            Texts.translate(
                "message.adv_rocketry.showcase.showcase_built_blocks_multiblocks_items",
                blocks.size(),
                multiblocks,
                items.size(),
                (unformed.isEmpty() ? "" : ". Did not form: " + String.join(", ", unformed))),
        true);
    return blocks.size() + multiblocks + items.size();
  }

  private static void clear(ServerLevel level, BlockPos start, int width, int depth, int height) {
    BlockPos min = start.offset(-2, 0, -2);
    BlockPos max = start.offset(width + 1, height, depth + 1);
    AABB area = new AABB(min.getCenter(), max.getCenter()).inflate(4);
    level.getEntitiesOfClass(ItemFrame.class, area).forEach(ItemFrame::discard);
    level.getEntitiesOfClass(Display.TextDisplay.class, area).forEach(Display.TextDisplay::discard);
    for (int x = min.getX(); x <= max.getX(); x++)
      for (int z = min.getZ(); z <= max.getZ(); z++) {
        level.setBlock(
            new BlockPos(x, start.getY() - 1, z),
            Blocks.SMOOTH_STONE.defaultBlockState(),
            Block.UPDATE_CLIENTS);
        for (int y = start.getY(); y <= max.getY(); y++)
          level.setBlock(
              new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
      }
  }

  private static void label(CommandSourceStack source, Vec3 at, String text, float scale) {
    String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'");
    String command =
        String.format(
            Locale.ROOT,
            "summon minecraft:text_display %.3f %.3f %.3f"
                + " {billboard:\"center\",alignment:\"center\",text:'\"%s\"',"
                + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],"
                + "translation:[0f,0f,0f],scale:[%.2ff,%.2ff,%.2ff]}}",
            at.x,
            at.y,
            at.z,
            escaped,
            scale,
            scale,
            scale);
    source
        .getServer()
        .getCommands()
        .performPrefixedCommand(source.withSuppressedOutput().withPermission(4), command);
  }

  private static void placeBlock(ServerLevel level, BlockPos pos, Block block) {
    BlockState state = BlueprintPlacer.facing(block.defaultBlockState(), Direction.NORTH);
    if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
      level.setBlock(
          pos,
          state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER),
          Block.UPDATE_ALL);
      level.setBlock(
          pos.above(),
          state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER),
          Block.UPDATE_ALL);
    } else level.setBlock(pos, state, Block.UPDATE_ALL);
  }
}

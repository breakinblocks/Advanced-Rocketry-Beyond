// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.ModTags;
import advRocketry.processing.MachinePorts;
import advRocketry.space.GalaxyData;
import java.util.Collection;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original combustion-independent thermite light and station lamp. */
public final class LightingRegistry {
  private static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(Main.MODID);
  private static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(Main.MODID);

  public static final Supplier<Block> CIRCLE_LIGHT =
      BLOCKS.register(
          "circle_light",
          () -> new Block(BlockBehaviour.Properties.of().strength(2).lightLevel(state -> 15)));
  public static final Supplier<TorchBlock> THERMITE_TORCH =
      BLOCKS.register(
          "thermite_torch",
          () ->
              new TorchBlock(
                  ParticleTypes.FLAME,
                  BlockBehaviour.Properties.of()
                      .noCollission()
                      .instabreak()
                      .lightLevel(state -> 15)));
  public static final Supplier<WallTorchBlock> THERMITE_WALL_TORCH =
      BLOCKS.register(
          "thermite_wall_torch",
          () ->
              new WallTorchBlock(
                  ParticleTypes.FLAME,
                  BlockBehaviour.Properties.of()
                      .noCollission()
                      .instabreak()
                      .lightLevel(state -> 15)));
  public static final Supplier<ExtinguishedTorchBlock> UNLIT_TORCH =
      BLOCKS.register(
          "unlit_torch",
          () ->
              new ExtinguishedTorchBlock(
                  BlockBehaviour.Properties.of().noCollission().instabreak()));
  public static final Supplier<ExtinguishedWallTorchBlock> UNLIT_WALL_TORCH =
      BLOCKS.register(
          "unlit_wall_torch",
          () ->
              new ExtinguishedWallTorchBlock(
                  BlockBehaviour.Properties.of().noCollission().instabreak()));
  public static final Supplier<Item> UNLIT_TORCH_ITEM =
      ITEMS.register(
          "unlit_torch",
          () ->
              new StandingAndWallBlockItem(
                  UNLIT_TORCH.get(),
                  UNLIT_WALL_TORCH.get(),
                  new Item.Properties(),
                  Direction.DOWN));

  static {
    ITEMS.register("circle_light", () -> new BlockItem(CIRCLE_LIGHT.get(), new Item.Properties()));
    ITEMS.register(
        "thermite_torch",
        () ->
            new StandingAndWallBlockItem(
                THERMITE_TORCH.get(),
                THERMITE_WALL_TORCH.get(),
                new Item.Properties(),
                Direction.DOWN));
  }

  private LightingRegistry() {}

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    bus.addListener(LightingRegistry::creative);
    NeoForge.EVENT_BUS.addListener(LightingRegistry::placed);
    NeoForge.EVENT_BUS.addListener(LightingRegistry::drops);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) {
      event.accept(CIRCLE_LIGHT.get());
      event.accept(THERMITE_TORCH.get());
    }
  }

  public static boolean combustible(ServerLevel level, BlockPos pos) {
    var planet = GalaxyData.get(level.getServer()).planet(level);
    return SealedRooms.breathable(level, pos) || planet.oxygen && planet.atmosphere > 25;
  }

  static ItemInteractionResult relight(
      ItemStack stack, Level level, BlockPos pos, BlockState state) {
    if (!stack.is(Items.TORCH) && !stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (level instanceof ServerLevel serverLevel && combustible(serverLevel, pos)) {
      BlockState lit =
          state.is(UNLIT_WALL_TORCH.get())
              ? Blocks.WALL_TORCH
                  .defaultBlockState()
                  .setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING))
              : Blocks.TORCH.defaultBlockState();
      level.setBlock(pos, lit, 3);
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }

  private static void placed(BlockEvent.EntityPlaceEvent event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    BlockPos pos = event.getPos();
    BlockState state = event.getPlacedBlock();
    if (combustible(level, pos)) return;
    if (state.is(ModTags.TORCHES)) {
      event.setCanceled(true);
      return;
    }
    extinguish(level, pos, state);
  }

  private static void extinguish(ServerLevel level, BlockPos pos, BlockState state) {
    if (state.is(Blocks.TORCH)) level.setBlock(pos, UNLIT_TORCH.get().defaultBlockState(), 3);
    else if (state.is(Blocks.WALL_TORCH))
      level.setBlock(
          pos,
          UNLIT_WALL_TORCH
              .get()
              .defaultBlockState()
              .setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING)),
          3);
  }

  static void atmosphereLost(ServerLevel level, Collection<BlockPos> positions) {
    for (BlockPos pos : positions) {
      if (!level.hasChunkAt(pos)) continue;
      BlockState state = level.getBlockState(pos);
      boolean torch = state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
      boolean configured = !torch && state.is(ModTags.TORCHES);
      if (!torch && !configured || combustible(level, pos)) continue;
      if (configured) {
        level.removeBlock(pos, false);
        Block.popResource(level, pos, new ItemStack(state.getBlock()));
      } else extinguish(level, pos, state);
    }
  }

  private static void drops(BlockDropsEvent event) {
    if (!AdvancedRocketryConfig.dropExtinguishedTorches()
        || !event.getState().is(UNLIT_TORCH.get()) && !event.getState().is(UNLIT_WALL_TORCH.get()))
      return;
    for (var drop : event.getDrops()) {
      ItemStack stack = drop.getItem();
      if (stack.is(Items.TORCH))
        drop.setItem(new ItemStack(UNLIT_TORCH_ITEM.get(), stack.getCount()));
    }
  }
}

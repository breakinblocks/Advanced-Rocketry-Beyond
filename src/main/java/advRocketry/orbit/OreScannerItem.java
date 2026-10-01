// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.Tags;

/** Original ore-mapping satellite controller, with a server-scanned density map. */
public final class OreScannerItem extends Item {
  public OreScannerItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!(level instanceof ServerLevel serverLevel)
        || !(player instanceof ServerPlayer serverPlayer))
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    long id = SatelliteLink.read(stack);
    GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
    SatelliteLogic.advance(galaxy, id, serverLevel.getGameTime());
    Satellite record = galaxy.satellites.get(id);
    int orbit = StationLogic.orbitPlanet(galaxy, serverLevel, player.blockPosition());
    if (record == null || !record.orbits(SatelliteType.ORE_SCANNER, orbit)) {
      player.displayClientMessage(
          Component.translatable("message.adv_rocketry.ore_scanner.no_ore_mapper_in_this_orbit"),
          true);
      return InteractionResultHolder.success(stack);
    }
    if (record.energy < 1000) {
      player.displayClientMessage(
          Component.translatable("message.adv_rocketry.ore_scanner.ore_mapper_needs_1_000_fe"),
          true);
      return InteractionResultHolder.success(stack);
    }
    record.energy -= 1000;
    galaxy.setDirty();
    int[] pixels = scan(serverLevel, player.blockPosition());
    serverPlayer.openMenu(
        new MenuProvider() {
          @Override
          public Component getDisplayName() {
            return Component.translatable("message.adv_rocketry.ore_scanner.orbital_ore_map");
          }

          @Override
          public AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player owner) {
            return new OreScanMenu(menuId, pixels);
          }
        },
        buffer -> buffer.writeVarIntArray(pixels));
    return InteractionResultHolder.success(stack);
  }

  static int[] scan(ServerLevel level, BlockPos center) {
    int[] pixels = new int[256];
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        int worldX = center.getX() + x * 2 - 16;
        int worldZ = center.getZ() + z * 2 - 16;
        BlockPos column = new BlockPos(worldX, center.getY(), worldZ);
        if (!level.hasChunkAt(column)) continue;
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
        int ores = 0;
        for (int y = level.getMinBuildHeight(); y < top; y++) {
          if (level.getBlockState(new BlockPos(worldX, y, worldZ)).is(Tags.Blocks.ORES)) ores++;
        }
        pixels[z * 16 + x] = Math.min(255, ores * 64);
      }
    return pixels;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    long id = SatelliteLink.read(stack);
    tooltip.add(
        id > 0
            ? Texts.translate("message.adv_rocketry.ore_scanner.linked_to_ore_mapper", id)
            : Component.translatable("message.adv_rocketry.ore_scanner.unprogrammed"));
  }
}

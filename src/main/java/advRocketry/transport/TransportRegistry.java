// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.transport;

import advRocketry.Main;
import advRocketry.processing.MachinePorts;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TransportRegistry {
  public enum Kind {
    ENERGY("energy_cable", 5000),
    FLUID("fluid_pipe", 4000),
    ITEM("item_conduit", 16);
    public final String id;
    public final int rate;

    Kind(String id, int rate) {
      this.id = id;
      this.rate = rate;
    }
  }

  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
  private static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, Main.MODID);
  public static final Map<Kind, Supplier<TransportBlock>> ALL = new EnumMap<>(Kind.class);

  static {
    for (Kind kind : Kind.values()) {
      var block =
          BLOCKS.register(
              kind.id,
              () ->
                  new TransportBlock(
                      kind, BlockBehaviour.Properties.of().strength(1.5f).noOcclusion()));
      ALL.put(kind, block);
      ITEMS.register(kind.id, () -> new BlockItem(block.get(), new Item.Properties()));
      MachinePorts.ALL.put(kind.id, block::get);
    }
  }

  public static final Supplier<BlockEntityType<TransportBlockEntity>> ENTITY =
      ENTITIES.register(
          "transport",
          () ->
              BlockEntityType.Builder.of(
                      TransportBlockEntity::new,
                      ALL.values().stream().map(Supplier::get).toArray(Block[]::new))
                  .build(null));
  public static final Supplier<MenuType<TransportMenu>> MENU =
      MENUS.register("transport", () -> IMenuTypeExtension.create(TransportMenu::new));

  private TransportRegistry() {}

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    MENUS.register(bus);
  }
}

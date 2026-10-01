// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.Main;
import advRocketry.processing.MachinePorts;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HovercraftRegistry {
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  private static final DeferredRegister<EntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.ENTITY_TYPE, Main.MODID);
  public static final Supplier<HovercraftItem> ITEM =
      ITEMS.register("hovercraft", () -> new HovercraftItem(new Item.Properties()));
  public static final Supplier<EntityType<HovercraftEntity>> ENTITY =
      ENTITIES.register(
          "hovercraft",
          () ->
              EntityType.Builder.of(HovercraftEntity::new, MobCategory.MISC)
                  .sized(2.5f, 1f)
                  .clientTrackingRange(10)
                  .updateInterval(1)
                  .build("hovercraft"));

  private HovercraftRegistry() {}

  public static void register(IEventBus bus) {
    ITEMS.register(bus);
    ENTITIES.register(bus);
    bus.addListener(HovercraftRegistry::creative);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) event.accept(ITEM.get());
  }
}

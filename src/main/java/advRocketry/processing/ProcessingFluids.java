// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.Main;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ProcessingFluids {
  private static final DeferredRegister<FluidType> TYPES =
      DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Main.MODID);
  private static final DeferredRegister<Fluid> FLUIDS =
      DeferredRegister.create(Registries.FLUID, Main.MODID);
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, Main.MODID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Registries.ITEM, Main.MODID);
  public static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();

  public static final class Definition {
    public final String name;
    public final Supplier<FluidType> type;
    public final Supplier<FlowingFluid> source;
    public final Supplier<FlowingFluid> flowing;
    public final Supplier<LiquidBlock> block;
    public final Supplier<Item> bucket;
    public final int color;

    private Definition(String name, int color) {
      this.name = name;
      this.color = color;
      type =
          TYPES.register(
              name,
              () ->
                  name.equals("enriched_lava")
                      ? new EnrichedLavaFluidType(propertiesFor(name))
                      : new FluidType(propertiesFor(name)));
      boolean gas = !name.equals("rocket_fuel") && !name.equals("enriched_lava");
      source =
          FLUIDS.register(
              name,
              () ->
                  gas
                      ? new RisingGasFluid(properties(), true)
                      : new BaseFlowingFluid.Source(properties()));
      flowing =
          FLUIDS.register(
              "flowing_" + name,
              () ->
                  gas
                      ? new RisingGasFluid(properties(), false)
                      : new BaseFlowingFluid.Flowing(properties()));
      block =
          BLOCKS.register(
              name,
              () -> {
                var properties =
                    BlockBehaviour.Properties.of()
                        .noCollission()
                        .strength(100)
                        .noLootTable()
                        .replaceable()
                        .liquid()
                        .lightLevel(
                            state ->
                                name.equals("enriched_lava")
                                    ? 15
                                    : name.equals("rocket_fuel") ? 2 : 0);
                return new LiquidBlock(source.get(), properties);
              });
      bucket =
          ITEMS.register(
              name + "_bucket",
              () ->
                  new BucketItem(
                      source.get(),
                      new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    }

    private BaseFlowingFluid.Properties properties() {
      return new BaseFlowingFluid.Properties(type, source, flowing)
          .block(block)
          .bucket(bucket)
          .tickRate(viscosity(name) / 200);
    }
  }

  static {
    define("oxygen", 0xff6ce2ff);
    define("hydrogen", 0xffdbc1c1);
    define("nitrogen", 0xffdfe5fe);
    define("helium", 0xffe1d4ae);
    define("helium3", 0xffd5c7a3);
    define("ammonia", 0xffb4bed6);
    define("methane", 0xff9ab8c2);
    define("rocket_fuel", 0xffe5d884);
    define("enriched_lava", 0xffffffff);
  }

  private ProcessingFluids() {}

  public static FlowingFluid oxygen() {
    return DEFINITIONS.get("oxygen").source.get();
  }

  public static FlowingFluid hydrogen() {
    return DEFINITIONS.get("hydrogen").source.get();
  }

  public static boolean isInLava(Entity entity) {
    return entity.isInLava()
        || entity.getFluidTypeHeight(DEFINITIONS.get("enriched_lava").type.get()) > 0;
  }

  public static void lavaContact(EntityTickEvent.Post event) {
    Entity entity = event.getEntity();
    if (entity.level().isClientSide || entity.isInLava() || !isInLava(entity)) return;
    entity.lavaHurt();
    entity.fallDistance *=
        entity.getFluidFallDistanceModifier(DEFINITIONS.get("enriched_lava").type.get());
  }

  private static FluidType.Properties propertiesFor(String name) {
    FluidType.Properties properties = FluidType.Properties.create().viscosity(viscosity(name));
    if (name.equals("enriched_lava"))
      return properties
          .density(3000)
          .temperature(1300)
          .lightLevel(15)
          .canExtinguish(false)
          .canDrown(false)
          .canSwim(false)
          .supportsBoating(false)
          .pathType(PathType.LAVA)
          .adjacentPathType(PathType.LAVA);
    if (name.equals("rocket_fuel")) return properties.density(800).lightLevel(2);
    return properties
        .density(-1000)
        .canPushEntity(false)
        .canSwim(false)
        .canDrown(false)
        .canExtinguish(false)
        .supportsBoating(false);
  }

  private static int viscosity(String name) {
    return name.equals("enriched_lava") ? 6000 : name.equals("rocket_fuel") ? 1500 : 1000;
  }

  private static void define(String name, int color) {
    DEFINITIONS.put(name, new Definition(name, color));
  }

  public static void register(IEventBus bus) {
    TYPES.register(bus);
    FLUIDS.register(bus);
    BLOCKS.register(bus);
    ITEMS.register(bus);
    bus.addListener(ProcessingFluids::creative);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get())
      DEFINITIONS.values().forEach(fluid -> event.accept(fluid.bucket.get()));
  }
}

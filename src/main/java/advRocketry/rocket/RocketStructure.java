// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.util.AssemblyFailure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Palette-compressed ship contents, including native block-entity inventories and components. */
public final class RocketStructure {
  public record Cell(BlockPos position, BlockState state, CompoundTag data) {}

  public final List<Cell> cells = new ArrayList<>();
  public int width = 1, height = 1, depth = 1;
  public int thrust, consumption, capacity, oxidizerCapacity;
  public int seats;
  public float drillingPower;
  public int intakePower;
  public RocketPartBlock.Fuel fuel = RocketPartBlock.Fuel.MONOPROPELLANT;
  public int destination = 1;
  public long stationId;

  public record StoredFuel(
      ResourceLocation primaryFluid,
      int primaryAmount,
      ResourceLocation oxidizerFluid,
      int oxidizerAmount) {}

  public StoredFuel storedFuel(HolderLookup.Provider registries) {
    ResourceLocation primary = RocketFuelRegistry.defaultFluid(fuel);
    ResourceLocation oxidizer = RocketFuelRegistry.defaultFluid(RocketPartBlock.Fuel.OXIDIZER);
    int primaryAmount = 0, oxidizerAmount = 0;
    for (Cell cell : cells) {
      if (!(cell.state().getBlock() instanceof RocketPartBlock part)
          || part.kind != RocketPartBlock.Kind.TANK) continue;
      FluidTank tank = new FluidTank(1000);
      tank.readFromNBT(registries, cell.data().getCompound("tank"));
      if (tank.isEmpty()) continue;
      ResourceLocation fluid = RocketFuelRegistry.fluidId(tank.getFluid());
      if (!RocketFuelRegistry.allowed(part.fuel, tank.getFluid()))
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_structure.rocket_tank_contains_an_unconfigured_fuel"));
      if (part.fuel == fuel) {
        if (primaryAmount > 0 && !primary.equals(fluid))
          throw new AssemblyFailure(
              Component.translatable(
                  "message.adv_rocketry.rocket_structure.do_not_mix_propellants_in_rocket"));
        primary = fluid;
        primaryAmount += tank.getFluidAmount();
      } else if (part.fuel == RocketPartBlock.Fuel.OXIDIZER) {
        if (oxidizerAmount > 0 && !oxidizer.equals(fluid))
          throw new AssemblyFailure(
              Component.translatable(
                  "message.adv_rocketry.rocket_structure.do_not_mix_oxidizers_in_rocket"));
        oxidizer = fluid;
        oxidizerAmount += tank.getFluidAmount();
      }
    }
    return new StoredFuel(primary, primaryAmount, oxidizer, oxidizerAmount);
  }

  public CompoundTag save(boolean inventories) {
    CompoundTag tag = new CompoundTag();
    ListTag palette = new ListTag();
    Map<BlockState, Integer> states = new LinkedHashMap<>();
    int[] blocks = new int[cells.size()];
    ListTag entities = new ListTag();
    for (int i = 0; i < cells.size(); i++) {
      Cell cell = cells.get(i);
      int index =
          states.computeIfAbsent(
              cell.state(),
              state -> {
                palette.add(NbtUtils.writeBlockState(state));
                return states.size();
              });
      blocks[i] =
          (index << 14)
              | (cell.position().getY() << 8)
              | (cell.position().getZ() << 4)
              | cell.position().getX();
      if (inventories && !cell.data().isEmpty()) {
        CompoundTag entity = new CompoundTag();
        entity.putInt("index", i);
        entity.put("data", cell.data().copy());
        entities.add(entity);
      }
    }
    tag.put("palette", palette);
    tag.putIntArray("blocks", blocks);
    tag.put("block_entities", entities);
    tag.putInt("width", width);
    tag.putInt("height", height);
    tag.putInt("depth", depth);
    tag.putInt("thrust", thrust);
    tag.putInt("consumption", consumption);
    tag.putInt("capacity", capacity);
    tag.putInt("oxidizer_capacity", oxidizerCapacity);
    tag.putInt("seats", seats);
    tag.putFloat("drilling_power", drillingPower);
    tag.putInt("intake_power", intakePower);
    tag.putInt("destination", destination);
    tag.putLong("station_id", stationId);
    tag.putString("fuel", fuel.name());
    return tag;
  }

  public static RocketStructure load(CompoundTag tag) {
    RocketStructure structure = new RocketStructure();
    List<BlockState> palette = new ArrayList<>();
    for (Tag entry : tag.getList("palette", Tag.TAG_COMPOUND))
      palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), (CompoundTag) entry));
    Map<Integer, CompoundTag> entities = new HashMap<>();
    for (Tag entry : tag.getList("block_entities", Tag.TAG_COMPOUND)) {
      CompoundTag entity = (CompoundTag) entry;
      entities.put(entity.getInt("index"), entity.getCompound("data"));
    }
    int[] blocks = tag.getIntArray("blocks");
    for (int i = 0; i < blocks.length; i++) {
      int packed = blocks[i];
      int state = packed >>> 14;
      if (state >= palette.size())
        throw new AssemblyFailure(
            Component.translatable(
                "message.adv_rocketry.rocket_structure.rocket_contains_an_invalid_block_palette"));
      structure.cells.add(
          new Cell(
              new BlockPos(packed & 15, (packed >>> 8) & 63, (packed >>> 4) & 15),
              palette.get(state),
              entities.getOrDefault(i, new CompoundTag()).copy()));
    }
    structure.width = tag.getInt("width");
    structure.height = tag.getInt("height");
    structure.depth = tag.getInt("depth");
    structure.thrust = tag.getInt("thrust");
    structure.consumption = tag.getInt("consumption");
    structure.capacity = tag.getInt("capacity");
    structure.oxidizerCapacity = tag.getInt("oxidizer_capacity");
    structure.seats = tag.getInt("seats");
    structure.drillingPower = tag.getFloat("drilling_power");
    structure.intakePower = tag.getInt("intake_power");
    structure.destination = tag.getInt("destination");
    structure.stationId = tag.getLong("station_id");
    structure.fuel = RocketPartBlock.Fuel.valueOf(tag.getString("fuel"));
    return structure;
  }

  public double acceleration(float gravity) {
    return (thrust - cells.size() * gravity) / 10000d;
  }

  public int requiredFuel(int launchY, float gravity) {
    return requiredFuel(launchY, gravity, AdvancedRocketryConfig.orbitHeight());
  }

  public int requiredFuel(int launchY, float gravity, int orbitHeight) {
    double acceleration = acceleration(AdvancedRocketryConfig.gravityAffectsFuel() ? gravity : 1f);
    return acceleration <= 0
        ? Integer.MAX_VALUE
        : (int)
            Math.ceil(
                2 * consumption * Math.sqrt(2 * Math.max(0, orbitHeight - launchY) / acceleration));
  }
}

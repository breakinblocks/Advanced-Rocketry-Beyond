// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.ModComponents;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.PlanetIdChipItem;
import advRocketry.orbit.StationLink;
import advRocketry.space.GalaxyData;
import advRocketry.util.ComponentText;
import advRocketry.util.MachineEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class RocketBlockEntity extends BlockEntity implements MenuProvider {
  public int destination = 1;
  public long stationId;
  public CompoundTag stationLandingPads = new CompoundTag();
  public Component status = Component.translatable("status.adv_rocketry.rocket.awaiting_scan");
  public int mass;
  public int thrust;
  public int fuel;
  public int scannedVolume;
  public int buildRemaining;
  public int buildTotal;
  public boolean ready;
  RocketEntity lastAssembledRocket;

  public void beginAssembly() {
    if (!(level instanceof ServerLevel server)) return;
    if (buildRemaining > 0) return;
    if (kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER) DeployableAssembly.scan(this);
    else RocketAssembly.scan(this);
    if (!ready) return;
    if (energy.consume(100, true) < 100) {
      status =
          Component.translatable("status.adv_rocketry.rocket.assembly_requires_100_fe_per_tick");
      setChanged();
      server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
      return;
    }
    buildTotal =
        Math.max(
            1, (int) Math.round(scannedVolume * AdvancedRocketryConfig.buildSpeedMultiplier()));
    buildRemaining = buildTotal;
    status = Component.translatable("status.adv_rocketry.rocket.assembling_rocket");
    setChanged();
    server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  public void tick() {
    if (buildRemaining <= 0 || !(level instanceof ServerLevel)) return;
    if (energy.consume(100, true) < 100) return;
    energy.consume(100, false);
    buildRemaining--;
    if (buildRemaining == 0) {
      if (kind() == RocketPartBlock.Kind.DEPLOYABLE_BUILDER) DeployableAssembly.assemble(this);
      else RocketAssembly.assemble(this);
    }
    if (buildRemaining == 0 || buildRemaining % 20 == 0) setChanged();
  }

  public final ItemStackHandler guidanceInventory =
      new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
          return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
          ItemStack chip = getStackInSlot(slot);
          if (PlanetIdChipItem.programmed(chip)) destination = PlanetIdChipItem.planet(chip);
          else if (chip.is(OrbitalRegistry.STATION_CHIP.get())) {
            destination = GalaxyData.SPACE_ID;
            stationId = StationLink.read(chip);
          }
          if (level instanceof ServerLevel server) {
            var route = GuidanceComputer.route(server, chip, worldPosition);
            if (route != null) {
              destination = route.planet();
              stationId = route.stationId();
            }
          }
          setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return slot == 0 && GuidanceComputer.accepts(stack);
        }
      };
  public final ItemStackHandler stationInventory =
      new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
          setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return switch (slot) {
            case 0 -> stack.is(OrbitalRegistry.HATCH.get().asItem());
            case 1 ->
                stack.is(OrbitalRegistry.STATION_CHIP.get())
                    && !stack.has(ModComponents.STATION_LINK);
            default -> false;
          };
        }
      };
  public final MachineEnergy energy = MachineEnergy.consumer(500000, this::setChanged);
  public final FluidTank tank =
      new FluidTank(tankCapacity(), this::validFuel) {
        @Override
        protected void onContentsChanged() {
          setChanged();
        }
      };

  public RocketBlockEntity(BlockPos pos, BlockState state) {
    super(RocketRegistry.BLOCK_ENTITY.get(), pos, state);
  }

  public static int tankCapacity() {
    return Math.max(1, (int) Math.round(1000 * AdvancedRocketryConfig.fuelCapacityMultiplier()));
  }

  public RocketPartBlock.Kind kind() {
    return ((RocketPartBlock) getBlockState().getBlock()).kind;
  }

  private boolean validFuel(FluidStack stack) {
    if (!(getBlockState().getBlock() instanceof RocketPartBlock block)) return false;
    return RocketFuelRegistry.allowed(block.fuel, stack);
  }

  public void cycleDestination() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    destination = GuidanceComputer.nextDestination(serverLevel, destination);
    if (destination != GalaxyData.SPACE_ID) stationId = 0;
    setChanged();
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new RocketMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("destination", destination);
    tag.putLong("station_id", stationId);
    tag.putString("status", Component.Serializer.toJson(status, registries));
    tag.putBoolean("ready", ready);
    tag.putInt("mass", mass);
    tag.putInt("thrust", thrust);
    tag.putInt("fuel", fuel);
    tag.putInt("scanned_volume", scannedVolume);
    tag.putInt("build_remaining", buildRemaining);
    tag.putInt("build_total", buildTotal);
    tag.putInt("energy", energy.getEnergyStored());
    tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
    tag.put("station_inventory", stationInventory.serializeNBT(registries));
    tag.put("station_landing_pads", stationLandingPads.copy());
    tag.put("guidance_inventory", guidanceInventory.serializeNBT(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    destination = tag.contains("destination") ? tag.getInt("destination") : 1;
    stationId = tag.getLong("station_id");
    status = ComponentText.read(tag.getString("status"), registries);
    ready = tag.getBoolean("ready");
    mass = tag.getInt("mass");
    thrust = tag.getInt("thrust");
    fuel = tag.getInt("fuel");
    scannedVolume = tag.getInt("scanned_volume");
    buildRemaining = Math.max(0, tag.getInt("build_remaining"));
    buildTotal = Math.max(buildRemaining, tag.getInt("build_total"));
    energy.setEnergy(tag.getInt("energy"));
    tank.readFromNBT(registries, tag.getCompound("tank"));
    if (tag.contains("station_inventory"))
      stationInventory.deserializeNBT(registries, tag.getCompound("station_inventory"));
    stationLandingPads = tag.getCompound("station_landing_pads").copy();
    if (tag.contains("guidance_inventory"))
      guidanceInventory.deserializeNBT(registries, tag.getCompound("guidance_inventory"));
  }
}

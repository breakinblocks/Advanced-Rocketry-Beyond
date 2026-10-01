// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.processing.PortBlockEntity;
import advRocketry.util.ComponentText;
import advRocketry.util.MachineEnergy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Original satellite builder and launch hatch roles, backed by native inventory and FE storage. */
public final class OrbitalBlockEntity extends BlockEntity implements MenuProvider {
  public final OrbitalInventory inventory = new OrbitalInventory(this);
  public final MachineEnergy energy =
      new MachineEnergy(100000, 100000, 100000, this::setChanged) {
        @Override
        public boolean canReceive() {
          return !producesEnergy();
        }

        @Override
        public boolean canExtract() {
          return producesEnergy();
        }
      };
  public Component status = Component.translatable("status.adv_rocketry.orbital.ready");
  public int builderProgress;
  public int warpDestination;
  public int holoTarget = -1;
  public int holoSelectedStar = -1;
  final List<HologramBodyEntity> holograms = new ArrayList<>();
  public int holoCenter = -1;
  public int holoStar;
  public int holoSize = 20;
  public int holoRedstone;
  public int powerMadeLastTick;
  public long blackHoleBurnUntil;
  public int observationTicks;
  public int observatoryOpenProgress;
  public int asteroidCandidate = -1;
  public int[] asteroidOptions = new int[0];
  public int asteroidOptionIndex = -1;
  public int asteroidCatalogHash;
  public long asteroidSeed;
  public String elevatorDimension = "";
  public long elevatorPosition;
  public int areaGravity = 15;
  public int areaRadius = 15;
  public int areaDirection = Direction.DOWN.ordinal();
  public float areaCurrentGravity;
  float areaSyncedGravity = Float.NaN;
  public int laserX;
  public int laserZ;
  public int laserMode;
  public int laserTicks;
  public int laserDepth;
  public UUID laserNodeId;
  public LaserNodeEntity laserNode;
  public int laserTargetPlanet = Integer.MIN_VALUE;
  public boolean laserTerrain;
  public boolean laserRunning;
  public boolean laserJammed;
  public boolean beaconEnabled = true;
  public String dockingId = "";
  public String dockingTarget = "";
  public int railgunMinimum = 1;
  public int railgunRedstone;
  public int railgunTicks;
  public int researchChannels;
  public int[] researchProgress = new int[3];
  public int discoveryTicks;
  public int terraformingTicks;
  public int terraformingDirection = 1;
  public boolean terraformingEnabled;
  public boolean terraformerRunning;
  public int fieldLength;
  public boolean formed;
  private int lastStationComparatorSignal = -1;
  TerraformerLogic.Ports terraformerPorts;

  public OrbitalBlockEntity(BlockPos pos, BlockState state) {
    super(OrbitalRegistry.BLOCK_ENTITY.get(), pos, state);
    laserTerrain = AdvancedRocketryConfig.laserDrillPlanet();
  }

  public OrbitalBlock.Kind kind() {
    return ((OrbitalBlock) getBlockState().getBlock()).kind;
  }

  public boolean producesEnergy() {
    return solarArray() || microwaveReceiver() || blackHoleGenerator();
  }

  public int pullFrom(PortBlockEntity port, int maximum) {
    int room = energy.getMaxEnergyStored() - energy.getEnergyStored();
    if (room <= 0) return 0;
    return energy.generate(port.energyStorage.consume(Math.min(room, maximum), false), false);
  }

  public boolean hatch() {
    return kind() == OrbitalBlock.Kind.HATCH;
  }

  public boolean builder() {
    return kind() == OrbitalBlock.Kind.BUILDER;
  }

  public boolean terminal() {
    return kind() == OrbitalBlock.Kind.TERMINAL;
  }

  public boolean microwaveReceiver() {
    return kind() == OrbitalBlock.Kind.MICROWAVE_RECEIVER;
  }

  public boolean solarArray() {
    return kind() == OrbitalBlock.Kind.SOLAR_ARRAY;
  }

  public boolean blackHoleGenerator() {
    return kind() == OrbitalBlock.Kind.BLACK_HOLE_GENERATOR;
  }

  public boolean stationGravityController() {
    return kind() == OrbitalBlock.Kind.STATION_GRAVITY_CONTROLLER;
  }

  public boolean observatory() {
    return kind() == OrbitalBlock.Kind.OBSERVATORY;
  }

  public boolean spaceElevator() {
    return kind() == OrbitalBlock.Kind.SPACE_ELEVATOR;
  }

  public boolean areaGravityController() {
    return kind() == OrbitalBlock.Kind.AREA_GRAVITY_CONTROLLER;
  }

  public boolean orbitalLaser() {
    return kind() == OrbitalBlock.Kind.ORBITAL_LASER;
  }

  public boolean biomeScanner() {
    return kind() == OrbitalBlock.Kind.BIOME_SCANNER;
  }

  public boolean beacon() {
    return kind() == OrbitalBlock.Kind.BEACON;
  }

  public boolean dockingPort() {
    return kind() == OrbitalBlock.Kind.DOCKING_PORT;
  }

  public boolean railgun() {
    return kind() == OrbitalBlock.Kind.RAILGUN;
  }

  public boolean astrobodyProcessor() {
    return kind() == OrbitalBlock.Kind.ASTROBODY_PROCESSOR;
  }

  public boolean atmosphereTerraformer() {
    return kind() == OrbitalBlock.Kind.ATMOSPHERE_TERRAFORMER;
  }

  public void toggleBeacon() {
    if (!beacon()) return;
    beaconEnabled = !beaconEnabled;
    status =
        beaconEnabled
            ? Component.translatable("status.adv_rocketry.orbital.beacon_enabled")
            : Component.translatable("status.adv_rocketry.orbital.beacon_disabled");
    BeaconLogic.tick(this);
    setChanged();
  }

  public int observatoryRange() {
    return ObservatoryStructure.range(this);
  }

  public boolean observatoryStructure() {
    return ObservatoryStructure.complete(this)
        && level.canSeeSky(ObservatoryStructure.skyPosition(this));
  }

  int unitData(int slot, ResearchType type) {
    return DataUnitItem.research(inventory.getStackInSlot(slot)).amount(type);
  }

  int takeUnitData(int slot, ResearchType type, int maximum) {
    ItemStack stack = inventory.getStackInSlot(slot);
    int taken = Math.min(maximum, unitData(slot, type));
    if (taken > 0) {
      StoredResearch research = DataUnitItem.research(stack);
      DataUnitItem.store(stack, research.type(), research.amount() - taken);
      inventory.setStackInSlot(slot, stack);
    }
    return taken;
  }

  public boolean startObservation() {
    return ObservatoryLogic.scan(this);
  }

  public boolean programAsteroidChip() {
    return ObservatoryLogic.program(this);
  }

  public int addGeneratedEnergy(int amount) {
    return energy.generate(amount, false);
  }

  public void tickClient() {
    if (level == null) return;
    if (areaGravityController()
        && areaCurrentGravity > .02f
        && level.getGameTime() % 45 == Math.floorMod(worldPosition.hashCode(), 45))
      level.playLocalSound(
          worldPosition.getX() + .5,
          worldPosition.getY() + .5,
          worldPosition.getZ() + .5,
          OrbitalRegistry.GRAVITY_SOUND.get(),
          SoundSource.BLOCKS,
          .5f,
          1f,
          false);
    if (atmosphereTerraformer()
        && terraformerRunning
        && level.getGameTime() % 140 == Math.floorMod(worldPosition.hashCode(), 140))
      level.playLocalSound(
          worldPosition.getX() + .5,
          worldPosition.getY() + .5,
          worldPosition.getZ() + .5,
          OrbitalRegistry.TERRAFORMER_SOUND.get(),
          SoundSource.BLOCKS,
          .7f,
          1f,
          false);
  }

  public void setTerraformerRunning(boolean running) {
    if (terraformerRunning == running) return;
    terraformerRunning = running;
    setChanged();
    if (level instanceof ServerLevel server)
      server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  private void updateStationComparator() {
    if (level == null || level.isClientSide) return;
    int signal = StationMotionLogic.comparatorSignal(this);
    if (signal == lastStationComparatorSignal) return;
    lastStationComparatorSignal = signal;
    level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
  }

  public void tick() {
    FormedState.tick(this);
    switch (kind()) {
      case HOLOGRAPHIC_SELECTOR -> HolographicSelectorLogic.tick(this);
      case BUILDER -> SatelliteBuilderLogic.tick(this);
      case WARP_CORE -> WarpCoreLogic.refuel(this);
      case BLACK_HOLE_GENERATOR -> BlackHoleGeneratorLogic.tick(this);
      case FORCE_FIELD_PROJECTOR -> {
        if (level != null && level.getGameTime() % 5 == 0) ForceFieldLogic.advance(this);
      }
      case STATION_ORIENTATION_CONTROLLER -> StationMotionLogic.tickOrientation(this);
      case STATION_ALTITUDE_CONTROLLER -> {
        StationMotionLogic.tickAltitude(this);
        updateStationComparator();
      }
      case ATMOSPHERE_TERRAFORMER -> TerraformerLogic.tick(this);
      case WARP_CONTROLLER -> PlanetDiscoveryLogic.tick(this);
      case ASTROBODY_PROCESSOR -> AstrobodyProcessorLogic.tick(this);
      case RAILGUN -> RailgunLogic.tick(this);
      case DOCKING_PORT -> DockingPortLogic.register(this);
      case BEACON -> BeaconLogic.tick(this);
      case SPACE_ELEVATOR -> SpaceElevatorLogic.collectEnergy(this);
      case AREA_GRAVITY_CONTROLLER -> AreaGravityLogic.tick(this);
      case ORBITAL_LASER -> OrbitalLaserLogic.tick(this);
      case OBSERVATORY -> ObservatoryLogic.tick(this);
      case STATION_GRAVITY_CONTROLLER -> {
        StationMotionLogic.tickGravity(this);
        updateStationComparator();
      }
      case SOLAR_ARRAY -> SolarArrayLogic.tick(this);
      case MICROWAVE_RECEIVER -> MicrowaveReceiverLogic.tick(this);
      default -> {}
    }
  }

  public boolean warpCore() {
    return kind() == OrbitalBlock.Kind.WARP_CORE;
  }

  public boolean warpController() {
    return kind() == OrbitalBlock.Kind.WARP_CONTROLLER;
  }

  public boolean holographicSelector() {
    return kind() == OrbitalBlock.Kind.HOLOGRAPHIC_SELECTOR;
  }

  public boolean selectorControls() {
    return holographicSelector() || kind() == OrbitalBlock.Kind.PLANET_SELECTOR;
  }

  public boolean stationAltitudeController() {
    return kind() == OrbitalBlock.Kind.STATION_ALTITUDE_CONTROLLER;
  }

  public boolean stationOrientationController() {
    return kind() == OrbitalBlock.Kind.STATION_ORIENTATION_CONTROLLER;
  }

  public boolean forceFieldProjector() {
    return kind() == OrbitalBlock.Kind.FORCE_FIELD_PROJECTOR;
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new OrbitalMenu(id, inventory, this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("inventory", inventory.serializeNBT(registries));
    tag.put("energy", energy.serializeNBT(registries));
    tag.putString("status", Component.Serializer.toJson(status, registries));
    tag.putInt("builder_progress", builderProgress);
    tag.putInt("warp_destination", warpDestination);
    tag.putInt("holo_target", holoTarget);
    tag.putInt("holo_center", holoCenter);
    tag.putInt("holo_star", holoStar);
    tag.putInt("holo_size", holoSize);
    tag.putInt("holo_redstone", holoRedstone);
    tag.putInt("power_last_tick", powerMadeLastTick);
    tag.putLong("black_hole_burn_until", blackHoleBurnUntil);
    tag.putInt("observation_ticks", observationTicks);
    tag.putInt("observatory_open_progress", observatoryOpenProgress);
    tag.putInt("asteroid_candidate", asteroidCandidate);
    tag.putIntArray("asteroid_options", asteroidOptions);
    tag.putInt("asteroid_option_index", asteroidOptionIndex);
    tag.putInt("asteroid_catalog_hash", asteroidCatalogHash);
    tag.putLong("asteroid_seed", asteroidSeed);
    tag.putString("elevator_dimension", elevatorDimension);
    tag.putLong("elevator_position", elevatorPosition);
    tag.putInt("area_gravity", areaGravity);
    tag.putInt("area_radius", areaRadius);
    tag.putInt("area_direction", areaDirection);
    tag.putFloat("area_current_gravity", areaCurrentGravity);
    tag.putInt("laser_x", laserX);
    tag.putInt("laser_z", laserZ);
    tag.putInt("laser_mode", laserMode);
    tag.putInt("laser_ticks", laserTicks);
    tag.putInt("laser_depth", laserDepth);
    if (laserNodeId != null) tag.putUUID("laser_node_id", laserNodeId);
    tag.putInt("laser_target_planet", laserTargetPlanet);
    tag.putBoolean("laser_terrain", laserTerrain);
    tag.putBoolean("laser_running", laserRunning);
    tag.putBoolean("laser_jammed", laserJammed);
    tag.putBoolean("beacon_enabled", beaconEnabled);
    tag.putString("docking_id", dockingId);
    tag.putString("docking_target", dockingTarget);
    tag.putInt("railgun_minimum", railgunMinimum);
    tag.putInt("railgun_redstone", railgunRedstone);
    tag.putInt("railgun_ticks", railgunTicks);
    tag.putInt("research_channels", researchChannels);
    tag.putIntArray("research_progress", researchProgress);
    tag.putInt("discovery_ticks", discoveryTicks);
    tag.putInt("terraforming_ticks", terraformingTicks);
    tag.putInt("terraforming_direction", terraformingDirection);
    tag.putBoolean("terraforming_enabled", terraformingEnabled);
    tag.putBoolean("terraformer_running", terraformerRunning);
    tag.putInt("field_length", fieldLength);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    CompoundTag savedInventory = tag.getCompound("inventory").copy();
    savedInventory.putInt("Size", 12);
    inventory.deserializeNBT(registries, savedInventory);
    if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
    status = ComponentText.read(tag.getString("status"), registries);
    builderProgress = Math.clamp(tag.getInt("builder_progress"), 0, 100);
    warpDestination = tag.getInt("warp_destination");
    holoTarget = tag.contains("holo_target") ? tag.getInt("holo_target") : -1;
    holoCenter = tag.contains("holo_center") ? tag.getInt("holo_center") : -1;
    holoStar = tag.getInt("holo_star");
    holoSize = tag.contains("holo_size") ? Math.clamp(tag.getInt("holo_size"), 0, 100) : 20;
    holoRedstone = Math.floorMod(tag.getInt("holo_redstone"), 3);
    powerMadeLastTick = tag.getInt("power_last_tick");
    blackHoleBurnUntil = tag.getLong("black_hole_burn_until");
    observationTicks = tag.getInt("observation_ticks");
    observatoryOpenProgress = Math.clamp(tag.getInt("observatory_open_progress"), 0, 100);
    asteroidCandidate = tag.contains("asteroid_candidate") ? tag.getInt("asteroid_candidate") : -1;
    asteroidOptions = tag.getIntArray("asteroid_options");
    asteroidCatalogHash = tag.getInt("asteroid_catalog_hash");
    asteroidOptionIndex =
        tag.contains("asteroid_option_index")
            ? Math.clamp(tag.getInt("asteroid_option_index"), -1, asteroidOptions.length - 1)
            : -1;
    asteroidSeed = tag.getLong("asteroid_seed");
    elevatorDimension = tag.getString("elevator_dimension");
    elevatorPosition = tag.getLong("elevator_position");
    areaGravity = tag.contains("area_gravity") ? tag.getInt("area_gravity") : 15;
    areaRadius = tag.contains("area_radius") ? tag.getInt("area_radius") : 15;
    areaDirection =
        tag.contains("area_direction")
            ? Math.floorMod(tag.getInt("area_direction"), Direction.values().length)
            : Direction.DOWN.ordinal();
    areaCurrentGravity = tag.getFloat("area_current_gravity");
    laserX = tag.getInt("laser_x");
    laserZ = tag.getInt("laser_z");
    laserMode = Math.floorMod(tag.getInt("laser_mode"), 4);
    laserTicks = tag.getInt("laser_ticks");
    laserDepth = tag.getInt("laser_depth");
    laserNodeId = tag.hasUUID("laser_node_id") ? tag.getUUID("laser_node_id") : null;
    laserNode = null;
    laserTargetPlanet =
        tag.contains("laser_target_planet") ? tag.getInt("laser_target_planet") : Integer.MIN_VALUE;
    if (tag.contains("laser_terrain")) laserTerrain = tag.getBoolean("laser_terrain");
    laserRunning = tag.getBoolean("laser_running");
    laserJammed = tag.getBoolean("laser_jammed");
    beaconEnabled = !tag.contains("beacon_enabled") || tag.getBoolean("beacon_enabled");
    dockingId = tag.getString("docking_id");
    dockingTarget = tag.getString("docking_target");
    railgunMinimum =
        tag.contains("railgun_minimum") ? Math.clamp(tag.getInt("railgun_minimum"), 1, 64) : 1;
    railgunRedstone = Math.floorMod(tag.getInt("railgun_redstone"), 3);
    railgunTicks = tag.getInt("railgun_ticks");
    researchChannels = tag.getInt("research_channels") & 7;
    int[] progress = tag.getIntArray("research_progress");
    researchProgress = progress.length == 3 ? progress : new int[3];
    discoveryTicks = Math.max(0, tag.getInt("discovery_ticks"));
    terraformingTicks = Math.max(0, tag.getInt("terraforming_ticks"));
    terraformingDirection = tag.getInt("terraforming_direction") == -1 ? -1 : 1;
    terraformingEnabled = tag.getBoolean("terraforming_enabled");
    terraformerRunning = tag.getBoolean("terraformer_running");
    fieldLength = Math.clamp(tag.getInt("field_length"), 0, 32);
    formed = tag.getBoolean("formed");
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    tag.putBoolean("beacon_enabled", beaconEnabled);
    tag.putFloat("area_current_gravity", areaCurrentGravity);
    tag.putString("elevator_dimension", elevatorDimension);
    tag.putInt("power_last_tick", powerMadeLastTick);
    tag.putInt("observatory_open_progress", observatoryOpenProgress);
    tag.putBoolean("terraformer_running", terraformerRunning);
    tag.putLong("asteroid_seed", asteroidSeed);
    tag.putString("docking_id", dockingId);
    tag.putString("docking_target", dockingTarget);
    tag.putBoolean("formed", formed);
    return tag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}

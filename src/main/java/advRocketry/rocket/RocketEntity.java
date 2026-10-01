// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.orbit.AsteroidChipItem;
import advRocketry.orbit.GasMission;
import advRocketry.orbit.LandingPadLogic;
import advRocketry.orbit.LinkTarget;
import advRocketry.orbit.LinkerItem;
import advRocketry.orbit.MiningMission;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.PlanetIdChipItem;
import advRocketry.orbit.SatelliteLogic;
import advRocketry.orbit.StationLink;
import advRocketry.orbit.StationLogic;
import advRocketry.processing.MachinePorts;
import advRocketry.processing.PortBlock;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.GasGiantGases;
import advRocketry.space.Planet;
import advRocketry.space.PlanetRuntime;
import advRocketry.space.TerrainType;
import advRocketry.util.Texts;
import advRocketry.util.TicketedTransit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/** Persistent native rocket: carries the exact assembled blocks and their saved inventories. */
public final class RocketEntity extends Entity {
  private static final TicketedTransit<RocketEntity> TRANSIT =
      new TicketedTransit<>(
          "advanced_rocketry_flight", rocket -> rocket.flight() != 0 || rocket.asteroidRcs());
  static final EntityDataAccessor<CompoundTag> SHIP =
      SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);
  static final EntityDataAccessor<Integer> FLIGHT =
      SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
  RocketStructure structure = new RocketStructure();
  int propellant;
  int oxidizer;
  ResourceLocation primaryFluid =
      RocketFuelRegistry.defaultFluid(RocketPartBlock.Fuel.MONOPROPELLANT);
  ResourceLocation oxidizerFluid = RocketFuelRegistry.defaultFluid(RocketPartBlock.Fuel.OXIDIZER);
  double primaryFuelCarry;
  double oxidizerFuelCarry;
  private int selectedGas;
  boolean deployable;
  private Direction forwardDirection = Direction.NORTH;
  static final EntityDataAccessor<Boolean> COASTING =
      SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
  double launchX;
  double launchY;
  double launchZ;
  double horizontalSpeed;
  int landingY;
  int transferHeight = AdvancedRocketryConfig.orbitHeight();
  int overridePlanet = Integer.MIN_VALUE;
  double overrideX;
  double overrideZ;
  @Nullable private BlockPos builderPos;
  @Nullable private UUID missionSource;

  public boolean returnedFrom(UUID source) {
    return source.equals(missionSource);
  }

  double verticalSpeed;
  static final EntityDataAccessor<CompoundTag> NAVIGATION =
      SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);
  SpaceNavigation navigation;
  static final EntityDataAccessor<Boolean> ASTEROID_RCS =
      SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
  Vec3 asteroidVelocity = Vec3.ZERO;
  boolean freeLaunch;
  private int forwardInput, turnInput, verticalInput, inputAge;
  final TicketedTransit<RocketEntity>.Tracker transit = TRANSIT.track(this);
  final List<ItemStack> missionCargo = new ArrayList<>();
  private RocketStructure inventoryCellsOf;
  private List<RocketStructure.Cell> inventoryCells = List.of();
  public final IFluidHandler fluids = new RocketFluidHandler();

  public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
    super(type, level);
    noPhysics = true;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(SHIP, new CompoundTag());
    builder.define(FLIGHT, 0);
    builder.define(NAVIGATION, new CompoundTag());
    builder.define(ASTEROID_RCS, false);
    builder.define(COASTING, false);
  }

  <T> void sync(EntityDataAccessor<T> key, T value) {
    entityData.set(key, value);
  }

  <T> T synced(EntityDataAccessor<T> key) {
    return entityData.get(key);
  }

  public RocketStructure structure() {
    return structure;
  }

  public int flight() {
    return entityData.get(FLIGHT);
  }

  @Nullable
  public SpaceNavigation navigation() {
    CompoundTag data = entityData.get(NAVIGATION);
    return level().isClientSide ? data.isEmpty() ? null : SpaceNavigation.load(data) : navigation;
  }

  public void spaceControls(Player pilot, int forward, int turn, int vertical, boolean toggleRcs) {
    if (level().isClientSide || getFirstPassenger() != pilot) return;
    if (flight() == 0
        && level() instanceof ServerLevel level
        && GalaxyData.get(level.getServer()).planet(level).terrain == TerrainType.ASTEROID) {
      if (toggleRcs) entityData.set(ASTEROID_RCS, !asteroidRcs());
      if (!asteroidRcs()) {
        asteroidVelocity = Vec3.ZERO;
        return;
      }
    } else if (flight() != 3 || navigation == null) return;
    forwardInput = Math.clamp(forward, -1, 1);
    turnInput = Math.clamp(turn, -1, 1);
    verticalInput = Math.clamp(vertical, -1, 1);
    inputAge = 0;
    if (toggleRcs && navigation != null) navigation.rcs = !navigation.rcs;
  }

  public boolean asteroidRcs() {
    return entityData.get(ASTEROID_RCS);
  }

  boolean emptyGuidance() {
    return guidanceCell() != null && guidanceChip().isEmpty();
  }

  @Nullable
  RocketStructure.Cell guidanceCell() {
    for (RocketStructure.Cell cell : structure.cells)
      if (cell.state().getBlock() instanceof RocketPartBlock part
          && part.kind == RocketPartBlock.Kind.GUIDANCE) return cell;
    return null;
  }

  static boolean isItemPort(RocketStructure.Cell cell) {
    return cell.state().getBlock() instanceof PortBlock port
        && (port.kind() == MachinePorts.Kind.ITEM_INPUT
            || port.kind() == MachinePorts.Kind.ITEM_OUTPUT);
  }

  List<RocketStructure.Cell> inventoryCells() {
    if (inventoryCellsOf != structure) {
      inventoryCells =
          structure.cells.stream()
              .filter(cell -> PackedInventory.create(cell, registryAccess()) != null)
              .toList();
      inventoryCellsOf = structure;
    }
    return inventoryCells;
  }

  public boolean enginesActive() {
    return !entityData.get(COASTING)
        && (flight() == 1
            || flight() == 2 && getY() < 300 && AdvancedRocketryConfig.automaticRetroRockets());
  }

  public int propellant() {
    return propellant;
  }

  public int monitorFuel() {
    return propellant + oxidizer;
  }

  public int monitorCapacity() {
    return structure.capacity + structure.oxidizerCapacity;
  }

  public int monitorVelocity() {
    return (int) Math.round((getY() - yo) * 100);
  }

  public double relativeHeight() {
    int ground =
        level()
            .getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                blockPosition().getX(),
                blockPosition().getZ());
    return Math.clamp((getY() - ground) / Math.max(1d, transferHeight - ground), 0, 1);
  }

  public static boolean occupiesLandingColumn(
      ServerLevel level, AABB column, @Nullable RocketEntity requester) {
    return TRANSIT
        .active()
        .anyMatch(
            rocket ->
                rocket != requester
                    && !rocket.isRemoved()
                    && rocket.level() == level
                    && rocket.getBoundingBox().intersects(column));
  }

  public BlockPos selectedLandingPad(long stationId) {
    var cell = guidanceCell();
    if (cell == null) return null;
    var pads = cell.data().getCompound("station_landing_pads");
    return pads.contains(Long.toString(stationId))
        ? BlockPos.of(pads.getLong(Long.toString(stationId)))
        : null;
  }

  public boolean selectLandingPad(@Nullable BlockPos pos) {
    if (!(level() instanceof ServerLevel level) || flight() != 0) return false;
    var chip = guidanceChip();
    if (!chip.is(OrbitalRegistry.STATION_CHIP.get())) return false;
    long id = StationLink.read(chip);
    var galaxy = GalaxyData.get(level.getServer());
    var station = galaxy.stations.get(id);
    if (station == null || !station.deployed) return false;
    if (pos != null
        && (!station.landingPads.containsKey(pos.asLong())
            || !LandingPadLogic.available(
                PlanetRuntime.create(level.getServer(), galaxy.planets.get(GalaxyData.SPACE_ID)),
                pos,
                this))) return false;
    var cell = guidanceCell();
    if (cell == null) return false;
    var pads = cell.data().getCompound("station_landing_pads");
    if (pos == null) pads.remove(Long.toString(id));
    else pads.putLong(Long.toString(id), pos.asLong());
    cell.data().put("station_landing_pads", pads);
    return true;
  }

  public void cycleConsoleDestination() {
    if (!(level() instanceof ServerLevel level) || flight() != 0 || !guidanceChip().isEmpty())
      return;
    structure.destination = GuidanceComputer.nextDestination(level, structure.destination);
    if (structure.destination != GalaxyData.SPACE_ID) structure.stationId = 0;
  }

  public int selectedGas() {
    return selectedGas;
  }

  public BlockPos missionOrigin() {
    return deployable && flight() != 0
        ? BlockPos.containing(launchX, launchY, launchZ)
        : blockPosition();
  }

  public int moveCargoItems(ItemStackHandler station, boolean unload) {
    return RocketCargo.moveItems(this, station, unload);
  }

  public boolean cargoComplete(boolean fluid, boolean unload) {
    return RocketCargo.complete(this, fluid, unload);
  }

  public int moveCargoFluid(FluidTank station, boolean unload) {
    return RocketCargo.moveFluid(this, station, unload);
  }

  public ItemStack guidanceChip() {
    var cell = guidanceCell();
    if (cell == null) return ItemStack.EMPTY;
    ItemStackHandler inventory = new ItemStackHandler(1);
    inventory.deserializeNBT(registryAccess(), cell.data().getCompound("guidance_inventory"));
    return inventory.getStackInSlot(0).copy();
  }

  public boolean setGuidanceChip(ItemStack chip) {
    if (flight() != 0 || !chip.isEmpty() && !GuidanceComputer.accepts(chip)) return false;
    var cell = guidanceCell();
    if (cell == null) return false;
    ItemStackHandler inventory = new ItemStackHandler(1);
    inventory.setStackInSlot(0, chip.copyWithCount(chip.isEmpty() ? 0 : 1));
    cell.data().put("guidance_inventory", inventory.serializeNBT(registryAccess()));
    if (PlanetIdChipItem.programmed(chip)) {
      structure.destination = PlanetIdChipItem.planet(chip);
      structure.stationId = 0;
    } else if (chip.is(OrbitalRegistry.STATION_CHIP.get())) {
      structure.destination = GalaxyData.SPACE_ID;
      structure.stationId = StationLink.read(chip);
    }
    if (level() instanceof ServerLevel server) {
      var route = GuidanceComputer.route(server, chip, blockPosition());
      if (route != null) {
        structure.destination = route.planet();
        structure.stationId = route.stationId();
      }
    }
    return true;
  }

  @Override
  public void onAddedToLevel() {
    super.onAddedToLevel();
    transit.added();
  }

  @Override
  public void onRemovedFromLevel() {
    transit.removed();
    super.onRemovedFromLevel();
  }

  public static void serverStopped(ServerStoppedEvent event) {
    TRANSIT.clear();
  }

  @Nullable
  static RocketEntity loaded(MinecraftServer server, UUID id) {
    return TRANSIT
        .active()
        .filter(
            rocket ->
                !rocket.isRemoved()
                    && rocket.getUUID().equals(id)
                    && rocket.level().getServer() == server)
        .findFirst()
        .orElse(null);
  }

  @Nullable
  static RocketEntity returning(MinecraftServer server, UUID source) {
    return TRANSIT
        .active()
        .filter(
            rocket ->
                !rocket.isRemoved()
                    && rocket.returnedFrom(source)
                    && rocket.level().getServer() == server)
        .findFirst()
        .orElse(null);
  }

  public void initialize(RocketStructure structure, BlockPos origin) {
    this.structure = structure;
    primaryFluid = RocketFuelRegistry.defaultFluid(structure.fuel);
    moveTo(
        origin.getX() + structure.width / 2d,
        origin.getY(),
        origin.getZ() + structure.depth / 2d,
        0,
        0);
    RocketStructure.StoredFuel stored = structure.storedFuel(registryAccess());
    primaryFluid = stored.primaryFluid();
    propellant = Math.clamp(stored.primaryAmount(), 0, Math.max(0, structure.capacity));
    oxidizerFluid = stored.oxidizerFluid();
    oxidizer = Math.clamp(stored.oxidizerAmount(), 0, Math.max(0, structure.oxidizerCapacity));
    entityData.set(SHIP, structure.save(false));
    refreshDimensions();
  }

  public void initializeDeployable(
      RocketStructure structure, BlockPos origin, Direction forwardDirection) {
    initialize(structure, origin);
    deployable = true;
    this.forwardDirection = forwardDirection;
  }

  public void linkBuilder(BlockPos position) {
    builderPos = position.immutable();
  }

  public boolean belongsToBuilder(BlockPos position) {
    return position.equals(builderPos);
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (structure != null && (accessor.equals(FLIGHT) || accessor.equals(ASTEROID_RCS)))
      setBoundingBox(makeBoundingBox());
    if (accessor.equals(SHIP) && level().isClientSide && !entityData.get(SHIP).isEmpty()) {
      structure = RocketStructure.load(entityData.get(SHIP));
      refreshDimensions();
    }
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    if (structure == null) return super.getDimensions(pose);
    return EntityDimensions.scalable(Math.max(structure.width, structure.depth), structure.height);
  }

  @Override
  protected AABB makeBoundingBox() {
    if (structure == null || flight() != 3 && !asteroidRcs()) return super.makeBoundingBox();
    double minX = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    for (double x : new double[] {-structure.width / 2d, structure.width / 2d})
      for (double z : new double[] {0, structure.height}) {
        Vec3 corner = new Vec3(x, 0, z).yRot((float) -Math.toRadians(getYRot()));
        minX = Math.min(minX, corner.x);
        maxX = Math.max(maxX, corner.x);
        minZ = Math.min(minZ, corner.z);
        maxZ = Math.max(maxZ, corner.z);
      }
    return new AABB(
        getX() + minX,
        getY() - structure.depth / 2d,
        getZ() + minZ,
        getX() + maxX,
        getY() + structure.depth / 2d,
        getZ() + maxZ);
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  protected boolean canAddPassenger(Entity passenger) {
    return getPassengers().size() < structure.seats;
  }

  @Override
  protected void positionRider(Entity passenger, MoveFunction callback) {
    int index = getPassengers().indexOf(passenger);
    List<RocketStructure.Cell> seats =
        structure.cells.stream()
            .filter(
                cell ->
                    cell.state().getBlock() instanceof RocketPartBlock part
                        && part.kind == RocketPartBlock.Kind.SEAT)
            .toList();
    if (index >= 0 && index < seats.size()) {
      BlockPos seat = seats.get(index).position();
      if (flight() == 3 || asteroidRcs()) {
        Vec3 offset =
            new Vec3(
                    -structure.width / 2d + seat.getX() + .5,
                    seat.getY() + .5,
                    -structure.depth / 2d + seat.getZ() + .5)
                .xRot((float) -Math.PI / 2)
                .yRot((float) -Math.toRadians(getYRot()));
        callback.accept(passenger, getX() + offset.x, getY() + offset.y, getZ() + offset.z);
        return;
      }
      callback.accept(
          passenger,
          getX() - structure.width / 2d + seat.getX() + .5,
          getY() + seat.getY() + .5,
          getZ() - structure.depth / 2d + seat.getZ() + .5);
    }
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    if (player.getItemInHand(hand).is(ProcessingRegistry.PART_ITEMS.get("atm_analyzer").get())
        && structure.intakePower > 0) {
      if (level() instanceof ServerLevel serverLevel && GasMission.possible(serverLevel, this)) {
        Planet giant = GasMission.giant(serverLevel, this);
        if (giant != null) {
          var gases = GasGiantGases.harvestable(giant);
          selectedGas = (selectedGas + 1) % gases.size();
          player.displayClientMessage(
              Texts.translate("message.adv_rocketry.rocket.collecting", gases.get(selectedGas)),
              true);
        }
      }
      return InteractionResult.sidedSuccess(level().isClientSide);
    }
    if (player.getItemInHand(hand).getItem() instanceof LinkerItem) {
      if (!level().isClientSide) {
        ItemStack linker = player.getItemInHand(hand);
        LinkTarget data = LinkerItem.target(linker);
        if (data.position().isPresent()
            && data.position().get().dimension().equals(level().dimension())
            && level().getBlockEntity(data.position().get().pos())
                instanceof RocketInfrastructure port) {
          if (!port.link(this)) {
            player.displayClientMessage(
                Component.translatable(
                    "message.adv_rocketry.rocket.rocket_must_be_landed_and_within"),
                true);
            return InteractionResult.FAIL;
          }
          LinkerItem.setTarget(linker, LinkTarget.EMPTY);
          player.displayClientMessage(
              Component.translatable("message.adv_rocketry.rocket.rocket_infrastructure_linked"),
              true);
          return InteractionResult.SUCCESS;
        }
        LinkerItem.setTarget(linker, data.withRocket(getUUID()));
        player.displayClientMessage(
            Texts.translate("message.adv_rocketry.rocket.selected_rocket", getUUID()), true);
      }
      return InteractionResult.sidedSuccess(level().isClientSide);
    }
    if (level().isClientSide) {
      if (FluidUtil.tryEmptyContainer(
              player.getItemInHand(hand), fluids, Integer.MAX_VALUE, player, false)
          .isSuccess()) return InteractionResult.sidedSuccess(true);
    } else if (FluidUtil.interactWithFluidHandler(player, hand, fluids))
      return InteractionResult.SUCCESS;
    if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
    if (!level().isClientSide) {
      if ((player.isShiftKeyDown() || structure.seats == 0)
          && flight() == 0
          && player instanceof ServerPlayer serverPlayer)
        RocketConsoleMenu.open(serverPlayer, this, 0);
      else if (!player.startRiding(this))
        player.displayClientMessage(
            Component.translatable(
                "message.adv_rocketry.rocket.the_rocket_needs_an_available_seat"),
            true);
    }
    return InteractionResult.sidedSuccess(level().isClientSide);
  }

  public boolean launch(Player pilot) {
    if (getFirstPassenger() != pilot) return false;
    return beginLaunch(pilot);
  }

  public boolean launchFrom(RocketBlockEntity builder) {
    if (builderPos == null
        || !builderPos.equals(builder.getBlockPos())
        || builder.getLevel() != level()
        || !getPassengers().isEmpty()) return false;
    return beginLaunch(null);
  }

  public boolean launchFromMonitor() {
    return getPassengers().isEmpty() && beginLaunch(null);
  }

  private boolean beginLaunch(@Nullable Player pilot) {
    return RocketLaunch.begin(this, pilot);
  }

  @Override
  public void tick() {
    super.tick();
    if (level().isClientSide) {
      if (enginesActive())
        for (var cell : structure.cells)
          if (cell.state().getBlock() instanceof RocketPartBlock part
              && part.kind == RocketPartBlock.Kind.ENGINE) {
            BlockPos engine = cell.position();
            level()
                .addParticle(
                    ParticleTypes.FLAME,
                    getX() - structure.width / 2d + engine.getX() + .5,
                    getY() + engine.getY(),
                    getZ() - structure.depth / 2d + engine.getZ() + .5,
                    0,
                    -.2,
                    0);
          }
      return;
    }
    if (!(level() instanceof ServerLevel serverLevel)) return;
    transit.ticked(serverLevel);
    transit.update();
    if (flight() == 3 && navigation != null) {
      if (++inputAge > 10 || !(getFirstPassenger() instanceof Player))
        forwardInput = turnInput = verticalInput = 0;
      navigation.move(forwardInput, turnInput, verticalInput);
      setYRot(navigation.yaw);
      GalaxyData galaxy = GalaxyData.get(serverLevel.getServer());
      SpaceNavigation.Arrival arrival =
          navigation.navigate(galaxy, serverLevel.getServer().overworld().getGameTime());
      entityData.set(NAVIGATION, navigation.save());
      if (arrival != null) {
        structure.destination = arrival.station() == 0 ? arrival.planet() : GalaxyData.SPACE_ID;
        structure.stationId = arrival.station();
        transfer(serverLevel);
      }
      return;
    }
    if (flight() == 0 && asteroidRcs()) {
      if (GalaxyData.get(serverLevel.getServer()).planet(serverLevel).terrain
          != TerrainType.ASTEROID) {
        entityData.set(ASTEROID_RCS, false);
        asteroidVelocity = Vec3.ZERO;
        return;
      }
      if (++inputAge > 10 || !(getFirstPassenger() instanceof Player))
        forwardInput = turnInput = verticalInput = 0;
      setYRot((getYRot() + turnInput * 5) % 360);
      double angle = Math.toRadians(getYRot());
      asteroidVelocity =
          asteroidVelocity
              .add(
                  -Math.sin(angle) * forwardInput * .02,
                  verticalInput * .02,
                  Math.cos(angle) * forwardInput * .02)
              .scale(.9);
      noPhysics = false;
      move(MoverType.SELF, asteroidVelocity);
      noPhysics = true;
      if (horizontalCollision) asteroidVelocity = new Vec3(0, asteroidVelocity.y, 0);
      if (verticalCollision) asteroidVelocity = new Vec3(asteroidVelocity.x, 0, asteroidVelocity.z);
      return;
    }
    if (flight() == 1) {
      if (deployable) {
        tickDeployable(serverLevel);
        return;
      }
      if (AdvancedRocketryConfig.rocketsRequireFuel() && !burnFuel(structure.consumption)) {
        landingY = findLandingHeight(serverLevel);
        verticalSpeed = 0;
        entityData.set(FLIGHT, 2);
        return;
      }
      float gravity = GalaxyData.get(serverLevel.getServer()).planet(serverLevel).gravity;
      verticalSpeed += structure.acceleration(gravity);
      setPos(getX(), getY() + verticalSpeed, getZ());
      if (getY() >= transferHeight) {
        if (guidanceChip().is(OrbitalRegistry.SATELLITE_CHIP.get())) {
          SatelliteLogic.recoverCargo(serverLevel, structure, guidanceChip());
          abortTransfer(serverLevel);
          return;
        }
        if (freeLaunch) {
          enterSpaceFlight(serverLevel);
          return;
        }
        if (structure.intakePower > 0 && GasMission.begin(serverLevel, this)) {
          discard();
          return;
        }
        ItemStack chip = miningChip();
        if (chip != null && structure.drillingPower > 0 && structure.seats == 0) {
          if (MiningMission.begin(serverLevel, this, chip)) {
            discard();
            return;
          }
        }
        SatelliteLogic.deployCargo(serverLevel, structure, blockPosition());
        StationLogic.deployCargo(serverLevel, structure, blockPosition());
        transfer(serverLevel);
      }
    } else if (flight() == 2) {
      if (deployable) {
        tickDeployableReturn();
        return;
      }
      verticalSpeed = Math.max(-1, verticalSpeed - .02);
      double next = Math.max(landingY, getY() + verticalSpeed);
      setPos(getX(), next, getZ());
      if (next <= landingY) {
        if (AdvancedRocketryConfig.launchBlockDestruction())
          RocketScorch.scorch(serverLevel, blockPosition().below(), structure.thrust);
        LandingFloatLogic.placeIfNeeded(
            serverLevel, getX(), getZ(), landingY, structure.width, structure.depth);
        verticalSpeed = 0;
        entityData.set(FLIGHT, 0);
      }
    }
  }

  void tickDeployable(ServerLevel level) {
    double clearance = 4d * structure.height;
    double dropped = launchY - getY();
    entityData.set(COASTING, dropped < clearance);
    if (dropped < clearance) {
      double speed =
          .01 * (2.1 * structure.height - Math.abs(2d * structure.height - dropped) + .05);
      setPos(getX(), Math.max(launchY - clearance, getY() - speed), getZ());
      return;
    }
    if (AdvancedRocketryConfig.rocketsRequireFuel() && !burnFuel(structure.consumption)) {
      entityData.set(FLIGHT, 2);
      horizontalSpeed = 0;
      return;
    }
    horizontalSpeed += .01;
    setPos(
        getX() + forwardDirection.getStepX() * horizontalSpeed,
        getY(),
        getZ() + forwardDirection.getStepZ() * horizontalSpeed);
    if (position().distanceToSqr(new Vec3(launchX, launchY, launchZ)) > 128d * 128d) {
      if (GasMission.begin(level, this)) discard();
      else entityData.set(FLIGHT, 2);
    }
  }

  private void tickDeployableReturn() {
    double dx = launchX - getX(), dz = launchZ - getZ();
    double distance = Math.hypot(dx, dz);
    entityData.set(COASTING, distance <= .01);
    if (distance > .01) {
      double step = Math.min(distance, distance * .01 + .01);
      setPos(getX() + dx / distance * step, getY(), getZ() + dz / distance * step);
    } else {
      double y = Math.min(launchY, getY() + .075);
      setPos(launchX, y, launchZ);
      if (y >= launchY) {
        horizontalSpeed = 0;
        verticalSpeed = 0;
        entityData.set(FLIGHT, 0);
      }
    }
  }

  public static void tickUnscheduled(MinecraftServer server) {
    TRANSIT.tickUnscheduled(server);
  }

  boolean enoughFuel(int fuelPoints) {
    return RocketFuelRegistry.availablePoints(
                    propellant, primaryFuelCarry, structure.fuel, primaryFluid)
                + 1e-9
            >= fuelPoints
        && (structure.fuel != RocketPartBlock.Fuel.BIPROPELLANT
            || RocketFuelRegistry.availablePoints(
                        oxidizer, oxidizerFuelCarry, RocketPartBlock.Fuel.OXIDIZER, oxidizerFluid)
                    + 1e-9
                >= fuelPoints);
  }

  private boolean burnFuel(int fuelPoints) {
    if (!enoughFuel(fuelPoints)) return false;
    double primaryCost =
        RocketFuelRegistry.unroundedCost(
            fuelPoints, primaryFuelCarry, structure.fuel, primaryFluid);
    int primaryDrain = (int) Math.floor(primaryCost + 1e-9);
    propellant -= primaryDrain;
    primaryFuelCarry = Math.max(0, primaryCost - primaryDrain);
    if (structure.fuel == RocketPartBlock.Fuel.BIPROPELLANT) {
      double secondaryCost =
          RocketFuelRegistry.unroundedCost(
              fuelPoints, oxidizerFuelCarry, RocketPartBlock.Fuel.OXIDIZER, oxidizerFluid);
      int secondaryDrain = (int) Math.floor(secondaryCost + 1e-9);
      oxidizer -= secondaryDrain;
      oxidizerFuelCarry = Math.max(0, secondaryCost - secondaryDrain);
    }
    return true;
  }

  ItemStack miningChip() {
    ItemStack chip = guidanceChip();
    return AsteroidChipItem.programmed(chip) ? chip : null;
  }

  private void writeState(CompoundTag tag) {
    tag.put("ship", structure.save(true));
    tag.putInt("propellant", propellant);
    tag.putInt("oxidizer", oxidizer);
    tag.putString("primary_fluid", primaryFluid.toString());
    tag.putString("oxidizer_fluid", oxidizerFluid.toString());
    tag.putDouble("primary_fuel_carry", primaryFuelCarry);
    tag.putDouble("oxidizer_fuel_carry", oxidizerFuelCarry);
    tag.putInt("selected_gas", selectedGas);
    tag.putBoolean("deployable", deployable);
    tag.putString("forward_direction", forwardDirection.getName());
    tag.putDouble("launch_x", launchX);
    tag.putDouble("launch_y", launchY);
    tag.putDouble("launch_z", launchZ);
    if (builderPos != null) tag.putLong("builder_pos", builderPos.asLong());
    ListTag cargo = new ListTag();
    for (ItemStack stack : missionCargo) cargo.add(stack.save(registryAccess()));
    tag.put("mission_cargo", cargo);
  }

  private void readState(CompoundTag tag) {
    structure = RocketStructure.load(tag.getCompound("ship"));
    propellant = tag.getInt("propellant");
    oxidizer = tag.getInt("oxidizer");
    primaryFluid = savedFluid(tag, "primary_fluid", structure.fuel);
    oxidizerFluid = savedFluid(tag, "oxidizer_fluid", RocketPartBlock.Fuel.OXIDIZER);
    primaryFuelCarry = Math.clamp(tag.getDouble("primary_fuel_carry"), 0, 1);
    oxidizerFuelCarry = Math.clamp(tag.getDouble("oxidizer_fuel_carry"), 0, 1);
    selectedGas = tag.getInt("selected_gas");
    deployable = tag.getBoolean("deployable");
    Direction direction = Direction.byName(tag.getString("forward_direction"));
    forwardDirection =
        direction != null && direction.getAxis().isHorizontal() ? direction : Direction.NORTH;
    launchX = tag.getDouble("launch_x");
    launchY = tag.contains("launch_y") ? tag.getDouble("launch_y") : getY();
    launchZ = tag.getDouble("launch_z");
    builderPos = tag.contains("builder_pos") ? BlockPos.of(tag.getLong("builder_pos")) : null;
    missionCargo.clear();
    for (Tag entry : tag.getList("mission_cargo", Tag.TAG_COMPOUND))
      ItemStack.parse(registryAccess(), entry).ifPresent(missionCargo::add);
  }

  public CompoundTag miningSnapshot() {
    CompoundTag tag = new CompoundTag();
    tag.putUUID("source_rocket", getUUID());
    writeState(tag);
    tag.putDouble("y", getY());
    tag.putDouble("x", getX());
    tag.putDouble("z", getZ());
    return tag;
  }

  public static RocketEntity returnFromMission(
      ServerLevel level, CompoundTag mission, List<ItemStack> harvest) {
    RocketEntity rocket = RocketRegistry.ROCKET.get().create(level);
    if (rocket == null) throw new IllegalStateException("Rocket entity registration failed");
    rocket.missionSource =
        mission.hasUUID("source_rocket") ? mission.getUUID("source_rocket") : null;
    rocket.readState(mission);
    rocket.missionCargo.addAll(harvest);
    double x = mission.getDouble("x"), z = mission.getDouble("z");
    if (rocket.deployable && mission.contains("launch_y")) {
      rocket.entityData.set(FLIGHT, 2);
      rocket.entityData.set(SHIP, rocket.structure.save(false));
      rocket.setPos(
          rocket.launchX + rocket.forwardDirection.getStepX() * 64d,
          mission.getDouble("y"),
          rocket.launchZ + rocket.forwardDirection.getStepZ() * 64d);
      rocket.refreshDimensions();
      return rocket;
    }
    // Older mission snapshots lack a berth; retain their vertical landing route.
    rocket.deployable = false;
    rocket.landingY = rocket.findLandingHeight(level, x, z);
    rocket.verticalSpeed = -1;
    rocket.entityData.set(FLIGHT, 2);
    rocket.entityData.set(SHIP, rocket.structure.save(false));
    int returnHeight =
        GalaxyData.get(level.getServer()).planet(level).id == GalaxyData.SPACE_ID ? 100 : 200;
    rocket.setPos(
        x,
        Math.min(
            level.getMaxBuildHeight() - rocket.structure.height - 1,
            rocket.landingY + returnHeight),
        z);
    rocket.refreshDimensions();
    return rocket;
  }

  int findLandingHeight(ServerLevel level) {
    return findLandingHeight(level, getX(), getZ());
  }

  int findLandingHeight(ServerLevel level, double centerX, double centerZ) {
    int top = level.getMinBuildHeight() + 1;
    BlockPos origin =
        BlockPos.containing(centerX - structure.width / 2d, 0, centerZ - structure.depth / 2d);
    for (int x = 0; x < structure.width; x++)
      for (int z = 0; z < structure.depth; z++)
        top =
            Math.max(
                top,
                level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    origin.getX() + x,
                    origin.getZ() + z));
    return top;
  }

  private void enterSpaceFlight(ServerLevel level) {
    RocketTransfer.enterSpaceFlight(this, level);
  }

  private void transfer(ServerLevel level) {
    RocketTransfer.transfer(this, level);
  }

  private void abortTransfer(ServerLevel level) {
    RocketTransfer.abortTransfer(this, level);
  }

  public BlockPos origin() {
    return BlockPos.containing(
        getX() - structure.width / 2d, getY(), getZ() - structure.depth / 2d);
  }

  void disassemble(@Nullable Player player) {
    RocketDisassembly.disassemble(this, player);
  }

  private static ResourceLocation savedFluid(
      CompoundTag tag, String key, RocketPartBlock.Fuel type) {
    ResourceLocation saved = ResourceLocation.tryParse(tag.getString(key));
    return saved != null && BuiltInRegistries.FLUID.containsKey(saved)
        ? saved
        : RocketFuelRegistry.defaultFluid(type);
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {
    if (missionSource != null) tag.putUUID("mission_source", missionSource);
    writeState(tag);
    tag.putDouble("horizontal_speed", horizontalSpeed);
    tag.putBoolean("coasting", entityData.get(COASTING));
    tag.putInt("flight", flight());
    tag.putBoolean("free_launch", freeLaunch);
    tag.putBoolean("asteroid_rcs", asteroidRcs());
    tag.putDouble("rcs_vx", asteroidVelocity.x);
    tag.putDouble("rcs_vy", asteroidVelocity.y);
    tag.putDouble("rcs_vz", asteroidVelocity.z);
    if (navigation != null) tag.put("space_navigation", navigation.save());
    tag.putDouble("vertical_speed", verticalSpeed);
    tag.putInt("landing_y", landingY);
    tag.putInt("transfer_height", transferHeight);
    tag.putInt("override_planet", overridePlanet);
    tag.putDouble("override_x", overrideX);
    tag.putDouble("override_z", overrideZ);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {
    missionSource = tag.hasUUID("mission_source") ? tag.getUUID("mission_source") : null;
    readState(tag);
    horizontalSpeed = tag.getDouble("horizontal_speed");
    entityData.set(COASTING, tag.getBoolean("coasting"));
    entityData.set(FLIGHT, tag.getInt("flight"));
    freeLaunch = tag.getBoolean("free_launch");
    entityData.set(ASTEROID_RCS, tag.getBoolean("asteroid_rcs"));
    asteroidVelocity =
        new Vec3(tag.getDouble("rcs_vx"), tag.getDouble("rcs_vy"), tag.getDouble("rcs_vz"));
    navigation =
        tag.contains("space_navigation")
            ? SpaceNavigation.load(tag.getCompound("space_navigation"))
            : null;
    entityData.set(NAVIGATION, navigation == null ? new CompoundTag() : navigation.save());
    verticalSpeed = tag.getDouble("vertical_speed");
    landingY = tag.getInt("landing_y");
    transferHeight =
        tag.contains("transfer_height") && tag.getInt("transfer_height") > 0
            ? tag.getInt("transfer_height")
            : AdvancedRocketryConfig.orbitHeight();
    overridePlanet =
        tag.contains("override_planet") ? tag.getInt("override_planet") : Integer.MIN_VALUE;
    overrideX = tag.getDouble("override_x");
    overrideZ = tag.getDouble("override_z");
    entityData.set(SHIP, structure.save(false));
    refreshDimensions();
  }

  private final class RocketFluidHandler implements IFluidHandler {
    @Override
    public int getTanks() {
      return structure.fuel == RocketPartBlock.Fuel.BIPROPELLANT ? 2 : 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
      return new FluidStack(
          BuiltInRegistries.FLUID.get(tank == 0 ? primaryFluid : oxidizerFluid),
          tank == 0 ? propellant : oxidizer);
    }

    @Override
    public int getTankCapacity(int tank) {
      return tank == 0 ? structure.capacity : structure.oxidizerCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
      if (tank < 0 || tank >= getTanks()) return false;
      RocketPartBlock.Fuel type = tank == 0 ? structure.fuel : RocketPartBlock.Fuel.OXIDIZER;
      if (!RocketFuelRegistry.allowed(type, stack)) return false;
      return tank == 0
          ? propellant == 0 || primaryFluid.equals(RocketFuelRegistry.fluidId(stack))
          : oxidizer == 0 || oxidizerFluid.equals(RocketFuelRegistry.fluidId(stack));
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
      if (flight() != 0) return 0;
      for (int tank = 0; tank < getTanks(); tank++)
        if (isFluidValid(tank, stack)) {
          int filled =
              Math.max(
                  0,
                  Math.min(
                      stack.getAmount(),
                      getTankCapacity(tank) - (tank == 0 ? propellant : oxidizer)));

          if (action.execute()) {
            if (tank == 0) {
              if (propellant == 0 && !primaryFluid.equals(RocketFuelRegistry.fluidId(stack)))
                primaryFuelCarry = 0;
              primaryFluid = RocketFuelRegistry.fluidId(stack);
              propellant += filled;
            } else {
              if (oxidizer == 0 && !oxidizerFluid.equals(RocketFuelRegistry.fluidId(stack)))
                oxidizerFuelCarry = 0;
              oxidizerFluid = RocketFuelRegistry.fluidId(stack);
              oxidizer += filled;
            }
          }
          return filled;
        }
      return 0;
    }

    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
      return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int amount, FluidAction action) {
      return FluidStack.EMPTY;
    }
  }
}

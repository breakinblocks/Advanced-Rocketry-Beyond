package advRocketry.multiblock;

import advRocketry.orbit.OrbitalBlock;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.WarpCoreLogic;
import advRocketry.processing.MultiblockMachine;
import advRocketry.processing.PortBlock;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Resolves the physical blocks beneath a formed machine on both logical sides. */
public final class FormedMachineParts {
    private static final Map<Level, Index> LEVELS = Collections.synchronizedMap(new WeakHashMap<>());

    private record Part(BlockPos controller, Multiblock.Placed cell) {}

    private record Footprint(Multiblock definition, Direction facing, Map<BlockPos, Part> parts) {}

    private static final class Index {
        private final Map<BlockPos, Footprint> machines = new HashMap<>();
        private final Map<BlockPos, Map<BlockPos, Part>> owners = new HashMap<>();
    }

    private FormedMachineParts() {}

    private static boolean formed(BlockState state) {
        if (state.getBlock() instanceof OrbitalBlock) return state.getValue(OrbitalBlock.FORMED);
        return state.getBlock() instanceof ProcessingBlock && state.getValue(ProcessingBlock.FORMED);
    }

    public static void update(BlockEntity entity) {
        Level level = entity.getLevel();
        if (level == null) return;
        BlockPos origin = entity.getBlockPos();
        BlockState state = entity.getBlockState();
        if (!formed(state) || entity.isRemoved()) {
            remove(entity);
            return;
        }
        Multiblock definition;
        if (entity instanceof MultiblockMachine machine) {
            PlacedMultiblock structure = machine.structure();
            definition = structure == null ? null : structure.multiblock();
        } else {
            var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            definition = Multiblocks.get(level.registryAccess(), id);
            if (definition == null || definition.controller() != state.getBlock()) {
                var override = Multiblocks.forController(level.registryAccess(), state.getBlock());
                definition = override == null ? null : Multiblocks.get(level.registryAccess(), override);
            }
        }
        Direction facing =
                state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH);
        Index index = LEVELS.computeIfAbsent(level, ignored -> new Index());
        Footprint previous = index.machines.get(origin);
        if (previous != null && previous.definition == definition && previous.facing == facing) return;
        remove(index, origin);
        if (definition == null) return;
        Map<BlockPos, Part> parts = new HashMap<>();
        for (Multiblock.Placed cell : definition.place(origin, facing)) {
            if (cell.pos().equals(origin)) continue;
            Part part = new Part(origin, cell);
            parts.put(cell.pos(), part);
            index.owners
                    .computeIfAbsent(cell.pos(), ignored -> new LinkedHashMap<>())
                    .put(origin, part);
        }
        index.machines.put(origin, new Footprint(definition, facing, parts));
    }

    public static void remove(BlockEntity entity) {
        Level level = entity.getLevel();
        Index index = level == null ? null : LEVELS.get(level);
        if (index != null) remove(index, entity.getBlockPos());
    }

    private static void remove(Index index, BlockPos origin) {
        Footprint previous = index.machines.remove(origin);
        if (previous == null) return;
        for (BlockPos pos : previous.parts.keySet()) {
            Map<BlockPos, Part> owners = index.owners.get(pos);
            owners.remove(origin);
            if (owners.isEmpty()) index.owners.remove(pos);
        }
    }

    public static BlockEntity controllerAt(Level level, BlockPos pos) {
        Part part = partAt(level, pos);
        return part == null ? null : level.getBlockEntity(part.controller);
    }

    private static Part partAt(Level level, BlockPos pos) {
        Index index = LEVELS.get(level);
        if (index == null || !level.isLoaded(pos)) return null;
        Map<BlockPos, Part> owners = index.owners.get(pos);
        if (owners == null) return null;
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof OrbitalBlock || state.getBlock() instanceof ProcessingBlock)
            return null;
        for (Part part : owners.values()) {
            if (level.isLoaded(part.controller)
                    && formed(level.getBlockState(part.controller))
                    && part.cell.key().test(state)) return part;
        }
        return null;
    }

    public static boolean withinMenuRange(Player player, BlockPos controller) {
        if (player.distanceToSqr(controller.getCenter()) <= 64) return true;
        Level level = player.level();
        Index index = LEVELS.get(level);
        Footprint footprint = index == null ? null : index.machines.get(controller);
        if (footprint == null || !formed(level.getBlockState(controller))) return false;
        for (Part part : footprint.parts.values()) {
            BlockPos pos = part.cell.pos();
            if (player.distanceToSqr(pos.getCenter()) <= 64
                    && level.isLoaded(pos)
                    && !level.getBlockState(pos).isAir()
                    && part.cell.key().test(level.getBlockState(pos))) return true;
        }
        return false;
    }

    public static VoxelShape warpPartShape(Level level, BlockPos pos) {
        if (controllerAt(level, pos) instanceof OrbitalBlockEntity orbital && orbital.warpCore())
            return WarpCoreLogic.partShape(orbital, pos);
        return null;
    }

    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isSpectator()
                || event.getHand() != InteractionHand.MAIN_HAND
                || player.isSecondaryUseActive()
                || event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PortBlock) return;
        BlockEntity controller = controllerAt(event.getLevel(), event.getPos());
        if (controller == null) return;
        if (player instanceof ServerPlayer serverPlayer) {
            if (controller instanceof ProcessingBlockEntity machine) ProcessingBlock.open(machine, serverPlayer);
            if (controller instanceof OrbitalBlockEntity machine) OrbitalBlock.open(machine, serverPlayer);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }
}

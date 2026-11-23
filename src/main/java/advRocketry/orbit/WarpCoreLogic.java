// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.processing.PortBlockEntity;
import advRocketry.space.GalaxyData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The original three-layer warp-core frame and its station fuel input. */
public final class WarpCoreLogic {
    public static final int MAX_FUEL = 1000;
    private static final TagKey<Item> DILITHIUM =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/dilithium"));

    private static final Map<BlockPos, VoxelShape> PART_SHAPES = buildPartShapes();

    private WarpCoreLogic() {}

    private static Map<BlockPos, VoxelShape> buildPartShapes() {
        // Centered on the crystal cell, using the formed model's two pedestal tiers
        // and a three-rectangle approximation for each octagonal section.
        VoxelShape model = Shapes.or(
                Shapes.box(-.28, 0, -.28, 1.28, .22, 1.28),
                Shapes.box(-.07, .22, -.07, 1.07, .42, 1.07),
                Shapes.box(.06, .42, .06, .94, 4.36, .94),
                Shapes.box(-.10, .42, .25, 1.10, 4.36, .75),
                Shapes.box(.25, .42, -.10, .75, 4.36, 1.10),
                Shapes.box(.302, 4.32, .302, .698, 4.46, .698),
                Shapes.box(.22, 4.32, .384, .78, 4.46, .616),
                Shapes.box(.384, 4.32, .22, .616, 4.46, .78));
        Map<BlockPos, VoxelShape> shapes = new HashMap<>();
        for (int y = 0; y <= 4; y++)
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    VoxelShape part = Shapes.join(model.move(-x, -y, -z), Shapes.block(), BooleanOp.AND);
                    if (!part.isEmpty()) shapes.put(new BlockPos(x, y, z), part);
                }
        return Map.copyOf(shapes);
    }

    public static VoxelShape partShape(OrbitalBlockEntity core, BlockPos part) {
        Direction facing = core.getBlockState()
                .getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                .orElse(Direction.NORTH);
        BlockPos crystal = core.getBlockPos().relative(facing.getOpposite());
        return PART_SHAPES.getOrDefault(part.subtract(crystal), Shapes.empty());
    }

    public static boolean complete(OrbitalBlockEntity core) {
        return core.warpCore() && PlacedMultiblock.complete(core, "warp_core");
    }

    public static PortBlockEntity input(OrbitalBlockEntity core) {
        if (!complete(core)) return null;
        return PlacedMultiblock.of(core, "warp_core").entity('I', PortBlockEntity.class);
    }

    public static int dilithiumInInput(OrbitalBlockEntity core) {
        PortBlockEntity input = input(core);
        if (input == null) return 0;
        int count = 0;
        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {
            var stack = input.inventory.getStackInSlot(slot);
            if (stack.is(DILITHIUM)) count += stack.getCount();
        }
        return count;
    }

    public static int stationFuel(OrbitalBlockEntity core) {
        if (!(core.getLevel() instanceof ServerLevel level)) return 0;
        Station station = StationLogic.at(level, core.getBlockPos());
        return station == null ? 0 : station.fuel;
    }

    public static void refuel(OrbitalBlockEntity core) {
        if (!(core.getLevel() instanceof ServerLevel level)) return;
        GalaxyData galaxy = GalaxyData.get(level.getServer());
        Station station = StationLogic.at(galaxy, level, core.getBlockPos());
        if (station == null) return;
        boolean formed = complete(core);
        register(galaxy, station, core.getBlockPos(), formed);
        if (!formed) return;
        PortBlockEntity input = input(core);
        if (input == null) return;
        int points = AdvancedRocketryConfig.fuelPointsPerDilithium();
        if (points <= 0) return;
        int fuel = station.fuel;
        for (int slot = 0; slot < input.inventory.getSlots() && fuel <= MAX_FUEL - points; slot++) {
            while (fuel <= MAX_FUEL - points
                    && input.inventory.getStackInSlot(slot).is(DILITHIUM)) {
                input.inventory.extractItem(slot, 1, false);
                fuel += points;
            }
        }
        int refueled = fuel;
        if (refueled != station.fuel) StationLogic.update(galaxy, station, record -> record.fuel = refueled);
    }

    private static void register(GalaxyData galaxy, Station station, BlockPos position, boolean formed) {
        long[] positions = station.warpCores;
        long packed = position.asLong();
        int index = -1;
        for (int i = 0; i < positions.length; i++)
            if (positions[i] == packed) {
                index = i;
                break;
            }
        if (formed && index < 0) {
            long[] updated = Arrays.copyOf(positions, positions.length + 1);
            updated[positions.length] = packed;
            StationLogic.update(galaxy, station, record -> record.warpCores = updated);
        } else if (!formed && index >= 0) {
            long[] updated = new long[positions.length - 1];
            System.arraycopy(positions, 0, updated, 0, index);
            System.arraycopy(positions, index + 1, updated, index, positions.length - index - 1);
            StationLogic.update(galaxy, station, record -> record.warpCores = updated);
        }
    }

    public static void unregister(OrbitalBlockEntity core) {
        if (!(core.getLevel() instanceof ServerLevel level)) return;
        GalaxyData galaxy = GalaxyData.get(level.getServer());
        Station station = StationLogic.at(galaxy, level, core.getBlockPos());
        if (station != null) register(galaxy, station, core.getBlockPos(), false);
    }
}

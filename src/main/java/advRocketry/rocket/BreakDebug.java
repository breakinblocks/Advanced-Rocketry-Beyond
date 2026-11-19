package advRocketry.rocket;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

public final class BreakDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Pending> PENDING = new ArrayList<>();
    private static BlockEvent.BreakEvent current;
    private static boolean currentCanceled;
    private static EventPriority currentPriority;
    static boolean assemblyCheck;

    private BreakDebug() {}

    private record Pending(ServerLevel level, BlockPos pos, BlockState original, String player, boolean canceled) {}

    public static void register() {
        for (EventPriority priority : EventPriority.values())
            NeoForge.EVENT_BUS.addListener(
                    priority, true, BlockEvent.BreakEvent.class, event -> breakStage(priority, event));
        NeoForge.EVENT_BUS.addListener(
                EventPriority.HIGHEST,
                true,
                PlayerInteractEvent.LeftClickBlock.class,
                event -> leftClick(EventPriority.HIGHEST, event));
        NeoForge.EVENT_BUS.addListener(
                EventPriority.LOWEST,
                true,
                PlayerInteractEvent.LeftClickBlock.class,
                event -> leftClick(EventPriority.LOWEST, event));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, BreakDebug::verify);
    }

    public static boolean enabled() {
        return AdvancedRocketryConfig.debugBlockBreaks();
    }

    static boolean relevant(ServerLevel level, BlockPos pos, BlockState state) {
        return Main.MODID.equals(
                        BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace())
                || !level.getEntitiesOfClass(RocketEntity.class, new AABB(pos).inflate(8))
                        .isEmpty();
    }

    private static String describe(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return player.getGameProfile().getName()
                + " mode=" + player.gameMode.getGameModeForPlayer().getName()
                + " mayBuild=" + player.mayBuild()
                + " mayInteract=" + level.mayInteract(player, pos)
                + " reach=" + player.canInteractWithBlock(pos, 1.0)
                + " dist=" + String.format("%.2f", Math.sqrt(player.distanceToSqr(pos.getCenter())))
                + " onGround=" + player.onGround()
                + " vehicle="
                + (player.getVehicle() == null
                        ? "none"
                        : player.getVehicle().getType().toShortString());
    }

    private static void breakStage(EventPriority priority, BlockEvent.BreakEvent event) {
        if (!enabled()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer player)
                || !relevant(level, event.getPos(), event.getState())) return;
        if (priority == EventPriority.HIGHEST || event != current) {
            current = event;
            currentCanceled = event.isCanceled();
            currentPriority = priority;
            LOGGER.info(
                    "[AR debug] {} start {} {} state={} canceled={} player: {}",
                    assemblyCheck ? "assembly permission check" : "break",
                    level.dimension().location(),
                    event.getPos().toShortString(),
                    event.getState(),
                    event.isCanceled(),
                    describe(player, level, event.getPos()));
        } else if (event.isCanceled() != currentCanceled) {
            currentCanceled = event.isCanceled();
            LOGGER.info(
                    "[AR debug] break {} canceled changed to {} by a listener between priority {} and {}",
                    event.getPos().toShortString(),
                    event.isCanceled(),
                    currentPriority,
                    priority);
        }
        currentPriority = priority;
        if (priority == EventPriority.LOWEST) {
            LOGGER.info("[AR debug] break final {} canceled={}", event.getPos().toShortString(), event.isCanceled());
            if (!assemblyCheck)
                PENDING.add(new Pending(
                        level,
                        event.getPos().immutable(),
                        event.getState(),
                        player.getGameProfile().getName(),
                        event.isCanceled()));
            current = null;
        }
    }

    private static void leftClick(EventPriority priority, PlayerInteractEvent.LeftClickBlock event) {
        if (!enabled()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        BlockState state = level.getBlockState(event.getPos());
        if (!relevant(level, event.getPos(), state)) return;
        LOGGER.info(
                "[AR debug] left click {} {} {} action={} canceled={} useBlock={} useItem={} state={} player: {}",
                priority,
                level.dimension().location(),
                event.getPos().toShortString(),
                event.getAction(),
                event.isCanceled(),
                event.getUseBlock(),
                event.getUseItem(),
                state,
                describe(player, level, event.getPos()));
    }

    private static void verify(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;
        for (Pending pending : List.copyOf(PENDING)) {
            BlockState now = pending.level().getBlockState(pending.pos());
            String outcome =
                    now.isAir() ? "removed" : now.equals(pending.original()) ? "still present" : "replaced with " + now;
            LOGGER.info(
                    "[AR debug] break check {} {} by {} (event canceled={}): block {} one tick later",
                    pending.level().dimension().location(),
                    pending.pos().toShortString(),
                    pending.player(),
                    pending.canceled(),
                    outcome);
        }
        PENDING.clear();
    }

    static void assembled(ServerLevel level, RocketStructure structure, BlockPos origin, ServerPlayer player) {
        if (!enabled()) return;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (var cell : structure.cells) {
            minY = Math.min(minY, origin.getY() + cell.position().getY());
            maxY = Math.max(maxY, origin.getY() + cell.position().getY());
        }
        LOGGER.info(
                "[AR debug] assembled rocket in {} origin={} size={}x{}x{} cells={} y={}..{} player={}",
                level.dimension().location(),
                origin.toShortString(),
                structure.width,
                structure.height,
                structure.depth,
                structure.cells.size(),
                minY,
                maxY,
                player == null ? "none" : player.getGameProfile().getName());
    }
}

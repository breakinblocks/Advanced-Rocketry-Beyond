// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.life.AtmosphereAnalyzerItem;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.life.SealDetectorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class LeakMarker {
    private static final DustParticleOptions GAP = new DustParticleOptions(new Vector3f(1f, .2f, .2f), 1.4f);
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(1f, .55f, .25f), .8f);
    private static final double RANGE = 48;
    private static final int TRAIL_LIMIT = 64;

    private LeakMarker() {}

    public static void tick(OxygenBlockEntity vent, BlockPos gap) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        Level level = vent.getLevel();
        if (player == null || level == null || player.level() != level) return;
        Vec3 center = Vec3.atCenterOf(gap);
        Vec3 source = Vec3.atCenterOf(vent.getBlockPos());
        if (player.distanceToSqr(center) > RANGE * RANGE && player.distanceToSqr(source) > RANGE * RANGE) return;
        if (!shows(client, player, vent)) return;
        long time = level.getGameTime();
        if (time % 4 == 0) {
            double pulse = .35 + .15 * Math.sin(time * .3);
            for (int corner = 0; corner < 8; corner++)
                level.addParticle(
                        GAP,
                        center.x + ((corner & 1) == 0 ? -pulse : pulse),
                        center.y + ((corner & 2) == 0 ? -pulse : pulse),
                        center.z + ((corner & 4) == 0 ? -pulse : pulse),
                        0,
                        0,
                        0);
            level.addParticle(GAP, center.x, center.y, center.z, 0, 0, 0);
        }
        if (time % 10 == 0) {
            Vec3 line = center.subtract(source);
            int steps = Math.min(TRAIL_LIMIT, (int) Math.ceil(line.length()));
            double offset = (time / 10 % 4) / 4d;
            for (int step = 0; step < steps; step++) {
                Vec3 point = source.add(line.scale((step + offset) / Math.max(1, steps)));
                level.addParticle(TRAIL, point.x, point.y, point.z, 0, 0, 0);
            }
        }
    }

    private static boolean shows(Minecraft client, Player player, OxygenBlockEntity vent) {
        if (tool(player.getMainHandItem()) || tool(player.getOffhandItem())) return true;
        if (SuitHud.analyzer(player.getItemBySlot(EquipmentSlot.HEAD))) return true;
        return client.hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(vent.getBlockPos());
    }

    private static boolean tool(ItemStack stack) {
        return stack.getItem() instanceof SealDetectorItem || stack.getItem() instanceof AtmosphereAnalyzerItem;
    }
}

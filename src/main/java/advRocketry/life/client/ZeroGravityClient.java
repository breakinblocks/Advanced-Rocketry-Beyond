package advRocketry.life.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ZeroGravityClient {
    private static final double THRUST = 0.03;
    private static final double MAX_SPEED = 0.4;
    private static final double DRAG = 0.9;

    private ZeroGravityClient() {}

    public static void tick(PlayerTickEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player == null || event.getEntity() != player || player.getAttributeValue(Attributes.GRAVITY) >= 1.0E-5)
            return;
        if (player.getAbilities().flying
                || player.isPassenger()
                || player.isFallFlying()
                || player.isInWater()
                || player.isSpectator()) return;
        boolean up = player.input.jumping;
        boolean down = player.input.shiftKeyDown;
        if (player.onGround() && !up) return;
        var motion = player.getDeltaMovement();
        double thrust = (up ? THRUST : 0) - (down ? THRUST : 0);
        double y = thrust == 0 ? motion.y * DRAG : Mth.clamp(motion.y + thrust, -MAX_SPEED, MAX_SPEED);
        player.setDeltaMovement(motion.x, y, motion.z);
    }
}

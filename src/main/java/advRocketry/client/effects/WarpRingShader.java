package advRocketry.client.effects;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.orbit.WarpCoreLighting;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/** Draws soft, additive halos around the travelling rings without changing world light. */
public final class WarpRingShader extends RenderType {
    private static final int SEGMENTS = 24;
    private static ShaderInstance shader;
    private static final RenderType HALO = create(
            "adv_rocketry_warp_ring",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> shader))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private WarpRingShader() {
        super(
                "adv_rocketry_warp_ring",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                () -> {},
                () -> {});
    }

    public static void register(RegisterShadersEvent event) {
        shader = null;
        try {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(Main.MODID, "warp_ring"),
                            DefaultVertexFormat.POSITION_TEX_COLOR),
                    loaded -> shader = loaded);
        } catch (IOException error) {
            LogUtils.getLogger().warn("Warp ring shader unavailable; retaining the emissive rings", error);
        }
    }

    public static void render(PoseStack pose, MultiBufferSource.BufferSource buffers, double time, boolean moving) {
        if (shader == null || ShaderCompatibility.shaderPackActive() || !AdvancedRocketryConfig.advancedVfx()) return;
        VertexConsumer vertices = buffers.getBuffer(HALO);
        double spill = moving ? WarpCoreLighting.ringPulse(time, 0) : .5;
        int spillColor = (int) (24 + 64 * spill) << 24 | 0x258fd9;
        // The containment socket is a fixed surface on the formed model.
        double socketRadius = .48 + spill * .07;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double a = segment * Math.TAU / SEGMENTS;
            double b = (segment + 1) * Math.TAU / SEGMENTS;
            vertex(pose, vertices, a, 0, .424, .5f, spillColor);
            vertex(pose, vertices, b, socketRadius, .424, 0, spillColor);
            vertex(pose, vertices, a, socketRadius, .424, 0, spillColor);
            vertex(pose, vertices, a, 0, .424, .5f, spillColor);
        }
        for (int ring = 0; ring < WarpCoreLighting.RING_COUNT; ring++) {
            double pulse = moving ? WarpCoreLighting.ringPulse(time, ring) : .5;
            double height = WarpCoreLighting.ringHeight(time, ring);
            double radius = .39 + pulse * .055;
            double spread = .16 + pulse * .21;
            int color = (int) (65 + pulse * 160) << 24 | 0x47bcff;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                double a = segment * Math.TAU / SEGMENTS;
                double b = (segment + 1) * Math.TAU / SEGMENTS;
                vertex(pose, vertices, a, radius, height - spread, 0, color);
                vertex(pose, vertices, a, radius, height + spread, 1, color);
                vertex(pose, vertices, b, radius, height + spread, 1, color);
                vertex(pose, vertices, b, radius, height - spread, 0, color);
            }
        }
        buffers.endBatch(HALO);
    }

    private static void vertex(
            PoseStack pose, VertexConsumer vertices, double angle, double radius, double y, float v, int color) {
        vertices.addVertex(
                        pose.last(), (float) (Math.cos(angle) * radius), (float) y, (float) (Math.sin(angle) * radius))
                .setUv((float) (angle / Math.TAU), v)
                .setColor(color);
    }
}

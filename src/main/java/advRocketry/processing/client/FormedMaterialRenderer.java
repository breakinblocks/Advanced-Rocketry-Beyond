package advRocketry.processing.client;

import advRocketry.Main;
import advRocketry.multiblock.PlacedMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FormedMaterialRenderer {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/formed_machine_trim.obj");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/formed_machine_trim.png");

    private static ObjModel model;

    private FormedMaterialRenderer() {}

    public static void clearModel() {
        model = null;
    }

    public static void slabs(PlacedMultiblock layout, PoseStack pose, MultiBufferSource buffers, int overlay) {
        if (layout != null) FormedDeckRenderer.render(layout, pose, buffers, overlay);
    }

    public static void render(
            BlockEntity entity,
            BlockPos pos,
            String part,
            float yOffset,
            float yScale,
            PoseStack pose,
            MultiBufferSource buffers,
            int overlay) {
        if (model == null) model = new ObjModel(MODEL);
        BlockPos offset = pos.subtract(entity.getBlockPos());
        pose.pushPose();
        pose.translate(offset.getX(), offset.getY() + yOffset, offset.getZ());
        pose.scale(1, yScale, 1);
        model.renderPart(
                part,
                pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                LevelRenderer.getLightColor(entity.getLevel(), pos.above()),
                overlay);
        pose.popPose();
    }
}

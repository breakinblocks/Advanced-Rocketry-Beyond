package advRocketry.life.client;

import advRocketry.Main;
import advRocketry.life.AirlockDoorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class AirlockDoorRenderer implements BlockEntityRenderer<AirlockDoorBlockEntity> {
    private static final ResourceLocation UPPER =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/smallairlockdoor_upper");
    private static final ResourceLocation LOWER =
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/smallairlockdoor_lower");
    private static final float NEAR = 0.5f;
    private static final float FAR = 2.5f;
    private static final float SIDE_MIN = 1;
    private static final float SIDE_MAX = 15;

    public AirlockDoorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            AirlockDoorBlockEntity door,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState state = door.getBlockState();
        if (!(state.getBlock() instanceof DoorBlock)) return;
        float open = door.openness(partialTick);
        if (open >= 1) return;
        boolean upper = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
        boolean towardHigh = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(upper ? UPPER : LOWER);
        float shift = (SIDE_MAX - SIDE_MIN) * open;
        float sideFrom = towardHigh ? SIDE_MIN + shift : SIDE_MIN;
        float sideTo = towardHigh ? SIDE_MAX : SIDE_MAX - shift;
        float texFrom = towardHigh ? SIDE_MIN : SIDE_MIN + shift;
        float texTo = towardHigh ? SIDE_MAX - shift : SIDE_MAX;
        float bottom = upper ? 0 : 3;
        float top = upper ? 13 : 16;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(
                Axis.YP.rotationDegrees(-(state.getValue(DoorBlock.FACING).toYRot() + 90)));
        poseStack.translate(-0.5, 0, -0.5);
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));

        float u0 = sprite.getU(texFrom / 16f);
        float u1 = sprite.getU(texTo / 16f);
        float vTop = sprite.getV((16 - top) / 16f);
        float vBottom = sprite.getV((16 - bottom) / 16f);
        vertex(buffer, pose, NEAR, bottom, sideFrom, u0, vBottom, light, overlay, -1, 0, 0);
        vertex(buffer, pose, NEAR, bottom, sideTo, u1, vBottom, light, overlay, -1, 0, 0);
        vertex(buffer, pose, NEAR, top, sideTo, u1, vTop, light, overlay, -1, 0, 0);
        vertex(buffer, pose, NEAR, top, sideFrom, u0, vTop, light, overlay, -1, 0, 0);
        vertex(buffer, pose, FAR, bottom, sideTo, u1, vBottom, light, overlay, 1, 0, 0);
        vertex(buffer, pose, FAR, bottom, sideFrom, u0, vBottom, light, overlay, 1, 0, 0);
        vertex(buffer, pose, FAR, top, sideFrom, u0, vTop, light, overlay, 1, 0, 0);
        vertex(buffer, pose, FAR, top, sideTo, u1, vTop, light, overlay, 1, 0, 0);

        if (open > 0) {
            float e0 = sprite.getU(1 / 16f);
            float e1 = sprite.getU(2 / 16f);
            if (towardHigh) {
                vertex(buffer, pose, FAR, bottom, sideFrom, e0, vBottom, light, overlay, 0, 0, -1);
                vertex(buffer, pose, NEAR, bottom, sideFrom, e1, vBottom, light, overlay, 0, 0, -1);
                vertex(buffer, pose, NEAR, top, sideFrom, e1, vTop, light, overlay, 0, 0, -1);
                vertex(buffer, pose, FAR, top, sideFrom, e0, vTop, light, overlay, 0, 0, -1);
            } else {
                vertex(buffer, pose, NEAR, bottom, sideTo, e0, vBottom, light, overlay, 0, 0, 1);
                vertex(buffer, pose, FAR, bottom, sideTo, e1, vBottom, light, overlay, 0, 0, 1);
                vertex(buffer, pose, FAR, top, sideTo, e1, vTop, light, overlay, 0, 0, 1);
                vertex(buffer, pose, NEAR, top, sideTo, e0, vTop, light, overlay, 0, 0, 1);
            }
        }
        poseStack.popPose();
    }

    private static void vertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float depth,
            float y,
            float side,
            float u,
            float v,
            int light,
            int overlay,
            float nx,
            float ny,
            float nz) {
        buffer.addVertex(pose, depth, y, side)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}

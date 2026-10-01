package advRocketry.life.client;

import advRocketry.Main;
import advRocketry.life.SpaceSuitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class SpaceHelmetLayer<T extends LivingEntity, M extends HumanoidModel<T>>
    extends RenderLayer<T, M> {
  public static final ModelLayerLocation LAYER =
      new ModelLayerLocation(
          ResourceLocation.fromNamespaceAndPath(Main.MODID, "space_helmet"), "main");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          Main.MODID, "textures/models/armor/spacesuit_helmet.png");
  private final ModelPart shell;
  private final ModelPart visor;

  public SpaceHelmetLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
    super(parent);
    shell = models.bakeLayer(LAYER).getChild("helmet");
    visor = shell.getChild("visor_glass");
  }

  public static LayerDefinition createLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    CubeDeformation none = CubeDeformation.NONE;
    PartDefinition helmet =
        root.addOrReplaceChild(
            "helmet",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-5.5F, 0.0F, -5.5F, 11.0F, 1.0F, 11.0F, none)
                .texOffs(0, 0)
                .addBox(4.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, none)
                .texOffs(18, 0)
                .addBox(-5.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, none)
                .texOffs(36, 0)
                .addBox(-4.0F, -8.0F, 4.0F, 8.0F, 8.0F, 1.0F, none)
                .texOffs(44, 39)
                .addBox(-4.0F, -2.0F, -5.0F, 8.0F, 2.0F, 1.0F, none)
                .texOffs(32, 48)
                .addBox(-4.0F, -8.0F, -5.0F, 8.0F, 1.0F, 1.0F, none)
                .texOffs(0, 39)
                .addBox(-5.0F, -9.0F, -4.0F, 10.0F, 1.0F, 8.0F, none)
                .texOffs(0, 28)
                .addBox(-4.0F, -9.0F, -5.0F, 8.0F, 1.0F, 10.0F, none)
                .texOffs(0, 48)
                .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F, none)
                .texOffs(36, 28)
                .addBox(-3.0F, -11.0F, -3.0F, 6.0F, 1.0F, 6.0F, none)
                .texOffs(54, 0)
                .addBox(5.0F, -6.0F, -1.0F, 1.0F, 3.0F, 3.0F, none)
                .texOffs(36, 39)
                .addBox(-6.0F, -6.0F, -1.0F, 1.0F, 3.0F, 3.0F, none),
            PartPose.ZERO);
    helmet.addOrReplaceChild(
        "visor_glass",
        CubeListBuilder.create()
            .texOffs(44, 16)
            .addBox(-4.0F, -7.0F, -5.5F, 8.0F, 5.0F, 1.0F, none),
        PartPose.ZERO);
    return LayerDefinition.create(mesh, 64, 64);
  }

  public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(LAYER, SpaceHelmetLayer::createLayer);
  }

  public static void register(EntityRenderersEvent.AddLayers event) {
    for (var skin : event.getSkins()) {
      PlayerRenderer renderer = event.getSkin(skin);
      if (renderer != null) addTo(renderer, event.getEntityModels());
    }
    for (EntityType<?> type : event.getEntityTypes()) {
      if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?> renderer
          && renderer.getModel() instanceof HumanoidModel<?>)
        addTo(renderer, event.getEntityModels());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void addTo(LivingEntityRenderer renderer, EntityModelSet models) {
    renderer.addLayer(new SpaceHelmetLayer(renderer, models));
  }

  @Override
  public void render(
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch) {
    var stack = entity.getItemBySlot(EquipmentSlot.HEAD);
    if (!(stack.getItem() instanceof SpaceSuitItem suit) || suit.getType() != ArmorItem.Type.HELMET)
      return;
    pose.pushPose();
    getParentModel().head.translateAndRotate(pose);
    visor.visible = false;
    shell.render(
        pose,
        buffers.getBuffer(RenderType.armorCutoutNoCull(TEXTURE)),
        light,
        OverlayTexture.NO_OVERLAY);
    visor.visible = true;
    visor.render(
        pose,
        buffers.getBuffer(RenderType.entityTranslucentCull(TEXTURE)),
        light,
        OverlayTexture.NO_OVERLAY);
    pose.popPose();
  }
}

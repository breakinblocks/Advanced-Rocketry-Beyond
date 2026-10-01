// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life.client;

import advRocketry.Main;
import advRocketry.life.SpaceSuitItem;
import advRocketry.processing.client.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renders the original MIT jetpack OBJ when installed in a space suit. */
public final class JetpackLayer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
  private static final ResourceLocation MODEL =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "multiblock/jetpack.obj");
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/machine/jetpack.png");
  private static ObjModel model;

  public JetpackLayer(
      RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
    super(parent);
  }

  public static void register(EntityRenderersEvent.AddLayers event) {
    for (var skin : event.getSkins()) {
      PlayerRenderer renderer = event.getSkin(skin);
      if (renderer != null) renderer.addLayer(new JetpackLayer(renderer));
    }
  }

  public static void clearModel() {
    model = null;
  }

  @Override
  public void render(
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      AbstractClientPlayer player,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch) {
    var chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (!(chest.getItem() instanceof SpaceSuitItem suit) || suit.countModule(chest, "jetpack") == 0)
      return;
    if (model == null) model = new ObjModel(MODEL);
    pose.pushPose();
    getParentModel().body.translateAndRotate(pose);
    model.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, 0, -1);
    pose.popPose();
  }
}

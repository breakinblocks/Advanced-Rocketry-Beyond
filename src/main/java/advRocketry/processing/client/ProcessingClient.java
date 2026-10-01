// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.processing.MachinePorts;
import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

public final class ProcessingClient {
  private ProcessingClient() {}

  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    ProcessingRegistry.clientMachineTick = MachineSounds::tick;
    event.registerBlockEntityRenderer(
        ProcessingRegistry.BLOCK_ENTITY.get(), ProcessingRenderer::new);
  }

  public static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(ProcessingRegistry.MENU.get(), MachineScreen::new);
    event.register(MachinePorts.COAL_GENERATOR_MENU.get(), CoalGeneratorScreen::new);
  }

  public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
    ProcessingFluids.DEFINITIONS
        .values()
        .forEach(
            fluid ->
                event.registerFluidType(
                    new IClientFluidTypeExtensions() {
                      @Override
                      public ResourceLocation getStillTexture() {
                        return ResourceLocation.fromNamespaceAndPath(
                            "adv_rocketry",
                            "fluid/"
                                + (fluid.name.equals("enriched_lava") ? "lava" : "oxygen")
                                + "_still");
                      }

                      @Override
                      public ResourceLocation getFlowingTexture() {
                        return ResourceLocation.fromNamespaceAndPath(
                            "adv_rocketry",
                            "fluid/"
                                + (fluid.name.equals("enriched_lava") ? "lava" : "oxygen")
                                + "_flow");
                      }

                      @Override
                      public int getTintColor() {
                        return fluid.color;
                      }
                    },
                    fluid.type.get()));
  }

  public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    ProcessingFluids.DEFINITIONS
        .values()
        .forEach(
            fluid -> event.register(new DynamicFluidContainerModel.Colors(), fluid.bucket.get()));
    ProcessingRegistry.COLORS.forEach(
        (name, color) -> {
          var item = ProcessingRegistry.PART_ITEMS.get(name);
          if (item != null)
            event.register((stack, tint) -> tint == 0 ? 0xff000000 | color : -1, item.get());
        });
  }

  public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
    ProcessingRegistry.COLORS.forEach(
        (name, color) -> {
          var block = ProcessingRegistry.PART_BLOCKS.get(name);
          if (block != null)
            event.register(
                (state, level, pos, tint) -> tint == 0 ? 0xff000000 | color : -1, block.get());
        });
  }

  public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(
        (ResourceManagerReloadListener)
            manager -> {
              ProcessingRenderer.clearModels();
              Minecraft.getInstance().execute(MachineSounds::clear);
            });
  }
}

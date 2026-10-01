// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC.

package advRocketry.compat.guideme;

import advRocketry.Main;
import advRocketry.processing.MachinePorts;
import guideme.Guide;
import guideme.GuideItemSettings;
import guideme.Guides;
import guideme.compiler.TagCompiler;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Loaded only on the client, after the optional GuideME dependency is found. */
public final class RocketryGuide {
  public static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "guide");

  private RocketryGuide() {}

  public static void register(IEventBus bus) {
    Guide.builder(ID)
        .itemSettings(
            new GuideItemSettings(
                Optional.of(Component.translatable("adv_rocketry.guide_name")),
                List.of(Component.translatable("adv_rocketry.guide_tooltip")),
                Optional.empty()))
        .extension(TagCompiler.EXTENSION_POINT, new MachineRecipesTag())
        .build();
    bus.addListener(RocketryGuide::creativeTab);
  }

  private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
    if (event.getTab() == MachinePorts.TAB.get()) event.accept(Guides.createGuideItem(ID));
  }
}

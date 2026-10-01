// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiTheme;
import advRocketry.multiblock.Multiblocks;
import advRocketry.processing.HoloProjectorItem;
import advRocketry.processing.ProjectorBlueprint;
import advRocketry.processing.ProjectorSelection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

/** Original projector machine picker and required-material list in a native screen. */
public final class ProjectorScreen extends Screen {
  private final InteractionHand hand;
  private final Map<ResourceLocation, List<ProjectorBlueprint.Material>> cache = new HashMap<>();
  private final List<ResourceLocation> ids;
  private int selected;
  private int first;
  private int materialFirst;
  private int left;
  private int top;
  private int panelWidth;
  private int panelHeight;
  private int rows;

  private ProjectorScreen(InteractionHand hand, ResourceLocation machine) {
    super(Component.translatable("gui.adv_rocketry.projector.holographic_projector"));
    this.hand = hand;
    ids = Multiblocks.ids(Minecraft.getInstance().level.registryAccess());
    selected = Math.max(0, ids.indexOf(machine));
  }

  public static void open(InteractionHand hand) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null)
      minecraft.setScreen(
          new ProjectorScreen(
              hand, HoloProjectorItem.machine(minecraft.player.getItemInHand(hand))));
  }

  @Override
  protected void init() {
    panelWidth = Math.min(440, width - 12);
    panelHeight = Math.min(280, height - 12);
    left = (width - panelWidth) / 2;
    top = (height - panelHeight) / 2;
    rows = Math.max(1, (panelHeight - 80) / 22);
    first = selected / rows * rows;
    buttons();
  }

  private void buttons() {
    clearWidgets();
    int column = panelWidth / 2;
    for (int row = 0; row < rows && first + row < ids.size(); row++) {
      int index = first + row;
      Component title = Multiblocks.title(ids.get(index));
      var entry =
          addRenderableWidget(
              UiButton.builder(
                      title,
                      button -> {
                        selected = index;
                        materialFirst = 0;
                        StructurePreview.clear();
                        PacketDistributor.sendToServer(
                            new ProjectorSelection(ids.get(index), hand));
                        buttons();
                      })
                  .bounds(left + 8, top + 34 + row * 22, column - 16, 20)
                  .tooltip(Tooltip.create(title))
                  .build());
      entry.active = index != selected;
      ((UiButton) entry).selected(index == selected);
    }
    addRenderableWidget(
                UiButton.builder(
                        Component.literal("<"),
                        button -> {
                          first = Math.max(0, first - rows);
                          buttons();
                        })
                    .bounds(left + 8, top + panelHeight - 32, 24, 20)
                    .build())
            .active =
        first > 0;
    addRenderableWidget(
                UiButton.builder(
                        Component.literal(">"),
                        button -> {
                          first += rows;
                          buttons();
                        })
                    .bounds(left + column - 32, top + panelHeight - 32, 24, 20)
                    .build())
            .active =
        first + rows < ids.size();
    addRenderableWidget(
        UiButton.builder(
                Component.translatable("gui.adv_rocketry.projector.done"), button -> onClose())
            .bounds(left + column + 8, top + panelHeight - 32, column - 16, 20)
            .build());
  }

  private List<ProjectorBlueprint.Material> materials() {
    if (ids.isEmpty()) return List.of();
    ResourceLocation id = ids.get(selected);
    return cache.computeIfAbsent(
        id,
        key -> ProjectorBlueprint.materials(Minecraft.getInstance().level.registryAccess(), key));
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    UiTheme.title(graphics, font, title, left + 8, top + 8, panelWidth - 16);
    int column = panelWidth / 2;
    graphics.drawString(
        font,
        Component.translatable("gui.adv_rocketry.projector.materials"),
        left + column + 8,
        top + 24,
        UiTheme.MUTED,
        false);
    List<ProjectorBlueprint.Material> materials = materials();
    Component hovered = null;
    for (int row = 0; row < rows && row + materialFirst < materials.size(); row++) {
      var material = materials.get(row + materialFirst);
      String names =
          String.join(
              Component.translatable("gui.adv_rocketry.projector.or").getString(),
              material.alternatives().stream().map(block -> block.getName().getString()).toList());
      Component line =
          Component.translatable(
              material.optional()
                  ? "gui.adv_rocketry.projector.material_optional"
                  : "gui.adv_rocketry.projector.material",
              material.count(),
              names);
      int x = left + column + 8;
      int y = top + 38 + row * 22;
      graphics.drawString(font, UiTheme.fit(font, line, column - 16), x, y, UiTheme.TEXT, false);
      if (mouseX >= x && mouseX < left + panelWidth - 8 && mouseY >= y - 4 && mouseY < y + 16)
        hovered = line;
    }
    if (hovered != null)
      graphics.renderTooltip(font, font.split(hovered, Math.min(320, width - 24)), mouseX, mouseY);
  }

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.renderBackground(graphics, mouseX, mouseY, partialTick);
    UiTheme.window(graphics, left, top, panelWidth, panelHeight);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    if (x >= left + panelWidth / 2 && x < left + panelWidth && y >= top && y < top + panelHeight) {
      materialFirst =
          Math.clamp(
              materialFirst + (vertical < 0 ? 1 : -1), 0, Math.max(0, materials().size() - rows));
      return true;
    }
    return super.mouseScrolled(x, y, horizontal, vertical);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}

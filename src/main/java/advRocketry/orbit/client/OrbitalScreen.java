// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit.client;

import advRocketry.client.ui.ModContainerScreen;
import advRocketry.client.ui.UiButton;
import advRocketry.client.ui.UiEditBox;
import advRocketry.client.ui.UiTheme;
import advRocketry.orbit.AsteroidCatalog;
import advRocketry.orbit.BeaconLogic;
import advRocketry.orbit.DockingPortConfig;
import advRocketry.orbit.OrbitalAction;
import advRocketry.orbit.OrbitalMenu;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.orbit.WarpCoreLogic;
import advRocketry.space.GalaxyData;
import advRocketry.space.GalaxySync;
import advRocketry.util.Texts;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public final class OrbitalScreen extends ModContainerScreen<OrbitalMenu> {
  private int scannerPage;
  private int asteroidPreviewScroll;
  private List<ResourceLocation> earthBiomes;
  private SurveyKey surveyKey;
  private List<SurveyRow> surveyRows = List.of();
  private EditBox dockingIdBox;
  private EditBox dockingTargetBox;

  public OrbitalScreen(OrbitalMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = menu.entity.observatory() ? 320 : 176;
    imageHeight =
        menu.entity.warpController()
                || menu.entity.builder()
                || menu.entity.observatory()
                || menu.entity.stationOrientationController()
            ? 245
            : menu.entity.areaGravityController()
                    || menu.entity.orbitalLaser()
                    || menu.entity.biomeScanner()
                    || menu.entity.astrobodyProcessor()
                    || menu.entity.atmosphereTerraformer()
                    || menu.entity.selectorControls()
                ? 220
                : 190;
  }

  @Override
  protected void init() {
    super.init();
    if (menu.entity.warpController()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.next_target"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.NEXT_WARP_TARGET.id()))
              .bounds(leftPos + 8, topPos + 94, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.warp"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.WARP.id()))
              .bounds(leftPos + 96, topPos + 94, 72, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.discover"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.DISCOVER.id()))
              .bounds(leftPos + 8, topPos + 118, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.import_chip"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.IMPORT_PLANET.id()))
              .bounds(leftPos + 96, topPos + 118, 72, 20)
              .build());
    } else if (menu.entity.selectorControls()) {
      String[] labels = {
        "gui.adv_rocketry.orbital.selector.next",
        "gui.adv_rocketry.orbital.selector.target",
        "gui.adv_rocketry.orbital.selector.center",
        "gui.adv_rocketry.orbital.selector.back",
        "gui.adv_rocketry.orbital.selector.star",
        "gui.adv_rocketry.orbital.selector.redstone",
        "gui.adv_rocketry.orbital.selector.size_down",
        "gui.adv_rocketry.orbital.selector.size_up"
      };
      int[] ids = {
        OrbitalAction.SELECTOR_NEXT.id(),
        OrbitalAction.SELECTOR_TARGET.id(),
        OrbitalAction.SELECTOR_CENTER.id(),
        OrbitalAction.SELECTOR_BACK.id(),
        OrbitalAction.SELECTOR_STAR.id(),
        OrbitalAction.SELECTOR_REDSTONE.id(),
        OrbitalAction.SELECTOR_SIZE_DOWN.id(),
        OrbitalAction.SELECTOR_SIZE_UP.id()
      };
      for (int index = 0; index < labels.length; index++) {
        int buttonId = ids[index];
        addRenderableWidget(
            new SelectorButton(
                UiButton.builder(
                        Component.translatable(labels[index]),
                        button ->
                            minecraft.gameMode.handleInventoryButtonClick(
                                menu.containerId, buttonId))
                    .bounds(leftPos + 8 + index % 3 * 54, topPos + 35 + index / 3 * 25, 50, 20)));
      }
    } else if (menu.entity.atmosphereTerraformer()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.increase"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.TERRAFORM_INCREASE.id()))
              .bounds(leftPos + 8, topPos + 75, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.decrease"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.TERRAFORM_DECREASE.id()))
              .bounds(leftPos + 96, topPos + 75, 72, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.pause_resume"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.TERRAFORM_TOGGLE.id()))
              .bounds(leftPos + 42, topPos + 101, 92, 20)
              .build());
    } else if (menu.entity.stationAltitudeController()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("-10"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.ALTITUDE_DOWN.id()))
              .bounds(leftPos + 8, topPos + 72, 48, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("+10"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.ALTITUDE_UP.id()))
              .bounds(leftPos + 60, topPos + 72, 48, 20)
              .build());
    } else if (menu.entity.stationOrientationController()) {
      for (int axis = 0; axis < 3; axis++) {
        int down = OrbitalAction.ROTATION_X_DOWN.id() + axis * 2, up = down + 1;
        int y = topPos + 50 + axis * 24;
        addRenderableWidget(
            UiButton.builder(
                    Component.literal("-10"),
                    button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, down))
                .bounds(leftPos + 75, y, 42, 20)
                .build());
        addRenderableWidget(
            UiButton.builder(
                    Component.literal("+10"),
                    button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, up))
                .bounds(leftPos + 123, y, 42, 20)
                .build());
      }
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.reset"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.ROTATION_RESET.id()))
              .bounds(leftPos + 70, topPos + 125, 92, 20)
              .build());
    } else if (menu.entity.observatory()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.observe"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.OBSERVE.id()))
              .bounds(leftPos + 8, topPos + 80, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.program_chip"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.PROGRAM_ASTEROID.id()))
              .bounds(leftPos + 96, topPos + 80, 72, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.next_target"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.NEXT_ASTEROID.id()))
              .bounds(leftPos + 8, topPos + 105, 76, 20)
              .build());
    } else if (menu.entity.spaceElevator()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.travel"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.TRAVEL.id()))
              .bounds(leftPos + 50, topPos + 70, 76, 20)
              .build());
    } else if (menu.entity.areaGravityController()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("−10%"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.AREA_GRAVITY_DOWN.id()))
              .bounds(leftPos + 8, topPos + 72, 48, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("+10%"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.AREA_GRAVITY_UP.id()))
              .bounds(leftPos + 60, topPos + 72, 48, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.radius_2"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.AREA_RADIUS.id()))
              .bounds(leftPos + 8, topPos + 96, 80, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.direction"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.AREA_DIRECTION.id()))
              .bounds(leftPos + 92, topPos + 96, 76, 20)
              .build());
    } else if (menu.entity.orbitalLaser()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.start_stop"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.LASER_TOGGLE.id()))
              .bounds(leftPos + 8, topPos + 72, 78, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.clear_jam"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.LASER_CLEAR_JAM.id()))
              .bounds(leftPos + 92, topPos + 72, 76, 20)
              .build());
      for (int buttonId = OrbitalAction.LASER_X_DOWN.id();
          buttonId <= OrbitalAction.LASER_Z_UP.id();
          buttonId++) {
        int selected = buttonId;
        String label =
            switch (OrbitalAction.fromId(selected)) {
              case LASER_X_DOWN -> "X-";
              case LASER_X_UP -> "X+";
              case LASER_Z_DOWN -> "Z-";
              default -> "Z+";
            };
        addRenderableWidget(
            UiButton.builder(
                    Component.literal(label),
                    button ->
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selected))
                .bounds(
                    leftPos + 8 + (buttonId - OrbitalAction.LASER_X_DOWN.id()) * 42,
                    topPos + 96,
                    38,
                    20)
                .build());
      }
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.void_terrain"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.LASER_TERRAIN.id()))
              .bounds(leftPos + 8, topPos + 120, 160, 14)
              .build());
    } else if (menu.entity.biomeScanner()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.previous"),
                  button -> scannerPage = Math.max(0, scannerPage - 1))
              .bounds(leftPos + 8, topPos + 108, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.next"), button -> scannerPage++)
              .bounds(leftPos + 92, topPos + 108, 76, 20)
              .build());
    } else if (menu.entity.astrobodyProcessor()) {
      for (int channel = 0; channel < 3; channel++) {
        int selected = channel;
        String label =
            switch (channel) {
              case 0 -> "gui.adv_rocketry.orbital.channel_composition";
              case 1 -> "gui.adv_rocketry.orbital.channel_distance";
              default -> "gui.adv_rocketry.orbital.channel_mass";
            };
        addRenderableWidget(
            UiButton.builder(
                    Component.translatable(label),
                    button ->
                        minecraft.gameMode.handleInventoryButtonClick(
                            menu.containerId, OrbitalAction.RESEARCH_COMPOSITION.id() + selected))
                .bounds(leftPos + 8 + channel * 54, topPos + 82, 52, 20)
                .build());
      }
    } else if (menu.entity.railgun()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.min"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.RAILGUN_MINIMUM_DOWN.id()))
              .bounds(leftPos + 8, topPos + 72, 48, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.min_2"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.RAILGUN_MINIMUM_UP.id()))
              .bounds(leftPos + 60, topPos + 72, 48, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.signal"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.RAILGUN_REDSTONE.id()))
              .bounds(leftPos + 112, topPos + 72, 56, 20)
              .build());
    } else if (menu.entity.dockingPort()) {
      dockingIdBox =
          new UiEditBox(
              font,
              leftPos + 58,
              topPos + 24,
              110,
              18,
              Component.translatable("gui.adv_rocketry.orbital.my_id"));
      dockingIdBox.setMaxLength(32);
      dockingIdBox.setValue(menu.entity.dockingId);
      addRenderableWidget(dockingIdBox);
      dockingTargetBox =
          new UiEditBox(
              font,
              leftPos + 58,
              topPos + 47,
              110,
              18,
              Component.translatable("gui.adv_rocketry.orbital.target_id"));
      dockingTargetBox.setMaxLength(32);
      dockingTargetBox.setValue(menu.entity.dockingTarget);
      addRenderableWidget(dockingTargetBox);
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.save_ids"),
                  button ->
                      PacketDistributor.sendToServer(
                          new DockingPortConfig(
                              menu.entity.getBlockPos(),
                              dockingIdBox.getValue(),
                              dockingTargetBox.getValue())))
              .bounds(leftPos + 8, topPos + 74, 160, 20)
              .build());
    } else if (menu.entity.beacon()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.toggle_beacon"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.BEACON_TOGGLE.id()))
              .bounds(leftPos + 8, topPos + 70, 160, 20)
              .build());
    } else if (menu.entity.stationGravityController()) {
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("−10%"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.GRAVITY_DOWN.id()))
              .bounds(leftPos + 8, topPos + 70, 76, 20)
              .build());
      addRenderableWidget(
          UiButton.builder(
                  Component.literal("+10%"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.GRAVITY_UP.id()))
              .bounds(leftPos + 96, topPos + 70, 72, 20)
              .build());
    } else if (menu.entity.terminal())
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.download"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.TRANSFER_DATA.id()))
              .bounds(leftPos + 100, topPos + 70, 68, 20)
              .build());
    else if (menu.entity.builder())
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.build"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.BUILD.id()))
              .bounds(leftPos + 104, topPos + 70, 62, 20)
              .build());
    if (menu.entity.builder())
      addRenderableWidget(
          UiButton.builder(
                  Component.translatable("gui.adv_rocketry.orbital.copy_chip"),
                  button ->
                      minecraft.gameMode.handleInventoryButtonClick(
                          menu.containerId, OrbitalAction.COPY_CHIP.id()))
              .bounds(leftPos + 8, topPos + 95, 78, 20)
              .build());
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(graphics, partialTick, mouseX, mouseY);

    if (menu.entity.builder())
      UiTheme.progress(graphics, leftPos + 8, topPos + 125, 88, 7, menu.builderProgress() / 100d);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    drawTitle(graphics);
    if (menu.entity.warpController()) {
      var planet = GalaxySync.clientPlanet(menu.warpDestination());
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.target",
              (planet == null ? menu.warpDestination() : planet.name)),
          8,
          26,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.required_artifacts"),
          8,
          58,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.fe_scan_1000", menu.energy(), menu.discoveryTicks()),
          8,
          141,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 153, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.selectorControls()) {
      var planet = GalaxySync.clientPlanet(menu.hologramTarget());
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.selected", (planet == null ? "none" : planet.name)),
          8,
          20,
          UiTheme.ACCENT,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.star_scale_signal",
              menu.hologramStar(),
              menu.hologramSize(),
              menu.hologramRedstone()),
          8,
          113,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 123, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.atmosphereTerraformer()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.biome_changer_remote"),
          8,
          26,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.cycle", menu.terraformingTicks() / 10d),
          8,
          58,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 123, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.stationAltitudeController()) {
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.altitude_km", (menu.stationAltitude() * 200 + 100)),
          8,
          29,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.target_km", (menu.stationTargetAltitude() * 200 + 100)),
          8,
          49,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 93, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.stationOrientationController()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.rotation_per_hour"),
          8,
          26,
          UiTheme.MUTED,
          false);
      for (int axis = 0; axis < 3; axis++)
        drawText(
            graphics,
            Component.literal("XYZ".charAt(axis) + ": " + menu.targetRotation(axis)),
            8,
            56 + axis * 24,
            UiTheme.TEXT,
            false);
      drawText(graphics, playerInventoryTitle, 8, 153, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.stationGravityController()) {
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.current", menu.stationCurrentGravity()),
          8,
          29,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.target_2", menu.stationTargetGravity()),
          8,
          49,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 93, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.observatory()) {
      int candidate = menu.asteroidCandidate();
      List<AsteroidCatalog.Asteroid> asteroids =
          AsteroidCatalog.types(minecraft.level.registryAccess());
      String name =
          candidate >= 0 && candidate < asteroids.size()
              ? asteroids.get(candidate).displayName().getString()
              : Component.translatable("gui.adv_rocketry.orbital.none").getString();
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.chip_dist_comp_mass_out"),
          8,
          27,
          UiTheme.MUTED,
          false);
      drawText(graphics, font.plainSubstrByWidth(name, 160), 8, 64, UiTheme.TEXT, false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.target_3",
              (menu.asteroidOptionIndex() + 1),
              menu.asteroidOptions()),
          90,
          111,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.scan_100_data_chip_500_fe"),
          8,
          131,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 143, UiTheme.TEXT, false);
      renderAsteroidSurvey(graphics, candidate);
      return;
    }
    if (menu.entity.spaceElevator()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.geostationary_tether"),
          8,
          26,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.energy_fe", menu.energy()),
          8,
          48,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 93, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.areaGravityController()) {
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.area_gravity", menu.areaGravity()),
          8,
          26,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.radius",
              menu.areaRadius(),
              Direction.values()[menu.areaDirection()]),
          8,
          48,
          UiTheme.TEXT,
          false);
      drawText(graphics, playerInventoryTitle, 8, 123, UiTheme.TEXT, false);
      return;
    }
    if (menu.entity.orbitalLaser()) {
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.target_4", menu.laserX(), menu.laserZ()),
          8,
          26,
          UiTheme.MUTED,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.energy_fe_2",
              Component.translatable(
                  menu.laserTerrain()
                      ? "gui.adv_rocketry.orbital.laser_terrain"
                      : "gui.adv_rocketry.orbital.laser_void"),
              menu.energy(),
              Component.translatable(
                  menu.laserJammed()
                      ? "gui.adv_rocketry.orbital.laser_jammed"
                      : menu.laserRunning()
                          ? "gui.adv_rocketry.orbital.laser_running"
                          : "gui.adv_rocketry.orbital.laser_idle")),
          8,
          48,
          UiTheme.TEXT,
          false);
      return;
    }
    if (menu.entity.biomeScanner()) {
      int planetId = menu.scannedPlanet();
      var planet = GalaxySync.clientPlanet(planetId);
      if (planet == null) {
        drawText(
            graphics,
            planetId == -1
                ? Component.translatable("gui.adv_rocketry.orbital.station_in_warp_transit")
                : Component.translatable(
                    "gui.adv_rocketry.orbital.scanner_frame_or_station_unavailable"),
            8,
            27,
            UiTheme.TEXT,
            false);
      } else {
        drawText(
            graphics,
            Texts.translate("gui.adv_rocketry.orbital.biomes_below", planet.name),
            8,
            25,
            UiTheme.MUTED,
            false);
        List<ResourceLocation> biomes =
            planet.id == GalaxyData.EARTH_ID ? earthBiomes() : planet.biomes;
        int first = scannerPage * 6;
        for (int index = first; index < Math.min(first + 6, biomes.size()); index++)
          drawText(
              graphics,
              biomes.get(index).toString(),
              8,
              39 + (index - first) * 11,
              UiTheme.TEXT,
              false);
        if (biomes.isEmpty())
          drawText(
              graphics,
              Component.translatable("gui.adv_rocketry.orbital.no_biome_catalog_entries"),
              8,
              39,
              UiTheme.TEXT,
              false);
      }
      return;
    }
    if (menu.entity.astrobodyProcessor()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.asteroid_chip"),
          50,
          31,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.energy_fe_3", menu.energy()),
          8,
          62,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.channels", Integer.toBinaryString(menu.researchChannels())),
          8,
          110,
          UiTheme.MUTED,
          false);
      return;
    }
    if (menu.entity.railgun()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.linker"),
          50,
          27,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.min_signal_fe",
              menu.railgunMinimum(),
              menu.railgunRedstone(),
              menu.energy()),
          50,
          49,
          UiTheme.TEXT,
          false);
      return;
    }
    if (menu.entity.beacon()) {
      drawText(
          graphics,
          Texts.translate(
              "gui.adv_rocketry.orbital.beacon_state",
              Component.translatable(
                  menu.beaconEnabled()
                      ? "gui.adv_rocketry.orbital.beacon_enabled"
                      : "gui.adv_rocketry.orbital.beacon_disabled"),
              Component.translatable(
                  BeaconLogic.complete(menu.entity)
                      ? "gui.adv_rocketry.orbital.frame_ready"
                      : "gui.adv_rocketry.orbital.frame_incomplete")),
          8,
          40,
          UiTheme.TEXT,
          false);
      return;
    }
    if (menu.entity.dockingPort()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.my_id"),
          8,
          29,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.target_5"),
          8,
          52,
          UiTheme.TEXT,
          false);
      return;
    }
    drawText(
        graphics,
        menu.entity.hatch()
            ? Component.translatable("gui.adv_rocketry.orbital.satellite_payload")
            : menu.entity.microwaveReceiver()
                ? Component.translatable("gui.adv_rocketry.orbital.microwave_satellite_chip")
                : menu.entity.solarArray()
                    ? Component.translatable("gui.adv_rocketry.orbital.solar_array")
                    : menu.entity.blackHoleGenerator()
                        ? Component.translatable("gui.adv_rocketry.horizon.chip")
                        : menu.entity.warpCore()
                            ? Texts.translate(
                                "gui.adv_rocketry.orbital.station_warp_fuel",
                                menu.stationFuel(),
                                WarpCoreLogic.MAX_FUEL)
                            : menu.entity.terminal()
                                ? Component.translatable(
                                    "gui.adv_rocketry.orbital.id_chip_data_unit")
                                : Component.translatable(
                                    "gui.adv_rocketry.orbital.controller_modules"),
        8,
        26,
        UiTheme.MUTED,
        false);
    if (menu.entity.blackHoleGenerator())
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.horizon.bonus", menu.horizonBonus()),
          52,
          46,
          UiTheme.TEXT,
          false);
    if (menu.entity.builder())
      drawText(
          graphics,
          menu.builderProgress() > 0
              ? Texts.translate("gui.adv_rocketry.orbital.assembly", menu.builderProgress())
              : Component.translatable("gui.adv_rocketry.orbital.assembly_ready"),
          8,
          116,
          UiTheme.TEXT,
          false);
    else if (menu.entity.terminal())
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.energy_fe", menu.energy()),
          8,
          74,
          UiTheme.TEXT,
          false);
    else if (menu.entity.microwaveReceiver()
        || menu.entity.solarArray()
        || menu.entity.blackHoleGenerator())
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.energy_fe", menu.energy()),
          8,
          74,
          UiTheme.TEXT,
          false);
    else if (menu.entity.warpCore())
      drawText(
          graphics,
          menu.warpCoreFormed()
              ? Component.translatable("gui.adv_rocketry.orbital.warp_core_formed")
              : Component.translatable("gui.adv_rocketry.orbital.build_the_warp_core_frame"),
          8,
          48,
          UiTheme.TEXT,
          false);
    else if (!menu.entity.hatch())
      drawText(
          graphics,
          Texts.translate("gui.adv_rocketry.orbital.energy_fe", menu.energy()),
          68,
          27,
          UiTheme.TEXT,
          false);
    drawText(
        graphics, playerInventoryTitle, 8, menu.entity.builder() ? 143 : 93, UiTheme.TEXT, false);
  }

  private void renderAsteroidSurvey(GuiGraphics graphics, int candidate) {
    drawText(
        graphics,
        Component.translatable("gui.adv_rocketry.orbital.estimated_cargo"),
        184,
        24,
        UiTheme.MUTED,
        false);
    List<AsteroidCatalog.Asteroid> asteroids =
        AsteroidCatalog.types(minecraft.level.registryAccess());
    if (candidate < 0 || candidate >= asteroids.size()) {
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.select_a_target"),
          184,
          43,
          UiTheme.TEXT,
          false);
      return;
    }
    AsteroidCatalog.Asteroid asteroid = asteroids.get(candidate);
    List<SurveyRow> entries = survey(asteroid);
    asteroidPreviewScroll = Math.clamp(asteroidPreviewScroll, 0, Math.max(0, entries.size() - 4));
    for (int row = 0; row < 4 && asteroidPreviewScroll + row < entries.size(); row++) {
      SurveyRow surveyRow = entries.get(asteroidPreviewScroll + row);
      AsteroidCatalog.SurveyEntry entry = surveyRow.entry();
      ItemStack stack = surveyRow.stack();
      int y = 42 + row * 36;
      graphics.renderItem(stack, 184, y);
      drawText(
          graphics,
          font.plainSubstrByWidth(stack.getHoverName().getString(), 108),
          204,
          y,
          UiTheme.TEXT,
          false);
      drawText(
          graphics,
          Component.literal(entry.midpoint() + " +/- " + entry.variability()),
          204,
          y + 13,
          UiTheme.MUTED,
          false);
    }
    if (entries.size() > 4)
      drawText(
          graphics,
          Component.translatable("gui.adv_rocketry.orbital.scroll_for_more_cargo"),
          184,
          188,
          UiTheme.TEXT,
          false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.orbital.mission_time_x", asteroid.timeMultiplier()),
        184,
        202,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.orbital.composition", menu.compositionData()),
        184,
        216,
        UiTheme.TEXT,
        false);
    drawText(
        graphics,
        Texts.translate("gui.adv_rocketry.orbital.mass", menu.massData()),
        184,
        226,
        UiTheme.TEXT,
        false);
  }

  private List<ResourceLocation> earthBiomes() {
    if (earthBiomes == null)
      earthBiomes =
          minecraft.level.registryAccess().registryOrThrow(Registries.BIOME).keySet().stream()
              .sorted()
              .toList();
    return earthBiomes;
  }

  private List<SurveyRow> survey(AsteroidCatalog.Asteroid asteroid) {
    SurveyKey key =
        new SurveyKey(asteroid, menu.asteroidSeed(), menu.compositionData(), menu.massData());
    if (!key.equals(surveyKey)) {
      surveyKey = key;
      surveyRows =
          AsteroidCatalog.survey(asteroid, key.seed(), key.composition(), key.mass()).stream()
              .flatMap(
                  entry ->
                      BuiltInRegistries.ITEM
                          .getOptional(entry.item())
                          .filter(item -> item != Items.AIR)
                          .map(item -> new SurveyRow(entry, new ItemStack(item)))
                          .stream())
              .toList();
    }
    return surveyRows;
  }

  private record SurveyKey(
      AsteroidCatalog.Asteroid asteroid, long seed, int composition, int mass) {}

  private record SurveyRow(AsteroidCatalog.SurveyEntry entry, ItemStack stack) {}

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (menu.entity.observatory()
        && mouseX >= leftPos + 176
        && mouseX < leftPos + imageWidth
        && mouseY >= topPos
        && mouseY < topPos + imageHeight) {
      asteroidPreviewScroll = Math.max(0, asteroidPreviewScroll - (int) Math.signum(scrollY));
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  private static final class SelectorButton extends UiButton {
    SelectorButton(Button.Builder builder) {
      super(builder);
    }

    @Override
    public void playDownSound(SoundManager manager) {
      manager.play(SimpleSoundInstance.forUI(OrbitalRegistry.SELECTOR_BUTTON_SOUND.get(), 1f, .7f));
    }
  }
}

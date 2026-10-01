// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.orbit;

import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.space.Star;
import advRocketry.util.Texts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Original star-system navigation, scale and redstone states backed by native station data. */
public final class HolographicSelectorLogic {
  private HolographicSelectorLogic() {}

  public static boolean enabled(OrbitalBlockEntity selector) {
    if (selector.getLevel() == null) return false;
    boolean powered = selector.getLevel().hasNeighborSignal(selector.getBlockPos());
    return selector.holoRedstone == 0
        || selector.holoRedstone == 1 && powered
        || selector.holoRedstone == 2 && !powered;
  }

  public static List<Planet> planets(GalaxyData galaxy, OrbitalBlockEntity selector) {
    return galaxy.planets.values().stream()
        .filter(
            planet ->
                planet.id >= 0
                    && (selector.holoCenter >= 0
                        ? planet.id == selector.holoCenter || planet.parent == selector.holoCenter
                        : planet.star == selector.holoStar && planet.parent < 0))
        .sorted(Comparator.comparingInt(planet -> planet.id))
        .toList();
  }

  public enum Kind {
    PLANET,
    STAR,
    COMPANION,
    BACK
  }

  public record Body(
      Kind kind, int id, Component name, Vec3 offset, float radius, int color, boolean selected) {}

  /** One server-owned layout supplies both the visible body and its interaction bounds. */
  public static List<Body> bodies(GalaxyData galaxy, OrbitalBlockEntity selector) {
    if (!enabled(selector) || !selector.holographicSelector()) return List.of();
    List<Body> result = new ArrayList<>();
    float scale = .8f + selector.holoSize / 20f;
    if (selector.holoStar < 0) {
      for (Star star : galaxy.stars.values()) {
        boolean selected = star.id() == selector.holoSelectedStar;
        result.add(
            new Body(
                Kind.STAR,
                star.id(),
                Component.literal(star.name()),
                new Vec3(star.x() / 100d * scale, .1, star.z() / 100d * scale),
                .25f * scale,
                selected ? 0xd07feaff : starColor(star.temperature(), star.blackHole()),
                selected));
      }
    } else {
      Star star = galaxy.stars.get(selector.holoStar);
      if (selector.holoCenter < 0 && star != null)
        result.add(
            new Body(
                Kind.STAR,
                star.id(),
                Component.literal(star.name()),
                new Vec3(0, .1, 0),
                .35f * scale,
                starColor(star.temperature(), star.blackHole()),
                false));
      if (selector.holoCenter < 0 && star != null) {
        for (int index = 0; index < star.companions().size(); index++) {
          Star.Companion companion = star.companions().get(index);
          double phase = index * Math.PI * 2 / star.companions().size();
          double distance = companion.separation() * .05 * scale;
          result.add(
              new Body(
                  Kind.COMPANION,
                  index,
                  Component.literal(companion.name()),
                  new Vec3(Math.cos(phase) * distance, .1, Math.sin(phase) * distance),
                  .35f * scale * companion.size(),
                  starColor(companion.temperature(), companion.blackHole()),
                  false));
        }
      }
      for (Planet planet : planets(galaxy, selector)) {
        double distance =
            planet.id == selector.holoCenter ? 0 : scale * (.1 + planet.orbitalDistance / 100d);
        double angle =
            planet.orbitalAngle(
                selector.getLevel().getGameTime(),
                galaxy.stars.get(planet.star),
                galaxy.planets.get(planet.parent));
        boolean selected = planet.id == selector.holoTarget;
        result.add(
            new Body(
                Kind.PLANET,
                planet.id,
                Component.literal(planet.name),
                new Vec3(Math.cos(angle) * distance, .1, Math.sin(angle) * distance),
                .18f * scale * Math.max(.5f, planet.gravity * planet.gravity),
                selected
                    ? 0xd07feaff
                    : 0xa0000000 | (planet.skyColor == 0 ? 0x6a9ccd : planet.skyColor),
                selected));
      }
      result.add(
          new Body(
              Kind.BACK,
              0,
              Component.translatable("gui.adv_rocketry.orbital.selector.back"),
              new Vec3(0, .45 + selector.holoSize / 10d, 0),
              .15f,
              0xd07feaff,
              true));
    }
    return result;
  }

  private static int starColor(int temperature, boolean blackHole) {
    if (blackHole) return 0xa07855aa;
    float[] color = Star.color(temperature);
    return 0xb0000000
        | (int) (color[0] * 255) << 16
        | (int) (color[1] * 255) << 8
        | (int) (color[2] * 255);
  }

  public static void tick(OrbitalBlockEntity selector) {
    if (!(selector.getLevel() instanceof ServerLevel level)) return;
    List<Body> visible = bodies(GalaxyData.get(level.getServer()), selector);
    selector.holograms.removeIf(
        entity -> {
          boolean obsolete = entity.isRemoved() || visible.stream().noneMatch(entity::matches);
          if (obsolete && !entity.isRemoved()) entity.discard();
          return obsolete;
        });
    for (Body body : visible) {
      HologramBodyEntity entity =
          selector.holograms.stream()
              .filter(candidate -> candidate.matches(body))
              .findFirst()
              .orElse(null);
      if (entity == null) {
        entity = new HologramBodyEntity(OrbitalRegistry.HOLOGRAM_BODY.get(), level);
        entity.update(selector, body);
        if (level.addFreshEntity(entity)) selector.holograms.add(entity);
      } else entity.update(selector, body);
    }
  }

  public static boolean select(OrbitalBlockEntity selector, Kind kind, int id) {
    if (!(selector.getLevel() instanceof ServerLevel level)) return false;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    if (kind == Kind.COMPANION) return false;
    if (bodies(galaxy, selector).stream().noneMatch(body -> body.kind() == kind && body.id() == id))
      return false;
    if (kind == Kind.BACK) {
      control(selector, 44);
    } else if (kind == Kind.STAR) {
      if (selector.holoStar >= 0) return false;
      if (selector.holoSelectedStar == id) {
        selector.holoStar = id;
        selector.holoCenter = -1;
        selector.holoTarget = -1;
        selector.holoSelectedStar = -1;
      } else selector.holoSelectedStar = id;
      selector.status =
          Texts.translate(
              "status.adv_rocketry.holographic_selector.selected", galaxy.stars.get(id).name());
    } else if (kind == Kind.PLANET) {
      if (selector.holoTarget == id) {
        control(selector, 43);
      } else {
        selector.holoTarget = id;
        selector.status =
            Texts.translate(
                "status.adv_rocketry.holographic_selector.selected", galaxy.planets.get(id).name);
        if (StationLogic.at(galaxy, level, selector.getBlockPos()) != null)
          target(selector, galaxy);
      }
    }
    selector.setChanged();
    level.sendBlockUpdated(
        selector.getBlockPos(), selector.getBlockState(), selector.getBlockState(), 3);
    tick(selector);
    return true;
  }

  public static void control(OrbitalBlockEntity selector, int button) {
    if (!selector.selectorControls() || !(selector.getLevel() instanceof ServerLevel level)) return;
    GalaxyData galaxy = GalaxyData.get(level.getServer());
    switch (button) {
      case 41 -> {
        List<Planet> visible = planets(galaxy, selector);
        int index = -1;
        for (int i = 0; i < visible.size(); i++)
          if (visible.get(i).id == selector.holoTarget) index = i;
        if (!visible.isEmpty()) {
          Planet next = visible.get((index + 1) % visible.size());
          selector.holoTarget = next.id;
          selector.status =
              Texts.translate("status.adv_rocketry.holographic_selector.selected", next.name);
        }
      }
      case 42 -> target(selector, galaxy);
      case 43 -> {
        if (selector.holoTarget >= 0 && galaxy.planets.containsKey(selector.holoTarget)) {
          selector.holoCenter = selector.holoTarget;
          selector.holoStar = galaxy.planets.get(selector.holoTarget).star;
          selector.holoTarget = -1;
          selector.status =
              Component.translatable(
                  "status.adv_rocketry.holographic_selector.showing_planet_and_moons");
        }
      }
      case 44 -> {
        selector.holoSelectedStar = -1;
        if (selector.holoCenter >= 0) selector.holoCenter = -1;
        else selector.holoStar = selector.holoStar < 0 ? 0 : -1;
        selector.holoTarget = -1;
        selector.status =
            selector.holoStar < 0
                ? Component.translatable("status.adv_rocketry.holographic_selector.showing_stars")
                : Component.translatable(
                    "status.adv_rocketry.holographic_selector.showing_star_system");
      }
      case 45 -> {
        selector.holoSelectedStar = -1;
        List<Integer> stars = galaxy.stars.keySet().stream().sorted().toList();
        int index = stars.indexOf(selector.holoStar);
        if (!stars.isEmpty()) selector.holoStar = stars.get((index + 1) % stars.size());
        selector.holoCenter = -1;
        selector.holoTarget = -1;
        selector.status =
            Texts.translate(
                "status.adv_rocketry.holographic_selector.showing_star", selector.holoStar);
      }
      case 46 -> selector.holoSize = Math.min(100, selector.holoSize + 10);
      case 47 -> selector.holoSize = Math.max(0, selector.holoSize - 10);
      case 48 -> selector.holoRedstone = (selector.holoRedstone + 1) % 3;
      default -> {
        return;
      }
    }
    selector.setChanged();
    level.sendBlockUpdated(
        selector.getBlockPos(), selector.getBlockState(), selector.getBlockState(), 3);
  }

  private static void target(OrbitalBlockEntity selector, GalaxyData galaxy) {
    Planet selected = galaxy.planets.get(selector.holoTarget);
    if (selected == null || !selected.landable()) {
      selector.status =
          Component.translatable(
              "status.adv_rocketry.holographic_selector.choose_a_landable_planet");
      return;
    }
    Station station =
        selector.getLevel() == null
            ? null
            : StationLogic.at(galaxy, selector.getLevel(), selector.getBlockPos());
    if (station == null) {
      selector.status =
          Component.translatable(
              "status.adv_rocketry.holographic_selector.selector_must_be_on_a_space");
      return;
    }
    StationLogic.update(galaxy, station, record -> record.warpTarget = selected.id);
    selector.status =
        Texts.translate(
            "status.adv_rocketry.holographic_selector.station_targeting", selected.name);
  }
}

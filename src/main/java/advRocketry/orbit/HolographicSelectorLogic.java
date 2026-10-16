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
    return selector.hologram.redstone == 0
        || selector.hologram.redstone == 1 && powered
        || selector.hologram.redstone == 2 && !powered;
  }

  public static List<Planet> planets(GalaxyData galaxy, OrbitalBlockEntity selector) {
    return galaxy.planets.values().stream()
        .filter(
            planet ->
                planet.id >= 0
                    && (selector.hologram.center >= 0
                        ? planet.id == selector.hologram.center
                            || planet.parent == selector.hologram.center
                        : planet.star == selector.hologram.star && planet.parent < 0))
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
    float scale = .8f + selector.hologram.size / 20f;
    if (selector.hologram.star < 0) {
      for (Star star : galaxy.stars.values()) {
        boolean selected = star.id() == selector.hologram.selectedStar;
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
      Star star = galaxy.stars.get(selector.hologram.star);
      if (selector.hologram.center < 0 && star != null)
        result.add(
            new Body(
                Kind.STAR,
                star.id(),
                Component.literal(star.name()),
                new Vec3(0, .1, 0),
                .35f * scale,
                starColor(star.temperature(), star.blackHole()),
                false));
      if (selector.hologram.center < 0 && star != null) {
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
            planet.id == selector.hologram.center
                ? 0
                : scale * (.1 + planet.orbitalDistance / 100d);
        double angle =
            planet.orbitalAngle(
                selector.getLevel().getGameTime(),
                galaxy.stars.get(planet.star),
                galaxy.planets.get(planet.parent));
        boolean selected = planet.id == selector.hologram.target;
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
              new Vec3(0, .45 + selector.hologram.size / 10d, 0),
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
    selector.hologram.bodies.removeIf(
        entity -> {
          boolean obsolete = entity.isRemoved() || visible.stream().noneMatch(entity::matches);
          if (obsolete && !entity.isRemoved()) entity.discard();
          return obsolete;
        });
    for (Body body : visible) {
      HologramBodyEntity entity =
          selector.hologram.bodies.stream()
              .filter(candidate -> candidate.matches(body))
              .findFirst()
              .orElse(null);
      if (entity == null) {
        entity = new HologramBodyEntity(OrbitalRegistry.HOLOGRAM_BODY.get(), level);
        entity.update(selector, body);
        if (level.addFreshEntity(entity)) selector.hologram.bodies.add(entity);
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
      control(selector, OrbitalAction.SELECTOR_BACK.id());
    } else if (kind == Kind.STAR) {
      if (selector.hologram.star >= 0) return false;
      if (selector.hologram.selectedStar == id) {
        selector.hologram.star = id;
        selector.hologram.center = -1;
        selector.hologram.target = -1;
        selector.hologram.selectedStar = -1;
      } else selector.hologram.selectedStar = id;
      selector.status =
          Texts.translate(
              "status.adv_rocketry.holographic_selector.selected", galaxy.stars.get(id).name());
    } else if (kind == Kind.PLANET) {
      if (selector.hologram.target == id) {
        control(selector, OrbitalAction.SELECTOR_CENTER.id());
      } else {
        selector.hologram.target = id;
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
    switch (OrbitalAction.fromId(button)) {
      case SELECTOR_NEXT -> {
        List<Planet> visible = planets(galaxy, selector);
        int index = -1;
        for (int i = 0; i < visible.size(); i++)
          if (visible.get(i).id == selector.hologram.target) index = i;
        if (!visible.isEmpty()) {
          Planet next = visible.get((index + 1) % visible.size());
          selector.hologram.target = next.id;
          selector.status =
              Texts.translate("status.adv_rocketry.holographic_selector.selected", next.name);
        }
      }
      case SELECTOR_TARGET -> target(selector, galaxy);
      case SELECTOR_CENTER -> {
        if (selector.hologram.target >= 0 && galaxy.planets.containsKey(selector.hologram.target)) {
          selector.hologram.center = selector.hologram.target;
          selector.hologram.star = galaxy.planets.get(selector.hologram.target).star;
          selector.hologram.target = -1;
          selector.status =
              Component.translatable(
                  "status.adv_rocketry.holographic_selector.showing_planet_and_moons");
        }
      }
      case SELECTOR_BACK -> {
        selector.hologram.selectedStar = -1;
        if (selector.hologram.center >= 0) selector.hologram.center = -1;
        else selector.hologram.star = selector.hologram.star < 0 ? 0 : -1;
        selector.hologram.target = -1;
        selector.status =
            selector.hologram.star < 0
                ? Component.translatable("status.adv_rocketry.holographic_selector.showing_stars")
                : Component.translatable(
                    "status.adv_rocketry.holographic_selector.showing_star_system");
      }
      case SELECTOR_STAR -> {
        selector.hologram.selectedStar = -1;
        List<Integer> stars = galaxy.stars.keySet().stream().sorted().toList();
        int index = stars.indexOf(selector.hologram.star);
        if (!stars.isEmpty()) selector.hologram.star = stars.get((index + 1) % stars.size());
        selector.hologram.center = -1;
        selector.hologram.target = -1;
        selector.status =
            Texts.translate(
                "status.adv_rocketry.holographic_selector.showing_star", selector.hologram.star);
      }
      case SELECTOR_SIZE_UP -> selector.hologram.size = Math.min(100, selector.hologram.size + 10);
      case SELECTOR_SIZE_DOWN -> selector.hologram.size = Math.max(0, selector.hologram.size - 10);
      case SELECTOR_REDSTONE -> selector.hologram.redstone = (selector.hologram.redstone + 1) % 3;
      default -> {
        return;
      }
    }
    selector.setChanged();
    level.sendBlockUpdated(
        selector.getBlockPos(), selector.getBlockState(), selector.getBlockState(), 3);
  }

  private static void target(OrbitalBlockEntity selector, GalaxyData galaxy) {
    Planet selected = galaxy.planets.get(selector.hologram.target);
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

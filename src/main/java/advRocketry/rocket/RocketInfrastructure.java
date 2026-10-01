// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.rocket;

/** Native ground infrastructure selected with the in-mod linker. */
public interface RocketInfrastructure {
  boolean link(RocketEntity rocket);

  boolean linkBuilder(RocketBlockEntity builder);

  void unlink();

  void tick();
}

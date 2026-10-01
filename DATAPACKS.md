# Datapacks

Everything here can be changed by a datapack, KubeJS script or mod. Recipes,
tags and data maps reload with `/reload`. Stars, planets, asteroids, ore
profiles and multiblocks load when the world starts.

## Processing recipes

Recipe type `adv_rocketry:processing`, under `data/<namespace>/recipe/`.
Override a shipped recipe by its id, or disable it with a `neoforge:false`
condition.

| Field | Meaning |
|---|---|
| `machine` | Machine id, e.g. `lathe`, `centrifuge`, `plate_press` |
| `ticks`, `energy` | Duration and FE per tick |
| `inputs` | NeoForge sized ingredients, used up |
| `catalysts` | Sized ingredients that must be present but are not used up |
| `fluid_inputs` | Sized fluid ingredients |
| `outputs` | `id` or `tag`, with optional `count`, `components` and per-item `chance` |
| `fluid_outputs` | Fluid stacks |
| `weighted_outputs`, `rolls` | A random pool rolled `rolls` times |

A tag output gives the tag's first item. Weighted outputs whose tag is empty are
skipped.

## Stars

`data/<namespace>/adv_rocketry/star/`: `name`, `temperature`, `size`, map
position `x` and `z`, `black_hole`, `companions` (each with `temperature`,
`size`, `black_hole` and `separation`), and how many random planets
(`generated_planets`) and gas giants (`generated_gas_giants`) to add.

## Planets

`data/<namespace>/adv_rocketry/planet/`. Required: `name` and `star`.

- **Identity:** `parent` (makes it a moon), `dimension`, `known`, `icon`.
- **Physics:** `gas_giant`, `atmosphere`, `oxygen`, `temperature`, `gravity`,
  `gases`.
- **Orbit:** `orbital_distance`, `orbital_theta`, `orbital_phi`,
  `rotation_period`, `retrograde`.
- **Sky:** `sky_color`, `fog_color` (`#rrggbb` or an integer), `rings`,
  `ring_color`, `sky_render_override`, `color_override`, `shading`.
- **Terrain:** `terrain` (`terrestrial`, `asteroid`, `cave`, `moon`,
  `volcanic`), `sea_level`, `filler_block`, `ocean_block`, `caves`,
  `structures`, `force_rivers`.
- **Biomes:** `biomes`, optionally weighted, e.g.
  `[{"biome": "minecraft:plains", "weight": 9}]`. `crater_biomes` limits craters
  to listed biomes.
- **Features:** `craters`, `volcanoes`, `geodes`, each with a `*_frequency`
  multiplier (higher is rarer).
- **Resources:** `ores`, `laser_drill_ores`, `laser_drillable`, `geode_ores`,
  `crater_ores`, `warp_artifacts`. These take an id or `#tag` with an optional
  `count`.
- **Spawns:** `spawns`, e.g.
  `{"entity": "minecraft:sheep", "weight": 100, "min": 2, "max": 4}`, with an
  optional `data` compound for the spawned mob.

The planet whose `dimension` is `minecraft:overworld` is Earth. A planet with no
`dimension` uses its own id, so `adv_rocketry:luna` is the `adv_rocketry:luna`
dimension. `icon` takes a name from `adv_rocketry:textures/planets/` or a full
texture path.

New definitions join an existing world at the next startup, and a planet keeps
its id even if other definitions change. The `resetPlanetsFromDefinitions`
config option rebuilds the galaxy from the current definitions while keeping
stations, satellites, missions and terrain.

## Ore profiles

`data/<namespace>/adv_rocketry/ore_profile/` gives generated planets ores by
climate. A profile names a `pressure` (`super_high`, `high`, `normal`, `low`,
`none`), a `temperature` (`too_hot`, `hot`, `normal`, `cold`, `frigid`,
`snowball`) or both, and an `ores` list of
`{"block", "min_height", "max_height", "size", "count"}`. A profile matching
both beats one matching one. A planet's own `ores` list overrides all profiles.

## Asteroids

`data/<namespace>/adv_rocketry/asteroid/`: `distance`, `mass`,
`mass_variability`, `richness`, `richness_variability`, `probability`,
`time_multiplier`, a `base` item and weighted `ores`. The display name comes
from the `asteroid.<namespace>.<id>` translation key.

## Multiblocks

`data/<namespace>/adv_rocketry/multiblock/`. Each definition names its
`controller` block and lists `layers` from top to bottom. A layer is a list of
rows, starting at the controller's front and going back. Rows read left to right
as seen from the front.

- `c` marks the controller and a space is ignored.
- Every other character is defined in `keys`, with `any` (block ids, `#tags` or
  lists) and `air` (true if air is allowed).
- `rotates` (default true) turns the layout with the controller's facing.
- `order` sets the position in the projector list.

Machines find some parts by key, so a replacement layout should keep the
shipped letters for ports, motors, coils, lenses and panels.

## Tags

| Tag | Type | Purpose |
|---|---|---|
| `adv_rocketry:planet_biomes/excluded`, `high_pressure`, `single`, `airless`, `scorched` | biome | Automatic planet biome selection |
| `adv_rocketry:rocket_blacklist` | block | Cannot be part of a rocket |
| `adv_rocketry:sealable`, `adv_rocketry:unsealable` | block | Always or never airtight |
| `adv_rocketry:torches` | block | Torches that go out in vacuum |
| `adv_rocketry:atmosphere_immune` | entity type | Unaffected by atmosphere |
| `adv_rocketry:harvestable_gases` | fluid | Collectable from every gas giant |
| `adv_rocketry:laser_drill_ores` | item | Orbital laser ore pool |
| `adv_rocketry:geode_ores` | block | Geode ore pool |
| `adv_rocketry:motors`, `coils`, `machine_ports` | block | Parts the multiblocks accept |

## Data maps

Under `data/<namespace>/data_maps/<registry>/`:

| Data map | Registry | Fields |
|---|---|---|
| `adv_rocketry:black_hole_fuel` | item | `burn_time` in ticks |
| `adv_rocketry:rocket_propellant` | fluid | Multipliers for `monopropellant`, `bipropellant`, `oxidizer`, `nuclear` |
| `adv_rocketry:gas_giant_gas` | fluid | `min_gravity`, `max_gravity`, `chance` |
| `adv_rocketry:satellite_module` | item | `power_generation`, `energy_storage`, `data_storage` |

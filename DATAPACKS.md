# Datapacks

Everything here can be changed by a datapack, KubeJS script or mod. Recipes,
tags and data maps reload with `/reload`. Stars, planets, asteroids, ore
profiles and multiblocks load when the world starts.

## Survival acquisition

Centrifuge casings, the Basic Laser Gun, and the Earthbright Visor use crafting
recipes. Silicon, steel, titanium, titanium aluminide, and titanium iridium dusts
can be pressed from their ingots at one dust per ingot, without FE.

Ores, terrain, alien vegetation, and colored crystals come from world generation.
Filled buckets use fluid collection; extinguished torches come from atmospheric
extinguishing. Landing floats form automatically for rocket landings over lava,
and packed space stations come from the station assembler. Creative Energy Input
is intentionally creative-only. Satellite research uses `item_data_storage`;
planet selection uses `holographic_planet_selector`.

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

Outputs preserve their component patches when resolved through tags. Each operation
may produce at most 4096 items, including weighted rolls; output pools have at most
256 entries. Large results are split into stacks before saving.

### Common fluids and output overrides

Suits, oxygen vents and chargers accept `#c:oxygen`; jetpacks and chargers accept
`#c:hydrogen`. Terraformers consume `#c:oxygen` and `#c:nitrogen`. Tanks preserve
the supplied fluid ID and components; a partly filled tank only mixes identical
fluid stacks. AR supplies source and flowing entries in these common tags.
Rocket propellant defaults use `#c:hydrogen`, `#c:oxygen`, and `#c:rocket_fuel`
through the `adv_rocketry:rocket_propellant` fluid data map. The Fuel Generator's
rocket-fuel recipe also accepts `#c:rocket_fuel`.

Fluid recipe inputs use `{ "tag": "c:oxygen", "amount": 10 }`. Fluid outputs
use a concrete registry ID, for example `{ "id": "othermod:oxygen", "amount": 100 }`.
To choose another mod's electrolyzer outputs, override
`data/adv_rocketry/recipe/processing/hydrogenoxygen.json` in your datapack:

```json
{
  "type": "adv_rocketry:processing",
  "machine": "electrolyzer",
  "ticks": 100,
  "energy": 20,
  "fluid_inputs": [{ "tag": "c:water", "amount": 10 }],
  "fluid_outputs": [
    { "id": "othermod:hydrogen", "amount": 100 },
    { "id": "othermod:oxygen", "amount": 100 }
  ]
}
```

Replace the example IDs with installed fluids. Override
`data/adv_rocketry/recipe/processing/rocketfuel.json` the same way to choose the
chemical reactor's fuel output. Add replacement fluids to the common tags if
their mod does not already do so. Reload recipes and tags with `/reload`.

Gas-collection outputs come from a planet's `gases` list, the
`adv_rocketry:harvestable_gases` fluid tag, and the `adv_rocketry:gas_giant_gas`
data map for generated gas giants. These accept other mods' registered fluid
IDs. Pumps and rocket disassembly return the actual collected or stored fluid.
These production paths do not need an additional output config.

### Armor sealing

`adv_rocketry:armor_sealing` recipes select an `armor_type` (`helmet`, `chestplate`,
`leggings`, `boots`, or `body`) and specify `materials` (NeoForge sized ingredients),
`ticks`, and `energy` per tick. The chemical reactor preserves the armor's components
and adds the airtight enchantment. Override `adv_rocketry:sealing/<armor_type>` to
change a default recipe, or disable it with a `neoforge:false` condition.

### Coil timing

The block data map `adv_rocketry:processing_time` assigns a duration multiplier to
coils in multiblock `K` positions. Values range from 0.001 to 1000 and multiply
together. Override `data/adv_rocketry/data_maps/block/processing_time.json`; blocks
without an entry use a multiplier of 1.

### Multiblock validation

Definitions require exactly one `c` controller, defined symbols, and no more than
16384 occupied cells. Each dimension is limited to 64. Orbital lasers require two
`O` output hatches and a `P` energy input; railguns and astrobody processors require
`I`, `O`, and `P`; satellite builders require `P`. These roles must resolve to the
corresponding port blocks. Biome source weights must match their biome list.

## Liquid generator fuels

Recipe type `adv_rocketry:liquid_fuel`, under `data/<namespace>/recipe/`.
The Fuel Generator consumes the specified fluid amount once, then generates
`fe_per_tick` for `ticks` active ticks. Burning pauses when its energy buffer is
full. JEI shows all loaded fuel recipes and their total energy.

```json
{
  "type": "adv_rocketry:liquid_fuel",
  "fuel": { "fluid": "adv_rocketry:rocket_fuel", "amount": 1000 },
  "ticks": 1200,
  "fe_per_tick": 360
}
```

`fuel` is a NeoForge sized fluid ingredient; use `"tag": "yourpack:generator_fuels"`
instead of `"fluid"` to match a fluid tag. Amount must be 1Ã¢â‚¬â€œ16,000 mB, duration
1Ã¢â‚¬â€œ1,000,000 ticks, and output 1Ã¢â‚¬â€œ1,000,000 FE/t. Total energy is amount-independent:
`ticks Ãƒâ€” fe_per_tick` per consumed batch. Keep amounts within the tank capacity.

Override `adv_rocketry:liquid_fuel/rocket_fuel` or `adv_rocketry:liquid_fuel/lava`
to change the shipped fuels. Add recipe files for other liquids, including modded
ones. Overlapping matching recipes use the lowest recipe ID in lexical order.
Recipes and tags reload with `/reload`; a batch already consumed keeps its saved
duration/output, and subsequent batches use the new recipes. Unsupported fluid
remains recoverable from the tank. A recipe can be disabled with a `neoforge:false`
condition as with other recipes.

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
  `structures`, `force_rivers`, `noise_profile`, `generation_version`, and
  `biome_features`. Generation versions 2 and 3 run native biome features and modifiers;
  individual planetary plants apply their own pressure, temperature, and attachment rules.
  `noise_profile` accepts `auto`, `temperate`, `volcanic`, `lunar`, `fractured`,
  or `cavern`. `auto` derives a profile from terrain, pressure, and temperature.
  The selected profile is stored with the planet. `generation_version` defaults
  to `3`; `1` selects the original terrain implementation and `2` selects
  the continuous terrain without watershed habitats. The version is saved per planet.
  Version `3` connects temperate water rivers to wet banks, marsh/swamp transitions,
  underground waterways, and occasional sinkholes. This requires rivers, water oceans,
  pressure above 25, and temperature from 250 through 450 K. Disabling `caves`
  also disables underground waterways and sinkholes.
  `gravity` scales river incision using `3 + 5 * sqrt(clamp(gravity, 0.15, 4))`
  blocks at the channel center, plus local sediment variation. Higher gravity narrows
  the bank transition and deepens the bed; it does not relocate the river network.
  Forest and swamp palettes gain riparian transitions; other surface palettes retain
  their own biome selection. Rootwood favors wet ground, while bluewood occupies
  drier slopes. Young growth, old trees, dead snags, and fallen logs use their existing
  wood, leaf, and plant blocks and normal harvest behavior. Rootwood has broad,
  low crowns with hanging roots; bluewood has stepped, rising branch crowns.
  Riverbanks blend silt and rooted soil into exposed gravel according to slope,
  moisture, and local patches. Reeds line the water, sedges and groundcover extend
  inland, and aquatic fronds and floating leaves favor shallow margins.
  Moist low floodplains contain shallow pools and muddy islands. Rootwood stands
  favor raised islands, with open sedge patches and floating leaves around shallow
  water. These transitions taper out before uplands and preserve main-channel incision.
  Installed biome mods and datapacks participate in automatic selection through the
  biome registry. Terralith and Biomes O' Plenty surface biomes follow the normal
  temperature rules. `minecraft:is_nether` biomes (including Incendium and BOP's
  Nether biomes) enter the scorched pool above 450 K; `minecraft:is_end` biomes
  (including Nullscape and BOP's End biomes) enter the airless pool at pressure 25
  or below. Nether, End, river and underground tags are excluded from the ordinary
  surface pool. The existing `planet_biomes/airless`, `scorched`, and `excluded`
  tags can be replaced or extended by a datapack to change these choices.
  `adv_rocketry:planet_biomes/caves` adds optional temperate underground habitats;
  its defaults include selected Terralith caves and BOP's glowing grotto, while
  retaining vanilla lush/dripstone and AR caves. Missing optional entries are ignored.
  Imported surface biomes use their registered Overworld, Nether, or End surface
  rules with bedrock placements removed. AR retains its terrain, gravity, river,
  and cave shapes; biome features use the installed mod's definitions. Custom
  surface rules injected outside the noise-settings registry remain mod-dependent.
  Automatic selection applies when creating planets; existing saved palettes and
  explicitly supplied biome lists are retained. No third-party biome data is bundled.
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

Version 2 uses AR-owned density fields with continents, ridges, warped channels,
noise chambers and tunnels, plus native carvers. Caves default to enabled.
`caves: false` disables both
underground carving and automatic cave-biome selection. Wet temperate planets
retain vanilla lush and dripstone caves alongside root, fungal and mineral caves.
Cold planets select subglacial caves; hot and airless profiles use mineral and
lava/lunar tube habitats. River biomes follow the same channel field as terrain
and stop at the cave layer. Fractured islands and enclosed caverns omit rivers.
Terraforming resolves biomes at each vertical quart cell rather than flattening
an entire column into its surface biome.

The seven companion biome IDs are `alien_river`, `root_caverns`,
`fungal_caverns`, `mineral_caves`, `lava_tubes`, `lunar_tubes`, and
`subglacial_caves`, all under `adv_rocketry`. They are excluded from random
surface palettes but remain available for explicit definitions. Generation is
seeded per planet and independent of replacement Overworld/Nether noise graphs.

The planet whose `dimension` is `minecraft:overworld` is Earth. A planet with no
`dimension` uses its own id, so `adv_rocketry:luna` is the `adv_rocketry:luna`
dimension. `icon` takes a name from `adv_rocketry:textures/planets/` or a full
texture path.

New definitions join an existing world at the next startup, and a planet keeps
its id even if other definitions change. The `resetPlanetsFromDefinitions`
config option rebuilds the galaxy from the current definitions while keeping
stations, satellites, missions and terrain.

### Dynamic skies

An optional `sky` object in a planet definition controls its cosmetic layers:

```json
"sky": {
  "profile": "frozen",
  "clouds": {
    "coverage": 0.4,
    "color": "#b8d9ff",
    "height": 220,
    "speed": 0.5,
    "direction": 120
  },
  "haze": 0.8,
  "sunset": 1.2,
  "stars": 1.1,
  "star_density": 1.0,
  "twinkle": 0.6,
  "distortion": 0.5,
  "galaxy": 0.65,
  "aurora": 0.6,
  "nebula": 0.15,
  "seed": 37
}
```

Every field is optional. `profile` defaults to `auto`, derived from pressure,
terrain, planet temperature and local biome temperature. Explicit profiles are
`clear`, `temperate`, `dense`, `frozen` and `ash`. Existing `sky_color`,
`fog_color` and `color_override` still determine the atmospheric palette.

Cloud coverage defaults to automatic (`-1`); values from `0` to `1` set its
baseline. Zero disables clouds even in rain. Weather increases nonzero coverage
and darkens the layers. Cloud color accepts `#RRGGBB` or an RGB integer; omitted
or `-1` uses the profile's tint. Height defaults to 192 (range 64â€“512); clouds
fade out over the next 96 blocks during ascent. Speed defaults to 0.35 (0â€“4),
and direction to 45 degrees (0â€“360). Seed is an integer controlling stable
variation. Heights and pressure jointly determine atmospheric visibility.

Haze, sunset and stars default to 1 (range 0â€“2). Aurora and nebula default to 0
(range 0â€“1). Auroras require atmosphere and darkness, independently of rings.
Nebula and galactic-band orientation is shared within a star system. Airless worlds have no
atmospheric clouds or auroras. All numeric settings reject non-finite values.
These settings persist with the planet and synchronize to clients; existing
saved planets retain their settings under the normal definition-loading rules.

The client `advancedVfx`, `effectDetail` and `reducedMotion` controls also apply.
Detail 0 suppresses cloud/night decoration, detail 1 uses one cloud layer, and
detail 2 adds wisps. Reduced motion freezes wind and aurora animation; normal
celestial motion and camera parallax remain. Sky override switches still apply.

Shader packs can add their own clouds, fog and sky effects. The tested
Complementary configuration disables its cloud, aurora, atmospheric/border/bloom
fog and light-shaft layers so planetary settings own those effects; terrain
lighting and shadows remain shader-driven. The optional Iris 1.8 compatibility
bridge enables only this mod's planet shader programs. Other Iris versions or
shader packs may need separate validation; unsupported bridge versions log a
warning and use the available vanilla-program fallback.

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
| `adv_rocketry:planet_biomes/excluded`, `high_pressure`, `single`, `airless`, `scorched`, `caves` | biome | Automatic planet biome selection |
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

## Client effect resource packs

Visual profiles belong in resource packs at `assets/adv_rocketry/effects/<effect>.json`.
They reload with client resources and do not change server gameplay. Supported IDs are
`hologram`, `rocket`, `laser`, `railgun`, `blackhole`, `terraformer`, `gravity`, `etcher`,
`planet`, `celestial`, `elevator`, `materials`, `diagnostics`, `hud`, `transport`, `audio`, and `solar_array`.

| Field | Default | Bounds |
|---|---|---|
| `enabled` | `true` | Boolean |
| `color` | `7401434` (cyan RGB) | Integer, 0Ã¢â‚¬â€œ16777215 |
| `opacity` | `0.65` | 0Ã¢â‚¬â€œ1 |
| `speed` | `1.0` | 0Ã¢â‚¬â€œ4 |
| `segments` | `32` | 8Ã¢â‚¬â€œ64 |

Color, opacity and speed apply to procedural visuals; audio currently uses `enabled`.
The solar array pulses its green output indicators and sends a cyan sweep along the
connecting bar while generating power. Its profile color controls the bar; reduced
motion freezes both animations, and disabling advanced VFX removes the overlays.
Client configuration offers `advancedVFX`, `reducedMotion`, `singularityDistortion`, and
`effectDetail` (0 = low, 1 = medium, 2 = high). Low detail disables procedural clouds;
scene distortion requires high detail. Shared effect geometry allows up to 64, 128, or 256
draw reservations per frame and caps ring segments at 16, 32, or 64 respectively.
Reduced motion freezes procedural movement and suppresses exhaust particles and lens distortion.
The singularity lens handles at most four nearby sources and uses scene depth to retain foreground
occlusion. With Iris installed, it retains the geometry effect and skips scene distortion.
Hold Shift with a holographic projector to separate the construction preview into layers;
Shift-scroll still selects a layer.

### Starfields and galactic light

`star_density` defaults to 1 (range 0–4). At the default, detail levels 0/1/2
use 10,000/24,000/48,000 stars across the entire sphere, with extra density in
the galactic plane. `stars` controls visibility independently of density.
`twinkle` defaults to 0.6 and `distortion` to 0.5 (both range 0–1).
Distortion and chromatic scintillation fade with atmosphere; reduced motion
freezes both effects. Twinkle remains subtle in vacuum.

`galaxy` defaults to 0.65 (range 0–2) and controls the dusty galactic band;
`nebula` adds diffuse blue/violet clouds. Detail 0 or disabled advanced VFX
omits these diffuse layers while retaining the starfield.

Defined non-black-hole systems appear along their relative map X/Z directions,
with color from stellar temperature and apparent brightness scaled by size
and distance. Coincident systems are omitted. The band core points toward the
map's system centroid. The shared plane is tilted for presentation. Dust lanes,
arm-like clouds and background stars are procedural decoration: the map does
not define a three-dimensional physical galaxy. Cached geometry rebuilds when
the system catalog, observer, density or detail changes, and on resource reload.

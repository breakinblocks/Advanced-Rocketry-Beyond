# Biome material uses

This audit covers the 37 blocks registered by `BiomeMaterials` and the four legacy bluewood blocks (`alien_*`). All have block loot tables. It does not cover every machine or item in the mod.

| Materials | Harvest and existing uses | Renewal |
| --- | --- | --- |
| weathered_basalt, cooling_crust, mineral_crust, strike_glass, lunar_breccia, dark_regolith, banded_stone, wet_slate, glacial_stone, porous_stone | Pickaxe; drop themselves. Building blocks and stone crafting/tool materials. Cooling crust emits light. Strike glass also smelts to glass. | Finite terrain deposits |
| rooted_loam, alluvial_silt, volcanic_ash, luminous_moss | Drop themselves; shovel is fastest. Building/landscaping; ash and moss make dyes, moss emits light. Silt centrifuges into clay. | No reproduction mechanic |
| rootwood_log, stripped_rootwood_log, rootwood_planks | Drop themselves; axe is fastest. Logs craft into four planks or saw into six; wood tags support ordinary wood recipes and fuel. Logs can be stripped. | Sapling-grown trees |
| alien_log, alien_planks (bluewood) | Drop themselves; axe is fastest. Four crafted or six sawn planks per log; charcoal and ordinary wood recipes/fuel. | Sapling-grown trees |
| rootwood_leaves, alien_leaves | Shears/Silk Touch collect leaves; otherwise vanilla sapling chances, increased by Fortune. Compostable. | Sapling-grown trees |
| rootwood_sapling, alien_sapling | Drop themselves; plant to grow trees or compost. | Tree leaf drops |
| fan_fern, spiral_seedling, forest_groundcover, fallen_foliage, shelf_fungus, floating_leaves, aquatic_frond, salt_shrub | Drop themselves without a tool; decoration, dye and compost. Shelf fungus emits light. | Renewable with bone meal on suitable support |
| hanging_roots, jointed_reeds, marsh_sedge | Drop themselves without a tool; decoration, dye, compost and fiber. Reeds/sedge also make paper. | Renewable with bone meal on suitable support |
| luminous_seed_cluster, cave_blossom | Drop themselves without a tool; lighting, dye, compost and glowstone extraction. | Renewable with bone meal on suitable support |
| vent_growth, ember_curl, storm_blades, spark_mushroom, mineral_bloom | Drop themselves without a tool; decoration and dye. All except storm blades emit light. Spark mushrooms yield glowstone; mineral blooms yield quartz. These heat/mineral plants are not compostable. | Renewable with bone meal on suitable support |

Plants still require their native placement conditions: ceiling or wall support, suitable soil, water surfaces or submerged support as applicable. Harvesting does not make them freely placeable everywhere.

## Processing yields

| Input | Process | Output |
| --- | --- | --- |
| 3 jointed reeds, marsh sedge, or hanging roots (one species per batch) | Cutting machine, 80 ticks, 10 energy/tick | 2 string |
| 3 jointed reeds or marsh sedge (one species in a horizontal row) | Crafting | 3 paper |
| 4 luminous seed clusters, cave blossoms, or spark mushrooms (one species per batch) | Centrifuge, 200 ticks, 20 energy/tick | 1 glowstone dust |
| 4 alluvial silt | Centrifuge, 200 ticks, 20 energy/tick | 4 clay balls |
| 1 strike glass | Smelting, 200 ticks | 1 glass |
| 4 mineral blooms | Centrifuge, 200 ticks, 20 energy/tick | 1 quartz |

The existing hand recipe remains three jointed reeds for one string. Extraction consumes the input; none of these outputs crafts back into its source. Recipes use individual species deliberately, so unrelated plants cannot substitute for the habitat reward. All recipe IDs are under `adv_rocketry:biomes/` and can be replaced by datapacks.

## Plant cultivation

Bone meal on an established plant grows one offspring in an adjacent empty space, preserving its attachment and waterlogged state. A full or unsuitable habitat does not consume bone meal. Plants do not spread automatically.

- Floor plants require their normal soil; floating plants require water below, and aquatic fronds require a water source with solid support below.
- Ceiling plants spread across supported ceiling positions, and wall fungi spread along their existing wall plane.
- Heat plants reproduce on magma blocks, cooling crust, or weathered basalt.
- Mineral blooms reproduce on mineral crust, calcite, or amethyst blocks.
- Salt shrubs reproduce on sand-tagged blocks or alluvial silt.

These conditions apply to the offspring location. Ordinary placement rules remain unchanged, so decorative specimens on other sturdy blocks can still be kept.

## Local plant ambience

Root and fungal caverns have occasional water drips, mineral caves and crystal chasms have sparse crystal chimes, and volcanic habitats have occasional basalt-delta ambience. These biome sounds use Minecraft's ambient sound channel and can be adjusted through the generated biome datapack definitions. The probabilities are per client tick while the biome is active: .0015 for drips, .0008 for chimes, and .001 for volcanic additions. They are intermittent accents, not continuous loops.

Luminous seed clusters shed occasional spores; cave blossoms and spark mushrooms emit soft glints. Mineral blooms produce golden flecks, vent growth produces tiny flames, and ember curls shed ash. Waterlogged aquatic fronds release bubbles. These are local cosmetic particles controlled by the client's particle settings; they do not spread fire or change growth and drops.

## Habitat deposits

New planets using generation version 9 contain small exposed pockets in native AR biomes:

| Habitat | Host material | Deposit and use |
| --- | --- | --- |
| Alien rivers, marshes, deep swamps | Alluvial silt | Clay for bricks and terracotta |
| Mineral caves | Mineral crust | Calcite for building and mineral bloom cultivation |
| Stormland | Strike glass | Fused glass; collect with Silk Touch |
| Volcanic | Cooling crust | Magma for building, bubble columns and heat-plant cultivation |

Pockets check the biome at their actual elevation, including underground habitats. Each chunk has four candidate columns with a one-in-six chance per eligible exposed layer; each successful pocket replaces at most nine host blocks. Buried ores, other substrates and optional mod biomes are not replaced. Existing planets retain their saved generation version and do not gain these deposits automatically.

## Remaining work

- Most terrain blocks remain building and stone-crafting materials; this pass adds selective utility without making every rock an ore.

---
navigation:
  title: Sealed habitats
  icon: adv_rocketry:oxygen_vent
  position: 31
  parent: life_support.md
item_ids:
  - adv_rocketry:oxygen_vent
  - adv_rocketry:carbon_scrubber
  - adv_rocketry:carbon_scrubber_cartridge
  - adv_rocketry:airlock_door
  - adv_rocketry:pipe_sealer
  - adv_rocketry:seal_detector
---

# Sealed habitats

Enclose the room with sealing walls, floor and ceiling. Supply an Oxygen Vent inside with oxygen and FE. Wait for its status to confirm breathable air before removing your suit.

Use the Seal Detector on suspect blocks. Open doors and gaps can break the seal; use Airlock Doors for entrances. The vent's allowed room size is configurable.

A Carbon Scrubber beside the vent uses cartridges to reduce oxygen consumption at increased power cost. Cartridge wear can be changed by the server.

Place **four Pipe Sealers around a one-block opening**. The center may contain an energy cable, fluid pipe or item conduit. Removing part of the frame breaks this virtual seal.

Confirm air with the [Atmosphere Analyzer](atmospheres.md). Keep backup oxygen before opening walls or changing pipe routes.

## Finding a leak

A vent that stays idle with a room volume of 0 has found a way out. It traces the air back to the gap it escaped through:

- **Look at the vent.** Jade shows the leak's coordinates under the room volume.
- **Hold a Seal Detector or an Atmosphere Analyzer**, or wear a helmet with the analyzer module. A red marker pulses on the gap and an orange trail of particles runs from the vent to it, out to about 48 blocks.
- **Sneak and use the Seal Detector or Atmosphere Analyzer on the vent.** Chat gives the gap's coordinates and the block there, for example an open Airlock Door, a missing wall block or a pipe without a full Pipe Sealer frame.

Fix the gap and the vent rescans within a second. A large opening, such as a missing wall section, is marked at its edge, so look around the marker for the rest of the hole.

## Crafting

### Oxygen Vent

<RecipeFor id="adv_rocketry:oxygen_vent" fallbackText="No crafting recipe is enabled for this item on this server." />

### Carbon Scrubber

<RecipeFor id="adv_rocketry:carbon_scrubber" fallbackText="No crafting recipe is enabled for this item on this server." />

### Carbon Scrubber Cartridge

<RecipeFor id="adv_rocketry:carbon_scrubber_cartridge" fallbackText="No crafting recipe is enabled for this item on this server." />

### Airlock Door

<RecipeFor id="adv_rocketry:airlock_door" fallbackText="No crafting recipe is enabled for this item on this server." />

### Pipe Sealer

<RecipeFor id="adv_rocketry:pipe_sealer" fallbackText="No crafting recipe is enabled for this item on this server." />

### Seal Detector

<RecipeFor id="adv_rocketry:seal_detector" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing Carbon Scrubber Cartridge

<MachineRecipes item="adv_rocketry:carbon_scrubber_cartridge" kind="processing" />

[Back to Life support](life_support.md)

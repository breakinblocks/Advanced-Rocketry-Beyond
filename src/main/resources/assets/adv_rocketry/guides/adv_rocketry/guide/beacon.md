---
navigation:
  title: Beacons and finding locations
  icon: adv_rocketry:beacon
  position: 58
  parent: worlds.md
item_ids:
  - adv_rocketry:beacon
  - adv_rocketry:beacon_finder
---

# Beacons and finding locations

Build the beacon column with a redstone block on top. An active beacon marks a planetary location.

Use a held Beacon Finder for the nearest beacon’s distance and direction, or install it in a compatible helmet for directional markers. It finds beacons, not arbitrary landing pads. Mark a surface base before exploring away from your rocket.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/beacon.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Beacon</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 1" max="2 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="2 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 4 1" max="2 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Redstone Block</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 5 × 3 blocks** (width × height × depth), containing **9 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Beacon | 1 |
| Machine Structure | 7 |
| Redstone Block | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Beacon

<RecipeFor id="adv_rocketry:beacon" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing Beacon Finder

<MachineRecipes item="adv_rocketry:beacon_finder" kind="processing" />

[Back to Planets and equipment](worlds.md)

---
navigation:
  title: Space Elevator
  icon: adv_rocketry:space_elevator
  position: 43
  parent: stations.md
item_ids:
  - adv_rocketry:space_elevator
  - adv_rocketry:elevator_chip
---

# Space Elevator

Build one elevator on a planet and another on a station orbiting it. Pair the controllers with a Linker.

Set station altitude to **177–181**, all rotation velocities to **zero**, and X/Z tilt within **10.8 degrees** of level. The station must not already be anchored. Set these values with [station controllers](station_controls.md) before linking.

Linking anchors the station. Supply FE through the frame's plugs; a trip needs **50,000 FE**. Use the controls and capsule to travel. Sneak-use the controller with a Linker to unlink it before warping or changing orbit.

## Build example

<GameScene zoom="1.20" interactive={true}>
  <ImportStructure src="structures/space_elevator.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="3 0 0" max="4 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="5 0 0" max="6 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="4 0 0" max="5 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Space Elevator</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="8 0 1" max="9 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 0 1" max="6 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 0 2" max="8 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="7 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="1 0 3" max="2 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 3" max="6 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 3" max="8 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 3" max="3 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 0 3" max="7 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 4" max="3 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="6 0 4" max="9 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 4" max="6 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 5" max="3 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="6 0 5" max="9 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 5" max="4 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 0 5" max="6 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 5" max="5 1 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 6" max="3 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="6 0 6" max="9 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 6" max="6 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 7" max="2 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 7" max="6 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 7" max="8 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 7" max="3 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 0 7" max="7 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 8" max="2 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 0 8" max="8 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 8" max="7 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="0 0 9" max="1 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="8 0 9" max="9 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 0 9" max="6 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
</GameScene>

This north-facing example is **9 × 1 × 10 blocks** (width × height × depth), containing **68 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Advanced Machine Structure | 16 |
| Power Input Plug | 2 |
| Motor | 1 |
| Space Elevator | 1 |
| Steel Block | 4 |
| Prismarine Slab | 44 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Space Elevator

<RecipeFor id="adv_rocketry:space_elevator" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing Elevator Chip

<MachineRecipes item="adv_rocketry:elevator_chip" kind="processing" />

[Back to Space stations](stations.md)

---
navigation:
  title: Black-Hole Generator
  icon: adv_rocketry:black_hole_generator
  position: 45
  parent: stations.md
item_ids:
  - adv_rocketry:black_hole_generator
---

# Black-Hole Generator

This works on a station orbiting a **black hole**. Complete the five-layer frame, supply items through a service input, and connect power outputs.

It produces **500 FE/t** while burning items in the correct orbit. Check orbit, input and output capacity if idle. The example uses item-input and energy-output ports in the flexible positions.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/black_hole_generator.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Black Hole Generator</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 0" max="2 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 1" max="2 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="2 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 4 1" max="2 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 1 1" max="3 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Output Plug</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="2 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Output Plug</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 5 × 3 blocks** (width × height × depth), containing **10 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Black Hole Generator | 1 |
| Advanced Machine Structure | 6 |
| Power Output Plug | 2 |
| Input Hatch | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Black Hole Generator

<RecipeFor id="adv_rocketry:black_hole_generator" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Space stations](stations.md)

---
navigation:
  title: Rolling Machine
  icon: adv_rocketry:rolling_machine
  position: 14
  parent: manufacturing.md
item_ids:
  - adv_rocketry:rolling_machine
---

# Rolling Machine

Rolls ingots into plates and plates into sheets. Some recipes also require water in the fluid input. Check the listed amount: dry item inputs cannot start a wet recipe.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/rolling_machine.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 1 1" max="5 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 1 2" max="5 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="2 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="4 0 0" max="5 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="3 0 0" max="4 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Rolling Machine</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 1" max="5 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="4 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 2" max="5 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 0 2" max="4 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 2 × 3 blocks** (width × height × depth), containing **23 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Fluid Input Hatch | 1 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 2 |
| Machine Structure | 13 |
| Rolling Machine | 1 |
| Steel Block | 3 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Rolling Machine

<RecipeFor id="adv_rocketry:rolling_machine" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="rolling_machine" kind="processing" />

[Back to Manufacturing](manufacturing.md)

---
navigation:
  title: Precision Assembler
  icon: adv_rocketry:precision_assembler
  position: 19
  parent: manufacturing.md
item_ids:
  - adv_rocketry:precision_assembler
---

# Precision Assembler

Assembles circuits and precision components. Its flexible lower casing accepts service ports. The example has two item inputs, an item output and a power input; add suitable ports where allowed for recipes needing more inventory space.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.00" interactive={true}>
  <ImportStructure src="structures/precision_assembler.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 2 0" max="4 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="4 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="4 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 1 0" max="4 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="3 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 1 1" max="4 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="4 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 0 0" max="4 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Precision Assembler</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="3 0 1" max="4 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 0 2" max="4 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="3 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
</GameScene>

This north-facing example is **4 × 3 × 3 blocks** (width × height × depth), containing **34 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Copper Coil | 2 |
| Power Input Plug | 1 |
| Input Hatch | 2 |
| Output Hatch | 1 |
| Motor | 2 |
| Machine Structure | 23 |
| Precision Assembler | 1 |
| Glass | 2 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Precision Assembler

<RecipeFor id="adv_rocketry:precision_assembler" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="precision_assembler" kind="processing" />

[Back to Manufacturing](manufacturing.md)

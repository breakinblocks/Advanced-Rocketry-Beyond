---
navigation:
  title: Electrolyzer
  icon: adv_rocketry:electrolyzer
  position: 16
  parent: manufacturing.md
item_ids:
  - adv_rocketry:electrolyzer
---

# Electrolyzer

Splits fluids into gases. Water processing supplies hydrogen and oxygen for fuel and life support. Both products need output capacity: a full oxygen hatch can also stop hydrogen production.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/electrolyzer.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="2 1 1" max="3 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Electrolyzer</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 2 × 2 blocks** (width × height × depth), containing **9 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Copper Coil | 1 |
| Power Input Plug | 2 |
| Fluid Input Hatch | 1 |
| Fluid Output Hatch | 2 |
| Machine Structure | 2 |
| Electrolyzer | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Electrolyzer

<RecipeFor id="adv_rocketry:electrolyzer" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="electrolyzer" kind="processing" />

[Back to Manufacturing](manufacturing.md)

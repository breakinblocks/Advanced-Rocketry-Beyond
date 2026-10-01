---
navigation:
  title: Crystallizer
  icon: adv_rocketry:crystallizer
  position: 15
  parent: manufacturing.md
item_ids:
  - adv_rocketry:crystallizer
---

# Crystallizer

Grows silicon boules and other crystals. Quartz crucibles form the upper bath. Supply the specified item and fluid inputs and use matching output hatches. Cut silicon boules into wafers in the Cutting Machine.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/crystallizer.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 1 0" max="3 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Quartz Crucible</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="3 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Quartz Crucible</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Crystallizer</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 2 × 2 blocks** (width × height × depth), containing **12 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Fluid Input Hatch | 1 |
| Fluid Output Hatch | 1 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Crystallizer | 1 |
| Quartz Crucible | 6 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Crystallizer

<RecipeFor id="adv_rocketry:crystallizer" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="crystallizer" kind="processing" />

[Back to Manufacturing](manufacturing.md)

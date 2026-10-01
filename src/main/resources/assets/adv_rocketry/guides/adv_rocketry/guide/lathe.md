---
navigation:
  title: Lathe
  icon: adv_rocketry:lathe
  position: 13
  parent: manufacturing.md
item_ids:
  - adv_rocketry:lathe
---

# Lathe

Turns metal ingots into rods and shaped components. Supply its item input and keep the output clear. Rods feed motors, tools and advanced parts.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.00" interactive={true}>
  <ImportStructure src="structures/lathe.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="3 1 0" max="4 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Lathe</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="3 0 0" max="4 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="3 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
</GameScene>

This north-facing example is **4 × 2 × 1 blocks** (width × height × depth), containing **7 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 2 |
| Lathe | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Lathe

<RecipeFor id="adv_rocketry:lathe" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="lathe" kind="processing" />

[Back to Manufacturing](manufacturing.md)

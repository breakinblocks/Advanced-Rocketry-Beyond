---
navigation:
  title: Centrifuge
  icon: adv_rocketry:centrifuge
  position: 21
  parent: manufacturing.md
item_ids:
  - adv_rocketry:centrifuge
---

# Centrifuge

Separates fluids, including enriched lava. Enriched-lava products use the server-configured weighted ore pool. Percentages are chances per roll, not guaranteed stacks. Leave room for all possible item and fluid outputs.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.00" interactive={true}>
  <ImportStructure src="structures/centrifuge.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 3 0" max="2 4 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 3 0" max="1 4 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="3 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="0 3 1" max="1 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 2" max="3 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="1 2 0" max="2 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 0" max="1 3 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 2 1" max="3 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="1 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 2" max="3 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="1 3 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Centrifuge</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="3 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="3 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Centrifuge Casing</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="3 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 4 × 3 blocks** (width × height × depth), containing **33 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Fluid Input Hatch | 1 |
| Fluid Output Hatch | 7 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 9 |
| Centrifuge | 1 |
| Centrifuge Casing | 12 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Centrifuge

<RecipeFor id="adv_rocketry:centrifuge" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="centrifuge" kind="processing" />

[Back to Manufacturing](manufacturing.md)

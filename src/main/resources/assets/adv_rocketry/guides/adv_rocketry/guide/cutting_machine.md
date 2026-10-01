---
navigation:
  title: Cutting Machine
  icon: adv_rocketry:cutting_machine
  position: 18
  parent: manufacturing.md
item_ids:
  - adv_rocketry:cutting_machine
---

# Cutting Machine

Cuts silicon boules into wafers and processes wood. The saw blade assembly belongs in the structure. Any separate reusable ingredient still has to be present. The server may disable vanilla wood recipes.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/cutting_machine.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Cutting Machine</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Saw Blade Assembly</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 1 × 2 blocks** (width × height × depth), containing **6 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 1 |
| Cutting Machine | 1 |
| Saw Blade Assembly | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Cutting Machine

<RecipeFor id="adv_rocketry:cutting_machine" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="cutting_machine" kind="processing" />

[Back to Manufacturing](manufacturing.md)

---
navigation:
  title: Electric Arc Furnace
  icon: adv_rocketry:electric_arc_furnace
  position: 22
  parent: manufacturing.md
item_ids:
  - adv_rocketry:electric_arc_furnace
---

# Electric Arc Furnace

Smelts and alloys materials in a blast-brick enclosure. Keep its interior hollow and install the shown coils and power inputs. Flexible bottom positions accept hatches; this example has two inputs and one output for alloy production.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/electric_arc_furnace.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 4 1" max="2 5 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="3 4 1" max="4 5 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="2 4 1" max="3 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="1 4 2" max="4 5 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="1 4 3" max="2 5 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="3 4 3" max="4 5 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="2 4 3" max="3 5 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 3 0" max="4 4 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 3 1" max="1 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 3 1" max="5 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="2 4 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 3 1" max="4 4 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="0 3 2" max="1 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 3 2" max="5 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 3 3" max="1 4 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 3 3" max="5 4 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="2 3 3" max="3 4 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="1 3 4" max="4 4 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 2 0" max="5 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="1 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 2 1" max="5 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="1 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 2 2" max="5 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 2 3" max="1 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 2 3" max="5 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 2 4" max="5 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="4 1 0" max="5 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="3 1 0" max="4 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Electric Arc Furnace</BoxAnnotation>
  <BoxAnnotation min="4 1 1" max="5 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="4 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="5 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 1 3" max="5 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 1 4" max="5 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="5 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="5 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="5 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 0 3" max="5 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
  <BoxAnnotation min="0 0 4" max="5 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Blast Brick</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 5 × 5 blocks** (width × height × depth), containing **90 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Blast Brick | 79 |
| Copper Coil | 3 |
| Power Input Plug | 4 |
| Input Hatch | 2 |
| Output Hatch | 1 |
| Electric Arc Furnace | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Electric Arc Furnace

<RecipeFor id="adv_rocketry:electric_arc_furnace" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="electric_arc_furnace" kind="processing" />

[Back to Manufacturing](manufacturing.md)

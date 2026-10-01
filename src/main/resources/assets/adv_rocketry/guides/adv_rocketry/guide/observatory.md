---
navigation:
  title: Observatory
  icon: adv_rocketry:observatory
  position: 51
  parent: satellites.md
item_ids:
  - adv_rocketry:observatory
  - adv_rocketry:asteroid_chip
---

# Observatory

Complete the telescope and keep its required sky view open. The service rim accepts energy inputs and data buses; this example includes both.

Supply distance data to find asteroid targets. With the telescope open, spend **500 FE** to program a chip for the selected target. Composition and mass data refine cargo estimates.

Research the chip in the [Astrobody Data Processor](astrobody_data_processor.md) before preparing a mining rocket.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/observatory.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 4 1" max="2 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 4 1" max="4 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 4 1" max="3 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 4 2" max="4 5 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 4 3" max="4 5 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="4 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 2" max="2 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 3 2" max="4 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 3 2" max="3 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 3 3" max="4 4 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 0" max="4 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="1 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 1" max="5 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="1 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 2" max="5 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 3" max="1 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 3" max="5 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 2 3" max="3 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 2 4" max="4 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="3 1 0" max="4 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Observatory</BoxAnnotation>
  <BoxAnnotation min="4 1 1" max="5 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="4 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 1 2" max="5 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="4 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 3" max="1 2 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 1 3" max="5 2 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 1 3" max="4 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 4" max="4 2 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="4 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 1" max="5 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="4 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 2" max="5 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="3 0 2" max="4 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 3" max="1 1 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 3" max="5 1 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 3" max="4 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="1 0 4" max="4 1 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 5 × 5 blocks** (width × height × depth), containing **73 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Input Hatch | 2 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 37 |
| Data Bus | 19 |
| Observatory | 1 |
| Structure Tower | 8 |
| Glass | 3 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Observatory

<RecipeFor id="adv_rocketry:observatory" fallbackText="No crafting recipe is enabled for this item on this server." />

### Asteroid Chip

<RecipeFor id="adv_rocketry:asteroid_chip" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

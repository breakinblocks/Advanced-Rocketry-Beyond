---
navigation:
  title: Precision Laser Etcher
  icon: adv_rocketry:precision_laser_etcher
  position: 20
  parent: manufacturing.md
item_ids:
  - adv_rocketry:precision_laser_etcher
---

# Precision Laser Etcher

Etches circuit plates using the vacuum laser. Keep the interior air spaces clear. Supply any reusable lens requested by the recipe: “not consumed” does not mean optional.

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/precision_laser_etcher.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 2 0" max="3 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="1 2 1" max="2 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="3 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 1 2" max="3 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Precision Laser Etcher</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 3 × 3 blocks** (width × height × depth), containing **22 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 2 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 6 |
| Precision Laser Etcher | 1 |
| Structure Tower | 2 |
| Vacuum Laser | 1 |
| Prismarine Slab | 7 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Precision Laser Etcher

<RecipeFor id="adv_rocketry:precision_laser_etcher" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="precision_laser_etcher" kind="processing" />

[Back to Manufacturing](manufacturing.md)

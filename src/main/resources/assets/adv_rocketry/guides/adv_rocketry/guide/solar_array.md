---
navigation:
  title: Solar power
  icon: adv_rocketry:solar_panel
  position: 24
  parent: automation.md
item_ids:
  - adv_rocketry:solar_panel
  - adv_rocketry:solar_array
  - adv_rocketry:solar_array_panel
---

# Solar power

Standalone Solar Panels generate power according to stellar distance and atmosphere. Output can differ between planets.

A Solar Array has a controller with a Power Output Plug on either side and rows of **three Solar Array Panels** behind it. The example has three rows; extend the strip to **21 panel rows**, making **22 blocks overall** including the controller row. Solar Array Panels are different from standalone Solar Panels.

Connect consumers to the output plugs, keep panels exposed to the sky, and check the controller if generation is low.

## Build example

<GameScene zoom="3.00" interactive={true}>
  <ImportStructure src="structures/solar_array.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Solar Array</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Output Plug</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Output Plug</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Array Panel</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="3 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Array Panel</BoxAnnotation>
  <BoxAnnotation min="0 0 3" max="3 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Array Panel</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 1 × 4 blocks** (width × height × depth), containing **12 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Output Plug | 2 |
| Solar Array | 1 |
| Solar Array Panel | 9 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Solar Panel

<RecipeFor id="adv_rocketry:solar_panel" fallbackText="No crafting recipe is enabled for this item on this server." />

### Solar Array

<RecipeFor id="adv_rocketry:solar_array" fallbackText="No crafting recipe is enabled for this item on this server." />

### Solar Array Panel

<RecipeFor id="adv_rocketry:solar_array_panel" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Power and automation](automation.md)

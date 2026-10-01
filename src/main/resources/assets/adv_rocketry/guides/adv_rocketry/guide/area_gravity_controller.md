---
navigation:
  title: Area Gravity Controller
  icon: adv_rocketry:area_gravity_controller
  position: 46
  parent: stations.md
item_ids:
  - adv_rocketry:area_gravity_controller
---

# Area Gravity Controller

Place the controller above its power input and cross-shaped advanced-structure frame. Supply FE and choose the pull direction and strength.

It affects nearby entities rather than the whole station. Test with a clear landing area, especially when pulling sideways or upward.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/area_gravity_controller.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Area Gravity Controller</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 2 × 3 blocks** (width × height × depth), containing **6 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Area Gravity Controller | 1 |
| Advanced Machine Structure | 4 |
| Power Input Plug | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Area Gravity Controller

<RecipeFor id="adv_rocketry:area_gravity_controller" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Space stations](stations.md)

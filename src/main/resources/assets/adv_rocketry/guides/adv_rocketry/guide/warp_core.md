---
navigation:
  title: Warp Core
  icon: adv_rocketry:warp_core
  position: 41
  parent: stations.md
item_ids:
  - adv_rocketry:warp_core
---

# Warp Core

Complete the core frame near the station's Warp Controller. Supply dilithium and FE.

Select a destination at the controller or planet selector, check cost and readiness, then initiate travel. Fuel use and transit time are configurable. See [Warp travel](warp_travel.md).

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/warp_core.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 2 0" max="3 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="1 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="2 2 1" max="3 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="1 2 1" max="2 3 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="3 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 1 1" max="3 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Gold Block</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="2 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Warp Core</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Gold Block</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="3 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 3 × 3 blocks** (width × height × depth), containing **23 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Input Hatch | 1 |
| Machine Structure | 4 |
| Titanium Block | 15 |
| Warp Core | 1 |
| Gold Block | 2 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Warp Core

<RecipeFor id="adv_rocketry:warp_core" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Space stations](stations.md)

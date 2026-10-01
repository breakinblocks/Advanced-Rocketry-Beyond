---
navigation:
  title: Microwave power
  icon: adv_rocketry:microwave_receiver
  position: 50
  parent: satellites.md
item_ids:
  - adv_rocketry:microwave_receiver
  - adv_rocketry:microwave_transmitter
---

# Microwave power

Build and deploy a satellite with a Microwave Transmitter, power modules and storage. Keep its programmed chip.

Build the receiver's **5×5 panel layout** and insert the linked chip. Use standalone Solar Panels, not Solar Array Panels. The receiver draws stored satellite energy and exports FE to adjacent consumers.

Output depends on available orbital power and server settings. Check the link and satellite storage if it stops.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/microwave_receiver.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Microwave Receiver</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="4 0 0" max="5 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="0 0 4" max="1 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="4 0 4" max="5 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="4 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="3 0 2" max="4 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
  <BoxAnnotation min="1 0 3" max="4 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Solar Panel</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 1 × 5 blocks** (width × height × depth), containing **13 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Microwave Receiver | 1 |
| Solar Panel | 12 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Microwave Receiver

<RecipeFor id="adv_rocketry:microwave_receiver" fallbackText="No crafting recipe is enabled for this item on this server." />

### Microwave Transmitter

<RecipeFor id="adv_rocketry:microwave_transmitter" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

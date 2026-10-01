---
navigation:
  title: Biome Scanner
  icon: adv_rocketry:biome_scanner
  position: 56
  parent: satellites.md
item_ids:
  - adv_rocketry:biome_scanner
---

# Biome Scanner

Build this on a station above its motor, dish and redstone-block base. Keep the shaft unobstructed.

It reads the biome catalog of the planet below. Use the results for exploration and biome-changing plans; it is not a substitute for a surface ore scan.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/biome_scanner.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="2 3 2" max="3 4 3" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Biome Scanner</BoxAnnotation>
  <BoxAnnotation min="2 2 2" max="3 3 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Redstone Block</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="2 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="3 1 1" max="5 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="4 1 2" max="5 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="0 1 3" max="2 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="3 1 3" max="5 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="4 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="2 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="3 1 2" max="4 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="1 1 4" max="4 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Aluminum Block</BoxAnnotation>
  <BoxAnnotation min="2 1 1" max="3 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="2 1 2" max="3 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 1 3" max="3 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 4 × 5 blocks** (width × height × depth), containing **24 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Aluminum Block | 16 |
| Biome Scanner | 1 |
| Motor | 1 |
| Machine Structure | 1 |
| Structure Tower | 4 |
| Redstone Block | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Biome Scanner

<RecipeFor id="adv_rocketry:biome_scanner" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

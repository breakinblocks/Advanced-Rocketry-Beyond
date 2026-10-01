---
navigation:
  title: Orbital Laser
  icon: adv_rocketry:orbital_laser
  position: 44
  parent: stations.md
item_ids:
  - adv_rocketry:orbital_laser
---

# Orbital Laser

Build this on a station and keep the shown shaft and air spaces clear. It mines the planet below and delivers products to output hatches.

The default cost is **10,000 FE per operation**. Void-drill mode uses a configured ore table with cobblestone fallback. Terrain mode excavates an actual **3×3 shaft** and collects the drops.

Choose valid coordinates and keep storage clear. Servers can exclude planets and change output tables or cost. Terrain mode changes the world below, so choose the target deliberately.

## Build example

<GameScene zoom="1.09" interactive={true}>
  <ImportStructure src="structures/orbital_laser.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="9 2 3" max="10 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 2 4" max="11 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="9 2 5" max="10 3 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 0" max="5 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="4 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="6 1 1" max="7 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 1" max="6 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="4 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="4 1 2" max="7 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="4 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="8 1 3" max="9 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="10 1 3" max="11 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 3" max="5 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="9 1 3" max="10 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 1 3" max="7 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 1 3" max="4 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="6 1 4" max="7 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 1 4" max="11 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 1 4" max="8 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="8 1 5" max="9 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="10 1 5" max="11 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 5" max="5 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="9 1 5" max="10 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 1 5" max="7 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 1 5" max="4 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="4 1 6" max="7 2 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 6" max="4 2 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 1 6" max="1 2 7" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="6 1 7" max="7 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 7" max="6 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 1 7" max="4 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 1 7" max="1 2 8" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="4 1 8" max="5 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 8" max="4 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="4 0 0" max="5 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="4 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="6 0 1" max="7 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 1" max="6 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="4 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="8 0 2" max="9 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="10 0 2" max="11 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="9 0 2" max="10 1 3" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Orbital Laser</BoxAnnotation>
  <BoxAnnotation min="4 0 2" max="7 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="4 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="8 0 3" max="11 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 0 3" max="7 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="4 0 3" max="5 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 3" max="4 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="8 0 4" max="11 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 0 4" max="8 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="6 0 4" max="7 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 0 5" max="11 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 0 5" max="7 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="4 0 5" max="5 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 5" max="4 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="4 0 6" max="7 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 6" max="4 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 0 6" max="1 1 7" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="6 0 7" max="7 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 7" max="6 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Lens Block</BoxAnnotation>
  <BoxAnnotation min="1 0 7" max="4 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
  <BoxAnnotation min="0 0 7" max="1 1 8" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="4 0 8" max="5 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 8" max="4 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Vacuum Laser</BoxAnnotation>
</GameScene>

This north-facing example is **11 × 3 × 9 blocks** (width × height × depth), containing **122 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Advanced Machine Structure | 36 |
| Power Input Plug | 8 |
| Output Hatch | 2 |
| Machine Structure | 13 |
| Lens Block | 14 |
| Orbital Laser | 1 |
| Vacuum Laser | 48 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Orbital Laser

<RecipeFor id="adv_rocketry:orbital_laser" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Space stations](stations.md)

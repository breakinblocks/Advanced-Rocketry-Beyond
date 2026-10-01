---
navigation:
  title: Railgun cargo transport
  icon: adv_rocketry:railgun
  position: 47
  parent: stations.md
item_ids:
  - adv_rocketry:railgun
---

# Railgun cargo transport

Build both eleven-layer frames and pair them with a Linker. Railguns transfer item stacks within the same planetary system.

Supply sending items and FE, and leave receiver capacity. Check minimum stack size and redstone mode: small stacks or unmet conditions wait instead of launching. Keep endpoints loaded and connect local conduits to their hatches.

## Build example

<GameScene zoom="1.09" interactive={true}>
  <ImportStructure src="structures/railgun.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="4 10 3" max="5 11 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 10 4" max="4 11 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 10 4" max="6 11 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 10 4" max="5 11 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 10 5" max="5 11 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 9 3" max="5 10 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 9 4" max="4 10 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 9 4" max="6 10 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 9 4" max="5 10 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 9 5" max="5 10 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 8 3" max="5 9 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 8 4" max="4 9 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 8 4" max="6 9 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 8 4" max="5 9 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 8 5" max="5 9 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 7 3" max="5 8 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 7 4" max="4 8 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 7 4" max="6 8 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 7 4" max="5 8 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 7 5" max="5 8 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 6 3" max="5 7 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 6 4" max="4 7 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 6 4" max="6 7 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 6 4" max="5 7 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 6 5" max="5 7 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 5 3" max="5 6 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 5 4" max="4 6 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 5 4" max="6 6 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 5 4" max="5 6 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 5 5" max="5 6 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 4 3" max="5 5 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 4 4" max="4 5 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 4 4" max="6 5 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 4 4" max="5 5 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 4 5" max="5 5 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 3 3" max="5 4 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 3 4" max="4 4 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 3 4" max="6 4 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 3 4" max="5 4 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 3 5" max="5 4 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 2 3" max="5 3 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="3 2 4" max="4 3 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="5 2 4" max="6 3 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 2 4" max="5 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 5" max="5 3 6" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Copper Coil</BoxAnnotation>
  <BoxAnnotation min="4 1 2" max="5 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 1 3" max="4 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 1 3" max="6 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 3" max="5 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="2 1 4" max="3 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="6 1 4" max="7 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 1 4" max="6 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="3 1 5" max="4 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 1 5" max="6 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 1 5" max="5 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Titanium Block</BoxAnnotation>
  <BoxAnnotation min="4 1 6" max="5 2 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="8 0 0" max="9 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 0 0" max="6 1 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 0 1" max="8 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="6 0 1" max="7 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="5 0 1" max="6 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="4 0 1" max="5 1 2" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Railgun</BoxAnnotation>
  <BoxAnnotation min="3 0 1" max="4 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 2" max="8 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="7 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 3" max="2 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 3" max="9 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 3" max="7 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 0 4" max="2 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 4" max="9 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 4" max="4 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 0 4" max="7 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 0 4" max="5 1 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 5" max="2 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 5" max="9 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 5" max="7 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 6" max="2 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="7 0 6" max="8 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 6" max="7 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 0 7" max="2 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 0 7" max="8 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 0 7" max="3 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="6 0 7" max="7 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="3 0 7" max="6 1 8" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 0 8" max="1 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="8 0 8" max="9 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Steel Block</BoxAnnotation>
  <BoxAnnotation min="3 0 8" max="6 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
</GameScene>

This north-facing example is **9 × 11 × 9 blocks** (width × height × depth), containing **123 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Advanced Machine Structure | 32 |
| Copper Coil | 36 |
| Power Input Plug | 3 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 9 |
| Railgun | 1 |
| Steel Block | 8 |
| Titanium Block | 5 |
| Prismarine Slab | 26 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Railgun

<RecipeFor id="adv_rocketry:railgun" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Space stations](stations.md)

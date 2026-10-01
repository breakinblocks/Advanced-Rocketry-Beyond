---
navigation:
  title: Chemical Reactor
  icon: adv_rocketry:chemical_reactor
  position: 17
  parent: manufacturing.md
item_ids:
  - adv_rocketry:chemical_reactor
---

# Chemical Reactor

Combines items and fluids into fuels, chemicals and advanced materials. Supply different fluids through separate inputs and clear every output. It also applies Airtight Seal to ordinary armor; see [Armor sealing](armor_sealing.md).

Follow the frame below and connect its [ports](construction.md). Use the projector for layer-by-layer placement.

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/chemical_reactor.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Chemical Reactor</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 1 1" max="3 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="2 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="2 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="2 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 2 × 2 blocks** (width × height × depth), containing **10 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 2 |
| Fluid Input Hatch | 2 |
| Fluid Output Hatch | 2 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Motor | 1 |
| Chemical Reactor | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Chemical Reactor

<RecipeFor id="adv_rocketry:chemical_reactor" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing recipes

<MachineRecipes machine="chemical_reactor" kind="processing" />

[Back to Manufacturing](manufacturing.md)

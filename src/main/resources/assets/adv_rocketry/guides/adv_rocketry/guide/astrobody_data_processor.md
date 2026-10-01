---
navigation:
  title: Astrobody Data Processor
  icon: adv_rocketry:astrobody_data_processor
  position: 52
  parent: satellites.md
item_ids:
  - adv_rocketry:astrobody_data_processor
---

# Astrobody Data Processor

Insert a programmed Asteroid Chip and supply FE plus composition, distance and mass data through the buses.

Follow the menu's requirements and progress. Excess data of one type does not replace a missing type. Retrieve the researched chip from the output and install it in the [mining rocket](asteroid_mining.md).

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/astrobody_data_processor.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Astrobody Data Processor</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="3 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Prismarine Slab</BoxAnnotation>
  <BoxAnnotation min="2 0 0" max="3 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="2 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="3 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
</GameScene>

This north-facing example is **3 × 2 × 2 blocks** (width × height × depth), containing **12 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Astrobody Data Processor | 1 |
| Power Input Plug | 1 |
| Input Hatch | 1 |
| Output Hatch | 1 |
| Data Bus | 3 |
| Prismarine Slab | 5 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Astrobody Data Processor

<RecipeFor id="adv_rocketry:astrobody_data_processor" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

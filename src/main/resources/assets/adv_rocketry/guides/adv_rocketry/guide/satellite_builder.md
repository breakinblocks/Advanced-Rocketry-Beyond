---
navigation:
  title: Build and deploy satellites
  icon: adv_rocketry:satellite_builder
  position: 48
  parent: satellites.md
item_ids:
  - adv_rocketry:satellite_builder
  - adv_rocketry:satellite
  - adv_rocketry:satellite_id_chip
  - adv_rocketry:battery
  - adv_rocketry:advanced_battery
  - adv_rocketry:basic_satellite_solar_panel
  - adv_rocketry:large_satellite_solar_panel
---

# Build and deploy satellites

Place a Power Input Plug below the builder. Insert the chosen satellite controller component, compatible power, battery and data modules, and a blank Satellite ID Chip.

Assembly takes **100 powered ticks at 10 FE/t**. The chassis retains its modules. Basic and advanced batteries add **10,000 FE** and **40,000 FE** storage respectively, in addition to base capacity.

Keep the programmed chip. Put the completed Satellite in a rocket's Satellite Hatch and launch it. Deployed data satellites collect over time; power and storage constrain operation.

The builder also copies programmed satellite, station, planet and ore-scanner identifiers onto matching blank items. Keep backups. Continue with [satellite types](satellite_types.md), [microwave power](microwave_receiver.md) or [biome changes](biome_changing.md).

## Build example

<GameScene zoom="3.20" interactive={true}>
  <ImportStructure src="structures/satellite_builder.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="0 1 0" max="1 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Satellite Builder</BoxAnnotation>
  <BoxAnnotation min="0 0 0" max="1 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
</GameScene>

This north-facing example is **1 × 2 × 1 blocks** (width × height × depth), containing **2 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Satellite Builder | 1 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Satellite Builder

<RecipeFor id="adv_rocketry:satellite_builder" fallbackText="No crafting recipe is enabled for this item on this server." />

### Satellite

<RecipeFor id="adv_rocketry:satellite" fallbackText="No crafting recipe is enabled for this item on this server." />

### Satellite Id Chip

<RecipeFor id="adv_rocketry:satellite_id_chip" fallbackText="No crafting recipe is enabled for this item on this server." />

### Battery

<RecipeFor id="adv_rocketry:battery" fallbackText="No crafting recipe is enabled for this item on this server." />

### Advanced Battery

<RecipeFor id="adv_rocketry:advanced_battery" fallbackText="No crafting recipe is enabled for this item on this server." />

### Basic Satellite Solar Panel

<RecipeFor id="adv_rocketry:basic_satellite_solar_panel" fallbackText="No crafting recipe is enabled for this item on this server." />

### Large Satellite Solar Panel

<RecipeFor id="adv_rocketry:large_satellite_solar_panel" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

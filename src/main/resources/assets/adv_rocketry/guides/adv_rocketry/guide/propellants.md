---
navigation:
  title: Engines and propellants
  icon: adv_rocketry:rocket_motor
  position: 35
  parent: rockets.md
item_ids:
  - adv_rocketry:rocket_motor
  - adv_rocketry:advanced_rocket_motor
  - adv_rocketry:bipropellant_rocket_motor
  - adv_rocketry:advanced_bipropellant_rocket_motor
  - adv_rocketry:nuclear_rocket_motor
  - adv_rocketry:fuel_tank
  - adv_rocketry:bipropellant_fuel_tank
  - adv_rocketry:oxidizer_fuel_tank
  - adv_rocketry:nuclear_fuel_tank
  - adv_rocketry:nuclear_core
  - adv_rocketry:fueling_station
---

# Engines and propellants

Match tanks to the engine fuel family. Monopropellant engines use rocket fuel by default. Bipropellant engines need fuel and oxidizer, while nuclear engines have separate propellant rules. Servers can change accepted fluids and their fuel value.

Check the rocket's scan and fuel display. More tanks add capacity and mass; more engines add thrust and demand. Gravity, destination and server settings affect the required fuel.

The Fueling Station fills a nearby or linked rocket at **10 mB per tick for 30 FE** and emits redstone when full. Supply compatible fluid and power, using separate locked pipe networks where appropriate. Hand fueling is server-configurable.

Produce gases in the [Electrolyzer](electrolyzer.md) and fuel in the [Chemical Reactor](chemical_reactor.md). Keep return fuel and life-support oxygen in reserve.

## Crafting

### Rocket Motor

<RecipeFor id="adv_rocketry:rocket_motor" fallbackText="No crafting recipe is enabled for this item on this server." />

### Advanced Rocket Motor

<RecipeFor id="adv_rocketry:advanced_rocket_motor" fallbackText="No crafting recipe is enabled for this item on this server." />

### Bipropellant Rocket Motor

<RecipeFor id="adv_rocketry:bipropellant_rocket_motor" fallbackText="No crafting recipe is enabled for this item on this server." />

### Advanced Bipropellant Rocket Motor

<RecipeFor id="adv_rocketry:advanced_bipropellant_rocket_motor" fallbackText="No crafting recipe is enabled for this item on this server." />

### Nuclear Rocket Motor

<RecipeFor id="adv_rocketry:nuclear_rocket_motor" fallbackText="No crafting recipe is enabled for this item on this server." />

### Fuel Tank

<RecipeFor id="adv_rocketry:fuel_tank" fallbackText="No crafting recipe is enabled for this item on this server." />

### Bipropellant Fuel Tank

<RecipeFor id="adv_rocketry:bipropellant_fuel_tank" fallbackText="No crafting recipe is enabled for this item on this server." />

### Oxidizer Fuel Tank

<RecipeFor id="adv_rocketry:oxidizer_fuel_tank" fallbackText="No crafting recipe is enabled for this item on this server." />

### Nuclear Fuel Tank

<RecipeFor id="adv_rocketry:nuclear_fuel_tank" fallbackText="No crafting recipe is enabled for this item on this server." />

### Fueling Station

<RecipeFor id="adv_rocketry:fueling_station" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing Nuclear Core

<MachineRecipes item="adv_rocketry:nuclear_core" kind="processing" />

[Back to Rockets and travel](rockets.md)

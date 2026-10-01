---
navigation:
  title: Atmosphere checks
  icon: adv_rocketry:atm_analyzer
  position: 32
  parent: life_support.md
item_ids:
  - adv_rocketry:atm_analyzer
  - adv_rocketry:atmosphere_detector
---

# Atmosphere checks

The Atmosphere Analyzer reports local atmosphere class, pressure and breathability. Outdoor conditions can differ from a sealed room.

The Atmosphere Detector emits redstone when adjacent air matches the selected class. Test its selection in the actual room before relying on its signal.

Planetary beds require breathable surroundings by default, and servers can restrict respawning on planets. Establish life support before relying on a bed.

Using the analyzer on an uncrewed gas-intake rocket cycles the selected gas for a [gas mission](gas_mining.md).

## Crafting

### Atmosphere Detector

<RecipeFor id="adv_rocketry:atmosphere_detector" fallbackText="No crafting recipe is enabled for this item on this server." />


## Processing Atm Analyzer

<MachineRecipes item="adv_rocketry:atm_analyzer" kind="processing" />

[Back to Life support](life_support.md)

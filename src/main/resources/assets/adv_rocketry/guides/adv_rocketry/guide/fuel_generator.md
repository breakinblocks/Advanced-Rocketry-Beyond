---
navigation:
  title: Fuel Generator
  icon: adv_rocketry:fuel_generator
  position: 24
  parent: automation.md
item_ids:
  - adv_rocketry:fuel_generator
---

# Fuel Generator

The Fuel Generator burns liquids for power between the Coal Generator and the Black-Hole Generator. Feed its **16,000 mB tank** with fluid pipes or right-click with a filled bucket. Use an empty bucket to recover unused liquid. Energy cables can extract power, and adjacent consumers receive it directly.

With the default recipes, **1,000 mB of rocket fuel** produces **360 FE/t for 1,200 ticks**, totaling **432,000 FE**. **1,000 mB of lava** produces **160 FE/t for 1,000 ticks**, totaling **160,000 FE**. JEI lists the server's fuel recipes, including datapack additions.

The generator stores **1,000,000 FE**. It consumes one recipe batch when a burn starts and pauses its remaining burn time when the buffer cannot hold another tick of power. Unused liquid stays in the tank; a liquid without a matching recipe does not burn. Empty the tank before switching to a different liquid.

## Crafting

<RecipeFor id="adv_rocketry:fuel_generator" fallbackText="No crafting recipe is enabled for this item on this server." />

[Back to Power and automation](automation.md)

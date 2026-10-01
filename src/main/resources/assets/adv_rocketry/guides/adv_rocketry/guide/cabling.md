---
navigation:
  title: Cables, pipes and conduits
  icon: adv_rocketry:energy_cable
  position: 25
  parent: automation.md
item_ids:
  - adv_rocketry:energy_cable
  - adv_rocketry:fluid_pipe
  - adv_rocketry:item_conduit
---

# Cables, pipes and conduits

Right-click a cable to configure its six faces. **Insert** sends into an adjacent machine; **Extract** takes from it. **Disabled** disconnects that face, including other cable sections. Faces start on Insert, so set the source face to Extract.

| Network | Rate |
| --- | --- |
| Energy | 5,000 FE/t per machine input and output connection |
| Fluid | 4,000 mB/t per connected network |
| Items | 16 items per second per connected network |

Two energy inputs and two outputs can sustain **10,000 FE/t**. One output connection still accepts at most 5,000 FE/t. Connected machines may impose lower limits.

**Round robin** distributes individual items among successive accepting destinations. Fill-first fills destinations in order. Click the ghost filter with an item to match its type and components without consuming it; click with an empty cursor to clear it. The filter applies to this conduit's machine connections.

Extraction can ignore redstone, require a signal, or require no signal. Check the source endpoint if connected cables remain idle.

## Fluid locks and buffers

Sneak-right-click a fluid pipe with a **filled bucket** to lock the connected loaded network to that fluid. The bucket is not consumed. The lock does not convert existing contents.

Sneak-right-click with an **empty bucket** to void all fluid buffers in that loaded network. This leaves connected machine tanks and the fluid lock unchanged. Use **Clear fluid lock** in the menu to remove the lock.

Merged incompatible locks or buffered fluids stop transfer until resolved. Check disconnected branches before reconnecting them.

Traveling bands indicate successful transfers. Undelivered payloads stay buffered. Routes only traverse loaded chunks and resume as connections load; they never force-load chunks. Split networks larger than **4,096 sections**.

Use a [pipe-sealer frame](habitats.md) where cables cross a pressurized wall.

## Crafting

### Energy Cable

<RecipeFor id="adv_rocketry:energy_cable" fallbackText="No crafting recipe is enabled for this item on this server." />

### Fluid Pipe

<RecipeFor id="adv_rocketry:fluid_pipe" fallbackText="No crafting recipe is enabled for this item on this server." />

### Item Conduit

<RecipeFor id="adv_rocketry:item_conduit" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Power and automation](automation.md)

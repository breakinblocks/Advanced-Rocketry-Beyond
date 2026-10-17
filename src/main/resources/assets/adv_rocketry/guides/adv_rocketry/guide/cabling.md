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

Two energy inputs and two outputs can sustain **10,000 FE/t**. One output connection still accepts at most 5,000 FE/t. Connected machines may impose lower limits. Large source inventories are scanned over multiple transfer opportunities: at most 256 slots or tanks per source attempt and 2,048 per network tick. Scanning resumes where it stopped, so a large inventory may take longer to find a matching resource.

**Round robin** distributes individual items among successive accepting destinations. Fill-first fills destinations in order. Click the ghost filter with an item to match its type and components without consuming it; click with an empty cursor to clear it. The filter applies to this conduit's machine connections.

Extraction can ignore redstone, require a signal, or require no signal. Check the source endpoint if connected cables remain idle.

## Side filters and routing

The first row changes connection modes. The direction buttons below it select the face to edit. Each face has nine ghost entries: click with a carried item or filled container, or drag an item or fluid from JEI. Nothing is consumed. Click an entry with an empty cursor to clear it. The single shared item slot is a shared filter; a configured side filter replaces it on that face.

**Filter off** accepts everything permitted by the shared filter. An enabled **Allowlist** accepts matching entries; an enabled **Denylist** rejects them. An empty allowlist blocks everything. **Exact** compares components as well as type; **Identity** compares the item or fluid type alone. To enter a tag, type a slot number and tag such as `1=c:ingots/iron`, then press **Tag**. Hover a paper entry to see its tag.

Higher **Priority** destinations receive first. Lower priorities receive any remainder. Source routing chooses order within a priority group: legacy order, nearest, furthest, random, or round robin. Nearest and furthest measure connected pipe hops. The shared item round-robin control distributes individual items; source-side round robin rotates the preferred destination between operations.

**Pull** controls extraction redstone on the selected face; **Push** controls insertion. **Cap** limits how much that source can extract in one operation, within the network's normal rate. It cycles from one through larger values to the normal maximum. **Channel** cycles uncolored (-1) and 16 dye channels. Only equal channels join, including uncolored: an uncolored run cannot bridge two colored runs. A dye also sets the clicked pipe's channel. A tagged wrench toggles the clicked face between disconnected and Insert.

## Retry delays, sensors and overflow

**Retry** is optional and starts off. When enabled, unsuccessful source attempts back off exponentially, independently on each face. The maximum interval is 80 ticks for items, 20 for fluids, and 4 for energy. Successful movement resets the delay. Configuration or capability changes wake sources; ordinary external inventory changes may wait for the next retry. Item pipes already check once per second, so the longer item delay trades recovery time for fewer failed scans.

**Sensor on** emits redstone strength 15 for 20 ticks after successful movement at that endpoint. Adjacent blocks can receive the signal. Keep sensor wiring separate from its own Pull/Push control when feedback is unwanted.

**Void off** is the default. Enabling **VOID ON** deliberately deletes matching overflow on an Insert face, only after eligible real receivers have been tried. Side filters and insertion redstone still apply. Disable it to retain undelivered payloads instead. Jade shows the channel, priority, recent transfer amount, retry state, and whether disposal is enabled without scanning inventories.

## Storage and crafting

Open **Storage** from an Item Conduit's menu. It lists inventories on eligible Extract faces, in pages of 36 entries. Search by name or `@mod`. Left-click a listing to withdraw up to a stack; right-click for one. Listings refresh while open, and a click checks actual available stock again. The displayed stack count is capped at 99. Large inventories are scanned over multiple ticks; the listing is limited to 4,096 distinct stacks.

**Deposit held** inserts the carried stack through eligible Insert faces. Source and destination filters and redstone still apply. These manual transactions are separate from automatic pipe cadence, share a limit of 256 inventory probes per tick in each open terminal, and preserve remainders. Depositing through the terminal never voids items.

Place withdrawn ingredients into the crafting grid, then take the result normally. **Refill grid** pulls more of each exact ingredient already in the grid, up to a stack per occupied cell. Crafting only uses collected ingredients; recipe container remainders follow normal crafting rules. Closing returns the crafting grid to your inventory or drops what cannot fit. Shift-clicking the crafting result is disabled; take it normally. Closed terminals do no inventory scanning.

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

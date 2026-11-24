---
navigation:
  title: Observatory
  icon: adv_rocketry:observatory
  position: 51
  parent: satellites.md
item_ids:
  - adv_rocketry:observatory
  - adv_rocketry:asteroid_chip
  - adv_rocketry:observatory_auto_programmer
---

# Observatory

Complete the telescope and keep its required sky view open. The service rim accepts energy inputs and data buses; this example includes both.

Supply distance data to find asteroid targets. With the telescope open, spend **500 FE** to program a chip for the selected target. Composition and mass data refine cargo estimates.

Research the chip in the [Astrobody Data Processor](astrobody_data_processor.md) before preparing a mining rocket.

## Automatic mining runs

Put an **Observatory Auto-Programmer** in the Auto slot and the Observatory programs any blank chip in the Chip slot once a second, for the selected target, at the usual 500 FE and data cost. Pipes and hoppers can only insert blank asteroid chips and only take programmed chips from the Out slot.

A mining rocket comes home with its chip wiped. A Rocket Guidance port beside the pad pushes the blank chip into a pipe or chest next to it, and inserting into the port loads a chip straight into the docked rocket. A loop that runs without you:

1. The Guidance port sends the returned blank chip to the Observatory.
2. The Observatory programs it and a pipe carries it back into the Guidance port.
3. A Rocket Item Unloader empties the cargo and a Fueling Station refuels the rocket.
4. Combine their redstone signals (cargo empty, fuel full, Guidance port no longer empty) into the Rocket Monitoring Station, which launches on a rising signal.

Some asteroids only appear while the right artifact sits in the Observatory's artifact slots. When a pack uses them, the slots show every artifact held plus one free slot, and scroll once there are more than seven. Artifacts are checked, not used up.

## Build example

<GameScene zoom="2.40" interactive={true}>
  <ImportStructure src="structures/observatory.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="1 4 1" max="2 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 4 1" max="4 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 4 1" max="3 5 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 4 2" max="4 5 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 4 3" max="4 5 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 1" max="4 4 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 3 2" max="2 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 3 2" max="4 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 3 2" max="3 4 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 3 3" max="4 4 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 2 0" max="4 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 1" max="1 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 1" max="5 3 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 2" max="1 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 2" max="5 3 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 2 3" max="1 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 2 3" max="5 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="2 2 3" max="3 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Glass</BoxAnnotation>
  <BoxAnnotation min="1 2 4" max="4 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 0" max="2 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="3 1 0" max="4 2 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Input Hatch</BoxAnnotation>
  <BoxAnnotation min="2 1 0" max="3 2 1" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Observatory</BoxAnnotation>
  <BoxAnnotation min="4 1 1" max="5 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Output Hatch</BoxAnnotation>
  <BoxAnnotation min="1 1 1" max="4 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 1" max="1 2 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="0 1 2" max="1 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 1 2" max="5 2 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 1 2" max="4 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 1 3" max="1 2 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 1 3" max="5 2 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 1 3" max="4 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 1 4" max="4 2 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 0" max="4 1 1" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="0 0 1" max="1 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 1" max="5 1 2" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 1" max="4 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="0 0 2" max="1 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 2" max="5 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 2" max="2 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="3 0 2" max="4 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="2 0 2" max="3 1 3" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Motor</BoxAnnotation>
  <BoxAnnotation min="0 0 3" max="1 1 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="4 0 3" max="5 1 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
  <BoxAnnotation min="1 0 3" max="4 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Structure Tower</BoxAnnotation>
  <BoxAnnotation min="1 0 4" max="4 1 5" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Data Bus</BoxAnnotation>
</GameScene>

This north-facing example is **5 × 5 × 5 blocks** (width × height × depth), containing **73 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Power Input Plug | 1 |
| Input Hatch | 2 |
| Output Hatch | 1 |
| Motor | 1 |
| Machine Structure | 37 |
| Data Bus | 19 |
| Observatory | 1 |
| Structure Tower | 8 |
| Glass | 3 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Observatory

<RecipeFor id="adv_rocketry:observatory" fallbackText="No crafting recipe is enabled for this item on this server." />

### Asteroid Chip

<RecipeFor id="adv_rocketry:asteroid_chip" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Satellites and research](satellites.md)

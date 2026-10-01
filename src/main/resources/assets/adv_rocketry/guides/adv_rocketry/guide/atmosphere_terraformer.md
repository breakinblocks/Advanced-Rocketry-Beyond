---
navigation:
  title: Atmosphere Terraformer
  icon: adv_rocketry:atmosphere_terraformer
  position: 57
  parent: worlds.md
item_ids:
  - adv_rocketry:atmosphere_terraformer
---

# Atmosphere Terraformer

Complete the twelve-layer frame and link a biome-changing satellite. Supply nitrogen and oxygen through fluid inputs.

The default process uses **1,000 FE/t** over an **18,000-tick cycle**. Servers can change duration and fluid use. Inspect status for missing gases, satellite links or frame parts.

Keep a suit on and check the Atmosphere Analyzer as pressure changes. Terraforming retains the original pressure classification for ore-generation rules.

## Build example

<GameScene zoom="0.71" interactive={true}>
  <ImportStructure src="structures/atmosphere_terraformer.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <BoxAnnotation min="8 11 4" max="9 12 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="5 11 5" max="6 12 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="11 11 5" max="12 12 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="6 11 5" max="11 12 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 11 6" max="12 12 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 11 7" max="12 12 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 11 8" max="5 12 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="12 11 8" max="13 12 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="5 11 8" max="12 12 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 11 9" max="12 12 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 11 10" max="12 12 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 11 11" max="6 12 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="11 11 11" max="12 12 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="6 11 11" max="11 12 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 11 12" max="9 12 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Oxygen Vent</BoxAnnotation>
  <BoxAnnotation min="8 10 3" max="9 11 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 10 4" max="11 11 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 10 5" max="12 11 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 10 6" max="13 11 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 10 7" max="13 11 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 10 8" max="14 11 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 10 9" max="13 11 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 10 10" max="13 11 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 10 11" max="12 11 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 10 12" max="11 11 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 10 13" max="9 11 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 9 3" max="11 10 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 9 4" max="12 10 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 9 5" max="13 10 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 9 6" max="14 10 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 9 7" max="14 10 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 9 8" max="14 10 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 9 9" max="14 10 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 9 10" max="14 10 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 9 11" max="13 10 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 9 12" max="12 10 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 9 13" max="11 10 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 8 1" max="8 9 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 1" max="10 9 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 8 2" max="8 9 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 2" max="10 9 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="6 8 3" max="7 9 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 8 3" max="9 9 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="10 8 3" max="11 9 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 8 3" max="8 9 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 3" max="10 9 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="5 8 4" max="12 9 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 8 5" max="13 9 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 8 6" max="14 9 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 8 7" max="4 9 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="13 8 7" max="16 9 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="4 8 7" max="13 9 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 8 8" max="14 9 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="1 8 9" max="4 9 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="13 8 9" max="16 9 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="4 8 9" max="13 9 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 8 10" max="14 9 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 8 11" max="13 9 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 8 12" max="12 9 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 8 13" max="7 9 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 8 13" max="9 9 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="10 8 13" max="11 9 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 8 13" max="8 9 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 13" max="10 9 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 8 14" max="8 9 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 14" max="10 9 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 8 15" max="8 9 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 8 15" max="10 9 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 7 0" max="8 8 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 7 0" max="10 8 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 7 1" max="8 8 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 7 1" max="10 8 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="6 7 3" max="11 8 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 7 4" max="12 8 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 7 5" max="13 8 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 7 6" max="14 8 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 7 7" max="2 8 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="15 7 7" max="17 8 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 7 7" max="14 8 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 7 8" max="14 8 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 7 9" max="2 8 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="15 7 9" max="17 8 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 7 9" max="14 8 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="3 7 10" max="14 8 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="4 7 11" max="13 8 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="5 7 12" max="12 8 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="6 7 13" max="11 8 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 7 15" max="8 8 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 7 15" max="10 8 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 7 16" max="8 8 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 7 16" max="10 8 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 6 0" max="8 7 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 6 0" max="10 7 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 6 1" max="8 7 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 6 1" max="10 7 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="0 6 7" max="2 7 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="15 6 7" max="17 7 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 6 7" max="10 7 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 6 8" max="10 7 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 6 9" max="2 7 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="15 6 9" max="17 7 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 6 9" max="10 7 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 6 15" max="8 7 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 6 15" max="10 7 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 6 16" max="8 7 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 6 16" max="10 7 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 5 0" max="8 6 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 5 0" max="10 6 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="0 5 7" max="1 6 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 5 7" max="17 6 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 5 7" max="10 6 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 5 8" max="10 6 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 5 9" max="1 6 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 5 9" max="17 6 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 5 9" max="10 6 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 5 16" max="8 6 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 5 16" max="10 6 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 4 0" max="8 5 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 4 0" max="10 5 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="0 4 7" max="1 5 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 4 7" max="17 5 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="8 4 7" max="10 5 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 4 8" max="10 5 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="0 4 9" max="1 5 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 4 9" max="17 5 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 4 9" max="10 5 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 4 16" max="8 5 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 4 16" max="10 5 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 3 0" max="8 4 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 3 0" max="10 4 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="0 3 7" max="1 4 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 3 7" max="17 4 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 3 7" max="10 4 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="7 3 8" max="8 4 9" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="9 3 8" max="10 4 9" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="8 3 8" max="9 4 9" color="#50d8c2" thickness="0.75" alwaysOnTop={true}>Atmosphere Terraformer</BoxAnnotation>
  <BoxAnnotation min="0 3 9" max="1 4 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 3 9" max="17 4 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 3 9" max="8 4 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="9 3 9" max="10 4 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Advanced Machine Structure</BoxAnnotation>
  <BoxAnnotation min="8 3 9" max="9 4 10" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Power Input Plug</BoxAnnotation>
  <BoxAnnotation min="7 3 16" max="8 4 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 3 16" max="10 4 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 2 0" max="8 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 2 0" max="10 3 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="6 2 3" max="11 3 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="5 2 4" max="7 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 2 4" max="12 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 2 4" max="10 3 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="4 2 5" max="7 3 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 2 5" max="13 3 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 2 5" max="10 3 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 2 6" max="14 3 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="0 2 7" max="1 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 2 7" max="17 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 2 7" max="4 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 2 7" max="11 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 2 7" max="14 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 2 7" max="6 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 2 7" max="13 3 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 2 8" max="4 3 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 2 8" max="11 3 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 2 8" max="14 3 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 2 8" max="6 3 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 2 8" max="13 3 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="0 2 9" max="1 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="16 2 9" max="17 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 2 9" max="4 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 2 9" max="11 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 2 9" max="14 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 2 9" max="6 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 2 9" max="13 3 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 2 10" max="14 3 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 2 11" max="7 3 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 2 11" max="13 3 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 2 11" max="10 3 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="5 2 12" max="7 3 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 2 12" max="12 3 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 2 12" max="10 3 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 2 13" max="11 3 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 2 16" max="8 3 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 2 16" max="10 3 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 1 0" max="8 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 0" max="10 2 1" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 1 1" max="8 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 1" max="10 2 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 1 2" max="8 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 2" max="10 2 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="6 1 3" max="11 2 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="5 1 4" max="7 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 1 4" max="12 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 1 4" max="10 2 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="4 1 5" max="7 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 1 5" max="13 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 1 5" max="10 2 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 1 6" max="14 2 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="0 1 7" max="3 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="14 1 7" max="17 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 1 7" max="4 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 1 7" max="11 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 1 7" max="14 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 1 7" max="6 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 1 7" max="13 2 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 1 8" max="4 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 1 8" max="11 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 1 8" max="14 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 1 8" max="6 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 1 8" max="13 2 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="0 1 9" max="3 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="14 1 9" max="17 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="3 1 9" max="4 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="6 1 9" max="11 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="13 1 9" max="14 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 1 9" max="6 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 1 9" max="13 2 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 1 10" max="14 2 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 1 11" max="7 2 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 1 11" max="13 2 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 1 11" max="10 2 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="5 1 12" max="7 2 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 1 12" max="12 2 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 1 12" max="10 2 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 1 13" max="11 2 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 1 14" max="8 2 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 14" max="10 2 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 1 15" max="8 2 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 15" max="10 2 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 1 16" max="8 2 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 1 16" max="10 2 17" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 0 1" max="8 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 1" max="10 1 2" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 0 2" max="8 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 2" max="10 1 3" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="6 0 3" max="7 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 3" max="11 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 3" max="8 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 3" max="10 1 4" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="8 0 3" max="9 1 4" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="5 0 4" max="7 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 4" max="12 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 4" max="10 1 5" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="4 0 5" max="7 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 5" max="13 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 5" max="10 1 6" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="3 0 6" max="14 1 7" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="1 0 7" max="4 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="13 0 7" max="16 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="4 0 7" max="6 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 0 7" max="13 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 0 7" max="11 1 8" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="3 0 8" max="4 1 9" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="13 0 8" max="14 1 9" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="4 0 8" max="6 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 0 8" max="13 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 0 8" max="11 1 9" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="1 0 9" max="4 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="13 0 9" max="16 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="4 0 9" max="6 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="11 0 9" max="13 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 0 9" max="11 1 10" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="3 0 10" max="14 1 11" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="4 0 11" max="7 1 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 11" max="13 1 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 11" max="10 1 12" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="5 0 12" max="7 1 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 12" max="12 1 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 12" max="10 1 13" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Fuel Tank</BoxAnnotation>
  <BoxAnnotation min="6 0 13" max="7 1 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="10 0 13" max="11 1 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Concrete</BoxAnnotation>
  <BoxAnnotation min="7 0 13" max="8 1 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 13" max="10 1 14" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="8 0 13" max="9 1 14" color="#f0bd66" thickness="0.75" alwaysOnTop={true}>Fluid Input Hatch</BoxAnnotation>
  <BoxAnnotation min="7 0 14" max="8 1 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 14" max="10 1 15" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="7 0 15" max="8 1 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
  <BoxAnnotation min="9 0 15" max="10 1 16" color="#91a7b8" thickness="0.75" alwaysOnTop={true}>Clay</BoxAnnotation>
</GameScene>

This north-facing example is **17 × 12 × 17 blocks** (width × height × depth), containing **863 blocks**. Teal marks the controller and gold marks service parts. Rotate to inspect the back.

### Example materials

| Component | Count |
| --- | --- |
| Atmosphere Terraformer | 1 |
| Advanced Machine Structure | 432 |
| Power Input Plug | 3 |
| Fluid Input Hatch | 4 |
| Concrete | 207 |
| Fuel Tank | 72 |
| Oxygen Vent | 8 |
| Clay | 136 |

Keep the shown air spaces empty. The projector lists allowed substitutions and optional extensions; these counts describe this example, not every possible build.

## Crafting

### Atmosphere Terraformer

<RecipeFor id="adv_rocketry:atmosphere_terraformer" fallbackText="No crafting recipe is enabled for this item on this server." />


[Back to Planets and equipment](worlds.md)

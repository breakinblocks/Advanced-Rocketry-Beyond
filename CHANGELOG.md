# Changelog

## Unreleased

### Changed

- New art by xMausie for many blocks: the Coal Generator, Fuel Generator, Liquid Tank, Seat and Wireless Transceiver have new models, and the Carbon Scrubber, Fueling Station, Guidance Computer, Monitoring Station, Observatory, Oxygen Charger, Rocket Builder, Rocket Item Loader and Unloader, Satellite Hatch, Station Altitude Controller, Station Builder, Suit Workstation and Warp Controller have new textures on every side. The Item Input and Output ports, Oxygen Vent and Structure Block have new connected textures, and the Structure Block is now an open frame you can see through. The Airlock Door, space suit helmet, hovercraft and the energy cable, fluid pipe and item conduit items are redrawn too, and the Airlock Door has a glass window
- JEI has Asteroid Mining, Gas Giant Harvesting and Rocket Propellant pages. Asteroid pages show each asteroid's expected yield per mission, its ore share, mass and mission time, and any required artifacts. Gas pages show which gas giants carry each gas, by gravity and chance, and the harvest time per intake. Propellant pages show each fluid's fuel value for each tank and engine type. All three read the server's data, so datapack changes show up (FTBTesting/Testing-Issues#5242)
- The Oxygen Charger also fills chestplates from other mods that hold oxygen, such as the NauTec Diving Chestplate
- The Launch Pad has a new smooth concrete texture with a raised kerb and a yellow edge line, and it now connects to neighbouring Launch Pads so a whole pad shows one border around its outside edge and around any gaps
- The Airlock Door now slides open and shut like a sci-fi door with its own whoosh sound, instead of swinging like a vanilla door. It opens with redstone only
- Space stations now have zero gravity. Hold jump to drift up and sneak to drift down
- Rocket destinations now only list planets in the star system you are launching from. With no chip in the guidance computer, a rocket launching from a station heads for the planet the station orbits
- Faster sky rendering on planets and in space, with a lower render-thread cost for the atmosphere and cloud layers
- Planet definitions take `structure_sets`, which limits a planet with `structures` to the listed structure sets or tags, and `reservoirs`, which places sealed underground fluid pockets
- The space suit HUD no longer shows the panel listing worn suit pieces and their installed modules, which covered the top left of the screen and overlapped other mods such as FTB Ultimine. The oxygen gauge and atmosphere readout are unchanged

### Fixed

- A space suit helmet and chestplate with oxygen in the tanks now let you breathe underwater, using the suit's oxygen instead of letting you drown
- Rockets no longer block clicks on the blocks around them. Their hitbox now fits the rocket's own blocks instead of the whole launch pad area it was built on
- Launching a rocket from a station in another star system no longer fails with "Interstellar travel requires a warp-capable station" when no destination was picked
- Machines, orbital blocks, motors, generators, tanks, rocket loaders and life support blocks now mine at pickaxe speed and show the right tool, and leaves mine faster with a hoe (FTBTesting/Testing-Issues#5230)
- The Warp Core guide page now shows titanium blocks in the two centre spots, matching what the structure needs, instead of gold blocks (FTBTesting/Testing-Issues#5231)
- A formed Warp Core keeps its block texture instead of leaving a see-through hole in the frame, and formed machine controllers no longer hide the faces of the blocks next to them (FTBTesting/Testing-Issues#5232)
- The Atmosphere Terraformer guide page now matches the current structure, with concrete instead of clay
- Clouds no longer render in space or on planets with thin atmospheres, including with shader packs
- Right-clicking a Launch Pad, Landing Pad, rocket part, pipe or cable while holding a block now places the block instead of needing a sneak-click
- Landing the hovercraft no longer deals fall damage to the rider (FTBTesting/Testing-Issues#5210)
- Seats, rocket motors and fuel tanks no longer hide the faces of the blocks next to them, which made nearby blocks see-through

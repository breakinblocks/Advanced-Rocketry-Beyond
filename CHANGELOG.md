# Changelog

## Unreleased

### Changed

- The Launch Pad has a new smooth concrete texture with a raised kerb and a yellow edge line, and it now connects to neighbouring Launch Pads so a whole pad shows one border around its outside edge and around any gaps
- The Airlock Door now slides open and shut like a sci-fi door with its own whoosh sound, instead of swinging like a vanilla door. It opens with redstone only
- Space stations now have zero gravity. Hold jump to drift up and sneak to drift down
- Rocket destinations now only list planets in the star system you are launching from. With no chip in the guidance computer, a rocket launching from a station heads for the planet the station orbits
- Faster sky rendering on planets and in space, with a lower render-thread cost for the atmosphere and cloud layers

### Fixed

- Launching a rocket from a station in another star system no longer fails with "Interstellar travel requires a warp-capable station" when no destination was picked
- Machines, orbital blocks, motors, generators, tanks, rocket loaders and life support blocks now mine at pickaxe speed and show the right tool, and leaves mine faster with a hoe (FTBTesting/Testing-Issues#5230)
- The Warp Core guide page now shows titanium blocks in the two centre spots, matching what the structure needs, instead of gold blocks (FTBTesting/Testing-Issues#5231)
- The Atmosphere Terraformer guide page now matches the current structure, with concrete instead of clay
- Clouds no longer render in space or on planets with thin atmospheres, including with shader packs
- Right-clicking a Launch Pad, Landing Pad, rocket part, pipe or cable while holding a block now places the block instead of needing a sneak-click

- Landing the hovercraft no longer deals fall damage to the rider (FTBTesting/Testing-Issues#5210)
- Seats, rocket motors and fuel tanks no longer hide the faces of the blocks next to them, which made nearby blocks see-through

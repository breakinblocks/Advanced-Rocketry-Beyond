# Remote operation and chunk loading

Automatic chunk loading is enabled by default. Server owners can control it in the world's `serverconfig/adv_rocketry-server.toml`:

```toml
[chunkLoading]
    enabled = true
    rockets = true
    elevators = true
    orbitalMachines = true
    remoteTargets = true
    missionReturns = true
    biomeJobs = true
    wireless = true
```

The master switch overrides every category. These settings control chunk loading; they do not bypass energy, inventory, structure, orbit or mission requirements.

| Setting | Coverage |
| --- | --- |
| `rockets` | Moving rockets keep their current chunk ticking, including restoration after a server restart. |
| `elevators` | Linked, formed elevator anchors, partner destinations and moving capsules. |
| `orbitalMachines` | Formed solar arrays, black hole generators, linked microwave receivers and railguns; active orbital lasers, terraformers, satellite construction and discovery; astrobody processors, warp cores, station motion controllers and powered force fields. |
| `remoteTargets` | Railgun destination areas and the orbital laser's current terrain shaft. |
| `missionReturns` | Landing areas needed to deliver completed asteroid and gas missions. |
| `biomeJobs` | The current cell of a satellite biome-changing job. |
| `wireless` | Linked wireless transceivers whose mode is not OFF. |

Machines keep their controller, multiblock footprint and immediately adjacent service connections loaded. Force-field projectors cover their field path. Each owner has independent tickets, so breaking one machine does not unload another machine sharing its chunks. Tickets are released when the owner is removed or ceases to qualify. Moving vehicles release tickets when they stop or are destroyed.

Persistent machine and vehicle tickets are restored in normal and dynamically created planet/station dimensions. Remote work uses temporary ticking leases, renewed only while needed and expiring 60 ticks after the final request. Disabling a category releases its persistent tickets during maintenance; existing temporary leases expire normally. Disabled categories do not prevent operation in chunks loaded by players or another loader. Waiting mission returns and biome jobs retain their progress until their targets are available.

Satellite power and research are galaxy saved data, not entities in an orbital chunk. They advance on the server without a terminal being open or any satellite chunk being loaded. Horizon modules likewise use satellite records. Station warp countdowns and mission timers also run from saved data; only work that touches blocks or creates a returning rocket needs chunks. None of these systems simulates time while the server is offline.

This does not load an entire planet, station or attached factory. Long pipe networks and distant external power supplies may need their own loaders. Inactive ordinary processing machines and starter generators do not acquire tickets. Existing eligible infrastructure acquires its first ticket when loaded; after switching loading back on, previously unloaded infrastructure may need a player visit or an external loader to reactivate it.

## Verification

The isolated integration world runs with Iris and Complementary Unbound + Euphoria enabled, with the player remaining in the overworld and test infrastructure on a distant dynamic planet. Checks cover default settings, overlapping owners, removal, master/category switches, temporary lease expiry, satellite research without a terminal, delayed and successful gas-mission returns, remote biome-job completion, and saved machine/rocket recovery after restart. The check harness is a local tool and is excluded from the release jar. Railgun, elevator and wireless call sites were reviewed against the shared ticket lifecycle; their complete gameplay flows are not covered by this integration scenario.

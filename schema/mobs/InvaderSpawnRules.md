# Description
Invader Spawn Rules hold the per-dimension spawn rules for [Invaders](./Invader.md).  

They are supplied through the `apotheosis:invader_spawn_rules` data map, which is keyed by dimension type. Invaders can only spawn in dimensions that have an entry in this data map.  

Entries are placed in the file `data/<namespace>/data_maps/dimension_type/invader_spawn_rules.json`, following the standard NeoForge data map format.

# Dependencies
This object references the following objects:
1. [WorldTier](../tier/WorldTier.md)
2. [SurfaceType](../util/SurfaceType.md)

# Schema
```js
{
    "spawn_chances": {          // [Mandatory] || Per-tier chances that an eligible natural spawn is replaced with an invader. All five world tiers must be specified. Range: [0, 1].
        WorldTier: float
    },
    "surface_type": SurfaceType // [Mandatory] || The surface check applied to the target spawn position.
    "cooldown": integer,        // [Optional]  || A per-dimension cooldown (in ticks) between invader spawns. Defaults to the cooldown from the Apotheosis config. Range: [0, 720000].
    "cursed": integer,          // [Optional]  || A per-dimension override for the 'Curse Boss Items' config option.
    "auto_aggro": integer,      // [Optional]  || A per-dimension override for the 'Boss Auto-Aggro' config option.
}
```

# Examples
Spawn rules for the overworld and the Twilight Forest. The twilight entry uses a NeoForge loading condition, so it only applies when the mod is installed.

```json
{
    "values": {
        "minecraft:overworld": {
            "spawn_chances": {
                "haven": 0.0,
                "frontier": 0.018,
                "ascent": 0.02,
                "summit": 0.025,
                "pinnacle": 0.03
            },
            "surface_type": "needs_sky_or_same_vertical_slice"
        },
        "twilightforest:twilight_forest_type": {
            "neoforge:conditions": [
                {
                    "type": "neoforge:mod_loaded",
                    "modid": "twilightforest"
                }
            ],
            "spawn_chances": {
                "haven": 0.0,
                "frontier": 0.05,
                "ascent": 0.053,
                "summit": 0.06,
                "pinnacle": 0.063
            },
            "surface_type": "needs_surface"
        }
    }
}
```

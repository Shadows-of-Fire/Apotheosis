# Description
A Rogue Spawner is a preconfigured mob spawner that generates randomly underground during world generation.  

When placed, the spawner receives the configured stats and spawn potentials, a chest with the specified loot table is placed below it, a random cover block from the `#apotheosis:rogue_spawner_covers` block tag is placed above it, and vines are placed on any exposed sides.  

There is also a configurable chance for the chest to use the valuable loot table (`apotheosis:chests/chest_valuable`) instead of the spawner's own loot table.

# Dependencies
This object references the following objects:
1. [PresetSpawnerStats](./PresetSpawnerStats.md)

# Schema
```js
{
    "weight": integer,            // [Mandatory] || Weight (relative to other rogue spawners) of this object.
    "stats": PresetSpawnerStats,  // [Mandatory] || The spawner stats applied to the placed spawner.
    "loot_table": "string",       // [Mandatory] || Registry name of the loot table used for the chest below the spawner.
    "spawn_potentials": [         // [Mandatory] || A weighted list of possible spawns. Must not be empty.
        {
            "weight": integer,    // [Mandatory] || Weight of this entry, relative to other entries in the list.
            "data": SpawnData     // [Mandatory] || A vanilla SpawnData object, holding the entity to spawn and optional custom spawn rules.
        }
    ]
}
```

Note: Rogue Spawners are selected during world generation, so their weights are not tier-dependent, and luck does not apply.

# Examples
A swarm spawner which spawns groups of speedy spiders, using the swarm loot table.

```json
{
    "type": "apotheosis:rogue_spawner",
    "loot_table": "apotheosis:chests/spawner_swarm",
    "spawn_potentials": [
        {
            "data": {
                "entity": {
                    "active_effects": [
                        {
                            "duration": 10000000,
                            "amplifier": 0,
                            "id": "minecraft:speed",
                            "show_particles": 0,
                            "visible": 0
                        }
                    ],
                    "id": "minecraft:spider"
                }
            },
            "weight": 10
        }
    ],
    "stats": {
        "apothic_spawners:max_delay": 125,
        "apothic_spawners:max_nearby_entities": 10,
        "apothic_spawners:min_delay": 50,
        "apothic_spawners:req_player_range": 14,
        "apothic_spawners:spawn_count": 2,
        "apothic_spawners:spawn_range": 7
    },
    "weight": 30
}
```

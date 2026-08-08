# Description
Preset Spawner Stats are a collection of spawner stats, stored as raw values keyed by stat id, which are applied to a placed mob spawner.  

When Apothic Spawners is installed, stats are applied through its spawner stat registry, meaning all of its stats may be used here.  
Otherwise, only the stats corresponding to vanilla spawner fields are applied, and any others are skipped.

# Schema
Preset Spawner Stats do not have an explicit list of fields, as they are an unbounded map from stat id to stat value.

```js
{
    "string": value  // [Optional] || A map of spawner stat registry names to values. Values may be numbers or booleans, depending on the stat.
}
```

The stats corresponding to vanilla spawner fields, which are always available, are as follows:

1. `apothic_spawners:min_delay` - The minimum delay between spawn attempts, in ticks.
2. `apothic_spawners:max_delay` - The maximum delay between spawn attempts, in ticks.
3. `apothic_spawners:spawn_count` - The number of spawn attempts performed per activation.
4. `apothic_spawners:max_nearby_entities` - The maximum number of nearby entities before spawning is halted.
5. `apothic_spawners:req_player_range` - The distance (in blocks) a player must be within for the spawner to activate.
6. `apothic_spawners:spawn_range` - The horizontal radius (in blocks) in which mobs may be placed.

Additional stats, such as `apothic_spawners:youthful`, are only functional when Apothic Spawners is installed.

# Examples
Stats for a fast-cycling spawner that requires a player within 14 blocks, and spawns two mobs at a time.

```json
{
    "apothic_spawners:max_delay": 125,
    "apothic_spawners:max_nearby_entities": 10,
    "apothic_spawners:min_delay": 50,
    "apothic_spawners:req_player_range": 14,
    "apothic_spawners:spawn_count": 2,
    "apothic_spawners:spawn_range": 7
}
```

# Description
A Supporting Entity is an extra entity spawned alongside a boss, used for mounts and reinforcements in [BasicBossData](./BasicBossData.md).

# Dependencies
This object references the following objects:
1. [CompoundTag](../../../../../Placebo/blob/-/schema/CompoundTag.md)

# Schema
```js
{
    "entity": "string",  // [Mandatory] || Registry name of the entity type to spawn.
    "nbt": CompoundTag,  // [Optional]  || Entity NBT loaded onto the spawned entity. Default value = no NBT.
    "x": float,          // [Optional]  || X-offset from the spawn position. Default value = 0.
    "y": float,          // [Optional]  || Y-offset from the spawn position. Default value = 0.
    "z": float           // [Optional]  || Z-offset from the spawn position. Default value = 0.
}
```

# Examples
A perpetually angry bee, used as the mount for the Honeyed Archer elite.

```json
{
    "entity": "minecraft:bee",
    "nbt": {
        "AngerTime": 99999999,
        "CannotEnterHiveTicks": 999999
    }
}
```

# Description
A SupportingEntity defines an entity that can be spawned alongside a boss entity, either as a mount or as a supporting companion. It specifies the entity type, any NBT data to apply, and positional offsets.

# Dependencies
This object references the following objects:
1. [EntityType](../../../../../Minecraft/blob/-/schema/EntityType.md)
2. [CompoundTag](../../../../../Placebo/blob/-/schema/CompoundTag.md)

# Schema
```js
{
    "entity": EntityType,       // [Mandatory] || The registry name of the entity type to spawn.
    "nbt": CompoundTag,         // [Optional]  || Any NBT data to apply to the entity.
    "x": double,                // [Optional]  || The X offset from the spawn position. Defaults to 0.
    "y": double,                // [Optional]  || The Y offset from the spawn position. Defaults to 0.
    "z": double                 // [Optional]  || The Z offset from the spawn position. Defaults to 0.
}
```

# Examples

## Basic Supporting Entity
A basic enderman with no special properties.

```json
{
    "entity": "minecraft:enderman"
}
```

## Supporting Entity with NBT and Position
An enderman with glowing effect that spawns 3 blocks away from the boss.

```json
{
    "entity": "minecraft:enderman",
    "nbt": {
        "Glowing": 1,
        "CustomNameVisible": 1
    },
    "x": 3.0,
    "y": 0.0,
    "z": 0.0
}
```

## Mount Entity
A spider that the boss will ride.

```json
{
    "entity": "minecraft:spider",
    "nbt": {
        "Attributes": [
            {
                "Name": "minecraft:generic.movement_speed",
                "Base": 0.4
            }
        ]
    }
}
```

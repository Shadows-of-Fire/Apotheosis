# Description
A Spawn Condition is a check applied to a spawning mob, used to determine if certain effects will apply.  

These are used by Invaders and Elites to determine if they may spawn, as well as Augmentations to determine if they are applicable.  

# Dependencies
This object references the following objects:
1. [MobSpawnType](../util/MobSpawnType.md)
2. [SurfaceType](../util/SurfaceType.md)
3. [CompoundTag](../../../../../Placebo/blob/1.21/schema/CompoundTag.md)

# Subtypes
Spawn Conditions are subtyped, meaning each subtype declares a `"type"` key and its own parameters.

## Spawn Type Condition
Requires that the target mob have been spawned with any one of the specified spawn types.  

### Schema
```js
{
    "type": "apotheosis:spawn_type",
    "spawn_types": [  // [Mandatory] || The set of valid spawn types.
        MobSpawnType
    ]
}
```

### Examples
A condition which requires that the mob be from a spawn egg, or a spawner.

```json
{
    "type": "apotheosis:spawn_type",
    "spawn_types": [
        "spawn_egg",
        "spawner"
    ]
}
```

## Surface Type Condition
Requires that the entity be spawned with a specific surface type.  

### Schema
```js
{
    "type": "apotheosis:surface_type",
    "surface_type": SurfaceType     // [Mandatory] || The surface type.
}
```

### Examples
A condition which requires that the entity was spawned underground.

```json
{
    "type": "apotheosis:surface_type",
    "surface_type": "below_surface"
}
```

## Entity Tag Condition
Requires that the spawned entity have a specific entity type tag.  

### Schema
```js
{
    "type": "apotheosis:has_tag",
    "tag": "string", // [Mandatory] || The registry name of the tag.
}
```

### Examples
A condition which requires the entity be a member of `minecraft:skeletons`.

```json
{
    "type": "apotheosis:has_tag",
    "tag": "minecraft:skeletons"
}
```

## Is Monster Condition
Requires that the spawned entity be a monster, meaning it must derive from the vanilla Monster class.  

Note: Some hostile mobs, such as slimes and ghasts, do not derive from Monster, and will not pass this condition.

### Schema
```js
{
    "type": "apotheosis:is_monster"
}
```

## NBT Condition
Requires that the entity have some arbitrary NBT value.  

**Warning:** Execution of this rule is **very** expensive, as it requires serializing every entity to NBT on spawn.  
It may be preferrable to request a new spawn condition be added, instead of using this condition in production.

### Schema
```js
{
    "type": "apotheosis:nbt",
    "nbt": CompoundTag, // [Mandatory] || The required NBT data.
}
```

### Examples
A condition which requires the entity not be invulnerable.

```json
{
    "type": "apotheosis:nbt",
    "nbt": {
        "Invulnerable": false
    }
}
```

## And Condition
Makes a single condition which computes the logical-and of all child conditions.  
Typically, this is not necessary, since users of spawn conditions are expected to combine conditions using a logical and by default.

### Schema
```js
{
    "type": "apotheosis:and",
    "spawn_conditions": [     // [Mandatory] || The conditions to combine.
        SpawnCondition
    ]
}
```

## Or Condition
Makes a single condition which computes the logical-or of all child conditions.  

### Schema
```js
{
    "type": "apotheosis:or",
    "spawn_conditions": [     // [Mandatory] || The conditions to combine.
        SpawnCondition
    ]
}
```

## Not Condition
Makes a condition which inverts a single underlying condition.  

### Schema
```js
{
    "type": "apotheosis:not",
    "spawn_condition": SpawnCondition  // [Mandatory] || The condition to invert.
}
```

## Xor Condition
Makes a single condition which computes the logical-xor (exclusive or) of two conditions. 

### Schema
```js
{
    "type": "apotheosis:xor",
    "left": SpawnCondition,    // [Mandatory] || The left condition.
    "right": SpawnCondition    // [Mandatory] || The right condition.
}
```

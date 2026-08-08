# Description
An All Stats Bonus is a [Gem Bonus](./GemBonus.md) which applies the same modifier to every attribute in a list, using a single value and operation.  

This is intended for "+X% to all stats" style gems, so the operation is usually `add_multiplied_total` with a small value.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:all_stats",
    "gem_class": GemClass,   // [Mandatory] || The set of loot categories this bonus applies to.
    "operation": "string",   // [Mandatory] || The operation used for every modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
    "values": {              // [Mandatory] || The modifier value for each supported purity, used for every attribute.
        Purity: float
    },
    "attributes": HolderSet  // [Mandatory] || The attributes receiving the modifier. Accepts a list of attribute registry names, or a single #-prefixed tag name.
}
```

# Examples
An all stats bonus for helmets, granting a percentage increase to a set of attributes.

```json
{
    "type": "apotheosis:all_stats",
    "attributes": [
        "minecraft:generic.max_health",
        "minecraft:generic.movement_speed",
        "minecraft:generic.attack_damage",
        "minecraft:generic.armor",
        "apothic_attributes:crit_chance",
        "apothic_attributes:experience_gained"
    ],
    "gem_class": "apotheosis:helmet",
    "operation": "add_multiplied_total",
    "values": {
        "flawed": 0.05,
        "normal": 0.075,
        "flawless": 0.1,
        "perfect": 0.15
    }
}
```

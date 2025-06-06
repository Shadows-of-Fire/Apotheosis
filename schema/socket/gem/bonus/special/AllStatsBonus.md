# Description
AllStatsBonus is a special gem bonus that applies the same modifier value to multiple attributes simultaneously. This allows for creating gems that provide bonuses to a selected set of attributes using a consistent operation and value.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. Attribute - Minecraft's attribute registry entries

# Schema
```js
{
    "type": "apotheosis:all_stats_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,               // [Mandatory] || The item types this gem can be applied to
    "operation": string,                 // [Mandatory] || The attribute operation (ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL)
    "values": {                          // [Mandatory] || Per-purity bonus values
        Purity: float
    },
    "attributes": [                      // [Mandatory] || List of attributes to modify
        "attribute_id"
    ]
}
```

# Examples

## Combat Stats Gem
A gem that provides percentage-based bonuses to multiple combat attributes.

```json
{
    "type": "apotheosis:all_stats_bonus",
    "gem_class": {
        "key": "combat/melee",
        "types": ["sword", "axe"]
    },
    "operation": "MULTIPLY_BASE",
    "values": {
        "chipped": 0.05,
        "flawed": 0.075,
        "normal": 0.1,
        "flawless": 0.125,
        "perfect": 0.15
    },
    "attributes": [
        "minecraft:generic.attack_damage",
        "minecraft:generic.attack_speed",
        "minecraft:generic.movement_speed"
    ]
}
```

## Defensive Attributes Gem
A gem that provides bonuses to multiple defensive attributes.

```json
{
    "type": "apotheosis:all_stats_bonus",
    "gem_class": {
        "key": "combat/armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "operation": "MULTIPLY_TOTAL",
    "values": {
        "normal": 0.03,
        "flawless": 0.04,
        "perfect": 0.05
    },
    "attributes": [
        "minecraft:generic.armor",
        "minecraft:generic.armor_toughness",
        "minecraft:generic.max_health"
    ]
}
```

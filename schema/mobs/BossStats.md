# Description
Boss Stats hold everything that a boss might need to buff itself: enchantments for its gear, potion effects, and attribute modifiers.  

Effects are applied with infinite duration, unless the mob is a creeper, in which case the duration is reduced to five minutes.

# Dependencies
This object references the following objects:
1. [ChancedEffectInstance](../../../../../Placebo/blob/1.21/schema/ChancedEffectInstance.md)
2. [RandomAttributeModifier](../../../../../Placebo/blob/1.21/schema/RandomAttributeModifier.md)

# Schema
```js
{
    "enchant_chance": float,     // [Mandatory] || The chance that each item (other than the affix item) is enchanted.
    "enchantment_levels": {      // [Mandatory] || The enchanting levels used when enchanting the boss's items, comparable to levels used at an enchanting table.
        "primary": integer,      // [Mandatory] || The level used for the affix item.
        "secondary": integer     // [Mandatory] || The level used for all other items.
    },
    "effects": [                 // [Optional]  || Potion effects applied to the boss when spawned. Default value = empty list.
        ChancedEffectInstance
    ],
    "attribute_modifiers": [     // [Optional]  || Attribute modifiers applied to the boss when spawned. Default value = empty list.
        RandomAttributeModifier
    ]
}
```

# Examples
Stats which always enchant the boss's items with high-level enchantments, grant fire resistance, and provide randomized health and damage boosts.

```json
{
    "attribute_modifiers": [
        {
            "attribute": "minecraft:generic.max_health",
            "operation": "add_value",
            "value": {
                "min": 90.0,
                "max": 140.0
            }
        },
        {
            "attribute": "minecraft:generic.attack_damage",
            "operation": "add_multiplied_total",
            "value": {
                "min": 4.0,
                "max": 10.0
            }
        }
    ],
    "effects": [
        {
            "amplifier": 1.0,
            "effect": "minecraft:fire_resistance"
        }
    ],
    "enchant_chance": 1.0,
    "enchantment_levels": {
        "primary": 100,
        "secondary": 80
    }
}
```

# Description
BossStats defines the enhanced attributes, effects, and enchantment properties applied to boss entities in Apotheosis. These stats can be used to make boss entities more powerful by increasing their attributes, giving them potion effects, and enchanting their equipment.

# Dependencies
This object references the following objects:
1. [MobEffect](../../../../../Minecraft/blob/-/schema/MobEffect.md)
2. [Attribute](../../../../../Minecraft/blob/-/schema/Attribute.md)
3. [ChancedEffectInstance](../../../../../Placebo/blob/-/schema/ChancedEffectInstance.md)
4. [RandomAttributeModifier](../../../../../Placebo/blob/-/schema/RandomAttributeModifier.md)
5. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "enchant_chance": float,             // [Mandatory] || The chance (0-1) that boss items (aside from the affix item) are enchanted.
    "enchantment_levels": {              // [Mandatory] || The enchantment levels to use for the boss's items.
        "primary": int,                  // The enchantment level to use for the primary (affixed) item.
        "secondary": int                 // The enchantment level to use for all other items.
    },
    "effects": [                         // [Optional]  || List of potion effects that could be applied to this boss. Defaults to empty list.
        ChancedEffectInstance
    ],
    "attribute_modifiers": [             // [Optional]  || List of attribute modifiers to apply to this boss when spawned. Defaults to empty list.
        RandomAttributeModifier
    ]
}
```

# Examples

## Basic Melee Boss Stats
A set of stats for a basic melee boss with increased health, damage, and some resistance.

```json
{
    "enchant_chance": 0.5,
    "enchantment_levels": {
        "primary": 30,
        "secondary": 15
    },
    "effects": [
        {
            "chance": 1.0,
            "effect": "minecraft:resistance",
            "amplifier": 1
        }
    ],
    "attribute_modifiers": [
        {
            "attribute": "minecraft:generic.max_health",
            "operation": "add_value",
            "value": {
                "min": 20.0,
                "max": 40.0
            }
        },
        {
            "attribute": "minecraft:generic.attack_damage",
            "operation": "add_multiplied_base",
            "value": {
                "min": 0.5,
                "max": 0.8
            }
        },
        {
            "attribute": "minecraft:generic.knockback_resistance",
            "operation": "add_value",
            "value": 0.5
        }
    ]
}
```

## Advanced Ranged Boss Stats
A more complex set of stats for a high-tier ranged boss with multiple effects and attributes.

```json
{
    "enchant_chance": 1.0,
    "enchantment_levels": {
        "primary": 50,
        "secondary": 30
    },
    "effects": [
        {
            "chance": 1.0,
            "effect": "minecraft:speed",
            "amplifier": {
                "min": 1,
                "max": 2
            }
        },
        {
            "chance": 1.0,
            "effect": "minecraft:resistance",
            "amplifier": 2
        },
        {
            "chance": 0.5,
            "effect": "minecraft:invisibility",
            "amplifier": 0
        },
        {
            "chance": 0.3,
            "effect": "minecraft:fire_resistance",
            "amplifier": 0
        }
    ],
    "attribute_modifiers": [
        {
            "attribute": "minecraft:generic.max_health",
            "operation": "add_value",
            "value": {
                "min": 40.0,
                "max": 60.0
            }
        },
        {
            "attribute": "minecraft:generic.attack_damage",
            "operation": "add_multiplied_base",
            "value": {
                "min": 0.8,
                "max": 1.2
            }
        },
        {
            "attribute": "minecraft:generic.movement_speed",
            "operation": "add_multiplied_base",
            "value": {
                "min": 0.3,
                "max": 0.5
            }
        },
        {
            "attribute": "minecraft:generic.armor",
            "operation": "add_value",
            "value": 10.0
        },
        {
            "attribute": "minecraft:generic.armor_toughness",
            "operation": "add_value",
            "value": 6.0
        },
        {
            "attribute": "minecraft:generic.knockback_resistance",
            "operation": "add_value",
            "value": 0.8
        }
    ]
}
```

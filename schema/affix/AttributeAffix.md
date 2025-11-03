# Description
AttributeAffix is one of the most common Affix implementations, which adds attribute modifiers to items. These modifiers can enhance properties like attack damage, attack speed, armor, movement speed, etc. The strength of the attribute modification scales with the affix level.

# Dependencies
This object references the following objects:
1. [AffixDefinition](./AffixDefinition.md)
2. [LootCategory](../loot/LootCategory.md)
3. [LootRarity](../loot/LootRarity.md)
4. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:attribute",      // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "attribute": "string",              // [Mandatory] || The resource location of the attribute to modify
    "operation": Operation,              // [Mandatory] || The attribute modifier operation
    "values": {                          // [Mandatory] || A map of rarity IDs to the value functions for that rarity.
        LootRarity: StepFunction
    },
    "categories": [                      // [Mandatory] || The supported loot categories.
        LootCategory
    ]
}
```

The attribute operation (`operation`) must be one of the following values:
- `"add_value"` - Adds a flat value to the attribute
- `"add_multiplied_base"` - Adds a percentage of the base value
- `"add_multiplied_total"` - Adds a percentage of the total value (after other modifiers)

# Examples

## Basic Attack Damage Affix
A basic attribute affix that increases attack damage by a percentage.

```json
{
    "type": "apotheosis:attribute",
    "definition": {
        "affix_type": "stat",
        "exclusive_set": [],
        "weights": {
            "weight": 10,
            "quality": 1.0
        }
    },
    "attribute": "minecraft:generic.attack_damage",
    "operation": "add_multiplied_base",
    "values": {
        "common": {
            "min": 0.05,
            "max": 0.1
        },
        "uncommon": {
            "min": 0.1,
            "max": 0.15
        },
        "rare": {
            "min": 0.15,
            "max": 0.2
        },
        "epic": {
            "min": 0.2,
            "max": 0.25
        },
        "mythic": {
            "min": 0.25,
            "max": 0.3
        }
    },
    "categories": [
        "apotheosis:melee_weapon"
    ]
}
```

## Advanced Armor Toughness Affix
An attribute affix that adds flat armor toughness values to armor pieces.

```json
{
    "type": "apotheosis:attribute",
    "definition": {
        "affix_type": "stat",
        "exclusive_set": [
            "apotheosis:armor_toughness_lesser"
        ],
        "weights": {
            "ascent": {
                "weight": 5,
                "quality": 0.8
            },
            "summit": {
                "weight": 10,
                "quality": 1.0
            }
        }
    },
    "attribute": "minecraft:generic.armor_toughness",
    "operation": "add_value",
    "values": {
        "rare": {
            "min": 1.0,
            "max": 2.0
        },
        "epic": {
            "min": 2.0,
            "max": 3.0
        },
        "mythic": {
            "min": 3.0,
            "max": 4.0
        }
    },
    "categories": [
        "apotheosis:helmet",
        "apotheosis:chestplate",
        "apotheosis:leggings",
        "apotheosis:boots"
    ]
}
```
